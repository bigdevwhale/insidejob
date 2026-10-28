package app.insidejob.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.insidejob.domain.Box
import app.insidejob.domain.State
import app.insidejob.ui.Words
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.Hand
import app.insidejob.ui.theme.Mono
import app.insidejob.ui.theme.Title

/** Where each box sits in the cross-section; [box] null is an empty floor waiting for its first box. */
class Slot(val box: Box?, val level: Int, val rect: Rect)

class BankLayout(val u: Float, val height: Float, val floors: List<Pair<Int, Rect>>, val ground: Float, val slots: List<Slot>) {
    fun hit(p: Offset): Slot? = slots.firstOrNull { it.rect.inflate(6 * u).contains(p) }
}

private val COLS = mapOf(1 to 4, 2 to 3, 3 to 3)
private val ROW = mapOf(1 to 74f, 2 to 52f, 3 to 66f)
private val SPAN = mapOf(1 to (24f to 336f), 2 to (44f to 316f), 3 to (84f to 276f))

fun bankLayout(s: State, width: Float): BankLayout {
    val u = width / 360f
    val floors = mutableListOf<Pair<Int, Rect>>()
    val slots = mutableListOf<Slot>()
    var y = 116f
    var ground = 0f
    for (level in 1..3) {
        val boxes = s.floor(level)
        val cols = COLS.getValue(level)
        val rows = maxOf(1, (boxes.size + cols - 1) / cols)
        val (x0, x1) = SPAN.getValue(level)
        val h = 30f + rows * ROW.getValue(level)
        floors += level to Rect(x0 * u, y * u, x1 * u, (y + h) * u)
        val cell = (x1 - x0 - 12f) / cols
        val (bw, bh) = when (level) { 1 -> 58f to 58f; 2 -> 84f to 36f; else -> 52f to 52f }
        val list: List<Box?> = boxes.ifEmpty { if (level == 1) emptyList() else listOf(null) }
        list.forEachIndexed { i, b ->
            val col = i % cols
            val row = i / cols
            val inRow = minOf(cols, list.size - row * cols)
            // A short last row is centred.
            val shift = (cols - inRow) * cell / 2f
            val cx = x0 + 6f + shift + cell * (col + .5f)
            val top = y + 24f + row * ROW.getValue(level)
            slots += Slot(b, level, Rect((cx - bw / 2) * u, top * u, (cx + bw / 2) * u, (top + bh) * u))
        }
        y += h
        if (level == 1) { ground = y; y += 18f } else y += 14f
    }
    return BankLayout(u, (y + 6f) * u, floors, ground * u, slots)
}

/** The bank «Нельзя» in cross-section: the facade, the hall, the vault and the central safe, with red string between what rests on what. */
@Composable
fun BankPlan(s: State, w: Words, job: Box?, modifier: Modifier = Modifier, onSlot: (Slot) -> Unit) {
    val tm = rememberTextMeasurer()
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart), label = "p")
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wpx = constraints.maxWidth.toFloat()
        val lay = remember(s, wpx) { bankLayout(s, wpx) }
        val h = with(LocalDensity.current) { lay.height.toDp() }
        Canvas(Modifier.fillMaxWidth().height(h).pointerInput(lay) { detectTapGestures { p -> lay.hit(p)?.let(onSlot) } }) {
            drawBank(s, w, job, lay, tm, pulse)
        }
    }
}

fun DrawScope.drawBank(s: State, w: Words, job: Box?, lay: BankLayout, tm: TextMeasurer, pulse: Float) {
    val u = lay.u
    fun q(x: Float, y: Float) = Offset(x * u, y * u)
    val st = Stroke(1.4f * u)
    val label = TextStyle(fontFamily = Mono, fontSize = (7.6f * u / density).sp, color = C.line2, letterSpacing = (1.2f * u / density).sp)

    // The facade: a pediment with the bank's name, and columns.
    drawPath(Path().apply { to(q(40f, 62f)); ln(q(180f, 20f)); ln(q(320f, 62f)); close() }, C.line, style = st)
    drawPath(Path().apply { to(q(62f, 58f)); ln(q(180f, 28f)); ln(q(298f, 58f)) }, C.line, style = Stroke(.8f * u))
    val name = tm.measure(w.t("НЕЛЬЗЯ", "NO"), TextStyle(fontFamily = Title, fontSize = (11f * u / density).sp, color = C.line, letterSpacing = (3f * u / density).sp))
    drawText(name, topLeft = Offset(180f * u - name.size.width / 2f, 40f * u))
    drawRect(C.line, q(36f, 62f), Size(288f * u, 8f * u), style = st)
    for (x in listOf(54f, 66f, 96f, 108f, 252f, 264f, 294f, 306f)) drawLine(C.line, q(x, 70f), q(x, 116f), 1.1f * u)
    rotate(-6f, q(270f, 96f)) {
        drawText(tm.measure(w.t("вход через\nмелочи ↓", "in through\nsmall things ↓"), TextStyle(fontFamily = Hand, fontSize = (17f * u / density).sp, color = C.mustard, lineHeight = (15f * u / density).sp)), topLeft = q(246f, 76f))
    }

    // Earth below the hall, hatched.
    val ground = lay.ground
    clipRectHatch(Rect(0f, ground, size.width, lay.height), u)
    drawLine(C.line, Offset(0f, ground), Offset(size.width, ground), 2.2f * u)

    for ((level, r) in lay.floors) {
        drawRect(C.navy, r.topLeft, r.size)
        drawRect(C.line, r.topLeft, r.size, style = st)
        drawText(tm.measure(w.floor(level).uppercase(), label), topLeft = Offset(r.left + 8f * u, r.top + 5f * u))
    }

    // Red string: each box to the one it rests on.
    val byId = lay.slots.mapNotNull { sl -> sl.box?.let { it.id to sl } }.toMap()
    for (sl in lay.slots) {
        val b = sl.box ?: continue
        val below = byId[b.restsOn] ?: continue
        val a = Offset(sl.rect.center.x, sl.rect.bottom)
        val z = Offset(below.rect.center.x, below.rect.top)
        drawPath(Path().apply { to(a); quadraticTo((a.x + z.x) / 2 - 8f * u, (a.y + z.y) / 2 + 6f * u, z.x, z.y) }, C.red, style = Stroke(1.3f * u))
        drawCircle(C.red, 2.6f * u, a)
        drawCircle(C.red, 2.6f * u, z)
    }

    for (sl in lay.slots) drawSlot(sl, s, w, job, tm, u, pulse)
}

private fun DrawScope.clipRectHatch(r: Rect, u: Float) {
    val step = 7f * u
    clipRect(r.left, r.top, r.right, r.bottom) {
        var x = r.left - r.height
        while (x < r.right) {
            drawLine(C.line2.copy(alpha = .35f), Offset(x, r.top), Offset(x + r.height, r.bottom), .9f * u)
            x += step
        }
    }
}

private fun DrawScope.drawSlot(sl: Slot, s: State, w: Words, job: Box?, tm: TextMeasurer, u: Float, pulse: Float) {
    val r = sl.rect
    val b = sl.box
    val small = TextStyle(fontFamily = Mono, fontSize = (7.6f * u / density).sp, textAlign = TextAlign.Center)
    if (b == null) {
        drawRect(C.line2.copy(alpha = .7f), r.topLeft, r.size, style = Stroke(1.2f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f * u, 4f * u))))
        drawLine(C.line2, Offset(r.center.x - 7f * u, r.center.y), Offset(r.center.x + 7f * u, r.center.y), 1.4f * u)
        drawLine(C.line2, Offset(r.center.x, r.center.y - 7f * u), Offset(r.center.x, r.center.y + 7f * u), 1.4f * u)
        return
    }
    val current = job?.id == b.id
    val color = when { b.open -> C.cyan; current -> C.orange; else -> C.line }
    val dashed = if (b.open) PathEffect.dashPathEffect(floatArrayOf(5f * u, 4f * u)) else null
    if (current) {
        val rr = (r.maxDimension / 2f) * (1f + .35f * pulse)
        drawCircle(C.orange.copy(alpha = .55f * (1f - pulse)), rr, r.center, style = Stroke(1.2f * u))
    }
    when (sl.level) {
        3 -> {
            if (b.open) {
                drawCircle(color, r.width / 2f, r.center, style = Stroke(1.6f * u, pathEffect = dashed))
                tick(r.center, r.width * .28f, u)
            } else door(r.center, r.width / 2f, color, C.navy2, turn = if (current) pulse * 30f else 0f)
        }
        2 -> {
            drawRect(if (b.open) C.navy else C.navy2, r.topLeft, r.size)
            drawRect(color, r.topLeft, r.size, style = Stroke(if (current) 2.2f * u else 1.4f * u, pathEffect = dashed))
            val c = Offset(r.left + 17f * u, r.center.y)
            if (b.open) tick(c, 9f * u, u) else {
                drawCircle(color, 11f * u, c, style = Stroke(1f * u))
                drawLine(color, Offset(c.x, c.y - 9f * u), Offset(c.x, c.y + 9f * u), 1f * u)
                drawLine(color, Offset(c.x - 9f * u, c.y), Offset(c.x + 9f * u, c.y), 1f * u)
            }
            drawText(tm.measure(w.no(b.no), small.copy(color = color, textAlign = TextAlign.Start)), topLeft = Offset(r.left + 33f * u, r.top + 5f * u))
            val word = b.text.split(' ').firstOrNull { it.length > 3 }?.trim(',', '.', '«', '»')?.lowercase() ?: ""
            drawText(tm.measure("«$word»", small.copy(color = C.line2, textAlign = TextAlign.Start, fontSize = (7f * u / density).sp)), topLeft = Offset(r.left + 33f * u, r.top + 18f * u))
        }
        else -> {
            drawRect(if (b.open) C.navy else C.navy2, r.topLeft, r.size)
            drawRect(color, r.topLeft, r.size, style = Stroke(if (current) 2.2f * u else 1.4f * u, pathEffect = dashed))
            val c = Offset(r.center.x, r.top + 24f * u)
            if (b.open) tick(c, 12f * u, u) else {
                drawCircle(color, 12f * u, c, style = Stroke(1.4f * u))
                drawCircle(color, 3f * u, c)
                drawLine(color, Offset(c.x, c.y - 12f * u), Offset(c.x, c.y - 16f * u), 1.4f * u)
                // The progress of the job: an arc of the dial lit.
                val p = s.progress(b)
                if (p > 0f) drawArc(C.orange, -90f, 360f * p, false, Offset(c.x - 15f * u, c.y - 15f * u), Size(30f * u, 30f * u), style = Stroke(2f * u, cap = StrokeCap.Round))
            }
            val t = tm.measure(w.no(b.no) + if (b.open) w.t(" ВСКРЫТ", " OPEN") else "", small.copy(color = color))
            drawText(t, topLeft = Offset(r.center.x - t.size.width / 2f, r.bottom - 14f * u))
        }
    }
}

private fun DrawScope.tick(c: Offset, r: Float, u: Float) {
    drawPath(Path().apply { to(Offset(c.x - r, c.y)); ln(Offset(c.x - r * .25f, c.y + r * .7f)); ln(Offset(c.x + r, c.y - r * .8f)) }, C.orange, style = Stroke(3f * u, cap = StrokeCap.Round))
}

val Color.dim get() = copy(alpha = .5f)

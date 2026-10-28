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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import app.insidejob.domain.Box
import app.insidejob.domain.Crew
import app.insidejob.domain.HP
import app.insidejob.domain.State
import app.insidejob.domain.Sys
import app.insidejob.ui.Words
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.Hand
import app.insidejob.ui.theme.Mono
import app.insidejob.ui.theme.Title

/** A hall on the box's plan: the way in, one of the guard's systems, or the safe itself. */
sealed interface Hall {
    data object Entrance : Hall
    data class Guard(val sys: Sys) : Hall
    data object Safe : Hall
}

class PlanLayout(val u: Float, val height: Float, val halls: List<Pair<Hall, Rect>>) {
    fun hit(p: Offset) = halls.firstOrNull { it.second.contains(p) }?.first
}

private const val ENTRY = 92f
private const val ROOM = 100f
private const val SAFE = 112f

fun planLayout(b: Box, width: Float): PlanLayout {
    val u = width / 360f
    val halls = mutableListOf<Pair<Hall, Rect>>()
    var y = 14f
    fun add(h: Hall, hh: Float) { halls += h to Rect(26f * u, y * u, 334f * u, (y + hh) * u); y += hh }
    add(Hall.Safe, SAFE)
    for (s in b.sys.reversed()) add(Hall.Guard(s), ROOM)
    add(Hall.Entrance, ENTRY)
    return PlanLayout(u, (y + 26f) * u, halls)
}

/** The box seen from above: from the entrance at the bottom, through each of the guard's halls, to the safe. */
@Composable
fun VaultPlan(s: State, w: Words, b: Box, modifier: Modifier = Modifier, onHall: (Hall) -> Unit) {
    val tm = rememberTextMeasurer()
    val t by rememberInfiniteTransition(label = "lasers").animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse), label = "l")
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wpx = constraints.maxWidth.toFloat()
        val lay = remember(b.systems, wpx) { planLayout(b, wpx) }
        val h = with(LocalDensity.current) { lay.height.toDp() }
        Canvas(Modifier.fillMaxWidth().height(h).pointerInput(lay) { detectTapGestures { p -> lay.hit(p)?.let(onHall) } }) {
            drawPlan(s, w, b, lay, tm, t)
        }
    }
}

fun DrawScope.drawPlan(s: State, w: Words, b: Box, lay: PlanLayout, tm: TextMeasurer, t: Float) {
    val u = lay.u
    val wall = Stroke(2f * u)
    val label = TextStyle(fontFamily = Mono, fontSize = (8f * u / density).sp, color = C.line2, letterSpacing = (1.2f * u / density).sp)
    // Walls, with a doorway on alternate sides between halls, and the route through them.
    val route = Path()
    val halls = lay.halls
    val last = halls.last().second
    route.to(Offset(last.center.x, last.bottom + 20f * u))
    for ((i, pair) in halls.withIndex()) {
        val r = pair.second
        drawLine(C.line, r.topLeft, r.bottomLeft, wall.width)
        drawLine(C.line, r.topRight, r.bottomRight, wall.width)
        if (i == 0) drawLine(C.line, r.topLeft, r.topRight, wall.width)
        // The wall below this hall, with its gap.
        val gapX = if (i == halls.lastIndex) r.center.x else if ((halls.lastIndex - i) % 2 == 1) r.left + 70f * u else r.right - 70f * u
        drawLine(C.line, r.bottomLeft, Offset(gapX - 20f * u, r.bottom), wall.width)
        drawLine(C.line, Offset(gapX + 20f * u, r.bottom), r.bottomRight, wall.width)
    }
    for (i in halls.indices.reversed()) {
        val r = halls[i].second
        val gapX = if (i == halls.lastIndex) r.center.x else if ((halls.lastIndex - i) % 2 == 1) r.left + 70f * u else r.right - 70f * u
        route.ln(Offset(gapX, r.bottom))
        if (i == 0) route.ln(Offset(r.left + 110f * u, r.center.y + 34f * u))
    }
    drawPath(route, C.orange, style = Stroke(2f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f * u, 6f * u), -t * 26f * u)))

    for ((hall, r) in halls) when (hall) {
        Hall.Safe -> safe(s, w, b, r, tm, u, t)
        Hall.Entrance -> entrance(s, w, b, r, tm, u, label)
        is Hall.Guard -> guard(s, w, b, hall.sys, r, tm, u, t, halls.indexOfFirst { it.first == hall }, halls.size, label)
    }
}

private fun DrawScope.safe(s: State, w: Words, b: Box, r: Rect, tm: TextMeasurer, u: Float, t: Float) {
    val c = Offset(r.left + 84f * u, r.center.y)
    val ready = s.ready(b)
    door(c, 36f * u, if (b.open) C.cyan else C.line, C.navy2, turn = if (ready) t * 40f else 0f, glow = if (ready) .6f + .4f * t else 0f)
    val x = r.left + 138f * u
    val kick = TextStyle(fontFamily = Mono, fontSize = (8.5f * u / density).sp, color = C.mustard, letterSpacing = (1.4f * u / density).sp)
    val k = tm.measure(w.t("ВНУТРИ:", "INSIDE:"), kick)
    val loot = tm.measure(b.loot.uppercase(), TextStyle(fontFamily = Title, fontSize = (15f * u / density).sp, lineHeight = (17f * u / density).sp, color = C.mustard), constraints = Constraints(maxWidth = (r.right - x - 10f * u).toInt()))
    val top = r.center.y - (k.size.height + loot.size.height) / 2f
    drawText(k, topLeft = Offset(x, top))
    drawText(loot, topLeft = Offset(x, top + k.size.height + 2f * u))
    if (ready) {
        val go = tm.measure(w.t("всё отключено — вскрывай", "all dark: open it"), TextStyle(fontFamily = Hand, fontSize = (18f * u / density).sp, color = C.orange))
        drawText(go, topLeft = Offset(x, maxOf(r.top + 4f * u, top - go.size.height - 2f * u)))
    }
}

private fun DrawScope.entrance(s: State, w: Words, b: Box, r: Rect, tm: TextMeasurer, u: Float, label: TextStyle) {
    drawText(tm.measure(w.t("ВХОД · РАЗВЕДКА", "WAY IN · RECON"), label), topLeft = Offset(r.left + 8f * u, r.top + 6f * u))
    // The crew, waiting.
    for ((i, c) in Crew.entries.withIndex()) {
        val p = Offset(r.left + 26f * u + i * 26f * u, r.bottom - 22f * u)
        drawCircle(c.color(), 10.5f * u, p)
        val l = tm.measure(c.initial(w), TextStyle(fontFamily = Title, fontSize = (10f * u / density).sp, color = C.ink, textAlign = TextAlign.Center))
        drawText(l, topLeft = Offset(p.x - l.size.width / 2f, p.y - l.size.height / 2f))
    }
    val note = TextStyle(fontFamily = Hand, fontSize = (16f * u / density).sp, lineHeight = (16f * u / density).sp, color = C.mustard)
    val lines = buildList {
        add(
            if (s.dossier(b)) w.t("приговор: ", "verdict: ") + listOf(b.judge, b.age).filter { it.isNotBlank() }.joinToString(", ").ifBlank { w.t("в деле", "on file") }
            else w.t("досье пустое ✗", "no dossier yet ✗"),
        )
        val d = s.duty(b)
        add(
            when {
                d != null -> w.t("дежурит ", "on duty ") + w.weekdays(d.first) + ", " + w.part(d.second)
                s.sighted(b) -> w.t("охрану видели ✓", "guard sighted ✓")
                else -> w.t("охрану ещё не видели ✗", "guard not seen yet ✗")
            },
        )
    }
    rotate(-3f, Offset(r.right - 90f * u, r.center.y)) {
        val txt = tm.measure(lines.joinToString("\n"), note, constraints = Constraints(maxWidth = (176f * u).toInt()))
        drawText(txt, topLeft = Offset(r.right - 184f * u, r.top + 18f * u))
    }
}

private fun DrawScope.guard(s: State, w: Words, b: Box, sys: Sys, r: Rect, tm: TextMeasurer, u: Float, t: Float, index: Int, count: Int, label: TextStyle) {
    val hits = s.hits(b, sys).coerceAtMost(HP)
    val off = hits >= HP
    val num = count - 1 - index
    drawText(tm.measure(w.t("ЗАЛ $num", "HALL $num"), label), topLeft = Offset(r.left + 8f * u, r.top + 6f * u))
    // Beams in the right half: fewer as the system takes hits, dashed blue once it's dark.
    val bx0 = r.left + 176f * u
    val bx1 = r.right - 8f * u
    val by0 = r.top + 26f * u
    val by1 = r.bottom - 12f * u
    val beams = if (off) 3 else 2 * (HP - hits) + 1
    val color = if (off) C.cyan else C.laser.copy(alpha = .7f + .3f * t)
    val eff = if (off) PathEffect.dashPathEffect(floatArrayOf(5f * u, 5f * u)) else null
    for (i in 0 until beams) {
        val f = if (beams == 1) .5f else i / (beams - 1f)
        val y0 = by0 + (by1 - by0) * f
        val y1 = by1 - (by1 - by0) * f
        drawLine(color, Offset(bx0, y0), Offset(bx1, y1), 1.3f * u, pathEffect = eff)
        drawCircle(color, 2.6f * u, Offset(bx0, y0))
    }
    val tag = tm.measure(
        (if (off) w.t("ОТКЛЮЧЕНА", "DOWN") else w.t("АКТИВНА", "LIVE")),
        TextStyle(fontFamily = Title, fontSize = (10f * u / density).sp, color = if (off) C.ink else C.paper, letterSpacing = (1.4f * u / density).sp),
    )
    val tagR = Rect(r.right - tag.size.width - 18f * u, r.top + 4f * u, r.right - 6f * u, r.top + 4f * u + tag.size.height + 4f * u)
    drawRect(if (off) C.cyan else C.red, tagR.topLeft, tagR.size)
    drawText(tag, topLeft = Offset(tagR.left + 6f * u, tagR.top + 2f * u))

    // The device and its name on the left.
    val g = Offset(r.left + 34f * u, r.center.y + 6f * u)
    drawCircle(C.navy2, 22f * u, g)
    glyph(sys, g, 15f * u, if (off) C.cyan else C.line, off)
    val x = r.left + 64f * u
    val name = tm.measure(w.device(sys).uppercase(), TextStyle(fontFamily = Title, fontSize = (13f * u / density).sp, color = if (off) C.cyan else C.line), constraints = Constraints(maxWidth = (bx0 - x - 6f * u).toInt()))
    val art = tm.measure("«" + w.sys(sys) + "»", TextStyle(fontFamily = Mono, fontSize = (8.5f * u / density).sp, color = C.line2), constraints = Constraints(maxWidth = (bx0 - x - 6f * u).toInt()))
    val top = g.y - (name.size.height + art.size.height + 12f * u) / 2f
    drawText(name, topLeft = Offset(x, top))
    drawText(art, topLeft = Offset(x, top + name.size.height))
    // Hit pips.
    for (i in 0 until HP) {
        val p = Offset(x + i * 13f * u, top + name.size.height + art.size.height + 4f * u)
        drawRect(if (i < hits) C.cyan else C.line.copy(alpha = .18f), p, Size(10f * u, 5f * u))
    }
}

fun Crew.initial(w: Words) = w.crew(this).take(1)

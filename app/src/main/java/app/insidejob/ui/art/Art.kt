package app.insidejob.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.insidejob.domain.Crew
import app.insidejob.domain.Sys
import app.insidejob.ui.theme.C
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A path in units of [k] around [o]. */
fun path(o: Offset, k: Float, block: Path.(p: (Float, Float) -> Offset) -> Unit): Path = Path().apply {
    block { x, y -> Offset(o.x + x * k, o.y + y * k) }
}

fun Path.to(p: Offset) = moveTo(p.x, p.y)
fun Path.ln(p: Offset) = lineTo(p.x, p.y)

val dashes = PathEffect.dashPathEffect(floatArrayOf(10f, 7f))

/**
 * A system of the guard's, drawn as its device on the blueprint in a box of radius [r] around [c].
 * Off systems are drawn dashed.
 */
fun DrawScope.glyph(s: Sys, c: Offset, r: Float, color: Color, off: Boolean = false) {
    val k = r
    val st = Stroke(maxOf(1.4f, r * .09f), cap = StrokeCap.Round, pathEffect = if (off) PathEffect.dashPathEffect(floatArrayOf(r * .22f, r * .16f)) else null)
    fun p(x: Float, y: Float) = Offset(c.x + x * k, c.y + y * k)
    fun line(x0: Float, y0: Float, x1: Float, y1: Float) = drawLine(color, p(x0, y0), p(x1, y1), st.width, StrokeCap.Round, st.pathEffect)
    when (s) {
        Sys.MIND -> {
            for (y in listOf(-.6f, 0f, .6f)) { drawCircle(color, k * .1f, p(-.8f, y)); line(-.8f, y, .8f, -y * .6f) }
            line(-.8f, -.6f, .8f, .6f)
        }
        Sys.FORTUNE -> {
            drawRect(color, p(-.85f, -.6f), Size(1.7f * k, 1.2f * k), style = st)
            for (row in 0..2) for (col in 0..3) drawRect(color, p(-.7f + col * .38f, -.45f + row * .32f), Size(.26f * k, .18f * k))
        }
        Sys.CATASTROPHE -> {
            drawArc(color, 180f, 180f, false, p(-.5f, -.3f), Size(k, k), style = st)
            line(-.65f, .2f, .65f, .2f)
            for (a in listOf(-150.0, -120.0, -90.0, -60.0, -30.0)) {
                val rad = a * PI / 180
                line((cos(rad) * .62).toFloat(), (.2 + sin(rad) * .62).toFloat(), (cos(rad) * .95).toFloat(), (.2 + sin(rad) * .95).toFloat())
            }
        }
        Sys.ALLNONE -> {
            drawRoundRect(color, p(-.8f, -.32f), Size(1.6f * k, .64f * k), CornerRadius(.32f * k), style = st)
            drawCircle(color, .24f * k, p(.48f, 0f))
            line(-.5f, -.12f, -.5f, .12f)
        }
        Sys.OVERGEN -> {
            drawCircle(color, .75f * k, c, style = st)
            for (i in 0 until 12) { val a = i * PI / 6; line((cos(a) * .55).toFloat(), (sin(a) * .55).toFloat(), (cos(a) * .72).toFloat(), (sin(a) * .72).toFloat()) }
            drawCircle(color, .18f * k, c)
            line(0f, 0f, 0f, -.45f)
        }
        Sys.LABEL -> {
            drawRoundRect(color, p(-.6f, -.8f), Size(1.2f * k, 1.6f * k), CornerRadius(.1f * k), style = st)
            drawCircle(color, .2f * k, p(0f, -.35f), style = st)
            for ((i, x) in listOf(-.4f, -.28f, -.2f, -.04f, .06f, .2f, .3f, .4f).withIndex()) line(x, .2f, x, if (i % 3 == 0) .62f else .52f)
        }
        Sys.SHOULD -> {
            drawCircle(color, .26f * k, p(0f, -.35f))
            drawPath(path(c, k) { q -> to(q(-.38f, -.62f)); ln(q(.38f, -.62f)); ln(q(.3f, -.78f)); ln(q(-.3f, -.78f)); close() }, color)
            drawPath(path(c, k) { q -> to(q(-.55f, .85f)); ln(q(-.4f, 0f)); ln(q(.4f, 0f)); ln(q(.55f, .85f)) }, color, style = st)
            drawCircle(color, .1f * k, p(.75f, .7f)); line(.62f, .55f, .9f, .55f)
        }
        Sys.FILTER -> {
            drawRect(color, p(-.7f, -.3f), Size(1.1f * k, .55f * k), style = st)
            drawCircle(color, .16f * k, p(.55f, -.03f), style = st)
            line(-.2f, .25f, -.2f, .7f); line(-.5f, .7f, .1f, .7f)
        }
        Sys.DISCOUNT -> {
            drawRect(color, p(-.7f, -.7f), Size(1.4f * k, .6f * k), style = st)
            line(-.5f, -.45f, .5f, -.45f)
            for (x in listOf(-.5f, -.25f, 0f, .25f, .5f)) line(x, 0f, x + .04f, .75f)
        }
        Sys.EMOTION -> {
            drawRoundRect(color, p(-.14f, -.85f), Size(.28f * k, 1.2f * k), CornerRadius(.14f * k), style = st)
            drawCircle(color, .3f * k, p(0f, .55f))
            for (y in listOf(-.55f, -.3f, -.05f)) line(.2f, y, .38f, y)
        }
        Sys.PERSONAL -> {
            drawCircle(color, .22f * k, p(-.6f, -.55f))
            drawPath(path(c, k) { q -> to(q(-.5f, -.42f)); ln(q(.85f, .75f)); ln(q(.1f, .85f)); close() }, color.copy(alpha = .35f))
            drawPath(path(c, k) { q -> to(q(-.5f, -.42f)); ln(q(.85f, .75f)); ln(q(.1f, .85f)); close() }, color, style = st)
            drawCircle(color, .14f * k, p(.45f, .55f))
        }
        Sys.COMPARE -> {
            drawRect(color, p(-.8f, -.8f), Size(.65f * k, 1.6f * k), style = st)
            drawRect(color, p(.15f, -.8f), Size(.65f * k, 1.6f * k), style = st)
            line(-.65f, -.4f, -.35f, -.6f); line(.3f, -.4f, .6f, -.6f)
        }
        Sys.FIXED -> {
            for (row in 0..3) {
                val y = -.8f + row * .4f
                line(-.85f, y, .85f, y)
                val off2 = if (row % 2 == 0) 0f else .28f
                for (x in listOf(-.56f, 0f, .56f)) line(x + off2 - .14f, y, x + off2 - .14f, y + .4f)
            }
            line(-.85f, .8f, .85f, .8f)
        }
        Sys.CONDITION -> {
            drawCircle(color, .2f * k, p(0f, -.1f), style = st)
            line(0f, .1f, 0f, .85f)
            line(0f, -.1f, -.75f, -.35f); line(0f, -.1f, .75f, -.35f); line(0f, -.1f, 0f, -.85f)
            line(-.5f, .85f, .5f, .85f)
        }
    }
}

@Composable
fun Glyph(s: Sys, color: Color, size: Dp = 28.dp, off: Boolean = false) {
    Canvas(Modifier.size(size)) { glyph(s, center, this.size.minDimension * .42f, color, off) }
}

fun Crew.color(): Color = when (this) {
    Crew.LOCK -> C.mustard
    Crew.PROF -> C.cyan
    Crew.PEN -> C.paper
    Crew.SHADOW -> C.violet
    Crew.DRIVER -> C.orange
}

/** A crew member in cut paper, in the manner of a sixties title sequence, on its own colour. */
fun DrawScope.figure(c: Crew) {
    val k = size.minDimension / 104f
    val o = Offset(0f, 0f)
    fun q(x: Float, y: Float) = Offset(x * k, y * k)
    drawRect(c.color())
    val ink = C.ink
    // shoulders and head
    drawPath(path(o, k) { p -> to(p(30f, 104f)); ln(p(38f, 64f)); quadraticTo(52f * k, 56f * k, 66f * k, 64f * k); ln(p(74f, 104f)); close() }, ink)
    drawCircle(ink, 14.5f * k, q(52f, 44f))
    when (c) {
        Crew.LOCK -> {
            drawPath(path(o, k) { p -> to(p(34f, 46f)); quadraticTo(34f * k, 22f * k, 52f * k, 24f * k); quadraticTo(72f * k, 22f * k, 70f * k, 46f * k); ln(p(66f, 50f)); quadraticTo(66f * k, 34f * k, 52f * k, 34f * k); quadraticTo(40f * k, 34f * k, 38f * k, 50f * k); close() }, C.red)
            drawLine(ink, q(72f, 72f), q(94f, 50f), 3f * k)
            drawLine(ink, q(94f, 50f), q(98f, 47f), 2f * k)
            drawLine(ink, q(90f, 54f), q(95f, 55f), 2f * k)
        }
        Crew.PROF -> {
            drawPath(path(o, k) { p -> to(p(34f, 32f)); ln(p(70f, 32f)); ln(p(70f, 28f)); quadraticTo(70f * k, 16f * k, 52f * k, 16f * k); quadraticTo(34f * k, 16f * k, 34f * k, 28f * k); close() }, ink)
            drawRect(ink, q(28f, 32f), Size(48f * k, 4f * k))
            drawCircle(C.cyan, 4f * k, q(46f, 45f), style = Stroke(1.8f * k))
            drawCircle(C.cyan, 4f * k, q(58f, 45f), style = Stroke(1.8f * k))
            drawPath(path(o, k) { p -> to(p(44f, 66f)); quadraticTo(34f * k, 84f * k, 48f * k, 90f * k); quadraticTo(62f * k, 94f * k, 62f * k, 82f * k) }, C.paper, style = Stroke(2.4f * k))
            drawCircle(C.paper, 4f * k, q(62f, 80f))
        }
        Crew.PEN -> {
            drawOval(C.red, q(31f, 24f), Size(38f * k, 14f * k))
            drawCircle(C.red, 3f * k, q(58f, 25f))
            drawPath(path(o, k) { p -> to(p(68f, 92f)); ln(p(90f, 58f)); ln(p(94f, 61f)); ln(p(72f, 95f)); close() }, ink)
            drawPath(path(o, k) { p -> to(p(90f, 58f)); ln(p(95f, 52f)); ln(p(97f, 60f)); close() }, C.orange)
        }
        Crew.SHADOW -> {
            drawPath(path(o, k) { p -> to(p(40f, 62f)); ln(p(52f, 76f)); ln(p(64f, 62f)); close() }, C.violet)
            drawPath(path(o, k) { p -> to(p(30f, 38f)); ln(p(74f, 38f)); ln(p(68f, 34f)); quadraticTo(66f * k, 22f * k, 52f * k, 22f * k); quadraticTo(38f * k, 22f * k, 36f * k, 34f * k); close() }, ink)
            drawRoundRect(C.mustard, q(40f, 42f), Size(9f * k, 7f * k), CornerRadius(2f * k))
            drawRoundRect(C.mustard, q(55f, 42f), Size(9f * k, 7f * k), CornerRadius(2f * k))
        }
        Crew.DRIVER -> {
            drawPath(path(o, k) { p -> to(p(36f, 38f)); quadraticTo(36f * k, 24f * k, 52f * k, 24f * k); quadraticTo(68f * k, 24f * k, 68f * k, 38f * k); close() }, ink)
            drawRect(ink, q(36f, 36f), Size(40f * k, 4f * k))
            drawCircle(C.paper, 16f * k, q(52f, 86f), style = Stroke(4f * k))
            drawLine(C.paper, q(36f, 86f), q(68f, 86f), 3f * k)
            drawLine(C.paper, q(52f, 86f), q(52f, 102f), 3f * k)
        }
    }
}

/** A round vault door seen face on: bolts, a wheel turned by [turn] degrees. */
fun DrawScope.door(c: Offset, r: Float, edge: Color, fill: Color, turn: Float = 0f, glow: Float = 0f) {
    if (glow > 0f) drawCircle(C.orange.copy(alpha = .25f * glow), r * 1.25f, c)
    drawCircle(fill, r, c)
    drawCircle(edge, r, c, style = Stroke(r * .07f))
    drawCircle(edge, r * .74f, c, style = Stroke(r * .03f))
    for (i in 0 until 8) {
        val a = i * PI / 4
        drawCircle(edge, r * .05f, Offset(c.x + (cos(a) * r * .87f).toFloat(), c.y + (sin(a) * r * .87f).toFloat()))
    }
    rotate(turn, c) {
        for (i in 0 until 4) {
            val a = i * PI / 4
            drawLine(edge, Offset(c.x - (cos(a) * r * .56f).toFloat(), c.y - (sin(a) * r * .56f).toFloat()), Offset(c.x + (cos(a) * r * .56f).toFloat(), c.y + (sin(a) * r * .56f).toFloat()), r * .07f, StrokeCap.Round)
        }
        drawCircle(edge, r * .16f, c)
    }
}

/** The loot: a gold bar, a gem, a coin or a key, chosen by the box. Locked loot is a dashed padlock. */
fun DrawScope.loot(kind: Int, c: Offset, w: Float, locked: Boolean = false) {
    val k = w / 120f
    fun q(x: Float, y: Float) = Offset(c.x + (x - 60f) * k, c.y + (y - 32f) * k)
    if (locked) {
        val st = Stroke(2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * k, 4f * k)))
        drawRoundRect(C.steel.copy(alpha = .6f), q(38f, 22f), Size(44f * k, 34f * k), CornerRadius(4f * k), style = st)
        drawArc(C.steel.copy(alpha = .6f), 180f, 180f, false, q(46f, 8f), Size(28f * k, 28f * k), style = st)
        return
    }
    when (kind % 4) {
        0 -> {
            drawPath(Path().apply { to(q(14f, 52f)); ln(q(28f, 16f)); ln(q(92f, 16f)); ln(q(106f, 52f)); close() }, C.gold)
            drawPath(Path().apply { to(q(28f, 16f)); ln(q(92f, 16f)); ln(q(86f, 24f)); ln(q(34f, 24f)); close() }, Color(0xB3FFF1C4))
            drawLine(Color(0xFF7A5512), q(14f, 52f), q(106f, 52f), 2f * k)
        }
        1 -> {
            drawPath(Path().apply { to(q(60f, 4f)); ln(q(96f, 28f)); ln(q(60f, 60f)); ln(q(24f, 28f)); close() }, C.cyan)
            drawPath(Path().apply { to(q(60f, 4f)); ln(q(46f, 28f)); ln(q(74f, 28f)); close() }, Color(0xFFC8F0FF))
            drawPath(Path().apply { to(q(24f, 28f)); ln(q(96f, 28f)); to(q(46f, 28f)); ln(q(60f, 60f)); ln(q(74f, 28f)) }, C.navy, style = Stroke(1.4f * k))
        }
        2 -> {
            drawCircle(C.gold, 26f * k, q(60f, 32f))
            drawCircle(Color(0xFF7A5512), 20f * k, q(60f, 32f), style = Stroke(1.6f * k))
            drawCircle(Color(0xFFFFF1C4), 6f * k, q(52f, 24f))
        }
        else -> {
            drawCircle(C.gold, 13f * k, q(34f, 32f), style = Stroke(6f * k))
            drawLine(C.gold, q(46f, 32f), q(100f, 32f), 7f * k)
            drawLine(C.gold, q(88f, 32f), q(88f, 46f), 6f * k)
            drawLine(C.gold, q(98f, 32f), q(98f, 42f), 6f * k)
        }
    }
}

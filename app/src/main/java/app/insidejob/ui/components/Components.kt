package app.insidejob.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.P
import app.insidejob.ui.theme.T

/** Blueprint paper: a fine grid with a heavier one every five squares. */
fun Modifier.blueprint(color: Color = C.navy) = this.background(color).drawBehind {
    val small = 12.dp.toPx()
    var i = 0
    var x = 0f
    while (x < size.width) {
        drawLine(if (i % 5 == 0) C.grid else C.grid.copy(alpha = .45f), Offset(x, 0f), Offset(x, size.height), 1f)
        x += small; i++
    }
    i = 0
    var y = 0f
    while (y < size.height) {
        drawLine(if (i % 5 == 0) C.grid else C.grid.copy(alpha = .45f), Offset(0f, y), Offset(size.width, y), 1f)
        y += small; i++
    }
}

/** Typewriter paper: faint ruled lines. */
fun Modifier.ruled(color: Color = C.paper) = this.background(color).drawBehind {
    val step = 24.dp.toPx()
    var y = step
    while (y < size.height) { drawLine(C.ink2.copy(alpha = .07f), Offset(0f, y), Offset(size.width, y), 1f); y += step }
}

enum class Kind { ORANGE, INK, GHOST, LINE, PAPER }

@Composable
fun Btn(text: String, modifier: Modifier = Modifier, kind: Kind = Kind.ORANGE, enabled: Boolean = true, onClick: () -> Unit) {
    val (bg, fg, edge) = when (kind) {
        Kind.ORANGE -> Triple(C.orange, C.ink, C.orange)
        Kind.INK -> Triple(C.ink, C.paper, C.ink)
        Kind.GHOST -> Triple(Color.Transparent, C.ink, C.ink)
        Kind.LINE -> Triple(Color.Transparent, C.orange, C.orange)
        Kind.PAPER -> Triple(C.paper, C.ink, C.paper)
    }
    Box(
        modifier.background(if (enabled) bg else C.line2.copy(alpha = .35f)).border(1.5.dp, if (enabled) edge else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick).height(50.dp).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Every button is one line and one height: a long label shrinks a little instead of wrapping.
        BasicText(
            text.uppercase(), style = T.Button.copy(color = if (enabled) fg else C.paper.copy(alpha = .7f), textAlign = TextAlign.Center), maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = T.Button.fontSize, stepSize = .5.sp),
        )
    }
}

/** A choice chip; [dark] for chips on the blueprint. */
@Composable
fun Chip(text: String, on: Boolean, modifier: Modifier = Modifier, dark: Boolean = false, onClick: () -> Unit) {
    val (bg, fg, edge) = when {
        on && dark -> Triple(C.orange, C.ink, C.orange)
        on -> Triple(C.ink, C.paper, C.ink)
        dark -> Triple(Color.Transparent, C.line, C.line2)
        else -> Triple(Color.Transparent, C.ink, C.ink2)
    }
    Box(modifier.background(bg).border(1.2.dp, edge).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text, style = T.Small.copy(color = fg, fontSize = 12.sp))
    }
}

/** A box on the blueprint: dark glass with a thin line. */
@Composable
fun Plate(modifier: Modifier = Modifier, edge: Color = C.line2, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().background(Color(0xD0081830)).border(1.dp, edge)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 14.dp, vertical = 12.dp),
        content = content,
    )
}

/** A card on paper. */
@Composable
fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().background(C.cream).border(1.5.dp, C.ink)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 14.dp, vertical = 12.dp),
        content = content,
    )
}

@Composable
fun Kicker(text: String, modifier: Modifier = Modifier, style: TextStyle = T.Kicker) = Text(text.uppercase(), style = style, modifier = modifier)

@Composable
fun Gap(h: Int = 10) = Spacer(Modifier.height(h.dp))

@Composable
fun Flow(modifier: Modifier = Modifier, gap: Dp = 6.dp, content: @Composable () -> Unit) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap)) { content() }
}

/** A typed field on paper; [lines] above one lets it grow. */
@Composable
fun Field(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, lines: Int = 1, max: Int = 160, bg: Color = Color.White.copy(alpha = .55f)) {
    Box(modifier.fillMaxWidth().background(bg).border(1.2.dp, C.ink2).padding(horizontal = 10.dp, vertical = 10.dp)) {
        if (value.isEmpty()) Text(hint, style = T.Typed.copy(color = C.muted))
        BasicTextField(
            value, { onChange(it.take(max)) }, Modifier.fillMaxWidth().heightIn(min = (lines * 21).dp), textStyle = T.Typed, singleLine = lines == 1,
            cursorBrush = SolidColor(C.red), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}

/** A row of bars, [on] of [n] lit. */
@Composable
fun Meter(on: Int, n: Int, modifier: Modifier = Modifier, lit: Color = C.orange, dim: Color = C.line.copy(alpha = .15f), height: Dp = 6.dp) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(n) { Box(Modifier.weight(1f).height(height).background(if (it < on) lit else dim)) }
    }
}

/** How much the guard is believed, 0..100. */
@Composable
fun Faith(value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier, dark: Boolean = false) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Slider(
            value.toFloat(), { onChange(it.toInt()) }, Modifier.weight(1f), valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = C.red, activeTrackColor = C.red, inactiveTrackColor = if (dark) C.line.copy(alpha = .2f) else C.paper2,
            ),
        )
        Text("$value%", style = (if (dark) T.Sub else P.Sub), modifier = Modifier.padding(start = 10.dp))
    }
}

/** A dossier sheet sliding over the screen: paper with a red edge, a title and a way back. */
@Composable
fun Sheet(kicker: String, title: String, onClose: () -> Unit, closeLabel: String, foot: (@Composable ColumnScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(C.scrim).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(top = 26.dp).ruled()) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(C.red))
            Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp, top = 12.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Kicker(kicker, style = P.Kicker)
                    Text(title, style = P.Head, modifier = Modifier.padding(top = 2.dp))
                }
                Text(closeLabel, style = P.Small, modifier = Modifier.clickable(onClick = onClose).padding(12.dp))
            }
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp), content = content)
            if (foot != null) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(C.ink2.copy(alpha = .3f)))
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), content = foot)
            }
        }
    }
}

/** A rubber stamp, slightly crooked. */
@Composable
fun Stamp(text: String, modifier: Modifier = Modifier, color: Color = C.red, sub: String? = null) {
    Column(
        modifier.border(3.dp, color).padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text.uppercase(), style = T.Sub.copy(color = color, letterSpacing = 2.sp))
        if (sub != null) Text(sub.uppercase(), style = T.Kicker.copy(color = color, fontSize = 9.sp))
    }
}

/** Short messages at the bottom of the screen. */
class Toaster {
    var text by mutableStateOf<String?>(null)
    var seq by mutableIntStateOf(0)
    fun show(s: String) { text = s; seq++ }
}

val LocalToast = compositionLocalOf { Toaster() }

@Composable
fun BoxScope.ToastHost(t: Toaster) {
    LaunchedEffect(t.seq) { if (t.text != null) { kotlinx.coroutines.delay(3200); t.text = null } }
    val s = t.text ?: return
    Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 18.dp, end = 18.dp, bottom = 84.dp)) {
        Text(s, style = T.Body.copy(color = C.ink, textAlign = TextAlign.Center), modifier = Modifier.background(C.mustard).clickable { t.text = null }.padding(horizontal = 16.dp, vertical = 11.dp))
    }
}

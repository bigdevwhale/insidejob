package app.insidejob.ui.box

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.insidejob.container
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.art.door
import app.insidejob.ui.art.loot
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Faith
import app.insidejob.ui.components.Field
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.LocalToast
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.T
import kotlinx.coroutines.launch

/** The safe opens: the dial spins, the door swings, and the right that was kept inside is engraved and taken. */
@Composable
fun CrackScreen(h: Home, nav: Nav, id: Long, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = h.s.box(id) ?: return onClose()
    BackHandler(onBack = onClose)
    val scope = rememberCoroutineScope()
    val turn = remember { Animatable(0f) }
    val swing = remember { Animatable(0f) }
    var stage by remember { mutableIntStateOf(0) }
    var engraving by remember { mutableStateOf("") }
    var faith by remember { mutableIntStateOf(minOf(b.faith, 40)) }
    Box(
        Modifier.fillMaxSize().background(Brush.radialGradient(listOf(C.velvet2, C.velvet, C.ink), radius = 1400f))
            .clickable(remember { MutableInteractionSource() }, null) {},
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Gap(16)
            Kicker(w.place(b.level) + " " + w.no(b.no), style = T.Kicker.copy(color = C.rose))
            Text("«${b.text}»", style = T.Sub.copy(color = C.paper), modifier = Modifier.padding(top = 4.dp))
            Canvas(Modifier.fillMaxWidth().height(260.dp)) {
                val r = size.minDimension * .36f
                val cc = Offset(size.width / 2f, size.height / 2f)
                // The loot behind the door.
                drawCircle(C.ink, r, cc)
                if (swing.value > .2f) loot((b.id % 4).toInt(), cc, r * 1.3f * swing.value)
                // The door swings out to the left, narrowing as it turns.
                withTransform({
                    translate(left = -r * 1.15f * swing.value)
                    scale(1f - .8f * swing.value, 1f, Offset(cc.x - r, cc.y))
                }) { door(cc, r, C.gold, C.velvet, turn.value) }
            }
            when (stage) {
                0 -> {
                    Text(w.t("Вся охрана отключена, разведка проведена. Осталось набрать код.", "Every system is down and the recon is done. Only the code is left."), style = T.Body.copy(color = C.paper, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                    Gap(16)
                    Btn(w.t("Крутить диск", "Spin the dial"), Modifier.fillMaxWidth()) {
                        stage = 1
                        scope.launch {
                            turn.animateTo(-300f, tween(900, easing = FastOutSlowInEasing))
                            turn.animateTo(140f, tween(700, easing = FastOutSlowInEasing))
                            turn.animateTo(-60f, tween(500, easing = FastOutSlowInEasing))
                            swing.animateTo(1f, tween(1300, easing = FastOutSlowInEasing))
                            stage = 2
                        }
                    }
                }
                1 -> Text(w.t("щёлк… щёлк… щёлк…", "click… click… click…"), style = T.Note.copy(color = C.gold, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                else -> {
                    Text(w.t("ВНУТРИ:", "INSIDE:"), style = T.Kicker.copy(color = C.rose, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                    Text(b.loot.uppercase(), style = T.Head.copy(color = C.gold, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    Gap(16)
                    Kicker(w.t("Перо гравирует новое правило", "The Quill engraves the new rule"), style = T.Kicker.copy(color = C.rose))
                    Gap(6)
                    Field(engraving, { engraving = it }, w.t("Просить о помощи — нормально. Люди чаще рады помочь.", "Asking for help is normal. People are usually glad to."), lines = 2, bg = C.paper)
                    Gap(14)
                    Kicker(w.t("Насколько теперь веришь охране", "How much you believe the guard now"), style = T.Kicker.copy(color = C.rose))
                    Faith(faith, { faith = it }, dark = true)
                    Gap(14)
                    Btn(w.t("Забрать добычу", "Take the loot"), Modifier.fillMaxWidth(), enabled = engraving.isNotBlank()) {
                        c.write { repo.crack(b, engraving, faith) }
                        toast.show(w.t("Сейф вскрыт. Добыча в витрине.", "The safe is open. The loot is in the case."))
                        onClose()
                        nav.overlays.removeAll { it == Overlay.Plan(b.id) }
                        nav.push(Overlay.Loot(b.id))
                    }
                    Gap(20)
                }
            }
        }
    }
}

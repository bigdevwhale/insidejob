package app.insidejob.ui.box

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.insidejob.container
import app.insidejob.domain.Box
import app.insidejob.domain.Crew
import app.insidejob.domain.FAITH
import app.insidejob.domain.Move
import app.insidejob.domain.Sys
import app.insidejob.domain.date
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.Words
import app.insidejob.ui.art.Hall
import app.insidejob.ui.art.VaultPlan
import app.insidejob.ui.art.color
import app.insidejob.ui.art.figure
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.Kind
import app.insidejob.ui.components.LocalToast
import app.insidejob.ui.components.Plate
import app.insidejob.ui.components.blueprint
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.T

/** One box from above: the guard's voice, its halls, and the crew to send in. */
@Composable
fun BoxScreen(h: Home, nav: Nav, id: Long) {
    val s = h.s
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = s.box(id) ?: run { nav.pop(); return }
    BackHandler { nav.pop() }
    var menu by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().blueprint().clickable(remember { MutableInteractionSource() }, null) {}) {
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 24.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(w.t("‹ Банк", "‹ Bank"), style = T.Small.copy(color = C.line), modifier = Modifier.clickable { nav.pop() }.padding(12.dp))
                Box(Modifier.weight(1f))
                Text("⋯", style = T.Head.copy(color = C.line2), modifier = Modifier.clickable { menu = !menu }.padding(horizontal = 14.dp, vertical = 6.dp))
            }
            Column(Modifier.padding(horizontal = 18.dp)) {
                Kicker(w.place(b.level) + " " + w.no(b.no) + w.t(" · вид сверху", " · from above"))
                Text("«${b.text}»".uppercase(), style = T.Head, modifier = Modifier.padding(top = 2.dp))
                // The guard's voice fades as the belief does.
                Text(
                    w.t("Охрана: ", "The guard: ") + "«${b.voice}»", style = T.Body.copy(color = C.laser, fontStyle = FontStyle.Italic, fontSize = (12 + b.faith / 12).sp),
                    modifier = Modifier.padding(top = 6.dp).alpha((.25f + .75f * b.faith / 100f).coerceIn(.25f, 1f)),
                )
                Row(Modifier.padding(top = 6.dp).clickable { nav.push(Overlay.Faith(b.id)) }, verticalAlignment = Alignment.CenterVertically) {
                    Text(w.t("Веришь охране: ", "You believe the guard: "), style = T.Small)
                    Text("${b.faith}%", style = T.Sub.copy(color = C.orange))
                    Text(w.t("  · было ${b.faith0}% ›", "  · was ${b.faith0}% ›"), style = T.Small)
                }
            }
            if (menu) Menu(h, nav, b) { menu = false }
            VaultPlan(s, w, b, Modifier.padding(top = 8.dp)) { hall ->
                when (hall) {
                    Hall.Entrance -> nav.push(Overlay.Dossier(b.id))
                    is Hall.Guard -> nav.push(Overlay.Guard(b.id, hall.sys))
                    Hall.Safe -> if (s.ready(b)) nav.push(Overlay.Crack(b.id)) else toast.show(
                        if (!s.recon(b)) w.t("Сначала разведка: досье и хотя бы одна запись Тени.", "Recon first: the dossier and at least one Shadow note.")
                        else w.t("Сейф под охраной: отключи все системы.", "The safe is still guarded: take every system down."),
                    )
                }
            }
            Column(Modifier.padding(horizontal = 16.dp)) {
                if (s.ready(b)) {
                    Btn(w.t("Вскрыть сейф", "Crack the safe"), Modifier.fillMaxWidth()) { nav.push(Overlay.Crack(b.id)) }
                    Gap(14)
                }
                Kicker(w.t("Кого послать", "Who goes in"))
                Gap(8)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (m in Crew.entries) CrewToken(w, m, Modifier.weight(1f)) {
                        when (m) {
                            Crew.LOCK -> nav.push(Overlay.Evidence(b.id, null))
                            Crew.PROF -> nav.push(Overlay.Question(b.id, null))
                            Crew.PEN -> nav.push(Overlay.Dossier(b.id))
                            Crew.SHADOW -> nav.push(Overlay.Sighting(b.id))
                            Crew.DRIVER -> nav.push(Overlay.Job(b.id, null))
                        }
                    }
                }
                Gap(16)
                val below = s.box(b.restsOn)
                if (below != null) {
                    Plate(onClick = { nav.swap(Overlay.Plan(below.id)) }) {
                        Kicker(w.t("Стоит на", "Rests on"))
                        Text(w.place(below.level) + " " + w.no(below.no) + ": «${below.text}»", style = T.Body, modifier = Modifier.padding(top = 4.dp))
                        Text(if (below.open) w.t("вскрыта: здесь минус одна сигнализация в каждом зале", "open: one alarm less in every hall here") else w.t("вскроешь её — здесь ослабнет охрана", "open it and the security here weakens"), style = T.Small)
                    }
                    Gap(10)
                } else if (b.level < 3) {
                    Btn(w.t("Что под этим? Спуститься ниже", "What's under this? Go down"), Modifier.fillMaxWidth(), kind = Kind.LINE) { nav.push(Overlay.NewBox(under = b.id, level = b.level + 1)) }
                    Gap(10)
                }
                val log = s.moves(b).filter { it.who != FAITH }.takeLast(6).reversed()
                if (log.isNotEmpty()) {
                    Kicker(w.t("Журнал дела", "Job log"), Modifier.padding(top = 6.dp))
                    for (m in log) LogRow(w, h, m)
                }
            }
        }
    }
}

@Composable
fun CrewToken(w: Words, m: Crew, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(52.dp).border(1.5.dp, C.ink)) { figure(m) }
        Text(w.crew(m), style = T.Small.copy(color = m.color(), fontSize = 10.5.sp), maxLines = 1, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun LogRow(w: Words, h: Home, m: Move) {
    val who = Crew.of(m.who) ?: return
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Box(Modifier.padding(top = 5.dp).size(8.dp).background(who.color()))
        Column(Modifier.padding(start = 10.dp)) {
            Text(w.crew(who) + " · " + w.short(m.at.date(h.zone)) + (Sys.of(m.system)?.let { " · " + w.sys(it) } ?: ""), style = T.Small)
            Text(m.text, style = T.Body.copy(fontSize = 12.5.sp))
        }
    }
}

@Composable
private fun Menu(h: Home, nav: Nav, b: Box, close: () -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    var sure by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth().background(C.paper).padding(12.dp)) {
        if (h.s.job?.id != b.id) Text(w.t("Сделать текущим делом", "Make it the current job"), style = T.Body.copy(color = C.ink), modifier = Modifier.fillMaxWidth().clickable { c.write { repo.current(b.id) }; close() }.padding(vertical = 8.dp))
        Text(w.t("Изменить формулировку", "Edit the wording"), style = T.Body.copy(color = C.ink), modifier = Modifier.fillMaxWidth().clickable { close(); nav.push(Overlay.Dossier(b.id)) }.padding(vertical = 8.dp))
        Text(
            if (sure) w.t("Точно стереть досье и всю работу по нему?", "Really erase the file and all its work?") else w.t("Стереть досье", "Erase the file"),
            style = T.Body.copy(color = C.red),
            modifier = Modifier.fillMaxWidth().clickable { if (sure) { c.write { repo.deleteBox(b) }; close(); nav.pop() } else sure = true }.padding(vertical = 8.dp),
        )
    }
}

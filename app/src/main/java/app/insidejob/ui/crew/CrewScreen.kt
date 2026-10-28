package app.insidejob.ui.crew

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.insidejob.domain.Crew
import app.insidejob.domain.date
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.art.figure
import app.insidejob.ui.box.Says
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.Sheet
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.P

/** Five on one job, each with their level. */
@Composable
fun CrewScreen(h: Home, nav: Nav) {
    val w = h.w
    Column(Modifier.fillMaxSize().background(C.paper).statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp)) {
            Kicker(w.t("Пятеро на одно дело", "Five on one job"), style = P.Kicker)
            Text(w.t("КОМАНДА", "THE CREW"), style = P.Big, modifier = Modifier.padding(top = 2.dp))
        }
        for (m in Crew.entries) {
            Gap(10)
            Member(h, m) { nav.push(Overlay.Member(m)) }
        }
        Gap(16)
        Text(
            w.t("Чем чаще зовёшь, тем они сильнее.", "The more you call them, the stronger they get."),
            style = P.Note.copy(color = C.ink2, fontSize = 21.sp), modifier = Modifier.padding(horizontal = 20.dp).rotate(-1.5f),
        )
    }
}

@Composable
private fun Member(h: Home, m: Crew, onClick: () -> Unit) {
    val w = h.w
    val lv = h.s.level(m)
    Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(108.dp).clickable(onClick = onClick)) {
        Canvas(Modifier.size(108.dp)) { figure(m) }
        Column(Modifier.weight(1f).fillMaxHeight().background(C.cream).border(2.dp, C.ink).padding(horizontal = 12.dp, vertical = 9.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(w.crew(m).uppercase(), style = P.Sub.copy(fontSize = 19.sp))
                Text("  " + w.trade(m).uppercase(), style = P.Kicker.copy(fontSize = 9.5.sp), modifier = Modifier.padding(bottom = 2.dp))
            }
            Text(w.skill(m), style = P.Small.copy(fontSize = 10.5.sp, lineHeight = 14.sp), modifier = Modifier.padding(top = 3.dp).weight(1f), maxLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { Box(Modifier.size(14.dp, 5.dp).background(if (it < lv) C.ink else C.paper2)) }
                val next = h.s.toNext(m)
                Text(if (next == null) w.t("  мастер", "  master") else w.t("  ещё $next до уровня", "  $next to next level"), style = P.Small.copy(fontSize = 9.5.sp))
            }
        }
    }
}

/** What one crew member has done, newest first. */
@Composable
fun MemberSheet(h: Home, m: Crew, onClose: () -> Unit) {
    val w = h.w
    val s = h.s
    Sheet(w.trade(m) + w.t(" · уровень ", " · level ") + s.level(m), w.crew(m), onClose, w.t("Закрыть", "Close")) {
        Says(m, w.skill(m))
        Gap(14)
        val rows: List<Triple<Long, Long, String>> = when (m) {
            Crew.PEN -> s.boxes.flatMap { b ->
                listOfNotNull(
                    b.appeal.takeIf { it.isNotBlank() }?.let { Triple(b.at, b.id, w.t("Апелляция: ", "Appeal: ") + it) },
                    b.engraving.takeIf { it.isNotBlank() }?.let { Triple(b.openedAt, b.id, w.t("Гравировка: ", "Engraving: ") + it) },
                )
            }
            Crew.DRIVER -> s.done.map { Triple(it.doneAt, it.boxId, it.task + " — " + w.result(it.result).lowercase()) }
            else -> s.moves.filter { it.who == m.name }.map { Triple(it.at, it.boxId, it.text) }
        }.sortedByDescending { it.first }
        if (rows.isEmpty()) Text(w.t("Ещё ни одного дела. Позови из чертежа любой ячейки.", "No jobs yet. Call them from any box's plan."), style = P.Body)
        for ((at, box, text) in rows.take(40)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(w.short(at.date(h.zone)) + (s.box(box)?.let { " · " + w.no(it.no) } ?: ""), style = P.Kicker)
                Text(text, style = P.Body.copy(fontSize = 13.sp))
            }
        }
    }
}

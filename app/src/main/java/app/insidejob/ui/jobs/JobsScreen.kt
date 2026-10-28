package app.insidejob.ui.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.insidejob.container
import app.insidejob.domain.Crew
import app.insidejob.domain.Op
import app.insidejob.domain.Sys
import app.insidejob.domain.date
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.box.Says
import app.insidejob.ui.box.SysPick
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Chip
import app.insidejob.ui.components.Faith
import app.insidejob.ui.components.Field
import app.insidejob.ui.components.Flow
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.Kind
import app.insidejob.ui.components.LocalToast
import app.insidejob.ui.components.Sheet
import app.insidejob.ui.components.Stamp
import app.insidejob.ui.components.ruled
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.P
import app.insidejob.ui.theme.T

/** The desk with the jobs: the ones waiting for a report on top, the stamped ones below. */
@Composable
fun JobsScreen(h: Home, nav: Nav) {
    val s = h.s
    val w = h.w
    Column(Modifier.fillMaxSize().background(C.desk).statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp)) {
            Kicker(w.t("Шофёр и протоколы", "The driver's records"), style = T.Kicker.copy(color = Color(0xFFB9A88C)))
            Text(w.t("ДЕЛА", "JOBS"), style = T.Big.copy(color = C.paper), modifier = Modifier.padding(top = 2.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Score(s.done.size.toString(), w.t("дела", "jobs"), Modifier.weight(1f))
            Score(s.cameTrue.toString(), w.t("сбылось", "came true"), Modifier.weight(1f))
            Score(s.oddsDrop?.let { "−$it%" } ?: "—", w.t("вера охране", "faith in the guard"), Modifier.weight(1f))
        }
        Column(Modifier.padding(horizontal = 14.dp)) {
            val job = s.job
            if (job != null) Btn(w.t("+ Новая операция", "+ New job"), Modifier.fillMaxWidth()) { nav.push(Overlay.Job(job.id, null)) }
            else Text(w.t("Операции планируют против запертой ячейки. Сначала заведи досье в банке.", "Jobs are planned against a locked box. Open a file in the bank first."), style = T.Body.copy(color = C.paper2))
            if (s.ops.isEmpty() && job != null) {
                Gap(14)
                Text(
                    w.t("Операция — это проверка делом. Охрана делает прогноз, ты идёшь и смотришь, что будет на самом деле. Самые маленькие операции тоже считаются.", "A job is a test in real life. The guard makes a forecast; you go and see what really happens. The smallest jobs count too."),
                    style = T.Body.copy(color = C.paper2),
                )
            }
            for (op in s.pending) { Gap(18); Folder(h, op) { nav.push(Overlay.Report(op.id)) } }
            if (s.done.isNotEmpty()) { Gap(20); Kicker(w.t("Закрытые дела", "Closed jobs"), style = T.Kicker.copy(color = Color(0xFFB9A88C))) }
            for (op in s.done) { Gap(16); Folder(h, op) { nav.push(Overlay.Report(op.id)) } }
        }
    }
}

@Composable
private fun Score(big: String, label: String, modifier: Modifier) {
    Column(modifier.background(Color(0xFF1A1613)).border(1.dp, C.deskLine).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(big, style = T.Head.copy(color = C.mustard))
        Text(label.uppercase(), style = T.Kicker.copy(color = Color(0xFFB9A88C), fontSize = 9.sp), textAlign = TextAlign.Center)
    }
}

/** A job's folder, typed up, stamped when it's closed. */
@Composable
fun Folder(h: Home, op: Op, onClick: () -> Unit) {
    val w = h.w
    val b = h.s.box(op.boxId)
    Box(Modifier.fillMaxWidth()) {
        Box(Modifier.offset(y = (-10).dp).size(120.dp, 14.dp).background(C.paper2))
        Column(Modifier.fillMaxWidth().ruled().clickable(onClick = onClick).padding(16.dp)) {
            Kicker(
                (if (op.done) w.t("Закрыто · ", "Closed · ") + w.short(op.doneAt.date(h.zone)) else w.t("Сов. секретно · ", "Top secret · ") + w.short(op.at.date(h.zone))) +
                    (b?.let { " · " + w.no(it.no) } ?: ""),
                style = P.Kicker.copy(color = if (op.done) C.muted else C.red),
            )
            Text(op.task, style = P.Sub, modifier = Modifier.padding(top = 6.dp))
            Gap(8)
            Kicker(w.t("Прогноз охраны", "The guard's forecast"), style = P.Kicker)
            Text(op.guard, style = T.Typed)
            Odds(op.odds)
            if (op.done) {
                if (op.outcome.isNotBlank()) {
                    Gap(6)
                    Kicker(w.t("Что было на самом деле", "What really happened"), style = P.Kicker)
                    Text(op.outcome, style = T.Typed)
                }
                if (op.oddsAfter >= 0) { Gap(4); Kicker(w.t("Вера охране после", "Faith in the guard after"), style = P.Kicker); Odds(op.oddsAfter, C.ink) }
                Gap(10)
                Stamp(w.result(op.result), Modifier.rotate(-6f), color = if (op.result == 2) C.ink2 else C.red, sub = w.t("сигнализация", "the alarm"))
            } else {
                Text(w.t("Отчёт ›", "Report ›"), style = P.Small.copy(color = C.red), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun Odds(v: Int, color: Color = C.red) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(10.dp).border(1.5.dp, C.ink)) { Box(Modifier.fillMaxWidth(v / 100f).height(10.dp).background(color)) }
        Text("$v%", style = P.Sub.copy(fontSize = 15.sp), modifier = Modifier.padding(start = 8.dp))
    }
}

/** The Driver plans a job against one box. */
@Composable
fun JobSheet(h: Home, id: Long, sys: Sys?, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = h.s.box(id) ?: return onClose()
    var task by remember { mutableStateOf("") }
    var guard by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("") }
    var odds by remember { mutableIntStateOf(80) }
    var pick by remember { mutableStateOf(sys ?: h.s.active(b).firstOrNull() ?: b.sys.firstOrNull()) }
    var size by remember { mutableIntStateOf(1) }
    Sheet(w.crew(Crew.DRIVER) + " · " + w.no(b.no), w.t("Операция", "A job"), onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Завести мотор", "Start the engine"), Modifier.fillMaxWidth(), enabled = task.isNotBlank() && guard.isNotBlank()) {
            c.write { repo.job(id, task, guard, odds, pick, ref) }
            toast.show(w.t("Операция в папке. Шофёр ждёт отчёта.", "The job's in the folder. The driver waits for the report."))
            onClose()
        }
    }) {
        Says(Crew.DRIVER, w.t("Выберем маленькое дело, которое проверит охрану. Сделаешь — расскажешь, что было.", "Let's pick a small job that tests the guard. Do it, then tell me what happened."))
        Gap(10)
        Text("«${b.text}»", style = P.Small)
        Gap(10)
        Kicker(w.t("Готовые операции", "Ready jobs"), style = P.Kicker)
        Flow(Modifier.padding(top = 6.dp)) {
            for (n in 1..3) Chip(when (n) { 1 -> w.t("на десять минут", "ten minutes"); 2 -> w.t("посерьёзнее", "bigger"); else -> w.t("большая", "big") }, size == n) { size = n }
        }
        val used = h.s.ops.map { it.ref }.toSet()
        val jobs = h.s.cat.jobs.filter { it.sphere.name == b.sphere && it.size == size && it.id !in used }
        Gap(6)
        for (j in jobs.take(8)) {
            val on = ref == j.id
            Column(
                Modifier.fillMaxWidth().padding(bottom = 6.dp).background(if (on) C.ink else C.cream).border(1.dp, C.ink2)
                    .clickable { ref = j.id; task = w.task(j); guard = w.guard(j) }.padding(horizontal = 12.dp, vertical = 9.dp),
            ) {
                Text(w.task(j), style = P.Body.copy(color = if (on) C.paper else C.ink))
                Text(w.t("охрана: ", "the guard: ") + w.guard(j), style = P.Small.copy(color = if (on) C.mustard else C.muted))
            }
        }
        Gap(10)
        Kicker(w.t("Задача", "The task"), style = P.Kicker)
        Field(task, { task = it; ref = "" }, w.t("Попросить коллегу посмотреть отчёт до сдачи", "Ask a colleague to look over the report"), lines = 2)
        Gap(8)
        Kicker(w.t("Прогноз охраны: что случится", "The guard's forecast: what will happen"), style = P.Kicker)
        Field(guard, { guard = it }, w.t("Вздохнёт и решит, что я не справляюсь", "They'll sigh and decide I can't cope"), lines = 2)
        Gap(8)
        Kicker(w.t("Насколько охрана уверена", "How sure the guard is"), style = P.Kicker)
        Faith(odds, { odds = it })
        Kicker(w.t("Какую систему проверяем", "Which system it tests"), style = P.Kicker)
        SysPick(h, b, pick) { pick = it }
        Gap(12)
    }
}

/** After the job: what really happened, and the stamp. */
@Composable
fun ReportSheet(h: Home, id: Long, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val op = h.s.ops.firstOrNull { it.id == id } ?: return onClose()
    var outcome by remember { mutableStateOf(op.outcome) }
    var result by remember { mutableIntStateOf(op.result) }
    var after by remember { mutableIntStateOf(if (op.oddsAfter >= 0) op.oddsAfter else op.odds / 2) }
    var sure by remember { mutableStateOf(false) }
    Sheet(w.crew(Crew.DRIVER) + w.t(" · отчёт", " · report"), op.task, onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Поставить штамп", "Stamp it"), Modifier.fillMaxWidth(), enabled = result >= 0) {
            c.write { repo.report(id, outcome, result, after) }
            toast.show(
                when (result) {
                    0 -> w.t("Сигнализация не сработала. Охрана соврала.", "The alarm stayed quiet. The guard lied.")
                    1 -> w.t("Звякнула, но не завыла. Уже кое-что.", "It rang, but didn't wail. That's something.")
                    else -> w.t("Сработала. Бывает: это тоже разведка.", "It went off. It happens: that's recon too.")
                },
            )
            onClose()
        }
    }) {
        Kicker(w.t("Прогноз охраны", "The guard's forecast"), style = P.Kicker)
        Text(op.guard + " — ${op.odds}%", style = T.Typed)
        Gap(12)
        Kicker(w.t("Что было на самом деле", "What really happened"), style = P.Kicker)
        Field(outcome, { outcome = it }, w.t("Нашла две опечатки. Сказала: «Хорошо, что спросил».", "Found two typos. Said: “Good thing you asked.”"), lines = 4, max = 400)
        Gap(12)
        Kicker(w.t("Сигнализация", "The alarm"), style = P.Kicker)
        Flow(Modifier.padding(top = 6.dp)) {
            for (r in 0..2) Chip(w.result(r) + when (r) { 0 -> w.t(" — не сбылось", ": didn't happen"); 1 -> w.t(" — отчасти", ": partly"); else -> w.t(" — сбылось", ": came true") }, result == r) { result = r }
        }
        Gap(12)
        Kicker(w.t("Насколько теперь веришь прогнозу", "How much you believe the forecast now"), style = P.Kicker)
        Faith(after, { after = it })
        Gap(14)
        Btn(if (sure) w.t("Точно убрать?", "Really remove it?") else w.t("Отменить операцию", "Call off the job"), Modifier.fillMaxWidth(), kind = Kind.GHOST) {
            if (sure) { c.write { repo.deleteJob(id) }; onClose() } else sure = true
        }
        Gap(12)
    }
}

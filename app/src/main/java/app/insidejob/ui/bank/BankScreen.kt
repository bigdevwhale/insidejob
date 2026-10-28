package app.insidejob.ui.bank

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.art.BankPlan
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.Kind
import app.insidejob.ui.components.Meter
import app.insidejob.ui.components.Plate
import app.insidejob.ui.components.blueprint
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.T

/** The bank in cross-section, today's job, and jobs waiting for a report. */
@Composable
fun BankScreen(h: Home, nav: Nav) {
    val s = h.s
    val w = h.w
    val job = s.job
    Column(Modifier.fillMaxSize().blueprint().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp, top = 10.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Kicker(w.t("Объект · чертёж № 1", "Target · drawing no. 1"))
                Text(w.t("БАНК «НЕЛЬЗЯ»", "THE BANK OF NO"), style = T.Big, modifier = Modifier.padding(top = 2.dp))
            }
            Text("⚙", style = T.Head.copy(color = C.line2), modifier = Modifier.clickable { nav.push(Overlay.Settings) }.padding(12.dp))
        }
        BankPlan(s, w, job, Modifier.padding(top = 4.dp)) { slot ->
            val b = slot.box
            when {
                b != null && b.open -> nav.push(Overlay.Loot(b.id))
                b != null -> nav.push(Overlay.Plan(b.id))
                else -> nav.push(Overlay.NewBox(level = slot.level))
            }
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            if (job == null) {
                Plate {
                    Kicker(if (s.boxes.isEmpty()) w.t("Банк ещё не изучен", "The bank hasn't been cased") else w.t("Всё вскрыто", "Everything's open"))
                    Text(
                        if (s.boxes.isEmpty()) w.t("С какого правила начнём? Возьми то, что мешает чаще всего: про работу, людей, деньги, тело.", "Which rule do we start with? Take the one that gets in the way most often: work, people, money, the body.")
                        else w.t("Ни одной запертой ячейки. Если охрана вернётся с чем-то новым, заведи на неё досье.", "Not one locked box. If the guard comes back with something new, open a file on it."),
                        style = T.Body, modifier = Modifier.padding(top = 6.dp),
                    )
                }
            } else {
                Plate(onClick = { nav.push(Overlay.Plan(job.id)) }) {
                    Kicker(w.t("Текущее дело · ", "Current job · ") + w.place(job.level) + " " + w.no(job.no))
                    Text("«${job.text}»", style = T.Sub, modifier = Modifier.padding(top = 4.dp))
                    Text(
                        buildAnnotatedString {
                            append(w.t("Внутри: ", "Inside: "))
                            withStyle(SpanStyle(color = C.mustard)) { append(job.loot.lowercase()) }
                            append(". ")
                            val n = job.sys.size
                            val off = n - s.active(job).size
                            append(w.t("Охрана: $n ${w.plural(n, "система", "системы", "систем")}, ${w.plural(off, "отключена", "отключены", "отключено")} $off.", "Security: $n ${w.plural(n, "system", "systems", "systems")}, $off down."))
                        },
                        style = T.Small.copy(color = C.line), modifier = Modifier.padding(top = 4.dp),
                    )
                    Meter((s.progress(job) * 10).toInt(), 10, Modifier.padding(top = 10.dp))
                }
                Gap(10)
                Btn(w.t("Открыть чертёж", "Open the plan"), Modifier.fillMaxWidth()) { nav.push(Overlay.Plan(job.id)) }
            }
            for (op in s.pending.take(3)) {
                Gap(10)
                Plate(edge = C.mustard, onClick = { nav.push(Overlay.Report(op.id)) }) {
                    Kicker(w.t("Операция ждёт отчёта", "A job waiting for its report"), style = T.Kicker.copy(color = C.mustard))
                    Text(op.task, style = T.Body.copy(color = C.paper), modifier = Modifier.padding(top = 4.dp))
                    Text(w.t("Как всё прошло? ›", "How did it go? ›"), style = T.Small.copy(color = C.orange), modifier = Modifier.padding(top = 4.dp))
                }
            }
            s.duty()?.let { (day, part) ->
                Gap(14)
                Text(w.t("Тень: охрана чаще всего дежурит ", "Shadow: the guard is on duty most ") + w.weekdays(day) + ", " + w.part(part) + ".", style = T.Note.copy(fontSize = 19.sp))
            }
            Gap(12)
            Btn(w.t("+ Новая ячейка", "+ New box"), Modifier.fillMaxWidth(), kind = Kind.LINE) { nav.push(Overlay.NewBox()) }
        }
    }
}

private val Int.sp get() = androidx.compose.ui.unit.TextUnit(toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

package app.insidejob.ui.box

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.insidejob.container
import app.insidejob.domain.Box
import app.insidejob.domain.Crew
import app.insidejob.domain.Crisis
import app.insidejob.domain.HP
import app.insidejob.domain.Sys
import app.insidejob.domain.date
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.Words
import app.insidejob.ui.art.figure
import app.insidejob.ui.art.glyph
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Card
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
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.P
import app.insidejob.ui.theme.T

/** Which of the box's systems a move hits. */
@Composable
fun SysPick(h: Home, b: Box, picked: Sys?, onPick: (Sys) -> Unit) {
    val w = h.w
    Flow(Modifier.padding(top = 6.dp)) {
        for (s in b.sys) {
            val off = h.s.off(b, s)
            Chip(w.device(s) + if (off) " ✓" else "", s == picked) { onPick(s) }
        }
    }
}

/** A crew member's face beside a line of their speech. */
@Composable
fun Says(who: Crew, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Canvas(Modifier.size(56.dp).border(1.5.dp, C.ink)) { figure(who) }
        Text(text, style = P.Body.copy(fontStyle = FontStyle.Italic), modifier = Modifier.padding(start = 12.dp).weight(1f))
    }
}

/** A hall of the plan: what the system is and who can take it down. */
@Composable
fun GuardSheet(h: Home, nav: Nav, id: Long, sys: Sys, onClose: () -> Unit) {
    val w = h.w
    val b = h.s.box(id) ?: return onClose()
    val hits = h.s.hits(b, sys).coerceAtMost(HP)
    Sheet(w.device(sys), "«" + w.sys(sys) + "»", onClose, w.t("Закрыть", "Close")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(64.dp).background(C.navy)) { glyph(sys, center, size.minDimension * .34f, if (hits >= HP) C.cyan else C.line, hits >= HP) }
            Column(Modifier.padding(start = 12.dp)) {
                Text(if (hits >= HP) w.t("Отключена", "Down") else w.t("Активна", "Live"), style = P.Sub.copy(color = if (hits >= HP) C.navy2 else C.red))
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(HP) { Box(Modifier.size(18.dp, 6.dp).background(if (it < hits) C.navy2 else C.paper2)) }
                }
                Text(w.t("попаданий $hits из $HP", "$hits of $HP hits"), style = P.Small)
            }
        }
        Gap(12)
        Text(w.how(sys), style = P.Body)
        Gap(8)
        Text(w.beat(sys), style = P.Small)
        Gap(16)
        Kicker(w.t("Кто идёт", "Who goes"), style = P.Kicker)
        Gap(6)
        for ((m, what) in listOf(
            Crew.LOCK to w.t("доказательство против · 1 попадание", "evidence against · 1 hit"),
            Crew.PROF to w.t("вопрос из-за стены · 1 попадание", "a question through the wall · 1 hit"),
            Crew.DRIVER to w.t("проверить в деле · до 2 попаданий", "test it for real · up to 2 hits"),
        )) {
            Card(Modifier.padding(bottom = 8.dp), onClick = {
                nav.swap(
                    when (m) {
                        Crew.LOCK -> Overlay.Evidence(id, sys)
                        Crew.PROF -> Overlay.Question(id, sys)
                        else -> Overlay.Job(id, sys)
                    },
                )
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(40.dp)) { figure(m) }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(w.crew(m).uppercase(), style = P.Sub)
                        Text(what, style = P.Small)
                    }
                    Text("›", style = P.Head.copy(color = C.red))
                }
            }
        }
    }
}

/** The Pick: one piece of evidence against the guard. */
@Composable
fun EvidenceSheet(h: Home, id: Long, sys: Sys?, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = h.s.box(id) ?: return onClose()
    var pick by remember { mutableStateOf(sys ?: h.s.active(b).firstOrNull() ?: b.sys.firstOrNull()) }
    var text by remember { mutableStateOf("") }
    var help by remember { mutableStateOf(false) }
    if (help) return HelpSheet(h) { help = false }
    Sheet(w.crew(Crew.LOCK) + " · " + w.trade(Crew.LOCK), w.t("Доказательство против", "Evidence against"), onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Подшить в дело", "File it"), Modifier.fillMaxWidth(), enabled = text.isNotBlank()) {
            if (Crisis.check(text)) { help = true; return@Btn }
            c.write { repo.move(id, Crew.LOCK, text, pick) }
            toast.show(pick?.let { w.t("Луч погас: ", "A beam goes out: ") + w.device(it).lowercase() } ?: w.t("Подшито", "Filed"))
            onClose()
        }
    }) {
        Says(Crew.LOCK, w.t("Охрана говорит: «${b.voice}». Найди случай, когда вышло иначе. Факт, чьи-то слова, твой поступок.", "The guard says: “${b.voice}”. Find a time it went otherwise. A fact, someone's words, something you did."))
        Gap(12)
        Text("«${b.text}»", style = P.Sub)
        Gap(10)
        Field(text, { text = it }, w.t("В марте попросил Лёшу помочь с переездом. Он приехал с тортом.", "In March I asked Alex to help me move. He turned up with a cake."), lines = 4, max = 400)
        Gap(10)
        Kicker(w.t("Какую систему гасит", "Which system it hits"), style = P.Kicker)
        SysPick(h, b, pick) { pick = it }
    }
}

/** The Professor: a question through the wall, and the answer that makes the lock click. */
@Composable
fun QuestionSheet(h: Home, id: Long, sys: Sys?, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = h.s.box(id) ?: return onClose()
    val qs = remember(id, sys) { h.s.questions(b).let { all -> if (sys != null) all.filter { it.sys == sys || it.sys == null }.sortedBy { if (it.sys == sys) 0 else 1 } else all } }
    var i by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var help by remember { mutableStateOf(false) }
    if (help) return HelpSheet(h) { help = false }
    val q = qs.getOrNull(i % maxOf(1, qs.size))
    val target = q?.sys?.takeIf { it in b.sys } ?: sys ?: h.s.active(b).firstOrNull() ?: b.sys.firstOrNull()
    Sheet(w.crew(Crew.PROF) + " · " + w.trade(Crew.PROF), w.t("Стучат из-за стены", "A knock through the wall"), onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Простучать ответ", "Tap back the answer"), Modifier.fillMaxWidth(), enabled = text.isNotBlank() && q != null) {
            if (Crisis.check(text)) { help = true; return@Btn }
            c.write { repo.move(id, Crew.PROF, text, target, ref = q!!.id) }
            toast.show(w.t("Щёлк. Механизм поддался.", "Click. The mechanism gave."))
            onClose()
        }
    }) {
        Text("•• ••• · • •••• · ••• •", style = P.Sub.copy(color = C.muted, letterSpacing = 3.sp))
        Gap(8)
        if (q == null) Text(w.t("Профессор молчит: вопросов не нашлось.", "The Professor is silent: no questions found."), style = P.Body)
        else Says(Crew.PROF, "«" + w.text(q) + "»")
        Text(w.t("Другой вопрос ›", "Another question ›"), style = P.Small.copy(color = C.red), modifier = Modifier.clickable { i++; text = "" }.padding(vertical = 10.dp))
        Text("«${b.text}»", style = P.Small)
        Gap(8)
        Field(text, { text = it }, w.t("Твой ответ, честно и коротко", "Your answer, honest and short"), lines = 4, max = 400)
        if (target != null) Text(w.t("Механизм: ", "Mechanism: ") + w.device(target).lowercase(), style = P.Small, modifier = Modifier.padding(top = 8.dp))
    }
}

/** The Shadow: when the guard spoke today. */
@Composable
fun SightingSheet(h: Home, id: Long, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = h.s.box(id) ?: return onClose()
    var text by remember { mutableStateOf("") }
    var pick by remember { mutableStateOf<Sys?>(null) }
    Sheet(w.crew(Crew.SHADOW) + " · " + w.trade(Crew.SHADOW), w.t("Охрана на посту", "The guard on duty"), onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Записать в журнал", "Log it"), Modifier.fillMaxWidth(), enabled = text.isNotBlank()) {
            c.write { repo.move(id, Crew.SHADOW, text, pick) }
            toast.show(w.t("Тень записала смену охраны.", "The Shadow noted the guard's shift."))
            onClose()
        }
    }) {
        Says(Crew.SHADOW, w.t("Когда сегодня звучало «${b.voice}»? Где ты {был|была}, кто был рядом, что ты сделал потом.", "When did “${b.voice}” sound today? Where you were, who was around, what you did next."))
        Gap(12)
        Field(text, { text = it }, w.t("Планёрка, начальник спросил, кто возьмёт отчёт. Промолчал.", "The stand-up; the boss asked who'd take the report. Said nothing."), lines = 3, max = 300)
        Gap(10)
        Kicker(w.t("Какая система включилась (если понятно)", "Which system came on (if you can tell)"), style = P.Kicker)
        SysPick(h, b, pick) { pick = if (pick == it) null else it }
        val seen = h.s.moves(b).filter { it.who == Crew.SHADOW.name }
        if (seen.isNotEmpty()) {
            Gap(14)
            Kicker(w.t("Журнал Тени", "The Shadow's log"), style = P.Kicker)
            for (m in seen.takeLast(5).reversed()) Text(w.short(m.at.date(h.zone)) + " · " + m.text, style = P.Small, modifier = Modifier.padding(top = 4.dp))
            h.s.duty(b)?.let { (d, p) -> Text(w.t("Чаще всего ", "Mostly ") + w.weekdays(d) + ", " + w.part(p), style = P.Note, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}

/** Recon and the Quill: the old verdict, who passed it, and the appeal. */
@Composable
fun DossierSheet(h: Home, id: Long, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = h.s.box(id) ?: return onClose()
    var text by remember { mutableStateOf(b.text) }
    var voice by remember { mutableStateOf(b.voice) }
    var loot by remember { mutableStateOf(b.loot) }
    var age by remember { mutableStateOf(b.age) }
    var judge by remember { mutableStateOf(b.judge) }
    var scene by remember { mutableStateOf(b.scene) }
    var appeal by remember { mutableStateOf(b.appeal) }
    Sheet(w.t("Досье · ", "Dossier · ") + w.place(b.level) + " " + w.no(b.no), w.t("Разведка и апелляция", "Recon and appeal"), onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Сохранить досье", "Save the dossier"), Modifier.fillMaxWidth(), enabled = text.isNotBlank()) {
            c.write { repo.edit(b.copy(text = text.trim(), voice = voice.trim(), loot = loot.trim(), age = age.trim(), judge = judge.trim(), scene = scene.trim(), appeal = appeal.trim())) }
            if (appeal.isNotBlank() && b.appeal.isBlank()) toast.show(w.t("Перо переписало приговор.", "The Quill rewrote the verdict."))
            onClose()
        }
    }) {
        Kicker(w.t("Убеждение", "The belief"), style = P.Kicker)
        Field(text, { text = it }, "", lines = 2)
        Gap(8)
        Kicker(w.t("Как говорит охрана", "How the guard puts it"), style = P.Kicker)
        Field(voice, { voice = it }, w.t("«Сам. Не позорься.»", "“Do it yourself. Don't embarrass yourself.”"), max = 80)
        Gap(8)
        Kicker(w.t("Что лежит внутри", "What's inside"), style = P.Kicker)
        Field(loot, { loot = it }, w.t("Право на …", "The right to …"), max = 80)
        Gap(16)
        Says(Crew.SHADOW, w.t("Каждый замок кто-то поставил. Когда ты впервые это {услышал|услышала} и от кого?", "Someone installed every lock. When did you first hear this, and from whom?"))
        Gap(10)
        Kicker(w.t("Сколько тебе было лет", "How old you were"), style = P.Kicker)
        Field(age, { age = it }, w.t("лет восемь", "about eight"), max = 40)
        Gap(8)
        Kicker(w.t("Кто вынес приговор", "Who passed the verdict"), style = P.Kicker)
        Field(judge, { judge = it }, w.t("отец, учительница, первая любовь…", "dad, a teacher, a first love…"), max = 60)
        Gap(8)
        Kicker(w.t("Как это было", "How it went"), style = P.Kicker)
        Field(scene, { scene = it }, w.t("Плакал над задачей, попросил помочь. «Сам. Мужик должен сам».", "Cried over homework, asked for help. “Do it yourself.”"), lines = 3, max = 300)
        Gap(16)
        Says(Crew.PEN, w.t("Тот приговор писал ребёнок под диктовку. Перепиши его так, как сказал бы взрослый, который тебя любит.", "That verdict was written by a child, to dictation. Rewrite it the way a grown-up who loves you would."))
        Gap(10)
        Field(appeal, { appeal = it }, w.t("Восьмилетнему положено просить. Отец сам не умел.", "An eight-year-old is meant to ask. Dad never learned how himself."), lines = 3, max = 300)
        if (b.appeal.isNotBlank()) {
            Gap(12)
            Stamp(w.t("Дело пересмотрено", "Case reviewed"), Modifier.rotate(-6f))
        }
        Gap(12)
    }
}

/** How much the guard is believed now. */
@Composable
fun FaithSheet(h: Home, id: Long, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val b = h.s.box(id) ?: return onClose()
    var v by remember { mutableIntStateOf(b.faith) }
    Sheet(w.place(b.level) + " " + w.no(b.no), w.t("Насколько веришь охране", "How much you believe the guard"), onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Записать", "Record it"), Modifier.fillMaxWidth()) { c.write { repo.faith(b, v) }; onClose() }
    }) {
        Text("«${b.text}»", style = P.Sub)
        Gap(10)
        Text(w.t("Не как надо бы, а как чувствуется сейчас. Цифры нужны, чтобы видеть, как охрана теряет голос.", "Not as it should be, but as it feels right now. The numbers show the guard losing its voice."), style = P.Small)
        Gap(10)
        Faith(v, { v = it })
        Gap(12)
        Kicker(w.t("Как менялось", "How it changed"), style = P.Kicker)
        val log = h.s.faithLog(b)
        Row(Modifier.fillMaxWidth().height(70.dp).padding(top = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for ((_, f) in log.takeLast(16)) Box(Modifier.weight(1f).height((6 + f * .6f).dp).background(C.red.copy(alpha = .35f + f / 200f)))
        }
        Text(log.firstOrNull()?.let { w.short(it.first.date(h.zone)) + " — " + w.t("сейчас", "now") } ?: "", style = P.Small)
    }
}

/** For words that mean someone may be in danger. */
@Composable
fun HelpSheet(h: Home, onClose: () -> Unit) {
    val w = h.w
    val ctx = LocalContext.current
    fun open(uri: String) { runCatching { ctx.startActivity(Intent(if (uri.startsWith("tel:")) Intent.ACTION_DIAL else Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
    Sheet(w.t("Стоп, дело подождёт", "Stop, the job can wait"), w.t("Ты не {один|одна}", "You're not alone"), onClose, w.t("Закрыть", "Close")) {
        Text(w.t("Если ты думаешь о том, чтобы причинить себе вред, это слишком тяжело, чтобы нести одному. Позвони или напиши тем, кто умеет помочь: это бесплатно и анонимно.",
            "If you're thinking about hurting yourself, that's too heavy to carry alone. Call or write to people who know how to help: it's free and anonymous."), style = P.Body)
        Gap(14)
        Card(onClick = { open("tel:112") }) {
            Text(w.t("Экстренная помощь — 112", "Emergency — 112 / 911 / 999"), style = P.Sub)
            Text(w.t("Если опасность прямо сейчас", "If you're in danger right now"), style = P.Small)
        }
        Gap(8)
        if (w.ru) {
            Card(onClick = { open("tel:88002000122") }) {
                Text("8-800-2000-122", style = P.Sub)
                Text("Телефон доверия для детей, подростков и родителей, бесплатно по России", style = P.Small)
            }
            Gap(8)
            Card(onClick = { open("tel:+74959895050") }) {
                Text("+7 495 989-50-50", style = P.Sub)
                Text("Экстренная психологическая помощь МЧС, круглосуточно", style = P.Small)
            }
            Gap(8)
        }
        Card(onClick = { open("https://findahelpline.com") }) {
            Text("findahelpline.com", style = P.Sub)
            Text(w.t("Бесплатные линии помощи в любой стране", "Free helplines in any country"), style = P.Small)
        }
        Gap(14)
        Text(w.t("И, если можешь, скажи кому-то рядом: «Мне сейчас очень плохо». Этого достаточно для начала.", "And if you can, tell someone near you: “I'm not okay right now.” That's enough to start."), style = P.Body.copy(fontStyle = FontStyle.Italic))
        Gap(14)
        Btn(w.t("Вернуться", "Go back"), Modifier.fillMaxWidth(), kind = Kind.GHOST, onClick = onClose)
    }
}

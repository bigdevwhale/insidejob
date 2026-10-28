package app.insidejob.ui.box

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.insidejob.container
import app.insidejob.domain.Crew
import app.insidejob.domain.Crisis
import app.insidejob.domain.Sphere
import app.insidejob.domain.Sys
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Chip
import app.insidejob.ui.components.Faith
import app.insidejob.ui.components.Field
import app.insidejob.ui.components.Flow
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.LocalToast
import app.insidejob.ui.components.Sheet
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.P
import kotlinx.coroutines.launch

/**
 * Opens a file on a new box: from the catalogue or in one's own words.
 * Opened from another box ([under]), it is the downward arrow: what would it mean if the upper belief were true?
 */
@Composable
fun NewBoxSheet(h: Home, nav: Nav, under: Long, startLevel: Int, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val scope = rememberCoroutineScope()
    val upper = h.s.box(under)
    var sphere by remember { mutableStateOf(upper?.let { Sphere.of(it.sphere) } ?: Sphere.WORK) }
    var level by remember { mutableIntStateOf(startLevel) }
    var text by remember { mutableStateOf("") }
    var voice by remember { mutableStateOf("") }
    var loot by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("") }
    val systems = remember { mutableStateListOf<Sys>() }
    var faith by remember { mutableIntStateOf(70) }
    var help by remember { mutableStateOf(false) }
    if (help) return HelpSheet(h) { help = false }
    val used = h.s.boxes.map { it.ref }.toSet()
    val ready = text.isNotBlank() && systems.isNotEmpty()

    Sheet(
        if (upper != null) w.t("Спуск ниже · ", "Going down · ") + w.place(upper.level) + " " + w.no(upper.no) else w.t("Новое досье", "A new file"),
        if (upper != null) w.t("Что под этим?", "What's under this?") else w.t("Какая ячейка следующая", "Which box is next"),
        onClose, w.t("Закрыть", "Close"),
        foot = {
            if (!ready) Text(
                if (text.isBlank()) w.t("Сформулируй убеждение или возьми из списка.", "Word the belief or take one from the list.") else w.t("Отметь хотя бы одну систему охраны.", "Mark at least one security system."),
                style = P.Small, modifier = Modifier.padding(bottom = 8.dp),
            )
            Btn(w.t("Завести досье", "Open the file"), Modifier.fillMaxWidth(), enabled = ready) {
                if (Crisis.check(text) || Crisis.check(voice)) { help = true; return@Btn }
                val l = loot.ifBlank { w.t("Право жить без этого правила", "The right to live without this rule") }
                val v = voice.ifBlank { text }
                scope.launch {
                    val id = c.repo.box(text, level, sphere.name, systems.toList(), v, l, faith, ref, under)
                    if (upper != null) {
                        toast.show(w.t("Под ячейкой нашлось ", "Found under the box: ") + w.place(level) + ".")
                        onClose()
                    } else nav.swap(Overlay.Plan(id))
                }
            }
        },
    ) {
        if (upper != null) {
            Says(Crew.PROF, w.t("Допустим, «${upper.text}» — правда. Что это говорит о тебе? О людях? Как устроен мир, если так?", "Say “${upper.text}” is true. What does it say about you? About people? What is the world like, if so?"))
            Gap(12)
            val lower = h.s.boxes.filter { it.level > upper.level && it.id != upper.id }
            if (lower.isNotEmpty()) {
                Kicker(w.t("Или это уже лежит в банке", "Or it's already in the bank"), style = P.Kicker)
                Flow(Modifier.padding(top = 6.dp)) {
                    for (b in lower) Chip(w.no(b.no) + " «" + b.text.take(28) + "…»", false) {
                        c.write { repo.rest(upper, b.id) }
                        toast.show(w.t("Красная нить протянута.", "Red string tied."))
                        onClose()
                    }
                }
                Gap(14)
            }
        }
        Kicker(w.t("Сфера", "Sphere"), style = P.Kicker)
        Flow(Modifier.padding(top = 6.dp)) { for (sp in Sphere.entries) Chip(w.sphere(sp), sp == sphere) { sphere = sp } }
        Gap(12)
        Kicker(w.t("Этаж", "Floor"), style = P.Kicker)
        Flow(Modifier.padding(top = 6.dp)) {
            for (l in 1..3) if (upper == null || l > upper.level) Chip(w.floor(l), l == level) { level = l }
        }
        Gap(12)
        val picks = h.s.cat.beliefs.filter { it.sphere == sphere && it.level == level && it.id !in used }
        if (picks.isNotEmpty()) {
            Kicker(w.t("Частые ячейки этого этажа", "Common boxes on this floor"), style = P.Kicker)
            Gap(6)
            for (b in picks.take(12)) {
                val on = ref == b.id
                Column(
                    Modifier.fillMaxWidth().padding(bottom = 6.dp).background(if (on) C.ink else C.cream).border(1.dp, C.ink2)
                        .clickable {
                            ref = b.id; text = w.text(b); voice = w.voice(b); loot = w.loot(b)
                            systems.clear(); systems.addAll(b.systems)
                        }.padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    Text(w.text(b), style = P.Body.copy(color = if (on) C.paper else C.ink))
                    Text(w.loot(b), style = P.Small.copy(color = if (on) C.mustard else C.muted))
                }
            }
            Gap(10)
        }
        Kicker(w.t("Своими словами", "In your own words"), style = P.Kicker)
        Field(text, { text = it; ref = "" }, w.t("Если попрошу о помощи, меня сочтут слабым", "If I ask for help, they'll think I'm weak"), lines = 2)
        Gap(8)
        Kicker(w.t("Как говорит охрана", "How the guard puts it"), style = P.Kicker)
        Field(voice, { voice = it }, w.t("«Сам. Не позорься.»", "“Do it yourself. Don't embarrass yourself.”"), max = 80)
        Gap(8)
        Kicker(w.t("Что лежит внутри", "What's locked inside"), style = P.Kicker)
        Field(loot, { loot = it }, w.t("Право на помощь", "The right to ask for help"), max = 80)
        Gap(12)
        Kicker(w.t("Системы охраны (одна-три)", "Security systems (one to three)"), style = P.Kicker)
        Flow(Modifier.padding(top = 6.dp)) {
            for (s in Sys.entries) Chip(w.sys(s), s in systems) {
                if (s in systems) systems.remove(s) else if (systems.size < 3) systems.add(s)
            }
        }
        systems.lastOrNull()?.let { Text(w.how(it), style = P.Small, modifier = Modifier.padding(top = 8.dp)) }
        Gap(12)
        Kicker(w.t("Насколько веришь сейчас", "How much you believe it now"), style = P.Kicker)
        Faith(faith, { faith = it })
        Gap(12)
    }
}

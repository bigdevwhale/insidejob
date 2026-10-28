package app.insidejob.ui.loot

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.insidejob.container
import app.insidejob.domain.Box as Safe
import app.insidejob.domain.date
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Overlay
import app.insidejob.ui.art.loot
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Faith
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.Kind
import app.insidejob.ui.components.LocalToast
import app.insidejob.ui.components.Sheet
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.P
import app.insidejob.ui.theme.T

/** The velvet case: every right taken back, with the boxes still shut shown as dashed padlocks. */
@Composable
fun LootScreen(h: Home, nav: Nav) {
    val w = h.w
    val s = h.s
    Column(
        Modifier.fillMaxSize().background(Brush.radialGradient(listOf(C.velvet2, C.velvet, Color(0xFF1D0710)), radius = 1600f))
            .statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
    ) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp)) {
            Kicker(w.t("Вынесено из банка «Нельзя»", "Taken from the Bank of No"), style = T.Kicker.copy(color = C.rose))
            Text(w.t("ДОБЫЧА", "THE LOOT"), style = T.Big.copy(color = C.paper), modifier = Modifier.padding(top = 2.dp))
            Text(
                if (s.opened.isEmpty()) w.t("Витрина пока пустая. Первая вещь появится, когда вскроешь первую ячейку.", "The case is empty for now. The first thing appears when the first box is cracked.")
                else w.t("Каждая вещь — новое правило, выгравированное твоей рукой. В тяжёлый день её можно достать и прочитать.", "Each piece is a new rule engraved in your hand. On a hard day, take it out and read it."),
                style = T.Small.copy(color = C.rose), modifier = Modifier.padding(top = 6.dp),
            )
        }
        val items: List<Safe> = s.opened + s.shut.sortedWith(compareBy({ it.level }, { it.no }))
        for (row in items.chunked(2)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (b in row) Item(h, b, Modifier.weight(1f)) { if (b.open) nav.push(Overlay.Loot(b.id)) else nav.push(Overlay.Plan(b.id)) }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Item(h: Home, b: Safe, modifier: Modifier, onClick: () -> Unit) {
    val w = h.w
    Column(
        modifier.background(Color(0x47000000)).border(1.dp, if (b.open) C.gold.copy(alpha = .4f) else C.steel.copy(alpha = .3f)).clickable(onClick = onClick).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.fillMaxWidth().height(64.dp)) { loot((b.id % 4).toInt(), center, size.height * 1.7f, locked = !b.open) }
        Text(
            (if (b.open || b.level < 3) b.loot else w.t("Центральный сейф", "The central safe")).uppercase(),
            style = T.Sub.copy(color = if (b.open) C.gold else C.steel.copy(alpha = .6f), fontSize = 14.sp, textAlign = TextAlign.Center), modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            if (b.open) w.t("из ", "from ") + w.no(b.no) + " «" + b.text + "»" else w.no(b.no) + w.t(" · дело идёт", " · job in progress"),
            style = T.Small.copy(color = C.rose, fontSize = 9.5.sp, lineHeight = 13.sp, textAlign = TextAlign.Center), maxLines = 3, modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** One piece of loot: the engraving to read on a hard day, and the way back if the guard returns. */
@Composable
fun LootSheet(h: Home, nav: Nav, id: Long, onClose: () -> Unit) {
    val w = h.w
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val b = h.s.box(id) ?: return onClose()
    var back by remember { mutableStateOf(false) }
    var faith by remember { mutableIntStateOf(maxOf(b.faith, 50)) }
    Sheet(w.t("Добыча · ", "Loot · ") + w.no(b.no), b.loot, onClose, w.t("Закрыть", "Close")) {
        Canvas(Modifier.fillMaxWidth().height(120.dp).background(C.velvet)) { loot((b.id % 4).toInt(), center, size.height * 1.9f) }
        Gap(14)
        Kicker(w.t("Выгравировано", "Engraved"), style = P.Kicker)
        Text(b.engraving.ifBlank { b.loot }, style = P.Head.copy(fontStyle = FontStyle.Italic), modifier = Modifier.padding(top = 4.dp))
        Gap(14)
        Kicker(w.t("Откуда", "Where from"), style = P.Kicker)
        Text("«${b.text}»", style = P.Body)
        if (b.open) Text(w.t("Вскрыта ", "Cracked ") + w.date(b.openedAt.date(h.zone)) + w.t(". Вера охране: было ${b.faith0}%, стало ${b.faith}%.", ". Faith in the guard: was ${b.faith0}%, now ${b.faith}%."), style = P.Small, modifier = Modifier.padding(top = 6.dp))
        if (b.appeal.isNotBlank()) {
            Gap(12)
            Kicker(w.t("Апелляция", "The appeal"), style = P.Kicker)
            Text(b.appeal, style = T.Typed)
        }
        Gap(20)
        if (!back) {
            Btn(w.t("Охрана вернулась", "The guard came back"), Modifier.fillMaxWidth(), kind = Kind.GHOST) { back = true }
            Text(w.t("Бывает. Ячейку запрут снова, но часть старой работы останется.", "It happens. The box gets locked again, but part of the old work holds."), style = P.Small, modifier = Modifier.padding(top = 6.dp))
        } else {
            Text(w.t("Насколько снова веришь охране?", "How much do you believe the guard again?"), style = P.Body)
            Faith(faith, { faith = it })
            Btn(w.t("Запереть и вернуться к делу", "Lock it and get back to work"), Modifier.fillMaxWidth(), kind = Kind.INK) {
                c.write { repo.relock(b, faith) }
                toast.show(w.t("По одному попаданию в каждом зале осталось.", "One hit in every hall still holds."))
                onClose()
                nav.push(Overlay.Plan(b.id))
            }
        }
        Gap(16)
    }
}

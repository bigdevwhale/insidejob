package app.insidejob.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import app.insidejob.R
import app.insidejob.container
import app.insidejob.data.Settings
import app.insidejob.domain.Crew
import app.insidejob.domain.State
import app.insidejob.domain.Sys
import app.insidejob.notify.Bell
import app.insidejob.ru
import app.insidejob.ui.bank.BankScreen
import app.insidejob.ui.box.BoxScreen
import app.insidejob.ui.box.CrackScreen
import app.insidejob.ui.box.DossierSheet
import app.insidejob.ui.box.EvidenceSheet
import app.insidejob.ui.box.FaithSheet
import app.insidejob.ui.box.GuardSheet
import app.insidejob.ui.box.HelpSheet
import app.insidejob.ui.box.NewBoxSheet
import app.insidejob.ui.box.QuestionSheet
import app.insidejob.ui.box.SightingSheet
import app.insidejob.ui.components.LocalToast
import app.insidejob.ui.components.ToastHost
import app.insidejob.ui.components.Toaster
import app.insidejob.ui.crew.CrewScreen
import app.insidejob.ui.crew.MemberSheet
import app.insidejob.ui.jobs.JobSheet
import app.insidejob.ui.jobs.JobsScreen
import app.insidejob.ui.jobs.ReportSheet
import app.insidejob.ui.loot.LootScreen
import app.insidejob.ui.loot.LootSheet
import app.insidejob.ui.onboarding.Onboarding
import app.insidejob.ui.settings.SettingsSheet
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.Title
import kotlinx.coroutines.delay
import java.time.ZoneId

/** Everything a screen reads, at one moment. */
class Home(val s: State, val settings: Settings, val w: Words, val now: Long) {
    val ru get() = w.ru
    val zone: ZoneId get() = s.zone
}

enum class Tab { BANK, CREW, JOBS, LOOT }

sealed interface Overlay {
    /** A new box; [under] is the box whose foundation it will be, [level] its floor. */
    data class NewBox(val under: Long = 0, val level: Int = 1) : Overlay
    data class Plan(val box: Long) : Overlay
    data class Guard(val box: Long, val sys: Sys) : Overlay
    data class Evidence(val box: Long, val sys: Sys?) : Overlay
    data class Question(val box: Long, val sys: Sys?) : Overlay
    data class Sighting(val box: Long) : Overlay
    data class Dossier(val box: Long) : Overlay
    data class Job(val box: Long, val sys: Sys?) : Overlay
    data class Report(val op: Long) : Overlay
    data class Faith(val box: Long) : Overlay
    data class Crack(val box: Long) : Overlay
    data class Loot(val box: Long) : Overlay
    data class Member(val crew: Crew) : Overlay
    data object Settings : Overlay
    data object Help : Overlay
}

class Nav(val overlays: SnapshotStateList<Overlay>, val tab: (Tab) -> Unit) {
    fun push(o: Overlay) = overlays.add(o)
    fun pop() { if (overlays.isNotEmpty()) overlays.removeAt(overlays.lastIndex) }
    /** Replaces the top overlay, so back goes to what was under it. */
    fun swap(o: Overlay) { pop(); push(o) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobRoot(settings: Settings) {
    val ctx = LocalContext.current
    val c = ctx.container
    val state by c.repo.state.collectAsStateWithLifecycle()
    val toaster = remember { Toaster() }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(settings) { c.repo.refresh() }
    val s = state ?: run { Box(Modifier.fillMaxSize().background(C.navy)); return }
    val words = remember(ctx.ru, settings.fem, s.cat) { Words(ctx.ru, settings.fem, s.cat) }
    if (!settings.onboarded) {
        CompositionLocalProvider(LocalToast provides toaster) { Box(Modifier.fillMaxSize().background(C.navy)) { Onboarding(words, settings); ToastHost(toaster) } }
        return
    }
    val life = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        life.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            c.repo.refresh()
            Bell.plan(ctx)
            while (true) { now = System.currentTimeMillis(); delay(60_000) }
        }
    }
    val home = Home(s, settings, words, now)

    var tab by remember { mutableStateOf(Tab.BANK) }
    val overlays = remember { mutableStateListOf<Overlay>() }
    val nav = remember { Nav(overlays) { tab = it } }
    BackHandler(enabled = overlays.isEmpty() && tab != Tab.BANK) { tab = Tab.BANK }

    // Light status icons everywhere but on the crew's paper board.
    val view = LocalView.current
    val light = tab == Tab.CREW && overlays.isEmpty()
    LaunchedEffect(light) {
        (ctx as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = light }
    }

    CompositionLocalProvider(LocalToast provides toaster) {
        Box(Modifier.fillMaxSize().background(C.navy)) {
            Column(Modifier.fillMaxSize().imePadding()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        Tab.BANK -> BankScreen(home, nav)
                        Tab.CREW -> CrewScreen(home, nav)
                        Tab.JOBS -> JobsScreen(home, nav)
                        Tab.LOOT -> LootScreen(home, nav)
                    }
                }
                if (!WindowInsets.isImeVisible) TabBar(tab) { tab = it; overlays.clear() }
            }
            // The plan of a box lies under its sheets, so it stays put while a sheet is open over it.
            val plan = overlays.lastOrNull { it is Overlay.Plan } as? Overlay.Plan
            if (plan != null) BoxScreen(home, nav, plan.box)
            when (val o = overlays.lastOrNull()) {
                is Overlay.NewBox -> NewBoxSheet(home, nav, o.under, o.level) { nav.pop() }
                is Overlay.Guard -> GuardSheet(home, nav, o.box, o.sys) { nav.pop() }
                is Overlay.Evidence -> EvidenceSheet(home, o.box, o.sys) { nav.pop() }
                is Overlay.Question -> QuestionSheet(home, o.box, o.sys) { nav.pop() }
                is Overlay.Sighting -> SightingSheet(home, o.box) { nav.pop() }
                is Overlay.Dossier -> DossierSheet(home, o.box) { nav.pop() }
                is Overlay.Job -> JobSheet(home, o.box, o.sys) { nav.pop() }
                is Overlay.Report -> ReportSheet(home, o.op) { nav.pop() }
                is Overlay.Faith -> FaithSheet(home, o.box) { nav.pop() }
                is Overlay.Crack -> CrackScreen(home, nav, o.box) { nav.pop() }
                is Overlay.Loot -> LootSheet(home, nav, o.box) { nav.pop() }
                is Overlay.Member -> MemberSheet(home, o.crew) { nav.pop() }
                Overlay.Settings -> SettingsSheet(home) { nav.pop() }
                Overlay.Help -> HelpSheet(home) { nav.pop() }
                is Overlay.Plan, null -> Unit
            }
            ToastHost(toaster)
        }
    }
}

@Composable
private fun TabBar(tab: Tab, onTab: (Tab) -> Unit) {
    val items = listOf(Tab.BANK to R.string.tab_bank, Tab.CREW to R.string.tab_crew, Tab.JOBS to R.string.tab_jobs, Tab.LOOT to R.string.tab_loot)
    Column(Modifier.fillMaxWidth().background(C.bar)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .12f)))
        Row(Modifier.fillMaxWidth().navigationBarsPadding()) {
            for ((t, label) in items) {
                val color = if (t == tab) C.orange else Color(0xFF7D93AD)
                Column(Modifier.weight(1f).clickable { onTab(t) }.padding(top = 9.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(22.dp)) { icon(t, color) }
                    Text(
                        stringResource(label).uppercase(),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = Title, fontSize = 11.sp, letterSpacing = 1.4.sp, color = color),
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }
    }
}

private fun DrawScope.icon(t: Tab, color: Color) {
    val k = size.width / 24f
    val st = Stroke(1.6f * k)
    fun p(x: Float, y: Float) = Offset(x * k, y * k)
    when (t) {
        Tab.BANK -> {
            drawPath(Path().apply { moveTo(3f * k, 9f * k); lineTo(12f * k, 4f * k); lineTo(21f * k, 9f * k); close() }, color, style = st)
            for (x in listOf(6f, 10f, 14f, 18f)) drawLine(color, p(x, 10f), p(x, 18f), 1.6f * k)
            drawLine(color, p(3f, 20f), p(21f, 20f), 1.6f * k)
        }
        Tab.CREW -> {
            drawCircle(color, 3f * k, p(8f, 8f), style = st)
            drawCircle(color, 3f * k, p(16f, 8f), style = st)
            drawArc(color, 180f, 180f, false, p(3f, 15f), Size(10f * k, 8f * k), style = st)
            drawArc(color, 180f, 180f, false, p(11f, 15f), Size(10f * k, 8f * k), style = st)
        }
        Tab.JOBS -> {
            drawPath(Path().apply { moveTo(4f * k, 6f * k); lineTo(10f * k, 6f * k); lineTo(12f * k, 8f * k); lineTo(20f * k, 8f * k); lineTo(20f * k, 19f * k); lineTo(4f * k, 19f * k); close() }, color, style = st)
            drawLine(color, p(8f, 13f), p(16f, 13f), 1.6f * k)
            drawLine(color, p(8f, 16f), p(13f, 16f), 1.6f * k)
        }
        Tab.LOOT -> {
            drawPath(Path().apply { moveTo(4f * k, 15f * k); lineTo(7f * k, 9f * k); lineTo(17f * k, 9f * k); lineTo(20f * k, 15f * k); close() }, color, style = st)
            drawRect(color, p(4f, 15f), Size(16f * k, 4f * k), style = st)
        }
    }
}

package app.insidejob

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import app.insidejob.data.DemoSeed
import app.insidejob.domain.Sys
import app.insidejob.ui.Home
import app.insidejob.ui.Nav
import app.insidejob.ui.Words
import app.insidejob.ui.bank.BankScreen
import app.insidejob.ui.box.BoxScreen
import app.insidejob.ui.box.CrackScreen
import app.insidejob.ui.box.DossierSheet
import app.insidejob.ui.box.GuardSheet
import app.insidejob.ui.box.NewBoxSheet
import app.insidejob.ui.box.QuestionSheet
import app.insidejob.ui.components.LocalToast
import app.insidejob.ui.components.Toaster
import app.insidejob.ui.crew.CrewScreen
import app.insidejob.ui.jobs.JobSheet
import app.insidejob.ui.jobs.JobsScreen
import app.insidejob.ui.loot.LootScreen
import app.insidejob.ui.loot.LootSheet
import app.insidejob.ui.onboarding.Onboarding
import app.insidejob.ui.settings.SettingsSheet
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.JobTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Every main screen over the demo history, drawn to PNGs (run with `-Prender`), English and Russian. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }
    private val app get() = ApplicationProvider.getApplicationContext<InsideJobApp>()

    private fun home(): Home = runBlocking {
        val c = app.container
        DemoSeed.seedIfEmpty(c)
        val s = c.repo.refresh()
        val st = c.settings.now()
        Home(s, st, Words(lang == "ru", st.fem, s.cat), System.currentTimeMillis())
    }

    private fun nav() = Nav(mutableStateListOf()) {}

    private fun shot(name: String, h: Home, content: @Composable (Home) -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            JobTheme {
                CompositionLocalProvider(LocalToast provides Toaster()) {
                    Box(Modifier.fillMaxSize().background(C.navy)) { content(h) }
                }
            }
        }
        rule.mainClock.advanceTimeBy(3_000)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private val Home.job get() = s.job!!.id

    @Test fun welcome() = shot("0-welcome", home()) { Onboarding(it.w, it.settings) }
    @Test fun welcome2() = shot("0-welcome-crew", home()) { Onboarding(it.w, it.settings, startPage = 1) }
    @Test fun bank() = shot("1-bank", home()) { BankScreen(it, nav()) }
    @Test fun plan() = shot("2-plan", home()) { BoxScreen(it, nav(), it.job) }
    @Test fun guard() = shot("3-guard", home()) { h -> BoxScreen(h, nav(), h.job); GuardSheet(h, nav(), h.job, Sys.MIND) {} }
    @Test fun question() = shot("3-question", home()) { h -> BoxScreen(h, nav(), h.job); QuestionSheet(h, h.job, Sys.MIND) {} }
    @Test fun crew() = shot("4-crew", home()) { CrewScreen(it, nav()) }
    @Test fun jobs() = shot("5-jobs", home()) { JobsScreen(it, nav()) }
    @Test fun job() = shot("5-job-new", home()) { h -> JobsScreen(h, nav()); JobSheet(h, h.job, Sys.MIND) {} }
    @Test fun loot() = shot("6-loot", home()) { LootScreen(it, nav()) }
    @Test fun lootOne() = shot("6-loot-one", home()) { h -> LootScreen(h, nav()); LootSheet(h, nav(), h.s.opened.first().id) {} }
    @Test fun dossier() = shot("x-dossier", home()) { h -> BoxScreen(h, nav(), h.job); DossierSheet(h, h.job) {} }
    @Test fun newBox() = shot("x-new", home()) { h -> BankScreen(h, nav()); NewBoxSheet(h, nav(), 0, 1) {} }
    @Test fun crack() = shot("x-crack", home()) { h -> CrackScreen(h, nav(), h.job) {} }
    @Test fun settings() = shot("x-settings", home()) { h -> BankScreen(h, nav()); SettingsSheet(h) {} }
}

class RenderArtScreensEn : ScreensBase() {
    override val lang = "en"
}

@Config(qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderArtScreensRu : ScreensBase() {
    override val lang = "ru"
}

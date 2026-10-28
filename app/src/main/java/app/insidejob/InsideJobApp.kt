package app.insidejob

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import app.insidejob.data.BankDb
import app.insidejob.data.DemoSeed
import app.insidejob.data.Repo
import app.insidejob.data.SettingsStore
import app.insidejob.domain.Catalogue
import app.insidejob.notify.Bell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val catalogue: Catalogue by lazy { Catalogue.load { runCatching { app.assets.open(it) }.getOrNull() } }
    val db = BankDb.create(app)
    val settings = SettingsStore(app)
    val repo = Repo(db, settings) { catalogue }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Fire-and-forget so a write survives the screen being closed right after tapping. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }
}

class InsideJobApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Bell.channel(this)
        // The demo shows nothing until its history is in place, so the first screen doesn't jump.
        if (BuildConfig.DEMO) container.repo.hold = true
        container.scope.launch {
            container.catalogue
            if (BuildConfig.DEMO) DemoSeed.seedIfEmpty(container)
            container.repo.hold = false
            container.repo.refresh()
        }
    }
}

val Context.container: AppContainer get() = (applicationContext as InsideJobApp).container

/** Resources in the in-app language, which AppCompat doesn't localise outside activities before Android 13. */
fun Context.localized(): Context {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return this
    val config = Configuration(resources.configuration)
    config.setLocale(tags[0])
    return createConfigurationContext(config)
}

/** Is this context in Russian? */
val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"

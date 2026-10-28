package app.insidejob.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** Russian words take the feminine form. */
    val fem: Boolean = false,
    /** The box the crew works on; 0 for the first shut one. */
    val current: Long = 0,
    /** The Shadow's evening round, «Когда сегодня звучала охрана?»; -1 for none. */
    val bellHour: Int = 21,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        fem = this[FEM] ?: false,
        current = this[CURRENT] ?: 0,
        bellHour = this[BELL] ?: 21,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[FEM] = s.fem
            it[CURRENT] = s.current
            it[BELL] = s.bellHour
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val FEM = booleanPreferencesKey("fem")
        val CURRENT = longPreferencesKey("current")
        val BELL = intPreferencesKey("bell_hour")
    }
}

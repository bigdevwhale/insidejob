package app.insidejob.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.insidejob.BuildConfig
import app.insidejob.container
import app.insidejob.data.Settings
import app.insidejob.notify.Bell
import app.insidejob.ui.Home
import app.insidejob.ui.Words
import app.insidejob.ui.components.Card
import app.insidejob.ui.components.Chip
import app.insidejob.ui.components.Flow
import app.insidejob.ui.components.Gap
import app.insidejob.ui.components.Sheet
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.P

/** The StillCraft bot: deep links file a bug or an idea for this app, or follow its releases. */
object Community {
    private const val BOT = "https://t.me/ArcaneWhisperBot?start="
    const val SLUG = "insidejob"
    const val BUG = "${BOT}bug_$SLUG"
    const val IDEA = "${BOT}wish_$SLUG"
    const val FOLLOW = "${BOT}f_$SLUG"
    const val CHAT = "https://t.me/+Q4gFEDk8O842OGVi"
}

@Composable
fun SettingsSheet(h: Home, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val w = h.w
    val st = h.settings
    fun save(f: (Settings) -> Settings) = c.write { settings.update(f); Bell.plan(ctx) }

    Sheet(w.t("Штаб", "Headquarters"), w.t("Настройки", "Settings"), onClose, w.t("Закрыть", "Close")) {
        Card {
            Text(w.t("Язык", "Language"), style = P.Sub)
            Flow(Modifier.padding(top = 8.dp)) {
                Chip("Русский", h.ru) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru")) }
                Chip("English", !h.ru) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) }
            }
            if (h.ru) {
                Text("Как к тебе обращаться", style = P.Small, modifier = Modifier.padding(top = 10.dp))
                Flow(Modifier.padding(top = 6.dp)) {
                    Chip("Ты пришёл", !st.fem) { save { it.copy(fem = false) } }
                    Chip("Ты пришла", st.fem) { save { it.copy(fem = true) } }
                }
            }
        }
        Gap(10)
        Card {
            Text(w.t("Вечерний обход Тени", "The Shadow's evening round"), style = P.Sub)
            Text(w.t("«Когда сегодня звучала охрана?» — только если за день ещё ничего не записано.", "“When did the guard speak today?”, only if nothing was logged that day."), style = P.Small)
            Flow(Modifier.padding(top = 8.dp)) {
                for (hr in listOf(20, 21, 22)) Chip("$hr:00", st.bellHour == hr) { save { it.copy(bellHour = hr) } }
                Chip(w.t("не напоминать", "never"), st.bellHour < 0) { save { it.copy(bellHour = -1) } }
            }
        }
        Gap(10)
        Card(onClick = { c.write { settings.update { it.copy(onboarded = false) } }; onClose() }) {
            Text(w.t("Инструктаж", "Briefing"), style = P.Sub)
            Text(w.t("Посмотреть, как всё устроено, с начала ›", "See how it all works from the start ›"), style = P.Small.copy(color = C.red))
        }
        Gap(10)
        CommunityLinks(w)
        Gap(10)
        Card {
            Text(w.t("О «Деле века»", "About Inside Job"), style = P.Sub)
            Text(
                w.t(
                    "Игра по мотивам когнитивно-поведенческой терапии: доказательства за и против, сократические вопросы, поведенческие эксперименты, поиск истоков убеждения. Это не лечение и не замена психотерапевту. Без интернета, рекламы и аккаунта; всё хранится на телефоне.",
                    "A game based on cognitive behavioural therapy: evidence for and against, Socratic questions, behavioural experiments, tracing a belief to its source. It's not a treatment and not a substitute for a therapist. No internet, no ads, no account; everything stays on the phone.",
                ),
                style = P.Small,
            )
            Text(
                w.t("Версия ${BuildConfig.VERSION_NAME} · шрифты PT Sans Narrow, PT Mono, IBM Plex Mono и Caveat (OFL) · исходники на GitHub", "Version ${BuildConfig.VERSION_NAME} · PT Sans Narrow, PT Mono, IBM Plex Mono and Caveat fonts (OFL) · source on GitHub"),
                style = P.Small.copy(color = C.red),
                modifier = Modifier.padding(top = 8.dp).clickable { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bigdevwhale/insidejob"))) } },
            )
        }
        Gap(20)
    }
}

/** Report a bug, suggest an idea, follow updates, join the chat; each opens Telegram. */
@Composable
private fun CommunityLinks(w: Words) {
    val ctx = LocalContext.current
    fun open(url: String) { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
    Card {
        Text(w.t("Сообщество", "Community"), style = P.Sub)
        Text(
            w.t("Приложение делается вместе с теми, кто им пользуется. Всё идёт через бота StillCraft в Telegram.", "The app is made together with the people who use it. It all goes through the StillCraft bot on Telegram."),
            style = P.Small, modifier = Modifier.padding(bottom = 4.dp),
        )
        for ((icon, label, sub, url) in listOf(
            Link("🐞", w.t("Сообщить о баге", "Report a bug"), w.t("что сломалось и как повторить", "what broke and how to repeat it"), Community.BUG),
            Link("💡", w.t("Предложить идею", "Suggest an idea"), w.t("система охраны, операция, функция", "a security system, a job, a feature"), Community.IDEA),
            Link("🔔", w.t("Подписаться на обновления", "Follow updates"), w.t("бот напишет, когда выйдет версия", "the bot writes when a version is out"), Community.FOLLOW),
            Link("💬", w.t("Чат StillCraft", "StillCraft chat"), w.t("все приложения и люди", "all the apps and people"), Community.CHAT),
        )) {
            Row(Modifier.fillMaxWidth().clickable { open(url) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = P.Sub, modifier = Modifier.width(34.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, style = P.Body)
                    Text(sub, style = P.Small)
                }
                Text("›", style = P.Sub.copy(color = C.red))
            }
        }
    }
}

private data class Link(val icon: String, val label: String, val sub: String, val url: String)

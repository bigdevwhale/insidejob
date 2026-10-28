package app.insidejob.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.insidejob.container
import app.insidejob.data.Settings
import app.insidejob.domain.Crew
import app.insidejob.domain.Sys
import app.insidejob.ui.Words
import app.insidejob.ui.art.door
import app.insidejob.ui.art.figure
import app.insidejob.ui.art.glyph
import app.insidejob.ui.art.loot
import app.insidejob.ui.components.Btn
import app.insidejob.ui.components.Chip
import app.insidejob.ui.components.Flow
import app.insidejob.ui.components.Kicker
import app.insidejob.ui.components.blueprint
import app.insidejob.ui.theme.C
import app.insidejob.ui.theme.T

/** Three pages: the bank, the crew, the loot. */
@Composable
fun Onboarding(w: Words, settings: Settings, startPage: Int = 0) {
    val c = LocalContext.current.container
    var page by remember { mutableIntStateOf(startPage) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { c.write { this.settings.update { it.copy(onboarded = true) } } }
    val t by rememberInfiniteTransition(label = "intro").animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "t")
    val pages = listOf(
        Triple(w.t("Объект", "The target"), w.t("БАНК «НЕЛЬЗЯ»", "THE BANK OF NO"), w.t(
            "Годами ты складывал сюда то, что себе запретил: просить о помощи, отдыхать, ошибаться, говорить «нет». Каждое убеждение — ячейка под охраной, а охрана говорит твоим голосом.",
            "For years you've been depositing here what you forbade yourself: asking for help, resting, making mistakes, saying no. Each belief is a guarded box, and the guard speaks in your voice.",
        )),
        Triple(w.t("Пятеро на одно дело", "Five on one job"), w.t("КОМАНДА", "THE CREW"), w.t(
            "Отмычка ищет доказательства против, Профессор задаёт вопросы, Тень записывает, когда дежурит охрана, Перо переписывает старый приговор, Шофёр везёт проверять всё делом. Каждая система охраны гаснет от их работы.",
            "The Pick finds evidence against, the Professor asks questions, the Shadow logs when the guard is on duty, the Quill rewrites the old verdict, the Driver takes you to test it for real. Each security system goes dark from their work.",
        )),
        Triple(w.t("Зачем всё это", "What it's for"), w.t("ЗАБРАТЬ СВОЁ", "TAKE BACK WHAT'S YOURS"), w.t(
            "Вскрытая ячейка отдаёт то, что в ней лежало: право на помощь, на отдых, на ошибку. Это игра по мотивам когнитивной терапии, а не лечение. Всё хранится только на телефоне.",
            "A cracked box gives back what it kept: the right to help, to rest, to get it wrong. It's a game based on cognitive therapy, not a treatment. Everything stays on the phone.",
        )),
    )
    Column(Modifier.fillMaxSize().blueprint().statusBarsPadding()) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (page) {
                0 -> Canvas(Modifier.size(260.dp)) {
                    door(center, size.minDimension * .36f, C.line, C.navy2, turn = t * 30f)
                    val ring = size.minDimension * .44f
                    for ((i, s) in listOf(Sys.MIND, Sys.CATASTROPHE, Sys.SHOULD, Sys.FILTER, Sys.FORTUNE, Sys.LABEL).withIndex()) {
                        val a = Math.toRadians(i * 60.0 - 90.0)
                        val p = Offset(center.x + (Math.cos(a) * ring).toFloat(), center.y + (Math.sin(a) * ring).toFloat())
                        drawCircle(C.navy, size.minDimension * .07f, p)
                        glyph(s, p, size.minDimension * .05f, if (i % 2 == 0) C.laser else C.line)
                    }
                }
                1 -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (m in Crew.entries) Canvas(Modifier.size(62.dp).border(1.5.dp, C.ink)) { figure(m) }
                }
                else -> Canvas(Modifier.size(260.dp).background(C.velvet)) {
                    loot(0, Offset(size.width * .32f, size.height * .38f), size.width * .42f)
                    loot(1, Offset(size.width * .7f, size.height * .36f), size.width * .38f)
                    loot(2, Offset(size.width * .36f, size.height * .72f), size.width * .38f)
                    loot(3, Offset(size.width * .7f, size.height * .72f), size.width * .38f)
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(C.bar).navigationBarsPadding().padding(20.dp)) {
            Kicker(pages[page].first)
            Text(pages[page].second, style = T.Big, modifier = Modifier.padding(top = 2.dp))
            Text(pages[page].third, style = T.Body, modifier = Modifier.padding(top = 8.dp))
            if (page == pages.lastIndex && w.ru) {
                Text("Как к тебе обращаться?", style = T.Small, modifier = Modifier.padding(top = 12.dp))
                Flow(Modifier.padding(top = 6.dp)) {
                    Chip("Ты пришёл", !settings.fem, dark = true) { c.write { this.settings.update { it.copy(fem = false) } } }
                    Chip("Ты пришла", settings.fem, dark = true) { c.write { this.settings.update { it.copy(fem = true) } } }
                }
            }
            Row(Modifier.padding(top = 14.dp)) {
                for (i in pages.indices) Box(Modifier.padding(end = 6.dp).size(18.dp, 4.dp).background(if (i == page) C.orange else C.line2.copy(alpha = .4f)))
            }
            Btn(if (page < pages.lastIndex) w.t("Дальше", "Next") else w.t("Начать дело", "Start the job"), Modifier.fillMaxWidth().padding(top = 14.dp)) {
                if (page < pages.lastIndex) page++
                else if (Build.VERSION.SDK_INT >= 33) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                else c.write { this.settings.update { it.copy(onboarded = true) } }
            }
        }
    }
}

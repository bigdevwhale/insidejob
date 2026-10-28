package app.insidejob.ui

import app.insidejob.domain.Belief
import app.insidejob.domain.Catalogue
import app.insidejob.domain.Crew
import app.insidejob.domain.Job
import app.insidejob.domain.Question
import app.insidejob.domain.Sphere
import app.insidejob.domain.Sys
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

/** The words that come from code rather than resources, in both languages and both Russian genders. */
class Words(val ru: Boolean, val fem: Boolean, val cat: Catalogue) {
    private val pair = Regex("\\{([^|{}]*)\\|([^|{}]*)\\}")

    /** «Ты {должен|должна}» in the user's gender. */
    fun g(s: String): String = pair.replace(s) { if (fem) it.groupValues[2] else it.groupValues[1] }

    fun t(r: String, e: String) = if (ru) g(r) else e

    fun plural(n: Int, one: String, few: String, many: String): String {
        val a = abs(n) % 100
        val b = a % 10
        return when {
            !ru -> if (n == 1) one else many
            a in 11..14 -> many
            b == 1 -> one
            b in 2..4 -> few
            else -> many
        }
    }

    fun text(b: Belief) = g(b.text(ru))
    fun voice(b: Belief) = g(b.voice(ru))
    fun loot(b: Belief) = g(b.loot(ru))
    fun text(q: Question) = g(q.text(ru))
    fun task(j: Job) = g(j.task(ru))
    fun guard(j: Job) = g(j.guard(ru))

    fun sys(s: Sys) = if (ru) s.ru else s.en
    fun device(s: Sys) = if (ru) s.deviceRu else s.deviceEn
    fun how(s: Sys) = t(s.howRu, s.howEn)
    fun beat(s: Sys) = t(s.beatRu, s.beatEn)

    fun sphere(s: Sphere) = when (s) {
        Sphere.WORK -> t("Работа", "Work")
        Sphere.PEOPLE -> t("Люди", "People")
        Sphere.LOVE -> t("Любовь", "Love")
        Sphere.FAMILY -> t("Семья", "Family")
        Sphere.BODY -> t("Тело", "Body")
        Sphere.MONEY -> t("Деньги", "Money")
        Sphere.SELF -> t("Отношение к себе", "Self")
        Sphere.CRAFT -> t("Учёба и творчество", "Learning and making")
    }

    fun floor(level: Int) = when (level) {
        1 -> t("Этаж 1 · правила дня", "Floor 1 · rules of the day")
        2 -> t("−1 · хранилище · законы", "−1 · the vault · laws")
        else -> t("−2 · центральный сейф", "−2 · the central safe")
    }

    fun place(level: Int) = when (level) {
        1 -> t("ячейка", "box")
        2 -> t("хранилище", "vault")
        else -> t("центральный сейф", "central safe")
    }

    fun crew(c: Crew) = when (c) {
        Crew.LOCK -> t("Отмычка", "Pick")
        Crew.PROF -> t("Профессор", "Professor")
        Crew.PEN -> t("Перо", "Quill")
        Crew.SHADOW -> t("Тень", "Shadow")
        Crew.DRIVER -> t("Шофёр", "Driver")
    }

    fun trade(c: Crew) = when (c) {
        Crew.LOCK -> t("взломщица", "cracksman")
        Crew.PROF -> t("медвежатник", "safecracker")
        Crew.PEN -> t("фальсификатор", "forger")
        Crew.SHADOW -> t("наводчик", "lookout")
        Crew.DRIVER -> t("водитель", "wheelman")
    }

    fun skill(c: Crew) = when (c) {
        Crew.LOCK -> t("Ищет доказательства против. Каждая находка гасит луч.", "Finds evidence against. Every find puts out a beam.")
        Crew.PROF -> t("Слушает замок стетоскопом и задаёт вопросы, от которых щёлкает механизм.", "Listens to the lock with a stethoscope and asks questions that make the mechanism click.")
        Crew.PEN -> t("Переписывает старый приговор взрослым почерком и гравирует новое правило.", "Rewrites the old verdict in a grown-up hand and engraves the new rule.")
        Crew.SHADOW -> t("Записывает, когда дежурит охрана: дни, часы, люди рядом.", "Notes when the guard is on duty: days, hours, who's around.")
        Crew.DRIVER -> t("Везёт на дело в реальную жизнь. Потом честно пишет, что было.", "Drives you to the job in real life. Then writes down honestly what happened.")
    }

    val locale: Locale get() = if (ru) Locale.forLanguageTag("ru") else Locale.ENGLISH

    fun date(d: LocalDate): String = if (ru) "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.FULL, locale)}" else "${d.month.getDisplayName(TextStyle.SHORT, locale)} ${d.dayOfMonth}"

    fun short(d: LocalDate): String = if (ru) "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.SHORT, locale).trimEnd('.')}" else "${d.month.getDisplayName(TextStyle.SHORT, locale)} ${d.dayOfMonth}"

    /** «по понедельникам». */
    fun weekdays(d: DayOfWeek) = if (ru) when (d) {
        DayOfWeek.MONDAY -> "по понедельникам"
        DayOfWeek.TUESDAY -> "по вторникам"
        DayOfWeek.WEDNESDAY -> "по средам"
        DayOfWeek.THURSDAY -> "по четвергам"
        DayOfWeek.FRIDAY -> "по пятницам"
        DayOfWeek.SATURDAY -> "по субботам"
        DayOfWeek.SUNDAY -> "по воскресеньям"
    } else "on ${d.getDisplayName(TextStyle.FULL, locale)}s"

    fun part(p: Int) = when (p) {
        0 -> t("утром", "in the morning")
        1 -> t("днём", "in the afternoon")
        2 -> t("вечером", "in the evening")
        else -> t("ночью", "at night")
    }

    /** «№07». */
    fun no(n: Int) = "№" + n.toString().padStart(2, '0')

    fun result(r: Int) = when (r) {
        0 -> t("Не сработала", "Stayed quiet")
        1 -> t("Звякнула", "Rang a little")
        else -> t("Сработала", "Went off")
    }
}

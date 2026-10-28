package app.insidejob.data

import app.insidejob.AppContainer
import app.insidejob.domain.Crew
import app.insidejob.domain.Sys

/**
 * The demo's history, five weeks of it: two boxes cracked (speaking in public, rest that must be earned),
 * the current job on asking for help half done, a law and a core belief found below it, jobs closed and one waiting.
 */
object DemoSeed {
    suspend fun seedIfEmpty(c: AppContainer) {
        val r = c.repo
        if (r.refresh().boxes.isNotEmpty()) return
        val t = r.clock()
        fun ago(days: Int, hour: Int = 10) = t - days * DAY + (hour - 12) * HOUR
        c.settings.update { it.copy(onboarded = true) }

        // Cracked: «Я не умею выступать».
        val talk = r.box("Я не умею выступать: начну говорить и всё забуду", 1, "WORK", listOf(Sys.FORTUNE, Sys.LABEL), "Опозоришься при всех.", "Право говорить с дрожью в голосе", 85, at = ago(34))
        r.edit(r.refresh().box(talk)!!.copy(age = "лет двенадцать", judge = "класс и учительница литературы", scene = "Забыл стих у доски, все смеялись.", appeal = "Двенадцатилетний забыл стих. Это не приговор голосу на всю жизнь."))
        r.move(talk, Crew.SHADOW, "Совещание, надо было показать квартальный план. Отдал слово Диме.", Sys.FORTUNE, at = ago(33, 11))
        r.move(talk, Crew.LOCK, "На свадьбе брата сказал тост без бумажки, люди потом подходили.", Sys.LABEL, at = ago(32, 21))
        r.move(talk, Crew.PROF, "Если бы друг запнулся на докладе, я бы и не запомнил.", Sys.LABEL, ref = "label-01", at = ago(31, 22))
        r.move(talk, Crew.LOCK, "Вёл созвон с подрядчиком на сорок минут, никто не жаловался.", Sys.LABEL, at = ago(30, 20))
        val j1 = r.job(talk, "Рассказать на планёрке про свой участок за три минуты", "Собьюсь, покраснею, все заметят", 80, Sys.FORTUNE, at = ago(30, 9))
        r.report(j1, "Сбился один раз, переспросил себя по бумажке. Начальник сказал «спасибо, понятно».", 0, 35, at = ago(29, 12))
        r.move(talk, Crew.PROF, "Прогноз «всё забуду» за год сбывался один раз из десяти.", Sys.FORTUNE, ref = "fortune-01", at = ago(28, 22))
        r.crack(r.refresh().box(talk)!!, "Можно говорить и волноваться. Голос дрожит, а слушают всё равно.", 30, at = ago(27, 21))

        // Cracked: «Отдых надо заслужить».
        val rest = r.box("Отдыхать можно, только когда всё сделано", 2, "BODY", listOf(Sys.SHOULD, Sys.CONDITION), "Сначала дела, потом лежать.", "Право на выходной без оправданий", 75, at = ago(26))
        r.edit(r.refresh().box(rest)!!.copy(judge = "мама", age = "с детства", appeal = "Мама так жила, потому что не было выбора. У меня он есть."))
        r.move(rest, Crew.SHADOW, "Суббота, сел смотреть кино и через десять минут пошёл мыть посуду.", Sys.SHOULD, at = ago(25, 15))
        r.move(rest, Crew.LOCK, "После отпуска в прошлом году работал лучше, чем до.", Sys.CONDITION, at = ago(24, 21))
        r.move(rest, Crew.PROF, "Моё правило, не мамино: никто не уволит меня за субботу.", Sys.SHOULD, ref = "should-01", at = ago(23, 22))
        val j2 = r.job(rest, "Провести воскресенье без списка дел", "Весь день буду грызть себя, а в понедельник завал", 70, Sys.CONDITION, at = ago(22, 10))
        r.report(j2, "Немного грыз до обеда, потом прошло. Понедельник как понедельник.", 0, 25, at = ago(20, 20))
        r.move(rest, Crew.LOCK, "Друзья не считают меня лентяем, хотя я отказался от переработок.", Sys.SHOULD, at = ago(19, 21))
        r.move(rest, Crew.PROF, "Отдых — не награда, а часть работы, как заправка для машины.", Sys.CONDITION, ref = "cond-01", at = ago(18, 22))
        r.move(rest, Crew.LOCK, "Сходил в баню в будний вечер, проект не рухнул.", Sys.SHOULD, at = ago(17, 22))
        r.crack(r.refresh().box(rest)!!, "Отдых не надо заслуживать. Я устаю — значит, отдыхаю.", 25, at = ago(16, 21))

        // Below the current job: a law and a core belief.
        val core = r.box("Я недостаточно хорош", 3, "SELF", listOf(Sys.LABEL, Sys.FILTER, Sys.DISCOUNT), "Ты всегда будешь недотягивать.", "Право быть достаточным", 65, at = ago(12))
        val law = r.box("Я должен справляться со всем сам", 2, "WORK", listOf(Sys.SHOULD, Sys.CONDITION), "Сам. Мужик должен сам.", "Право делить ношу", 80, at = ago(13))
        r.rest(r.refresh().box(law)!!, core)

        // The current job.
        val help = r.box("Если попрошу о помощи, меня сочтут слабым", 1, "WORK", listOf(Sys.MIND, Sys.CATASTROPHE, Sys.SHOULD), "Сам. Не позорься.", "Право на помощь", 85, ref = "work-ask-help", at = ago(14))
        r.rest(r.refresh().box(help)!!, law)
        r.edit(r.refresh().box(help)!!.copy(age = "лет восемь", judge = "отец", scene = "Плакал над задачей, попросил помочь. «Сам. Мужик должен сам»."))
        r.move(help, Crew.SHADOW, "Планёрка: начальник спросил, кто возьмёт отчёт. Взял, хотя не успеваю.", Sys.SHOULD, at = ago(14, 10))
        r.move(help, Crew.SHADOW, "Сломался кран. Полдня искал видео вместо того, чтобы позвонить соседу.", Sys.MIND, at = ago(7, 9))
        r.move(help, Crew.SHADOW, "Планёрка опять. Промолчал про сроки.", Sys.SHOULD, at = ago(0, 10))
        r.move(help, Crew.LOCK, "В марте попросил Лёшу помочь с переездом. Он приехал с тортом.", Sys.MIND, at = ago(11, 21))
        r.move(help, Crew.PROF, "Когда Марина просит меня посмотреть её код, я думаю, что она внимательная, а не слабая.", Sys.MIND, ref = "mind-01", at = ago(9, 22))
        r.move(help, Crew.LOCK, "Самое страшное, что может быть, — откажут. Это я переживу.", Sys.CATASTROPHE, at = ago(8, 21))
        val j3 = r.job(help, "Попросить Марину посмотреть отчёт до сдачи", "Вздохнёт и решит, что я не справляюсь", 85, Sys.CATASTROPHE, at = ago(6, 10))
        r.report(j3, "Нашла две опечатки. Сказала: «Хорошо, что спросил до сдачи». Попросила потом глянуть её доклад.", 0, 20, at = ago(5, 18))
        r.move(help, Crew.PROF, "Если бы так делал отец, я бы считал его нормальным человеком, а не слабаком.", Sys.SHOULD, ref = "should-02", at = ago(3, 22))
        r.faith(r.refresh().box(help)!!, 55, at = ago(3, 22))
        r.job(help, "Позвонить соседу и попросить ключ на 12", "Скажет, что руки не из того места", 60, Sys.MIND, at = ago(1, 19))
        r.current(help)
    }

    private const val HOUR = 3_600_000L
    private const val DAY = 24 * HOUR
}

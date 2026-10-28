package app.insidejob.domain

/**
 * The bank's security: each system is one of the guard's tricks of thought.
 * [device] is how it looks on the blueprint, [how] how the guard sounds when it's on, [beat] what takes it down.
 */
enum class Sys(
    val ru: String, val en: String,
    val deviceRu: String, val deviceEn: String,
    val howRu: String, val howEn: String,
    val beatRu: String, val beatEn: String,
) {
    MIND(
        "Чтение мыслей", "Mind reading",
        "Лазерная сетка", "Laser grid",
        "«Они подумают, что ты слабак»: охрана точно знает, что у других в голове, и ни разу не проверяла.",
        "“They'll think you're weak”: the guard knows exactly what's in other heads and has never checked.",
        "Спросить, а не угадывать. Каждый случай, когда люди подумали иначе, гасит луч.",
        "Ask instead of guessing. Every time people thought otherwise, a beam goes out.",
    ),
    FORTUNE(
        "Предсказание", "Fortune telling",
        "Табло прогнозов", "Forecast board",
        "«Ничего не выйдет»: табло заранее показывает исход, как расписание поездов.",
        "“It won't work”: the board shows the outcome in advance, like a train timetable.",
        "Записать прогноз и проверить его делом. Табло, которое врёт, перестают читать.",
        "Write the forecast down and test it. A board that lies stops being read.",
    ),
    CATASTROPHE(
        "Катастрофа", "Catastrophe",
        "Сирена", "Siren",
        "«Это будет конец»: сирена воет одинаково на мышь и на пожар.",
        "“That'll be the end”: the siren wails the same at a mouse and at a fire.",
        "Пройти по шагам: что худшее, что вероятное, как бы ты справился.",
        "Walk it through: the worst, the likely, and how you'd cope.",
    ),
    ALLNONE(
        "Всё или ничего", "All or nothing",
        "Двоичный замок", "Binary lock",
        "«Не идеально — значит провал»: у замка только два положения.",
        "“Not perfect means a failure”: the lock has only two positions.",
        "Найти середину: что получилось на шестьдесят процентов и этого хватило.",
        "Find the middle: what came out at sixty percent and was enough.",
    ),
    OVERGEN(
        "Всегда и никогда", "Always and never",
        "Кодовый замок", "Combination lock",
        "«Так всегда»: один случай охрана набирает как код на все двери.",
        "“It's always like this”: the guard dials one case as the code to every door.",
        "Вспомнить исключения. Каждое — неверная цифра в коде.",
        "Recall the exceptions. Each is a wrong digit in the code.",
    ),
    LABEL(
        "Ярлык", "Labelling",
        "Сканер пропусков", "Badge scanner",
        "«Ты неудачник»: сканер читает один ярлык и не пускает дальше.",
        "“You're a loser”: the scanner reads one label and lets you no further.",
        "Описать поступок, а не человека. Ярлык — одно слово, ты — много.",
        "Describe the act, not the person. A label is one word; you are many.",
    ),
    SHOULD(
        "Должен", "Should",
        "Пост охраны с собакой", "Guard post with a dog",
        "«Ты {должен|должна}»: охранник цитирует правила, которых никто не писал.",
        "“You must”: the guard quotes rules nobody wrote down.",
        "Спросить, чьё это правило и что будет, если заменить «должен» на «хочу» или «можно».",
        "Ask whose rule it is and what happens if “must” becomes “want” or “may”.",
    ),
    FILTER(
        "Только плохое", "Only the bad",
        "Камера наблюдения", "Security camera",
        "Камера пишет только промахи, а всё остальное — снег на экране.",
        "The camera records only the misses; everything else is snow on the screen.",
        "Досмотреть запись целиком: что ещё было в тот день.",
        "Watch the whole tape: what else happened that day.",
    ),
    DISCOUNT(
        "Не считается", "Doesn't count",
        "Шредер", "Shredder",
        "«Повезло» и «из вежливости»: хорошее идёт в шредер раньше, чем его прочитают.",
        "“Luck” and “just being polite”: the good goes into the shredder before it's read.",
        "Достать из шредера и подшить в дело: хорошее тоже доказательство.",
        "Pull it out of the shredder and file it: the good is evidence too.",
    ),
    EMOTION(
        "Чувствую — значит правда", "I feel it, so it's true",
        "Тепловой датчик", "Heat sensor",
        "Страшно — значит опасно, стыдно — значит виноват: датчик путает тепло с пожаром.",
        "Scared means danger, ashamed means guilty: the sensor mistakes warmth for fire.",
        "Отделить чувство от факта: чувство настоящее, вывод — нет.",
        "Split the feeling from the fact: the feeling is real, the conclusion isn't.",
    ),
    PERSONAL(
        "Из-за меня", "Because of me",
        "Прожектор", "Searchlight",
        "Что бы ни случилось, прожектор светит на тебя.",
        "Whatever happens, the searchlight swings onto you.",
        "Нарисовать всю картину: кто и что ещё на это повлияло.",
        "Draw the whole picture: who and what else played a part.",
    ),
    COMPARE(
        "Сравнение", "Comparing",
        "Зеркальный коридор", "Hall of mirrors",
        "У других лучше: зеркала показывают их витрину рядом с твоей кухней.",
        "Others have it better: the mirrors show their shop window next to your kitchen.",
        "Сравнить себя с собой год назад.",
        "Compare yourself with you a year ago.",
    ),
    FIXED(
        "Я такой", "That's just me",
        "Бетонная стена", "Concrete wall",
        "«Ты такой и не изменишься»: стена делает вид, что её не строили.",
        "“You're like this and won't change”: the wall pretends it was never built.",
        "Найти, что в тебе уже менялось. Стену клали по кирпичу — по кирпичу и разбирают.",
        "Find what has changed in you already. A wall laid brick by brick comes down the same way.",
    ),
    CONDITION(
        "Только если", "Only if",
        "Турникет", "Turnstile",
        "Пускает, только если заплатишь: успехом, пользой, удобством для всех.",
        "It lets you through only if you pay: with success, with use, with being easy for everyone.",
        "Спросить, нужен ли такой билет тем, кого ты любишь.",
        "Ask whether the people you love need such a ticket.",
    );

    companion object {
        fun of(code: String): Sys? = entries.firstOrNull { it.name == code }
    }
}

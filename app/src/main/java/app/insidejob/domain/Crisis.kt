package app.insidejob.domain

/** Words that mean someone may be in danger: they get a help screen instead of the heist. */
object Crisis {
    private val WORDS = Regex(
        "суицид|самоубий|покончить с собой|убить себя|убью себя|не хочу жить|не хочется жить|жить не хочу|лучше бы меня не было|выпилиться|вскрыть вены|" +
            "suicid|kill myself|end my life|want to die|don't want to live|dont want to live|better off dead|self[- ]harm|hurt myself",
        RegexOption.IGNORE_CASE,
    )

    fun check(text: String) = WORDS.containsMatchIn(text)
}

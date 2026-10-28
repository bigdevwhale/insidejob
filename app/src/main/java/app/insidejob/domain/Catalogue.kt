package app.insidejob.domain

import java.io.InputStream

/** A belief from the catalogue, with the guard's line and the right it keeps. */
class Belief(
    val id: String, val sphere: Sphere, val level: Int, val systems: List<Sys>,
    private val ru: String, private val en: String,
    private val voiceRu: String, private val voiceEn: String,
    private val lootRu: String, private val lootEn: String,
) {
    fun text(ru: Boolean) = if (ru) this.ru else en
    fun voice(ru: Boolean) = if (ru) voiceRu else voiceEn
    fun loot(ru: Boolean) = if (ru) lootRu else lootEn
}

/** One of the Professor's questions; [sys] null means it fits any lock. */
class Question(val id: String, val sys: Sys?, private val ru: String, private val en: String) {
    fun text(ru: Boolean) = if (ru) this.ru else en
}

/** A ready job for a sphere, with the guard's usual forecast. */
class Job(val id: String, val sphere: Sphere, val size: Int, private val ru: String, private val en: String, private val guardRu: String, private val guardEn: String) {
    fun task(ru: Boolean) = if (ru) this.ru else en
    fun guard(ru: Boolean) = if (ru) guardRu else guardEn
}

class Catalogue(val beliefs: List<Belief>, val questions: List<Question>, val jobs: List<Job>) {
    private val byId = beliefs.associateBy { it.id }
    private val qById = questions.associateBy { it.id }
    operator fun get(id: String) = byId[id]
    fun question(id: String) = qById[id]

    companion object {
        private fun rows(open: (String) -> InputStream?, name: String, cols: Int): List<List<String>> =
            open(name)?.bufferedReader(Charsets.UTF_8)?.useLines { lines ->
                lines.map { it.trimEnd('\r') }.filter { it.isNotBlank() && !it.startsWith("#") }
                    .map { it.split('\t') }.filter { it.size >= cols }.toList()
            }.orEmpty()

        fun load(open: (String) -> InputStream?): Catalogue = Catalogue(
            rows(open, "beliefs.tsv", 10).map { r ->
                Belief(r[0], Sphere.of(r[1]), r[2].toIntOrNull()?.coerceIn(1, 3) ?: 1, r[3].split(' ').mapNotNull { Sys.of(it) }, r[4], r[5], r[6], r[7], r[8], r[9])
            },
            rows(open, "prof.tsv", 4).map { r -> Question(r[0], Sys.of(r[1]), r[2], r[3]) },
            rows(open, "jobs.tsv", 7).map { r -> Job(r[0], Sphere.of(r[1]), r[2].toIntOrNull() ?: 1, r[3], r[4], r[5], r[6]) },
        )

        val EMPTY = Catalogue(emptyList(), emptyList(), emptyList())
    }
}

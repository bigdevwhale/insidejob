package app.insidejob.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A safe-deposit box in the bank «Нельзя»: one belief, and the right it has kept locked away.
 * Boxes on floor 1 are the rules of the day, the vault below keeps the laws, the central safe the core beliefs.
 */
@Entity(tableName = "box")
data class Box(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** The number on the door, given in order of opening the file. */
    val no: Int,
    val text: String,
    /** 1 the hall, 2 the vault, 3 the central safe. */
    val level: Int,
    val sphere: String,
    /** The guard's systems, as [Sys] codes, space-separated. */
    val systems: String,
    /** What the guard says, in his own words. */
    val voice: String,
    /** What lies inside: the right the belief took. */
    val loot: String,
    /** How much the belief was believed when the file was opened, and now (0..100). */
    val faith0: Int,
    val faith: Int,
    val at: Long,
    val openedAt: Long = 0,
    /** The new rule engraved on the loot when the box is opened. */
    val engraving: String = "",
    /** The box below this one, on which it rests; 0 for none. */
    val restsOn: Long = 0,
    /** The catalogue belief it came from, if any. */
    val ref: String = "",
    // Reconnaissance: when and by whom the old verdict was passed.
    val age: String = "",
    val judge: String = "",
    val scene: String = "",
    /** The Pen's appeal: the old verdict rewritten in a grown-up hand. */
    val appeal: String = "",
    /** The guard came back this many times; hits before [relockedAt] mostly wear off. */
    val relocks: Int = 0,
    val relockedAt: Long = 0,
) {
    val sys: List<Sys> get() = systems.split(' ').mapNotNull { Sys.of(it) }
    val open: Boolean get() = openedAt > 0
}

/** One move by the crew on a box: a piece of evidence, an answer to the Professor, a sighting of the guard, a re-rating. */
@Entity(tableName = "move")
data class Move(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boxId: Long,
    val at: Long,
    /** A [Crew] name, or FAITH for a re-rating (then [text] is the number). */
    val who: String,
    /** The system it hits, a [Sys] code; blank when none. */
    val system: String = "",
    val text: String,
    /** The Professor's question id. */
    val ref: String = "",
)

/** A job: a real-life test of the belief, the guard's prediction before and what happened after. */
@Entity(tableName = "op")
data class Op(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boxId: Long,
    val at: Long,
    val task: String,
    val guard: String,
    /** How sure the guard is, 0..100. */
    val odds: Int,
    val system: String = "",
    val ref: String = "",
    val doneAt: Long = 0,
    val outcome: String = "",
    /** -1 not yet; 0 the alarm stayed quiet; 1 it rang a little; 2 it came true. */
    val result: Int = -1,
    /** How sure the guard is afterwards. */
    val oddsAfter: Int = -1,
) {
    val done: Boolean get() = doneAt > 0
}

enum class Sphere { WORK, PEOPLE, LOVE, FAMILY, BODY, MONEY, SELF, CRAFT;
    companion object { fun of(s: String) = entries.firstOrNull { it.name == s } ?: SELF }
}

/** The crew: each one is a way to weaken the guard. */
enum class Crew { LOCK, PROF, PEN, SHADOW, DRIVER;
    companion object { fun of(s: String) = entries.firstOrNull { it.name == s } }
}

const val FAITH = "FAITH"

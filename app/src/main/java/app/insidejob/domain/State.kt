package app.insidejob.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Hits a system takes before it goes dark. */
const val HP = 3

/** Uses a crew member needs for each level. */
val LEVELS = listOf(1, 3, 7, 15, 30)

fun Long.date(zone: ZoneId): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
fun Long.day(zone: ZoneId): Long = date(zone).toEpochDay()
fun Long.hour(zone: ZoneId): Int = Instant.ofEpochMilli(this).atZone(zone).hour

/** Part of the day the guard was heard in: 0 morning, 1 day, 2 evening, 3 night. */
fun partOfDay(hour: Int) = when (hour) { in 5..11 -> 0; in 12..17 -> 1; in 18..23 -> 2; else -> 3 }

/** The whole bank at one moment, with the rules of the heist. */
class State(
    val boxes: List<Box>,
    val moves: List<Move>,
    val ops: List<Op>,
    val cat: Catalogue,
    /** The box the crew is working on, as chosen; 0 for the first shut one. */
    private val current: Long,
    val zone: ZoneId,
) {
    private val movesBy = moves.groupBy { it.boxId }
    private val opsBy = ops.groupBy { it.boxId }
    private val byId = boxes.associateBy { it.id }

    fun box(id: Long): Box? = byId[id]
    fun moves(b: Box): List<Move> = movesBy[b.id].orEmpty()
    fun ops(b: Box): List<Op> = opsBy[b.id].orEmpty()

    val shut: List<Box> get() = boxes.filter { !it.open }
    val opened: List<Box> get() = boxes.filter { it.open }.sortedBy { it.openedAt }

    /** Today's job: the chosen box, or the shallowest, oldest shut one. */
    val job: Box? get() = box(current)?.takeIf { !it.open } ?: shut.sortedWith(compareBy({ it.level }, { it.at })).firstOrNull()

    fun floor(level: Int): List<Box> = boxes.filter { it.level == level }.sortedBy { it.no }

    /** Boxes that rest on [b]: when it opens, their alarms lose power. */
    fun above(b: Box): List<Box> = boxes.filter { it.restsOn == b.id }

    /**
     * Hits on one system: evidence and answers one each, a job whose alarm stayed quiet two, one that rang a little one.
     * After the guard came back, only one old hit per system still holds. An opened box below adds one.
     */
    fun hits(b: Box, s: Sys): Int {
        val raw = moves(b).filter { it.system == s.name && (it.who == Crew.LOCK.name || it.who == Crew.PROF.name) }.map { it.at to 1 } +
            ops(b).filter { it.done && it.system == s.name }.map { it.doneAt to when (it.result) { 0 -> 2; 1 -> 1; else -> 0 } }
        val after = raw.filter { it.first >= b.relockedAt }.sumOf { it.second }
        val before = raw.filter { it.first < b.relockedAt }.sumOf { it.second }.coerceAtMost(1)
        val below = if (b.restsOn != 0L && box(b.restsOn)?.open == true) 1 else 0
        return after + before + below
    }

    fun off(b: Box, s: Sys) = hits(b, s) >= HP
    fun active(b: Box): List<Sys> = b.sys.filterNot { off(b, it) }

    /** Reconnaissance: the old verdict found out, and the guard seen on duty at least once. */
    fun dossier(b: Box) = b.age.isNotBlank() || b.judge.isNotBlank() || b.scene.isNotBlank()
    fun sighted(b: Box) = moves(b).any { it.who == Crew.SHADOW.name && it.at >= b.relockedAt }
    fun recon(b: Box) = dossier(b) && sighted(b)

    /** Every system dark and the recon done: the safe can be opened. */
    fun ready(b: Box) = !b.open && b.sys.isNotEmpty() && active(b).isEmpty() && recon(b)

    fun progress(b: Box): Float {
        if (b.open) return 1f
        val sum = b.sys.sumOf { minOf(hits(b, it), HP) } + (if (dossier(b)) 1 else 0) + (if (sighted(b)) 1 else 0)
        return sum / (b.sys.size * HP + 2f)
    }

    /** Questions already answered on this box. */
    fun answered(b: Box): Set<String> = moves(b).filter { it.who == Crew.PROF.name }.map { it.ref }.toSet()

    /** The Professor's next questions for a box: its live systems first, then any lock; unanswered before answered. */
    fun questions(b: Box): List<Question> {
        val live = active(b).ifEmpty { b.sys }.toSet()
        val done = answered(b)
        val fit = cat.questions.filter { it.sys == null || it.sys in live }
        val (fresh, old) = fit.partition { it.id !in done }
        return fresh.sortedBy { if (it.sys != null) 0 else 1 }.let { mine ->
            // Mix them so the same lock doesn't always come first.
            val seed = (b.id * 31 + done.size).toInt()
            mine.shuffled(java.util.Random(seed.toLong()))
        } + old
    }

    fun uses(c: Crew): Int = when (c) {
        Crew.LOCK, Crew.PROF, Crew.SHADOW -> moves.count { it.who == c.name }
        Crew.PEN -> boxes.count { it.appeal.isNotBlank() } + boxes.count { it.engraving.isNotBlank() }
        Crew.DRIVER -> ops.count { it.done }
    }

    /** 0 for never called, then 1..5. */
    fun level(c: Crew): Int = LEVELS.count { uses(c) >= it }

    /** Uses still needed for the next level, or null at the top. */
    fun toNext(c: Crew): Int? = LEVELS.firstOrNull { uses(c) < it }?.let { it - uses(c) }

    val pending: List<Op> get() = ops.filter { !it.done }.sortedBy { it.at }
    val done: List<Op> get() = ops.filter { it.done }.sortedByDescending { it.doneAt }
    val quiet: Int get() = done.count { it.result == 0 }
    val cameTrue: Int get() = done.count { it.result == 2 }

    /** How much less the guard was believed after the jobs, on average, in points. */
    val oddsDrop: Int?
        get() = done.filter { it.oddsAfter >= 0 }.takeIf { it.isNotEmpty() }?.map { it.odds - it.oddsAfter }?.average()?.toInt()

    /** The Shadow's notes: on which weekday and in which part of the day the guard speaks most. */
    fun duty(b: Box? = null): Pair<DayOfWeek, Int>? {
        val seen = moves.filter { it.who == Crew.SHADOW.name && (b == null || it.boxId == b.id) }
        if (seen.size < 2) return null
        val day = seen.groupingBy { it.at.date(zone).dayOfWeek }.eachCount().maxBy { it.value }.key
        val part = seen.groupingBy { partOfDay(it.at.hour(zone)) }.eachCount().maxBy { it.value }.key
        return day to part
    }

    fun faithLog(b: Box): List<Pair<Long, Int>> =
        listOf(b.at to b.faith0) + moves(b).filter { it.who == FAITH }.mapNotNull { m -> m.text.toIntOrNull()?.let { m.at to it } }

    val nextNo: Int get() = (boxes.maxOfOrNull { it.no } ?: 0) + 1
}

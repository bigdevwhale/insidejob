package app.insidejob.data

import app.insidejob.domain.Box
import app.insidejob.domain.Catalogue
import app.insidejob.domain.Crew
import app.insidejob.domain.FAITH
import app.insidejob.domain.Move
import app.insidejob.domain.Op
import app.insidejob.domain.State
import app.insidejob.domain.Sys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZoneId

/** The heist ledger: boxes, the crew's moves and the jobs in Room, read together with the settings. */
class Repo(db: BankDb, private val settings: SettingsStore, private val catalogue: () -> Catalogue) {
    private val dao = db.dao()
    private val mutex = Mutex()
    private val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state

    /** The clock; the demo seed winds it back to lay down past weeks. */
    var clock: () -> Long = System::currentTimeMillis
    var zone: ZoneId = ZoneId.systemDefault()

    /** While true, changes aren't shown, so the demo seed doesn't flicker. */
    @Volatile var hold = false

    suspend fun refresh(): State {
        val s = State(dao.boxes(), dao.moves(), dao.ops(), catalogue(), settings.now().current, zone)
        if (!hold) _state.value = s
        return s
    }

    private suspend fun <T> op(block: suspend (Long) -> T): T = mutex.withLock { block(clock()).also { refresh() } }

    /** Opens a file on a new box. A box found by going down from another becomes its foundation. */
    suspend fun box(
        text: String, level: Int, sphere: String, systems: List<Sys>, voice: String, loot: String, faith: Int,
        ref: String = "", under: Long = 0, at: Long? = null,
    ): Long = op { now ->
        val no = (dao.boxes().maxOfOrNull { it.no } ?: 0) + 1
        val id = dao.insert(
            Box(
                no = no, text = text.trim().take(160), level = level.coerceIn(1, 3), sphere = sphere,
                systems = systems.distinct().joinToString(" ") { it.name }, voice = voice.trim().take(80), loot = loot.trim().take(80),
                faith0 = faith, faith = faith, at = at ?: now, ref = ref,
            ),
        )
        if (under != 0L) dao.box(under)?.let { dao.update(it.copy(restsOn = id)) }
        else settings.update { it.copy(current = id) }
        id
    }

    suspend fun edit(b: Box) = op { dao.update(b) }

    suspend fun rest(upper: Box, lowerId: Long) = op { dao.box(upper.id)?.let { dao.update(it.copy(restsOn = lowerId)) } }

    suspend fun current(id: Long) = op { settings.update { s -> s.copy(current = id) } }

    suspend fun move(boxId: Long, who: Crew, text: String, system: Sys?, ref: String = "", at: Long? = null) = op { now ->
        dao.insert(Move(boxId = boxId, at = at ?: now, who = who.name, system = system?.name ?: "", text = text.trim().take(400), ref = ref))
    }

    suspend fun deleteMove(id: Long) = op { dao.deleteMove(id) }

    suspend fun faith(b: Box, faith: Int, at: Long? = null) = op { now ->
        dao.insert(Move(boxId = b.id, at = at ?: now, who = FAITH, text = faith.toString()))
        dao.box(b.id)?.let { dao.update(it.copy(faith = faith)) }
    }

    suspend fun job(boxId: Long, task: String, guard: String, odds: Int, system: Sys?, ref: String = "", at: Long? = null): Long = op { now ->
        dao.insert(Op(boxId = boxId, at = at ?: now, task = task.trim().take(160), guard = guard.trim().take(160), odds = odds, system = system?.name ?: "", ref = ref))
    }

    suspend fun report(id: Long, outcome: String, result: Int, oddsAfter: Int, at: Long? = null) = op { now ->
        dao.op(id)?.let { dao.update(it.copy(doneAt = at ?: now, outcome = outcome.trim().take(400), result = result, oddsAfter = oddsAfter)) }
    }

    suspend fun deleteJob(id: Long) = op { dao.deleteOp(id) }

    /** The safe is open: the new rule is engraved on the loot. */
    suspend fun crack(b: Box, engraving: String, faith: Int, at: Long? = null) = op { now ->
        val t = at ?: now
        dao.insert(Move(boxId = b.id, at = t, who = FAITH, text = faith.toString()))
        dao.box(b.id)?.let { dao.update(it.copy(openedAt = t, engraving = engraving.trim().take(160), faith = faith)) }
        settings.update { it.copy(current = 0) }
    }

    /** The guard came back: the box is shut again, but some of the old work holds. */
    suspend fun relock(b: Box, faith: Int) = op { now ->
        dao.box(b.id)?.let { dao.update(it.copy(openedAt = 0, relocks = it.relocks + 1, relockedAt = now, faith = faith)) }
        dao.insert(Move(boxId = b.id, at = now, who = FAITH, text = faith.toString()))
        settings.update { it.copy(current = b.id) }
    }

    suspend fun deleteBox(b: Box) = op {
        dao.deleteMovesOf(b.id)
        dao.deleteOpsOf(b.id)
        dao.unrest(b.id)
        dao.deleteBox(b.id)
        settings.update { s -> if (s.current == b.id) s.copy(current = 0) else s }
    }
}

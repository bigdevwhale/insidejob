package app.insidejob

import app.insidejob.domain.Box
import app.insidejob.domain.Catalogue
import app.insidejob.domain.Crew
import app.insidejob.domain.Move
import app.insidejob.domain.Op
import app.insidejob.domain.State
import app.insidejob.domain.Sys
import app.insidejob.ui.Words
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.ZoneId

class LogicTest {
    private val zone = ZoneId.of("UTC")
    private fun box(id: Long, vararg sys: Sys, restsOn: Long = 0, relockedAt: Long = 0, open: Long = 0) =
        Box(id = id, no = id.toInt(), text = "b$id", level = 1, sphere = "WORK", systems = sys.joinToString(" ") { it.name }, voice = "", loot = "",
            faith0 = 80, faith = 80, at = 0, restsOn = restsOn, relockedAt = relockedAt, openedAt = open)
    private fun mv(box: Long, who: Crew, sys: Sys?, at: Long = 10) = Move(boxId = box, at = at, who = who.name, system = sys?.name ?: "", text = "x")

    @Test fun evidenceAndAnswersHitOnce_jobsTwice() {
        val b = box(1, Sys.MIND, Sys.SHOULD)
        val s = State(
            listOf(b),
            listOf(mv(1, Crew.LOCK, Sys.MIND), mv(1, Crew.PROF, Sys.MIND), mv(1, Crew.SHADOW, Sys.MIND)),
            listOf(Op(boxId = 1, at = 1, task = "t", guard = "g", odds = 80, system = "SHOULD", doneAt = 5, result = 0), Op(boxId = 1, at = 1, task = "t", guard = "g", odds = 80, system = "SHOULD", doneAt = 6, result = 2)),
            Catalogue.EMPTY, 0, zone,
        )
        assertEquals(2, s.hits(b, Sys.MIND))
        assertEquals(2, s.hits(b, Sys.SHOULD))
        assertFalse(s.off(b, Sys.MIND))
    }

    @Test fun readyNeedsEverySystemDownAndRecon() {
        val b = box(1, Sys.MIND)
        val hits = List(3) { mv(1, Crew.LOCK, Sys.MIND) }
        val s1 = State(listOf(b), hits, emptyList(), Catalogue.EMPTY, 0, zone)
        assertFalse(s1.ready(b))
        val withRecon = b.copy(judge = "dad")
        val s2 = State(listOf(withRecon), hits + mv(1, Crew.SHADOW, null), emptyList(), Catalogue.EMPTY, 0, zone)
        assertTrue(s2.ready(withRecon))
    }

    @Test fun anOpenFoundationTakesAHitOffTheBoxesAbove() {
        val upper = box(1, Sys.MIND, restsOn = 2)
        val lower = box(2, Sys.LABEL, open = 100)
        val s = State(listOf(upper, lower), emptyList(), emptyList(), Catalogue.EMPTY, 0, zone)
        assertEquals(1, s.hits(upper, Sys.MIND))
        assertEquals(listOf(upper), s.above(lower))
    }

    @Test fun afterTheGuardComesBackOnlyOneOldHitHolds() {
        val b = box(1, Sys.MIND, relockedAt = 50)
        val s = State(listOf(b), List(3) { mv(1, Crew.LOCK, Sys.MIND, at = 10) } + mv(1, Crew.LOCK, Sys.MIND, at = 60), emptyList(), Catalogue.EMPTY, 0, zone)
        assertEquals(2, s.hits(b, Sys.MIND))
    }

    @Test fun crewLevels() {
        val s = State(listOf(box(1, Sys.MIND)), List(7) { mv(1, Crew.LOCK, Sys.MIND) }, emptyList(), Catalogue.EMPTY, 0, zone)
        assertEquals(3, s.level(Crew.LOCK))
        assertEquals(8, s.toNext(Crew.LOCK))
        assertEquals(0, s.level(Crew.PROF))
    }

    @Test fun genderPairs() {
        val cat = Catalogue.EMPTY
        assertEquals("Ты должна сама", Words(true, true, cat).t("Ты {должен|должна} {сам|сама}", ""))
        assertEquals("Ты должен сам", Words(true, false, cat).t("Ты {должен|должна} {сам|сама}", ""))
    }

    /** The catalogue files parse in full, and every gender pair is well formed. */
    @Test fun catalogueLoads() {
        val dir = File("src/main/assets")
        val cat = Catalogue.load { File(dir, it).takeIf { f -> f.exists() }?.inputStream() }
        assertTrue(cat.beliefs.size >= 150)
        assertTrue(cat.questions.size >= 250)
        assertTrue(cat.jobs.size >= 200)
        for (sys in Sys.entries) assertTrue("no questions for $sys", cat.questions.count { it.sys == sys } >= 8)
        val broken = Regex("[{}|]")
        val w = Words(true, true, cat)
        for (b in cat.beliefs) for (t in listOf(w.text(b), w.voice(b), w.loot(b))) assertFalse(t, broken.containsMatchIn(t))
        for (q in cat.questions) assertFalse(w.text(q), broken.containsMatchIn(w.text(q)))
        for (j in cat.jobs) assertFalse(w.task(j), broken.containsMatchIn(w.task(j) + w.guard(j)))
    }
}

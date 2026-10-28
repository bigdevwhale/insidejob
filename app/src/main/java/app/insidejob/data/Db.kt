package app.insidejob.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import app.insidejob.domain.Box
import app.insidejob.domain.Move
import app.insidejob.domain.Op

@Dao
interface BankDao {
    @Query("SELECT * FROM box ORDER BY level, no") suspend fun boxes(): List<Box>
    @Query("SELECT * FROM move ORDER BY at, id") suspend fun moves(): List<Move>
    @Query("SELECT * FROM op ORDER BY at, id") suspend fun ops(): List<Op>
    @Query("SELECT * FROM box WHERE id = :id") suspend fun box(id: Long): Box?
    @Query("SELECT * FROM op WHERE id = :id") suspend fun op(id: Long): Op?

    @Insert suspend fun insert(b: Box): Long
    @Insert suspend fun insert(m: Move): Long
    @Insert suspend fun insert(o: Op): Long
    @Update suspend fun update(b: Box)
    @Update suspend fun update(o: Op)

    @Query("DELETE FROM move WHERE id = :id") suspend fun deleteMove(id: Long)
    @Query("DELETE FROM op WHERE id = :id") suspend fun deleteOp(id: Long)
    @Query("DELETE FROM move WHERE boxId = :id") suspend fun deleteMovesOf(id: Long)
    @Query("DELETE FROM op WHERE boxId = :id") suspend fun deleteOpsOf(id: Long)
    @Query("DELETE FROM box WHERE id = :id") suspend fun deleteBox(id: Long)
    @Query("UPDATE box SET restsOn = 0 WHERE restsOn = :id") suspend fun unrest(id: Long)
}

@Database(entities = [Box::class, Move::class, Op::class], version = 1, exportSchema = true)
abstract class BankDb : RoomDatabase() {
    abstract fun dao(): BankDao

    companion object {
        fun create(c: Context): BankDb = Room.databaseBuilder(c, BankDb::class.java, "bank.db").build()
    }
}

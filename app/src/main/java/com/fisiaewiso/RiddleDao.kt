package com.fisiaewiso

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RiddleDao {
    @Insert
    suspend fun insertAll(vararg riddles: Riddle)

    @Update
    suspend fun update(riddle: Riddle)

    @Query("SELECT * FROM riddles")
    fun getAllRiddles(): Flow<List<Riddle>>

    @Query("SELECT * FROM riddles WHERE riddleMainNumber = :riddleMainNumber")
    fun getRiddlesByNumber(riddleMainNumber: Int): Flow<List<Riddle>>

    @Query("SELECT * FROM riddles WHERE id = :id")
    suspend fun getRiddleById(id: Int): Riddle

    @Query("SELECT * FROM riddles WHERE riddleNumber = :riddleNumber")
    fun getRiddleByNumber(riddleNumber: Int): Riddle

    @Query("SELECT * FROM riddles WHERE riddleIndex = :riddleIndex")
    suspend fun getRiddleByIndex(riddleIndex: Int): Riddle

}
@Dao
interface ResultDao {
    @Insert
    suspend fun insert(result: Result)

    @Query("SELECT * FROM results")
    fun getAllRiddles(): Flow<List<Result>>
}
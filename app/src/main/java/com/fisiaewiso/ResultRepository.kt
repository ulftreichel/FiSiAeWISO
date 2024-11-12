package com.fisiaewiso

class ResultRepository(private val resultDao: ResultDao, private val riddleDao: RiddleDao) {
    suspend fun insert(result: Result) {
        resultDao.insert(result)
    }

}
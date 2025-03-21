package com.example.picktimeapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.picktimeapp.data.db.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE lessonId = :lessonId")
    fun getLessonById(lessonId: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE isFavorite = 1")
    fun getFavoriteLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE isDownloaded = 1")
    fun getDownloadedLessons(): Flow<List<LessonEntity>>
}
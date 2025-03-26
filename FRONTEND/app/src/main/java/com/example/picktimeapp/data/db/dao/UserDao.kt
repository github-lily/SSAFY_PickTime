package com.example.picktimeapp.data.db.dao

// 예시 코드들
//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import androidx.room.Update
//import com.example.picktimeapp.data.db.entity.UserEntity
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface UserDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertUser(user: UserEntity)
//
//    @Update
//    suspend fun updateUser(user: UserEntity)
//
//    @Delete
//    suspend fun deleteUser(user: UserEntity)
//
//    @Query("SELECT * FROM users WHERE userId = :userId")
//    fun getUserById(userId: String): Flow<UserEntity?>
//
//    @Query("SELECT * FROM users LIMIT 1")
//    fun getCurrentUser(): Flow<UserEntity?>
//}
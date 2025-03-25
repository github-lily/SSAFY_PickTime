package com.example.picktimeapp.data.repository
// 예시 코드
//import com.example.picktimeapp.data.api.ApiService
//import com.example.picktimeapp.data.api.LoginRequest
//import com.example.picktimeapp.data.api.SignupRequest
//import com.example.picktimeapp.data.db.dao.UserDao
//import com.example.picktimeapp.data.db.entity.UserEntity
//import kotlinx.coroutines.flow.Flow
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class UserRepository @Inject constructor(
//    private val apiService: ApiService,
//    private val userDao: UserDao
//) {
//    // 로그인 기능
//    suspend fun login(email: String, password: String): Result<String> {
//        return try {
//            val response = apiService.login(LoginRequest(email, password))
//            // 로그인 성공 시 유저 정보 로컬에 저장
//            userDao.insertUser(
//                UserEntity(
//                    userId = response.userId,
//                    name = "", // API 응답에서 이름 정보가 없으면 비워둠
//                    email = email
//                )
//            )
//            Result.success(response.token)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // 회원가입 기능
//    suspend fun signup(name: String, email: String, password: String): Result<Boolean> {
//        return try {
//            val response = apiService.signup(SignupRequest(name, email, password))
//            Result.success(response.success)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // 현재 로그인한 사용자 정보 가져오기
//    fun getCurrentUser(): Flow<UserEntity?> {
//        return userDao.getCurrentUser()
//    }
//
//    // 사용자 정보 업데이트
//    suspend fun updateUserProfile(user: UserEntity) {
//        userDao.updateUser(user)
//        // API로 업데이트하는 코드도 추가 가능
//    }
//}
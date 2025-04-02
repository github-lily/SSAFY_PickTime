package com.example.picktimeapp.network


import retrofit2.http.GET


data class PracticeListResponse(
    val stages: List<StageItem>,
    val clearRate: Double,
)

data class StageItem(
    val stageId: Int,
    val stageDescription: String,
    val steps: List<StepItem>,
    val isClear: Boolean
)

data class StepItem(
    val stepId: Int,
    val stepDescription: String,
    val stepNumber: Int,
    val isClear: Boolean,
    val star: Int
) {
    // 읽기 전용 프로퍼티
    val isStepClear: Boolean
        get() = isClear
}



interface PracticeListApi {
    @GET("practice")
    suspend fun getPracticeList(): PracticeListResponse
}

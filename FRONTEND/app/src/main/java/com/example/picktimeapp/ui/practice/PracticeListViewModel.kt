package com.example.picktimeapp.ui.practice

data class StageItem(
    val id: Int,
    val description: String,
    val isClear: Boolean,
    val steps: List<StepItem>
)

data class StepItem(
    val stepNumber: Int,
    val description: String,
    val isCompleted: Boolean,
    val starCount: Int
)


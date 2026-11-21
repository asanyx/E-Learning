package com.example.elearning.domain.model

data class Quiz(val id: String, val courseId: String, val title: String, val passThresholdPercent: Int)

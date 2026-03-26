// Question.kt
package com.flag.bozi.model

data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

package com.example.model

data class OpenWithApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable
)

package com.example.domain.model

data class GridItem(
    val id: String,
    val icon: Int,
    val title: String,
    val onClick: () -> Unit
)
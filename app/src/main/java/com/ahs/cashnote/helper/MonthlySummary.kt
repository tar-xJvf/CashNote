package com.ahs.cashnote.helper

data class MonthlySummary(
    val month: String,          // "01", "02", ..., "12"
    val typeTransaction: String, // "INCOME" atau "EXPENSE"
    val total: Float
)

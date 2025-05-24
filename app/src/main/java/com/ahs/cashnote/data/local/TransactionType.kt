package com.ahs.cashnote.data.local

enum class TransactionType {
    INCOME,
    EXPENSE;

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

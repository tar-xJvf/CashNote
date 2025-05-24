package com.ahs.cashnote.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TransactionFormViewModel: ViewModel() {
    private val _description = MutableStateFlow("")
    private val _amount = MutableStateFlow(0.0)

    val description: StateFlow<String> = _description
    val amount : StateFlow<Double> = _amount

    val isDescriptionValid: Flow<Boolean> = _description.map {it.isNotBlank()}
    val isAmountValid: Flow<Boolean> = _amount.map {it > 0.0}

    val isFormValid: Flow<Boolean> = combine(isDescriptionValid, isAmountValid) { descriptionValid, amountValid ->
        descriptionValid && amountValid
    }

    fun onDescriptionChanged(newDesc: String) {
        _description.value = newDesc
    }

    fun onAmountChanged(newAmount: Double) {
        _amount.value = newAmount
    }
}
package com.ahs.cashnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahs.cashnote.data.repository.TransactionRepository


class TransactionViewModelFactory(private val repository: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return when{
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TransactionViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TransactionFormViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TransactionFormViewModel() as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class")
            }

        }
    }
}
package com.ahs.cashnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.ahs.cashnote.data.local.Transactions
import com.ahs.cashnote.data.repository.TransactionRepository
import com.ahs.cashnote.helper.MonthlySummary
import kotlinx.coroutines.launch


class TransactionViewModel(private val repository: TransactionRepository) : ViewModel(){
    val allTransaction: LiveData<List<Transactions>> = repository.allTransaction
    val getTotalIncomes: LiveData<Double> = repository.getTotalIncome()
    val getTotalExpenses: LiveData<Double> = repository.getTotalExpense()
    val getRecentTransactions: LiveData<List<Transactions>> = repository.getRecentTransactions()
    val monthlySummary: LiveData<List<MonthlySummary>> = repository.monthlySummary()
    val getTransactionsByDateRange: LiveData<List<Transactions>> = repository.getTransactionsByDateRange("2023-01-01", "2023-12-31")

    private val _dateRange = MutableLiveData<Pair<String, String>>()

    val filteredTransactions: LiveData<List<Transactions>> = _dateRange.switchMap {
        repository.getTransactionsByDateRange(it.first, it.second)
    }

    fun setDateRange(startDate: String, endDate: String) {
        _dateRange.value = Pair(startDate, endDate)
    }

    fun insert(transaction: Transactions) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun insertAllCsv(transactions: List<Transactions>) = viewModelScope.launch {
        repository.insertAll(transactions)
    }

    fun delete(transaction: Transactions) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun getTransactionById(id: Int): LiveData<Transactions> {
        return repository.getTransactionById(id)
    }

    fun update(transaction: Transactions) = viewModelScope.launch {
        repository.updateTransaction(transaction)

    }


}
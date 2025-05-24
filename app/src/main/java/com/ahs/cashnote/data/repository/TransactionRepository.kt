package com.ahs.cashnote.data.repository

import androidx.lifecycle.LiveData
import com.ahs.cashnote.data.local.TransactionDao
import com.ahs.cashnote.data.local.Transactions
import com.ahs.cashnote.helper.MonthlySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepository(private val dao: TransactionDao) {
    val allTransaction: LiveData<List<Transactions>> = dao.getAllTransaction()

    suspend fun insert(transactions: Transactions) {
        dao.insertTransaction(transactions)

    }

    suspend fun insertAll(transactions: List<Transactions>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(transactions)
        }
    }

    suspend fun delete(transactions: Transactions) {
        dao.deleteTransaction(transactions)
    }

    fun getTransactionById(id: Int): LiveData<Transactions> = dao.getTransactionById(id)

    suspend fun updateTransaction(transactions: Transactions) = dao.updateTransaction(transactions)

    fun getTotalIncome(): LiveData<Double> = dao.getTotalIncome()

    fun getTotalExpense(): LiveData<Double> = dao.getTotalExpense()

    fun getRecentTransactions(): LiveData<List<Transactions>> = dao.getRecentTransactions()

    fun monthlySummary(): LiveData<List<MonthlySummary>> = dao.getMonthlyIncomeAndExpense()

    fun getTransactionsByDateRange(startDate: String, endDate: String): LiveData<List<Transactions>> = dao.getTransactionsByDateRange(startDate, endDate)
}
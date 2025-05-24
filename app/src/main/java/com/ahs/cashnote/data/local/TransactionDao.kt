package com.ahs.cashnote.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ahs.cashnote.helper.MonthlySummary

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(expense: Transactions)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transactions>)

    @Delete
    suspend fun deleteTransaction(expense: Transactions)

    @Query("SELECT * FROM tbl_transaction ORDER BY date DESC")
    fun getAllTransaction(): LiveData<List<Transactions>>

    @Query("SELECT * FROM tbl_transaction WHERE id = :id")
    fun getTransactionById(id: Int): LiveData<Transactions>

    @Update
    suspend fun updateTransaction(transactions: Transactions)

    @Query("""
    SELECT SUM(amount)
    FROM tbl_transaction
    WHERE typeTransaction = 'INCOME'
      AND strftime('%Y-%m', date) = strftime('%Y-%m', 'now')
""")
    fun getTotalIncome(): LiveData<Double>

    @Query("""
    SELECT SUM(amount)
    FROM tbl_transaction
    WHERE typeTransaction = 'EXPENSE'
      AND strftime('%Y-%m', date) = strftime('%Y-%m', 'now')
""")

    fun getTotalExpense(): LiveData<Double>

    @Query("SELECT * FROM tbl_transaction ORDER BY date DESC LIMIT 5")
    fun getRecentTransactions(): LiveData<List<Transactions>>


    @Query("""
    SELECT 
        strftime('%m', date) AS month,
        typeTransaction,
        SUM(amount) AS total
    FROM tbl_transaction
    GROUP BY month, typeTransaction
    ORDER BY month
""")
    fun getMonthlyIncomeAndExpense(): LiveData<List<MonthlySummary>>

  @Query("SELECT * FROM tbl_transaction WHERE date BETWEEN :startDate AND :endDate")
  fun getTransactionsByDateRange(startDate: String, endDate: String): LiveData<List<Transactions>>

}
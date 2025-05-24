package com.ahs.cashnote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_transaction")
data class Transactions(
    @PrimaryKey(autoGenerate = true)
    val id:Int =0,
    val date:String,
    val description:String,
    val category:String,
    val amount:Double,
    val typeTransaction: TransactionType

)
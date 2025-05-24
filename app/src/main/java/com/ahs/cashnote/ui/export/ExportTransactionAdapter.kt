package com.ahs.cashnote.ui.export

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ahs.cashnote.data.local.Transactions
import com.ahs.cashnote.databinding.ItemTransactionBinding
import com.ahs.cashnote.helper.toRupiah

class ExportTransactionAdapter : ListAdapter<Transactions, ExportTransactionAdapter.TransactionViewHolder>(DiffCallback()){

    class DiffCallback : DiffUtil.ItemCallback<Transactions>() {
        override fun areItemsTheSame(oldItem: Transactions, newItem: Transactions): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transactions, newItem: Transactions): Boolean {
            return oldItem == newItem
        }
    }
    class  TransactionViewHolder(view: ItemTransactionBinding): RecyclerView.ViewHolder(view.root){
        val tvDate = view.tvDate
        val tvDesc = view.tvDesc
        val tvCategory= view.tvCategory
        val tvAmount = view.tvAmount
        val tvType = view.tvTypeTransaction
        val btnEdit = view.btnEdit
        val btnDelete = view.btnDelete
        fun bind(transactions: Transactions){
            tvDate.text = transactions.date
            tvDesc.text = transactions.description
            tvCategory.text = transactions.category
            tvAmount.text = transactions.amount.toRupiah()
            tvType.text = transactions.typeTransaction.toString()

            btnEdit.visibility = GONE
            btnDelete.visibility = GONE

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }


    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    override fun getItemCount() = currentList.size

}

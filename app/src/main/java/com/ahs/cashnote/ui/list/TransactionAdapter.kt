package com.ahs.cashnote.ui.list


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahs.cashnote.R
import com.ahs.cashnote.data.local.Transactions
import com.ahs.cashnote.helper.toRupiah

class TransactionAdapter(
    private val onEditClick: (Transactions) -> Unit,
    private val onDeleteClick: (Transactions) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val transaction = mutableListOf<Transactions>()

    fun submitList(list: List<Transactions>) {
        transaction.clear()
        transaction.addAll(list)
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvDesc = view.findViewById<TextView>(R.id.tvDesc)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        val tvType = view.findViewById<TextView>(R.id.tvTypeTransaction)
        val btnEdit = view.findViewById<TextView>(R.id.btnEdit)
        val btnDelete = view.findViewById<TextView>(R.id.btnDelete)

        fun bind(transactions: Transactions) {
            tvDate.text = transactions.date
            tvDesc.text = transactions.description
            tvCategory.text = transactions.category
            tvAmount.text = transactions.amount.toRupiah()
            tvType.text = transactions.typeTransaction.toString()

            btnEdit.setOnClickListener { onEditClick(transactions) }
            btnDelete.setOnClickListener { onDeleteClick(transactions) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transaction[position])
    }

    override fun getItemCount() = transaction.size


}


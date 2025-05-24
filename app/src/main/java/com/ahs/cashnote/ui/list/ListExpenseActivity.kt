package com.ahs.cashnote.ui.list

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahs.cashnote.data.local.TransactionDatabase
import com.ahs.cashnote.data.local.TransactionType
import com.ahs.cashnote.data.repository.TransactionRepository
import com.ahs.cashnote.databinding.ActivityListExpenseBinding
import com.ahs.cashnote.ui.add.AddDataActivity
import com.ahs.cashnote.viewmodel.TransactionViewModel
import com.ahs.cashnote.viewmodel.TransactionViewModelFactory

class ListExpenseActivity : AppCompatActivity() {
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var bindingListExpense: ActivityListExpenseBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bindingListExpense = ActivityListExpenseBinding.inflate(layoutInflater)
        val view = bindingListExpense.root
        setContentView(view)

        setupToolbar()
        setupInserts()
        setupSearchView()


        val recyclerView = bindingListExpense.rvExpense
        val dao = TransactionDatabase.getDatabase(this).transactionDao()
        val repository = TransactionRepository(dao)
        val factory = TransactionViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        viewModel.allTransaction.observe(this, Observer { transactions ->
            adapter.submitList(transactions)
        })
        adapter = TransactionAdapter(
            onEditClick = { transactions ->
                val bundle = Bundle().apply {
                    putInt("transactionId", transactions.id)
                }
                val intent = Intent(this, AddDataActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            },
            onDeleteClick = { transactions ->

                if (transactions.typeTransaction == TransactionType.EXPENSE) {
                    viewModel.delete(transactions)
                    Toast.makeText(
                        this@ListExpenseActivity,
                        "Data deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ListExpenseActivity,
                        "Data cannot be deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }
        )//end of adapter

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.allTransaction.observe(this) { list ->
            adapter.submitList(list)
        }

    }

    private fun setupSearchView() {
        val searchView = bindingListExpense.searchView
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = viewModel.allTransaction.value
                    ?.filter { transactions ->
                        transactions.description.contains(newText.orEmpty(), ignoreCase = true) ||
                                transactions.category.contains(
                                    newText.orEmpty(),
                                    ignoreCase = true
                                ) ||
                                transactions.date.contains(newText.orEmpty(), ignoreCase = true) ||
                                transactions.amount.toString()
                                    .contains(newText.orEmpty(), ignoreCase = true) ||
                                transactions.typeTransaction.toString()
                                    .contains(newText.orEmpty(), ignoreCase = true)

                    }
                adapter.submitList(filteredList ?: emptyList())
                return true
            }

        })
    }

    private fun setupInserts() {
        ViewCompat.setOnApplyWindowInsetsListener(bindingListExpense.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun setupToolbar() {
        bindingListExpense.toolbar.title = "List Expense"
        setSupportActionBar(bindingListExpense.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
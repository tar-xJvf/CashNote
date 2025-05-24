package com.ahs.cashnote.ui.add

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ahs.cashnote.R
import com.ahs.cashnote.data.local.TransactionDatabase
import com.ahs.cashnote.data.local.TransactionType
import com.ahs.cashnote.data.local.Transactions
import com.ahs.cashnote.data.repository.TransactionRepository
import com.ahs.cashnote.databinding.ActivityAddDataBinding
import com.ahs.cashnote.helper.addThousandsSeparator
import com.ahs.cashnote.viewmodel.TransactionFormViewModel
import com.ahs.cashnote.viewmodel.TransactionViewModel
import com.ahs.cashnote.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Calendar

class AddDataActivity : AppCompatActivity() {

    private lateinit var addDataBinding: ActivityAddDataBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var formViewModel: TransactionFormViewModel

    private var transactionId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        addDataBinding = ActivityAddDataBinding.inflate(layoutInflater)
        val view = addDataBinding.root
        setContentView(view)
        setupToolbar()
        setupInserts()
        addDataBinding.edAmount.addThousandsSeparator()
        setupViewModels()
        setupSpinner()
        setupSpinnerTransaction()
        observeFormValidation()
        getIntentData()
        if (transactionId != null) loadTransactionData()
        addDataBinding.btSaved.setOnClickListener { onSaveClicked() }


    }//end of onCreate

    private fun setupToolbar() {
        addDataBinding.toolbar.title = "Add Data"
        setSupportActionBar(addDataBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addDataBinding.btSaved.isEnabled = false


    }

    private fun setupInserts() {
        ViewCompat.setOnApplyWindowInsetsListener(addDataBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewModels() {
        val dao = TransactionDatabase.getDatabase(applicationContext).transactionDao()
        val repository = TransactionRepository(dao)
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        formViewModel = ViewModelProvider(this, factory)[TransactionFormViewModel::class.java]

    }

    private fun setupSpinnerTransaction() {
        val spTransaction = addDataBinding.spTransaction
        val transactionList = listOf("Income", "Expense")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            transactionList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spTransaction.adapter = adapter

        spTransaction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = transactionList[position]
                Toast.makeText(this@AddDataActivity, "Selected : $selectedItem", Toast.LENGTH_SHORT)
                    .show()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }//end of setupSpinnerTransaction

    private fun setupSpinner() {
        val spinner = addDataBinding.spCategory
        val categoryList = listOf("Groceries shopping", "Services", "Shopping", "Etc", "Income")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = categoryList[position]
                Toast.makeText(this@AddDataActivity, "Selected : $selectedItem", Toast.LENGTH_SHORT)
                    .show()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }//end of spinner
    }//end of setupSpinner

    private fun observeFormValidation() {
        lifecycleScope.launch {
            combine(
                addDataBinding.edDescription.textChange(),
                addDataBinding.edAmount.textChange()
            ) { description, amount ->
                description.isNotEmpty() && amount.isNotEmpty()
            }.distinctUntilChanged()
                .collect { isFormValid ->
                    addDataBinding.btSaved.isEnabled = isFormValid
                }

        }//end of lifecycleScope
    }

    private fun getIntentData() {
        transactionId = intent.getIntExtra("transactionId", -1).takeIf { it != -1 }
        if (transactionId != null) {
            addDataBinding.toolbar.title = "Edit Data"
        }
    }

    @SuppressLint("DefaultLocale")
    private fun loadTransactionData() {
        addDataBinding.btSaved.text = if (transactionId != null) "Update" else "Save"
        viewModel.getTransactionById(transactionId!!).observe(this) { transaction ->
            transaction?.let {
                addDataBinding.datePickerButton.text = it.date
                addDataBinding.edDescription.setText(it.description)
                addDataBinding.spCategory.setSelection(
                    (addDataBinding.spCategory.adapter as ArrayAdapter<String>).getPosition(it.category)
                )

                val formattedAmount = String.format("%,.0f", it.amount).replace(",", ".")
                addDataBinding.edAmount.setText(formattedAmount)
                addDataBinding.spTransaction.setSelection(
                    (addDataBinding.spTransaction.adapter as ArrayAdapter<String>).getPosition(it.typeTransaction.toString())
                )
            }
        }
    }

    private fun onSaveClicked() {
        with(addDataBinding) {
            if (edDescription.text.toString().isEmpty()) {
                edDescription.error = getString(R.string.empty_field)
                return
            }
            if (edAmount.text.toString().isEmpty()) {
                edAmount.error = getString(R.string.empty_field)
                return
            }

            val transactions = Transactions(
                id = transactionId ?: 0,
                date = datePickerButton.text.toString(),
                description = edDescription.text.toString(),
                category = spCategory.selectedItem.toString(),
                amount = edAmount.text.toString().replace(".", "").toDouble(),
                typeTransaction = TransactionType.valueOf(
                    spTransaction.selectedItem.toString().uppercase()
                ),
            )
            if (transactionId != null) {
                viewModel.update(transactions)
                clearForm()
                Toast.makeText(
                    this@AddDataActivity,
                    "Data updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                viewModel.insert(transactions)
                clearForm()
                Toast.makeText(this@AddDataActivity, "Data added successfully", Toast.LENGTH_SHORT)
                    .show()
            }//end of if
            finish()
        }//end of with
    }


    private fun clearForm() {
        addDataBinding.edDescription.text?.clear()
        addDataBinding.edAmount.text?.clear()
        addDataBinding.edDescription.requestFocus()
    }


    fun EditText.textChange(): Flow<String> = callbackFlow {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                trySend(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        }//end of textWatcher
        addTextChangedListener(textWatcher)
        awaitClose { removeTextChangedListener(textWatcher) }

    }//end of textChange

    @SuppressLint("DefaultLocale")
    fun openDatePicker(view: View) {
        val button = view as Button
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            // Format: dd/MM/yyyy
            val formattedDate = String.format("%02d-%02d-%02d", year, month + 1, day)
            button.text = formattedDate
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }//end of openDatePicker

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}//end of AddDataActivity
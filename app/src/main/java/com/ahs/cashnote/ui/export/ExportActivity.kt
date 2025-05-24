package com.ahs.cashnote.ui.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahs.cashnote.R
import com.ahs.cashnote.data.local.TransactionDatabase
import com.ahs.cashnote.data.local.TransactionType
import com.ahs.cashnote.data.local.Transactions
import com.ahs.cashnote.data.repository.TransactionRepository
import com.ahs.cashnote.databinding.ActivityExportBinding
import com.ahs.cashnote.viewmodel.TransactionViewModel
import com.ahs.cashnote.viewmodel.TransactionViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExportBinding
    private lateinit var viewModel: TransactionViewModel
    private var adapter = ExportTransactionAdapter()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var filePickerLauncher: ActivityResultLauncher<Array<String>>


    var startDate: String? = null
    var endDate: String? = null

    private var incomeAmount: Double = 0.0
    private var fixedExpenses: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExportBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupToolbar()
        setupInsert()

        val dao = TransactionDatabase.getDatabase(this).transactionDao()
        val repository = TransactionRepository(dao)
        transactionRepository = TransactionRepository(dao)
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        adapter = ExportTransactionAdapter()
        binding.rvExport.layoutManager = LinearLayoutManager(this)
        binding.rvExport.adapter = adapter
        val divider = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        binding.rvExport.addItemDecoration(divider)


        // Observe data
        viewModel.filteredTransactions.observe(this) { transactions ->
            adapter.submitList(transactions)
        }

        // total Income
        viewModel.getTotalIncomes.observe(this) { totalIncome ->
            incomeAmount = totalIncome ?: 0.0
        }

        // total Expense
        viewModel.getTotalExpenses.observe(this) { totalExpense ->
            fixedExpenses = totalExpense ?: 0.0
        }

        setupDateFilter()
        binding.btnExportPDF.setOnClickListener { exportToPdf() }
        binding.btnExportCsv.setOnClickListener { exportToCSV() }
        binding.btnImport.setOnClickListener {
            filePickerLauncher.launch(arrayOf("*/*"))

        }
        filePickerLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    showImportConfirmationDialog(it)
                }
            }


    }//end of onCreate


    private fun showImportConfirmationDialog(fileUri: Uri) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_import_confirm, null)
        val builder = AlertDialog.Builder(this)
            .setTitle("Import CSV")
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        dialogView.findViewById<Button>(R.id.btnImport).setOnClickListener {
            importCsv(fileUri)
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun setupDateFilter() {
        val etStartDate = binding.etStartDate
        val etEndDate = binding.etEndDate


        val radioAll = binding.radioAll
        val radioRange = binding.radioRange
        val radioGroupFilter = binding.radioGroupFilter

        val dateRangeContainer = binding.dateRangeContainer

        radioGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioAll -> {
                    binding.dateRangeContainer.visibility = View.GONE
                    viewModel.setDateRange("2000-01-01", "2099-12-31") // Semua data
                }

                R.id.radioRange -> {
                    binding.dateRangeContainer.visibility = View.VISIBLE
                }
            }
        }



        radioAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dateRangeContainer.visibility = View.GONE

            }
        }

        radioRange.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dateRangeContainer.visibility = View.VISIBLE
            }
        }

        etStartDate.setOnClickListener {
            showMaterialDatePicker { selectedDate ->
                etStartDate.setText(selectedDate)
                startDate = selectedDate
            }
        }

        etEndDate.setOnClickListener {
            showMaterialDatePicker { selectedDate ->
                etEndDate.setText(selectedDate)
                endDate = selectedDate

                if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
                    viewModel.setDateRange(startDate!!, endDate!!)
                }
            }
        }
    }

    private fun setupInsert() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Export/Import Data"
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun exportToPdf() {
        val transactions = adapter.currentList
        if (transactions.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val boldPaint = Paint().apply {
            isFakeBoldText = true
            textSize = 14f
        }
        paint.textSize = 12f

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 50
        val startX = 40

        //header
        canvas.drawText("Transaction Report", 200f, y.toFloat(), boldPaint)
        y += 25
        if (startDate.isNullOrEmpty() && endDate.isNullOrEmpty()) {
            canvas.drawText("Date: All Data", startX.toFloat(), y.toFloat(), paint)
        } else {
            canvas.drawText(
                "Date: ${startDate ?: "-"} s/d ${endDate ?: "-"}",
                startX.toFloat(),
                y.toFloat(),
                paint
            )
        }
        y += 50


        // column header
        canvas.drawText("No", startX.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("Date", startX + 30.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("Description", startX + 120.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("Amount", startX + 320.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("Type", startX + 440.toFloat(), y.toFloat(), boldPaint)
        y += 10
        canvas.drawLine(startX.toFloat(), y.toFloat(), 550.toFloat(), y.toFloat(), paint)
        y += 20

        transactions.forEachIndexed { index, transaction ->
            if (y > 800) {
                pdfDocument.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 2).create()
                pdfDocument.startPage(newPageInfo)
                y = 50
            }

            canvas.drawText((index + 1).toString(), startX.toFloat(), y.toFloat(), paint)
            canvas.drawText(transaction.date, startX + 30.toFloat(), y.toFloat(), paint)
            canvas.drawText(transaction.description, startX + 120.toFloat(), y.toFloat(), paint)
            val formattedAmount = "Rp %,.0f".format(transaction.amount).replace(',', '.')
            canvas.drawText(formattedAmount, startX + 320.toFloat(), y.toFloat(), paint)
            canvas.drawText(
                transaction.typeTransaction.toString(),
                startX + 440.toFloat(),
                y.toFloat(),
                paint
            )
            y += 25
        }
        canvas.drawLine(startX.toFloat(), y.toFloat(), 550.toFloat(), y.toFloat(), paint)
        y += 25
        val formattedAmount = "Rp %,.0f".format(incomeAmount).replace(',', '.')
        canvas.drawText("Total Income: $formattedAmount", startX.toFloat(), y.toFloat(), boldPaint)
        y += 25
        val formattedExpense = "Rp %,.0f".format(fixedExpenses).replace(',', '.')
        canvas.drawText(
            "Total Expense: $formattedExpense",
            startX.toFloat(),
            y.toFloat(),
            boldPaint
        )

        pdfDocument.finishPage(page)

        val fileName = "Transaction_Report_${System.currentTimeMillis()}.pdf"
        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF saved successfully: ${file.absolutePath}", Toast.LENGTH_LONG)
                .show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun exportToCSV() {
        val transactions = adapter.currentList
        if (transactions.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val csvHeader = "Date,Description,Category,Amount,Type"
        val fileName = "transaction_export_${System.currentTimeMillis()}.csv"

        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, fileName)


        try {
            FileWriter(file).use { writer ->
                writer.appendLine(csvHeader)
                transactions.forEachIndexed { index, transaction ->
                    val line =
                        "${transaction.date},${transaction.description},${transaction.category},${transaction.amount},${transaction.typeTransaction}"
                    writer.appendLine(line)
                }
            }
            Toast.makeText(
                this@ExportActivity,
                "CSV saved: ${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this@ExportActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }


    }

    private fun importCsv(uri: Uri) {
        lifecycleScope.launch {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    reader.readLine() // skip header

                    val transactionList = mutableListOf<Transactions>()

                    reader.forEachLine { line ->
                        Log.d("CSV", "Line: $line")
                        val tokens = line.split(",")

                        if (tokens.size >= 5) {
                            val date = tokens[0].trim()
                            val description = tokens[1].trim()
                            val category = tokens[2].trim()
                            val amount = tokens[3].toDoubleOrNull() ?: 0.0
                            val type = when (tokens[4].trim().uppercase()) {
                                "INCOME" -> TransactionType.INCOME
                                "EXPENSE" -> TransactionType.EXPENSE
                                else -> return@forEachLine // skip invalid rows
                            }


                            val transaction = Transactions(
                                date = date,
                                description = description,
                                category = category,
                                amount = amount,
                                typeTransaction = type
                            )

                            transactionList.add(transaction)


                        }
                    }

                    Log.d("IMPORT", "Total transactions parsed: ${transactionList.size}")


                    viewModel.insertAllCsv(transactionList)


                    Toast.makeText(
                        this@ExportActivity,
                        "Import successfully: ${transactionList.size} transactions",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ExportActivity,
                    "Import failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}//end of class


private fun ExportActivity.showMaterialDatePicker(onDateSelected: (String) -> Unit) {
    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select date")
        .build()

    datePicker.show(supportFragmentManager, "DATE_PICKER")
    datePicker.addOnPositiveButtonClickListener { selection ->
        val selectedDate = Date(selection)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate)
        onDateSelected(formattedDate)
    }
}




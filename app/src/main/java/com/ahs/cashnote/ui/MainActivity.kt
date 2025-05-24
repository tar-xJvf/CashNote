package com.ahs.cashnote.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahs.cashnote.R
import com.ahs.cashnote.data.local.TransactionDatabase
import com.ahs.cashnote.data.local.TransactionType
import com.ahs.cashnote.data.repository.TransactionRepository
import com.ahs.cashnote.databinding.ActivityMainBinding
import com.ahs.cashnote.helper.toRupiah
import com.ahs.cashnote.ui.add.AddDataActivity
import com.ahs.cashnote.ui.export.ExportActivity
import com.ahs.cashnote.ui.list.ListExpenseActivity
import com.ahs.cashnote.ui.list.TransactionAdapter
import com.ahs.cashnote.ui.theme.SettingActivity
import com.ahs.cashnote.viewmodel.TransactionViewModel
import com.ahs.cashnote.viewmodel.TransactionViewModelFactory
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private var income: Double = 0.0
    private var expense: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupInserts()
        loadDatabase()


        binding.fab.setOnClickListener { view ->
            //Add new data
            startActivity(Intent(this@MainActivity, AddDataActivity::class.java))
        }


    }

    @SuppressLint("SetTextI18n")
    private fun loadDatabase() {

        val dao = TransactionDatabase.getDatabase(this).transactionDao()
        val repository = TransactionRepository(dao)
        val factory = TransactionViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        // Observer Income & Expense
        viewModel.getTotalIncomes.observe(this, Observer { totalIncome ->
            income = totalIncome ?: 0.0
            binding.tvIncome.text = income.toRupiah()
            updateTotalBalance()
        })
        viewModel.getTotalExpenses.observe(this, Observer { totalExpense ->
            expense = totalExpense ?: 0.0
            binding.tvExpense.text = expense.toRupiah()
            updateTotalBalance()
        })

        // Observer Recent Transactions
        viewModel.getRecentTransactions.observe(this) { list ->
            adapter.submitList(list)
        }
        // Observer Monthly Summary Chart

        viewModel.monthlySummary.observe(this) { list ->
            val incomeMap = mutableMapOf<String, Float>()
            val expenseMap = mutableMapOf<String, Float>()
            val monthNames = listOf(
                "JAN",
                "FEB",
                "MAR",
                "APR",
                "MAY",
                "JUN",
                "JUL",
                "AUG",
                "SEP",
                "OCT",
                "NOV",
                "DEC"
            )

            list.forEach { item ->

                val monthIndex = (item.month.toIntOrNull() ?: 1) - 1 // JAN = index 0
                val monthName = if (monthIndex in 0..11) monthNames[monthIndex] else "UNKNOWN"

                if (item.typeTransaction == "INCOME") {
                    incomeMap[monthName.toString()] = item.total
                } else {
                    expenseMap[monthName.toString()] = item.total
                }
            } //end of forEach

            val incomeEntries = ArrayList<BarEntry>()
            val expenseEntries = ArrayList<BarEntry>()
            val xAxisLabels = ArrayList<String>()


            monthNames.forEachIndexed { index, month ->
                val incomeTotal = incomeMap[month] ?: 0f
                val expenseTotal = expenseMap[month] ?: 0f
                incomeEntries.add(BarEntry(index.toFloat(), incomeTotal))
                expenseEntries.add(BarEntry(index.toFloat(), expenseTotal))
                xAxisLabels.add(month)
            }//end of forEachIndexed


            val incomeSet = BarDataSet(incomeEntries, "Income")
            incomeSet.colors =
                listOf(ContextCompat.getColor(this@MainActivity, R.color.colorbarDataIncome))
            val expenseSet = BarDataSet(expenseEntries, "Expense")
            expenseSet.colors =
                listOf(ContextCompat.getColor(this@MainActivity, R.color.colorbarDataExpense))

            val valueFormatter = object : ValueFormatter() {
                private val formatter = DecimalFormat("#,###", DecimalFormatSymbols().apply {
                    groupingSeparator = '.'
                })

                override fun getFormattedValue(value: Float): String {
                    return if (value >= 1000f) {
                        "${formatter.format(value / 1000)}K"
                    } else {
                        value.toInt().toString()
                    }
                }
            }

            incomeSet.valueFormatter = valueFormatter
            expenseSet.valueFormatter = valueFormatter
            // 🔍 Dark mode check
            val isDarkMode =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val backgroundColor = if (isDarkMode) Color.parseColor("#212121") else Color.WHITE
            var textColor = if (isDarkMode) Color.WHITE else Color.BLACK
            var gridColor = if (isDarkMode) Color.GRAY else Color.LTGRAY

            incomeSet.valueTextColor = textColor
            expenseSet.valueTextColor = textColor

            val data = BarData(incomeSet, expenseSet).apply {
                barWidth = 0.3f
                setValueTextColor(textColor)
            }
            val barWidth = 0.3f
            val barSpace = 0.05f
            val groupSpace = 0.26f
            data.barWidth = barWidth

            val groupCount = monthNames.size
            val chart = binding.barChart

            chart.data = data



            // 🖌️ Apply colors
            chart.setBackgroundColor(backgroundColor)
            chart.legend.textColor = textColor
            chart.description.isEnabled = false


            chart.xAxis.apply {
                axisMinimum = 0f
                axisMaximum = 0f + data.getGroupWidth(groupSpace, barSpace) * groupCount
                granularity = 1f
                isGranularityEnabled = true
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                labelCount = monthNames.size
                setDrawGridLines(true)

                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        chart.xAxis.textColor = textColor
                        return if (index in 0 until xAxisLabels.size) xAxisLabels[index] else ""

                    }
                }
            }


            chart.axisLeft.apply {
                axisMinimum = 0f
                chart.axisLeft.gridColor = gridColor
                chart.axisLeft.textColor = textColor
            }
          chart.axisRight.isEnabled = false
            chart.description.isEnabled = false
            chart.setFitBars(false)
            chart.groupBars(0f, groupSpace, barSpace)
            chart.invalidate()

        }//end of observe


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
                        this@MainActivity,
                        "Data deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Data cannot be deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )//end of adapter
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvRecentTransactions.adapter = adapter

    }//end of loadDatabase


    @SuppressLint("SetTextI18n")
    private fun updateTotalBalance() {
        val totalBalance = income - expense
        binding.tvTotalBalance.text = totalBalance.toRupiah()
    }

    private fun setupInserts() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_list -> {
                val intent = Intent(this, ListExpenseActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_export -> {
                val intent = Intent(this, ExportActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_settings -> {
                startActivity(Intent(this, SettingActivity::class.java))
                return true
            }

            R.id.action_about -> {
                val intent = Intent(this, GuideActivity::class.java)
                startActivity(intent)
                return true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }
}


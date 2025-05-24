package com.ahs.cashnote.helper

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.NumberFormat

fun EditText.addThousandsSeparator() {
    this.addTextChangedListener(object : TextWatcher {
        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString() != current) {
                this@addThousandsSeparator.removeTextChangedListener(this)

                val cleanString = s.toString().replace(".", "").replace(",", "")
                if (cleanString.isNotEmpty()) {
                    val parsed = cleanString.toDoubleOrNull()
                    val formatter: NumberFormat = DecimalFormat("#,###")
                    val formatted = formatter.format(parsed ?: 0.0).replace(",", ".")

                    current = formatted
                    this@addThousandsSeparator.setText(formatted)
                    this@addThousandsSeparator.setSelection(formatted.length)
                }

                this@addThousandsSeparator.addTextChangedListener(this)
            }
        }
    })
}

@SuppressLint("DefaultLocale")
fun Double.toRupiah(): String {
    return "Rp" + String.format("%,.0f", this).replace(",", ".")
}


package com.example.currencyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var sourceAmount: EditText
    private lateinit var targetAmount: TextView
    private lateinit var sourceCurrency: Spinner
    private lateinit var targetCurrency: Spinner
    private lateinit var sourceSymbol: TextView
    private lateinit var targetSymbol: TextView
    private lateinit var exchangeRateInfo: TextView

    private val exchangeRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.9272,
        "VND" to 25391.0,
        "THB" to 36.0,
        "RUB" to 94.0
    )

    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "VND" to "₫",
        "THB" to "฿",
        "RUB" to "₽"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sourceAmount = findViewById(R.id.sourceAmount)
        targetAmount = findViewById(R.id.targetAmount)
        sourceCurrency = findViewById(R.id.sourceCurrency)
        targetCurrency = findViewById(R.id.targetCurrency)
        sourceSymbol = findViewById(R.id.sourceSymbol)
        targetSymbol = findViewById(R.id.targetSymbol)
        exchangeRateInfo = findViewById(R.id.exchangeRateInfo)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exchangeRates.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sourceCurrency.adapter = adapter
        targetCurrency.adapter = adapter

        // Set default selection for source and target currencies
        sourceCurrency.setSelection(adapter.getPosition("VND"))  // Default to VND
        targetCurrency.setSelection(adapter.getPosition("USD"))   // Default to USD

        sourceAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                convertCurrency()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateSymbols()
                convertCurrency()
                // Update exchange rate info whenever currency selection changes
                updateExchangeRateInfo(
                    exchangeRates[targetCurrency.selectedItem.toString()]!! / exchangeRates[sourceCurrency.selectedItem.toString()]!!,
                    sourceCurrency.selectedItem.toString(),
                    targetCurrency.selectedItem.toString()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        sourceCurrency.onItemSelectedListener = itemSelectedListener
        targetCurrency.onItemSelectedListener = itemSelectedListener

        updateSymbols() // Update symbols on startup
        convertCurrency() // Convert currency on startup
    }

    private fun updateSymbols() {
        val sourceCurrencyCode = sourceCurrency.selectedItem.toString()
        val targetCurrencyCode = targetCurrency.selectedItem.toString()

        sourceSymbol.text = currencySymbols[sourceCurrencyCode] ?: ""
        targetSymbol.text = currencySymbols[targetCurrencyCode] ?: ""
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun convertCurrency() {
        val sourceText = sourceAmount.text.toString()
        if (sourceText.isEmpty()) return

        val sourceValue = sourceText.toDoubleOrNull() ?: return
        val sourceCurrencyCode = sourceCurrency.selectedItem.toString()
        val targetCurrencyCode = targetCurrency.selectedItem.toString()

        val rate = exchangeRates[targetCurrencyCode]!! / exchangeRates[sourceCurrencyCode]!!
        val targetValue = sourceValue * rate

        targetAmount.text = String.format("%.2f", targetValue)
    }

    @SuppressLint("SimpleDateFormat", "DefaultLocale", "SetTextI18n")
    private fun updateExchangeRateInfo(rate: Double, sourceCurrencyCode: String, targetCurrencyCode: String) {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
        val date = dateFormat.format(Date())
        exchangeRateInfo.text = "1 $sourceCurrencyCode = ${String.format("%.8f", rate)} $targetCurrencyCode\nUpdated $date"
    }
}

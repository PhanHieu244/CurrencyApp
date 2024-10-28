package com.example.currencyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.ComponentActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var sourceAmount: EditText
    private lateinit var targetAmount: EditText
    private lateinit var sourceCurrency: Spinner
    private lateinit var targetCurrency: Spinner
    private lateinit var sourceSymbol: TextView
    private lateinit var targetSymbol: TextView
    private lateinit var exchangeRateInfo: TextView

    // Exchange rates and symbols data
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

    private var isSourceFocused = true

    // Hold references to TextWatchers
    private lateinit var sourceAmountWatcher: TextWatcher
    private lateinit var targetAmountWatcher: TextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        sourceAmount = findViewById(R.id.sourceAmount)
        targetAmount = findViewById(R.id.targetAmount)
        sourceCurrency = findViewById(R.id.sourceCurrency)
        targetCurrency = findViewById(R.id.targetCurrency)
        sourceSymbol = findViewById(R.id.sourceSymbol)
        targetSymbol = findViewById(R.id.targetSymbol)
        exchangeRateInfo = findViewById(R.id.exchangeRateInfo)

        // Set up adapters for the spinners
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exchangeRates.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sourceCurrency.adapter = adapter
        targetCurrency.adapter = adapter

        // Set default currencies
        sourceCurrency.setSelection(adapter.getPosition("VND"))
        targetCurrency.setSelection(adapter.getPosition("USD"))

        // Setup watchers for amount fields
        setupTextWatchers()

        // Focus listeners to handle input from source or target fields
        setupFocusListeners()

        // Currency selection listeners to update symbols and exchange rate
        setupCurrencySelectionListeners(adapter)

        // Initial setup
        updateSymbols()
        convertCurrency()
    }

    private fun setupTextWatchers() {
        sourceAmountWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isSourceFocused) convertCurrency()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        targetAmountWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isSourceFocused) convertCurrency()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun setupFocusListeners() {
        sourceAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isSourceFocused = true
                sourceAmount.addTextChangedListener(sourceAmountWatcher)
                targetAmount.removeTextChangedListener(targetAmountWatcher)
                setupRateInfo()
            }
        }

        targetAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isSourceFocused = false
                targetAmount.addTextChangedListener(targetAmountWatcher)
                sourceAmount.removeTextChangedListener(sourceAmountWatcher)
                setupRateInfo()
            }
        }
    }

    private fun setupCurrencySelectionListeners(adapter: ArrayAdapter<String>) {
        val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateSymbols()
                convertCurrency()
                setupRateInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        sourceCurrency.onItemSelectedListener = itemSelectedListener
        targetCurrency.onItemSelectedListener = itemSelectedListener
    }

    private fun updateSymbols() {
        sourceSymbol.text = currencySymbols[sourceCurrency.selectedItem.toString()] ?: ""
        targetSymbol.text = currencySymbols[targetCurrency.selectedItem.toString()] ?: ""
    }

    private fun setupRateInfo(){
        if(isSourceFocused){
            updateExchangeRateInfo(
                getExchangeRate(),
                sourceCurrency.selectedItem.toString(),
                targetCurrency.selectedItem.toString()
            )
        }
        else{
            updateExchangeRateInfo(
                getExchangeRate(),
                targetCurrency.selectedItem.toString(),
                sourceCurrency.selectedItem.toString()
            )
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun convertCurrency() {
        // Use var to allow reassignment
        var sourceText = if (isSourceFocused) sourceAmount.text.toString() else targetAmount.text.toString()

        // If the source text is empty, set it to "0"
        if (sourceText.isEmpty()) sourceText = "0"

        // Convert the source text to a double or return if it fails
        val sourceValue = sourceText.toDoubleOrNull() ?: return
        val targetValue = sourceValue * getExchangeRate()

        // Update the respective field based on focus
        if (isSourceFocused) {
            targetAmount.setText(String.format("%.2f", targetValue))
        } else {
            sourceAmount.setText(String.format("%.2f", targetValue))
        }
    }


    private fun getExchangeRate(): Double {
        val sourceRate = exchangeRates[sourceCurrency.selectedItem.toString()] ?: 1.0
        val targetRate = exchangeRates[targetCurrency.selectedItem.toString()] ?: 1.0
        return if (isSourceFocused) targetRate / sourceRate else sourceRate / targetRate
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "DefaultLocale")
    private fun updateExchangeRateInfo(rate: Double, sourceCurrencyCode: String, targetCurrencyCode: String) {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
        val date = dateFormat.format(Date())
        val decimalFormat = DecimalFormat("#.########")
        val formattedRate = decimalFormat.format(rate)

        exchangeRateInfo.text = "1 $sourceCurrencyCode = $formattedRate $targetCurrencyCode\nUpdated $date"
    }

}

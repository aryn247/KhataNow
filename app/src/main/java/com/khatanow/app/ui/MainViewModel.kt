package com.khatanow.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khatanow.app.data.local.AppDatabase
import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity
import com.khatanow.app.data.local.entities.TransactionWithDetails
import com.khatanow.app.data.repository.CustomerRepository
import com.khatanow.app.data.repository.DeviceRepository
import com.khatanow.app.data.repository.ProductRepository
import com.khatanow.app.data.repository.TransactionRepository
import com.khatanow.app.domain.voice.LocalVoiceParser
import com.khatanow.app.domain.voice.SpeechRecognizerManager
import com.khatanow.app.domain.voice.SpeechState
import com.khatanow.app.domain.voice.VoiceParseResult
import com.khatanow.app.util.DeviceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val customerRepository = CustomerRepository(db.customerDao())
    val productRepository = ProductRepository(db.productDao())
    val deviceId = DeviceUtils.getDeviceId(application)
    val transactionRepository = TransactionRepository(db.transactionDao(), deviceId)
    val deviceRepository = DeviceRepository(db.deviceDao())

    val speechManager = SpeechRecognizerManager(application)
    private val voiceParser = LocalVoiceParser()

    val customers: StateFlow<List<CustomerEntity>> = customerRepository.allCustomers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val products: StateFlow<List<ProductEntity>> = productRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val transactions: StateFlow<List<TransactionWithDetails>> = transactionRepository.allTransactions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val speechState: StateFlow<SpeechState> = speechManager.speechState

    private val _parseResult = MutableStateFlow<VoiceParseResult?>(null)
    val parseResult: StateFlow<VoiceParseResult?> = _parseResult.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    init {
        viewModelScope.launch {
            speechState.collect { state ->
                if (state is SpeechState.FinalResult) {
                    parseVoiceInput(state.text)
                }
            }
        }
    }

    fun startVoiceRecording() {
        _parseResult.value = null
        speechManager.startListening()
    }

    fun stopVoiceRecording() {
        speechManager.stopListening()
    }

    fun resetVoiceState() {
        speechManager.reset()
        _parseResult.value = null
    }

    fun parseVoiceInput(spokenText: String) {
        viewModelScope.launch {
            val currentCustomers = customerRepository.getAllCustomersList()
            val currentProducts = productRepository.getAllProductsList()
            val result = voiceParser.parse(spokenText, currentCustomers, currentProducts)
            _parseResult.value = result
        }
    }

    fun confirmVoiceTransaction(
        customer: CustomerEntity,
        product: ProductEntity,
        quantity: Int
    ) {
        viewModelScope.launch {
            transactionRepository.addTransaction(
                customerId = customer.id,
                productId = product.id,
                quantity = quantity
            )
            resetVoiceState()
            _uiMessage.value = "Transaction saved for ${customer.name}!"
        }
    }

    fun saveManualTransaction(
        customerId: String,
        productId: String,
        quantity: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            transactionRepository.addTransaction(
                customerId = customerId,
                productId = productId,
                quantity = quantity
            )
            _uiMessage.value = "Transaction saved!"
            onSuccess()
        }
    }

    fun addCustomer(name: String, phone: String? = null) {
        viewModelScope.launch {
            if (name.trim().isNotEmpty()) {
                customerRepository.addCustomer(name, phone)
                _uiMessage.value = "Customer added: $name"
            }
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.updateCustomer(customer)
            _uiMessage.value = "Customer updated"
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(id)
            _uiMessage.value = "Customer removed"
        }
    }

    fun addProduct(name: String, unit: String? = null) {
        viewModelScope.launch {
            if (name.trim().isNotEmpty()) {
                productRepository.addProduct(name, unit)
                _uiMessage.value = "Product added: $name"
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
            _uiMessage.value = "Product updated"
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            productRepository.deleteProduct(id)
            _uiMessage.value = "Product removed"
        }
    }

    fun updateTransactionQuantity(transactionId: String, newQuantity: Int) {
        viewModelScope.launch {
            if (newQuantity > 0) {
                transactionRepository.updateTransactionQuantity(transactionId, newQuantity)
                _uiMessage.value = "Transaction updated"
            } else {
                transactionRepository.deleteTransaction(transactionId)
                _uiMessage.value = "Transaction deleted"
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
            _uiMessage.value = "Transaction deleted"
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
    }
}

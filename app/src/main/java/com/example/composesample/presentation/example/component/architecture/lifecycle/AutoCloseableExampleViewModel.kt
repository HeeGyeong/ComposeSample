package com.example.composesample.presentation.example.component.architecture.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable

class AutoCloseableExampleViewModel(
    private val itemsService: ItemsService,
    private val customersService: CustomersService,
    private val networkService: NetworkService
) : ViewModel(
    itemsService,
    customersService,
    networkService
) {
    val items: StateFlow<List<String>> = itemsService.items
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val customers: StateFlow<List<String>> = customersService.customers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val networkStatus: StateFlow<String> = networkService.status
        .stateIn(viewModelScope, SharingStarted.Lazily, "Disconnected")

    val serviceStatus: StateFlow<Map<String, ServiceStatusInfo>> = 
        combine(
            itemsService.isActive,
            customersService.isActive,
            networkService.isActive
        ) { itemsActive, customersActive, networkActive ->
            mapOf(
                "ItemsService" to ServiceStatusInfo(
                    name = "ItemsService",
                    isActive = itemsActive,
                    description = "아이템 데이터 관리"
                ),
                "CustomersService" to ServiceStatusInfo(
                    name = "CustomersService",
                    isActive = customersActive,
                    description = "고객 데이터 관리"
                ),
                "NetworkService" to ServiceStatusInfo(
                    name = "NetworkService",
                    isActive = networkActive,
                    description = "네트워크 연결 관리"
                )
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun addItem(item: String) {
        viewModelScope.launch {
            itemsService.addItem(item)
        }
    }

    fun addCustomer(customer: String) {
        viewModelScope.launch {
            customersService.addCustomer(customer)
        }
    }

    fun connectNetwork() {
        viewModelScope.launch {
            networkService.connect()
        }
    }

    fun disconnectNetwork() {
        viewModelScope.launch {
            networkService.disconnect()
        }
    }
}

data class ServiceStatusInfo(
    val name: String,
    val isActive: Boolean,
    val description: String
)

interface ItemsService : Closeable {
    val items: Flow<List<String>>
    val isActive: Flow<Boolean>
    suspend fun addItem(item: String)
    suspend fun getItems(): List<String>
}

interface CustomersService : Closeable {
    val customers: Flow<List<String>>
    val isActive: Flow<Boolean>
    suspend fun addCustomer(customer: String)
    suspend fun getCustomers(): List<String>
}

interface NetworkService : Closeable {
    val status: Flow<String>
    val isActive: Flow<Boolean>
    suspend fun connect()
    suspend fun disconnect()
}

class RealItemsService : ItemsService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _items = MutableStateFlow<List<String>>(emptyList())
    override val items: Flow<List<String>> = _items.asStateFlow()
    
    private val _isActive = MutableStateFlow(true)
    override val isActive: Flow<Boolean> = _isActive.asStateFlow()

    init {
        scope.launch {
            delay(1000)
            _items.value = listOf("Item 1", "Item 2", "Item 3")
        }
    }

    override suspend fun addItem(item: String) {
        _items.update { it + item }
    }

    override suspend fun getItems(): List<String> = _items.value

    override fun close() {
        _isActive.value = false
        scope.cancel()
        println("✅ ItemsService closed - CoroutineScope cancelled")
    }
}

class RealCustomersService : CustomersService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _customers = MutableStateFlow<List<String>>(emptyList())
    override val customers: Flow<List<String>> = _customers.asStateFlow()
    
    private val _isActive = MutableStateFlow(true)
    override val isActive: Flow<Boolean> = _isActive.asStateFlow()

    init {
        scope.launch {
            delay(1500)
            _customers.value = listOf("Customer A", "Customer B")
        }
    }

    override suspend fun addCustomer(customer: String) {
        _customers.update { it + customer }
    }

    override suspend fun getCustomers(): List<String> = _customers.value

    override fun close() {
        _isActive.value = false
        scope.cancel()
        println("✅ CustomersService closed - Database connection closed")
    }
}

class RealNetworkService : NetworkService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _status = MutableStateFlow("Disconnected")
    override val status: Flow<String> = _status.asStateFlow()
    
    private val _isActive = MutableStateFlow(true)
    override val isActive: Flow<Boolean> = _isActive.asStateFlow()
    
    private var connectionJob: Job? = null

    override suspend fun connect() {
        _status.value = "Connecting..."
        delay(2000)
        _status.value = "Connected"
        
        connectionJob = scope.launch {
            var counter = 0
            while (isActive) {
                delay(3000)
                counter++
                _status.value = "Connected (${counter} messages received)"
            }
        }
    }

    override suspend fun disconnect() {
        connectionJob?.cancel()
        _status.value = "Disconnected"
    }

    override fun close() {
        _isActive.value = false
        connectionJob?.cancel()
        scope.cancel()
        println("✅ NetworkService closed - Network connections closed")
    }
}

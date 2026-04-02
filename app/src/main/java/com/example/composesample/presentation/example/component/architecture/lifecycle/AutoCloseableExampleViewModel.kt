package com.example.composesample.presentation.example.component.architecture.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val _lastClosedService = MutableStateFlow<String?>(null)
    val lastClosedService: StateFlow<String?> = _lastClosedService.asStateFlow()

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

    fun forceCloseService(serviceName: String) {
        _lastClosedService.value = serviceName
        when (serviceName) {
            "ItemsService" -> itemsService.close()
            "CustomersService" -> customersService.close()
            "NetworkService" -> networkService.close()
        }
    }

    fun simulateViewModelClear() {
        println("\n🔴🔴🔴 === SIMULATING ViewModel.onCleared() === 🔴🔴🔴")
        onCleared()
        println("🔴🔴🔴 === All AutoCloseable services closed === 🔴🔴🔴\n")
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
        println("🟢 ItemsService initialized - Starting background updates")
        scope.launch {
            delay(1000)
            _items.value = listOf("Item 1", "Item 2", "Item 3")
            
            // 백그라운드에서 계속 아이템 업데이트 (close 되면 중단됨)
            var counter = 4
            while (isActive) {
                delay(5000)
                _items.update { current -> 
                    current + "Auto Item $counter (${System.currentTimeMillis() % 10000})"
                }
                println("📦 ItemsService: Auto-added Item $counter")
                counter++
            }
        }
    }

    override suspend fun addItem(item: String) {
        _items.update { it + item }
        println("➕ ItemsService: Added item '$item'")
    }

    override suspend fun getItems(): List<String> = _items.value

    override fun close() {
        println("🔴 ItemsService.close() called - Cancelling scope")
        _isActive.value = false
        scope.cancel()
        println("✅ ItemsService closed - CoroutineScope cancelled, no more updates")
    }
}

class RealCustomersService : CustomersService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _customers = MutableStateFlow<List<String>>(emptyList())
    override val customers: Flow<List<String>> = _customers.asStateFlow()
    
    private val _isActive = MutableStateFlow(true)
    override val isActive: Flow<Boolean> = _isActive.asStateFlow()

    init {
        println("🟢 CustomersService initialized - Starting DB sync")
        scope.launch {
            delay(1500)
            _customers.value = listOf("Customer A", "Customer B")
            
            // 백그라운드에서 계속 고객 동기화 (close 되면 중단됨)
            var counter = 1
            while (isActive) {
                delay(7000)
                _customers.update { current ->
                    current + "DB Customer $counter (${System.currentTimeMillis() % 10000})"
                }
                println("👤 CustomersService: Synced Customer $counter from DB")
                counter++
            }
        }
    }

    override suspend fun addCustomer(customer: String) {
        _customers.update { it + customer }
        println("➕ CustomersService: Added customer '$customer'")
    }

    override suspend fun getCustomers(): List<String> = _customers.value

    override fun close() {
        println("🔴 CustomersService.close() called - Closing DB connection")
        _isActive.value = false
        scope.cancel()
        println("✅ CustomersService closed - Database connection closed, no more sync")
    }
}

class RealNetworkService : NetworkService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _status = MutableStateFlow("Disconnected")
    override val status: Flow<String> = _status.asStateFlow()
    
    private val _isActive = MutableStateFlow(true)
    override val isActive: Flow<Boolean> = _isActive.asStateFlow()
    
    private var connectionJob: Job? = null

    init {
        println("🟢 NetworkService initialized - Ready to connect")
    }

    override suspend fun connect() {
        println("🌐 NetworkService: Connecting...")
        _status.value = "Connecting..."
        delay(2000)
        _status.value = "Connected"
        println("✅ NetworkService: Connected")
        
        connectionJob = scope.launch {
            var counter = 0
            while (isActive) {
                delay(3000)
                counter++
                _status.value = "Connected (${counter} messages received)"
                println("📨 NetworkService: Received message $counter")
            }
        }
    }

    override suspend fun disconnect() {
        println("🔌 NetworkService: Disconnecting...")
        connectionJob?.cancel()
        _status.value = "Disconnected"
        println("⏸️ NetworkService: Disconnected")
    }

    override fun close() {
        println("🔴 NetworkService.close() called - Closing all connections")
        _isActive.value = false
        connectionJob?.cancel()
        scope.cancel()
        println("✅ NetworkService closed - All network connections closed")
    }
}

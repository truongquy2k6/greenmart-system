package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GreenMartViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = GreenMartRepository(database.greenMartDao())

    private val sharedPrefs = application.getSharedPreferences("greenmart_prefs", android.content.Context.MODE_PRIVATE)

    private val _currentMaKH = MutableStateFlow<String>(sharedPrefs.getString("logged_ma_kh", "") ?: "")
    val currentMaKH: StateFlow<String> = _currentMaKH.asStateFlow()
    val maKH: String get() = _currentMaKH.value

    private fun saveSession(maKH: String) {
        _currentMaKH.value = maKH
        sharedPrefs.edit().putString("logged_ma_kh", maKH).apply()
    }

    // --- State Streams ---
    val customerState: StateFlow<KhachHang?> = _currentMaKH
        .flatMapLatest { id -> repository.getKhachHangFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoriesState: StateFlow<List<LoaiSanPham>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProductsStore: StateFlow<List<SanPham>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String>("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered Products State List
    val filteredProducts: StateFlow<List<SanPham>> = combine(
        allProductsStore,
        _selectedCategory,
        _searchQuery
    ) { products, category, query ->
        var list = products
        if (category != "Tất cả") {
            list = list.filter { it.MaLoai == category }
        }
        if (query.isNotBlank()) {
            list = list.filter { it.TenSP.contains(query, ignoreCase = true) || it.MoTa.contains(query, ignoreCase = true) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeVouchers: StateFlow<List<KhuyenMai>> = repository.activeVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myVouchers: StateFlow<List<KhuyenMai>> = _currentMaKH
        .flatMapLatest { id -> repository.getMyVouchers(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storesList: StateFlow<List<CuaHang>> = repository.allStores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStore = MutableStateFlow<CuaHang?>(null)
    val selectedStore: StateFlow<CuaHang?> = _selectedStore.asStateFlow()

    val redeemableGifts: StateFlow<List<QuaDongTichDiem>> = repository.allGifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val redemptionHistory: StateFlow<List<LichSuDoiQua>> = _currentMaKH
        .flatMapLatest { id -> repository.getMyRedemptions(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastOrders: StateFlow<List<HoaDon>> = _currentMaKH
        .flatMapLatest { id ->
            if (id.isNotBlank()) {
                viewModelScope.launch {
                    repository.syncInvoicesFromApi(id)
                }
            }
            repository.getMyInvoices(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Cart & Checkout States ---
    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cartItems: StateFlow<Map<String, Int>> = _cartItems.asStateFlow()

    private val _appliedVoucher = MutableStateFlow<KhuyenMai?>(null)
    val appliedVoucher: StateFlow<KhuyenMai?> = _appliedVoucher.asStateFlow()

    private val _selectedCheckoutStore = MutableStateFlow<CuaHang?>(null)
    val selectedCheckoutStore: StateFlow<CuaHang?> = _selectedCheckoutStore.asStateFlow()

    // Combined cart items with full details
    val cartWithDetails: StateFlow<List<Pair<SanPham, Int>>> = combine(_cartItems, allProductsStore) { cartMap, products ->
        cartMap.mapNotNull { entry ->
            val product = products.find { it.MaSP == entry.key }
            if (product != null) product to entry.value else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Financial Summary
    val cartSummary: StateFlow<CartSummary> = combine(cartWithDetails, _appliedVoucher) { details, voucher ->
        val subtotal = details.sumOf { it.first.DonGia * it.second }
        var discount = 0.0

        if (voucher != null && subtotal >= voucher.DieuKien) {
            discount = if (voucher.LoaiKM == "Giảm theo %") {
                val calculated = (subtotal * (voucher.GiaTri / 100.0))
                // Max discount of 50k as stated in sample data description
                if (voucher.MaKM == "KM02") minOf(calculated, 50000.0) else calculated
            } else {
                voucher.GiaTri
            }
        }

        // Apply flat delivery fee of 15k, free for bills over 300k
        val shippingFee = if (subtotal == 0.0 || subtotal >= 300000.0) 0.0 else 15000.0
        val total = maxOf(0.0, subtotal - discount + shippingFee)

        // Earn loyalty points (1 point for every 20,000 VND of grand total)
        val pointsEarned = (total / 20000.0).toInt()

        CartSummary(subtotal, discount, shippingFee, total, pointsEarned)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary())

    // --- Action Events ---
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        // Sync master data from remote SQL Server API
        viewModelScope.launch {
            repository.syncWithApi()
        }

        // Set default checkout store once stores are loaded
        viewModelScope.launch {
            storesList.collect { list ->
                if (list.isNotEmpty() && _selectedCheckoutStore.value == null) {
                    _selectedCheckoutStore.value = list.first()
                    _selectedStore.value = list.first()
                }
            }
        }
    }

    fun triggerToast(msg: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast(msg))
        }
    }

    fun selectCategory(categoryName: String) {
        _selectedCategory.value = categoryName
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(maSP: String) {
        // Đồng bộ khoảng trắng đệm char(10) của SQL Server: tìm mã sản phẩm chuẩn tương thích
        val matchedProduct = allProductsStore.value.find { it.MaSP.trim() == maSP.trim() }
        val exactMaSP = matchedProduct?.MaSP ?: maSP
        val currentMap = _cartItems.value.toMutableMap()
        currentMap[exactMaSP] = (currentMap[exactMaSP] ?: 0) + 1
        _cartItems.value = currentMap
    }

    fun subtractFromCart(maSP: String) {
        val currentMap = _cartItems.value.toMutableMap()
        val count = currentMap[maSP] ?: 0
        if (count <= 1) {
            currentMap.remove(maSP)
        } else {
            currentMap[maSP] = count - 1
        }
        _cartItems.value = currentMap
    }

    fun removeFromCart(maSP: String) {
        val currentMap = _cartItems.value.toMutableMap()
        currentMap.remove(maSP)
        _cartItems.value = currentMap
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _appliedVoucher.value = null
    }

    fun applyVoucher(voucher: KhuyenMai) {
        val currentSubtotal = cartSummary.value.subtotal
        if (currentSubtotal < voucher.DieuKien) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Đơn hàng chưa đạt mức tối thiểu ${String.format("%,.0f", voucher.DieuKien)}đ để áp dụng."))
            }
            return
        }
        _appliedVoucher.value = voucher
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast("Đã áp dụng mã giảm giá ${voucher.TenKM} thành công!"))
        }
    }

    fun removeVoucher() {
        _appliedVoucher.value = null
    }

    fun setCheckoutStore(store: CuaHang) {
        _selectedCheckoutStore.value = store
    }

    fun setSelectedStore(store: CuaHang) {
        _selectedStore.value = store
    }

    // --- Claim / Buy Vouchers ---
    fun claimPublicVoucher(voucher: KhuyenMai) {
        if (maKH.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Vui lòng đăng nhập để thực hiện lưu mã giảm giá!"))
            }
            return
        }
        viewModelScope.launch {
            val success = repository.claimVoucher(maKH, voucher.MaKM)
            if (success) {
                _uiEvent.emit(UiEvent.ShowToast("Đã lưu mã giảm giá ${voucher.TenKM} vào ví của bạn."))
            } else {
                _uiEvent.emit(UiEvent.ShowToast("Lưu mã giảm giá thất bại."))
            }
        }
    }

    // --- Point Exchange (Redemption) ---
    fun redeemGift(gift: QuaDongTichDiem) {
        if (maKH.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Vui lòng đăng nhập để thực hiện đổi quà tích điểm!"))
            }
            return
        }
        viewModelScope.launch {
            val (success, message) = repository.redeemPointForGift(maKH, gift)
            if (success) {
                _uiEvent.emit(UiEvent.ShowDialog("Đổi Quà Thành Công!", message))
            } else {
                _uiEvent.emit(UiEvent.ShowToast(message))
            }
        }
    }

    // --- Payment Config retrieval ---
    private val _paymentConfig = MutableStateFlow<CauHinh?>(null)
    val paymentConfig: StateFlow<CauHinh?> = _paymentConfig.asStateFlow()

    fun loadPaymentConfigForSelectedStore() {
        val store = _selectedCheckoutStore.value ?: return
        viewModelScope.launch {
            val config = repository.getCauHinhByStore(store.MaCH)
            _paymentConfig.value = config
        }
    }

    // --- Order placement ---
    fun submitCheckout(paymentMethod: String): Flow<CheckoutResult> = flow {
        emit(CheckoutResult.Loading("Đang khởi tạo giao dịch thanh toán..."))
        
        val store = _selectedCheckoutStore.value
        if (store == null) {
            emit(CheckoutResult.Error("Vui lòng chọn cửa hàng lấy hàng."))
            return@flow
        }

        val cart = cartWithDetails.value
        if (cart.isEmpty()) {
            emit(CheckoutResult.Error("Giỏ hàng của bạn đang trống."))
            return@flow
        }

        val summary = cartSummary.value

        // Simulate secure bank QR coding or online banking processing
        kotlinx.coroutines.delay(1200)
        emit(CheckoutResult.Loading("Đang đối soát thông tin qua cổng $paymentMethod..."))

        kotlinx.coroutines.delay(1000)
        emit(CheckoutResult.Loading("Đã ghi nhận thanh toán thành công. Đang cập nhật điểm thành viên..."))
        
        // 1. Process Database Place Order
        val (success, invoiceId) = repository.placeOrder(
            maKH = maKH,
            maCH = store.MaCH,
            maKM = _appliedVoucher.value?.MaKM,
            subtotal = summary.subtotal,
            discount = summary.discount,
            total = summary.total,
            paymentMethod = paymentMethod,
            itemsInCart = cart,
            pointsEarned = summary.pointsEarned
        )

        kotlinx.coroutines.delay(500)
        if (success) {
            // Clear cart
            clearCart()
            emit(CheckoutResult.Success(invoiceId, "Đơn hàng $invoiceId hoàn tất! Cửa hàng ${store.TenCH} đã bắt đầu chế biến thực tươi nấu nướng."))
        } else {
            emit(CheckoutResult.Error("Đặt hàng thất bại. Vui lòng kiểm tra kết nối cơ sở dữ liệu."))
        }
    }

    fun getInvoiceDetails(maHD: String): Flow<List<ChiTietHoaDon>> {
        return repository.getInvoiceDetails(maHD)
    }

    // --- Authentication Actions (Login, Sign-up, Logout) ---
    suspend fun login(phoneNumber: String, matKhau: String): Boolean {
        if (phoneNumber.isBlank() || matKhau.isBlank()) {
            triggerToast("Vui lòng nhập đầy đủ Số điện thoại & Mật khẩu!")
            return false
        }
        val user = repository.getKhachHangByPhone(phoneNumber.trim())
        if (user != null) {
            if (user.MatKhau == matKhau) {
                _currentMaKH.value = user.MaKH
                triggerToast("Đăng nhập thành công! Chào mừng ${user.HoTen}.")
                return true
            } else {
                triggerToast("Sai mật khẩu. Vui lòng kiểm tra lại!")
                return false
            }
        } else {
            triggerToast("Không tìm thấy tài khoản với số điện thoại này.")
            return false
        }
    }

    suspend fun register(hoTen: String, soDienThoai: String, email: String, diaChi: String, matKhau: String): Boolean {
        if (hoTen.isBlank() || soDienThoai.isBlank() || email.isBlank() || diaChi.isBlank() || matKhau.isBlank()) {
            triggerToast("Vui lòng điền đầy đủ tất cả các trường!")
            return false
        }
        
        // Check if phone already registered
        val existing = repository.getKhachHangByPhone(soDienThoai.trim())
        if (existing != null) {
            triggerToast("Số điện thoại này đã được đăng ký.")
            return false
        }

        val newCustomer = KhachHang(
            MaKH = "", // Auto-generated by remote Web API
            HoTen = hoTen.trim(),
            SoDienThoai = soDienThoai.trim(),
            DiaChi = diaChi.trim(),
            Email = email.trim(),
            DiemTichLuy = 50, // Welcome points
            NgayTao = System.currentTimeMillis(),
            TrangThai = "Hoạt động",
            MatKhau = matKhau
        )
        
        val generatedMaKH = repository.registerCustomer(newCustomer)
        if (generatedMaKH != null) {
            _currentMaKH.value = generatedMaKH
            triggerToast("Đăng ký thành công! Nhận quà tặng 50 điểm tích lũy mới.")
            return true
        } else {
            triggerToast("Đăng ký thất bại. Vui lòng kiểm tra lại kết nối mạng!")
            return false
        }
    }

    // --- OTP Passwordless Authentication ---
    suspend fun checkPhoneExists(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        return repository.getKhachHangByPhone(phoneNumber.trim()) != null
    }

    suspend fun loginWithOtp(phoneNumber: String): Boolean {
        val user = repository.getKhachHangByPhone(phoneNumber.trim())
        if (user != null) {
            saveSession(user.MaKH)
            triggerToast("Đăng nhập thành công! Chào mừng ${user.HoTen}.")
            return true
        } else {
            triggerToast("Đăng nhập thất bại. Không tìm thấy khách hàng!")
            return false
        }
    }

    suspend fun registerWithOtp(hoTen: String, soDienThoai: String, email: String, diaChi: String): Boolean {
        if (hoTen.isBlank() || soDienThoai.isBlank() || email.isBlank() || diaChi.isBlank()) {
            triggerToast("Vui lòng điền đầy đủ thông tin khách hàng!")
            return false
        }
        val newCustomer = KhachHang(
            MaKH = "", // Auto-generated by remote Web API
            HoTen = hoTen.trim(),
            SoDienThoai = soDienThoai.trim(),
            DiaChi = diaChi.trim(),
            Email = email.trim(),
            DiemTichLuy = 50, // Welcome points
            NgayTao = System.currentTimeMillis(),
            TrangThai = "Hoạt động",
            MatKhau = "OTP_AUTH"
        )
        
        val generatedMaKH = repository.registerCustomer(newCustomer)
        if (generatedMaKH != null) {
            saveSession(generatedMaKH)
            triggerToast("Đăng ký thành công! Nhận quà tặng 50 điểm tích lũy mới.")
            return true
        } else {
            triggerToast("Đăng ký thất bại. Vui lòng kiểm tra kết nối mạng!")
            return false
        }
    }

    fun updateCustomerProfile(hoTen: String, soDienThoai: String, email: String, diaChi: String) {
        viewModelScope.launch {
            val current = repository.getKhachHang(maKH)
            if (current != null) {
                val updated = current.copy(
                    HoTen = hoTen.trim(),
                    SoDienThoai = soDienThoai.trim(),
                    Email = email.trim(),
                    DiaChi = diaChi.trim()
                )
                repository.updateCustomer(updated)
                triggerToast("Cập nhật thông tin thành công!")
            }
        }
    }

    fun logout() {
        _currentMaKH.value = ""
        sharedPrefs.edit().remove("logged_ma_kh").apply()
        clearCart()
        triggerToast("Đã đăng xuất tài khoản.")
    }
}

data class CartSummary(
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val shippingFee: Double = 0.0,
    val total: Double = 0.0,
    val pointsEarned: Int = 0
)

sealed interface CheckoutResult {
    data class Loading(val msg: String) : CheckoutResult
    data class Success(val invoiceId: String, val message: String) : CheckoutResult
    data class Error(val error: String) : CheckoutResult
}

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class ShowDialog(val title: String, val message: String) : UiEvent
}

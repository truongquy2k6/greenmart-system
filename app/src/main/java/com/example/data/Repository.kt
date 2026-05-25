package com.example.data

import com.example.data.network.ApiCreateInvoiceRequest
import com.example.data.network.ApiInvoiceDetailRequest
import com.example.data.network.ApiRegisterRequest
import com.example.data.network.ApiUpdateRequest
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GreenMartRepository(private val dao: GreenMartDao) {

    // --- API Synchronization ---
    suspend fun syncWithApi() {
        try {
            // 1. Sync Categories (LoaiSanPham)
            val categories = RetrofitClient.apiService.getCategories()
            if (categories.isNotEmpty()) {
                dao.insertAllLoaiSanPham(categories)
            }

            // 2. Sync Products (SanPham)
            val products = RetrofitClient.apiService.getProducts()
            if (products.isNotEmpty()) {
                dao.insertAllSanPham(products)
            }

            // 3. Sync Stores (CuaHang)
            val stores = RetrofitClient.apiService.getStores()
            if (stores.isNotEmpty()) {
                dao.insertAllCuaHang(stores)
            }

            // 4. Sync Vouchers (KhuyenMai)
            val vouchers = RetrofitClient.apiService.getVouchers()
            if (vouchers.isNotEmpty()) {
                dao.insertAllKhuyenMai(vouchers)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Offline fallback - keep running with SQLite seed data
        }
    }

    // --- KhachHang (Customer) ---
    fun getKhachHangFlow(maKH: String): Flow<KhachHang?> = dao.getKhachHangFlow(maKH)
    
    suspend fun getKhachHang(maKH: String): KhachHang? = dao.getKhachHang(maKH)
    
    suspend fun getKhachHangByPhone(phone: String): KhachHang? {
        return try {
            val customer = RetrofitClient.apiService.getCustomerByPhone(phone)
            dao.insertKhachHang(customer)
            customer
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to local Room database if API fails or offline
            dao.getKhachHangByPhone(phone)
        }
    }
    
    suspend fun registerCustomer(khachHang: KhachHang): String? {
        return try {
            val response = RetrofitClient.apiService.registerCustomer(
                ApiRegisterRequest(
                    HoTen = khachHang.HoTen,
                    SoDienThoai = khachHang.SoDienThoai,
                    DiaChi = khachHang.DiaChi,
                    Email = khachHang.Email
                )
            )
            val customerWithId = khachHang.copy(MaKH = response.maKH)
            dao.insertKhachHang(customerWithId)
            response.maKH
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun updateCustomer(khachHang: KhachHang) {
        try {
            RetrofitClient.apiService.updateCustomer(
                ApiUpdateRequest(
                    MaKH = khachHang.MaKH,
                    HoTen = khachHang.HoTen,
                    SoDienThoai = khachHang.SoDienThoai,
                    DiaChi = khachHang.DiaChi,
                    Email = khachHang.Email,
                    DiemTichLuy = khachHang.DiemTichLuy,
                    TrangThai = khachHang.TrangThai
                )
            )
            dao.updateKhachHang(khachHang)
        } catch (e: Exception) {
            e.printStackTrace()
            dao.updateKhachHang(khachHang)
        }
    }

    // --- LoaiSanPham (Categories) ---
    val allCategories: Flow<List<LoaiSanPham>> = dao.getAllLoaiSanPhamFlow()

    // --- SanPham (Products) ---
    val activeProducts: Flow<List<SanPham>> = dao.getActiveProductsFlow()
    fun getProductsByCategory(maLoai: String): Flow<List<SanPham>> = dao.getProductsByCategoryFlow(maLoai)
    fun searchProducts(query: String): Flow<List<SanPham>> = dao.searchProductsFlow(query)
    suspend fun getProductById(maSP: String): SanPham? = dao.getProductById(maSP)

    // --- CuaHang (Stores) ---
    val allStores: Flow<List<CuaHang>> = dao.getAllCuaHangFlow()

    // --- KhuyenMai (Vouchers) ---
    val activeVouchers: Flow<List<KhuyenMai>> = dao.getActiveKhuyenMaiFlow()
    suspend fun getKhuyenMaiById(maKM: String): KhuyenMai? = dao.getKhuyenMaiById(maKM)

    // --- VoucherKhachHang (Saved Vouchers) ---
    fun getMyVouchers(maKH: String): Flow<List<KhuyenMai>> = dao.getMyUnusedVouchersFlow(maKH)
    
    suspend fun claimVoucher(maKH: String, maKM: String): Boolean {
        // Safe check
        val voucher = dao.getKhuyenMaiById(maKM) ?: return false
        dao.insertVoucherKhachHang(
            VoucherKhachHang(
                MaKH = maKH,
                MaKM = maKM,
                DaDung = false,
                NgayNhan = System.currentTimeMillis()
            )
        )
        return true
    }

    // --- HoaDon & Order Placement ---
    fun getMyInvoices(maKH: String): Flow<List<HoaDon>> = dao.getMyInvoicesFlow(maKH)
    fun getInvoiceDetails(maHD: String): Flow<List<ChiTietHoaDon>> = dao.getInvoiceDetailsFlow(maHD)
    
    // Sync invoices history from API to local SQLite DB
    suspend fun syncInvoicesFromApi(maKH: String) {
        try {
            val invoices = RetrofitClient.apiService.getInvoices(maKH)
            for (invoice in invoices) {
                dao.insertHoaDon(invoice)
                try {
                    val details = RetrofitClient.apiService.getInvoiceDetails(invoice.MaHD)
                    dao.insertAllChiTietHoaDon(details)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun placeOrder(
        maKH: String,
        maCH: String,
        maKM: String?,
        subtotal: Double,
        discount: Double,
        total: Double,
        paymentMethod: String,
        itemsInCart: List<Pair<SanPham, Int>>,
        pointsEarned: Int
    ): Pair<Boolean, String> {
        return try {
            // 1. Prepare invoice details API request
            val details = itemsInCart.map { (product, quantity) ->
                ApiInvoiceDetailRequest(
                    MaSP = product.MaSP,
                    SoLuong = quantity,
                    DonGia = product.DonGia
                )
            }
            
            // 2. Post to API
            val orderResponse = RetrofitClient.apiService.placeOrder(
                ApiCreateInvoiceRequest(
                    MaKH = maKH,
                    TongTien = total,
                    MaKM = maKM,
                    GiamGia = discount,
                    PhuongThucThanhToan = paymentMethod,
                    ChiTiet = details
                )
            )
            
            val maHD = orderResponse.maHD
            val timestamp = System.currentTimeMillis()

            // 3. Cache the invoice locally in SQLite Room DB
            val newInvoice = HoaDon(
                MaHD = maHD,
                NgayLap = timestamp,
                TongTien = total,
                MaKH = maKH,
                MaCH = maCH,
                MaKM = maKM,
                GiamGia = discount,
                PhuongThucThanhToan = paymentMethod,
                TrangThai = "Đã thanh toán"
            )
            dao.insertHoaDon(newInvoice)

            val localDetails = itemsInCart.map { (product, quantity) ->
                ChiTietHoaDon(
                    MaHD = maHD,
                    MaSP = product.MaSP,
                    SoLuong = quantity,
                    DonGia = product.DonGia,
                    ThanhTien = product.DonGia * quantity
                )
            }
            dao.insertAllChiTietHoaDon(localDetails)

            // 4. Mark Voucher as Used if applied
            if (maKM != null) {
                dao.markVoucherAsUsed(maKH, maKM)
            }

            // 5. Sync updated customer points from remote database
            try {
                val updatedCustomer = RetrofitClient.apiService.getCustomerByPhone(dao.getKhachHang(maKH)?.SoDienThoai ?: "")
                dao.insertKhachHang(updatedCustomer)
            } catch (e: Exception) {
                val customer = dao.getKhachHang(maKH)
                if (customer != null) {
                    val updatedPoints = customer.DiemTichLuy + pointsEarned
                    dao.updateKhachHang(customer.copy(DiemTichLuy = updatedPoints))
                }
            }

            Pair(true, maHD)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "")
        }
    }

    // --- Loyalty Gift System ---
    val allGifts: Flow<List<QuaDongTichDiem>> = dao.getAllGiftsFlow()
    fun getMyRedemptions(maKH: String): Flow<List<LichSuDoiQua>> = dao.getMyRedemptionHistoryFlow(maKH)

    suspend fun redeemPointForGift(maKH: String, gift: QuaDongTichDiem): Pair<Boolean, String> {
        val customer = dao.getKhachHang(maKH) ?: return Pair(false, "Không tìm thấy thông tin khách hàng.")
        
        if (customer.DiemTichLuy < gift.DiemCanThiet) {
            return Pair(false, "Điểm tích lũy không đủ để đổi món quà này.")
        }

        if (gift.SoLuongCon <= 0) {
            return Pair(false, "Món quà này đã tạm thời hết hàng.")
        }

        // Deduct points from Customer and update in remote DB
        val updatedCustomer = customer.copy(DiemTichLuy = customer.DiemTichLuy - gift.DiemCanThiet)
        updateCustomer(updatedCustomer)

        // Reduce gift quantity locally
        val updatedGift = gift.copy(SoLuongCon = gift.SoLuongCon - 1)
        dao.insertAllGifts(listOf(updatedGift))

        // Save to redemption history
        val maLichSu = "LS" + UUID.randomUUID().toString().take(8).uppercase()
        dao.insertLichSuDoiQua(
            LichSuDoiQua(
                MaLichSu = maLichSu,
                MaKH = maKH,
                MaQua = gift.MaQua,
                NgayDoi = System.currentTimeMillis(),
                SoDiemKhauTru = gift.DiemCanThiet
            )
        )

        return Pair(true, "Mã đổi quà của bạn: ${maLichSu.takeLast(6)}. Hãy mang tới cửa hàng GreenMart gần nhất để nhận!")
    }

    // --- Configuration (Bank details for stores) ---
    suspend fun getCauHinhByStore(maCH: String): CauHinh? {
        val local = dao.getCauHinhByStore(maCH)
        if (local != null && local.BankId.isNotBlank()) return local
        return try {
            val remote = RetrofitClient.apiService.getStoreBankConfig(maCH)
            dao.insertAllCauHinh(listOf(remote))
            remote
        } catch (e: Exception) {
            e.printStackTrace()
            local
        }
    }
}

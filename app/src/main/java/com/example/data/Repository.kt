package com.example.data

import com.example.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GreenMartRepository(private val dao: GreenMartDao) {

    private val apiService = RetrofitClient.apiService

    suspend fun syncWithApi() {
        try {
            val categories = apiService.getCategories()
            if (categories.isNotEmpty()) {
                dao.insertAllLoaiSanPham(categories)
            }
            val products = apiService.getProducts()
            if (products.isNotEmpty()) {
                dao.insertAllSanPham(products)
            }
            val stores = apiService.getStores()
            if (stores.isNotEmpty()) {
                dao.insertAllCuaHang(stores)
            }
            val vouchers = apiService.getVouchers()
            if (vouchers.isNotEmpty()) {
                dao.insertAllKhuyenMai(vouchers)
            }
        } catch (e: Exception) {
            // Ignore startup sync network errors
        }
    }


    // --- Master Data Streams ---
    val allCategories: Flow<List<LoaiSanPham>> = dao.getAllLoaiSanPhamFlow()
    val activeProducts: Flow<List<SanPham>> = dao.getActiveProductsFlow()
    val activeVouchers: Flow<List<KhuyenMai>> = dao.getActiveKhuyenMaiFlow()
    val allStores: Flow<List<CuaHang>> = dao.getAllCuaHangFlow()
    val allGifts: Flow<List<QuaDongTichDiem>> = dao.getAllGiftsFlow()

    // --- Customer Authentication & Profiles ---
    fun getKhachHangFlow(maKH: String): Flow<KhachHang?> = dao.getKhachHangFlow(maKH)

    suspend fun getKhachHang(maKH: String): KhachHang? = dao.getKhachHang(maKH)

    suspend fun getKhachHangByPhone(phone: String): KhachHang? {
        val trimmed = phone.trim()
        return try {
            val remote = apiService.getCustomerByPhone(trimmed)
            dao.insertKhachHang(remote)
            remote
        } catch (e: Exception) {
            dao.getKhachHangByPhone(trimmed)
        }
    }

    suspend fun registerCustomer(khachHang: KhachHang): String? {
        return try {
            val response = apiService.registerCustomer(
                ApiRegisterRequest(
                    HoTen = khachHang.HoTen,
                    SoDienThoai = khachHang.SoDienThoai,
                    DiaChi = khachHang.DiaChi,
                    Email = khachHang.Email
                )
            )
            val registered = khachHang.copy(
                MaKH = response.maKH,
                MatKhau = khachHang.MatKhau
            )
            dao.insertKhachHang(registered)
            response.maKH
        } catch (e: Exception) {
            val localMaKH = "KH" + (System.currentTimeMillis() % 1000000)
            val local = khachHang.copy(MaKH = localMaKH)
            dao.insertKhachHang(local)
            localMaKH
        }
    }

    suspend fun updateCustomer(khachHang: KhachHang) {
        dao.updateKhachHang(khachHang)
        try {
            apiService.updateCustomer(
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
        } catch (e: Exception) {
            // Offline/Network error - ignore remote update
        }
    }

    // --- Vouchers & Loyalty ---
    fun getMyVouchers(maKH: String): Flow<List<KhuyenMai>> = dao.getMyUnusedVouchersFlow(maKH)

    suspend fun claimVoucher(maKH: String, maKM: String): Boolean {
        return try {
            dao.insertVoucherKhachHang(VoucherKhachHang(maKH, maKM, false))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMyRedemptions(maKH: String): Flow<List<LichSuDoiQua>> = dao.getMyRedemptionHistoryFlow(maKH)

    suspend fun redeemPointForGift(maKH: String, gift: QuaDongTichDiem): Pair<Boolean, String> {
        val customer = dao.getKhachHang(maKH) ?: return Pair(false, "Không tìm thấy thông tin khách hàng!")
        if (customer.DiemTichLuy < gift.DiemCanThiet) {
            return Pair(false, "Số điểm tích lũy hiện có (${customer.DiemTichLuy}đ) không đủ để đổi quà này!")
        }
        if (gift.SoLuongCon <= 0) {
            return Pair(false, "Quà tặng này đã hết hàng trong hệ thống!")
        }

        // Deduct customer points
        val updatedCustomer = customer.copy(DiemTichLuy = customer.DiemTichLuy - gift.DiemCanThiet)
        dao.updateKhachHang(updatedCustomer)

        // Deduct gift quantity
        val updatedGift = gift.copy(SoLuongCon = gift.SoLuongCon - 1)
        dao.insertAllGifts(listOf(updatedGift))

        // Save history entry
        val maLichSu = "LS" + (System.currentTimeMillis() % 1000000)
        val historyEntry = LichSuDoiQua(
            MaLichSu = maLichSu,
            MaKH = maKH,
            MaQua = gift.MaQua,
            NgayDoi = System.currentTimeMillis(),
            SoDiemKhauTru = gift.DiemCanThiet,
            TrangThai = "Đã quy đổi"
        )
        dao.insertLichSuDoiQua(historyEntry)

        // Remote update customer points
        try {
            apiService.updateCustomer(
                ApiUpdateRequest(
                    MaKH = updatedCustomer.MaKH,
                    HoTen = updatedCustomer.HoTen,
                    SoDienThoai = updatedCustomer.SoDienThoai,
                    DiaChi = updatedCustomer.DiaChi,
                    Email = updatedCustomer.Email,
                    DiemTichLuy = updatedCustomer.DiemTichLuy,
                    TrangThai = updatedCustomer.TrangThai
                )
            )
        } catch (e: Exception) {
            // Ignore remote syncing errors
        }

        return Pair(true, "Quy đổi quà '${gift.TenQua}' thành công! Trừ -${gift.DiemCanThiet} điểm. Mã quà nhận tại siêu thị: $maLichSu.")
    }

    // --- Configurations & Syncing ---
    suspend fun getCauHinhByStore(maCH: String): CauHinh? {
        val local = dao.getCauHinhByStore(maCH)
        if (local != null && local.BankId.isNotBlank()) return local
        return try {
            val remote = apiService.getStoreBankConfig(maCH)
            dao.insertAllCauHinh(listOf(remote))
            remote
        } catch (e: Exception) {
            local
        }
    }

    // --- Order placement & History ---
    fun getMyInvoices(maKH: String): Flow<List<HoaDon>> = dao.getMyInvoicesFlow(maKH)

    fun getInvoiceDetails(maHD: String): Flow<List<ChiTietHoaDon>> = dao.getInvoiceDetailsFlow(maHD)

    suspend fun syncInvoicesFromApi(maKH: String) {
        if (maKH.isBlank()) return
        try {
            val remoteInvoices = apiService.getInvoices(maKH)
            for (invoice in remoteInvoices) {
                dao.insertHoaDon(invoice)
                try {
                    val details = apiService.getInvoiceDetails(invoice.MaHD)
                    dao.insertAllChiTietHoaDon(details)
                } catch (ex: Exception) {
                    // Ignore single invoice detail errors
                }
            }
        } catch (e: Exception) {
            // Ignore network sync exceptions
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
        val trimmedKH = maKH.trim()
        val trimmedCH = maCH.trim()
        val trimmedKM = maKM?.trim()
        val trimmedMethod = paymentMethod.trim()

        val localInvoiceId = "HD" + (System.currentTimeMillis() % 1000000)

        return try {
            val detailsRequest = itemsInCart.map { (product, qty) ->
                ApiInvoiceDetailRequest(
                    MaSP = product.MaSP.trim(),
                    SoLuong = qty,
                    DonGia = product.DonGia
                )
            }
            val request = ApiCreateInvoiceRequest(
                MaKH = trimmedKH,
                TongTien = total,
                MaKM = trimmedKM,
                GiamGia = discount,
                PhuongThucThanhToan = trimmedMethod,
                ChiTiet = detailsRequest
            )
            val response = apiService.placeOrder(request)
            val finalInvoiceId = response.maHD.trim()

            // Save locally
            val localHoaDon = HoaDon(
                MaHD = finalInvoiceId,
                NgayLap = System.currentTimeMillis(),
                TongTien = total,
                MaKH = trimmedKH,
                MaNV = "NV001",
                MaCH = trimmedCH,
                MaKM = trimmedKM,
                GiamGia = discount,
                PhuongThucThanhToan = trimmedMethod,
                TrangThai = "Chờ xử lý"
            )
            dao.insertHoaDon(localHoaDon)

            val localDetails = itemsInCart.map { (product, qty) ->
                ChiTietHoaDon(
                    MaHD = finalInvoiceId,
                    MaSP = product.MaSP.trim(),
                    SoLuong = qty,
                    DonGia = product.DonGia,
                    ThanhTien = product.DonGia * qty
                ).apply {
                    id = 0
                }
            }
            dao.insertAllChiTietHoaDon(localDetails)

            // Update user points
            val customer = dao.getKhachHang(trimmedKH)
            if (customer != null) {
                val updatedPoints = customer.DiemTichLuy + pointsEarned
                val updatedCustomer = customer.copy(DiemTichLuy = updatedPoints)
                dao.updateKhachHang(updatedCustomer)
                
                // remote update points
                try {
                    apiService.updateCustomer(
                        ApiUpdateRequest(
                            MaKH = updatedCustomer.MaKH,
                            HoTen = updatedCustomer.HoTen,
                            SoDienThoai = updatedCustomer.SoDienThoai,
                            DiaChi = updatedCustomer.DiaChi,
                            Email = updatedCustomer.Email,
                            DiemTichLuy = updatedCustomer.DiemTichLuy,
                            TrangThai = updatedCustomer.TrangThai
                        )
                    )
                } catch (e: Exception) {
                    // ignore
                }
            }

            if (trimmedKM != null) {
                dao.markVoucherAsUsed(trimmedKH, trimmedKM)
            }

            Pair(true, finalInvoiceId)

        } catch (e: Exception) {
            android.util.Log.e("GreenMart", "Place order remote API failed: ", e)
            Pair(false, e.message ?: "Mất kết nối mạng hoặc lỗi cơ sở dữ liệu server.")
        }
    }
}

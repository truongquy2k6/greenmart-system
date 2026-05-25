package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GreenMartDao {

    // --- KhachHang (Customer) ---
    @Query("SELECT * FROM KhachHang WHERE MaKH = :maKH LIMIT 1")
    fun getKhachHangFlow(maKH: String): Flow<KhachHang?>

    @Query("SELECT * FROM KhachHang WHERE MaKH = :maKH LIMIT 1")
    suspend fun getKhachHang(maKH: String): KhachHang?

    @Query("SELECT * FROM KhachHang WHERE SoDienThoai = :phone LIMIT 1")
    suspend fun getKhachHangByPhone(phone: String): KhachHang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhachHang(khachHang: KhachHang)

    @Update
    suspend fun updateKhachHang(khachHang: KhachHang)

    // --- LoaiSanPham (Categories) ---
    @Query("SELECT * FROM LoaiSanPham")
    fun getAllLoaiSanPhamFlow(): Flow<List<LoaiSanPham>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoaiSanPham(loạiList: List<LoaiSanPham>)

    // --- SanPham (Products) ---
    @Query("SELECT * FROM SanPham WHERE TrangThai = 'Đang kinh doanh'")
    fun getActiveProductsFlow(): Flow<List<SanPham>>

    @Query("SELECT * FROM SanPham WHERE MaLoai = :maLoai AND TrangThai = 'Đang kinh doanh'")
    fun getProductsByCategoryFlow(maLoai: String): Flow<List<SanPham>>

    @Query("SELECT * FROM SanPham WHERE MaSP = :maSP LIMIT 1")
    suspend fun getProductById(maSP: String): SanPham?

    @Query("SELECT * FROM SanPham WHERE TenSP LIKE '%' || :query || '%' AND TrangThai = 'Đang kinh doanh'")
    fun searchProductsFlow(query: String): Flow<List<SanPham>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSanPham(sanPhamList: List<SanPham>)

    // --- CuaHang (Stores) ---
    @Query("SELECT * FROM CuaHang")
    fun getAllCuaHangFlow(): Flow<List<CuaHang>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCuaHang(cuaHangList: List<CuaHang>)

    // --- KhuyenMai (Vouchers) ---
    @Query("SELECT * FROM KhuyenMai WHERE TrangThai = 'Đang áp dụng'")
    fun getActiveKhuyenMaiFlow(): Flow<List<KhuyenMai>>

    @Query("SELECT * FROM KhuyenMai WHERE MaKM = :maKM LIMIT 1")
    suspend fun getKhuyenMaiById(maKM: String): KhuyenMai?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKhuyenMai(khuyenMaiList: List<KhuyenMai>)

    // --- VoucherKhachHang (Claimed Vouchers) ---
    @Query("""
        SELECT km.* FROM KhuyenMai km 
        INNER JOIN VoucherKhachHang vkh ON km.MaKM = vkh.MaKM 
        WHERE vkh.MaKH = :maKH AND vkh.DaDung = 0
    """)
    fun getMyUnusedVouchersFlow(maKH: String): Flow<List<KhuyenMai>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucherKhachHang(voucher: VoucherKhachHang)

    @Query("UPDATE VoucherKhachHang SET DaDung = 1 WHERE MaKH = :maKH AND MaKM = :maKM")
    suspend fun markVoucherAsUsed(maKH: String, maKM: String)

    // --- HoaDon (Invoices) ---
    @Query("SELECT * FROM HoaDon WHERE MaKH = :maKH ORDER BY NgayLap DESC")
    fun getMyInvoicesFlow(maKH: String): Flow<List<HoaDon>>

    @Query("SELECT * FROM HoaDon WHERE MaHD = :maHD LIMIT 1")
    suspend fun getInvoiceById(maHD: String): HoaDon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoaDon(hoaDon: HoaDon)

    // --- ChiTietHoaDon (Invoice Details) ---
    @Query("SELECT * FROM ChiTietHoaDon WHERE MaHD = :maHD")
    fun getInvoiceDetailsFlow(maHD: String): Flow<List<ChiTietHoaDon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChiTietHoaDon(details: List<ChiTietHoaDon>)

    // --- CauHinh (Configurations) ---
    @Query("SELECT * FROM CauHinh WHERE MaCH = :maCH LIMIT 1")
    suspend fun getCauHinhByStore(maCH: String): CauHinh?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCauHinh(cauHinhList: List<CauHinh>)

    // --- QuaDongTichDiem (Redeemable Gifts / Rewards) ---
    @Query("SELECT * FROM QuaDongTichDiem")
    fun getAllGiftsFlow(): Flow<List<QuaDongTichDiem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGifts(giftsList: List<QuaDongTichDiem>)

    // --- LichSuDoiQua (Gift Redemption History) ---
    @Query("SELECT * FROM LichSuDoiQua WHERE MaKH = :maKH ORDER BY NgayDoi DESC")
    fun getMyRedemptionHistoryFlow(maKH: String): Flow<List<LichSuDoiQua>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLichSuDoiQua(lichSu: LichSuDoiQua)
}

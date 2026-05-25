package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        LoaiSanPham::class,
        SanPham::class,
        KhachHang::class,
        CuaHang::class,
        KhuyenMai::class,
        VoucherKhachHang::class,
        HoaDon::class,
        ChiTietHoaDon::class,
        CauHinh::class,
        QuaDongTichDiem::class,
        LichSuDoiQua::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun greenMartDao(): GreenMartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "QuanLyHeThongGreenMart_db"
                ).addCallback(AppDatabaseCallback(scope))
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                var database: AppDatabase? = null
                for (i in 1..50) {
                    database = INSTANCE
                    if (database != null) break
                    kotlinx.coroutines.delay(100)
                }
                database?.let {
                    populateDatabase(it.greenMartDao())
                }
            }
        }

        private suspend fun populateDatabase(dao: GreenMartDao) {
            // Seed Customer Profile (Default customer to login as with 150 points)
            dao.insertKhachHang(
                KhachHang(
                    MaKH = "KH001",
                    HoTen = "Trần Huy Hoàng",
                    SoDienThoai = "0123456789",
                    DiaChi = "123 Đường Cầu Giấy, Hà Nội",
                    Email = "truongquy2k6@gmail.com",
                    DiemTichLuy = 320, // High points for demonstrating loyalty gift redemption
                    NgayTao = System.currentTimeMillis() - 30 * 24 * 3600 * 1000L, // Join 30 days ago
                    TrangThai = "Hoạt động"
                )
            )

            // Seed Categories (LoaiSanPham)
            val categories = listOf(
                LoaiSanPham("L01", "Rau Củ Hữu Cơ"),
                LoaiSanPham("L02", "Trái Cây Tươi"),
                LoaiSanPham("L03", "Thịt & Thủy Sản Sạch"),
                LoaiSanPham("L04", "Bơ Sữa & Trứng"),
                LoaiSanPham("L05", "Vật Phẩm Gia Đình Green")
            )
            dao.insertAllLoaiSanPham(categories)

            // Seed Products (SanPham) - Modern visual descriptions and prices in VND
            val products = listOf(
                // Rau Củ Hữu Cơ
                SanPham("SP001", "Xà Lách Mỡ Đà Lạt Mỹ", 25000.0, "Gói 300g", "https://images.unsplash.com/photo-1556801712-76c8eb07bbc9?w=300", "Xà lách mỡ trồng organic tại nông trại Đà Lạt, giòn ngọt, giàu khoáng chất thích hợp làm salad.", "NCC01", "L01", "Đang kinh doanh", System.currentTimeMillis()),
                SanPham("SP002", "Cà Chua Bi Cherry Đỏ", 45000.0, "Hộp 500g", "https://images.unsplash.com/photo-1592841200221-a6898f307baa?w=300", "Cà chua bi giống Hà Lan ngọt lịm thơm ngon, hàm lượng vitamin C cực cao, ăn trực tiếp rất bổ mát.", "NCC01", "L01", "Đang kinh doanh", System.currentTimeMillis()),
                SanPham("SP003", "Bông Cải Xanh VietGAP", 35000.0, "Cái 500g", "https://images.unsplash.com/photo-1584270354949-c26b0d5b4a0c?w=300", "Bông cải xanh mướt, hoa đều, giòn sần sật, giàu chất chống oxy hóa hỗ trợ tim mạch.", "NCC01", "L01", "Đang kinh doanh", System.currentTimeMillis()),

                // Trái Cây Tươi
                SanPham("SP004", "Táo Fuji Hữu Cơ Nhập Khẩu", 89000.0, "Kg", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=300", "Táo Fuji giòn ngọt đậm đà, da láng mọng nước, nhập khẩu tiêu chuẩn organic nghiêm ngặt.", "NCC02", "L02", "Đang kinh doanh", System.currentTimeMillis()),
                SanPham("SP005", "Bơ Sáp Đắk Lắk Loại 1", 55000.0, "Kg", "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=300", "Bơ sáp béo ngậy, cơm vàng dẻo mịn. Hoàn hảo để chế biến các món sinh tố bổ dưỡng hoặc salad bổ mát.", "NCC02", "L02", "Đang kinh doanh", System.currentTimeMillis()),
                SanPham("SP006", "Cam Sành Hàm Yên Mọng Nước", 42000.0, "Kg", "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=300", "Cam sành giàu vitamin C giúp tăng sức đề kháng cho cơ thể người dùng trong mùa hè nắng nóng.", "NCC02", "L02", "Đang kinh doanh", System.currentTimeMillis()),

                // Thịt & Thủy Sản Sạch
                SanPham("SP007", "Thịt Ba Chỉ Heo Thảo Mộc", 165000.0, "Khay 500g", "https://images.unsplash.com/photo-1602470520998-f4a511798504?w=300", "Thịt heo sạch nuôi bằng thảo mộc, thơm bùi, không chứa hormone tăng trưởng, bảo đảm vệ sinh.", "NCC03", "L03", "Đang kinh doanh", System.currentTimeMillis()),
                SanPham("SP008", "Ức Gà Thảo Dược Phi Lê", 75000.0, "Khay 500g", "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=300", "Phi lê ức gà nạc mềm mại, thực phẩm hoàn hảo thiết yếu cho chế độ ăn uống lành mạnh eat-clean.", "NCC03", "L03", "Đang kinh doanh", System.currentTimeMillis()),

                // Bơ Sữa & Trứng
                SanPham("SP009", "Sữa Tươi Thanh Trùng DalatMilk", 38000.0, "Chai 950ml", "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=300", "Sữa tươi sạch thanh trùng nguyên chất giữ trọn vị ngon tinh khiết từ cao nguyên Đà Lạt mộng mơ.", "NCC04", "L04", "Đang kinh doanh", System.currentTimeMillis()),
                SanPham("SP010", "Trứng Gà Omega-3 Organic", 34000.0, "Hộp 10 quả", "https://images.unsplash.com/photo-1516448620398-c5f44bf9f441?w=300", "Trứng gà nuôi thảo dại chứa hàm lượng Omega-3 vượt trội, lòng đỏ to đậm thơm bùi tuyệt hảo.", "NCC04", "L04", "Đang kinh doanh", System.currentTimeMillis()),

                // Vật Phẩm Gia Đình Green
                SanPham("SP011", "Nước Lau Sàn Bồ Hòn Quế Vy", 68000.0, "Chai 1L", "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=300", "Thành phần 100% sinh học bồ hòn lên men và tinh dầu quế đuổi côn trùng hiệu quả, cực an toàn cho trẻ nhỏ.", "NCC05", "L05", "Đang kinh doanh", System.currentTimeMillis()),
                SanPham("SP012", "Túi Sinh Học Tự Hủy GreenMart", 32000.0, "Cuộn 3 size", "https://images.unsplash.com/photo-1595079676339-1534801ad6cf?w=300", "Làm từ tinh bột ngô tự nhiên, phân hủy hoàn toàn sau 6 tháng trong môi trường để phòng chống ô nhiễm rác thải nhựa.", "NCC05", "L05", "Đang kinh doanh", System.currentTimeMillis())
            )
            dao.insertAllSanPham(products)

            // Seed Stores (CuaHang) for Map stores
            val stores = listOf(
                CuaHang(
                    MaCH = "CH001",
                    TenCH = "GreenMart Cầu Giấy",
                    DiaChi = "12 Đường Xuân Thủy, Dịch Vọng, Cầu Giấy, Hà Nội",
                    SoDienThoai = "0243123456",
                    Email = "caugiay@greenmart.vn",
                    Latitude = 21.0365,
                    Longitude = 105.7832
                ),
                CuaHang(
                    MaCH = "CH002",
                    TenCH = "GreenMart Hoàn Kiếm",
                    DiaChi = "45 Lý Thái Tổ, Tràng Tiền, Hoàn Kiếm, Hà Nội",
                    SoDienThoai = "0243987654",
                    Email = "hoankiem@greenmart.vn",
                    Latitude = 21.0278,
                    Longitude = 105.8560
                ),
                CuaHang(
                    MaCH = "CH003",
                    TenCH = "GreenMart Ba Đình",
                    DiaChi = "202 Đội Cấn, Liễu Giai, Ba Đình, Hà Nội",
                    SoDienThoai = "0243555888",
                    Email = "badinh@greenmart.vn",
                    Latitude = 21.0345,
                    Longitude = 105.8188
                ),
                CuaHang(
                    MaCH = "CH004",
                    TenCH = "GreenMart Đống Đa",
                    DiaChi = "79 Chùa Bộc, Trung Liệt, Đống Đa, Hà Nội",
                    SoDienThoai = "0243666999",
                    Email = "dongda@greenmart.vn",
                    Latitude = 21.0089,
                    Longitude = 105.8285
                )
            )
            dao.insertAllCuaHang(stores)

            // Seed Bank Configurations (CauHinh) corresponding to stores
            val configs = listOf(
                CauHinh("CH001", "Vietcombank", "1012345678", "GREENMART CAU GIAY"),
                CauHinh("CH002", "MBBank", "999888777123", "GREENMART HOAN KIEM"),
                CauHinh("CH003", "Techcombank", "190333222111", "GREENMART BA DINH"),
                CauHinh("CH004", "VietinBank", "103874920194", "GREENMART DONG DA")
            )
            dao.insertAllCauHinh(configs)

            // Seed Vouchers (KhuyenMai)
            val vouchers = listOf(
                KhuyenMai(
                    MaKM = "KM01",
                    TenKM = "Ưu Đãi Rau Xanh",
                    MoTa = "Giảm trực tiếp 10,000 VND cho đơn hàng rau củ quả tươi sạch từ 100k.",
                    LoaiKM = "Giảm theo số tiền",
                    GiaTri = 10000.0,
                    NgayBatDau = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L,
                    NgayKetThuc = System.currentTimeMillis() + 14 * 24 * 3600 * 1000L,
                    DieuKien = 100000.0,
                    TrangThai = "Đang áp dụng"
                ),
                KhuyenMai(
                    MaKM = "KM02",
                    TenKM = "Siêu Voucher 15%",
                    MoTa = "Mã mừng thành viên mới giảm 15% tổng giá trị đơn hàng, giảm tối đa 50k.",
                    LoaiKM = "Giảm theo %",
                    GiaTri = 15.0,
                    NgayBatDau = System.currentTimeMillis() - 10 * 24 * 3600 * 1000L,
                    NgayKetThuc = System.currentTimeMillis() + 30 * 24 * 3600 * 1000L,
                    DieuKien = 150000.0,
                    TrangThai = "Đang áp dụng"
                ),
                KhuyenMai(
                    MaKM = "KM03",
                    TenKM = "GreenMart Day 50k",
                    MoTa = "Đại hội mua sắm giảm ngày 50,000 VND cho hóa đơn bách hóa tươi sạch trên 350k.",
                    LoaiKM = "Giảm theo số tiền",
                    GiaTri = 50000.0,
                    NgayBatDau = System.currentTimeMillis() - 1 * 24 * 3600 * 1000L,
                    NgayKetThuc = System.currentTimeMillis() + 5 * 24 * 3600 * 1000L,
                    DieuKien = 350000.0,
                    TrangThai = "Đang áp dụng"
                )
            )
            dao.insertAllKhuyenMai(vouchers)

            // Claim some default vouchers for the customer to use immediately
            dao.insertVoucherKhachHang(VoucherKhachHang("KH001", "KM01", false))
            dao.insertVoucherKhachHang(VoucherKhachHang("KH001", "KM02", false))

            // Seed Redeemable Loyalty Gifts (QuaDongTichDiem)
            val gifts = listOf(
                QuaDongTichDiem("Q01", "Ly Sứ Cao Cấp Green Earth", 50, "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=300", 25),
                QuaDongTichDiem("Q02", "Bình Nước Thủy Tinh Giữ Nhiệt Bamboo", 100, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=300", 15),
                QuaDongTichDiem("Q03", "Nón Bảo Hiểm GreenMart An Toàn", 180, "https://images.unsplash.com/photo-1599819811279-d5ad9cccf838?w=300", 10),
                QuaDongTichDiem("Q04", "Bộ Hộp Thủy Tinh Đựng Thực Phẩm Bio-Lock", 250, "https://images.unsplash.com/photo-1532634922-8fe0b757fb13?w=300", 8),
                QuaDongTichDiem("Q05", "Voucher Mua Sắm Bất Kỳ Trị Giá 100,000 VND", 300, "https://images.unsplash.com/photo-1589758438368-0ad531db3366?w=300", 99)
            )
            dao.insertAllGifts(gifts)
        }
    }
}

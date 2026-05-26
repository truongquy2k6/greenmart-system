package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LoaiSanPham")
data class LoaiSanPham(
    @PrimaryKey val MaLoai: String,
    val TenLoai: String
)

@Entity(tableName = "SanPham")
data class SanPham(
    @PrimaryKey val MaSP: String,
    val TenSP: String,
    val DonGia: Double,
    val DonViTinh: String,
    val HinhAnh: String,
    val MoTa: String,
    val MaNCC: String,
    val MaLoai: String,
    val TrangThai: String,
    val NgayTao: Long
)

@Entity(tableName = "KhachHang")
data class KhachHang(
    @PrimaryKey val MaKH: String,
    val HoTen: String,
    val SoDienThoai: String = "",
    val DiaChi: String = "",
    val Email: String = "",
    val DiemTichLuy: Int = 0,
    val NgayTao: Long = 0L,
    val TrangThai: String = "Hoạt động",
    val MatKhau: String = "123456"
)

@Entity(tableName = "CuaHang")
data class CuaHang(
    @PrimaryKey val MaCH: String,
    val TenCH: String,
    val DiaChi: String,
    val SoDienThoai: String,
    val Email: String,
    val Latitude: Double = 21.0285, // Preloaded location markers for nearby stores
    val Longitude: Double = 105.8542
)

@Entity(tableName = "KhuyenMai")
data class KhuyenMai(
    @PrimaryKey val MaKM: String,
    val TenKM: String,
    val MoTa: String,
    val LoaiKM: String, // "Giảm theo số tiền" or "Giảm theo %"
    val GiaTri: Double,
    val NgayBatDau: Long,
    val NgayKetThuc: Long,
    val DieuKien: Double, // Minimum spent condition
    val TrangThai: String
)

@Entity(tableName = "VoucherKhachHang", primaryKeys = ["MaKH", "MaKM"])
data class VoucherKhachHang(
    val MaKH: String,
    val MaKM: String,
    val DaDung: Boolean = false,
    val NgayNhan: Long = System.currentTimeMillis()
)

@Entity(tableName = "HoaDon")
data class HoaDon(
    @PrimaryKey val MaHD: String,
    val NgayLap: Long,
    val TongTien: Double,
    val MaKH: String,
    val MaCH: String,
    val MaKM: String?,
    val GiamGia: Double,
    val PhuongThucThanhToan: String, // "Thẻ" or "Chuyển khoản" or "Tiền mặt"
    val TrangThai: String // "Đã hủy" or "Đã thanh toán" or "Chờ xử lý"
)

@Entity(tableName = "ChiTietHoaDon", primaryKeys = ["MaHD", "MaSP"])
data class ChiTietHoaDon(
    val MaHD: String,
    val MaSP: String,
    val SoLuong: Int,
    val DonGia: Double,
    val ThanhTien: Double
)

@Entity(tableName = "CauHinh")
data class CauHinh(
    @PrimaryKey val MaCH: String,
    val BankId: String,
    val AccountNo: String,
    val AccountName: String
)

@Entity(tableName = "QuaDongTichDiem")
data class QuaDongTichDiem(
    @PrimaryKey val MaQua: String,
    val TenQua: String,
    val DiemCanThiet: Int,
    val HinhAnh: String,
    val SoLuongCon: Int
)

@Entity(tableName = "LichSuDoiQua")
data class LichSuDoiQua(
    @PrimaryKey val MaLichSu: String,
    val MaKH: String,
    val MaQua: String,
    val NgayDoi: Long,
    val SoDiemKhauTru: Int
)

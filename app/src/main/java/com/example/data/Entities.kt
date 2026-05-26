package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LoaiSanPham(
    @PrimaryKey val MaLoai: String,
    val TenLoai: String
)

@Entity
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

@Entity
data class KhachHang(
    @PrimaryKey val MaKH: String = "",
    val HoTen: String = "",
    val SoDienThoai: String = "",
    val DiaChi: String = "",
    val Email: String = "",
    val DiemTichLuy: Int = 0,
    val NgayTao: Long = 0L,
    val TrangThai: String = "",
    val MatKhau: String = ""
)

@Entity
data class CuaHang(
    @PrimaryKey val MaCH: String,
    val TenCH: String,
    val DiaChi: String,
    val SoDienThoai: String,
    val Email: String,
    val Latitude: Double,
    val Longitude: Double
)

@Entity
data class KhuyenMai(
    @PrimaryKey val MaKM: String,
    val TenKM: String,
    val MoTa: String,
    val LoaiKM: String,
    val GiaTri: Double,
    val NgayBatDau: Long,
    val NgayKetThuc: Long,
    val DieuKien: Double,
    val TrangThai: String
)

@Entity
data class VoucherKhachHang(
    val MaKH: String,
    val MaKM: String,
    val DaDung: Boolean
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}

@Entity
data class HoaDon(
    @PrimaryKey val MaHD: String,
    val NgayLap: Long,
    val TongTien: Double,
    val MaKH: String,
    val MaNV: String,
    val MaCH: String,
    val MaKM: String?,
    val GiamGia: Double,
    val PhuongThucThanhToan: String,
    val TrangThai: String
)

@Entity
data class ChiTietHoaDon(
    val MaHD: String,
    val MaSP: String,
    val SoLuong: Int,
    val DonGia: Double,
    val ThanhTien: Double
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}

@Entity
data class CauHinh(
    @PrimaryKey val MaCH: String,
    val BankId: String,
    val AccountNo: String,
    val AccountName: String
)

@Entity
data class QuaDongTichDiem(
    @PrimaryKey val MaQua: String,
    val TenQua: String,
    val DiemCanThiet: Int,
    val HinhAnh: String,
    val SoLuongCon: Int
)

@Entity
data class LichSuDoiQua(
    @PrimaryKey val MaLichSu: String,
    val MaKH: String,
    val MaQua: String,
    val NgayDoi: Long,
    val SoDiemKhauTru: Int,
    val TrangThai: String
)

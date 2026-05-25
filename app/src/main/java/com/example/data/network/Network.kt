package com.example.data.network

import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

// ---------------- REQUEST/RESPONSE DTOS ----------------
data class ApiRegisterRequest(
    val HoTen: String,
    val SoDienThoai: String,
    val DiaChi: String?,
    val Email: String?
)

data class ApiRegisterResponse(
    val maKH: String,
    val message: String
)

data class ApiUpdateRequest(
    val MaKH: String,
    val HoTen: String,
    val SoDienThoai: String,
    val DiaChi: String?,
    val Email: String?,
    val DiemTichLuy: Int,
    val TrangThai: String?
)

data class ApiCreateInvoiceRequest(
    val MaKH: String,
    val TongTien: Double,
    val MaKM: String?,
    val GiamGia: Double,
    val PhuongThucThanhToan: String?,
    val ChiTiet: List<ApiInvoiceDetailRequest>
)

data class ApiInvoiceDetailRequest(
    val MaSP: String,
    val SoLuong: Int,
    val DonGia: Double
)

data class ApiCreateInvoiceResponse(
    val maHD: String,
    val message: String
)

data class ApiResponseWrapper(
    val message: String
)

// ---------------- RETROFIT SERVICE INTERFACE ----------------
interface GreenMartApiService {
    @GET("api/sanpham")
    suspend fun getProducts(
        @Query("kw") kw: String? = null,
        @Query("maLoai") maLoai: String? = null
    ): List<SanPham>

    @GET("api/loaisanpham")
    suspend fun getCategories(): List<LoaiSanPham>

    @GET("api/cuahang")
    suspend fun getStores(): List<CuaHang>

    @GET("api/khuyenmai")
    suspend fun getVouchers(): List<KhuyenMai>

    @GET("api/khachhang/tim")
    suspend fun getCustomerByPhone(
        @Query("sdt") sdt: String
    ): KhachHang

    @POST("api/khachhang")
    suspend fun registerCustomer(
        @Body request: ApiRegisterRequest
    ): ApiRegisterResponse

    @PUT("api/khachhang")
    suspend fun updateCustomer(
        @Body request: ApiUpdateRequest
    ): ApiResponseWrapper

    @POST("api/hoadon")
    suspend fun placeOrder(
        @Body request: ApiCreateInvoiceRequest
    ): ApiCreateInvoiceResponse

    @GET("api/khachhang/{maKH}/hoadon")
    suspend fun getInvoices(
        @Path("maKH") maKH: String
    ): List<HoaDon>

    @GET("api/hoadon/{maHD}/chitiet")
    suspend fun getInvoiceDetails(
        @Path("maHD") maHD: String
    ): List<ChiTietHoaDon>
}

// ---------------- RETROFIT CLIENT SINGLETON ----------------
object RetrofitClient {
    private const val BASE_URL = "https://chatty-dogs-bark.localtunnel.me/" // Emulator loopback to host localhost:5070

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: GreenMartApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GreenMartApiService::class.java)
    }
}

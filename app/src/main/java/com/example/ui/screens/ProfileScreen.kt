package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HoaDon
import com.example.data.KhachHang
import com.example.viewmodel.GreenMartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: GreenMartViewModel,
    customer: KhachHang?,
    pastOrders: List<HoaDon>
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var selectedOrderForDetail by remember { mutableStateOf<HoaDon?>(null) }

    if (customer == null) {
        var isLoginMode by remember { mutableStateOf(true) }
        var phoneInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        
        var fullNameInput by remember { mutableStateOf("") }
        var emailInput by remember { mutableStateOf("") }
        var addressInput by remember { mutableStateOf("") }
        
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGrayBackground)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo and title
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(ForestGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Thành Viên GreenMart",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                    Text(
                        text = "Hệ thống Thực Phẩm Sạch & Hữu Cơ",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(22.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { isLoginMode = true }
                                .background(
                                    if (isLoginMode) ForestGreen else Color.Transparent,
                                    shape = RoundedCornerShape(22.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Đăng Nhập",
                                color = if (isLoginMode) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { isLoginMode = false }
                                .background(
                                    if (!isLoginMode) ForestGreen else Color.Transparent,
                                    shape = RoundedCornerShape(22.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Đăng Ký Mới",
                                color = if (!isLoginMode) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoginMode) {
                        // Phone number field
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Số điện thoại") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                focusedLabelColor = ForestGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Mật khẩu") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                focusedLabelColor = ForestGreen
                            ),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.login(phoneInput, passwordInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ĐĂNG NHẬP", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "*Tài khoản thử nghiệm mặc định: 0123456789 / Mật khẩu: 123456",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Register Mode Fields
                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it },
                            label = { Text("Họ và tên khách hàng") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                focusedLabelColor = ForestGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Số điện thoại") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                focusedLabelColor = ForestGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Địa chỉ Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                focusedLabelColor = ForestGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Địa chỉ giao hàng mặc định") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                focusedLabelColor = ForestGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Mật khẩu thiết lập") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                focusedLabelColor = ForestGreen
                            ),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.register(
                                        fullNameInput,
                                        phoneInput,
                                        emailInput,
                                        addressInput,
                                        passwordInput
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ĐĂNG KÝ NGAY (+50 Điểm 🎁)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrayBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Personal details overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(ForestGreen, shape = RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (customer?.HoTen?.split(" ")?.lastOrNull()?.take(1) ?: "K"),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = customer?.HoTen ?: "Khách hàng GreenMart",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepText
                            )
                            val tier = if ((customer?.DiemTichLuy ?: 0) >= 300) "Hạng Vàng" else if ((customer?.DiemTichLuy ?: 0) >= 150) "Hạng Bạc" else "Hạng Thường"
                            Text(
                                text = "Mã KH: ${customer?.MaKH ?: "KH001"} • $tier 🏅",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact listings
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Số điện thoại: ${customer?.SoDienThoai ?: "Chưa cập nhật"}", fontSize = 13.sp, color = Color.DarkGray)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Địa chỉ email: ${customer?.Email ?: "Chưa cập nhật"}", fontSize = 13.sp, color = Color.DarkGray)
                    }

                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Địa chỉ nhận giao hàng mặc định: ${customer?.DiaChi ?: "Chưa cập nhật"}",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Đăng xuất", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đăng Xuất Tài Khoản", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 2. Historical Invoice header
        item {
            Text(
                text = "Lịch Sử Đơn Hàng Mua Sắm (${pastOrders.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepText
            )
        }

        if (pastOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Bạn chưa đặt chân cuộc hành trình mua sắm nào cùng GreenMart.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(pastOrders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOrderForDetail = order },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Mã đơn: ${order.MaHD}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepText
                            )
                            
                            Box(
                                modifier = Modifier
                                    .background(ForestGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = order.TrangThai,
                                    color = ForestGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Thời gian: ${dateFormatter.format(Date(order.NgayLap))}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Thanh toán qua: ${order.PhuongThucThanhToan}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val computedPointsEarned = (order.TongTien / 20000.0).toInt()
                            Text(
                                text = "🎁 +$computedPointsEarned điểm đã tích lũy",
                                fontSize = 11.sp,
                                color = OrganicAmber,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Tổng tiền: ${String.format("%,.0f", order.TongTien)}đ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }
        }
    }

    // Invoice detailed view receipt dialog
    selectedOrderForDetail?.let { header ->
        val detailsStateVal = viewModel.getInvoiceDetails(header.MaHD).collectAsState(initial = emptyList())
        val detailProducts by viewModel.allProductsStore.collectAsState()

        AlertDialog(
            onDismissRequest = { selectedOrderForDetail = null },
            title = {
                Text(
                    text = "BIÊN LAI ĐƠN HÀNG GREENMART",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Mã Đơn Hàng: ${header.MaHD}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text(text = "Thời gian: ${dateFormatter.format(Date(header.NgayLap))}", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "Cửa hàng: ${header.MaCH}", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "Phương thức: ${header.PhuongThucThanhToan}", fontSize = 11.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Sản phẩm đã chọn:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepText)
                    Spacer(modifier = Modifier.height(6.dp))

                    for (detail in detailsStateVal.value) {
                        val matchedProduct = detailProducts.find { it.MaSP == detail.MaSP }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${matchedProduct?.TenSP ?: "Sản phẩm sạch"} x${detail.SoLuong}",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${String.format("%,.0f", detail.ThanhTien)}đ",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng giảm giá áp mã:", fontSize = 12.sp, color = Color.Gray)
                        Text("-${String.format("%,.0f", header.GiamGia)}đ", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hóa đơn đã trả:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text("${String.format("%,.0f", header.TongTien)}đ", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = ForestGreen)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftCream, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Cảm ơn quý khách đã tin cậy chọn mua nông sản sạch ủng hộ những người nông dân Việt Nam chân chất!",
                            fontSize = 11.sp,
                            color = ForestGreen,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedOrderForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = SoftCream,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

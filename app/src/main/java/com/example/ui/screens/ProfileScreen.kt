package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
        val scrollState = rememberScrollState()
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DeepText,
            unfocusedTextColor = DeepText,
            focusedBorderColor = ForestGreen,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = ForestGreen,
            unfocusedLabelColor = Color.Gray,
            focusedLeadingIconColor = ForestGreen,
            unfocusedLeadingIconColor = Color.Gray
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGrayBackground)
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
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
                            colors = textFieldColors,
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
                            colors = textFieldColors,
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
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Số điện thoại") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Địa chỉ Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Địa chỉ giao hàng mặc định") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Mật khẩu thiết lập") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestGreen) },
                            singleLine = true,
                            colors = textFieldColors,
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

    var showQrDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPastOrdersDialog by remember { mutableStateOf(false) }
    var selectedPolicyTitle by remember { mutableStateOf<String?>(null) }
    var selectedPolicyBody by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrayBackground),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Beautiful Point & Barcode Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ForestGreen.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = customer?.HoTen ?: "Khách Hàng GreenMart",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val points = customer?.DiemTichLuy ?: 0
                            val rank = if (points >= 300) "Hạng Vàng 🏅" else if (points >= 150) "Hạng Bạc 🥈" else "Hạng Thường 🥉"
                            Box(
                                modifier = Modifier
                                    .background(ForestGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rank.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${customer?.DiemTichLuy ?: 0} điểm tích lũy",
                            fontSize = 12.sp,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BarcodeWidget(barcodeValue = customer?.SoDienThoai ?: "291322")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(80.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showQrDialog = true }
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Mã QR",
                            tint = ForestGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quét tích điểm",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }
        }

        // 2. BHX Point cards & badged shortcuts card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    BHXMenuItem(icon = Icons.Default.Notifications, title = "Thông báo", badgeText = "9+", onClick = {
                        viewModel.triggerToast("Bạn không có thông báo mới nào.")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Check, title = "Tiền dư", rightText = "0đ", onClick = {
                        viewModel.triggerToast("Tài khoản tiền dư của bạn đang là 0đ.")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Star, title = "Phiếu mua hàng", badgeText = "10", badgeColor = OrganicAmber, onClick = {
                        viewModel.triggerToast("Vui lòng mở tab 'Voucher Của Tôi' ở bên cạnh để sử dụng!")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Favorite, title = "Quà của tôi", badgeText = "1", badgeColor = OrganicAmber, onClick = {
                        viewModel.triggerToast("Vui lòng mở tab 'Quà Đổi Điểm' ở bên cạnh để nhận quà!")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Star, title = "Ưu đãi đặc biệt", badgeText = "2", badgeColor = OrganicAmber, onClick = {
                        viewModel.triggerToast("Xem danh sách Khuyến Mãi hot tại trang chủ GreenMart nhé!")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Star, title = "Tích điểm đổi quà", badgeText = "3", badgeColor = OrganicAmber, onClick = {
                        viewModel.triggerToast("Hãy mở tab 'Quà Đổi Điểm' để thực hiện đổi quà bằng điểm thưởng của bạn!")
                    })
                }
            }
        }

        // 3. Personal information section header
        item {
            Text(
                text = "Thông tin cá nhân",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepText,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        // 4. Personal Info Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    BHXMenuItem(icon = Icons.Default.Person, title = "Sửa thông tin cá nhân", onClick = {
                        showEditProfileDialog = true
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.LocationOn, title = "Địa chỉ nhận hàng", onClick = {
                        showEditProfileDialog = true
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.ReceiptLong, title = "Lịch sử đơn hàng mua sắm", rightText = "Đã mua ${pastOrders.size} đơn", onClick = {
                        showPastOrdersDialog = true
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Lock, title = "Thay đổi chính sách xử lý dữ liệu cá nhân", onClick = {
                        selectedPolicyTitle = "Chính sách xử lý dữ liệu cá nhân"
                        selectedPolicyBody = "GreenMart cam kết bảo vệ dữ liệu cá nhân của quý khách hàng tuyệt đối 100%. Thông tin về Số điện thoại, Email, Địa chỉ chỉ được sử dụng cho mục đích vận chuyển hàng hóa và tích lũy điểm thưởng thành viên."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.ExitToApp, title = "Đăng xuất tài khoản", onClick = {
                        viewModel.logout()
                    })
                }
            }
        }

        // 5. Customer support header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hỗ trợ khách hàng",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepText
                )
                Text(
                    text = "Phiên bản: 3.0.2.live(v17)",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // 6. Support actions card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    BHXMenuItem(icon = Icons.Default.Call, title = "Tư vấn: 1900.1908 (7:30 - 21:30)", onClick = {
                        viewModel.triggerToast("Đang kết nối tổng đài tư vấn: 1900.1908...")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Call, title = "Khiếu nại: 1800.1067 (7:30 - 21:30) Miễn phí", onClick = {
                        viewModel.triggerToast("Đang kết nối tổng đài khiếu nại: 1800.1067...")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Home, title = "Tìm kiếm cửa hàng", onClick = {
                        viewModel.triggerToast("Quý khách hãy chuyển sang tab Bản đồ cửa hàng ở phía dưới để tìm kiếm!")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.CreditCard, title = "Mua phiếu mua hàng", onClick = {
                        viewModel.triggerToast("Tính năng mua Phiếu mua hàng online sẽ sớm ra mắt!")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Refresh, title = "Cập nhật ứng dụng", onClick = {
                        viewModel.triggerToast("Ứng dụng đang chạy phiên bản mới nhất, không cần cập nhật!")
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Book, title = "Hướng dẫn mua hàng", onClick = {
                        selectedPolicyTitle = "Hướng dẫn mua hàng"
                        selectedPolicyBody = "1. Chọn sản phẩm sạch cần mua từ trang chủ hoặc danh mục.\n2. Chọn Cửa hàng soạn hàng gần nhất tại màn hình Giỏ hàng.\n3. Chọn hình thức thanh toán trực tuyến hoặc tiền mặt.\n4. Bấm đặt hàng và theo dõi tài xế giao rau sạch tới nhà!"
                    })
                }
            }
        }

        // 7. Policies Header
        item {
            Text(
                text = "Tất cả chính sách",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepText,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        // 8. Policies Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    BHXMenuItem(icon = Icons.Default.Home, title = "Giới thiệu công ty", onClick = {
                        selectedPolicyTitle = "Giới thiệu công ty"
                        selectedPolicyBody = "GreenMart là hệ thống siêu thị thực phẩm sạch, nông sản VietGAP chất lượng cao được thành lập năm 2026 với sứ mệnh mang đến sản phẩm an toàn, tươi sạch nhất từ nông trại Việt đến bữa cơm mỗi gia đình."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.CheckCircle, title = "Quy trình kiểm soát chất lượng", onClick = {
                        selectedPolicyTitle = "Quy trình kiểm soát chất lượng"
                        selectedPolicyBody = "100% rau củ quả tại GreenMart đều đạt tiêu chuẩn VietGAP hoặc Organic châu Âu. Sản phẩm được kiểm tra nồng độ thuốc bảo vệ thực vật nghiêm ngặt tại phòng lab của siêu thị trước khi bày bán lên kệ."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.List, title = "Quy chế hoạt động", onClick = {
                        selectedPolicyTitle = "Quy chế hoạt động"
                        selectedPolicyBody = "GreenMart hoạt động 24/7 trên cả hai nền tảng ứng dụng di động và hệ thống quản lý tại quầy nhằm đáp ứng nhu cầu giao hàng hỏa tốc trong 1 giờ cho mọi khách hàng."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Lock, title = "Chính sách xử lý dữ liệu cá nhân", onClick = {
                        selectedPolicyTitle = "Chính sách xử lý dữ liệu cá nhân"
                        selectedPolicyBody = "GreenMart cam kết bảo vệ dữ liệu cá nhân của quý khách hàng tuyệt đối 100%. Thông tin về Số điện thoại, Email, Địa chỉ chỉ được sử dụng cho mục đích vận chuyển hàng hóa và tích lũy điểm thưởng thành viên."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Refresh, title = "Chính sách đổi trả", onClick = {
                        selectedPolicyTitle = "Chính sách đổi trả"
                        selectedPolicyBody = "Bảo hành 1 đổi 1 lập tức trong vòng 24 giờ đối với tất cả các loại rau củ quả, thực phẩm tươi sống nếu quý khách hàng phát hiện sản phẩm bị dập nát, héo úa hoặc không đạt độ tươi ngon mong muốn."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.ShoppingCart, title = "Chính sách giao hàng", onClick = {
                        selectedPolicyTitle = "Chính sách giao hàng"
                        selectedPolicyBody = "Giao hàng siêu tốc miễn phí trong 1 giờ cho mọi đơn hàng có giá trị từ 300,000 VND trở lên. Đối với đơn hàng dưới 300,000 VND, phí vận chuyển đồng giá ưu đãi là 15,000 VND."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Person, title = "Chính sách khách hàng", onClick = {
                        selectedPolicyTitle = "Chính sách khách hàng"
                        selectedPolicyBody = "Khách hàng mua sắm được tích lũy 5% giá trị hóa đơn quy đổi thành điểm thưởng. Điểm thưởng có thể dùng để đổi quà tặng độc quyền hoặc khấu trừ trực tiếp khi thanh toán đơn hàng tiếp theo."
                    })
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    BHXMenuItem(icon = Icons.Default.Warning, title = "Cảnh báo giả mạo", onClick = {
                        selectedPolicyTitle = "Cảnh báo giả mạo"
                        selectedPolicyBody = "Hiện nay xuất hiện một số đầu số giả mạo GreenMart để lừa đảo thẻ cào. GreenMart chỉ liên hệ duy nhất qua hai tổng đài chính thức: Tư vấn 1900.1908 và Khiếu nại 1800.1067."
                    })
                }
            }
        }

        // 9. Corporate Info Footer Card
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Thông tin công ty",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "CÔNG TY CỔ PHẦN THƯƠNG MẠI GREENMART VIỆT NAM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Trụ sở chính: 128 Trần Quang Khải, Phường Tân Định, Quận 1, thành phố Hồ Chí Minh.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = "• Giấy chứng nhận đăng ký doanh nghiệp số: 0310471746 do Sở Kế hoạch và Đầu tư thành phố Hồ Chí Minh cấp lần đầu ngày 23/11/2010.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = "• Địa chỉ liên hệ: Tòa nhà GreenMart Office, Lô T2-1.2 Đường D1, Khu Công nghệ Cao, Phường Tăng Nhơn Phú B, thành phố Thủ Đức, Thành phố Hồ Chí Minh.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }
        }
        
        // 10. Safety Spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // QR Membership card dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text("Mã Thành Viên Tích Điểm", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(2.dp, ForestGreen, RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val size = this.size.width
                            drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(size * 0.25f, size * 0.25f))
                            drawRect(color = Color.White, topLeft = Offset(size * 0.05f, size * 0.05f), size = Size(size * 0.15f, size * 0.15f))
                            drawRect(color = Color.Black, topLeft = Offset(size * 0.08f, size * 0.08f), size = Size(size * 0.09f, size * 0.09f))

                            drawRect(color = Color.Black, topLeft = Offset(size * 0.75f, 0f), size = Size(size * 0.25f, size * 0.25f))
                            drawRect(color = Color.White, topLeft = Offset(size * 0.8f, size * 0.05f), size = Size(size * 0.15f, size * 0.15f))
                            drawRect(color = Color.Black, topLeft = Offset(size * 0.83f, size * 0.08f), size = Size(size * 0.09f, size * 0.09f))

                            drawRect(color = Color.Black, topLeft = Offset(0f, size * 0.75f), size = Size(size * 0.25f, size * 0.25f))
                            drawRect(color = Color.White, topLeft = Offset(size * 0.05f, size * 0.8f), size = Size(size * 0.15f, size * 0.15f))
                            drawRect(color = Color.Black, topLeft = Offset(size * 0.08f, size * 0.83f), size = Size(size * 0.09f, size * 0.09f))

                            for (x in 3..7) {
                                for (y in 3..7) {
                                    if ((x + y) % 2 == 0 || (x * y) % 3 == 0) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(size * (x * 0.11f), size * (y * 0.11f)),
                                            size = Size(size * 0.09f, size * 0.09f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mã KH: ${customer?.MaKH ?: "KH001"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Đưa mã này cho nhân viên thu ngân tại quầy GreenMart để tích lũy 5% điểm thưởng!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ĐÓNG MÃ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    // Edit Profile details dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(customer?.HoTen ?: "") }
        var editPhone by remember { mutableStateOf(customer?.SoDienThoai ?: "") }
        var editEmail by remember { mutableStateOf(customer?.Email ?: "") }
        var editAddress by remember { mutableStateOf(customer?.DiaChi ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Sửa Thông Tin Cá Nhân", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepText)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Họ và Tên") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen, focusedLabelColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Số điện thoại") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen, focusedLabelColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen, focusedLabelColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Địa chỉ") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen, focusedLabelColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEditProfileDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = {
                            viewModel.updateCustomerProfile(editName, editPhone, editEmail, editAddress)
                            showEditProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lưu", color = Color.White)
                    }
                }
            },
            containerColor = Color.White
        )
    }

    // Past Orders History list dialog
    if (showPastOrdersDialog) {
        AlertDialog(
            onDismissRequest = { showPastOrdersDialog = false },
            title = {
                Text(
                    text = "Lịch Sử Đơn Hàng Mua Sắm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    if (pastOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Bạn chưa đặt chân mua sắm nào cùng GreenMart.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pastOrders) { order ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            selectedOrderForDetail = order
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SoftCream),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(
                                                text = "Mã đơn: ${order.MaHD}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepText
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(ForestGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = order.TrangThai,
                                                    color = ForestGreen,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Thời gian: ${dateFormatter.format(Date(order.NgayLap))}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            val computedPointsEarned = (order.TongTien / 20000.0).toInt()
                                            Text(
                                                text = "🎁 +$computedPointsEarned điểm",
                                                fontSize = 11.sp,
                                                color = OrganicAmber,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${String.format("%,.0f", order.TongTien)}đ",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ForestGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPastOrdersDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ĐÓNG LỊCH SỬ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Policy Detail dialog
    if (selectedPolicyTitle != null && selectedPolicyBody != null) {
        AlertDialog(
            onDismissRequest = { 
                selectedPolicyTitle = null
                selectedPolicyBody = null
            },
            title = {
                Text(selectedPolicyTitle!!, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepText)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(selectedPolicyBody!!, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 20.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        selectedPolicyTitle = null
                        selectedPolicyBody = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ĐÃ ĐỌC & ĐỒNG Ý", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
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

@Composable
fun BarcodeWidget(barcodeValue: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.White)
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .height(42.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val barPattern = listOf(
                2, 1, 3, 1, 2, 4, 1, 2, 1, 3, 2, 1, 4, 1, 2, 2, 1, 3, 1, 2, 1, 4, 2, 1, 3, 2, 1, 4, 1, 2, 1, 3, 2, 1
            )
            barPattern.forEachIndexed { index, width ->
                Spacer(
                    modifier = Modifier
                        .width(width.dp)
                        .fillMaxHeight()
                        .background(if (index % 2 == 0) Color.Black else Color.Transparent)
                )
                Spacer(modifier = Modifier.width(1.dp))
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Đưa mã hoặc đọc số $barcodeValue để tích, sử dụng điểm",
            fontSize = 9.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BHXMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFEF4444),
    rightText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepText
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(badgeColor, shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (rightText != null) {
                Text(
                    text = rightText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepText,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

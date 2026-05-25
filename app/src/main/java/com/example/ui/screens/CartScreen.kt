package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.data.CauHinh
import com.example.data.CuaHang
import com.example.data.KhuyenMai
import com.example.data.SanPham
import com.example.viewmodel.CartSummary
import com.example.viewmodel.CheckoutResult
import com.example.viewmodel.GreenMartViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: GreenMartViewModel,
    cartItems: List<Pair<SanPham, Int>>,
    summary: CartSummary,
    myVouchers: List<KhuyenMai>,
    appliedVoucher: KhuyenMai?,
    stores: List<CuaHang>,
    selectedCheckoutStore: CuaHang?,
    onOrderSuccess: (String, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showVoucherBottomSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // Checkout configurations
    var selectedPaymentMethod by remember { mutableStateOf("Chuyển khoản QR") } // "Chuyển khoản QR" or "Thẻ ATM/Visa" or "Ví MoMo"
    var checkoutResultState by remember { mutableStateOf<CheckoutResult?>(null) }
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var storeBankConfig by remember { mutableStateOf<CauHinh?>(null) }

    // Fetch config on selected checkout store change
    LaunchedEffect(selectedCheckoutStore) {
        if (selectedCheckoutStore != null) {
            viewModel.loadPaymentConfigForSelectedStore()
        }
    }

    // Collect config
    val configState by viewModel.paymentConfig.collectAsState()
    LaunchedEffect(configState) {
        storeBankConfig = configState
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrayBackground)
    ) {
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = ForestGreen.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Giỏ hàng của bạn đang trống trơn!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Để kích thích tích điểm nâng hạng, hãy lấp đầy giỏ hàng bằng nhiều rau tươi, trái chín bổ dưỡng bạn nhé.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item Title list header
                    item {
                        Text(
                            text = "Danh sách sản phẩm mua sắm:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepText
                        )
                    }

                    // Loop items
                    items(cartItems) { (product, quantity) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product small photo
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    GreenMartImage(
                                        url = product.HinhAnh,
                                        contentDescription = product.TenSP,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Informational Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.TenSP,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Đơn vị: ${product.DonViTinh} | ĐG: ${String.format("%,.0f", product.DonGia)}đ",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${String.format("%,.0f", product.DonGia * quantity)} VND",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )
                                }

                                // Interactive counter adjust block
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.background(Color.LightGray.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                                ) {
                                    IconButton(
                                        onClick = { viewModel.subtractFromCart(product.MaSP) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Trừ", modifier = Modifier.size(16.dp), tint = ForestGreen)
                                    }
                                    Text(
                                        text = "$quantity",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.addToCart(product.MaSP) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Cộng", modifier = Modifier.size(16.dp), tint = ForestGreen)
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Remove trash bin action
                                IconButton(
                                    onClick = { viewModel.removeFromCart(product.MaSP) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    // 1. Selector segment for Pick-up / Delivery branch
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🏢 Chọn cửa hàng nhận & soạn hàng:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                stores.forEach { shop ->
                                    val checked = selectedCheckoutStore?.MaCH == shop.MaCH
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.setCheckoutStore(shop) }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = checked,
                                            onClick = { viewModel.setCheckoutStore(shop) },
                                            colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text(shop.TenCH, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            Text(shop.DiaChi, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Voucher selection input trigger card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showVoucherBottomSheet = true }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = ForestGreen)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (appliedVoucher != null) "Đã Áp Dụng: ${appliedVoucher.TenKM}" else "Chọn mã giảm giá / Voucher",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (appliedVoucher != null) ForestGreen else Color.DarkGray
                                        )
                                        Text(
                                            text = if (appliedVoucher != null) "Được khấu trừ thẳng -${String.format("%,.0f", summary.discount)}đ" else "Sở hữu ${myVouchers.size} mã có thể dùng",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (appliedVoucher != null) {
                                        TextButton(
                                            onClick = {
                                                viewModel.removeVoucher()
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Hủy", color = Color.Red, fontSize = 13.sp)
                                        }
                                    } else {
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Billing detailed summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Tổng số thanh toán:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepText)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tạm tính sản phẩm:", fontSize = 13.sp, color = Color.Gray)
                                    Text("${String.format("%,.0f", summary.subtotal)}đ", fontSize = 13.sp, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Khuyến mãi đã giảm:", fontSize = 13.sp, color = Color.Gray)
                                    Text("-${String.format("%,.0f", summary.discount)}đ", fontSize = 13.sp, color = Color.Red)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Phí chuyển phát (Giao tận nơi):", fontSize = 13.sp, color = Color.Gray)
                                    Text(
                                        text = if (summary.shippingFee == 0.0) "Miễn phí" else "${String.format("%,.0f", summary.shippingFee)}đ",
                                        fontSize = 13.sp,
                                        color = if (summary.shippingFee == 0.0) ForestGreen else Color.DarkGray,
                                        fontWeight = if (summary.shippingFee == 0.0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                
                                if (summary.subtotal < 300000.0) {
                                    Text(
                                        text = "⚡ Mua thêm ${String.format("%,.0f", 300000.0 - summary.subtotal)}đ để được freeship trọn đời",
                                        fontSize = 10.sp,
                                        color = OrganicAmber,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Thực Thanh Toán:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Text(
                                        text = "${String.format("%,.0f", summary.total)}đ",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Loyal reward points forecast box (Primary trigger context)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SoftCream, shape = RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = OrganicAmber)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tích lũy khủng: Bạn sẽ nhận thêm +${summary.pointsEarned} điểm Green!",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Solid bottom CTA payment panel
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tổng hóa đơn:", fontSize = 12.sp, color = Color.Gray)
                            Text("${String.format("%,.0f", summary.total)}đ", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ForestGreen)
                        }

                        Button(
                            onClick = { 
                                checkoutResultState = null
                                showCheckoutDialog = true 
                            },
                            modifier = Modifier
                                .height(46.dp)
                                .width(180.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(23.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thanh toán", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- Bottom Sheet for Voucher selection list ---
    if (showVoucherBottomSheet) {
        AlertDialog(
            onDismissRequest = { showVoucherBottomSheet = false },
            title = { Text("Chọn Mã Giảm Giá Khả Dụng", fontWeight = FontWeight.Bold, color = DeepText) },
            text = {
                if (myVouchers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text("Ví voucher trống. Hãy thu thập mã từ trang chủ!", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(myVouchers) { voucher ->
                            val currentSubtotal = summary.subtotal
                            val meetsCondition = currentSubtotal >= voucher.DieuKien

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = meetsCondition) {
                                        viewModel.applyVoucher(voucher)
                                        showVoucherBottomSheet = false
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (meetsCondition) Color.White else Color.LightGray.copy(alpha = 0.2f)
                                ),
                                border = if (meetsCondition) BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f)) else null
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = voucher.TenKM,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (meetsCondition) DeepText else Color.Gray
                                        )
                                        Text(
                                            text = voucher.MoTa,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "Đơn tố thiểu: ${String.format("%,.0f", voucher.DieuKien)}đ",
                                            fontSize = 10.sp,
                                            color = if (meetsCondition) ForestGreen else Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (!meetsCondition) {
                                        Text("Chưa đủ đ/k", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoucherBottomSheet = false }) {
                    Text("Hủy bỏ", color = Color.Gray)
                }
            },
            containerColor = SoftCream,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // --- Elegant Online Payment Gateway Simulation Dialog ---
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { if (checkoutResultState !is CheckoutResult.Loading) showCheckoutDialog = false },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("CỔNG THANH TOÁN ONLINE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepText)
                    IconButton(onClick = { if (checkoutResultState !is CheckoutResult.Loading) showCheckoutDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            text = {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (checkoutResultState == null) {
                        // Payment Method tab selectors
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            val tabs = listOf("Chuyển khoản QR", "Thẻ ATM/Visa", "Ví MoMo")
                            tabs.forEach { tab ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selectedPaymentMethod == tab) ForestGreen else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedPaymentMethod = tab }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab,
                                        color = if (selectedPaymentMethod == tab) Color.White else Color.DarkGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic visual display based on tab
                        when (selectedPaymentMethod) {
                            "Chuyển khoản QR" -> {
                                Text(
                                    text = "Quét mã VietQR chuyển khoản nhanh 24/7:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SoftCream),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("🏦 Ngân hàng: ${storeBankConfig?.BankId ?: "Vietcombank"}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepText)
                                        Text("👤 Tên tài khoản: ${storeBankConfig?.AccountName ?: "GREENMART CO."}", fontSize = 12.sp, color = Color.Gray)
                                        Text("💳 Số TK: ${storeBankConfig?.AccountNo ?: "1012345678"}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                                        Text("💵 Số tiền: ${String.format("%,.0f", summary.total)} VND", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                        Text("📝 Nội dung: GMPAY KH001", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Draw QR Code Simulator Graphic
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(Color.White)
                                        .border(1.dp, Color.LightGray)
                                        .padding(10.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Draw a simulated beautiful green QR code pattern
                                        val qrSize = size.width
                                        val block = qrSize / 8f
                                        
                                        // Outer boundaries
                                        drawRect(color = Color.Black, size = Size(qrSize, qrSize), style = Stroke(width = 4f))
                                        
                                        // 3 positional squares typical of QR codes
                                        drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(block * 2.5f, block * 2.5f))
                                        drawRect(color = Color.White, topLeft = Offset(block * 0.4f, block * 0.4f), size = Size(block * 1.7f, block * 1.7f))
                                        drawRect(color = Color.Black, topLeft = Offset(block * 0.8f, block * 0.8f), size = Size(block * 0.9f, block * 0.9f))

                                        drawRect(color = Color.Black, topLeft = Offset(qrSize - block * 2.5f, 0f), size = Size(block * 2.5f, block * 2.5f))
                                        drawRect(color = Color.White, topLeft = Offset(qrSize - block * 2.1f, block * 0.4f), size = Size(block * 1.7f, block * 1.7f))
                                        drawRect(color = Color.Black, topLeft = Offset(qrSize - block * 1.7f, block * 0.8f), size = Size(block * 0.9f, block * 0.9f))

                                        drawRect(color = Color.Black, topLeft = Offset(0f, qrSize - block * 2.5f), size = Size(block * 2.5f, block * 2.5f))
                                        drawRect(color = Color.White, topLeft = Offset(block * 0.4f, qrSize - block * 2.1f), size = Size(block * 1.7f, block * 1.7f))
                                        drawRect(color = Color.Black, topLeft = Offset(block * 0.8f, qrSize - block * 1.7f), size = Size(block * 0.9f, block * 0.9f))

                                        // Random organic qr blocks inside
                                        drawRect(color = ForestGreen, topLeft = Offset(block * 4.4f, block * 4.4f), size = Size(block * 1.5f, block * 1.5f))
                                        drawRect(color = ForestGreen, topLeft = Offset(block * 3.1f, block * 1.2f), size = Size(block * 0.8f, block * 2.1f))
                                        drawRect(color = ForestGreen, topLeft = Offset(block * 1.2f, block * 3.1f), size = Size(block * 2.1f, block * 0.8f))
                                        drawRect(color = ForestGreen, topLeft = Offset(block * 5.2f, block * 2.2f), size = Size(block * 0.9f, block * 1.4f))
                                        drawRect(color = Color.Black, topLeft = Offset(block * 2.2f, block * 5.2f), size = Size(block * 1.4f, block * 0.9f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Để hoàn tất nhanh, hãy bấm [Xác nhận Đã Chuyển Khoản] bên dưới.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            "Thẻ ATM/Visa" -> {
                                Text("Nhập thông tin thẻ ATM/Visa để trừ tài khoản trực tiếp:", fontSize = 12.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { if (it.length <= 16) cardNumber = it },
                                    label = { Text("Số thẻ (16 chữ số)", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = cardName,
                                    onValueChange = { cardName = it.uppercase() },
                                    label = { Text("Tên chủ thẻ (Viết liền không dấu)", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { if (it.length <= 5) cardExpiry = it },
                                    label = { Text("Hạn dùng MM/YY", fontSize = 12.sp) },
                                    placeholder = { Text("12/28") },
                                    modifier = Modifier.fillMaxWidth(0.5f),
                                    singleLine = true
                                )
                            }

                            "Ví MoMo" -> {
                                Text("Thanh toán bảo mật một chạm qua Ví MoMo:", fontSize = 12.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(12.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(Color(0xFFA50064), shape = RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("MoMo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Ví MoMo liên kết: 0123456789 (${cardName.ifBlank { "TRẦN HUY HOÀNG" }})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                    Text("Hệ thống sẽ chuyển tiếp sang app ví để xác thực mã giao dịch.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                }
                            }
                        }

                    } else {
                        // Displaying processing flow statuses
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (val result = checkoutResultState) {
                                is CheckoutResult.Loading -> {
                                    CircularProgressIndicator(color = ForestGreen, modifier = Modifier.size(44.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = result.msg,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                is CheckoutResult.Success -> {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(56.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Đặt Hàng Thành Công 🎉",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DeepText,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.message,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                                is CheckoutResult.Error -> {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(56.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Giao Dịch Thất Bại",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.error,
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (checkoutResultState == null) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.submitCheckout(selectedPaymentMethod).collect { result ->
                                    checkoutResultState = result
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Xác Nhận Thanh Toán • " + String.format("%,.0f", summary.total) + "đ")
                    }
                } else if (checkoutResultState is CheckoutResult.Success) {
                    Button(
                        onClick = {
                            val invoiceId = (checkoutResultState as CheckoutResult.Success).invoiceId
                            val message = (checkoutResultState as CheckoutResult.Success).message
                            showCheckoutDialog = false
                            onOrderSuccess(invoiceId, message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Về Trang Chủ Mua Tiếp")
                    }
                } else if (checkoutResultState is CheckoutResult.Error) {
                    Button(
                        onClick = { checkoutResultState = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Thử Lại")
                    }
                }
            },
            containerColor = SoftCream,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

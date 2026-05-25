package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.KhachHang
import com.example.data.KhuyenMai
import com.example.data.SanPham
import com.example.viewmodel.GreenMartViewModel
import com.example.viewmodel.UiEvent
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: GreenMartViewModel,
    customer: KhachHang?,
    activeVouchers: List<KhuyenMai>,
    products: List<SanPham>,
    onNavigateToCatalog: () -> Unit,
    onNavigateToVouchers: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrayBackground)
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // 1. Organic Green header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ForestGreen, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, shape = CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logogreenmart),
                                contentDescription = "GreenMart Logo",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "GREENMART",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif,
                                color = SoftCream,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Hệ Thống Thực Phẩm Hữu Cơ",
                                fontSize = 11.sp,
                                color = SoftCream.copy(alpha = 0.8f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToCatalog,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Personalized welcome segment
                Text(
                    text = "Xin chào, ${customer?.HoTen ?: "Khách hàng"} 👋",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Hôm nay bạn muốn đặt món ngon rau sạch nào?",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Loyalty Membership Card with Barcode Simulator
        if (customer != null) {
            val tier = if (customer.DiemTichLuy >= 300) "Vàng" else if (customer.DiemTichLuy >= 150) "Bạc" else "Thành viên"
            val pointsToNext = if (customer.DiemTichLuy >= 300) 0 else 300 - customer.DiemTichLuy
            val percent = if (customer.DiemTichLuy >= 300) 1f else customer.DiemTichLuy.toFloat() / 300f

            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(GreenGradientPrimary)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "THẺ THÀNH VIÊN LOYALTY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = customer.HoTen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        LoyaltyTierChip(tier)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Điểm tích lũy",
                            tint = OrganicAmber,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${customer.DiemTichLuy} Điểm Green",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (pointsToNext > 0) {
                        Text(
                            text = "Còn $pointsToNext điểm để nâng lên hạng Vàng ⭐",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { percent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = OrganicAmber,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    } else {
                        Text(
                            text = "Đã đạt hạng Vàng - Ưu đãi giảm 5% khi quét thanh toán!",
                            fontSize = 11.sp,
                            color = OrganicAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimal Barcode Simulator for cashiers to scan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Simulated barcode stripes
                            Row(
                                modifier = Modifier
                                    .height(30.dp)
                                    .fillMaxWidth(0.9f),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val bars = listOf(4,2,3,1,4,2,1,3,2,1,4,3,2,1,4,2,3,1)
                                bars.forEachIndexed { idx, width ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(width.dp)
                                            .background(if (idx % 2 == 0) Color.Black else Color.White)
                                    )
                                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "GM-${customer.SoDienThoai}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Horizontal Grid Category Actions
        SectionHeader(
            title = "Danh mục mua sắm",
            subtitle = "Thực phẩm organic sạch từ gốc đất trồng"
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val fastMenus = listOf(
                Triple("Rau Sạch", Icons.Default.Eco, "L01"),
                Triple("Trái Cây", Icons.Default.FilterVintage, "L02"),
                Triple("Thịt Sạch", Icons.Default.SetMeal, "L03"),
                Triple("Bơ Sữa", Icons.Default.LocalCafe, "L04")
            )
            fastMenus.forEach { (title, icon, categoryId) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.selectCategory(categoryId)
                            onNavigateToCatalog()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White, shape = CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = ForestGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Special Promo Vouchers section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mã Giảm Giá Đang Hot ⚡",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepText
            )
            Text(
                text = "Tất cả ví",
                fontSize = 13.sp,
                color = ForestGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToVouchers() }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Vouchers Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activeVouchers) { voucher ->
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .height(115.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Ticket left coupon part with dashed border visual
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(90.dp)
                                .background(Brush.verticalGradient(listOf(ForestGreen, LimeGreen)))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (voucher.LoaiKM == "Giảm theo %") "${voucher.GiaTri.toInt()}% OFF" else "${String.format("%,.0f", voucher.GiaTri / 1000)}k OFF",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Right details part
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(10.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = voucher.TenKM,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = voucher.MoTa,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ĐH từ ${String.format("%,.0f", voucher.DieuKien / 1000)}k",
                                    fontSize = 10.sp,
                                    color = ForestGreen,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Button(
                                    onClick = { viewModel.claimPublicVoucher(voucher) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Lưu mã", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Featured product highlights
        SectionHeader(
            title = "Hôm nay mua gì tại GreenMart?",
            subtitle = "Thực sạch VietGAP luôn tươi rói nhập kho mỗi sớm mai"
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products.take(6)) { product ->
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .clickable { onNavigateToCatalog() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            GreenMartImage(
                                url = product.HinhAnh,
                                contentDescription = product.TenSP,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Unit size tag
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(topStart = 8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = product.DonViTinh,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = product.TenSP,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format("%,.0f", product.DonGia)}đ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { 
                                    viewModel.addToCart(product.MaSP)
                                    coroutineScope.launch {
                                        viewModel.triggerToast("Đã thêm ${product.TenSP} vào Giỏ Hàng!")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = "Thêm", modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KhuyenMai
import com.example.viewmodel.GreenMartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoucherScreen(
    viewModel: GreenMartViewModel,
    myVouchers: List<KhuyenMai>,
    allVouchers: List<KhuyenMai>
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrayBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Hero top card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ForestGreen, shape = RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "VÍ VOUCHER KHÁCH HÀNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCream.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Bạn đang sở hữu ${myVouchers.size} mã giảm giá",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Chọn áp dụng trực tiếp tại giỏ hàng để kích hoạt mức giá sốc",
                            fontSize = 11.sp,
                            color = SoftCream.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section 1: My claimed vouchers
        item {
            Text(
                text = "Mã Giảm Giá Của Bạn (${myVouchers.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepText
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (myVouchers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bạn chưa có mã giảm giá nào trong Ví cá nhân.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Hãy thu thập thêm từ danh sách Khuyến Mãi bên dưới!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(myVouchers) { voucher ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        // Left ticket graphic
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(80.dp)
                                .background(GreenGradientPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (voucher.LoaiKM == "Giảm theo %") "${voucher.GiaTri.toInt()}% \nGIẢM" else "${String.format("%,.0f", voucher.GiaTri / 1000)}k \nGIẢM",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Right details side
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(12.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = voucher.TenKM,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepText
                                )
                                Text(
                                    text = "Đơn tối thiểu ${String.format("%,.0f", voucher.DieuKien)}đ. ${voucher.MoTa}",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    lineHeight = 14.sp
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hạn: ${dateFormatter.format(Date(voucher.NgayKetThuc))}",
                                    fontSize = 10.sp,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .background(ForestGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Đã trong ví",
                                        color = ForestGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Section 2: All Active Vouchers to collect
        item {
            Text(
                text = "Khai Thác Thêm Khuyến Mãi Hot 🔥",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepText
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allVouchers) { voucher ->
            val alreadyClaimed = myVouchers.any { it.MaKM == voucher.MaKM }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    // Left ticket graphic
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .background(if (alreadyClaimed) Brush.linearGradient(listOf(Color.LightGray, Color.LightGray)) else GreenGradientPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (voucher.LoaiKM == "Giảm theo %") "${voucher.GiaTri.toInt()}% \nGIẢM" else "${String.format("%,.0f", voucher.GiaTri / 1000)}k \nGIẢM",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Right details side
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = voucher.TenKM,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alreadyClaimed) Color.Gray else DeepText
                            )
                            Text(
                                text = "Đơn tối thiểu ${String.format("%,.0f", voucher.DieuKien)}đ. ${voucher.MoTa}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hạn: ${dateFormatter.format(Date(voucher.NgayKetThuc))}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )

                            if (alreadyClaimed) {
                                Text(
                                    text = "Đã thu nhận",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Button(
                                    onClick = { viewModel.claimPublicVoucher(voucher) },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrganicAmber),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Nhận mã", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KhachHang
import com.example.data.LichSuDoiQua
import com.example.data.QuaDongTichDiem
import com.example.viewmodel.GreenMartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoyaltyScreen(
    viewModel: GreenMartViewModel,
    customer: KhachHang?,
    gifts: List<QuaDongTichDiem>,
    history: List<LichSuDoiQua>
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrayBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Point balance display hero segment
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ForestGreen, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "VÍ ĐIỂM LOYALTY GREEN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftCream.copy(alpha = 0.8f),
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${customer?.DiemTichLuy ?: 0}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Điểm tích lũy khả dụng",
                        fontSize = 13.sp,
                        color = SoftCream
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account basic progress bar info
                    val tier = if ((customer?.DiemTichLuy ?: 0) >= 300) "HẠNG VÀNG ⭐" else if ((customer?.DiemTichLuy ?: 0) >= 150) "HẠNG BẠC ☘️" else "THÀNH VIÊN"
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Hạng hiện tại: $tier",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Rules block
        item {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "☘️ Thể Lệ Hạng Thẻ & Tích Điểm",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Tích lũy: Nhận ngay +1 điểm Green cho mỗi 20,000 VND chi tiêu thực thanh toán.\n" +
                               "• Hạng Bạc (Từ 150đ): Tặng voucher sinh nhật 50k & giảm 2% khi mua sắm trực tiếp.\n" +
                               "• Hạng Vàng (Từ 300đ): Được giảm 5% khi quét thẻ thanh toán & freeship trọn đời cho mọi đơn hàng.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 3. Gift catalog header
        item {
            SectionHeader(
                title = "Đổi Điểm Lấy Quà Xanh 🎁",
                subtitle = "Mang trọn món quà thiên nhiên organic về không gian căn bếp Việt"
            )
        }

        // List gifts
        if (gifts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Đang tải danh sách quà từ máy chủ...", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(gifts) { gift ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gift Image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            GreenMartImage(
                                url = gift.HinhAnh,
                                contentDescription = gift.TenQua,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = gift.TenQua,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = OrganicAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${gift.DiemCanThiet} điểm",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Còn lại: ${gift.SoLuongCon} phần quà",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val hasEnoughPoints = (customer?.DiemTichLuy ?: 0) >= gift.DiemCanThiet
                        Button(
                            onClick = { viewModel.redeemGift(gift) },
                            enabled = hasEnoughPoints && gift.SoLuongCon > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrganicAmber,
                                contentColor = Color.White,
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Đổi quà",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Redemption history
        if (history.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Lịch Sử Đổi Quà Thẻ",
                    subtitle = "Danh sách quà bạn đã quy đổi thành công"
                )
            }

            items(history) { log ->
                val matchedGift = gifts.find { it.MaQua == log.MaQua }
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = matchedGift?.TenQua ?: "Quà Tặng GreenMart",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Ngày đổi: ${dateFormatter.format(Date(log.NgayDoi))}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Mã xác minh: ${log.MaLichSu.takeLast(6)}",
                                fontSize = 11.sp,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "-${log.SoDiemKhauTru}đ",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

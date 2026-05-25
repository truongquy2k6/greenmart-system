package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LoaiSanPham
import com.example.data.SanPham
import com.example.viewmodel.GreenMartViewModel
import com.example.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: GreenMartViewModel,
    categories: List<LoaiSanPham>,
    filteredProducts: List<SanPham>,
    selectedCategory: String,
    searchQuery: String
) {
    var selectedProductForDetail by remember { mutableStateOf<SanPham?>(null) }
    var detailQuantity by remember { mutableIntStateOf(1) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrayBackground)
    ) {
        // 1. Organic Search Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ForestGreen)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Tìm kiếm rau, hoa quả, thịt thảo mộc...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ForestGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = ForestGreen)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(26.dp)
            )
        }

        // 2. Horizontal Categories list bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == "Tất cả",
                    onClick = { viewModel.selectCategory("Tất cả") },
                    label = { Text("Tất cả", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = DeepText
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category.MaLoai,
                    onClick = { viewModel.selectCategory(category.MaLoai) },
                    label = { Text(category.TenLoai, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = DeepText
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // 3. Status summary results text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sản phẩm tìm thấy: ${filteredProducts.size}",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        // 4. Products Grid layout
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = ForestGreen.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Không tìm thấy sản phẩm nào phù hợp.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedProductForDetail = product
                                detailQuantity = 1
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                GreenMartImage(
                                    url = product.HinhAnh,
                                    contentDescription = product.TenSP,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(topStart = 8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = product.DonViTinh,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = product.TenSP,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Text(
                                    text = product.MoTa,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format("%,.0f", product.DonGia)}đ",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )

                                    IconButton(
                                        onClick = {
                                            viewModel.addToCart(product.MaSP)
                                            viewModel.triggerToast("Đã thêm ${product.TenSP} vào Giỏ Hàng!")
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(ForestGreen, shape = CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Thêm",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. High-fidelity product detail dialog
    selectedProductForDetail?.let { product ->
        AlertDialog(
            onDismissRequest = { selectedProductForDetail = null },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        GreenMartImage(
                            url = product.HinhAnh,
                            contentDescription = product.TenSP,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(ForestGreen, shape = RoundedCornerShape(bottomStart = 12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Chuẩn Organic",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = product.TenSP,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                    Text(
                        text = "Đơn vị tính: ${product.DonViTinh} | Hạn sử dụng: Luôn tươi trong tuần",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.MoTa,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đơn giá:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${String.format("%,.0f", product.DonGia)}đ",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity Counter select block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chọn Số Lượng:",
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 14.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { if (detailQuantity > 1) detailQuantity-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Trừ", tint = ForestGreen)
                            }
                            Text(
                                text = "$detailQuantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = { detailQuantity++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Cộng", tint = ForestGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repeat(detailQuantity) {
                            viewModel.addToCart(product.MaSP)
                        }
                        selectedProductForDetail = null
                        viewModel.triggerToast("Đã thêm $detailQuantity ${product.TenSP} vào giỏ hàng!")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm vào giỏ hàng • ${String.format("%,.0f", product.DonGia * detailQuantity)}đ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedProductForDetail = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Để sau", color = Color.Gray)
                }
            },
            containerColor = SoftCream,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

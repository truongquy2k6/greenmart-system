package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GreenMartViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color constants prefixed specifically to avoid package name conflicts
private val ChatBotForestGreen = Color(0xFF2E7D32)
private val ChatBotEmeraldGreen = Color(0xFF4CAF50)
private val ChatBotDeepText = Color(0xFF1B5E20)
private val ChatBotOrganicAmber = Color(0xFFFFB300)
private val ChatBotSoftCream = Color(0xFFF9FBE7)
private val ChatBotSoftGrayBackground = Color(0xFFF5F5F5)

data class ChatMessage(
    val id: String,
    val sender: String, // "User", "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val recipeItems: List<String>? = null, // List of MaSP
    val recipeTitle: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatBotDialog(
    viewModel: GreenMartViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "1",
                    sender = "AI",
                    message = "Xin chào! Tôi là Trợ Lý Sức Khỏe AI của GreenMart 🥦.\nTôi có thể giúp bạn lên thực đơn, gợi ý các món ăn dinh dưỡng và tự động thêm các nguyên liệu tươi sạch chuẩn VietGAP vào Giỏ Hàng giúp bạn!\n\nBạn có thể thử bấm các gợi ý bên dưới để trải nghiệm nhé! 👇"
                )
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    fun sendSystemResponse(userMsg: String) {
        val lower = userMsg.lowercase()
        coroutineScope.launch {
            isTyping = true
            delay(1200) // Simulated smart AI thinking delay

            val aiResponse = when {
                lower.contains("salad") || lower.contains("giảm cân") || lower.contains("ăn kiêng") -> {
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = "AI",
                        message = "Món Salad giảm cân cao cấp là lựa chọn tuyệt vời! Salad ba chỉ hoặc ức gà bơ sáp xà lách mỡ mọng nước sẽ đem lại 100% năng lượng organic và vitamin lành mạnh cho bạn:\n\n• Xà Lách Mỡ Đà Lạt Mỹ (25.000đ)\n• Cà Chua Bi Cherry Đỏ (45.000đ)\n• Bơ Sáp Đắk Lắk Loại 1 (55.000đ)\n• Ức Gà Thảo Dược Phi Lê (75.000đ)\n\nTổng cộng chỉ 200.000đ cho 1 bữa ăn tràn đầy sức sống. Bạn muốn tôi soạn tất cả nguyên liệu vào giỏ hàng ngay không?",
                        recipeItems = listOf("SP001", "SP002", "SP005", "SP008"),
                        recipeTitle = "Nguyên Liệu Salad Giảm Cân Cao Cấp"
                    )
                }
                lower.contains("sáng") || lower.contains("bé") || lower.contains("sữa") -> {
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = "AI",
                        message = "Bữa sáng tràn đầy năng lượng bổ dưỡng cho gia đình và bé yêu nhà bạn đã sẵn sàng:\n\n• Táo Fuji Hữu Cơ Nhập Khẩu (89.000đ)\n• Sữa Tươi Thanh Trùng DalatMilk (38.000đ)\n\nSản phẩm sữa tươi nguyên chất thanh trùng kết hợp cùng táo hữu cơ giòn ngọt giúp kích thích hệ tiêu hóa phát triển toàn diện tốt nhất!",
                        recipeItems = listOf("SP004", "SP009"),
                        recipeTitle = "Bữa Sáng Dinh Dưỡng Cho Bé"
                    )
                }
                lower.contains("canh") || lower.contains("bông cải") || lower.contains("thịt") -> {
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = "AI",
                        message = "Canh ba chỉ cuộn bông cải xanh VietGAP cực kì thanh mát, bồi bổ cơ thể giải nhiệt ngày nắng nóng:\n\n• Bông Cải Xanh VietGAP (35.000đ)\n• Thịt Ba Chỉ Heo Thảo Mộc (165.000đ)\n• Cà Chua Bi Cherry Đỏ (45.000đ)\n\nMón ăn thơm bùi béo ngậy từ thịt heo sạch thảo mộc cùng bông cải xanh giòn sần sật cực giàu chất chống oxy hóa!",
                        recipeItems = listOf("SP003", "SP007", "SP002"),
                        recipeTitle = "Canh Ba Chỉ Bông Cải VietGAP"
                    )
                }
                lower.contains("vietgap") || lower.contains("sạch") || lower.contains("giới thiệu") -> {
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = "AI",
                        message = "100% rau củ quả tại GreenMart đạt chuẩn VietGAP hoặc Organic Châu Âu. Chúng tôi liên kết trực tiếp với các nông trại công nghệ cao tại Đà Lạt và Đắk Lắk, kiểm nghiệm nồng độ thuốc bảo vệ thực vật nghiêm ngặt trước khi nhập kệ.\nGreenMart cam kết bảo hành 1 đổi 1 trong 24h nếu rau củ bị dập héo!"
                    )
                }
                else -> {
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = "AI",
                        message = "Cảm ơn bạn đã trò chuyện cùng GreenMart AI! Tôi khuyên bạn nên thử các món rau quả organic tươi mát VietGAP hôm nay để bảo vệ sức khỏe gia đình nhé. Hãy thử gõ 'salad giảm cân' hoặc 'canh bông cải' để tôi gợi ý công thức và soạn hàng giúp bạn nhanh nhất!"
                    )
                }
            }

            messages = messages + aiResponse
            isTyping = false
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun handleSend(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "User",
            message = text.trim()
        )
        messages = messages + userMsg
        inputText = ""
        coroutineScope.launch {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
        sendSystemResponse(userMsg.message)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatBotSoftGrayBackground)
            ) {
                // 1. Premium AI Assistant Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(ChatBotForestGreen, ChatBotEmeraldGreen)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🤖", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Trợ Lý Nấu Ăn AI GreenMart",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Đang trực tuyến • Soạn hàng tự động",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // 2. Chat history bubble lazy column
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { chat ->
                        val isUser = chat.sender == "User"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(ChatBotForestGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🤖", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(
                                modifier = Modifier.widthIn(max = 280.dp),
                                horizontalAlignment = if (isUser) Alignment.End else Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) ChatBotForestGreen else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Text(
                                        text = chat.message,
                                        color = if (isUser) Color.White else Color.DarkGray,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    )
                                }

                                // Interactive Recipe add to cart card inside chat!
                                if (chat.recipeItems != null && chat.recipeTitle != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = ChatBotSoftCream),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ChatBotForestGreen.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🥗", fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = chat.recipeTitle,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = ChatBotDeepText
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    chat.recipeItems.forEach { maSP ->
                                                        viewModel.addToCart(maSP)
                                                    }
                                                    viewModel.triggerToast("Đã tự động thêm toàn bộ nguyên liệu của '${chat.recipeTitle}' vào Giỏ Hàng thành công!")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ChatBotForestGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ShoppingCart,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Bỏ hết vào giỏ hàng ngay",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (isUser) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, ChatBotForestGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👤", fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    if (isTyping) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(ChatBotForestGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🤖", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "AI đang suy nghĩ...",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Preset chips list for fast testing and navigation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = "💡 Gợi ý nhanh cho bạn hôm nay:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(ChatBotForestGreen.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .border(0.5.dp, ChatBotForestGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable { handleSend("Salad giảm cân organic") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("🥗 Salad giảm cân", fontSize = 11.sp, color = ChatBotForestGreen, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .background(ChatBotForestGreen.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .border(0.5.dp, ChatBotForestGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable { handleSend("Bữa sáng dinh dưỡng cho bé") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("🥛 Bữa sáng bổ dưỡng", fontSize = 11.sp, color = ChatBotForestGreen, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .background(ChatBotForestGreen.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .border(0.5.dp, ChatBotForestGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable { handleSend("Canh ba chỉ bông cải") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("🍲 Canh bông cải", fontSize = 11.sp, color = ChatBotForestGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Message input row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Hỏi công thức, dinh dưỡng...", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { handleSend(inputText) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChatBotForestGreen,
                                    focusedLabelColor = ChatBotForestGreen
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(ChatBotForestGreen, CircleShape)
                                    .clickable { handleSend(inputText) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

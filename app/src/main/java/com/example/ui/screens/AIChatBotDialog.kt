package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.GreenMartViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient

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
                    message = "Xin chào! Tôi là Trợ Lý Sức Khỏe AI của GreenMart 🥬.\nTôi có thể giúp bạn lên thực đơn, gợi ý các món ăn dinh dưỡng và tự động thêm các nguyên liệu tươi sạch chuẩn VietGAP vào Giỏ Hàng giúp bạn!\n\nBạn có thể thử bấm các gợi ý bên dưới để trải nghiệm nhé! 👇"
                )
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var requestTimestamps by remember { mutableStateOf(listOf<Long>()) }

    fun sendSystemResponse(userMsg: String) {
        val now = System.currentTimeMillis()
        val recentRequests = requestTimestamps.filter { now - it < 60000 }

        if (recentRequests.size >= 15) {
            val alertMsg = ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "AI",
                message = "⚠️ **CẢNH BÁO GIỚI HẠN YÊU CẦU:**\nHệ thống AI của GreenMart hiện tại đang giới hạn tối đa **15 câu hỏi mỗi phút** đối với gói miễn phí để tránh quá tải API.\n\nVui lòng đợi một lát rồi đặt câu hỏi tiếp theo nhé! Cảm ơn sự hợp tác của bạn! ⏰"
            )
            messages = messages + alertMsg
            coroutineScope.launch {
                delay(100)
                listState.animateScrollToItem(messages.size - 1)
            }
            return
        }

        requestTimestamps = recentRequests + now

        coroutineScope.launch {
            isTyping = true

            // Generate dynamic grounding database representations
            val productsContext = viewModel.allProductsStore.value.joinToString("\n") {
                "- ${it.TenSP} (Mã: ${it.MaSP}) - Giá: ${it.DonGia.toInt()}đ/${it.DonViTinh}. Mô tả: ${it.MoTa}"
            }
            val vouchersContext = viewModel.activeVouchers.value.joinToString("\n") {
                "- ${it.TenKM} (Mã: ${it.MaKM}) - Giảm ${it.GiaTri.toInt()}${if (it.LoaiKM == "Giảm theo %") "%" else "đ"} cho đơn từ ${it.DieuKien.toInt()}đ."
            }

            val systemPrompt = """
                Bạn là Trợ Lý Sức Khỏe AI thân thiện độc quyền của siêu thị GreenMart.
                Nhiệm vụ của bạn là hỗ trợ khách hàng lên thực đơn, tư vấn dinh dưỡng và gợi ý món ăn sạch VietGAP.

                RÀNG BUỘC CỰC KỲ NGHIÊM NGẶT:
                1. Bạn CHỈ ĐƯỢC PHÉP gợi ý các sản phẩm và chương trình khuyến mãi thực tế đang có trong danh sách được cung cấp bên dưới. TUYỆT ĐỐI không tự bịa ra bất kỳ sản phẩm, giá bán, hoặc mã khuyến mãi nào khác!
                2. Nếu người dùng hỏi mua hoặc hỏi nấu món gì không có nguyên liệu phù hợp trong danh sách của siêu thị, hãy lịch sự từ chối hoặc tư vấn họ sử dụng các sản phẩm thay thế hiện có.
                3. Khi gợi ý các món ăn, hãy trình bày ngắn gọn công thức và liệt kê các nguyên liệu cần mua kèm theo Tên sản phẩm, Mã sản phẩm (MaSP) và Giá bán chính xác.
                4. ĐẶC BIỆT: Ở cuối cùng câu trả lời, nếu có gợi ý nguyên liệu mua tại GreenMart, bạn BẮT BUỘC phải viết thêm một dòng chứa chuỗi định dạng JSON chính xác sau (Lưu ý viết đúng trên một dòng riêng biệt, không đặt trong markdown code block):
                [RECIPE_JSON: {"title": "Tên món ăn gợi ý", "items": ["MaSP1", "MaSP2"]}]
                Chuỗi JSON này vô cùng quan trọng để hệ thống hiển thị nút mua nhanh tự động.

                Danh sách sản phẩm GreenMart hiện có:
                $productsContext

                Danh sách chương trình khuyến mãi đang áp dụng:
                $vouchersContext
            """.trimIndent()

            val apiKey = "AIzaSyBAkPPTgwzAiNpkVcXyjElA1aPnaS4f6Q0"

            val rawResponse = callGeminiApi(apiKey, systemPrompt, messages)

            // Extract and parse recipe JSON [RECIPE_JSON: ...] from response
            val jsonRegex = Regex("\\[RECIPE_JSON:\\s*(\\{.*?\\})\\s*\\]")
            val match = jsonRegex.find(rawResponse)

            var recipeTitle: String? = null
            var recipeItems: List<String>? = null
            var cleanMessage = rawResponse

            if (match != null) {
                try {
                    val jsonStr = match.groupValues[1]
                    val recipeObj = JSONObject(jsonStr)
                    recipeTitle = recipeObj.optString("title", null)
                    val itemsArray = recipeObj.optJSONArray("items")
                    if (itemsArray != null && itemsArray.length() > 0) {
                        val list = mutableListOf<String>()
                        for (i in 0 until itemsArray.length()) {
                            list.add(itemsArray.getString(i))
                        }
                        recipeItems = list
                    }
                    cleanMessage = rawResponse.replace(match.value, "").trim()
                } catch (e: Exception) {
                    // Fallback
                }
            }

            val aiResponse = ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "AI",
                message = cleanMessage,
                recipeItems = recipeItems,
                recipeTitle = recipeTitle
            )

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatBotSoftGrayBackground)
                    .systemBarsPadding()
                    .imePadding()
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
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
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

                                if (!isUser && chat.id == "1") {
                                    val presetTopics = listOf(
                                        Triple("🥗 Salad Giảm Cân Organic", "Công thức Salad ức gà & bơ Đắk Lắk tốt lành", "Salad giảm cân organic"),
                                        Triple("🍼 Bữa Sáng Dinh Dưỡng", "Thực đơn dinh dưỡng từ Táo Fuji & Sữa DalatMilk", "Bữa sáng dinh dưỡng"),
                                        Triple("🍲 Canh Bông Cải VietGAP", "Canh bông cải xanh cuộn thịt heo sạch thanh mát", "Canh ba chỉ bông cải"),
                                        Triple("🛡️ Tiêu Chuẩn Rau Sạch", "Khám phá quy trình kiểm duyệt VietGAP GreenMart", "Rau sạch VietGAP")
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "👉 Gợi ý chủ đề hỏi AI nhanh:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ChatBotDeepText,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                                    )

                                    presetTopics.forEachIndexed { index, (title, desc, query) ->
                                        val bgColor = when (index) {
                                            0 -> Color(0xFFE8F5E9)
                                            1 -> Color(0xFFE3F2FD)
                                            2 -> Color(0xFFFFF3E0)
                                            else -> Color(0xFFF3E5F5)
                                        }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable { handleSend(query) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = bgColor),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = title,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = desc,
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = null,
                                                    tint = ChatBotForestGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 16.dp),
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
    }
}

suspend fun callGeminiApi(
    apiKey: String,
    systemPrompt: String,
    chatHistory: List<ChatMessage>
): String = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val rootJson = JSONObject()

        // System instruction JSON
        val sysInstructionJson = JSONObject().put(
            "parts",
            JSONArray().put(JSONObject().put("text", systemPrompt))
        )
        rootJson.put("systemInstruction", sysInstructionJson)

        // Contents JSON array representing the multi-turn conversational dialog history
        val contentsArray = JSONArray()
        chatHistory.forEach { msg ->
            val role = if (msg.sender == "User") "user" else "model"
            // Strip out custom recipe JSON blocks before sending to keep API context clean
            val cleanText = msg.message.substringBefore("[RECIPE_JSON:").trim()
            if (cleanText.isNotEmpty()) {
                val partsArray = JSONArray().put(JSONObject().put("text", cleanText))
                contentsArray.put(
                    JSONObject()
                        .put("role", role)
                        .put("parts", partsArray)
                )
            }
        }
        rootJson.put("contents", contentsArray)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val url = "https://greenmart-api-quy.loca.lt/api/chatbot"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
                return@withContext "Không nhận được phản hồi từ AI."
            } else {
                if (response.code == 429) {
                    return@withContext "Bot đang nhận quá nhiều yêu cầu cùng lúc (vượt giới hạn API miễn phí). Vui lòng đợi khoảng 1 phút rồi thử lại nhé!"
                }
                return@withContext "Lỗi kết nối AI: ${response.code}\nChi tiết: $bodyString"
            }
        }
    } catch (e: Exception) {
        return@withContext "Không thể kết nối đến máy chủ AI: ${e.message}"
    }
}

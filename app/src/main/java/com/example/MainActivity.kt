package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GreenMartViewModel
import com.example.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

data class AppNotification(
    val id: String,
    val title: String,
    val content: String,
    val time: String,
    val isRead: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold() {
    val context = LocalContext.current
    val viewModel: GreenMartViewModel = viewModel()

    // Collect all states in parallel
    val customer by viewModel.customerState.collectAsState()
    val categories by viewModel.categoriesState.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeVouchers by viewModel.activeVouchers.collectAsState()
    val myVouchers by viewModel.myVouchers.collectAsState()
    val stores by viewModel.storesList.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    val gifts by viewModel.redeemableGifts.collectAsState()
    val history by viewModel.redemptionHistory.collectAsState()
    val cartItems by viewModel.cartWithDetails.collectAsState()
    val summary by viewModel.cartSummary.collectAsState()
    val appliedVoucher by viewModel.appliedVoucher.collectAsState()
    val selectedCheckoutStore by viewModel.selectedCheckoutStore.collectAsState()
    val pastOrders by viewModel.pastOrders.collectAsState()

    // Navigation Tab state index
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Dialog triggering states from ViewModel shared queue
    var showDialogTitle by remember { mutableStateOf("") }
    var showDialogMessage by remember { mutableStateOf("") }
    var showInfoDialogState by remember { mutableStateOf(false) }

    // Bottom Sheet for active vouchers inside Care Tab
    var accountSubIndex by remember { mutableStateOf(0) } // 0: GiftsExchange, 1: MyVouchers, 2: History

    // State for AI Chatbot Dialog
    var showChatBotDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    var aiButtonOffsetX by remember { mutableStateOf(0f) }
    var aiButtonOffsetY by remember { mutableStateOf(0f) }

    // Splash screen state
    var showSplashScreen by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = true) {
        delay(1500)
        showSplashScreen = false
    }

    var notifications by remember {
        mutableStateOf(
            listOf(
                AppNotification("1", "Rau sạch VietGAP mới về hôm nay! 🥬", "Xà lách mỡ và cà chua bi hữu cơ tươi mới nhập kho lúc 5h sáng, giảm giá 10% đặc biệt cho thành viên.", "Vừa xong"),
                AppNotification("2", "Bạn nhận được Voucher 50.000đ 🎫", "Mã KM02 giảm ngay 50k cho hóa đơn từ 300k vừa được kích hoạt trong ví của bạn. Mua sắm ngay!", "1 giờ trước"),
                AppNotification("3", "Tính năng Trợ Lý Sức Khỏe AI đã hoạt động 🤖", "Hỏi AI công thức dinh dưỡng nấu canh chua, làm salad organic và tự soạn giỏ hàng tức thì!", "Hôm qua")
            )
        )
    }

    // Shared VM Single Live Event listener triggers
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ShowDialog -> {
                    showDialogTitle = event.title
                    showDialogMessage = event.message
                    showInfoDialogState = true
                }
            }
        }
    }

    if (showSplashScreen) {
        val ForestGreen = Color(0xFF2E7D32)
        val EmeraldGreen = Color(0xFF4CAF50)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(ForestGreen, EmeraldGreen))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.White, shape = CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logogreenmart),
                        contentDescription = "GreenMart Logo",
                        modifier = Modifier.size(90.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "GreenMart",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Siêu Thị Sạch Cho Mọi Nhà",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val ForestGreen = Color(0xFF2E7D32)
            val EmeraldGreen = Color(0xFF4CAF50)
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(colors = listOf(ForestGreen, EmeraldGreen)))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White, shape = CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logogreenmart),
                                contentDescription = "GreenMart Logo",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GreenMart",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                            .clickable { showNotificationsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Thông báo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        val unreadCount = notifications.count { !it.isRead }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 2.dp, end = 2.dp)
                                    .background(Color.Red, CircleShape)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Trang chủ"
                        )
                    },
                    label = { Text("Trang Chủ", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 0) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreen,
                        selectedTextColor = ForestGreen,
                        indicatorColor = ForestGreen.copy(alpha = 0.12f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 2: Catalog Shopping
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 1) Icons.Default.ShoppingBag else Icons.Outlined.ShoppingBag,
                            contentDescription = "Mua sắm"
                        )
                    },
                    label = { Text("Mua Sắm", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 1) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreen,
                        selectedTextColor = ForestGreen,
                        indicatorColor = ForestGreen.copy(alpha = 0.12f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 3: Store Locator
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 2) Icons.Default.NearMe else Icons.Outlined.NearMe,
                            contentDescription = "Cửa hàng"
                        )
                    },
                    label = { Text("Cửa Hàng", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 2) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreen,
                        selectedTextColor = ForestGreen,
                        indicatorColor = ForestGreen.copy(alpha = 0.12f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 4: Cart details
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    val count = cartItems.sumOf { it.second }
                                    Badge(containerColor = Color.Red) {
                                        Text("$count", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTabIndex == 3) Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart,
                                contentDescription = "Giỏ hàng"
                            )
                        }
                    },
                    label = { Text("Giỏ Hàng", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 3) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreen,
                        selectedTextColor = ForestGreen,
                        indicatorColor = ForestGreen.copy(alpha = 0.12f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 5: Account & loyalty hub
                NavigationBarItem(
                    selected = selectedTabIndex == 4,
                    onClick = { selectedTabIndex = 4 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 4) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = "Tài khoản"
                        )
                    },
                    label = { Text("Tài Khoản", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 4) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreen,
                        selectedTextColor = ForestGreen,
                        indicatorColor = ForestGreen.copy(alpha = 0.12f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                // Tab 0: Home screen Layout
                0 -> HomeScreen(
                    viewModel = viewModel,
                    customer = customer,
                    activeVouchers = activeVouchers,
                    products = filteredProducts,
                    onNavigateToCatalog = { selectedTabIndex = 1 },
                    onNavigateToVouchers = { 
                        selectedTabIndex = 4
                        accountSubIndex = 1 // Switch to subtab voucher folder
                    }
                )

                // Tab 1: Catalog view
                1 -> CatalogScreen(
                    viewModel = viewModel,
                    categories = categories,
                    filteredProducts = filteredProducts,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery
                )

                // Tab 2: Map stores local finder
                2 -> StoreLocatorScreen(
                    viewModel = viewModel,
                    stores = stores,
                    selectedStore = selectedStore
                )

                // Tab 3: Shopping Cart & Checkout Gateways
                3 -> CartScreen(
                    viewModel = viewModel,
                    cartItems = cartItems,
                    summary = summary,
                    myVouchers = myVouchers,
                    appliedVoucher = appliedVoucher,
                    stores = stores,
                    selectedCheckoutStore = selectedCheckoutStore,
                    onOrderSuccess = { invoiceId, msg ->
                        // Switch tab to account orders list or default home on success
                        selectedTabIndex = 4
                        accountSubIndex = 0 // Move to profile history invoice subtab directly
                    }
                )

                // Tab 4: Master loyalty care folder
                4 -> Column(modifier = Modifier.fillMaxSize()) {
                    // Sub tab selector
                    TabRow(
                        selectedTabIndex = accountSubIndex,
                        containerColor = ForestGreen,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[accountSubIndex]),
                                color = OrganicAmber
                            )
                        }
                    ) {
                        Tab(
                            selected = accountSubIndex == 0,
                            onClick = { accountSubIndex = 0 },
                            text = { Text("Tài Khoản & Đơn", color = Color.White, fontSize = 12.sp, fontWeight = if (accountSubIndex == 0) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) }
                        )
                        Tab(
                            selected = accountSubIndex == 1,
                            onClick = { accountSubIndex = 1 },
                            text = { Text("Voucher Của Tôi", color = Color.White, fontSize = 12.sp, fontWeight = if (accountSubIndex == 1) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) }
                        )
                        Tab(
                            selected = accountSubIndex == 2,
                            onClick = { accountSubIndex = 2 },
                            text = { Text("Quà Đổi Điểm", color = Color.White, fontSize = 12.sp, fontWeight = if (accountSubIndex == 2) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (accountSubIndex) {
                            0 -> ProfileScreen(
                                viewModel = viewModel,
                                customer = customer,
                                pastOrders = pastOrders,
                                myVouchersCount = myVouchers.size,
                                activeVouchersCount = activeVouchers.size,
                                unreadNotificationsCount = notifications.count { !it.isRead },
                                giftsCount = gifts.size,
                                onShowNotifications = { showNotificationsDialog = true },
                                onNavigateToSubTab = { index -> accountSubIndex = index },
                                onNavigateToMainTab = { index -> selectedTabIndex = index }
                            )
                            1 -> VoucherScreen(
                                viewModel = viewModel,
                                myVouchers = myVouchers,
                                allVouchers = activeVouchers
                            )
                            2 -> LoyaltyScreen(
                                viewModel = viewModel,
                                customer = customer,
                                gifts = gifts,
                                history = history
                            )
                        }
                    }
                }
            }

            // Draggable Floating AI Assistant Button overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                aiButtonOffsetX.roundToInt(),
                                aiButtonOffsetY.roundToInt()
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                aiButtonOffsetX += dragAmount.x
                                aiButtonOffsetY += dragAmount.y
                            }
                        }
                        .size(64.dp)
                        .shadow(6.dp, CircleShape)
                        .background(Color(0xFFA5D6A7), CircleShape)
                        .border(1.5.dp, Color(0xFF2E7D32), CircleShape)
                        .clickable { showChatBotDialog = true }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.aisongkhoe),
                        contentDescription = "AI Song Khoe",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Shared unified popup info dialog
    InfoDialog(
        show = showInfoDialogState,
        title = showDialogTitle,
        message = showDialogMessage,
        onDismiss = { showInfoDialogState = false }
    )

    // Interactive AI Chatbot Dialog
    if (showChatBotDialog) {
        AIChatBotDialog(
            viewModel = viewModel,
            onDismiss = { showChatBotDialog = false }
        )
    }

    // Interactive Notifications Dialog
    if (showNotificationsDialog) {
        val ForestGreen = Color(0xFF2E7D32)
        val DeepText = Color(0xFF1B5E20)
        val SoftCream = Color(0xFFF9FBE7)
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Thông Báo 🔔",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepText
                    )
                    Text(
                        text = "Đọc tất cả",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        modifier = Modifier
                            .clickable {
                                notifications = notifications.map { it.copy(isRead = true) }
                            }
                            .padding(4.dp)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không có thông báo nào", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        notifications.forEach { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (item.isRead) Color.White else SoftCream
                                ),
                                border = if (item.isRead) androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray) else androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        notifications = notifications.map { 
                                            if (it.id == item.id) it.copy(isRead = true) else it
                                        }
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 0.dp else 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                     Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (!item.isRead) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(ForestGreen, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = DeepText
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.time,
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.content,
                                        fontSize = 11.sp,
                                        color = if (item.isRead) Color.Gray else Color.DarkGray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ĐÓNG THÔNG BÁO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
}

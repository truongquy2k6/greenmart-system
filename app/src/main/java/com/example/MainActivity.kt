package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GreenMartViewModel
import com.example.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

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

    // State for Notifications Dialog
    var showNotificationsDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val ForestGreen = Color(0xFF2E7D32)
            val EmeraldGreen = Color(0xFF4CAF50)
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(colors = listOf(ForestGreen, EmeraldGreen)))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Tìm rau quả, thịt cá sạch VietGAP...",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = ForestGreen
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xóa",
                                    tint = Color.Gray,
                                    modifier = Modifier.clickable { viewModel.updateSearchQuery("") }
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.DarkGray,
                            unfocusedTextColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                            .clickable { showNotificationsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Thông báo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .background(Color.Red, CircleShape)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "3",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChatBotDialog = true },
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat AI",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hỏi AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                                pastOrders = pastOrders
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
                Text(
                    text = "Thông Báo Khuyến Mãi & Đơn Hàng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val notificationsList = listOf(
                        Triple("Rau sạch VietGAP mới về hôm nay! 🥬", "Xà lách mỡ và cà chua bi hữu cơ tươi mới nhập kho lúc 5h sáng, giảm giá 10% đặc biệt cho thành viên.", "Vừa xong"),
                        Triple("Bạn nhận được Voucher 50.000đ 🎫", "Mã KM02 giảm ngay 50k cho hóa đơn từ 300k vừa được kích hoạt trong ví của bạn. Mua sắm ngay!", "1 giờ trước"),
                        Triple("Tính năng Trợ Lý Sức Khỏe AI đã hoạt động 🤖", "Hỏi AI công thức dinh dưỡng nấu canh chua, làm salad organic và tự soạn giỏ hàng tức thì!", "Hôm qua")
                    )

                    notificationsList.forEach { (title, content, time) ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = SoftCream),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepText)
                                    Text(text = time, fontSize = 9.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = content, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ĐÓNG THÔNG BÁO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                        accountSubIndex = 2 // Move to history invoice subtab directly
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
                            text = { Text("Quà Đổi Điểm", color = Color.White, fontSize = 12.sp, fontWeight = if (accountSubIndex == 0) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) }
                        )
                        Tab(
                            selected = accountSubIndex == 1,
                            onClick = { accountSubIndex = 1 },
                            text = { Text("Voucher Của Tôi", color = Color.White, fontSize = 12.sp, fontWeight = if (accountSubIndex == 1) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) }
                        )
                        Tab(
                            selected = accountSubIndex == 2,
                            onClick = { accountSubIndex = 2 },
                            text = { Text("Tài Khoản & Đơn", color = Color.White, fontSize = 12.sp, fontWeight = if (accountSubIndex == 2) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (accountSubIndex) {
                            0 -> LoyaltyScreen(
                                viewModel = viewModel,
                                customer = customer,
                                gifts = gifts,
                                history = history
                            )
                            1 -> VoucherScreen(
                                viewModel = viewModel,
                                myVouchers = myVouchers,
                                allVouchers = activeVouchers
                            )
                            2 -> ProfileScreen(
                                viewModel = viewModel,
                                customer = customer,
                                pastOrders = pastOrders
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
}

package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BillEntity
import com.example.data.model.BillRoom
import com.example.data.model.PointItem
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BillViewModel
import com.example.util.PdfGenerator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BillingApp()
                }
            }
        }
    }
}

@Composable
fun BillingApp(viewModel: BillViewModel = viewModel()) {
    val context = LocalContext.current
    val savedBills by viewModel.filteredBills.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBarComponent(viewModel)
        },
        bottomBar = {
            BottomNavigationBarComponent(
                currentScreen = viewModel.currentScreen,
                onScreenSelected = { screen ->
                    viewModel.currentScreen = screen
                    if (screen == "create_bill") {
                        viewModel.selectedBillForView = null
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (viewModel.currentScreen) {
                "create_bill" -> CreateBillScreen(viewModel, context)
                "history" -> HistoryScreen(viewModel, savedBills, context)
                "rates_card" -> RatesCardScreen(viewModel)
                "bill_detail" -> BillDetailScreen(viewModel, context)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(viewModel: BillViewModel) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column {
                    Text(
                        text = "सिद्धिविनायक इलेक्ट्रिकल",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Siddhivinayak Electrical Services",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        },
        actions = {
            if (viewModel.currentScreen == "create_bill") {
                IconButton(onClick = {
                    viewModel.resetDraft()
                    Toast.makeText(viewModel.getApplication(), "डेटा रिसेट केला!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Form",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        modifier = Modifier.border(0.5.dp, Color(0xFFE2E2E2))
    )
}

@Composable
fun BottomNavigationBarComponent(
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarItem(
            selected = currentScreen == "create_bill" || currentScreen == "bill_detail",
            onClick = { onScreenSelected("create_bill") },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "New Bill") },
            label = { Text("नवीन बिल (New)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = currentScreen == "history",
            onClick = { onScreenSelected("history") },
            icon = { Icon(Icons.Default.List, contentDescription = "History") },
            label = { Text("इतिहास (History)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = currentScreen == "rates_card",
            onClick = { onScreenSelected("rates_card") },
            icon = { Icon(Icons.Default.Info, contentDescription = "Rates") },
            label = { Text("दर-पत्रक (Rates)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

@Composable
fun CreateBillScreen(viewModel: BillViewModel, context: Context) {
    var newRoomName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Customer Information Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ग्राहक आणि कंपनी माहिती Form",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = viewModel.customerName,
                    onValueChange = { viewModel.customerName = it },
                    label = { Text("ग्राहकाचे नाव (Customer Name) *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = viewModel.customerPhone,
                    onValueChange = { viewModel.customerPhone = it },
                    label = { Text("मोबाईल नंबर (Phone Number)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = viewModel.siteAddress,
                    onValueChange = { viewModel.siteAddress = it },
                    label = { Text("साइट पत्ता (Site Address)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = viewModel.advanceAmountStr,
                    onValueChange = { viewModel.advanceAmountStr = it },
                    label = { Text("अॅडव्हान्स जमा (Advance paid optional ₹)") },
                    leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // GST Mode Feature Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "जीएसटी (GST 18%) जोडा",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "एकूण बिलावर १८% जीएसटी समाविष्ट होईल",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = viewModel.isGstEnabled,
                        onCheckedChange = { viewModel.isGstEnabled = it }
                    )
                }
            }
        }

        // 2. Billing Summary Card (Reactive & Styled like Vibrant Premium Theme)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "एकूण रक्कम (Current Total):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "₹${String.format("%.2f", viewModel.calculateDraftTotal())}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "पॉइंट्स उपएकूण (Subtotal):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "₹${String.format("%.2f", viewModel.calculateDraftSubtotal())}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                if (viewModel.isGstEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "जीएसटी (GST 18%):",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "₹${String.format("%.2f", viewModel.calculateDraftGst())}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                val adv = viewModel.advanceAmountStr.toDoubleOrNull() ?: 0.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "अॅडव्हान्स दिले (Advance):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "- ₹${String.format("%.2f", adv)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val remainingBal = viewModel.calculateDraftTotal() - adv
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "बाकी देय (BALANCE DUE):",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "₹${String.format("%.2f", remainingBal)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = if (remainingBal > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        // 3. Room addition controller
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEDE8F5))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "खोल्यांनुसार पॉइंट्स यादी जोडा",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        placeholder = { Text("उदा. हॉल, बेडरूम, किचन...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (newRoomName.isNotBlank()) {
                                viewModel.addRoom(newRoomName)
                                newRoomName = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Room")
                    }
                }
            }
        }

        // List of Active Rooms in Draft
        if (viewModel.roomsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "सध्या कोणतीही खोली जोडलेली नाही.\nवर नाव लिहून 'Add Room' दाबा आणि वीज पॉइंट्स जोडा.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        } else {
            viewModel.roomsList.forEach { room ->
                RoomSectionCard(room = room, viewModel = viewModel)
            }

            // Summary Footer Card showing the automatically calculated grand totals
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "माहिती",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "एकूण गोषवारा (Session Summary)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${viewModel.roomsList.size} खोल्या (Rooms)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "एकूण वीज पॉइंट्स (Total Points):",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "${viewModel.calculateTotalPoints()} नग (Qty)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "पॉइंट्स उपएकूण (Subtotal):",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "₹${String.format("%.2f", viewModel.calculateDraftSubtotal())}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    if (viewModel.isGstEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "GST (18% inclusive):",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "₹${String.format("%.2f", viewModel.calculateDraftGst())}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "एकूण देय रक्कम (Grand Total):",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "₹${String.format("%.2f", viewModel.calculateDraftTotal())}",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 4. Digital Signature Pad for Customer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                com.example.ui.SignaturePad(
                    onSignatureCaptured = { bitmap ->
                        viewModel.signatureBitmap = bitmap
                    },
                    onClear = {
                        viewModel.signatureBitmap = null
                    }
                )
            }
        }

        // Save Button Option
        Button(
            onClick = {
                viewModel.saveBill(
                    context = context,
                    onSuccess = {
                        Toast.makeText(context, "बिल यशस्वीरित्या सेव्ह केले!", Toast.LENGTH_SHORT).show()
                        viewModel.currentScreen = "history"
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("बिल तयार करून सेव्ह करा (Save Bill)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun RoomSectionCard(room: BillRoom, viewModel: BillViewModel) {
    var expandedItemAdd by remember { mutableStateOf(false) }
    var selectedPointType by remember { mutableStateOf(viewModel.predefinedPoints.first().type) }
    var editRateStr by remember { mutableStateOf(viewModel.predefinedPoints.first().defaultRate.toString()) }
    var quantityVal by remember { mutableStateOf(1) }

    var isEditingName by remember { mutableStateOf(false) }
    var editNameField by remember(room.name) { mutableStateOf(room.name) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row with Inline Editing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditingName) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editNameField,
                            onValueChange = { editNameField = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                        IconButton(
                            onClick = {
                                if (editNameField.isNotBlank()) {
                                    viewModel.updateRoomName(room.id, editNameField)
                                    isEditingName = false
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "बदला", tint = Color(0xFF2E7D32))
                        }
                        IconButton(
                            onClick = {
                                editNameField = room.name
                                isEditingName = false
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "रद्द करा", tint = Color.Red)
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isEditingName = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = room.name.firstOrNull()?.uppercase() ?: "R",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = room.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "खोलीचे नाव बदला",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.deleteRoom(room.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = Color.Red.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Embedded Items Table inside standard room
            if (room.items.isEmpty()) {
                Text(
                    text = "या खोलीत वीज पॉइंट्स जोडलेले नाहीत.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            } else {
                room.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.type, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${item.quantity} नग x ₹${item.rate}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${item.total}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            IconButton(
                                onClick = { viewModel.deleteItemFromRoom(room.id, item.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete point", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Button to trigger "Add Points Form" inside this Room
            Button(
                onClick = { expandedItemAdd = !expandedItemAdd },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (expandedItemAdd) Color.Gray else MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.wrapContentSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (expandedItemAdd) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (expandedItemAdd) "फॉर्म बंद करा (Close)" else "इलेक्ट्रिकल पॉइंट जोडा (Add Point)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            AnimatedVisibility(visible = expandedItemAdd) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(Color(0xFFF9F7FC))
                        .border(1.dp, Color(0xFFECE6F4), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "नवीन इलेक्ट्रिक पॉइंट जोडा:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Drops Selection using Chips / Row of common templates
                    Text(text = "पॉइंट प्रकार निवडा:", fontSize = 11.sp, color = Color.Gray)
                    
                    // Quick selector of top points
                    val topPointTemplates = listOf("Bulb / Holder Point", "Ceiling Fan Point", "Simple Switchboard 5A", "Power Socket 15A/25A", "Distribution Box Fitting")
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            topPointTemplates.take(3).forEach { pt ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedPointType == pt) MaterialTheme.colorScheme.primary else Color(0xFFEDE8F5))
                                        .clickable {
                                            selectedPointType = pt
                                            editRateStr = (viewModel.predefinedPoints.find { it.type == pt }?.defaultRate ?: 100.0).toString()
                                        }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pt.substringBefore(" Point").substringBefore(" Fitting"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPointType == pt) Color.White else Color.Black,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            topPointTemplates.drop(3).forEach { pt ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedPointType == pt) MaterialTheme.colorScheme.primary else Color(0xFFEDE8F5))
                                        .clickable {
                                            selectedPointType = pt
                                            editRateStr = (viewModel.predefinedPoints.find { it.type == pt }?.defaultRate ?: 100.0).toString()
                                        }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pt.substringBefore(" Fitting"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPointType == pt) Color.White else Color.Black,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Simple Custom point name input
                    OutlinedTextField(
                        value = selectedPointType,
                        onValueChange = { selectedPointType = it },
                        label = { Text("पॉइंट नाव (Point Name/Type)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Rate Input
                    OutlinedTextField(
                        value = editRateStr,
                        onValueChange = { editRateStr = it },
                        label = { Text("दर/किंमत प्रति नग (Rate ₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Quantity picker with plus and minus counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "प्रमाण (Quantity):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantityVal > 1) quantityVal-- },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2DDF0))
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minus", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = quantityVal.toString(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { quantityVal++ },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2DDF0))
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Add action
                    Button(
                        onClick = {
                            val rateVal = editRateStr.toDoubleOrNull() ?: 0.0
                            if (selectedPointType.isNotBlank()) {
                                viewModel.addItemToRoom(room.id, selectedPointType, quantityVal, rateVal)
                                Toast.makeText(viewModel.getApplication(), "पॉइंट समाविष्ट केला!", Toast.LENGTH_SHORT).show()
                                expandedItemAdd = false
                                quantityVal = 1
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Point to List")
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: BillViewModel, billsList: List<BillEntity>, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "मागील बिले इतिहास (Saved Invoices)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Badge {
                Text("${billsList.size} Saved", modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        // Live Search component
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("ग्राहकाचे नाव किंवा पत्ता शोधा (Search bills)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (billsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "No data",
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (viewModel.searchQuery.isNotEmpty()) "या नावाची कोणतीही बिल सापडली नाहीत." else "इथे अद्याप कोणतीही बिले सेव्ह केलेली नाहीत.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(billsList) { bill ->
                    SavedBillCard(bill = bill, viewModel = viewModel, context = context)
                }
            }
        }
    }
}

@Composable
fun SavedBillCard(bill: BillEntity, viewModel: BillViewModel, context: Context) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("बिल डिलीट करायचे का?") },
            text = { Text("${bill.customerName} यांचे बिल फोनमधून डार्क डिलीट होईल. ही क्रिया पूर्ववत करता येणार नाही.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSavedBill(bill)
                    showDeleteConfirmDialog = false
                    Toast.makeText(context, "बिल डिलीट केले!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("डिलीट करा (Delete)", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("रद्द करा (Cancel)")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel.selectedBillForView = bill
                viewModel.currentScreen = "bill_detail"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFEFEFEF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Customer Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.customerName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "मोबाईल: +91 ${bill.customerPhone}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "दिनांक: ${bill.date}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Icon",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Details Calculation preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "एकूण रक्कम (Total)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₹${bill.totalAmount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "अॅडव्हान्स (Paid)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₹${bill.advanceAmount}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "बाकी रक्कम (Remaining)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₹${bill.remainingBalance}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (bill.remainingBalance > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun RatesCardScreen(viewModel: BillViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "शासकीय आणि प्रमाणित दर-पत्रक",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Siddhivinayak Electrical Services कडून आकारले जाणारे अंदाजित व नेहमीचे इलेक्ट्रिकल कामाचे दर पत्रक खालीलप्रमाणे आहेत.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Text(
            text = "प्रमाणित इलेक्ट्रिकल दर (Standard Rates Card)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.Gray
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.predefinedPoints) { pt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFEFEF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = pt.type, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "वर्ग: ${pt.category}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            text = "₹${pt.defaultRate}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillDetailScreen(viewModel: BillViewModel, context: Context) {
    val bill = viewModel.selectedBillForView ?: return
    val rooms = viewModel.getRoomsFromBill(bill)
    val scrollState = rememberScrollState()
    var showPrintPreview by remember { mutableStateOf(false) }

    if (showPrintPreview) {
        com.example.ui.PrintPreviewDialog(
            bill = bill,
            onDismiss = { showPrintPreview = false },
            onSharePdf = { header, customer, breakdown, signatures ->
                showPrintPreview = false
                val file = PdfGenerator.generateBillPdf(
                    context = context,
                    bill = bill,
                    signatureBitmap = null,
                    showHeader = header,
                    showCustomerDetails = customer,
                    showBreakdown = breakdown,
                    showSignatures = signatures
                )
                if (file != null) {
                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.fileprovider", file)
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Siddhivinayak Electrical Bill - ${bill.customerName}")
                        putExtra(android.content.Intent.EXTRA_TEXT, "नमस्कार, सिद्धिविनायक इलेक्ट्रिकल सर्व्हिसेस (Siddhivinayak Electrical Services) कडून आपले बिल सोबत जोडले आहे.\n\nएकूण रक्कम: ₹${bill.totalAmount}\nजमा रक्कम: ₹${bill.advanceAmount}\nबाकी रक्कम: ₹${bill.remainingBalance}\n\nधन्यवाद!")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "बिल पाठवा (Share PDF via)"))
                } else {
                    android.widget.Toast.makeText(context, "PDF जनरेट करण्यास अपयश आले", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen = "history" }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
            }
            Text(
                text = "बिल तपशील (Bill Detail Sheet)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Box(modifier = Modifier.width(48.dp)) // spacer
        }

        // Invoice Header Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEAEAEA))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "बिलाचा आयडी (Invoice #SVE-${bill.id})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = bill.date,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = bill.customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (bill.customerPhone.isNotEmpty()) {
                    Text(
                        text = "मोबाईल: +91 ${bill.customerPhone}",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
                if (bill.siteAddress.isNotEmpty()) {
                    Text(
                        text = "पत्ता: ${bill.siteAddress}",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Rooms Breakdown List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEDE8F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "कामाचा तपशील (Rooms Wise Breakdown)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (rooms.isEmpty()) {
                    Text(text = "कोणतीही खोली नि पॉइंट्स आढळले नाहीत.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    rooms.forEach { room ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "★ खोली: ${room.name}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        room.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, top = 4.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "- ${item.type} (${item.quantity} नग)",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "₹${item.total}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF2F2F2))
                    }
                }
            }
        }

        // Visual Calculations Summary Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "सामग्री/काम एकूण (Subtotal):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    val sub = rooms.flatMap { it.items }.sumOf { it.total }
                    Text(text = "₹${String.format("%.2f", sub)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                val checkGst = bill.totalAmount > (rooms.flatMap { it.items }.sumOf { it.total })
                if (checkGst) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "जीएसटी (GST 18% Included):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        val gst = bill.totalAmount - (rooms.flatMap { it.items }.sumOf { it.total })
                        Text(text = "₹${String.format("%.2f", gst)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "एकूण रक्कम (Grand Total):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "₹${bill.totalAmount}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "अॅडव्हान्स जमा (Advance Paid):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "- ₹${bill.advanceAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "उर्वरित बाकी (BALANCE DUE):", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                        Text(text = "₹${bill.remainingBalance}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Action Buttons: Real PDF Report Share, WhatsApp Text billing
        Button(
            onClick = {
                showPrintPreview = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("PDF प्रिंट आणि शेअर (Preview & Share PDF)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = {
                val shareText = StringBuilder().apply {
                    append("★ सिद्धिविनायक इलेक्ट्रिकल सर्व्हिसेस ★\n")
                    append("ग्राहक बिल तपशील (Invoice Summary)\n\n")
                    append("ग्राहक: ${bill.customerName}\n")
                    if (bill.customerPhone.isNotEmpty()) append("फोन: +91 ${bill.customerPhone}\n")
                    if (bill.siteAddress.isNotEmpty()) append("पत्ता: ${bill.siteAddress}\n")
                    append("तारीख: ${bill.date}\n")
                    append("---------------------------\n")
                    rooms.forEach { r ->
                        append("■ खोली: ${r.name}\n")
                        r.items.forEach { pt ->
                            append("- ${pt.type} (Qty ${pt.quantity}) x Rate ₹${pt.rate} = ₹${pt.total}\n")
                        }
                    }
                    append("---------------------------\n")
                    append("एकूण रक्कम: ₹${bill.totalAmount}\n")
                    append("जमा रक्कम: ₹${bill.advanceAmount}\n")
                    append("बाकी देय रक्कम: ₹${bill.remainingBalance}\n\n")
                    append("आपल्या व्यवसायाबद्दल मनःपूर्वक धन्यवाद! (SVE)")
                }.toString()

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "बिल मेसेज पाटोवा (Share via Text)"))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("WhatsApp मेसेज शेअर (Share Text Bill)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BillEntity
import com.example.data.model.BillRoom
import com.example.data.local.RoomsConverter
import com.example.util.PdfGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPreviewDialog(
    bill: BillEntity,
    onDismiss: () -> Unit,
    onSharePdf: (showHeader: Boolean, showCustomerDetails: Boolean, showBreakdown: Boolean, showSignatures: Boolean) -> Unit
) {
    var showHeader by remember { mutableStateOf(true) }
    var showCustomerDetails by remember { mutableStateOf(true) }
    var showBreakdown by remember { mutableStateOf(true) }
    var showSignatures by remember { mutableStateOf(true) }

    // Parse rooms to display in preview
    val roomsList = remember(bill.roomsJson) {
        try {
            RoomsConverter().fromRoomsString(bill.roomsJson)
        } catch (e: Exception) {
            emptyList<BillRoom>()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // full screen dialog
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "प्रिंट पूर्वदृश्य (Print Preview)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "कागदावर कसे दिसेल ते तपासा (Verify paper layout)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("रद्द करा (Cancel)")
                        }

                        Button(
                            onClick = {
                                onSharePdf(showHeader, showCustomerDetails, showBreakdown, showSignatures)
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PDF तयार व शेअर करा", fontSize = 13.sp)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF3F4F6)) // Elegant desk background
            ) {
                // Section Toggle Panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "कागदावरील विभाग निवडा (Toggle Page Sections):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 2x2 grid-like or compact Column for switches
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                ToggleItem(
                                    label = "कंपनी लोगो व शीर्षक",
                                    subtitle = " SES Brand Title",
                                    checked = showHeader,
                                    onCheckedChange = { showHeader = it }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ToggleItem(
                                    label = "ग्राहक व पत्ता माहिती",
                                    subtitle = "Site & Client Details",
                                    checked = showCustomerDetails,
                                    onCheckedChange = { showCustomerDetails = it }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                ToggleItem(
                                    label = "रूमनुसार कामाचा तपशील",
                                    subtitle = "Detailed Breakdown",
                                    checked = showBreakdown,
                                    onCheckedChange = { showBreakdown = it }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ToggleItem(
                                    label = "स्वाक्षरी ब्लॉक",
                                    subtitle = "Customer Sign Block",
                                    checked = showSignatures,
                                    onCheckedChange = { showSignatures = it }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "डिजिटल प्रिंट पूर्वदृश्य (Virtual A4 Page Review):",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )

                // Scrollable workspace containing the virtual sheet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE5E7EB)) // Matte gray table
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Virtual paper card (resembling the printout page)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(2.dp), // sharp corners like paper
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 1. SES LOGO & BRAND SECTION
                            AnimatedVisibility(
                                visible = showHeader,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Siddhivinayak Electrical Services",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2E1A47),
                                        fontFamily = FontFamily.SansSerif
                                    )
                                    Text(
                                        text = "सिद्धिविनायक इलेक्ट्रिकल सर्व्हिसेस | Gold Standard Works",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 9.sp,
                                        color = Color(0xFF6750A4)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .background(Color(0xFF6750A4))
                                    )
                                }
                            }

                            // 2. CLIENT & INVOICE META
                            AnimatedVisibility(
                                visible = showCustomerDetails,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text("CUSTOMER DETAILS:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text("Client: ${bill.customerName}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("Phone: +91 ${bill.customerPhone}", fontSize = 9.sp, color = Color.DarkGray)
                                            Text("Site Address: ${if (bill.siteAddress.isBlank()) "N/A" else bill.siteAddress}", fontSize = 9.sp, color = Color.DarkGray)
                                        }

                                        Column(modifier = Modifier.weight(0.8f), horizontalAlignment = Alignment.End) {
                                            Text("BILL INFO:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text("Invoice No: #SE-${1000 + bill.id}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text("Date: ${bill.date}", fontSize = 9.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE5E7EB))
                                    )
                                }
                            }

                            // 3. FITTINGS / WORK DETAILS
                            Column {
                                Text(
                                    text = "SERVICES & POINT DETAILS (तपशील):",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                if (showBreakdown) {
                                    // Row Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF9FAFB))
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Text("Points Description", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("Qty", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                        Text("Rate", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp), textAlign = TextAlign.End)
                                        Text("Total", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)
                                    }

                                    if (roomsList.isEmpty()) {
                                        Text("No electrical fittings added.", fontSize = 9.sp, color = Color.Gray)
                                    } else {
                                        roomsList.forEach { room ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            // Room divider badge
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF3E8FF))
                                                    .padding(2.dp)
                                            ) {
                                                Text("ROOM: ${room.name.uppercase()}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47))
                                            }

                                            room.items.forEach { item ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp, horizontal = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(item.type, fontSize = 8.sp, modifier = Modifier.weight(1f))
                                                    Text(item.quantity.toString(), fontSize = 8.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                                    Text("₹${String.format("%.0f", item.rate)}", fontSize = 8.sp, modifier = Modifier.width(45.dp), textAlign = TextAlign.End)
                                                    Text("₹${String.format("%.0f", item.total)}", fontSize = 8.sp, modifier = Modifier.width(55.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Compact list summary
                                    val totalCount = roomsList.flatMap { it.items }.sumOf { it.quantity }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF9FAFB))
                                            .padding(6.dp)
                                            .border(0.5.dp, Color(0xFFE5E7EB))
                                    ) {
                                        Text(
                                            "कामाचा संक्षिप्त गोषवारा (Section Hidden): रूमनुसार तपशील पत्रकातून वगळला आहे. एकूण ${roomsList.size} खोल्यांमधील $totalCount पॉइंट्सचे बिल जोडले आहे.",
                                            fontSize = 8.5.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFE5E7EB))
                                )
                            }

                            // 4. TOTALS BLOCK (Dynamic based on calculation)
                            val totalSubtotal = remember(roomsList) { roomsList.flatMap { it.items }.sumOf { it.total } }
                            val isGst = bill.totalAmount > totalSubtotal

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Points Subtotal (उपएकूण):", fontSize = 9.sp)
                                    Text("₹${String.format("%.2f", totalSubtotal)}", fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    if (isGst) {
                                        Text("GST 18% inclusive (जीएसटी समावेश):", fontSize = 9.sp)
                                        val gstVal = totalSubtotal * 0.18
                                        Text("₹${String.format("%.2f", gstVal)}", fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                    } else {
                                        Text("GST Mode:", fontSize = 9.sp)
                                        Text("Free / Service Tax Exempted", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Advance Paid (जमा रक्कम):", fontSize = 9.sp)
                                    Text("₹${String.format("%.2f", bill.advanceAmount)}", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GRAND TOTAL (एकूण देय):", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    Text("₹${String.format("%.2f", bill.totalAmount)}", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFEBEE))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("उर्वरित बाकी (BALANCE DUE):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    val balanceVal = bill.totalAmount - bill.advanceAmount
                                    Text("₹${String.format("%.2f", balanceVal)}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFC62828))
                                }
                            }

                            // 5. SIGNATURE SPACES
                            AnimatedVisibility(
                                visible = showSignatures,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE5E7EB))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Customer Signature (ग्राहकाची सही)", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text("✓ Signed Digitally / On File", fontSize = 8.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                                        }

                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                            Text("Authorized Signature (सिद्धिविनायक)", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text("For Siddhivinayak Electricals", fontSize = 8.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }

                            // Professional Footer Note
                            Text(
                                text = "This is a computer generated invoice powered by Siddhivinayak Electrical Services Toolchain.",
                                fontSize = 6.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleItem(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(subtitle, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.graphicsLayer(scaleX = 0.85f, scaleY = 0.85f)
            )
        }
    }
}

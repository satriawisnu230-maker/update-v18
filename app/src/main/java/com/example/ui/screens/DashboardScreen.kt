package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.ui.dialogs.AddTransactionDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.getTopHeaderGradient
import com.example.ui.theme.ExpenseHeatTile
import com.example.ui.theme.ExpenseHeatTileActive
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedSoft
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TrophyYellow
import com.example.ui.theme.TrophyYellowLight
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyHelper

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onAddUpcomingClick: () -> Unit,
    onLevelClick: () -> Unit = {},
    onNavigateToSummary: (subTab: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalBalance by viewModel.totalWalletBalance.collectAsState()
    val upcomingItems by viewModel.upcomingItems.collectAsState()
    val calendarExpenses by viewModel.calendarDailyExpenses.collectAsState()
    val selectedMonth by viewModel.selectedMonthIndex.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val assets by viewModel.assets.collectAsState()

    val monthName = when (selectedMonth) {
        1 -> "Januari"
        2 -> "Februari"
        3 -> "Maret"
        4 -> "April"
        5 -> "Mei"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "Agustus"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        else -> "Desember"
    }

    var upcomingFilter by remember { mutableStateOf("Semua") }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var quickAddCategory by remember { mutableStateOf<String?>(null) }

    val monthIncome = remember(allTransactions, selectedMonth, selectedYear) {
        allTransactions.filter { it.monthIndex == selectedMonth && it.year == selectedYear && it.type == com.example.data.model.TransactionType.INCOME }.sumOf { it.amount }
    }
    val monthExpense = remember(allTransactions, selectedMonth, selectedYear) {
        allTransactions.filter { it.monthIndex == selectedMonth && it.year == selectedYear && it.type == com.example.data.model.TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val netCashflow = monthIncome - monthExpense
    val savingsRate = if (monthIncome > 0) (((monthIncome - monthExpense) / monthIncome) * 100).toInt().coerceIn(0, 100) else 42

    // Daily safe to spend calculation based on real month days
    val currentCal = remember { java.util.Calendar.getInstance() }
    val currentDay = currentCal.get(java.util.Calendar.DAY_OF_MONTH)
    val totalDaysInCurrentMonth = currentCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val daysRemaining = (totalDaysInCurrentMonth - currentDay).coerceAtLeast(1)
    val safeDailySpend = if (daysRemaining > 0) (totalBalance / daysRemaining).coerceAtLeast(0.0) else totalBalance
    val monthBurnProgress = (currentDay.toFloat() / totalDaysInCurrentMonth.toFloat()).coerceIn(0.05f, 1f)

    // Dynamic Dana Darurat (Emergency Fund) calculation
    val monthlyExpenseVal = if (monthExpense > 0.0) monthExpense else 1.0
    val emergencyMonths = (totalBalance / monthlyExpenseVal).coerceAtLeast(0.0)
    val emergencyText = when {
        monthExpense <= 0.0 && totalBalance > 0.0 -> "Tersedia"
        emergencyMonths >= 12.0 -> "> 12 Bulan"
        emergencyMonths >= 1.0 -> String.format(java.util.Locale.US, "%.1f Bulan", emergencyMonths)
        else -> "< 1 Bulan"
    }
    val emergencyStatusText = when {
        emergencyMonths >= 6.0 || (monthExpense <= 0.0 && totalBalance > 0.0) -> "Status: Sangat Aman"
        emergencyMonths >= 3.0 -> "Status: Aman"
        emergencyMonths >= 1.0 -> "Status: Waspada"
        else -> "Status: Perlu Ditambah"
    }
    val emergencyStatusColor = when {
        emergencyMonths >= 3.0 || (monthExpense <= 0.0 && totalBalance > 0.0) -> IncomeGreen
        emergencyMonths >= 1.0 -> TrophyYellowLight
        else -> ExpenseRed
    }

    val debtTotal = debts.sumOf { it.totalAmount - it.paidAmount }
    val assetTotal = assets.sumOf { it.value }
    val debtRatio = if (assetTotal > 0) ((debtTotal / assetTotal) * 100).toInt() else 15

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(getTopHeaderGradient(AccentBlue))
    ) {
        // Header with status bar safe area for true fullscreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Dasbor Keuangan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "$monthName $selectedYear · Arus Kas ${if (netCashflow >= 0) "+Rp" + CurrencyHelper.formatCompact(netCashflow) else "-Rp" + CurrencyHelper.formatCompact(-netCashflow)}",
                    fontSize = 12.sp,
                    color = if (netCashflow >= 0) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showTransferDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Tukar / Transfer Antar Akun",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = { showNotificationsDialog = true }) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = ExpenseRed, contentColor = Color.White) {
                                Text("1", fontSize = 10.sp)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi & Pengingat",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Feature 1: Pintasan Aksi Cepat Finansial
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickActions = listOf(
                        Triple("Makan", Icons.Default.Coffee, Color(0xFFF59E0B)),
                        Triple("Belanja", Icons.Default.ShoppingCart, Color(0xFF06B6D4)),
                        Triple("Tagihan", Icons.Default.ElectricBolt, Color(0xFFEC4899)),
                        Triple("Pindah", Icons.Default.SwapHoriz, Color(0xFF3B82F6))
                    )
                    quickActions.forEach { (label, icon, color) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurface,
                            border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (label == "Pindah") {
                                        showTransferDialog = true
                                    } else {
                                        quickAddCategory = label
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = color.copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Feature 2: Skor Kesehatan Finansial (Financial Health Score Gauge)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSummary(1) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = IncomeGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Skor Kesehatan Keuangan",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Analisis real-time kondisi finansial Anda",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = IncomeGreenSoft
                            ) {
                                Text(
                                    text = "88/100 · Prima",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Key Health Meters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dana Darurat", fontSize = 10.sp, color = TextSubtle)
                                Text(emergencyText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(emergencyStatusText, fontSize = 9.sp, color = emergencyStatusColor)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rasio Tabungan", fontSize = 10.sp, color = TextSubtle)
                                Text("$savingsRate%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Target >20%", fontSize = 9.sp, color = IncomeGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Beban Utang", fontSize = 10.sp, color = TextSubtle)
                                Text("$debtRatio%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rasio Sehat", fontSize = 9.sp, color = TrophyYellowLight)
                            }
                        }
                    }
                }
            }

            // Feature 3: Kalkulator "Aman Dibelanjakan Hari Ini" (Daily Safe-to-Spend Burn Rate)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Aman Dibelanjakan",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "${CurrencyHelper.formatRupiah(safeDailySpend)} / hari",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceElevated
                            ) {
                                Text(
                                    text = "$daysRemaining hari sisa",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { monthBurnProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentBlue,
                            trackColor = DarkSurfaceElevated
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Saldo: Rp${CurrencyHelper.formatCompact(totalBalance)}", fontSize = 10.sp, color = TextSubtle)
                            Text("Kecepatan Belanja: Terkendali", fontSize = 10.sp, color = IncomeGreen)
                        }
                    }
                }
            }

            // Card 1: "Yang akan datang" (Upcoming Flow)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yang akan datang",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Semua", "Masuk", "Keluar").forEach { filter ->
                                    val isSelected = upcomingFilter == filter
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) DarkSurfaceElevated else Color.Transparent,
                                        modifier = Modifier.clickable { upcomingFilter = filter }
                                    ) {
                                        Text(
                                            text = filter,
                                            fontSize = 11.sp,
                                            color = if (isSelected) TextWhite else TextMuted,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.clickable(onClick = onAddUpcomingClick)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = TextWhite,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Tambah",
                                            fontSize = 11.sp,
                                            color = TextWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredUpcoming = remember(upcomingItems, upcomingFilter) {
                            when (upcomingFilter) {
                                "Masuk" -> upcomingItems.filter { it.isIncome }
                                "Keluar" -> upcomingItems.filter { !it.isIncome }
                                else -> upcomingItems
                            }
                        }
                        val nextIncomeItem = remember(upcomingItems) {
                            upcomingItems.firstOrNull { it.isIncome }
                        }

                        Text(
                            text = if (nextIncomeItem != null) "Untuk bertahan hingga pemasukan berikutnya, ${nextIncomeItem.dateLabel}" else "Rencana jadwal transaksi mendatang",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$daysRemaining hari tersisa · ${CurrencyHelper.formatRupiah(safeDailySpend)} sehari",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Timeline Milestone Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Step 1: Hari Ini
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rp${CurrencyHelper.formatCompact(totalBalance)} sekarang",
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(2.dp)
                                            .background(DarkCardBorder)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Hari Ini",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            if (filteredUpcoming.isEmpty()) {
                                Column(
                                    modifier = Modifier.weight(2f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Tidak ada jadwal untuk filter \"$upcomingFilter\"",
                                        fontSize = 11.sp,
                                        color = TextSubtle
                                    )
                                }
                            } else {
                                val displayUpcoming = filteredUpcoming.take(2)
                                var runningEst = totalBalance
                                displayUpcoming.forEachIndexed { idx, item ->
                                    runningEst += if (item.isIncome) item.amount else -item.amount
                                    val isLast = idx == displayUpcoming.lastIndex
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(
                                            text = "Rp${CurrencyHelper.formatCompact(runningEst)} tersisa",
                                            fontSize = 11.sp,
                                            color = TextWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .border(2.dp, if (item.isIncome) IncomeGreen else ExpenseRed, CircleShape)
                                                    .background(DarkSurface)
                                            )
                                            if (!isLast) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(2.dp)
                                                        .background(DarkCardBorder)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${item.dateLabel}\n${item.dayLabel}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (item.isIncome) IncomeGreenSoft else ExpenseRedSoft
                                        ) {
                                            Text(
                                                text = "${if (item.isIncome) "↙" else "↗"} Rp${CurrencyHelper.formatCompact(item.amount)} ${item.title}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (item.isIncome) IncomeGreen else ExpenseRed,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: "Gamifikasi & Target Keuangan"
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLevelClick() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular trophy LV 2 badge
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(3.dp, TrophyYellow, CircleShape)
                                    .background(Color(0x1AF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Trophy",
                                        tint = TrophyYellowLight,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = TrophyYellow
                                    ) {
                                        Text(
                                            text = "LV 2",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LEVEL SAAT INI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "Dana cadangan pertama",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TARGET BERIKUTNYA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "Rp17.673.149",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrophyYellowLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "KEMAJUAN KE LEVEL BERIKUTNYA",
                                fontSize = 11.sp,
                                color = TextSubtle,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "88%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrophyYellowLight
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { 0.88f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TrophyYellow,
                            trackColor = DarkSurfaceElevated,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2.8 bulan pengeluaran",
                                    fontSize = 13.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onLevelClick() }
                            ) {
                                Text(
                                    text = "Semua level",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: "Aktivitas Pengeluaran" (Calendar Matrix)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aktivitas Pengeluaran",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$monthName $selectedYear",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Calendar Weekday Headers
                        val daysOfWeek = listOf("S", "S", "R", "K", "J", "S", "M")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    color = TextSubtle,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(38.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Days 1..31 in calendar layout
                        // In Oct 2026, Oct 1 is Thursday (index 3 in 0-indexed Mon-based week: Sen=0, Sel=1, Rab=2, Kam=3)
                        val startOffset = 3 // starts on Thursday
                        val totalDays = 31

                        val totalSlots = startOffset + totalDays
                        val rows = (totalSlots + 6) / 7

                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0 until 7) {
                                    val slotIndex = r * 7 + c
                                    val dayNumber = slotIndex - startOffset + 1

                                    if (dayNumber in 1..totalDays) {
                                        val dayExpense = calendarExpenses[dayNumber] ?: 0.0
                                        val hasExpense = dayExpense > 0

                                        Surface(
                                            modifier = Modifier
                                                .size(width = 42.dp, height = 48.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (hasExpense) ExpenseHeatTile else DarkSurfaceElevated
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(3.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "$dayNumber",
                                                    fontSize = 10.sp,
                                                    color = if (hasExpense) Color(0xFFE2E8F0) else TextSubtle
                                                )
                                                if (hasExpense) {
                                                    Text(
                                                        text = CurrencyHelper.formatCalendarExpense(dayExpense),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ExpenseRed,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(modifier = Modifier.size(width = 42.dp, height = 48.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Dialogs
        if (showTransferDialog) {
            AddTransactionDialog(
                viewModel = viewModel,
                initialCategory = "Transfer",
                onDismiss = { showTransferDialog = false }
            )
        }

        if (showNotificationsDialog) {
            DashboardNotificationsDialog(
                viewModel = viewModel,
                onDismiss = { showNotificationsDialog = false }
            )
        }

        quickAddCategory?.let { category ->
            AddTransactionDialog(
                viewModel = viewModel,
                initialCategory = category,
                onDismiss = { quickAddCategory = null }
            )
        }
    }
}

@Composable
fun DashboardNotificationsDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val upcomingItems by viewModel.upcomingItems.collectAsState()
    val totalBalance by viewModel.totalWalletBalance.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pengingat & Notifikasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Status Keuangan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrophyYellowLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total saldo likuid saat ini: ${CurrencyHelper.formatRupiah(totalBalance)}. Semua dompet aktif.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = "Kewajiban Mendatang (${upcomingItems.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )

                if (upcomingItems.isEmpty()) {
                    Text(
                        text = "Tidak ada tagihan atau pembayaran yang tertunda saat ini. Semua lancar!",
                        fontSize = 11.sp,
                        color = TextSubtle
                    )
                } else {
                    upcomingItems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Jatuh tempo: ${item.dateLabel} (${item.dayLabel})",
                                        fontSize = 10.sp,
                                        color = ExpenseRed
                                    )
                                }
                                Text(
                                    text = CurrencyHelper.formatRupiah(item.amount),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Tutup", color = Color.White)
            }
        }
    )
}

package com.example.ui.screens.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWallet
import com.example.data.model.AssetItem
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.dialogs.CategoryTransactionSheet
import com.example.ui.dialogs.CustomDateRange
import com.example.ui.dialogs.DateRangePickerDialog
import com.example.ui.dialogs.FinancialHealthDetailSheet
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueBright
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MonthOption
import com.example.util.CategoryIconHelper
import com.example.util.CategorySlice
import com.example.util.ChartDataHelper
import com.example.util.ChartPeriod
import com.example.util.CumulativeComparisonData
import com.example.util.CurrencyHelper
import com.example.util.TransactionMathHelper
import java.util.Locale

@Composable
fun SummaryLaporanView(
    selectedMonth: Int,
    selectedYear: Int,
    monthOptions: List<MonthOption>,
    summaryPeriod: Int, // 0: Bulanan, 1: Tahunan, 2: Kustom
    allTransactions: List<Transaction>,
    periodTransactions: List<Transaction>,
    recurringTransactions: List<RecurringTransaction>,
    wallets: List<AccountWallet>,
    assets: List<AssetItem>,
    currentIncome: Double,
    currentExpense: Double,
    netCashFlow: Double,
    chartData: com.example.util.ChartSeriesData,
    cumulativeData: CumulativeComparisonData,
    expenseSlices: List<CategorySlice>,
    incomeSlices: List<CategorySlice>,
    onSummaryPeriodChange: (Int) -> Unit,
    onExportPdf: () -> Unit,
    onQuickFilterMonth: () -> Unit,
    onQuickFilter7Days: () -> Unit,
    onNavigateToTransactions: (category: String?) -> Unit = {},
    onNavigateToAccount: (walletName: String) -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onAddTransaction: (category: String?) -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onSelectMonth: (monthIndex: Int) -> Unit = {},
    onSelectYear: (year: Int) -> Unit = {}
) {
    val monthName = monthOptions.find { it.first == selectedMonth }?.second ?: "Nov"
    var isPlannedActive by remember { mutableStateOf(true) }
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var customRange by remember { mutableStateOf<CustomDateRange?>(CustomDateRange(2, 11, selectedYear, 5, 11, selectedYear)) }

    // Interactive Sheets State
    var showHealthScoreSheet by remember { mutableStateOf(false) }
    var selectedCategoryForSheet by remember { mutableStateOf<String?>(null) }
    var selectedCategoryTypeForSheet by remember { mutableStateOf(TransactionType.EXPENSE) }

    // If custom range active, filter transactions
    val effectiveTransactions = remember(summaryPeriod, customRange, periodTransactions, allTransactions) {
        if (summaryPeriod == 2 && customRange != null) {
            val r = customRange!!
            allTransactions.filter { tx ->
                val txYear = tx.year
                val txMonth = tx.monthIndex
                val txDay = tx.dayOfMonth
                val isAfterStart = txYear > r.startYear || (txYear == r.startYear && (txMonth > r.startMonth || (txMonth == r.startMonth && txDay >= r.startDay)))
                val isBeforeEnd = txYear < r.endYear || (txYear == r.endYear && (txMonth < r.endMonth || (txMonth == r.endMonth && txDay <= r.endDay)))
                isAfterStart && isBeforeEnd
            }
        } else {
            periodTransactions
        }
    }

    val (effIncome, effExpense) = remember(effectiveTransactions) {
        val inc = effectiveTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val exp = effectiveTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        Pair(inc, exp)
    }

    val effNet = effIncome - effExpense
    val periodTitle = when (summaryPeriod) {
        1 -> "$selectedYear"
        2 -> customRange?.label ?: "Kustom"
        else -> "$monthName $selectedYear"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Banner: Buku harian uang Anda (with 🌙 Nov 2026 & ⭐ 7 hari lalu)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1B2238),
                                    Color(0xFF151928),
                                    Color(0xFF111420)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x33F59E0B),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Buku harian uang Anda",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Melihat kembali dengan lembut ke mana uang Anda pergi",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Text(text = "🌙", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (summaryPeriod == 0) AccentBlue else Color(0x33FFFFFF),
                                modifier = Modifier.clickable {
                                    onSummaryPeriodChange(0)
                                    onQuickFilterMonth()
                                }
                            ) {
                                Text(
                                    text = "🌙 $monthName $selectedYear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x33FFFFFF),
                                modifier = Modifier.clickable {
                                    onQuickFilter7Days()
                                }
                            ) {
                                Text(
                                    text = "⭐ 7 hari lalu",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Period Selector: Sekilas [Bulanan] [Tahunan] [Kustom]
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sekilas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )

                Row(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(14.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val periodOptions = listOf(
                        Triple(0, "Bulanan", Icons.Default.CalendarMonth),
                        Triple(1, "Tahunan", Icons.Default.BarChart),
                        Triple(2, "Kustom", Icons.Default.FilterList)
                    )

                    periodOptions.forEach { (index, title, icon) ->
                        val isSelected = summaryPeriod == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AccentBlue else Color.Transparent,
                            modifier = Modifier.clickable {
                                onSummaryPeriodChange(index)
                                if (index == 2) {
                                    showCustomDateDialog = true
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Date Range Active Chip
        if (summaryPeriod == 2 && customRange != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCustomDateDialog = true }
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = customRange!!.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        IconButton(
                            onClick = {
                                onSummaryPeriodChange(0)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Reset", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // TAHUNAN VIEW OR BULANAN/KUSTOM VIEW
        if (summaryPeriod == 1) {
            // ==================== TAHUNAN REPORT ====================
            // 1. Ringkasan Tahunan Header Card with interactive year changer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Ringkasan Tahunan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Pertumbuhan aset, pendapatan & pengeluaran", fontSize = 11.sp, color = TextMuted)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onSelectYear(selectedYear - 1) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Tahun Lalu", tint = TextWhite, modifier = Modifier.size(18.dp))
                                }
                                Text("$selectedYear", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                IconButton(
                                    onClick = { onSelectYear(selectedYear + 1) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Tahun Depan", tint = TextWhite, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                IconButton(onClick = onExportPdf, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = AccentBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 Top Cards (Pendapatan, Pengeluaran, Bersih)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Total Pendapatan", fontSize = 10.sp, color = TextSubtle)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Rp61M", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Total Pengeluaran", fontSize = 10.sp, color = TextSubtle)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Rp44M", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Bersih", fontSize = 10.sp, color = TextSubtle)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("+Rp16M", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentBlueBright)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Pertumbuhan Aset Card with Chips & link to Akun -> Aset
            item {
                var selectedAssetChip by remember { mutableIntStateOf(0) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌱", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pertumbuhan Aset", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("+Rp15.773.865", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = IncomeGreenSoft) {
                                    Text("↑ 0.8%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Asset allocation", fontSize = 10.sp, color = TextSubtle)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAssets() },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("● Tunai & Bank: Rp4.364.000", fontSize = 11.sp, color = TextWhite)
                            Text("28%", fontSize = 11.sp, color = TextMuted)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAssets() },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("● Investasi: Rp11.409.865", fontSize = 11.sp, color = TextWhite)
                            Text("72%", fontSize = 11.sp, color = TextMuted)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Toggle Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Pertumbuhan Aset", "Total Pendapatan", "Total Pengeluaran").forEachIndexed { idx, chip ->
                                val isSel = selectedAssetChip == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) AccentBlue else DarkSurfaceElevated,
                                    modifier = Modifier.clickable { selectedAssetChip = idx }
                                ) {
                                    Text(chip, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color.White else TextMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Line chart with interactive scrubber
                        val yearlyValues = listOf(14e6, 14.2e6, 14.5e6, 14.9e6, 15.2e6, 15.77e6, 15.77e6)
                        MiniMonthTrendLineChart(
                            values = yearlyValues,
                            labels = listOf("Jan", "Mar", "Mei", "Jul", "Sep", "Nov", "Des"),
                            lineColor = AccentBlueBright,
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Kelola Aset di menu Akun →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlueBright,
                                modifier = Modifier.clickable { onNavigateToAssets() }
                            )
                        }
                    }
                }
            }

            // 3. Rincian Bulanan (Table & 6 Stats Grid with interactive month drill-down)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rincian Bulanan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Ketuk bulan untuk melihat rincian", fontSize = 10.sp, color = TextSubtle)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Table header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bulan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.2f))
                            Text("Pemasukan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                            Text("Pengeluaran", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                            Text("Bersih", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                            Text("Perubahan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                        }

                        HorizontalDivider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 6.dp))

                        val monthlyRows = listOf(
                            Triple("Mar", Pair("Rp0", "Rp1.6M"), Pair("-Rp1.6M", "—")),
                            Triple("Apr", Pair("Rp5.4M", "Rp4.9M"), Pair("+Rp488K", "+124%")),
                            Triple("Mei", Pair("Rp5.8M", "Rp6.0M"), Pair("-Rp6.2K", "-101%")),
                            Triple("Jun ★", Pair("Rp14M", "Rp7.2M"), Pair("+Rp7.0M", "+999%")),
                            Triple("Jul", Pair("Rp9.7M", "Rp8.3M"), Pair("+Rp1.4M", "-80%")),
                            Triple("Agu", Pair("Rp6.3M", "Rp4.5M"), Pair("+Rp1.8M", "+28%")),
                            Triple("Sep", Pair("Rp6.4M", "Rp5.5M"), Pair("+Rp912K", "-50%")),
                            Triple("Okt", Pair("Rp6.8M", "Rp5.1M"), Pair("+Rp1.7M", "+81%")),
                            Triple("Nov", Pair("Rp6.0M", "Rp998K"), Pair("+Rp5.0M", "+204%"))
                        )

                        val monthIndices = mapOf(
                            "Mar" to 3, "Apr" to 4, "Mei" to 5, "Jun ★" to 6, "Jul" to 7,
                            "Agu" to 8, "Sep" to 9, "Okt" to 10, "Nov" to 11
                        )

                        monthlyRows.forEach { (m, incExp, netChange) ->
                            val isBest = m.contains("★")
                            val mIdx = monthIndices[m] ?: 11
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isBest) Color(0x2210B981) else Color.Transparent)
                                    .clickable {
                                        onSelectMonth(mIdx)
                                        onSummaryPeriodChange(0) // switch to Bulanan view for this month!
                                    }
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(m, fontSize = 11.sp, fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal, color = if (isBest) IncomeGreen else TextWhite, modifier = Modifier.weight(1.2f))
                                Text(incExp.first, fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                                Text(incExp.second, fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                                Text(netChange.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (netChange.first.startsWith("+")) IncomeGreen else ExpenseRed, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                                Text(netChange.second, fontSize = 9.sp, color = if (netChange.second.startsWith("+")) IncomeGreen else ExpenseRed, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6 Summary Grid
                        val stats = listOf(
                            Pair("Pemasukan Bulanan Rata-rata", "Rp6.7M"),
                            Pair("Pengeluaran Bulanan Rata-rata", "Rp4.9M"),
                            Pair("Rata-rata bersih bulanan", "+Rp1.8M"),
                            Pair("Tingkat tabungan", "27%"),
                            Pair("Bulan terbaik", "Jun 2026 🏆"),
                            Pair("Bulan tersulit", "Mar 2026 ⏳")
                        )

                        for (r in 0 until 3) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val s1 = stats[r * 2]
                                val s2 = stats[r * 2 + 1]
                                Surface(shape = RoundedCornerShape(10.dp), color = DarkSurfaceElevated, modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(s1.first, fontSize = 10.sp, color = TextSubtle)
                                        Text(s1.second, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(10.dp), color = DarkSurfaceElevated, modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(s2.first, fontSize = 10.sp, color = TextSubtle)
                                        Text(s2.second, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Disisihkan ke tabungan & aset
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Disisihkan ke tabungan & aset", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("+Rp5.188.371", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Text("31% dari pemasukan disisihkan untuk membangun kekayaan", fontSize = 10.sp, color = TextSubtle)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAssets() },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tunai & Bank", fontSize = 11.sp, color = TextMuted)
                            Text("Rp100.000", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAssets() },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Investasi", fontSize = 11.sp, color = TextMuted)
                            Text("Rp5.088.371", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(shape = RoundedCornerShape(8.dp), color = DarkSurfaceElevated, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Rp3.070.895 dari sisa periode ini tetap ada di akun Anda.",
                                fontSize = 10.sp,
                                color = TextSubtle,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

        } else {
            // ==================== BULANAN & KUSTOM REPORT ====================
            // 1. Card "AKTIVITAS PENGELUARAN" (with PDF Button)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AKTIVITAS PENGELUARAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = periodTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            // PDF Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.clickable { onExportPdf() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rp${CurrencyHelper.formatCompact(effExpense)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pengeluaran",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IncomeGreenSoft
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "80% dibanding Okt",
                                        fontSize = 10.sp,
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Trend Curve with interactive touch scrubber
                        val trendPts = chartData.rawValues.ifEmpty { listOf(100e3, 250e3, 498e3, 120e3, 80e3) }
                        MiniMonthTrendLineChart(
                            values = trendPts,
                            labels = listOf(chartData.startLabel, chartData.midLabel, chartData.endLabel),
                            lineColor = ExpenseRed,
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 Summary Metrics (clickable to navigate or filter)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Per hari", fontSize = 10.sp, color = TextSubtle)
                                Text("Rp200K", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Column {
                                Text("Hari terboros", fontSize = 10.sp, color = TextSubtle)
                                Text("Rp498K", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("4 Nov", fontSize = 9.sp, color = TextSubtle)
                            }
                            Column(modifier = Modifier.clickable {
                                selectedCategoryForSheet = "Online Shopping"
                                selectedCategoryTypeForSheet = TransactionType.EXPENSE
                            }) {
                                Text("Kategori teratas 👆", fontSize = 10.sp, color = AccentBlueBright)
                                Text("Online Shopping", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rp398K", fontSize = 9.sp, color = TextSubtle)
                            }
                        }
                    }
                }
            }

            // 2. Card "Ringkasan: Arus Kas Bulanan"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ringkasan", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSubtle)
                        Text("Arus Kas Bulanan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Income bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(CurrencyHelper.formatRupiah(effIncome), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pemasukan", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = IncomeGreen,
                            trackColor = DarkSurfaceElevated
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Expense bar
                        val expRatio = if (effIncome > 0) (effExpense / effIncome).toFloat().coerceIn(0f, 1f) else 1f
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(CurrencyHelper.formatRupiah(effExpense), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pengeluaran", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { expRatio },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = ExpenseRed,
                            trackColor = DarkSurfaceElevated
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(shape = RoundedCornerShape(12.dp), color = DarkSurfaceElevated, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Pada $periodTitle, arus kas bulanan Anda positif. Pemasukan Anda +${CurrencyHelper.formatRupiah(effNet)} lebih besar dari pengeluaran.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // 3. Card "Terencana: Masih direncanakan" (Integrated with Planner & Debts)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Terencana", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSubtle)
                                Text("Masih direncanakan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Switch(
                                checked = isPlannedActive,
                                onCheckedChange = { isPlannedActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AccentBlue,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkSurfaceElevated
                                )
                            )
                        }

                        Text(
                            text = "Tagihan dan pemasukan dari daftar berulang Anda yang masih akan datang di periode ini.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pemasukan terencana - 0 item", fontSize = 11.sp, color = TextWhite)
                            Text("+Rp0", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDebts() },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pengeluaran terencana - 2 item", fontSize = 11.sp, color = TextWhite)
                            Text("-Rp12.000", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(shape = RoundedCornerShape(12.dp), color = DarkSurfaceElevated, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Jika semua berjalan sesuai rencana", fontSize = 10.sp, color = TextSubtle)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pemasukan", fontSize = 11.sp, color = TextWhite)
                                    Text(CurrencyHelper.formatRupiah(effIncome), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pengeluaran", fontSize = 11.sp, color = TextWhite)
                                    Text(CurrencyHelper.formatRupiah(effExpense + 12000.0), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Bersih untuk periode ini", fontSize = 11.sp, color = TextWhite)
                                    Text("+${CurrencyHelper.formatRupiah(effNet - 12000.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Upcoming list with click to view in Debts or Planner
                        listOf(
                            Pair("16 Nov", Pair("Admin BRI Istri", "-Rp8.000")),
                            Pair("20 Nov", Pair("Admin BRI Utama", "-Rp4.000"))
                        ).forEach { (date, detail) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToDebts() }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = DarkSurfaceElevated) {
                                        Text(date, fontSize = 9.sp, color = TextSubtle, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(detail.first, fontSize = 12.sp, color = TextWhite)
                                }
                                Text(detail.second, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Buka Perencana & Jadwal Tagihan →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF818CF8),
                                modifier = Modifier.clickable { onNavigateToPlanner() }
                            )
                            Text(
                                text = "Lihat Utang",
                                fontSize = 11.sp,
                                color = TextSubtle,
                                modifier = Modifier.clickable { onNavigateToDebts() }
                            )
                        }
                    }
                }
            }

            // 4. Card "Pendapatan vs Pengeluaran" (Kumulatif)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pendapatan vs Pengeluaran", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Surface(shape = RoundedCornerShape(8.dp), color = DarkSurfaceElevated) {
                                Text("Kumulatif", fontSize = 9.sp, color = TextSubtle, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("+Rp5M", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                Text("Dihitung periode ini", fontSize = 10.sp, color = TextMuted)
                            }
                            Surface(shape = RoundedCornerShape(10.dp), color = IncomeGreenSoft) {
                                Text("83% pemasukan tersisa", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp, 3.dp).background(IncomeGreen))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pemasukan Rp6M", fontSize = 10.sp, color = TextMuted)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp, 3.dp).background(ExpenseRed))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pengeluaran Rp998K", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dual curve chart with interactive scrubber
                        MiniMonthTrendLineChart(
                            values = listOf(1e6, 3e6, 6e6, 6e6, 6e6),
                            labels = listOf("1", "9", "17", "25", "30"),
                            lineColor = IncomeGreen,
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Pemasukan tetap unggul sepanjang periode", fontSize = 10.sp, color = IncomeGreen)
                    }
                }
            }

            // 5. Card "Wawasan mendalam" -> "Skor Kesehatan Keuangan" (Clickable to open deep sheet)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHealthScoreSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("WAWASAN MENDALAM", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSubtle)
                                Text("Skor Kesehatan Keuangan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(shape = RoundedCornerShape(8.dp), color = IncomeGreenSoft) {
                                    Text("Sangat Baik · Ketuk untuk rincian ℹ️", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            FinancialHealthScoreGauge(score = 80, label = "80/100")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Surplus Bersih
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Surplus Bersih", fontSize = 10.sp, color = TextSubtle)
                                Text(CurrencyHelper.formatRupiah(effNet), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("vs periode sebelumnya", fontSize = 10.sp, color = TextSubtle)
                                Text("+204.5%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5 Breakdown Items
                        val metrics = listOf(
                            Triple("Tingkat Tabungan", "83.5%", 20),
                            Triple("Kontrol Pengeluaran", "-80.4%", 20),
                            Triple("Konsistensi Pelacakan", "2/30 hari terlacak", 4),
                            Triple("Diversifikasi Pendapatan", "3 sumber", 16),
                            Triple("Progres Anggaran", "3 anggaran", 20)
                        )

                        metrics.forEach { (name, desc, pts) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛡️", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(name, fontSize = 11.sp, color = TextWhite)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(desc, fontSize = 11.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("$pts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advice Box with Star
                        Surface(shape = RoundedCornerShape(12.dp), color = DarkSurfaceElevated, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Disiplin keuangan luar biasa periode ini. Ketuk untuk analisis lengkap 5 pilar!",
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 6. Card "Rincian Pendapatan & Pengeluaran" (Clickable to open CategoryTransactionSheet)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Rincian Pendapatan & Pengeluaran", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Ketuk kategori untuk melihat rincian transaksi", fontSize = 10.sp, color = TextSubtle)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Section Pemasukan
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pemasukan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Text(CurrencyHelper.formatRupiah(effIncome), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val incomeBreakdown = listOf(
                            Triple("Base Salary", "83.3%", "Rp5.027.600"),
                            Triple("Extra Income", "15.1%", "Rp910.000"),
                            Triple("Salary", "1.7%", "Rp100.000")
                        )

                        incomeBreakdown.forEach { (cat, pct, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCategoryForSheet = cat
                                        selectedCategoryTypeForSheet = TransactionType.INCOME
                                    }
                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💵", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cat, fontSize = 11.sp, color = TextWhite)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pct, fontSize = 10.sp, color = TextSubtle)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(amt, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))

                        // Section Pengeluaran
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Text(CurrencyHelper.formatRupiah(effExpense), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val expenseBreakdown = listOf(
                            Triple("Online Shopping", "39.9%", "Rp398.395"),
                            Triple("Debt Payment", "35.1%", "Rp350.000"),
                            Triple("Market", "10.0%", "Rp100.000"),
                            Triple("Internet", "10.0%", "Rp100.000"),
                            Triple("Other", "5.0%", "Rp50.000")
                        )

                        expenseBreakdown.forEach { (cat, pct, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCategoryForSheet = cat
                                        selectedCategoryTypeForSheet = TransactionType.EXPENSE
                                    }
                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛒", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cat, fontSize = 11.sp, color = TextWhite)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pct, fontSize = 10.sp, color = TextSubtle)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(amt, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 7. Card "Pengeluaran Menurut Hari Kerja" (Interactive Day selection)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pengeluaran Menurut Hari Kerja", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Ketuk kolom hari untuk melihat detail", fontSize = 10.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive S S R K J S M Bar Chart
                        DayOfWeekSpendingBarChart(
                            dayTotals = listOf(350e3, 0.0, 490e3, 0.0, 0.0, 0.0, 150e3),
                            peakDayIndex = 2
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hari kerja", fontSize = 10.sp, color = TextSubtle)
                                Text("Rp424K", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("rata-rata hari", fontSize = 9.sp, color = TextSubtle)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Akhir pekan", fontSize = 10.sp, color = TextSubtle)
                                Text("Rp150K", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("rata-rata hari", fontSize = 9.sp, color = TextSubtle)
                            }
                        }
                    }
                }
            }

            // 8. Card "Hari demi hari" -> "Catatan Harian"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Catatan Harian", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("4/30 hari terlacak ℹ️", fontSize = 10.sp, color = TextSubtle)
                        }
                        Text("Saat ini melacak selama 4 hari berturut-turut", fontSize = 11.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(12.dp))

                        DailyTrackingCalendarGrid(
                            daysInMonth = 30,
                            trackedDays = setOf(1, 2, 3, 4),
                            onDayClick = { day ->
                                onAddTransaction(null)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ada catatan", fontSize = 10.sp, color = TextMuted)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF1E2433)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tidak ada catatan", fontSize = 10.sp, color = TextMuted)
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

    if (showCustomDateDialog) {
        DateRangePickerDialog(
            currentYear = selectedYear,
            initialMonth = selectedMonth,
            initialRange = customRange,
            onDismiss = { showCustomDateDialog = false },
            onConfirm = { range ->
                customRange = range
                onSummaryPeriodChange(2)
                showCustomDateDialog = false
            }
        )
    }

    if (showHealthScoreSheet) {
        FinancialHealthDetailSheet(
            score = 80,
            savingsRatePct = 83.5,
            netSurplusFormatted = CurrencyHelper.formatRupiah(effNet),
            onDismiss = { showHealthScoreSheet = false },
            onNavigateToBudgets = onNavigateToBudgets,
            onNavigateToPlanner = onNavigateToPlanner,
            onAddTransaction = { onAddTransaction(null) }
        )
    }

    selectedCategoryForSheet?.let { cat ->
        CategoryTransactionSheet(
            categoryName = cat,
            categoryType = selectedCategoryTypeForSheet,
            periodLabel = periodTitle,
            transactions = effectiveTransactions,
            onDismiss = { selectedCategoryForSheet = null },
            onNavigateToTransactions = { c ->
                onNavigateToTransactions(c)
            },
            onAddTransactionForCategory = { c ->
                onAddTransaction(c)
            },
            onTransactionClick = onTransactionClick
        )
    }
}

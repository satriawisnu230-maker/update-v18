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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWallet
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.dialogs.CategoryTransactionSheet
import com.example.ui.dialogs.CustomizeStatisticsDialog
import com.example.ui.dialogs.StatCardConfig
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
import com.example.util.CategoryIconHelper
import com.example.util.CategorySlice
import com.example.util.CurrencyHelper
import com.example.util.TransactionMathHelper

@Composable
fun SummaryStatistikView(
    selectedMonth: Int,
    selectedYear: Int,
    allTransactions: List<Transaction>,
    periodTransactions: List<Transaction>,
    recurringTransactions: List<RecurringTransaction>,
    wallets: List<AccountWallet>,
    currentIncome: Double,
    currentExpense: Double,
    expenseSlices: List<CategorySlice>,
    incomeSlices: List<CategorySlice>,
    onNavigateToTransactions: (category: String?) -> Unit = {},
    onNavigateToAccount: (walletName: String) -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onAddTransaction: (category: String?) -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    var showCustomizeDialog by remember { mutableStateOf(false) }
    var isSideBySideTrends by remember { mutableStateOf(true) }
    var selectedCategoryForSheet by remember { mutableStateOf<String?>(null) }
    var selectedCategoryTypeForSheet by remember { mutableStateOf(TransactionType.EXPENSE) }

    val defaultCards = remember {
        listOf(
            StatCardConfig("trends", "Tren Pendapatan & Pengeluaran", "📈", true),
            StatCardConfig("expense_donut", "Ikhtisar Pengeluaran", "🍩", true),
            StatCardConfig("label_expense", "Pengeluaran per label", "🏷️", true),
            StatCardConfig("upcoming", "Yang akan datang", "📅", true),
            StatCardConfig("cashflow", "Arus Kas", "📊", true),
            StatCardConfig("income_donut", "Ikhtisar Pendapatan", "💰", true),
            StatCardConfig("income_summary", "Ringkasan Penghasilan", "🎯", true),
            StatCardConfig("savings_rate", "Rasio menabung", "🪙", true),
            StatCardConfig("daily_average", "Rata-rata harian", "⏳", true),
            StatCardConfig("mom_compare", "Periode ini vs lalu", "⚖️", true),
            StatCardConfig("category_change", "Kategori paling berubah", "🔄", true),
            StatCardConfig("daily_pattern", "Pola harian", "📅", true),
            StatCardConfig("top_expenses", "Pengeluaran terbesar", "🔥", true),
            StatCardConfig("upcoming_bills", "Tagihan mendatang", "🗓️", true),
            StatCardConfig("period_highlights", "Sorotan periode", "✨", true),
            StatCardConfig("money_flow", "Aliran uang", "🔀", true),
            StatCardConfig("fixed_costs", "Biaya tetap", "🔄", true),
            StatCardConfig("cashflow_journey", "Perjalanan arus kas", "📈", true),
            StatCardConfig("account_spending", "Pengeluaran per akun", "💳", true)
        )
    }

    var cardConfigs by remember { mutableStateOf(defaultCards) }
    val isCardVisible: (String) -> Boolean = { id -> cardConfigs.find { it.id == id }?.isVisible != false }

    val netCashFlow = currentIncome - currentExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Customize Button Bar on top
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.clickable { showCustomizeDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sesuaikan", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 1. Tren Pendapatan & Pengeluaran (Side-by-side or stacked)
        if (isCardVisible("trends")) {
            item {
                if (isSideBySideTrends) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left: Tren Pemasukan
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Tren Pemasukan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rata-rata: Rp6.7M/bln", fontSize = 9.sp, color = TextSubtle)
                                Spacer(modifier = Modifier.height(8.dp))
                                MiniMonthTrendLineChart(
                                    values = listOf(5.4e6, 5.8e6, 14.2e6, 9.7e6, 6.8e6, 6.03e6),
                                    labels = listOf("Jun", "Jul", "Agu", "Sep", "Okt", "Nov"),
                                    lineColor = IncomeGreen,
                                    modifier = Modifier.fillMaxWidth().height(65.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Bulan Lalu", fontSize = 9.sp, color = TextSubtle)
                                Text("Rp6.804.800 ↓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Bulan ini", fontSize = 9.sp, color = TextSubtle)
                                        Text("Rp6.037.600", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0x33F43F5E)) {
                                        Text("↓ 11%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ExpenseRed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }

                        // Right: Tren Pengeluaran
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Tren Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rata-rata: Rp5.1M/bln", fontSize = 9.sp, color = TextSubtle)
                                Spacer(modifier = Modifier.height(8.dp))
                                MiniMonthTrendLineChart(
                                    values = listOf(7.2e6, 8.3e6, 4.5e6, 5.5e6, 5.1e6, 998e3),
                                    labels = listOf("Jun", "Jul", "Agu", "Sep", "Okt", "Nov"),
                                    lineColor = ExpenseRed,
                                    modifier = Modifier.fillMaxWidth().height(65.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Bulan Lalu", fontSize = 9.sp, color = TextSubtle)
                                Text("Rp5.097.165 ↓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Bulan ini", fontSize = 9.sp, color = TextSubtle)
                                        Text("Rp998.395", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = IncomeGreenSoft) {
                                        Text("↓ 80%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Card "Pengeluaran" (Donut Chart & List with interactive click)
        if (isCardVisible("expense_donut")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pengeluaran", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Rp998.395", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category items matching video (clickable)
                        val expItems = listOf(
                            Triple("Shopping", "40%", "Rp398.395"),
                            Triple("Debt Payment", "35%", "Rp350.000"),
                            Triple("Groceries", "10%", "Rp100.000"),
                            Triple("Bills & Fees", "10%", "Rp100.000"),
                            Triple("Other", "5%", "Rp50.000")
                        )

                        expItems.forEach { (cat, pct, amt) ->
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
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (cat) {
                                            "Shopping" -> Color(0xFFF43F5E)
                                            "Debt Payment" -> Color(0xFFF59E0B)
                                            "Groceries" -> Color(0xFF10B981)
                                            "Bills & Fees" -> Color(0xFF3B82F6)
                                            else -> Color(0xFF8B5CF6)
                                        }
                                    ) {
                                        Text(pct, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat, fontSize = 12.sp, color = TextWhite)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(amt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Card "Pengeluaran per label"
        if (isCardVisible("label_expense")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pengeluaran per label", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Rp0", fontSize = 14.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak Ada Data", fontSize = 12.sp, color = TextSubtle)
                        }
                    }
                }
            }
        }

        // 4. Card "Yang akan datang" (Integrated with Planner)
        if (isCardVisible("upcoming")) {
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
                            Text("Yang akan datang", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Semua", "Masuk", "Keluar", "+ Tambah").forEachIndexed { i, t ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (i == 0) AccentBlue else DarkSurfaceElevated,
                                        modifier = Modifier.clickable {
                                            if (t == "+ Tambah") onAddTransaction(null)
                                        }
                                    ) {
                                        Text(t, fontSize = 9.sp, color = if (i == 0) Color.White else TextMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Untuk bertahan hingga pemasukan berikutnya, 1 Des",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Text(
                            text = "26 hari tersisa · Rp197.592 sehari",
                            fontSize = 10.sp,
                            color = TextSubtle
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Timeline milestones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rp5.2M sekarang", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Hari Ini", fontSize = 9.sp, color = TextSubtle)
                            }
                            Column(modifier = Modifier.clickable { onNavigateToDebts() }) {
                                Text("Rp5.1M tersisa", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("16 Nov · Admin BRI", fontSize = 9.sp, color = ExpenseRed)
                            }
                            Column {
                                Text("Rp7.7M tersisa", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                Text("1 Des · Gaji Pokok", fontSize = 9.sp, color = IncomeGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Buka Perencana & Jadwal Tagihan →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8),
                            modifier = Modifier.clickable { onNavigateToPlanner() }
                        )
                    }
                }
            }
        }

        // 5. Card "Arus Kas"
        if (isCardVisible("cashflow")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Arus Kas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rata-rata: Rp2.545.398/bln", fontSize = 10.sp, color = TextSubtle)
                            }
                            Text("+5M", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        MiniMonthTrendLineChart(
                            values = listOf(1.6e6, 1.4e6, 870e3, 1.7e6, 5.0e6),
                            labels = listOf("Jun", "Jul", "Agu", "Sep", "Okt", "Nov"),
                            lineColor = IncomeGreen,
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(shape = RoundedCornerShape(8.dp), color = DarkSurfaceElevated, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("➕", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pemasukan melebihi pengeluaran dalam semua 6 bulan (Konsisten di semua periode)", fontSize = 9.sp, color = IncomeGreen)
                            }
                        }
                    }
                }
            }
        }

        // 6. Card "Pemasukan" (Clickable categories)
        if (isCardVisible("income_donut")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pemasukan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Rp6.037.600", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedCategoryForSheet = "Salary"
                                    selectedCategoryTypeForSheet = TransactionType.INCOME
                                }
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = IncomeGreen) {
                                    Text("85%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Salary", fontSize = 12.sp, color = TextWhite)
                            }
                            Text("Rp5.127.600", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedCategoryForSheet = "Extra Income"
                                    selectedCategoryTypeForSheet = TransactionType.INCOME
                                }
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = AccentBlue) {
                                    Text("15%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Extra Income", fontSize = 12.sp, color = TextWhite)
                            }
                            Text("Rp910.000", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                    }
                }
            }
        }

        // 7. Card "Ringkasan Penghasilan"
        if (isCardVisible("income_summary")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ringkasan Penghasilan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FinancialHealthScoreGauge(score = 83, label = "83%", modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Tersisa 83%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rp5.039.205 dari Rp6.037.600", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rata-rata belanja harian", fontSize = 11.sp, color = TextSubtle)
                            Text("Rp199.679 / hari", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Batas aman harian yang tersisa", fontSize = 11.sp, color = TextSubtle)
                            Text("Rp197.592 / hari", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                    }
                }
            }
        }

        // 8. Card "Rasio menabung"
        if (isCardVisible("savings_rate")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rasio menabung", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("83.5%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Text("Target rekomendasi: 20%", fontSize = 10.sp, color = TextSubtle)
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { 0.835f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = IncomeGreen,
                            trackColor = DarkSurfaceElevated
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Luar biasa! Anda menyisihkan lebih dari separuh pendapatan bulan ini.", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        // 9. Card "Rata-rata harian"
        if (isCardVisible("daily_average")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rata-rata harian", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pemasukan", fontSize = 10.sp, color = TextSubtle)
                                Text("Rp1.2M", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                Text("per hari aktif", fontSize = 9.sp, color = TextSubtle)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pengeluaran", fontSize = 10.sp, color = TextSubtle)
                                Text("Rp200K", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                                Text("per hari aktif", fontSize = 9.sp, color = TextSubtle)
                            }
                        }
                    }
                }
            }
        }

        // 10. Card "Periode ini vs lalu"
        if (isCardVisible("mom_compare")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Periode ini vs lalu", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pengeluaran bulan lalu", fontSize = 11.sp, color = TextMuted)
                            Text("Rp5.097.165", fontSize = 11.sp, color = TextWhite)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pengeluaran bulan ini", fontSize = 11.sp, color = TextMuted)
                            Text("Rp998.395", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = IncomeGreenSoft, modifier = Modifier.fillMaxWidth()) {
                            Text("Hemat Rp4.098.770 (-80.4%) dibanding Oktober", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = IncomeGreen, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }

        // 11. Card "Kategori paling berubah" (Clickable to open category sheet)
        if (isCardVisible("category_change")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Kategori paling berubah", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Perubahan terbesar dibanding periode sebelumnya", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            Pair("Other", "-Rp2.842.000"),
                            Pair("Groceries", "-Rp778.250"),
                            Pair("Food & Drink", "-Rp448.001"),
                            Pair("Debt Payment", "-Rp350.000")
                        ).forEach { (cat, diff) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedCategoryForSheet = cat
                                        selectedCategoryTypeForSheet = TransactionType.EXPENSE
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat, fontSize = 12.sp, color = TextWhite)
                                Text(diff, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                        }
                    }
                }
            }
        }

        // 12. Card "Pola harian"
        if (isCardVisible("daily_pattern")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pola harian", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("R adalah hari terborosmu (Rp498.395)", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        DayOfWeekSpendingBarChart(
                            dayTotals = listOf(350e3, 0.0, 498e3, 0.0, 0.0, 0.0, 150e3),
                            peakDayIndex = 2
                        )
                    }
                }
            }
        }

        // 13. Card "Pengeluaran terbesar" (Clickable to open transaction or category)
        if (isCardVisible("top_expenses")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pengeluaran terbesar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            Triple("Shopee Paylater", "4 Nov · 40%", "Rp398.395"),
                            Triple("mbk pinjem", "2 Nov · 35%", "Rp350.000"),
                            Triple("beli minyak dan jajan", "1 Nov · 10%", "Rp100.000"),
                            Triple("Wifi Kumbo", "4 Nov · 10%", "Rp100.000"),
                            Triple("Arisan DWP", "1 Nov · 5%", "Rp50.000")
                        ).forEach { (name, meta, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        onNavigateToTransactions(name)
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                    Text(meta, fontSize = 9.sp, color = TextSubtle)
                                }
                                Text(amt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                            }
                        }
                    }
                }
            }
        }

        // 14. Card "Tagihan mendatang" (Integrated with Planner)
        if (isCardVisible("upcoming_bills")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tagihan mendatang", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("30 hari ke depan", fontSize = 10.sp, color = TextSubtle)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            Triple("16 Nov", "Admin BRI Istri", "Rp8.000"),
                            Triple("20 Nov", "Admin BRI Utama", "Rp4.000"),
                            Triple("1 Des", "Gaji Pokok", "Rp2.684.000")
                        ).forEach { (dt, name, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToDebts() }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = DarkSurfaceElevated) {
                                        Text(dt, fontSize = 9.sp, color = TextSubtle, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(name, fontSize = 12.sp, color = TextWhite)
                                }
                                Text(amt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (name.contains("Gaji")) IncomeGreen else ExpenseRed)
                            }
                        }
                        HorizontalDivider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total jatuh tempo", fontSize = 11.sp, color = TextMuted)
                            Text("Rp2.696.000", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kelola Tagihan di Perencana →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8),
                            modifier = Modifier.clickable { onNavigateToPlanner() }
                        )
                    }
                }
            }
        }

        // 15. Card "Sorotan periode"
        if (isCardVisible("period_highlights")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sorotan periode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pengeluaran Terbesar", fontSize = 9.sp, color = TextSubtle)
                                Text("Online Shopping", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rp398K", fontSize = 8.sp, color = TextSubtle)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tabungan Tersisa", fontSize = 9.sp, color = TextSubtle)
                                Text("83%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                Text("Rp5.039.205", fontSize = 8.sp, color = TextSubtle)
                            }
                        }
                    }
                }
            }
        }

        // 16. Card "Aliran uang" (Sankey visual with clickable category nodes)
        if (isCardVisible("money_flow")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Aliran uang", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(10.dp))
                        SankeyMoneyFlowView(
                            incomeCategories = listOf(Pair("Salary", 5127600.0), Pair("Extra Income", 910000.0)),
                            expenseCategories = listOf(Pair("Shopping", 398395.0), Pair("Debt Payment", 350000.0)),
                            savingsAmount = 5039205.0,
                            onCategoryClick = { catName ->
                                selectedCategoryForSheet = catName
                                selectedCategoryTypeForSheet = if (catName.contains("Income") || catName == "Salary") TransactionType.INCOME else TransactionType.EXPENSE
                            }
                        )
                    }
                }
            }
        }

        // 17. Card "Biaya tetap"
        if (isCardVisible("fixed_costs")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Biaya tetap", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rp12.000", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("0% dari pemasukan periode ini", fontSize = 9.sp, color = TextSubtle)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Admin BRI Istri", fontSize = 11.sp, color = TextWhite)
                            Text("Rp8.000 / bulan", fontSize = 11.sp, color = TextMuted)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Admin BRI Utama", fontSize = 11.sp, color = TextWhite)
                            Text("Rp4.000 / bulan", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // 18. Card "Perjalanan arus kas"
        if (isCardVisible("cashflow_journey")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Perjalanan arus kas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("+Rp5.039.205", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Text("Nilai bersih berjalan selama periode", fontSize = 10.sp, color = TextSubtle)
                        Spacer(modifier = Modifier.height(10.dp))
                        MiniMonthTrendLineChart(
                            values = listOf(0.0, 1e6, 3e6, 5.03e6),
                            labels = listOf("1 Nov", "10 Nov", "20 Nov", "30 Nov"),
                            lineColor = IncomeGreen,
                            modifier = Modifier.fillMaxWidth().height(65.dp)
                        )
                    }
                }
            }
        }

        // 19. Card "Pengeluaran per akun" (Integrated with Akun)
        if (isCardVisible("account_spending")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pengeluaran per akun", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Ketuk untuk buka akun", fontSize = 10.sp, color = TextSubtle)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToAccount("BRI Utama") }
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1E293B)) {
                                    Text("BRI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentBlue, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("BRI Utama", fontSize = 12.sp, color = TextWhite)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = AccentBlue.copy(alpha = 0.2f)) {
                                    Text("90%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentBlue, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rp898.395", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToAccount("BRI Istri") }
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1E293B)) {
                                    Text("BRI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentBlue, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("BRI Istri", fontSize = 12.sp, color = TextWhite)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = AccentBlue.copy(alpha = 0.2f)) {
                                    Text("10%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentBlue, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rp100.000", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
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

    if (showCustomizeDialog) {
        CustomizeStatisticsDialog(
            initialCards = cardConfigs,
            isSideBySideTrends = isSideBySideTrends,
            onDismiss = { showCustomizeDialog = false },
            onSave = { updated, sbs ->
                cardConfigs = updated
                isSideBySideTrends = sbs
            }
        )
    }

    selectedCategoryForSheet?.let { cat ->
        CategoryTransactionSheet(
            categoryName = cat,
            categoryType = selectedCategoryTypeForSheet,
            periodLabel = "November $selectedYear",
            transactions = periodTransactions,
            onDismiss = { selectedCategoryForSheet = null },
            onNavigateToTransactions = { c -> onNavigateToTransactions(c) },
            onAddTransactionForCategory = { c -> onAddTransaction(c) },
            onTransactionClick = onTransactionClick
        )
    }
}

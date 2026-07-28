package com.dopamine.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopamine.app.model.ReportStatus
import com.dopamine.app.model.WeeklyReport
import com.dopamine.app.ui.components.IosButton
import com.dopamine.app.ui.components.IosCard
import com.dopamine.app.ui.components.IosTextField
import com.dopamine.app.ui.components.SegmentedControl
import com.dopamine.app.ui.theme.PrimaryBlue
import com.dopamine.app.ui.theme.StatusError
import com.dopamine.app.ui.theme.StatusSuccess
import com.dopamine.app.ui.theme.StatusWarning
import com.dopamine.app.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboardScreen(viewModel: MainViewModel) {
    val users by viewModel.users.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val isRejectOpen by viewModel.isRejectDialogOpen.collectAsState()
    val rejectReason by viewModel.rejectionReasonInput.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val hazeState = remember { HazeState() }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // --- Liquid Glass Background Glows ---
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-50).dp, y = (-50).dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 50.dp, y = 50.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(
                    state = hazeState,
                    backgroundColor = Color.Black.copy(alpha = 0.2f),
                    tint = Color.White.copy(alpha = 0.15f),
                    blurRadius = 80.dp
                )
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "MODERATÖR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Çıkış",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { viewModel.logout() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            val normalUsers = users.filter { !it.isModerator }
            
            // Sadece bu haftanın raporlarını göster
            val currentWeekReports = reports.filter {
                val reportCal = java.util.Calendar.getInstance().apply { timeInMillis = it.submissionTimestamp }
                val currentCal = java.util.Calendar.getInstance()
                reportCal.get(java.util.Calendar.YEAR) == currentCal.get(java.util.Calendar.YEAR) &&
                reportCal.get(java.util.Calendar.WEEK_OF_YEAR) == currentCal.get(java.util.Calendar.WEEK_OF_YEAR)
            }

            if (selectedTab == 0) {
                // Tab 1: Submitted Reports
                if (currentWeekReports.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bu hafta için henüz gönderilmiş rapor bulunmuyor.",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 100.dp) // padding for segmented control
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        currentWeekReports.forEach { report ->
                            ReportItemCard(
                                report = report,
                                onApprove = { viewModel.approveReport(report.id) },
                                onReject = { viewModel.openRejectDialog(report.id) }
                            )
                        }
                    }
                }
            } else {
                // Tab 2: Normal Users & Nudge List (NO moderators)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 100.dp) // padding for segmented control
                        .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    normalUsers.forEach { user ->
                        val submittedReport = currentWeekReports.find { it.userId == user.id }
                        val isSubmitted = submittedReport != null && submittedReport.status != ReportStatus.REJECTED

                        IosCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 0.dp,
                            backgroundColor = Color(0xFF111111),
                            borderColor = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.fullName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = if (user.district.isNotEmpty()) "@${user.username} • ${user.district}" else "@${user.username}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isSubmitted) StatusSuccess else StatusWarning)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isSubmitted) "Rapor Dolduruldu" else "Rapor Bekliyor",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSubmitted) StatusSuccess else StatusWarning
                                        )
                                    }
                                }

                                if (!isSubmitted) {
                                    IosButton(
                                        text = "Dürt",
                                        icon = Icons.Default.NotificationsActive,
                                        onClick = { viewModel.nudgeUser(user.id) },
                                        modifier = Modifier.width(100.dp),
                                        height = 42.dp,
                                        backgroundColor = PrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } // End of inner else
        } // End of Column

        SegmentedControl(
            items = listOf("Bekleyenler", "Kullanıcılar"),
            selectedIndex = selectedTab,
            onSegmentSelected = { selectedTab = it },
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
        )
    } // End of Box

    // Rejection Reason Modal Dialog
    if (isRejectOpen) {
        BasicAlertDialog(
            onDismissRequest = { viewModel.isRejectDialogOpen.value = false }
        ) {
            IosCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Raporu Reddet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusError
                    )

                    Text(
                        text = "Lütfen kullanıcıya iletilecek red nedenini yazın:",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    IosTextField(
                        value = rejectReason,
                        onValueChange = { viewModel.rejectionReasonInput.value = it },
                        label = "Red Nedeni",
                        placeholder = "Örn: Saha katılanlarının detayını eksik girmişsiniz...",
                        singleLine = false,
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IosButton(
                        text = "Reddet ve Bildirim Gönder",
                        backgroundColor = StatusError,
                        onClick = { viewModel.confirmRejectReport() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "İptal",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { viewModel.isRejectDialogOpen.value = false }
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportItemCard(
    report: WeeklyReport,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
    val formattedDate = dateFormat.format(Date(report.submissionTimestamp))

    IosCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        backgroundColor = Color(0xFF111111),
        borderColor = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = report.userFullName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (report.district.isNotEmpty()) "@${report.username} • ${report.district} • $formattedDate" else "@${report.username} • $formattedDate",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(status = report.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    TimeStatusBadge(isOnTime = report.isSubmittedOnTime)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Field stats summary grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.08f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatRow(Icons.Default.PersonAdd, "Yeni Üye Sayısı:", report.newMembersCount.toString())
                StatRow(Icons.Default.Home, "Ev Ziyareti:", report.homeVisitsCount.toString())
                StatRow(Icons.Default.Store, "Esnaf Ziyareti:", report.shopVisitsCount.toString())
                StatRow(Icons.Default.MenuBook, "Kitap Hediyesi:", report.bookGiftsCount.toString())
                StatRow(Icons.Default.Article, "Broşür Dağıtımı:", report.brochureDistributionCount.toString())
                StatRow(Icons.Default.Label, "Etiket Yapıştırma:", report.stickerPastingCount.toString())
                StatRow(Icons.Default.CardGiftcard, "Logolu Hediyelik:", report.logoGiftsCount.toString())
            }

            if (report.fieldWorkParticipants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp).padding(end = 6.dp)
                    )
                    Text(
                        text = "Saha Katılanları & Notlar:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = report.fieldWorkParticipants,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (report.status == ReportStatus.REJECTED && report.rejectionReason != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                    Text(
                        text = "Red Nedeni: ${report.rejectionReason}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatusError
                    )
                }
            }

            // Action Buttons
            if (report.status == ReportStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IosButton(
                        text = "Onayla",
                        icon = Icons.Default.CheckCircle,
                        backgroundColor = StatusSuccess,
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )

                    IosButton(
                        text = "Reddet",
                        icon = Icons.Default.Cancel,
                        backgroundColor = Color(0xFF111111),
                        contentColor = StatusError,
                        onClick = onReject,
                        modifier = Modifier.weight(1f).border(1.dp, StatusError.copy(alpha=0.3f), RoundedCornerShape(50)),
                        height = 44.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp).padding(end = 6.dp)
            )
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun StatusBadge(status: ReportStatus) {
    val (bgColor, textColor, text) = when (status) {
        ReportStatus.PENDING -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, "Bekliyor")
        ReportStatus.APPROVED -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, "Onaylandı")
        ReportStatus.REJECTED -> Triple(StatusError.copy(alpha = 0.15f), StatusError, "Reddedildi")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TimeStatusBadge(isOnTime: Boolean) {
    val bgColor = if (isOnTime) StatusSuccess.copy(alpha = 0.12f) else StatusError.copy(alpha = 0.12f)
    val textColor = if (isOnTime) StatusSuccess else StatusError
    val text = if (isOnTime) "Süresinde Gönderildi" else "Gecikmeli Gönderildi"
    val icon = if (isOnTime) Icons.Default.CheckCircle else Icons.Default.Warning

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(10.dp).padding(end = 4.dp)
            )
            Text(text = text, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

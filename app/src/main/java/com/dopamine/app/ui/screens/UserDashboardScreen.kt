package com.dopamine.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopamine.app.model.ReportStatus
import com.dopamine.app.ui.components.CounterInput
import com.dopamine.app.ui.components.IosButton
import com.dopamine.app.ui.components.IosCard
import com.dopamine.app.ui.components.IosTextField
import com.dopamine.app.ui.theme.PrimaryBlue
import com.dopamine.app.ui.theme.StatusError
import com.dopamine.app.ui.theme.StatusSuccess
import com.dopamine.app.ui.theme.StatusWarning
import com.dopamine.app.ui.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun UserDashboardScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val userReport = viewModel.getCurrentUserReport()

    // Form fields state
    var newMembers by remember(userReport) { mutableIntStateOf(userReport?.newMembersCount ?: 0) }
    var homeVisits by remember(userReport) { mutableIntStateOf(userReport?.homeVisitsCount ?: 0) }
    var shopVisits by remember(userReport) { mutableIntStateOf(userReport?.shopVisitsCount ?: 0) }
    var bookGifts by remember(userReport) { mutableIntStateOf(userReport?.bookGiftsCount ?: 0) }
    var brochureDist by remember(userReport) { mutableIntStateOf(userReport?.brochureDistributionCount ?: 0) }
    var stickerPasting by remember(userReport) { mutableIntStateOf(userReport?.stickerPastingCount ?: 0) }
    var logoGifts by remember(userReport) { mutableIntStateOf(userReport?.logoGiftsCount ?: 0) }
    var fieldParticipants by remember(userReport) { mutableStateOf(userReport?.fieldWorkParticipants ?: "") }

    // Check system time
    val cal = Calendar.getInstance()
    val isSunday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
    val isWednesday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY
    val currentHour = cal.get(Calendar.HOUR_OF_DAY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        IosCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hoş Geldin",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = user?.fullName ?: "Kullanıcı",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f))
                            .clickable { viewModel.refreshData() }
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scheduled Reminder Banners
            val showWednesdayBanner = isWednesday && currentHour >= 21
            val showSundayBanner = isSunday && currentHour >= 12

            if ((showWednesdayBanner || showSundayBanner) && (userReport == null || userReport.status == ReportStatus.REJECTED)) {
                val bannerTitle = if (showSundayBanner) "Pazar Hatırlatması" else "Çarşamba Hatırlatması"
                val bannerMessage = "Raporunuzu Teslim Edin"

                if (true) {
                    IosCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = StatusWarning.copy(alpha = 0.12f),
                        borderColor = StatusWarning,
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = StatusWarning,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = bannerTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = StatusWarning
                                )
                                Text(
                                    text = bannerMessage,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            // Moderator Nudge Alert
            if (user?.lastNudgeTimestamp != null) {
                IosCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PrimaryBlue.copy(alpha = 0.12f),
                    borderColor = PrimaryBlue,
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Moderatör haftalık raporunuzu doldurmanızı hatırlattı!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            // Rejection / Approval Notification Cards
            if (userReport?.status == ReportStatus.REJECTED) {
                IosCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = StatusError.copy(alpha = 0.12f),
                    borderColor = StatusError,
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusError,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Raporunuz Reddedildi!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusError
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Red Nedeni: ${userReport.rejectionReason ?: "Neden belirtilmedi"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Lütfen bilgileri düzelterek tekrar gönderin.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else if (userReport?.status == ReportStatus.APPROVED) {
                IosCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = StatusSuccess.copy(alpha = 0.12f),
                    borderColor = StatusSuccess,
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rapor Onaylandı",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                            Text(
                                text = "Haftalık saha çalışması raporunuz moderatör tarafından onaylanmıştır.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Report Form Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Haftalık Saha Rapor Formu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Form Inputs (All 8 requested fields)
            CounterInput(
                title = "Bu haftaki Yeni Üye Sayısı",
                icon = Icons.Default.PersonAdd,
                count = newMembers,
                onCountChange = { newMembers = it }
            )

            CounterInput(
                title = "Ev Ziyareti",
                icon = Icons.Default.Home,
                count = homeVisits,
                onCountChange = { homeVisits = it }
            )

            CounterInput(
                title = "Esnaf Ziyareti",
                icon = Icons.Default.Store,
                count = shopVisits,
                onCountChange = { shopVisits = it }
            )

            CounterInput(
                title = "Kitap Hediyesi",
                icon = Icons.Default.MenuBook,
                count = bookGifts,
                onCountChange = { bookGifts = it }
            )

            CounterInput(
                title = "Broşür Dağıtımı",
                icon = Icons.Default.Article,
                count = brochureDist,
                onCountChange = { brochureDist = it }
            )

            CounterInput(
                title = "Etiket Yapıştırma",
                icon = Icons.Default.Label,
                count = stickerPasting,
                onCountChange = { stickerPasting = it }
            )

            CounterInput(
                title = "Logolu Hediyelik",
                icon = Icons.Default.CardGiftcard,
                count = logoGifts,
                onCountChange = { logoGifts = it }
            )

            // Field work participants text area
            IosCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp).padding(end = 8.dp)
                        )
                        Text(
                            text = "Saha Çalışmasına Katılanlar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    IosTextField(
                        value = fieldParticipants,
                        onValueChange = { fieldParticipants = it },
                        label = "",
                        placeholder = "Katılan isimleri ve saha notları yazın...",
                        singleLine = false,
                        minLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            IosButton(
                text = if (userReport?.status == ReportStatus.REJECTED) "Raporu Güncelle & Tekrar Gönder" else "Raporu Gönder",
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = {
                    viewModel.submitReport(
                        newMembers, homeVisits, shopVisits, bookGifts,
                        brochureDist, stickerPasting, logoGifts, fieldParticipants
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

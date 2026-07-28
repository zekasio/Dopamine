import os

# ----------------- ModeratorDashboardScreen.kt -----------------
mod_path = 'app/src/main/java/com/dopamine/app/ui/screens/ModeratorDashboardScreen.kt'
with open(mod_path, 'r') as f:
    mod_code = f.read()

# Add imports
imports_to_add = """import androidx.compose.ui.text.style.TextAlign
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
"""
mod_code = mod_code.replace("import androidx.compose.ui.text.style.TextAlign", imports_to_add)

# Replace Column with Box + Haze
start_layout = """    var selectedTab by remember { mutableStateOf(0) }
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(
                    state = hazeState,
                    backgroundColor = Color.Black,
                    tint = Color.Black.copy(alpha = 0.2f),
                    blurRadius = 20.dp
                )
        ) {"""

mod_code = mod_code.replace("""    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {""", start_layout)

# Remove SegmentedControl
mod_code = mod_code.replace("""            SegmentedControl(
                items = listOf("Gelen Raporlar (${currentWeekReports.size})", "Dürtme Listesi (${normalUsers.size})"),
                selectedIndex = selectedTab,
                onSegmentSelected = { selectedTab = it }
            )""", """            // SegmentedControl removed.""")

# Add LiquidGlassBottomNav at the end of the Column
bottom_nav = """        } // End of Column

        // Liquid Glass Bottom Nav
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .hazeChild(state = hazeState, shape = androidx.compose.foundation.shape.CircleShape)
        ) {
            com.dopamine.app.ui.components.LiquidGlassBottomNav(
                items = listOf(
                    com.dopamine.app.ui.components.NavItem("Raporlar", androidx.compose.material.icons.Icons.Default.List),
                    com.dopamine.app.ui.components.NavItem("Kullanıcılar", androidx.compose.material.icons.Icons.Default.Person)
                ),
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            )
        }
    } // End of Box

    // Rejection Reason Modal Dialog"""

mod_code = mod_code.replace("""        }
    }

    // Rejection Reason Modal Dialog""", bottom_nav)

with open(mod_path, 'w') as f:
    f.write(mod_code)


# ----------------- UserDashboardScreen.kt -----------------
user_path = 'app/src/main/java/com/dopamine/app/ui/screens/UserDashboardScreen.kt'
with open(user_path, 'r') as f:
    user_code = f.read()

user_imports = """import androidx.compose.ui.text.style.TextAlign
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import java.util.Calendar
"""
user_code = user_code.replace("import java.util.Calendar", user_imports)

user_start_layout = """    var selectedTab by remember { mutableStateOf(0) }
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(
                    state = hazeState,
                    backgroundColor = Color.Black,
                    tint = Color.Black.copy(alpha = 0.2f),
                    blurRadius = 20.dp
                )
        ) {"""

user_code = user_code.replace("""    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {""", user_start_layout)

# Remove Nudge
nudge_block = """            // Moderator Nudge Alert
            if (user?.lastNudgeTimestamp != null) {
                IosCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PrimaryBlue.copy(alpha = 0.12f),
                    borderColor = PrimaryBlue,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50),
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
            }"""
# In the original file, it might not have androidx.compose.foundation.shape.RoundedCornerShape, but just RoundedCornerShape
# Let's use regex to remove the nudge block entirely.
import re
user_code = re.sub(r'            // Moderator Nudge Alert.*?            }', '            // Nudge removed', user_code, flags=re.DOTALL)

# Add editing block and date block
block_logic = """                        if (userReport.status == ReportStatus.PENDING || userReport.status == ReportStatus.REJECTED) {
                            IosButton(
                                text = "RAPORU DÜZENLE ▷",
                                icon = null,
                                backgroundColor = Color(0xFF111111),
                                contentColor = Color(0xFF00E5FF),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E5FF).copy(alpha=0.3f), androidx.compose.foundation.shape.RoundedCornerShape(50)),
                                onClick = { isEditingMode = true }
                            )
                        }
                    }
                }
            } else {
                val cal = Calendar.getInstance()
                val isMonday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                val isTuesday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY
                val isBlockedDay = (isMonday || isTuesday) && !isEditingMode && userReport?.status != ReportStatus.REJECTED

                if (isBlockedDay) {
                    Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp), contentAlignment = Alignment.TopCenter) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "GÜN GELMEDİ",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Pazartesi ve Salı günleri rapor gönderimi kapalıdır. Çarşamba gününü bekleyin.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    if (isEditingMode || userReport?.status == ReportStatus.REJECTED) {
                        IosCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFF00E5FF).copy(alpha = 0.1f),
                            borderColor = Color(0xFF00E5FF),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Raporunu Düzenliyorsun (Düzenleme Modu)",
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }"""

user_code = user_code.replace("""                        if (userReport.status == ReportStatus.PENDING) {
                            IosButton(
                                text = "RAPORU DÜZENLE ▷",
                                icon = null,
                                backgroundColor = Color(0xFF111111),
                                contentColor = Color(0xFF00E5FF),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E5FF).copy(alpha=0.3f), RoundedCornerShape(50)),
                                onClick = { isEditingMode = true }
                            )
                        }
                    }
                }
            } else {""", block_logic)

user_bottom_nav = """            }
        } // End Column

        // Liquid Glass Bottom Nav
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .hazeChild(state = hazeState, shape = androidx.compose.foundation.shape.CircleShape)
        ) {
            com.dopamine.app.ui.components.LiquidGlassBottomNav(
                items = listOf(
                    com.dopamine.app.ui.components.NavItem("Rapor", androidx.compose.material.icons.Icons.Default.Edit),
                    com.dopamine.app.ui.components.NavItem("Profil", androidx.compose.material.icons.Icons.Default.Person)
                ),
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            )
        }
    } // End Box
}"""

user_code = user_code.replace("""            }
        }
    }
}""", user_bottom_nav)

with open(user_path, 'w') as f:
    f.write(user_code)

print("Patch applied successfully.")

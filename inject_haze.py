import re
import sys

def patch_moderator():
    with open('clean_mod.kt', 'r') as f:
        mod = f.read()

    # Imports
    mod = mod.replace('import java.util.Locale\n',
                      'import java.util.Locale\n' +
                      'import dev.chrisbanes.haze.HazeState\n' +
                      'import dev.chrisbanes.haze.haze\n' +
                      'import dev.chrisbanes.haze.hazeChild\n' +
                      'import androidx.compose.material.icons.filled.List\n' +
                      'import androidx.compose.material.icons.filled.Person\n')

    # Add HazeState and Box wrapper
    # The original file has:
    # var selectedTab by remember { mutableStateOf(0) }
    # Column(
    #     modifier = Modifier
    #         .fillMaxSize()
    #         .background(Color.Black)
    # ) {
    target = """    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {"""
    replacement = """    var selectedTab by remember { mutableStateOf(0) }
    val hazeState = remember { HazeState() }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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
    
    if target not in mod:
        print("Error matching mod target")
        sys.exit(1)
    mod = mod.replace(target, replacement)

    # Remove SegmentedControl
    seg_target = """            SegmentedControl(
                items = listOf("Gelen Raporlar (${currentWeekReports.size})", "Dürtme Listesi (${normalUsers.size})"),
                selectedIndex = selectedTab,
                onSegmentSelected = { selectedTab = it }
            )"""
    mod = mod.replace(seg_target, "            // SegmentedControl removed")

    # Add BottomNav at the end of Column
    # It ends with:
    #         }
    #     }
    # 
    #     // Rejection Reason Modal Dialog
    end_target = """        }
    }

    // Rejection Reason Modal Dialog"""
    end_replacement = """        } // End of inner else
        } // End of Column

        // Liquid Glass Bottom Nav
        androidx.compose.foundation.layout.Box(
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

    if end_target not in mod:
        print("Error matching mod end_target")
        sys.exit(1)
    mod = mod.replace(end_target, end_replacement)

    with open('app/src/main/java/com/dopamine/app/ui/screens/ModeratorDashboardScreen.kt', 'w') as f:
        f.write(mod)

def patch_user():
    with open('clean_user.kt', 'r') as f:
        user = f.read()

    # Imports
    user = user.replace('import java.util.Calendar\n',
                        'import java.util.Calendar\n' +
                        'import dev.chrisbanes.haze.HazeState\n' +
                        'import dev.chrisbanes.haze.haze\n' +
                        'import dev.chrisbanes.haze.hazeChild\n' +
                        'import androidx.compose.ui.text.style.TextAlign\n' +
                        'import androidx.compose.material.icons.filled.AccessTime\n' +
                        'import androidx.compose.material.icons.filled.Edit\n' +
                        'import androidx.compose.material.icons.filled.Person\n')

    # Add HazeState and Box wrapper
    target = """    var isEditingMode by remember { mutableStateOf(false) }

    // Time checks removed to allow report submissions on any day

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {"""
    replacement = """    var isEditingMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val hazeState = remember { HazeState() }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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

    if target not in user:
        print("Error matching user target")
        sys.exit(1)
    user = user.replace(target, replacement)

    # Remove Nudge
    user = re.sub(r'            // Moderator Nudge Alert.*?            }', '            // Nudge Alert Removed', user, flags=re.DOTALL)

    # Update logic (Monday/Tuesday block)
    logic_target = """                        if (userReport.status == ReportStatus.PENDING) {
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
            } else {"""
    
    logic_replacement = """                        if (userReport.status == ReportStatus.PENDING || userReport.status == ReportStatus.REJECTED) {
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
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp), contentAlignment = Alignment.TopCenter) {
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
                                textAlign = TextAlign.Center
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

    if logic_target not in user:
        print("Error matching user logic_target")
        sys.exit(1)
    user = user.replace(logic_target, logic_replacement)

    # Add BottomNav at the end
    end_target = """                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}"""
    end_replacement = """                Spacer(modifier = Modifier.height(24.dp))
                } // End inner else
            } // End main else block
        } // End Column

        // Liquid Glass Bottom Nav
        androidx.compose.foundation.layout.Box(
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
    } // End Box wrapper
}"""
    
    if end_target not in user:
        print("Error matching user end_target")
        sys.exit(1)
    user = user.replace(end_target, end_replacement)

    with open('app/src/main/java/com/dopamine/app/ui/screens/UserDashboardScreen.kt', 'w') as f:
        f.write(user)

patch_moderator()
patch_user()
print("Success")

package com.dopamine.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopamine.app.ui.components.IosCard
import com.dopamine.app.ui.components.IosTextField
import com.dopamine.app.ui.theme.CardElevated
import com.dopamine.app.ui.theme.GlassBorderDark
import com.dopamine.app.ui.theme.PrimaryBlue
import com.dopamine.app.ui.theme.StatusError
import com.dopamine.app.ui.viewmodel.MainViewModel

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val users by viewModel.users.collectAsState()
    val resets by viewModel.resetRequests.collectAsState()
    val isUserFormOpen by viewModel.isUserFormOpen.collectAsState()

    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoaded = true
    }

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            AnimatedVisibility(
                visible = isLoaded,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.openUserForm(null) },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ekle")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Admin Paneli",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sistem Yönetimi",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141414))
                        .clickable { viewModel.logout() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = StatusError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Çıkış",
                            color = StatusError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (resets.isNotEmpty()) {
                    item {
                        Text(
                            text = "Şifre Sıfırlama İstekleri",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                    }
                    items(resets, key = { "reset_${it.id}" }) { reset ->
                        AnimatedVisibility(
                            visible = isLoaded,
                            enter = slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ) + fadeIn()
                        ) {
                            IosCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = CardElevated,
                                borderColor = GlassBorderDark
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
                                            text = reset.username,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = reset.message,
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deletePasswordReset(reset.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = StatusError)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Kullanıcılar",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )
                }
                
                items(users, key = { "user_${it.id}" }) { user ->
                    AnimatedVisibility(
                        visible = isLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn()
                    ) {
                        IosCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openUserForm(user) },
                            backgroundColor = CardElevated,
                            borderColor = GlassBorderDark
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF141414)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = user.fullName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "@${user.username} - ${if (user.isModerator) "Moderatör" else "Saha"}",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                if (user.username != "admin") {
                                    IconButton(onClick = { viewModel.deleteUser(user.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = StatusError)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isUserFormOpen) {
        val isEdit = viewModel.editingUser.collectAsState().value != null
        AlertDialog(
            onDismissRequest = { viewModel.isUserFormOpen.value = false },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = if (isEdit) "Kullanıcıyı Düzenle" else "Yeni Kullanıcı Ekle",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IosTextField(
                        value = viewModel.formFullName.collectAsState().value,
                        onValueChange = { viewModel.formFullName.value = it },
                        label = "Ad Soyad"
                    )
                    IosTextField(
                        value = viewModel.formUsername.collectAsState().value,
                        onValueChange = { viewModel.formUsername.value = it },
                        label = "Kullanıcı Adı"
                    )
                    IosTextField(
                        value = viewModel.formPassword.collectAsState().value,
                        onValueChange = { viewModel.formPassword.value = it },
                        label = if (isEdit) "Şifre (Değiştirmek istemiyorsanız boş bırakın)" else "Şifre",
                        isPassword = true
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Switch(
                            checked = viewModel.formIsMod.collectAsState().value,
                            onCheckedChange = { viewModel.formIsMod.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Moderatör Yetkisi", color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveUserForm() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.isUserFormOpen.value = false }) {
                    Text("İptal", color = Color.Gray)
                }
            }
        )
    }
}

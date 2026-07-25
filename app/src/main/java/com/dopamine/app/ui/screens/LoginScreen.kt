package com.dopamine.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopamine.app.ui.components.IosButton
import com.dopamine.app.ui.components.IosCard
import com.dopamine.app.ui.components.IosTextField
import com.dopamine.app.ui.theme.AccentPurple
import com.dopamine.app.ui.theme.PrimaryBlue
import com.dopamine.app.ui.theme.StatusSuccess
import com.dopamine.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val username by viewModel.loginUsername.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()

    val isResetOpen by viewModel.isResetDialogOpen.collectAsState()
    val resetMessage by viewModel.resetMessageInput.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryBlue.copy(alpha = 0.15f),
                        AccentPurple.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo / Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dopamine",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "Haftalık Saha Rapor Takip Sistemi",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Embedded Fixed Supabase Status Indicator
            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .background(
                        StatusSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Supabase Bağlı",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusSuccess
                )
            }

            IosCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Giriş Yap",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Username Input
                    IosTextField(
                        value = username,
                        onValueChange = { viewModel.loginUsername.value = it },
                        label = "Kullanıcı Adı",
                        placeholder = "Kullanıcı adınızı girin...",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input
                    IosTextField(
                        value = password,
                        onValueChange = { viewModel.loginPassword.value = it },
                        label = "Şifre",
                        placeholder = "••••••••",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    AnimatedVisibility(visible = loginError != null) {
                        Text(
                            text = loginError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button
                    IosButton(
                        text = "Giriş Yap",
                        icon = Icons.AutoMirrored.Filled.Login,
                        isLoading = isLoggingIn,
                        onClick = { viewModel.login() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Forgot Password link
                    Text(
                        text = "Şifremi Unuttum?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .clickable { viewModel.isResetDialogOpen.value = true }
                            .padding(8.dp)
                    )
                }
            }
        }
    }

    // Password Reset Dialog
    if (isResetOpen) {
        BasicAlertDialog(
            onDismissRequest = { viewModel.isResetDialogOpen.value = false }
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
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Şifre Sıfırlama Talebi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Yöneticiye şifre sıfırlama mesajı gönderin. Talep veritabanına ve yönetim paneline kaydedilecektir.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    IosTextField(
                        value = resetMessage,
                        onValueChange = { viewModel.resetMessageInput.value = it },
                        label = "Mesajınız",
                        placeholder = "Lütfen şifremi sıfırlayın...",
                        singleLine = false,
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IosButton(
                        text = "Yöneticiye Mesaj Gönder",
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = {
                            viewModel.sendPasswordResetRequest()
                            viewModel.isResetDialogOpen.value = false
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "İptal",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { viewModel.isResetDialogOpen.value = false }
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

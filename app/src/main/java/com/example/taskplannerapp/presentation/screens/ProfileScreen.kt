package com.example.taskplannerapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var showPasswordForm by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopStart)
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            Text("Профиль", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(32.dp))

            Text("Почта: ${user?.email ?: "неизвестно"}", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showPasswordForm = !showPasswordForm },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showPasswordForm) "Скрыть смену пароля" else "Сменить пароль")
            }

            if (showPasswordForm) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Текущий пароль") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Новый пароль") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Повторите пароль") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val email = user?.email
                        if (email != null &&
                            newPassword == confirmPassword &&
                            newPassword.length >= 6 &&
                            currentPassword.isNotBlank()
                        ) {
                            val credential = EmailAuthProvider.getCredential(email, currentPassword)
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Пароль обновлён", Toast.LENGTH_SHORT).show()
                                            showPasswordForm = false
                                            newPassword = ""
                                            confirmPassword = ""
                                            currentPassword = ""
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Ошибка при обновлении пароля", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Неверный текущий пароль", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Проверьте введённые поля", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Подтвердить смену пароля")
                }
            }
        }

        OutlinedButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom=125.dp)
        ) {
            Text("Выйти")
        }
    }
}

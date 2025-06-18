package com.example.taskplannerapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вход",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth(0.75f),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier
                .fillMaxWidth(0.75f),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column {
            Button(onClick = {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { onAuthSuccess() }
                    .addOnFailureListener {
                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                    }
            },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(45.dp)
            ) {
                Text("Войти")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                        val db = FirebaseDatabase.getInstance().reference
                        db.child("users").child(uid).setValue(mapOf("email" to email))
                        onAuthSuccess()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(context, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
                    }
            },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(45.dp)
                    .border(width = 2.dp, MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(percent = 50))
            ) {
                Text("Зарегистрироваться")
            }
        }
    }
}

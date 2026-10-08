package com.quoteguru.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QuoteGuruScreen() }
    }
}

@Composable
private fun QuoteGuruScreen() {
    val context = LocalContext.current
    val saved by QuotePrefs.flow(context).collectAsState(initial = QuoteSettings())
    var quote by remember(saved.quote) { mutableStateOf(saved.quote) }
    var author by remember(saved.author) { mutableStateOf(saved.author) }
    var dark by remember(saved.dark) { mutableStateOf(saved.dark) }
    var shape by remember(saved.shape) { mutableStateOf(saved.shape) }
    val scope = rememberCoroutineScope()
    val bg = if (dark) Color(0xFF06111B) else Color(0xFFF4F7FA)
    val text = if (dark) Color.White else Color(0xFF10202E)

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = bg) {
            Column(
                Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("QuoteGuru", color = text, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Quotes That Live With You", color = text.copy(alpha = .65f))
                    }
                    Switch(checked = dark, onCheckedChange = { dark = it })
                }

                QuotePreview(
                    quote.ifBlank { "Your quote goes here." },
                    author.ifBlank { "Author" },
                    dark,
                    shape,
                    Modifier.fillMaxWidth().height(220.dp)
                )

                OutlinedTextField(
                    value = quote,
                    onValueChange = { quote = it },
                    label = { Text("Quote") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Shape", color = text, fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Rounded", "Circle", "Rectangle", "Cut").forEachIndexed { i, label ->
                        FilterChip(
                            selected = shape == i,
                            onClick = { shape = i },
                            label = { Text(label) }
                        )
                    }
                }

                Text("Built-in quotes", color = text, fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Dream big and dare to fail." to "Norman Vaughan",
                        "Believe you can and you're halfway there." to "Theodore Roosevelt",
                        "The future depends on what you do today." to "Mahatma Gandhi"
                    ).forEach { item ->
                        TextButton(onClick = { quote = item.first; author = item.second }) {
                            Text(item.first.take(18) + "…")
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            QuotePrefs.save(context, QuoteSettings(quote, author, dark, shape))
                            QuoteWidget().updateAll(context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("Save & Update Widget", fontWeight = FontWeight.Bold)
                }

                Text(
                    "Add QuoteGuru from the launcher widget picker. Resize it freely in portrait or landscape.",
                    color = text.copy(alpha = .55f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun QuotePreview(
    quote: String,
    author: String,
    dark: Boolean,
    shape: Int,
    modifier: Modifier
) {
    val colors = if (dark) {
        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    } else {
        listOf(Color(0xFFFF3CAC), Color(0xFF784BA0), Color(0xFF2B86C5))
    }

    val clipShape = when (shape) {
        1 -> CircleShape
        2 -> RoundedCornerShape(0.dp)
        3 -> CutCornerShape(22.dp)
        else -> RoundedCornerShape(24.dp)
    }

    Box(
        modifier.clip(clipShape).background(Brush.linearGradient(colors)).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "“$quote”",
                color = Color.White,
                fontSize = 23.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text("— $author", color = Color.White.copy(alpha = .92f), fontSize = 17.sp)
        }
    }
}

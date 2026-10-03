package com.dheeraj.wallproai

import android.app.WallpaperManager
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

private const val API_BASE = "https://wallpro-ai-image.hatchable.site/api"
private val Bg=Color(0xFF08090D)
private val Card=Color(0xFF12141C)
private val Accent=Color(0xFF7C4DFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WallProApp() }
    }

    fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    fun saveWallpaper(bitmap: Bitmap): Boolean {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "WallPro-${Date.now()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WallPro-AI")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: return false
            true
        } catch (_: Exception) { false }
    }

    fun setWallpaper(bitmap: Bitmap): Boolean {
        return try {
            val manager = WallpaperManager.getInstance(this)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            manager.setStream(stream.toByteArray().inputStream())
            true
        } catch (_: Exception) { false }
    }
}

@Composable
fun WallProApp() {
    val activity = androidx.compose.ui.platform.LocalContext.current as MainActivity
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("Pixel 10") }
    var style by remember { mutableStateOf("Cinematic") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    fun generate() {
        if (prompt.trim().isEmpty()) { status = "Enter a wallpaper description first."; return }
        loading = true
        bitmap = null
        status = "Creating your wallpaper..."
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val conn = (URL(API_BASE + "/generate").openConnection() as HttpURLConnection)
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 15000
                    conn.readTimeout = 120000
                    conn.doOutput = true
                    val json = """{"prompt":${jsonQuote(prompt)},"target":${jsonQuote(target)},"style":${jsonQuote(style)}}"""
                    conn.outputStream.use { it.write(json.toByteArray()) }
                    val body = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream).bufferedReader().use { it.readText() }
                    if (conn.responseCode !in 200..299) throw IllegalStateException(extractError(body))
                    val imageUrl = Regex(""""url"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
                        ?: throw IllegalStateException("No image URL returned.")
                    val imageConn = (URL(imageUrl).openConnection() as HttpURLConnection)
                    imageConn.connectTimeout = 15000
                    imageConn.readTimeout = 60000
                    imageConn.inputStream.use { BitmapFactory.decodeStream(it) }
                }
                if (result == null) throw IllegalStateException("Could not decode the generated image.")
                bitmap = result
                status = "Wallpaper generated successfully."
            } catch (e: Exception) {
                status = e.message ?: "Generation failed."
            } finally { loading = false }
        }
    }

    MaterialTheme(colorScheme=darkColorScheme(primary=Accent, background=Bg, surface=Card)) {
        Surface(Modifier.fillMaxSize(), color=Bg) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement=Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text("WallPro", fontSize=30.sp, fontWeight=FontWeight.Bold)
                    Text("-AI", fontSize=30.sp, fontWeight=FontWeight.Bold, color=Accent)
                }
                Text("AI wallpaper studio • Pixel 10 optimized", color=Color.LightGray)
                OutlinedTextField(
                    value=prompt, onValueChange={prompt=it},
                    modifier=Modifier.fillMaxWidth().height(145.dp),
                    label={Text("Describe your wallpaper")},
                    placeholder={Text("Futuristic neon city at night, rain, sports car, cinematic lighting...")},
                    shape=RoundedCornerShape(18.dp)
                )
                Text("Screen", fontWeight=FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                    listOf("Pixel 10","Android TV","PC / Desktop","Custom").forEach { p ->
                        FilterChip(selected=target==p,onClick={target=p},label={Text(p,fontSize=12.sp)})
                    }
                }
                Text("Style", fontWeight=FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                    listOf("Cinematic","Realistic","Anime","Neon").forEach { p ->
                        FilterChip(selected=style==p,onClick={style=p},label={Text(p,fontSize=12.sp)})
                    }
                }
                Button(
                    onClick={::generate}, enabled=!loading,
                    modifier=Modifier.fillMaxWidth().height(54.dp),
                    shape=RoundedCornerShape(18.dp)
                ) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth=2.dp) else Text("✦  Generate Wallpaper",fontSize=16.sp) }
                if (status.isNotBlank()) Text(status, color=if(status.contains("success")) Color(0xFF7CFFB2) else Color.LightGray)
                bitmap?.let { image ->
                    Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp)) {
                        Column(Modifier.padding(12.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            Image(image.asImageBitmap(), contentDescription="Generated wallpaper", modifier=Modifier.fillMaxWidth().heightIn(min=280.dp,max=560.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick={
                                    scope.launch(Dispatchers.IO) {
                                        val ok=activity.saveWallpaper(image)
                                        withContext(Dispatchers.Main){activity.toast(if(ok)"Saved to Pictures/WallPro-AI" else "Could not save image.")}
                                    }
                                }, Modifier.weight(1f)) { Text("Save") }
                                Button(onClick={
                                    scope.launch(Dispatchers.IO) {
                                        val ok=activity.setWallpaper(image)
                                        withContext(Dispatchers.Main){activity.toast(if(ok)"Wallpaper applied." else "Could not set wallpaper.")}
                                    }
                                }, Modifier.weight(1f)) { Text("Set Wallpaper") }
                            }
                        }
                    }
                } ?: Spacer(Modifier.height(100.dp))
                Text("WallPro-AI • v0.2", color=Color.Gray, fontSize=12.sp)
            }
        }
    }
}

private fun jsonQuote(value: String): String = """ + value.replace("\","\\").replace(""","\\"").replace("
","\\n").replace("","\\r") + """
private fun extractError(body: String): String =
    Regex(""""error"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1) ?: "Server request failed."

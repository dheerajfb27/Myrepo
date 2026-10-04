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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val Bg = Color(0xFF08090D)
private val Card = Color(0xFF12141C)
private val Accent = Color(0xFF7C4DFF)

data class Wallpaper(val title: String, val imageUrl: String, val sourceUrl: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WallProApp() }
    }

    fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    fun downloadWallpaper(bitmap: Bitmap): Boolean = try {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "WallPro-" + System.currentTimeMillis() + ".jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WallPro")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false
        val written = contentResolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        } ?: false
        if (written) {
            val done = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            contentResolver.update(uri, done, null, null)
            true
        } else {
            contentResolver.delete(uri, null, null)
            false
        }
    } catch (_: Exception) {
        false
    }

    fun setWallpaper(bitmap: Bitmap): Boolean = try {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        WallpaperManager.getInstance(this).setStream(stream.toByteArray().inputStream())
        true
    } catch (_: Exception) {
        false
    }
}

@Composable
fun WallProApp() {
    val activity = LocalContext.current as MainActivity
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("nature wallpaper") }
    var wallpapers by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var selected by remember { mutableStateOf<Wallpaper?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var loadingImage by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var actionBusy by remember { mutableStateOf(false) }

    fun search() {
        val q = query.trim()
        if (q.isEmpty()) {
            status = "Enter something to search."
            return
        }
        loading = true
        status = "Searching wallpapers..."
        scope.launch {
            try {
                wallpapers = withContext(Dispatchers.IO) { searchWikimedia(q) }
                status = if (wallpapers.isEmpty()) "No wallpapers found." else wallpapers.size.toString() + " wallpapers found."
            } catch (_: Exception) {
                status = "Search failed. Check your internet connection."
            } finally {
                loading = false
            }
        }
    }

    fun selectWallpaper(item: Wallpaper) {
        selected = item
        selectedBitmap = null
        loadingImage = true
        scope.launch {
            try {
                selectedBitmap = withContext(Dispatchers.IO) { downloadBitmap(item.imageUrl) }
                if (selectedBitmap == null) status = "Could not load this wallpaper."
            } catch (_: Exception) {
                status = "Could not load this image."
            } finally {
                loadingImage = false
            }
        }
    }

    LaunchedEffect(Unit) { search() }

    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Card)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("WallPro", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text(" • Wallpapers", fontSize = 18.sp, color = Accent)
                }
                Text(
                    "Discover and download wallpapers from the internet",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Search wallpapers") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { search() },
                        enabled = !loading,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Search") }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf("Nature", "Space", "Cars", "Anime").forEach { category ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                query = category + " wallpaper"
                                search()
                            },
                            label = { Text(category, fontSize = 12.sp) }
                        )
                    }
                }

                if (status.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(status, color = Color.LightGray, fontSize = 12.sp)
                }

                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(wallpapers.chunked(2)) { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                row.forEach { item ->
                                    WallpaperCard(item, Modifier.weight(1f)) {
                                        selectWallpaper(item)
                                    }
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        selected?.let { item ->
            AlertDialog(
                onDismissRequest = {
                    selected = null
                    selectedBitmap = null
                },
                title = { Text(item.title.removePrefix("File:"), maxLines = 2) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (loadingImage) {
                            CircularProgressIndicator()
                        } else if (selectedBitmap != null) {
                            Image(
                                selectedBitmap!!.asImageBitmap(),
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("Image unavailable", color = Color.Gray)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Source: Wikimedia Commons", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            enabled = selectedBitmap != null && !actionBusy,
                            onClick = {
                                val image = selectedBitmap ?: return@OutlinedButton
                                actionBusy = true
                                scope.launch(Dispatchers.IO) {
                                    val ok = activity.downloadWallpaper(image)
                                    withContext(Dispatchers.Main) {
                                        actionBusy = false
                                        activity.toast(
                                            if (ok) "Wallpaper downloaded to Pictures/WallPro"
                                            else "Download failed. Please try again."
                                        )
                                    }
                                }
                            }
                        ) {
                            Text(if (actionBusy) "Saving..." else "Download")
                        }

                        Button(
                            enabled = selectedBitmap != null && !actionBusy,
                            onClick = {
                                val image = selectedBitmap ?: return@Button
                                actionBusy = true
                                scope.launch(Dispatchers.IO) {
                                    val ok = activity.setWallpaper(image)
                                    withContext(Dispatchers.Main) {
                                        actionBusy = false
                                        activity.toast(
                                            if (ok) "Wallpaper applied."
                                            else "Could not set wallpaper."
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("Set Wallpaper")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        selected = null
                        selectedBitmap = null
                    }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
private fun WallpaperCard(item: Wallpaper, modifier: Modifier, onClick: () -> Unit) {
    var bitmap by remember(item.imageUrl) { mutableStateOf<Bitmap?>(null) }
    var loading by remember(item.imageUrl) { mutableStateOf(true) }

    LaunchedEffect(item.imageUrl) {
        bitmap = try {
            withContext(Dispatchers.IO) { downloadBitmap(item.imageUrl) }
        } catch (_: Exception) {
            null
        }
        loading = false
    }

    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        if (bitmap != null) {
            Image(
                bitmap!!.asImageBitmap(),
                contentDescription = item.title,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                if (loading) CircularProgressIndicator(Modifier.size(24.dp))
                else Text("Image unavailable", color = Color.Gray)
            }
        }
    }
}

private fun searchWikimedia(query: String): List<Wallpaper> {
    val encoded = URLEncoder.encode(query + " filetype:bitmap", "UTF-8")
    val api = "https://commons.wikimedia.org/w/api.php?action=query&generator=search" +
        "&gsrsearch=" + encoded + "&gsrnamespace=6&gsrlimit=20" +
        "&prop=imageinfo&iiprop=url&iiurlwidth=900&format=json&origin=*"

    val connection = (URL(api).openConnection() as HttpURLConnection)
    connection.connectTimeout = 15000
    connection.readTimeout = 30000
    connection.setRequestProperty("User-Agent", "WallPro/1.0 Android")
    try {
        if (connection.responseCode !in 200..299) return emptyList()
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val pages = JSONObject(body).optJSONObject("query")?.optJSONObject("pages") ?: return emptyList()
        val result = mutableListOf<Wallpaper>()
        pages.keys().forEach { key ->
            val page = pages.optJSONObject(key) ?: return@forEach
            val title = page.optString("title")
            val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: return@forEach
            val imageUrl = info.optString("thumburl").ifBlank { info.optString("url") }
            if (imageUrl.isNotBlank()) {
                result.add(Wallpaper(title, imageUrl, "https://commons.wikimedia.org"))
            }
        }
        return result
    } finally {
        connection.disconnect()
    }
}

private fun downloadBitmap(url: String): Bitmap? {
    val connection = (URL(url).openConnection() as HttpURLConnection)
    connection.connectTimeout = 15000
    connection.readTimeout = 30000
    connection.setRequestProperty("User-Agent", "WallPro/1.0 Android")
    try {
        if (connection.responseCode !in 200..299) return null
        connection.inputStream.use { return BitmapFactory.decodeStream(it) }
    } finally {
        connection.disconnect()
    }
}

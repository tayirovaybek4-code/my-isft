package com.example.apksecuritylabpro

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// APK ichidan olingan dastlabki tekshiruv natijasi.
data class ScanResult(
    val packageName: String,
    val permissions: List<String>,
    val attentionLevel: String
)

// Faylni ishga tushirmasdan Android PackageManager yordamida tahlil qilamiz.
// Katta fayllarni o'qish fon oqimida bajarilishi kerak.
suspend fun scanApk(context: Context, uri: Uri): ScanResult = withContext(Dispatchers.IO) {
    val tempFile = File.createTempFile("apk_scan_", ".apk", context.cacheDir)
    try {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Faylni ochib bo'lmadi" }
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        val pm = context.packageManager
        val info: PackageInfo? = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageArchiveInfo(
                tempFile.absolutePath,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(tempFile.absolutePath, PackageManager.GET_PERMISSIONS)
        }
        requireNotNull(info) { "Bu fayldan APK ma'lumotlarini o'qib bo'lmadi" }
        val permissions = info.requestedPermissions?.toList() ?: emptyList()
        val sensitive = setOf(
            "android.permission.READ_SMS", "android.permission.SEND_SMS",
            "android.permission.RECORD_AUDIO", "android.permission.CAMERA",
            "android.permission.READ_CONTACTS", "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_CALL_LOG"
        )
        val count = permissions.count { it in sensitive }
        val level = when {
            count >= 4 -> "Ko'p sezgir ruxsat (yuqori e'tibor)"
            count >= 2 -> "Bir nechta sezgir ruxsat (e'tibor kerak)"
            else -> "Kam sezgir ruxsat (baribir tekshiring)"
        }
        ScanResult(info.packageName, permissions, level)
    } finally {
        tempFile.delete()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { APKSecurityApp() }
    }
}

@Composable
fun APKSecurityApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("APK tekshirishga tayyor") }
    var result by remember { mutableStateOf<ScanResult?>(null) }
    var busy by remember { mutableStateOf(false) }
    val background = Color(0xFF0B1120)
    val card = Color(0xFF172033)
    val accent = Color(0xFF07E8AB)

    // Telefonning tizim fayl tanlash oynasi: alohida xotira ruxsati kerak emas.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true
            result = null
            message = "APK tekshirilmoqda..."
            scope.launch {
                try {
                    result = scanApk(context, uri)
                    message = "Tekshiruv yakunlandi"
                } catch (e: Exception) {
                    message = "Xatolik: ${e.message ?: "Fayl o'qilmadi"}"
                } finally {
                    busy = false
                }
            }
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(background)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))
            Text("🛡️", fontSize = 58.sp)
            Spacer(Modifier.height(10.dp))
            Text("APK SECURITY LAB", color = Color.White,
                fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("PRO VERSION", color = accent)
            Spacer(Modifier.height(28.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = card)) {
                Column(Modifier.padding(20.dp)) {
                    Text("XAVFSIZLIK MARKAZI", color = accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(message, color = Color.White)
                }
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { picker.launch(arrayOf("*/*")) },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(if (busy) "TEKSHIRILMOQDA..." else "📦 APK TEKSHIRISH",
                    color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))
            result?.let { scan ->
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = card)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("TEKSHIRUV NATIJASI", color = accent, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Text("Paket: ${scan.packageName}", color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(scan.attentionLevel, color = Color.Yellow)
                        Spacer(Modifier.height(14.dp))
                        Text("SO'RALGAN RUXSATLAR", color = accent)
                        Spacer(Modifier.height(8.dp))
                        if (scan.permissions.isEmpty()) {
                            Text("Ruxsat topilmadi", color = Color.LightGray)
                        } else {
                            scan.permissions.forEach { permission ->
                                Text("• ${permission.substringAfterLast('.')}", color = Color.White)
                                Spacer(Modifier.height(5.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Diqqat: bu ruxsatlarning dastlabki tahlili, antivirus emas. " +
                    "Zararli dastur yo'qligiga kafolat bermaydi.",
                color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

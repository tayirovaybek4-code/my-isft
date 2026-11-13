package com.example.lakatsaaniqlash

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private const val UNIVERSITY_LATITUDE = 41.311081
private const val UNIVERSITY_LONGITUDE = 69.240562
private val Ink = Color(0xFF142720)
private val Pine = Color(0xFF153D31)
private val Mint = Color(0xFFD2F4DA)
private val Muted = Color(0xFF4F665B)
private val Paper = Color(0xFFF3F6F1)
private val DashboardColors = lightColorScheme(primary = Pine, background = Paper, onBackground = Ink)

@Composable
internal fun LocationDashboard(
    location: Location?,
    status: String,
    settingsNeeded: Boolean,
    light: Float?,
    acceleration: Float?,
    lightAvailable: Boolean,
    accelerometerAvailable: Boolean,
    shakeCount: Int,
    onLocate: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    onMap: () -> Unit
) {
    val distance = remember(location) {
        location?.let { fix ->
            val result = FloatArray(1)
            Location.distanceBetween(
                fix.latitude, fix.longitude,
                UNIVERSITY_LATITUDE, UNIVERSITY_LONGITUDE, result
            )
            if (result[0] < 1_000f) format("%.0f m", result[0])
            else format("%.2f km", result[0] / 1_000f)
        }
    }

    MaterialTheme(colorScheme = DashboardColors) {
        Scaffold { insets ->
            Column(
                modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Joylashuv va\nsensorlar", color = Ink, fontSize = 34.sp,
                    lineHeight = 38.sp, fontWeight = FontWeight.Bold)

                Panel(background = Pine) {
                    Text("Joriy joylashuv", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(status, color = Mint)
                    if (location != null) {
                        Text(format("%.6f°, %.6f°", location.latitude, location.longitude),
                            color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
                        if (location.hasAccuracy()) {
                            Text(format("Aniqlik ±%.0f m", location.accuracy), color = Mint)
                        }
                    }
                    Button(
                        onClick = if (settingsNeeded) onSettings else onLocate,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Pine)
                    ) {
                        Text(when {
                            settingsNeeded -> "Joylashuv sozlamalarini ochish"
                            location != null -> "Joylashuvni yangilash"
                            else -> "Joylashuvni aniqlash"
                        })
                    }
                    if (location != null) {
                        OutlinedButton(
                            onClick = onMap,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) { Text("Xaritada ochish") }
                    }
                }

                Panel {
                    Text("Universitetgacha masofa", color = Ink, fontWeight = FontWeight.SemiBold)
                    Text(distance ?: "—", color = Pine, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("To‘g‘ri chiziq bo‘yicha", color = Muted, fontSize = 14.sp)
                    Text(format("Manzil nuqtasi  %.6f°, %.6f°", UNIVERSITY_LATITUDE, UNIVERSITY_LONGITUDE),
                        color = Muted, fontSize = 13.sp)
                }

                Text("Telefon sensorlari", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Panel(modifier = Modifier.weight(1f)) {
                        Reading("Yorug‘lik", light?.let { format("%.0f lx", it) }, lightAvailable)
                    }
                    Panel(modifier = Modifier.weight(1f)) {
                        Reading("Tezlanish", acceleration?.let { format("%.1f m/s²", it) }, accelerometerAvailable)
                    }
                }
                Panel(background = if (shakeCount > 0) Mint else Color.White) {
                    Reading("Silkitishlar", "$shakeCount marta", accelerometerAvailable)
                }
                if (lightAvailable || accelerometerAvailable) {
                    OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                        Text("Sensorlarni yangilash")
                    }
                }
            }
        }
    }
}

@Composable
private fun Reading(label: String, reading: String?, available: Boolean) {
    Text(label, color = Muted, fontSize = 14.sp)
    Text(
        when {
            !available -> "Sensor yo‘q"
            reading == null -> "Kutilmoqda…"
            else -> reading
        },
        color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun Panel(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier.fillMaxWidth().background(background, RoundedCornerShape(20.dp))) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

private fun format(pattern: String, vararg values: Any) = String.format(Locale.US, pattern, *values)

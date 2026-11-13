package com.example.lakatsaaniqlash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var locations: LocationManager
    private lateinit var sensors: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null
    private var foreground = false
    private var tracking = false
    private var lastShake = 0L

    private var location by mutableStateOf<Location?>(null)
    private var status by mutableStateOf("Koordinatalarni olish uchun qidiruvni boshlang.")
    private var acceleration by mutableStateOf<Float?>(null)
    private var light by mutableStateOf<Float?>(null)
    private var shakeCount by mutableStateOf(0)
    private var settingsNeeded by mutableStateOf(false)

    private val listener = object : LocationListener {
        override fun onLocationChanged(fix: Location) {
            val previous = location
            if (previous != null &&
                fix.elapsedRealtimeNanos - previous.elapsedRealtimeNanos < 10_000_000_000L &&
                fix.accuracy > previous.accuracy
            ) return

            location = fix
            status = if (hasFinePermission()) "Joylashuv yangilanmoqda" else "Taxminiy joylashuv"
        }

        @Deprecated("Required by LocationListener on older Android versions")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderDisabled(provider: String) {
            if (foreground && tracking) startLocationUpdates()
        }

        override fun onProviderEnabled(provider: String) {
            if (foreground && tracking) startLocationUpdates()
        }
    }

    private val permissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (hasLocationPermission()) {
            tracking = true
            if (foreground) startLocationUpdates()
        } else {
            tracking = false
            status = "Joylashuv ruxsati berilmadi. Qayta urinib ko‘ring."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        locations = getSystemService(LocationManager::class.java)
        sensors = getSystemService(SensorManager::class.java)
        accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensors.getDefaultSensor(Sensor.TYPE_LIGHT)
        tracking = savedInstanceState?.getBoolean("tracking") ?: false

        setContent {
            LocationDashboard(
                location = location,
                status = status,
                settingsNeeded = settingsNeeded,
                light = light,
                acceleration = acceleration,
                lightAvailable = lightSensor != null,
                accelerometerAvailable = accelerometer != null,
                shakeCount = shakeCount,
                onLocate = ::requestLocation,
                onRefresh = ::bindSensors,
                onSettings = { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                onMap = ::openMap
            )
        }
    }

    override fun onResume() {
        super.onResume()
        foreground = true
        bindSensors()
        if (tracking) startLocationUpdates()
    }

    override fun onPause() {
        foreground = false
        locations.removeUpdates(listener)
        sensors.unregisterListener(this)
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("tracking", tracking)
        super.onSaveInstanceState(outState)
    }

    private fun hasFinePermission() =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermission() = hasFinePermission() ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocation() {
        if (hasLocationPermission()) {
            tracking = true
            startLocationUpdates()
        } else {
            permissions.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun startLocationUpdates() {
        locations.removeUpdates(listener)
        settingsNeeded = false
        if (!hasLocationPermission()) {
            location = null
            tracking = false
            status = "Joylashuv uchun ruxsat kerak."
            return
        }

        val providers = locations.getProviders(true).filter { provider ->
            provider == LocationManager.NETWORK_PROVIDER ||
                (hasFinePermission() && provider == LocationManager.GPS_PROVIDER)
        }
        if (providers.isEmpty()) {
            settingsNeeded = true
            location = null
            status = "Sozlamalardan joylashuv xizmatini yoqing."
            return
        }

        status = "Joylashuv signali qidirilmoqda…"
        try {
            providers.forEach { provider ->
                locations.requestLocationUpdates(provider, 2_000L, 0f, listener)
            }
        } catch (_: SecurityException) {
            locations.removeUpdates(listener)
            location = null
            tracking = false
            status = "Joylashuv uchun ruxsat kerak."
        } catch (_: IllegalArgumentException) {
            locations.removeUpdates(listener)
            status = "Joylashuv xizmati vaqtincha mavjud emas. Qayta urinib ko‘ring."
        }
    }

    private fun openMap() {
        val fix = location ?: return
        val point = "${fix.latitude},${fix.longitude}"
        val map = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$point?q=$point"))
        try {
            startActivity(map)
        } catch (_: android.content.ActivityNotFoundException) {
            val browser = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$point"))
            try {
                startActivity(browser)
            } catch (_: android.content.ActivityNotFoundException) {
                status = "Xaritani ochish uchun mos ilova topilmadi."
            }
        }
    }

    private fun bindSensors() {
        sensors.unregisterListener(this)
        light = null
        acceleration = null
        listOfNotNull(accelerometer, lightSensor).forEach { sensor ->
            sensors.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> light = event.values[0]
            Sensor.TYPE_ACCELEROMETER -> {
                val (x, y, z) = event.values
                val magnitude = sqrt(x * x + y * y + z * z)
                acceleration = magnitude
                val now = SystemClock.elapsedRealtime()
                if (magnitude > 2.5f * SensorManager.GRAVITY_EARTH && now - lastShake >= 1_200) {
                    shakeCount++
                    lastShake = now
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

package com.bytekoders.mypod.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.sqrt

class TiltSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltState = MutableStateFlow(Pair(0f, 0f))
    val tiltState: StateFlow<Pair<Float, Float>> = _tiltState.asStateFlow()

    private var isListening = false

    fun start() {
        if (isListening || accelerometer == null) return
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        // Pitch & Roll calculation in degrees
        val pitchRad = atan2(-ax.toDouble(), sqrt((ay * ay + az * az).toDouble()))
        val rollRad = atan2(ay.toDouble(), az.toDouble())

        val pitchDeg = (pitchRad * 180.0 / Math.PI).toFloat().coerceIn(-30f, 30f)
        val rollDeg = (rollRad * 180.0 / Math.PI).toFloat().coerceIn(-30f, 30f)

        _tiltState.value = Pair(pitchDeg, rollDeg)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

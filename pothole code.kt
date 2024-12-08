package com.example.potholedetectionapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var sensorEventListener: SensorEventListener

    // Ngưỡng gia tốc để phát hiện ổ gà (có thể điều chỉnh tùy thuộc vào thử nghiệm)
    private val threshold = 15.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Lấy cảm biến gia tốc
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Kiểm tra xem thiết bị có hỗ trợ cảm biến gia tốc không
        if (accelerometer != null) {
            // Đăng ký listener để lắng nghe dữ liệu từ cảm biến gia tốc
            sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            // Lấy giá trị gia tốc theo các trục X, Y, Z
                            val x = it.values[0]
                            val y = it.values[1]
                            val z = it.values[2]

                            // Tính toán độ lớn gia tốc (magnitude)
                            val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                            // Kiểm tra nếu gia tốc vượt quá ngưỡng, phát hiện ổ gà
                            if (acceleration > threshold) {
                                // Log thông báo có ổ gà
                                Log.d("PotholeDetection", "Phát hiện ổ gà! Gia tốc: $acceleration")
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Không cần xử lý khi độ chính xác thay đổi
                }
            }
        } else {
            Log.e("PotholeDetection", "Thiết bị không hỗ trợ cảm biến gia tốc")
        }
    }

    override fun onStart() {
        super.onStart()
        // Đăng ký listener khi Activity bắt đầu
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onResume() {
        super.onResume()
        // Đăng ký lại listener khi Activity được mở lại (trở thành foreground)
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        // Hủy đăng ký listener khi Activity không còn trong foreground (bị tạm dừng)
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onStop() {
        super.onStop()
        // Hủy đăng ký listener khi Activity không còn hiển thị (nếu cần)
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy tài nguyên khi Activity bị hủy (nếu có)
        sensorManager.unregisterListener(sensorEventListener)
    }
}

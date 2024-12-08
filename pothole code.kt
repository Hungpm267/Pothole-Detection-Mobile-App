package com.example.potholedetectionapp

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.text.SimpleDateFormat;
import java.util.*;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.widget.Toast;

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var sensorEventListener: SensorEventListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val potholes = mutableListOf<Pothole>()
    private val threshold = 15.0f // Ngưỡng gia tốc phát hiện ổ gà

    // Quyền vị trí
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo SensorManager và FusedLocationProviderClient
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Lấy cảm biến gia tốc
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        // Kiểm tra xem thiết bị có hỗ trợ cảm biến gia tốc không
        if (accelerometer != null) {
            // Đăng ký listener để lắng nghe dữ liệu từ cảm biến gia tốc
            sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            val x = it.values[0]
                            val y = it.values[1]
                            val z = it.values[2]

                            // Tính toán độ lớn gia tốc
                            val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                            if (acceleration > threshold) {
                                // Lấy tọa độ GPS và tính độ sâu
                                getLocationAndSavePothole(acceleration)
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

        // Yêu cầu quyền vị trí nếu chưa có
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getLocationAndSavePothole(acceleration: Float) {
        // Lấy tọa độ GPS
        fusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener<Location> { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val detectedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                // Tính độ sâu giả định (ví dụ: dựa trên gia tốc)
                val depth = calculateDepth(acceleration)

                // Tạo đối tượng Pothole và lưu vào danh sách
                val pothole = Pothole(
                    id = UUID.randomUUID().toString(),
                    latitude = latitude,
                    longitude = longitude,
                    acceleration = acceleration,
                    detectedAt = detectedAt,
                    tiltAngle = 30.0f,  // Ví dụ góc nghiêng
                    status = "Detected",
                    condition = "Bad",
                    depth = depth,
                    description = "Large crack",
                    analysisResult = "High severity"
                )

                // Lưu vào danh sách
                potholes.add(pothole)

                // Log thông tin ổ gà
                Log.d("PotholeDetection", "Phát hiện ổ gà! Gia tốc: $acceleration, Tọa độ: ($latitude, $longitude), Độ sâu: $depth, Thời gian: $detectedAt")
            } else {
                Toast.makeText(this, "Không thể lấy tọa độ GPS", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateDepth(acceleration: Float): Float {
        // Ví dụ: tính độ sâu giả định dựa trên gia tốc (có thể thay đổi theo logic cụ thể)
        return (acceleration - threshold) * 0.1f // Công thức giả định, điều chỉnh cho phù hợp
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
        // Hủy đăng ký listener khi Activity không còn trong foreground
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onStop() {
        super.onStop()
        // Hủy đăng ký listener khi Activity không còn hiển thị
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy tài nguyên khi Activity bị hủy
        sensorManager.unregisterListener(sensorEventListener)
    }

    // Xử lý quyền vị trí
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PotholeDetection", "Quyền vị trí đã được cấp.")
            } else {
                Toast.makeText(this, "Cần cấp quyền vị trí để tiếp tục", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

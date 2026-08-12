package com.secure.gps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    
    private TextView tvStatus, tvInfo;
    private Button btnStart, btnStop;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvStatus = findViewById(R.id.tvStatus);
        tvInfo = findViewById(R.id.tvInfo);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        
        double[] wgs = CoordinateConverter.TARGET_WGS84;
        tvInfo.setText(String.format(
            "目标位置:\n高德坐标: 36.66512, 114.13653\nWGS-84: %.6f, %.6f",
            wgs[0], wgs[1]
        ));
        
        checkPermissions();
        
        btnStart.setOnClickListener(v -> {
            if (isDeveloperOptionsEnabled()) {
                Toast.makeText(this, "⚠ 请关闭开发者选项后打卡", Toast.LENGTH_LONG).show();
            }
            startGPSService();
        });
        
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, GPSForegroundService.class));
            tvStatus.setText("● 已停止");
            GPSForegroundService.isRunning = false;
        });
    }
    
    private void startGPSService() {
        Intent intent = new Intent(this, GPSForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        tvStatus.setText("● GPS模拟运行中");
        moveTaskToBack(true);
        Toast.makeText(this, "GPS已注入，打开飞书打卡", Toast.LENGTH_SHORT).show();
    }
    
    private boolean isDeveloperOptionsEnabled() {
        return Settings.Global.getInt(
            getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
    }
    
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        tvStatus.setText(GPSForegroundService.isRunning ? "● GPS模拟运行中" : "● 未启动");
    }
}
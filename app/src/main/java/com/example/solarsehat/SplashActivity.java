package com.example.solarsehat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SolarSehat_Splash";
    private static final int SPLASH_DELAY_MS = 2000;
    private TextView txtSplashStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "Lifecycle: onCreate");

        txtSplashStatus = findViewById(R.id.txtSplashStatus);

        verifyModelAssets();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY_MS);
    }

    private void verifyModelAssets() {
        boolean yoloOk = ModelManager.isAssetFileExists(this, "yolov5n.tflite") ||
                ModelManager.isAssetFileExists(this, "yolov8n.tflite") ||
                ModelManager.isAssetFileExists(this, "yolo11n.tflite");
        boolean classOk = ModelManager.isAssetFileExists(this, "panel_classifier.tflite");

        if (yoloOk && classOk) {
            txtSplashStatus.setText("All AI models verified successfully!");
        } else {
            txtSplashStatus.setText("Note: Running with offline model fallback.");
        }
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "Lifecycle: onDestroy"); }
}

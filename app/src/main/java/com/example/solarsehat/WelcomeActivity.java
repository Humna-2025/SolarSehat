package com.example.solarsehat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SolarSehat_Welcome";
    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Log.d(TAG, "Lifecycle: onCreate");

        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnGetStarted) {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "Lifecycle: onDestroy"); }
}

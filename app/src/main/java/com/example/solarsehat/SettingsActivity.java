package com.example.solarsehat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SolarSehat_Settings";
    private static final String PREFS_NAME = "SolarSehatPrefs";

    private ImageButton btnBack;
    private TextView txtSettingsUserName, txtSettingsUserEmail, btnChangePassword, btnPrivacyInfo;
    private Button btnLogout, btnSaveSettings;
    private RadioGroup rgSettingsPrecision;
    private RadioButton rbFp16, rbFp32;
    private EditText edtSystemKw, edtSunHours, edtRupeeRate, edtCleanerPhone;
    private LinearLayout navHome, navScan, navReports, navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, "Lifecycle: onCreate");

        btnBack = findViewById(R.id.btnBack);
        txtSettingsUserName = findViewById(R.id.txtSettingsUserName);
        txtSettingsUserEmail = findViewById(R.id.txtSettingsUserEmail);
        btnLogout = findViewById(R.id.btnLogout);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnPrivacyInfo = findViewById(R.id.btnPrivacyInfo);

        rgSettingsPrecision = findViewById(R.id.rgSettingsPrecision);
        rbFp16 = findViewById(R.id.rbFp16);
        rbFp32 = findViewById(R.id.rbFp32);

        edtSystemKw = findViewById(R.id.edtSystemKw);
        edtSunHours = findViewById(R.id.edtSunHours);
        edtRupeeRate = findViewById(R.id.edtRupeeRate);
        edtCleanerPhone = findViewById(R.id.edtCleanerPhone);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        navHome = findViewById(R.id.navHome);
        navScan = findViewById(R.id.navScan);
        navReports = findViewById(R.id.navReports);
        navSettings = findViewById(R.id.navSettings);

        btnBack.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnChangePassword.setOnClickListener(this);
        btnPrivacyInfo.setOnClickListener(this);
        btnSaveSettings.setOnClickListener(this);

        navHome.setOnClickListener(this);
        navScan.setOnClickListener(this);
        navReports.setOnClickListener(this);
        navSettings.setOnClickListener(this);

        loadPreferences();
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        edtSystemKw.setText(prefs.getString("SYSTEM_KW", "5.0"));
        edtSunHours.setText(prefs.getString("SUN_HOURS", "5.5"));
        edtRupeeRate.setText(prefs.getString("RUPEE_RATE", "55.0"));
        edtCleanerPhone.setText(prefs.getString("CLEANER_PHONE", "03001234567"));

        String precision = prefs.getString("PRECISION", "FP16");
        if ("FP32".equals(precision)) {
            rbFp32.setChecked(true);
        } else {
            rbFp16.setChecked(true);
        }
    }

    private void savePreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("SYSTEM_KW", edtSystemKw.getText().toString().trim());
        editor.putString("SUN_HOURS", edtSunHours.getText().toString().trim());
        editor.putString("RUPEE_RATE", edtRupeeRate.getText().toString().trim());
        editor.putString("CLEANER_PHONE", edtCleanerPhone.getText().toString().trim());

        String precision = (rgSettingsPrecision.getCheckedRadioButtonId() == R.id.rbFp32) ? "FP32" : "FP16";
        editor.putString("PRECISION", precision);
        editor.apply();

        Toast.makeText(this, "System Settings Saved Successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBack || id == R.id.navHome) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.navScan) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.navReports) {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.btnLogout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (id == R.id.btnSaveSettings) {
            savePreferences();
        } else if (id == R.id.btnChangePassword) {
            Toast.makeText(this, "Change Password requested", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btnPrivacyInfo) {
            Toast.makeText(this, "SolarSehat operates 100% offline & secure.", Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "Lifecycle: onDestroy"); }
}

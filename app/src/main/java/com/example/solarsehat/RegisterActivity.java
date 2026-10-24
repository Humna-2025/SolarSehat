package com.example.solarsehat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SolarSehat_Register";

    private EditText edtRegName, edtRegEmail, edtRegPhone, edtRegPassword;
    private Button btnRegister, btnGoogleRegister;
    private TextView txtLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "Lifecycle: onCreate");

        edtRegName = findViewById(R.id.edtRegName);
        edtRegEmail = findViewById(R.id.edtRegEmail);
        edtRegPhone = findViewById(R.id.edtRegPhone);
        edtRegPassword = findViewById(R.id.edtRegPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        txtLoginLink = findViewById(R.id.txtLoginLink);

        btnRegister.setOnClickListener(this);
        btnGoogleRegister.setOnClickListener(this);
        txtLoginLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRegister || id == R.id.btnGoogleRegister) {
            Toast.makeText(this, "Account registered successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.txtLoginLink) {
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

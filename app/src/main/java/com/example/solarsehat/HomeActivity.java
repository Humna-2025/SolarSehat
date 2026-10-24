package com.example.solarsehat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "SolarSehat_Home";
    private static final String PREFS_NAME = "SolarSehatPrefs";

    private DrawerLayout drawerLayout;
    private ImageButton btnMenuDrawer, btnOverflowMenu;

    private TextView txtHomePowerGen, txtHomeHealthScore;
    private TextView txtRecentTitle, txtRecentSubtitle, txtRecentTime;
    private ImageView imgRecentThumb;

    private LinearLayout cardActionScan, cardActionReports, cardRecentAnalysis;
    private Button btnStartScan, btnModelBenchmarks;
    private LinearLayout navHome, navScan, navReports, navSettings;

    private ReportDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "Lifecycle: onCreate");

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        btnMenuDrawer = findViewById(R.id.btnMenuDrawer);
        btnOverflowMenu = findViewById(R.id.btnOverflowMenu);

        txtHomePowerGen = findViewById(R.id.txtHomePowerGen);
        txtHomeHealthScore = findViewById(R.id.txtHomeHealthScore);

        txtRecentTitle = findViewById(R.id.txtRecentTitle);
        txtRecentSubtitle = findViewById(R.id.txtRecentSubtitle);
        txtRecentTime = findViewById(R.id.txtRecentTime);
        imgRecentThumb = findViewById(R.id.imgRecentThumb);

        cardActionScan = findViewById(R.id.cardActionScan);
        cardActionReports = findViewById(R.id.cardActionReports);
        cardRecentAnalysis = findViewById(R.id.cardRecentAnalysis);

        btnStartScan = findViewById(R.id.btnStartScan);
        btnModelBenchmarks = findViewById(R.id.btnModelBenchmarks);

        navHome = findViewById(R.id.navHome);
        navScan = findViewById(R.id.navScan);
        navReports = findViewById(R.id.navReports);
        navSettings = findViewById(R.id.navSettings);

        btnMenuDrawer.setOnClickListener(this);
        btnOverflowMenu.setOnClickListener(this);
        cardActionScan.setOnClickListener(this);
        cardActionReports.setOnClickListener(this);
        cardRecentAnalysis.setOnClickListener(this);
        btnStartScan.setOnClickListener(this);
        btnModelBenchmarks.setOnClickListener(this);

        navHome.setOnClickListener(this);
        navScan.setOnClickListener(this);
        navReports.setOnClickListener(this);
        navSettings.setOnClickListener(this);

        navigationView.setNavigationItemSelectedListener(this);

        dbHelper = new ReportDatabaseHelper(this);

        loadDashboardData();
    }

    private void loadDashboardData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String systemKw = prefs.getString("SYSTEM_KW", "5.0");
        txtHomePowerGen.setText(String.format(Locale.getDefault(), "%s kW", systemKw));

        ReportItem latest = dbHelper.getLatestReport();
        if (latest != null) {
            txtRecentTitle.setText(String.format(Locale.getDefault(), "Panel %d - %s", latest.getId(), latest.getCondition()));
            txtRecentSubtitle.setText(String.format(Locale.getDefault(), "Power Loss: %.1f%%", latest.getLossPct() * 100));
            txtRecentTime.setText(latest.getDateStr());
            if (latest.getImagePath() != null) {
                Bitmap bmp = ImageUtils.decodeSampledBitmapFromFile(latest.getImagePath(), 120, 120);
                if (bmp != null) imgRecentThumb.setImageBitmap(bmp);
            }
            txtHomeHealthScore.setText(latest.isHealthy() ? "95%" : "72%");
        } else {
            txtRecentTitle.setText("No Scans Yet");
            txtRecentSubtitle.setText("Tap Scan Panel to start inspection");
            txtRecentTime.setText("N/A");
            txtHomeHealthScore.setText("100%");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnMenuDrawer) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else if (id == R.id.btnOverflowMenu) {
            showOverflowMenu(v);
        } else if (id == R.id.cardActionScan || id == R.id.btnStartScan || id == R.id.navScan) {
            Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
            startActivity(intent);
        } else if (id == R.id.cardActionReports || id == R.id.navReports || id == R.id.cardRecentAnalysis) {
            Intent intent = new Intent(HomeActivity.this, ReportsActivity.class);
            startActivity(intent);
        } else if (id == R.id.navSettings) {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.btnModelBenchmarks) {
            Intent intent = new Intent(HomeActivity.this, BenchmarkActivity.class);
            startActivity(intent);
        }
    }

    private void showOverflowMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.overflow_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return handleNavigationItemId(item.getItemId());
            }
        });
        popup.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        return handleNavigationItemId(item.getItemId());
    }

    private boolean handleNavigationItemId(int itemId) {
        if (itemId == R.id.menu_dashboard) {
            return true;
        } else if (itemId == R.id.menu_scan) {
            startActivity(new Intent(this, ScanActivity.class));
            return true;
        } else if (itemId == R.id.menu_reports) {
            startActivity(new Intent(this, ReportsActivity.class));
            return true;
        } else if (itemId == R.id.menu_accounts || itemId == R.id.action_accounts) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.menu_settings || itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.menu_benchmarks) {
            startActivity(new Intent(this, BenchmarkActivity.class));
            return true;
        } else if (itemId == R.id.menu_help || itemId == R.id.action_help) {
            Toast.makeText(this, "Help & Support: Clean panels every 2-4 weeks with fresh water.", Toast.LENGTH_LONG).show();
            return true;
        } else if (itemId == R.id.menu_about || itemId == R.id.action_about) {
            Toast.makeText(this, "SolarSehat v2.0 - Rooftop Solar Panel Defect Analyzer", Toast.LENGTH_LONG).show();
            return true;
        } else if (itemId == R.id.menu_logout || itemId == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); loadDashboardData(); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "Lifecycle: onDestroy"); }
}

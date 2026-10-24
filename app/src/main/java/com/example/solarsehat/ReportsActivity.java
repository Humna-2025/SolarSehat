package com.example.solarsehat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SolarSehat_Reports";

    private ImageButton btnBack;
    private Button btnFilterAll, btnFilterHealthy, btnFilterAttention;
    private ListView listReports;
    private TextView txtEmptyReports;
    private LinearLayout navHome, navScan, navReports, navSettings;

    private ReportDatabaseHelper dbHelper;
    private List<ReportItem> reportItems;
    private ReportAdapter adapter;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        Log.d(TAG, "Lifecycle: onCreate");

        btnBack = findViewById(R.id.btnBack);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterHealthy = findViewById(R.id.btnFilterHealthy);
        btnFilterAttention = findViewById(R.id.btnFilterAttention);
        listReports = findViewById(R.id.listReports);
        txtEmptyReports = findViewById(R.id.txtEmptyReports);

        navHome = findViewById(R.id.navHome);
        navScan = findViewById(R.id.navScan);
        navReports = findViewById(R.id.navReports);
        navSettings = findViewById(R.id.navSettings);

        btnBack.setOnClickListener(this);
        btnFilterAll.setOnClickListener(this);
        btnFilterHealthy.setOnClickListener(this);
        btnFilterAttention.setOnClickListener(this);

        navHome.setOnClickListener(this);
        navScan.setOnClickListener(this);
        navReports.setOnClickListener(this);
        navSettings.setOnClickListener(this);

        dbHelper = new ReportDatabaseHelper(this);
        loadReports("ALL");
    }

    private void loadReports(String filter) {
        currentFilter = filter;
        reportItems = dbHelper.getAllReports(filter);

        if (reportItems.isEmpty()) {
            txtEmptyReports.setVisibility(View.VISIBLE);
            listReports.setVisibility(View.GONE);
        } else {
            txtEmptyReports.setVisibility(View.GONE);
            listReports.setVisibility(View.VISIBLE);
            adapter = new ReportAdapter();
            listReports.setAdapter(adapter);
        }

        updateFilterButtonStyles();
    }

    private void updateFilterButtonStyles() {
        btnFilterAll.setBackgroundResource("ALL".equals(currentFilter) ? R.drawable.bg_button_emerald : R.drawable.bg_button_outline);
        btnFilterAll.setTextColor("ALL".equals(currentFilter) ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.primary_emerald));

        btnFilterHealthy.setBackgroundResource("HEALTHY".equals(currentFilter) ? R.drawable.bg_button_emerald : R.drawable.bg_button_outline);
        btnFilterHealthy.setTextColor("HEALTHY".equals(currentFilter) ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.primary_emerald));

        btnFilterAttention.setBackgroundResource("ATTENTION".equals(currentFilter) ? R.drawable.bg_button_emerald : R.drawable.bg_button_outline);
        btnFilterAttention.setTextColor("ATTENTION".equals(currentFilter) ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.primary_emerald));
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
        } else if (id == R.id.navSettings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.btnFilterAll) {
            loadReports("ALL");
        } else if (id == R.id.btnFilterHealthy) {
            loadReports("HEALTHY");
        } else if (id == R.id.btnFilterAttention) {
            loadReports("ATTENTION");
        }
    }

    private class ReportAdapter extends BaseAdapter {
        @Override public int getCount() { return reportItems.size(); }
        @Override public Object getItem(int position) { return reportItems.get(position); }
        @Override public long getItemId(int position) { return reportItems.get(position).getId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(ReportsActivity.this).inflate(R.layout.item_report_card, parent, false);
            }

            ImageView imgReportThumb = view.findViewById(R.id.imgReportThumb);
            TextView txtReportPanelName = view.findViewById(R.id.txtReportPanelName);
            TextView txtReportStatusTag = view.findViewById(R.id.txtReportStatusTag);
            TextView txtReportDate = view.findViewById(R.id.txtReportDate);
            TextView txtReportLossInfo = view.findViewById(R.id.txtReportLossInfo);

            ReportItem item = reportItems.get(position);
            txtReportPanelName.setText(item.getPanelName());
            txtReportDate.setText(item.getDateStr());
            txtReportStatusTag.setText(item.getCondition());

            if (item.isHealthy()) {
                txtReportStatusTag.setTextColor(ContextCompat.getColor(ReportsActivity.this, R.color.status_green));
                txtReportStatusTag.setBackgroundColor(0xFFE8F5E9);
            } else {
                txtReportStatusTag.setTextColor(ContextCompat.getColor(ReportsActivity.this, R.color.status_red));
                txtReportStatusTag.setBackgroundColor(0xFFFFEBEE);
            }

            String lossText = String.format(Locale.getDefault(), "Loss: %.1f%% • Rs. %,.0f", item.getLossPct() * 100, item.getMonthlyLossRs());
            txtReportLossInfo.setText(lossText);

            if (item.getImagePath() != null) {
                Bitmap bmp = ImageUtils.decodeSampledBitmapFromFile(item.getImagePath(), 120, 120);
                if (bmp != null) imgReportThumb.setImageBitmap(bmp);
            }

            return view;
        }
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); loadReports(currentFilter); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "Lifecycle: onDestroy"); }
}

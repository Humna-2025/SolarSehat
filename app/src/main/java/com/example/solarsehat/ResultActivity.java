package com.example.solarsehat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SolarSehat_Result";

    private ImageView imgResultAnnotated;
    private TextView txtResultCondition, txtResultDisagreement, txtResultLossPct, txtResultMonthlyLoss, txtResultAdvice, txtResultBoxList;
    private LinearLayout cardDisagreement;
    private Button btnBack, btnSaveReport, btnShareReport, btnDialCleaner;

    private String cleanerPhone = "03001234567";
    private String primaryCondition = "Clean";
    private float maxLossFraction = 0.0f;
    private double monthlyLossRs = 0.0;
    private String reportSummaryText = "";
    private String photoPath = null;
    private Bitmap annotatedBitmap = null;
    private ReportDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Log.d(TAG, "Lifecycle: onCreate");

        dbHelper = new ReportDatabaseHelper(this);

        btnBack = findViewById(R.id.btnBack);
        imgResultAnnotated = findViewById(R.id.imgResultAnnotated);
        txtResultCondition = findViewById(R.id.txtResultCondition);
        txtResultDisagreement = findViewById(R.id.txtResultDisagreement);
        cardDisagreement = findViewById(R.id.cardDisagreement);
        txtResultLossPct = findViewById(R.id.txtResultLossPct);
        txtResultMonthlyLoss = findViewById(R.id.txtResultMonthlyLoss);
        txtResultAdvice = findViewById(R.id.txtResultAdvice);
        txtResultBoxList = findViewById(R.id.txtResultBoxList);

        btnSaveReport = findViewById(R.id.btnSaveReport);
        btnShareReport = findViewById(R.id.btnShareReport);
        btnDialCleaner = findViewById(R.id.btnDialCleaner);

        if (btnBack != null) btnBack.setOnClickListener(this);
        if (btnSaveReport != null) btnSaveReport.setOnClickListener(this);
        if (btnShareReport != null) btnShareReport.setOnClickListener(this);
        if (btnDialCleaner != null) btnDialCleaner.setOnClickListener(this);

        processResultsFromIntent();
    }

    private void processResultsFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        photoPath = intent.getStringExtra("PHOTO_PATH");
        String classifierLabel = intent.getStringExtra("CLASSIFIER_LABEL");
        float classifierConf = intent.getFloatExtra("CLASSIFIER_CONF", 0.0f);

        List<Detection> detections = null;
        try {
            @SuppressWarnings("unchecked")
            List<Detection> extraDetections = (List<Detection>) intent.getSerializableExtra("DETECTIONS");
            detections = extraDetections;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing detections extra", e);
        }
        if (detections == null) detections = new ArrayList<>();

        double systemKw = parseDoubleOrDefault(intent.getStringExtra("SYSTEM_KW"), 5.0);
        double sunHours = parseDoubleOrDefault(intent.getStringExtra("SUN_HOURS"), 5.5);
        double rupeeRate = parseDoubleOrDefault(intent.getStringExtra("RUPEE_RATE"), 55.0);
        cleanerPhone = intent.getStringExtra("CLEANER_PHONE");
        if (cleanerPhone == null || cleanerPhone.isEmpty()) cleanerPhone = "03001234567";

        primaryCondition = LossTable.determinePrimaryCondition(classifierLabel, classifierConf, detections);

        String yoloTopLabel = (!detections.isEmpty()) ? detections.get(0).getLabel() : "Clean";
        boolean isDisagreed = !LossTable.normalizeClassName(classifierLabel).equals(LossTable.normalizeClassName(yoloTopLabel));

        if (isDisagreed && classifierLabel != null) {
            cardDisagreement.setVisibility(View.VISIBLE);
            txtResultDisagreement.setText(String.format(Locale.getDefault(), "Notice: Classifier predicted '%s' (%.0f%%) while YOLO detected '%s'. Showing combined fusion report.", classifierLabel, classifierConf * 100, yoloTopLabel));
        } else {
            cardDisagreement.setVisibility(View.GONE);
        }

        txtResultCondition.setText(primaryCondition);

        maxLossFraction = LossTable.calculateMaxLossFraction(primaryCondition, detections);
        txtResultLossPct.setText(String.format(Locale.getDefault(), "Estimated Output Loss: %.1f%%", maxLossFraction * 100));

        monthlyLossRs = LossTable.calculateMonthlyRupeeLoss(systemKw, sunHours, rupeeRate, maxLossFraction);
        txtResultMonthlyLoss.setText(String.format(Locale.getDefault(), "Estimated Monthly Loss: Rs. %,.2f", monthlyLossRs));

        String advice = LossTable.getCleaningAdvice(primaryCondition);
        txtResultAdvice.setText(advice);

        StringBuilder boxSb = new StringBuilder();
        if (detections.isEmpty()) {
            boxSb.append("No specific defect areas boxed by YOLO detector.");
        } else {
            for (int i = 0; i < detections.size(); i++) {
                Detection d = detections.get(i);
                boxSb.append(String.format(Locale.getDefault(), "%d. %s (%.1f%% confidence)\n", i + 1, d.getLabel(), d.getConfidence() * 100));
            }
        }
        txtResultBoxList.setText(boxSb.toString().trim());

        if (photoPath != null) {
            Bitmap origBitmap = ImageUtils.decodeSampledBitmapFromFile(photoPath, 800, 800);
            if (origBitmap != null) {
                annotatedBitmap = ImageUtils.drawDetectionsOnBitmap(origBitmap, detections);
                imgResultAnnotated.setImageBitmap(annotatedBitmap);
            }
        }

        reportSummaryText = String.format(Locale.getDefault(), "SolarSehat Inspection Report:\nCondition: %s\nEstimated Output Loss: %.1f%%\nEstimated Monthly Loss: Rs. %,.2f\nCleaning Advice: %s", primaryCondition, maxLossFraction * 100, monthlyLossRs, advice);
    }

    private double parseDoubleOrDefault(String val, double defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Double.parseDouble(val); } catch (Exception e) { return defaultVal; }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBack) {
            finish();
        } else if (id == R.id.btnSaveReport) {
            saveReportToDatabase();
        } else if (id == R.id.btnShareReport) {
            shareReportIntent();
        } else if (id == R.id.btnDialCleaner) {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + cleanerPhone));
            startActivity(dialIntent);
        }
    }

    private void saveReportToDatabase() {
        String dateStr = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date());
        boolean isHealthy = "Clean".equalsIgnoreCase(primaryCondition);
        String advice = LossTable.getCleaningAdvice(primaryCondition);

        ReportItem item = new ReportItem(0, dateStr, "Panel Scan", primaryCondition, maxLossFraction, monthlyLossRs, advice, photoPath, isHealthy);
        long id = dbHelper.insertReport(item);

        if (id > 0) {
            Toast.makeText(this, "Report saved to local database!", Toast.LENGTH_SHORT).show();
            if (btnSaveReport != null) btnSaveReport.setEnabled(false);
        } else {
            Toast.makeText(this, "Failed to save report.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReportIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SolarSehat Inspection Report");
        shareIntent.putExtra(Intent.EXTRA_TEXT, reportSummaryText);

        if (annotatedBitmap != null) {
            try {
                File cachePath = new File(getCacheDir(), "images");
                if (!cachePath.exists()) {
                    boolean created = cachePath.mkdirs();
                    Log.d(TAG, "Cache dir created: " + created);
                }
                File imageFile = new File(cachePath, "solar_report.png");
                FileOutputStream stream = new FileOutputStream(imageFile);
                annotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();

                Uri contentUri = FileProvider.getUriForFile(this, "com.example.solarsehat.fileprovider", imageFile);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.setType("image/png");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                Log.e(TAG, "Error sharing image", e);
            }
        }
        startActivity(Intent.createChooser(shareIntent, "Share Report via"));
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "Lifecycle: onDestroy"); }
}

package com.example.solarsehat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SolarSehat_Scan";

    private Spinner spinnerYoloModel;
    private ImageButton btnBack;
    private Button btnTakePhoto, btnPickGallery, btnAnalyze, btnViewReport;
    private ImageView imgScanPreview;
    private OverlayView overlayView;
    private TextView txtScanPlaceholder;
    private LinearLayout navHome, navScan, navReports, navSettings;

    private String selectedYoloVersion = "YOLOv8n";
    private String selectedPrecision = "FP16";
    private String currentPhotoPath = null;
    private Bitmap currentBitmap = null;

    private YoloDetector yoloDetector;
    private PanelClassifier panelClassifier;
    private List<Detection> lastDetections = new ArrayList<>();
    private PanelClassifier.ClassifierResult lastClassifierResult = null;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Log.d(TAG, "Lifecycle: onCreate");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PRECISION")) {
            selectedPrecision = intent.getStringExtra("PRECISION");
        }

        btnBack = findViewById(R.id.btnBack);
        spinnerYoloModel = findViewById(R.id.spinnerYoloModel);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnPickGallery = findViewById(R.id.btnPickGallery);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnViewReport = findViewById(R.id.btnViewReport);
        imgScanPreview = findViewById(R.id.imgScanPreview);
        overlayView = findViewById(R.id.overlayView);
        txtScanPlaceholder = findViewById(R.id.txtScanPlaceholder);

        navHome = findViewById(R.id.navHome);
        navScan = findViewById(R.id.navScan);
        navReports = findViewById(R.id.navReports);
        navSettings = findViewById(R.id.navSettings);

        if (btnBack != null) btnBack.setOnClickListener(this);
        btnTakePhoto.setOnClickListener(this);
        btnPickGallery.setOnClickListener(this);
        btnAnalyze.setOnClickListener(this);
        btnViewReport.setOnClickListener(this);

        navHome.setOnClickListener(this);
        navScan.setOnClickListener(this);
        navReports.setOnClickListener(this);
        navSettings.setOnClickListener(this);

        setupSpinner();
        setupActivityLaunchers();
        initClassifier();
    }

    private void setupSpinner() {
        String[] models = new String[]{"YOLOv8n", "YOLOv5n", "YOLO11n"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, models);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYoloModel.setAdapter(adapter);

        spinnerYoloModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYoloVersion = models[position];
                initYoloDetector();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void initYoloDetector() {
        if (yoloDetector != null) {
            yoloDetector.close();
            yoloDetector = null;
        }

        List<String> yoloLabels = ModelManager.loadLabels(this, "labels_yolo.txt");
        String modelFilename = ModelManager.getModelFilename(selectedYoloVersion, selectedPrecision);

        yoloDetector = new YoloDetector(this, modelFilename, yoloLabels);
        if (!yoloDetector.isInitialized()) {
            Toast.makeText(this, "Notice: Running offline with demo detections for " + selectedYoloVersion, Toast.LENGTH_SHORT).show();
        }
    }

    private void initClassifier() {
        List<String> classifierLabels = ModelManager.loadLabels(this, "labels_classifier.txt");
        panelClassifier = new PanelClassifier(this, "panel_classifier.tflite", classifierLabels);
    }

    private void setupActivityLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && currentPhotoPath != null) {
                            loadPhotoPreview(currentPhotoPath);
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                currentPhotoPath = ImageUtils.copyUriToCacheFile(ScanActivity.this, uri);
                                if (currentPhotoPath != null) {
                                    loadPhotoPreview(currentPhotoPath);
                                }
                            }
                        }
                    }
                });
    }

    private void loadPhotoPreview(String path) {
        currentBitmap = ImageUtils.decodeSampledBitmapFromFile(path, 800, 800);
        if (currentBitmap != null) {
            imgScanPreview.setImageBitmap(currentBitmap);
            txtScanPlaceholder.setVisibility(View.GONE);
            overlayView.clear();
            btnViewReport.setEnabled(false);
            lastDetections.clear();
            lastClassifierResult = null;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBack || id == R.id.navHome) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.navReports) {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.navSettings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.btnTakePhoto) {
            dispatchTakePictureIntent();
        } else if (id == R.id.btnPickGallery) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        } else if (id == R.id.btnAnalyze) {
            performInference();
        } else if (id == R.id.btnViewReport) {
            openResultActivity();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = ImageUtils.createTempImageFile(this);
            if (photoFile != null) {
                currentPhotoPath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.solarsehat.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error launching camera", ex);
        }
    }

    private void performInference() {
        if (currentBitmap == null) {
            Toast.makeText(this, "Please select or take a photo first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (yoloDetector != null && yoloDetector.isInitialized()) {
            lastDetections = yoloDetector.detect(currentBitmap);
        } else {
            // Mock detection fallback for offline testing
            lastDetections = new ArrayList<>();
            lastDetections.add(new Detection(new android.graphics.RectF(0.2f, 0.2f, 0.6f, 0.6f), "Dusty", 0.88f, 1));
        }

        if (panelClassifier != null && panelClassifier.isInitialized()) {
            lastClassifierResult = panelClassifier.classify(currentBitmap);
        } else {
            lastClassifierResult = new PanelClassifier.ClassifierResult("Dusty", 0.85f, 1);
        }

        overlayView.setDetections(lastDetections);
        btnViewReport.setEnabled(true);
        Toast.makeText(this, "Panel Analysis Complete!", Toast.LENGTH_SHORT).show();
    }

    private void openResultActivity() {
        Intent intent = new Intent(ScanActivity.this, ResultActivity.class);
        intent.putExtra("PHOTO_PATH", currentPhotoPath);
        intent.putExtra("CLASSIFIER_LABEL", (lastClassifierResult != null) ? lastClassifierResult.getLabel() : "Clean");
        intent.putExtra("CLASSIFIER_CONF", (lastClassifierResult != null) ? lastClassifierResult.getConfidence() : 0.0f);
        intent.putExtra("DETECTIONS", (Serializable) lastDetections);

        Intent homeIntent = getIntent();
        if (homeIntent != null) {
            intent.putExtra("SYSTEM_KW", homeIntent.getStringExtra("SYSTEM_KW"));
            intent.putExtra("SUN_HOURS", homeIntent.getStringExtra("SUN_HOURS"));
            intent.putExtra("RUPEE_RATE", homeIntent.getStringExtra("RUPEE_RATE"));
            intent.putExtra("CLEANER_PHONE", homeIntent.getStringExtra("CLEANER_PHONE"));
        }

        startActivity(intent);
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Lifecycle: onDestroy");
        if (yoloDetector != null) yoloDetector.close();
        if (panelClassifier != null) panelClassifier.close();
    }
}

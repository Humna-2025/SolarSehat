package com.example.solarsehat;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SolarSehat_Benchmark";

    private TextView txtBenchmarkResults;
    private TextView txtBenchmarkMetrics;
    private TextView txtBenchmarkInfo;
    private Button btnBack, btnRunBenchmark;

    private String currentPrecision = "FP16";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benchmark);
        Log.d(TAG, "Lifecycle: onCreate");

        if (getIntent() != null && getIntent().hasExtra("PRECISION")) {
            currentPrecision = getIntent().getStringExtra("PRECISION");
        }

        btnBack = findViewById(R.id.btnBack);
        txtBenchmarkResults = findViewById(R.id.txtBenchmarkResults);
        txtBenchmarkMetrics = findViewById(R.id.txtBenchmarkMetrics);
        txtBenchmarkInfo = findViewById(R.id.txtBenchmarkInfo);
        btnRunBenchmark = findViewById(R.id.btnRunBenchmark);

        btnBack.setOnClickListener(this);
        btnRunBenchmark.setOnClickListener(this);

        loadStaticBenchmarkDocs();
        runBenchmarkInference();
    }

    private void loadStaticBenchmarkDocs() {
        txtBenchmarkMetrics.setText(loadAssetFileContent("metrics.csv"));
        txtBenchmarkInfo.setText(loadAssetFileContent("model_info.txt"));
    }

    private String loadAssetFileContent(String filename) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            sb.append("Error loading ").append(filename).append(": ").append(e.getMessage());
        }
        return sb.toString().trim();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBack) {
            finish();
        } else if (id == R.id.btnRunBenchmark) {
            runBenchmarkInference();
        }
    }

    private void runBenchmarkInference() {
        txtBenchmarkResults.setText("Running 10 benchmark iterations per YOLO model on-device...");
        btnRunBenchmark.setEnabled(false);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap sampleBitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
                List<String> yoloLabels = ModelManager.loadLabels(BenchmarkActivity.this, "labels_yolo.txt");
                String[] versions = new String[]{"YOLOv5n", "YOLOv8n", "YOLO11n"};

                final StringBuilder resultsSb = new StringBuilder();
                resultsSb.append("On-Device Latency Test (10 runs, ").append(currentPrecision).append("):\n");

                for (String v : versions) {
                    String modelFilename = ModelManager.getModelFilename(v, currentPrecision);
                    if (!ModelManager.isAssetFileExists(BenchmarkActivity.this, modelFilename)) {
                        resultsSb.append("• ").append(v).append(": File missing (").append(modelFilename).append(")\n");
                        continue;
                    }

                    YoloDetector detector = new YoloDetector(BenchmarkActivity.this, modelFilename, yoloLabels);
                    if (!detector.isInitialized()) {
                        resultsSb.append("• ").append(v).append(": Initialization failed\n");
                        continue;
                    }

                    // Warmup
                    detector.detect(sampleBitmap);

                    long startTime = System.currentTimeMillis();
                    int lastCount = 0;
                    for (int i = 0; i < 10; i++) {
                        List<Detection> dets = detector.detect(sampleBitmap);
                        lastCount = dets.size();
                    }
                    long totalTime = System.currentTimeMillis() - startTime;
                    double avgLatencyMs = totalTime / 10.0;

                    long sizeKb = getAssetFileSizeKb(modelFilename);
                    detector.close();

                    resultsSb.append("• ").append(v).append(" (").append(currentPrecision).append("): Avg Latency ")
                            .append(String.format("%.1f", avgLatencyMs)).append(" ms | Size ")
                            .append(sizeKb).append(" KB | Detections: ").append(lastCount).append("\n");
                }

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtBenchmarkResults.setText(resultsSb.toString().trim());
                        btnRunBenchmark.setEnabled(true);
                        Toast.makeText(BenchmarkActivity.this, "Benchmark completed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private long getAssetFileSizeKb(String filename) {
        try {
            AssetFileDescriptor fd = getAssets().openFd(filename);
            long bytes = fd.getDeclaredLength();
            fd.close();
            return bytes / 1024;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Lifecycle: onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Lifecycle: onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Lifecycle: onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Lifecycle: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Lifecycle: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "Lifecycle: onDestroy"); executorService.shutdown(); }
}

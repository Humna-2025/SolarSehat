package com.example.solarsehat;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * ModelManager manages TFLite asset filenames, asset checks, and label loading.
 * Provides flat model names and safe loading to avoid app crashes.
 */
public class ModelManager {

    /**
     * Constructs the exact flat asset filename for a given YOLO version and precision.
     */
    public static String getModelFilename(String yoloVersion, String precision) {
        boolean isFp32 = "FP32".equalsIgnoreCase(precision);
        if (yoloVersion == null) yoloVersion = "YOLOv5n";

        String lower = yoloVersion.toLowerCase();
        if (lower.contains("v8") || lower.contains("yolov8")) {
            return isFp32 ? "yolov8n_fp32.tflite" : "yolov8n.tflite";
        } else if (lower.contains("11") || lower.contains("yolo11")) {
            return isFp32 ? "yolo11n_fp32.tflite" : "yolo11n.tflite";
        } else {
            return isFp32 ? "yolov5n_fp32.tflite" : "yolov5n.tflite";
        }
    }

    /**
     * Checks if an asset file exists in app/src/main/assets.
     */
    public static boolean isAssetFileExists(Context context, String filename) {
        if (context == null || filename == null) return false;
        try {
            String[] files = context.getAssets().list("");
            if (files != null) {
                for (String f : files) {
                    if (f.equalsIgnoreCase(filename)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Loads labels line-by-line from asset file (e.g., labels_yolo.txt, labels_classifier.txt).
     */
    public static List<String> loadLabels(Context context, String filename) {
        List<String> labels = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    labels.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return labels;
    }

    /**
     * Loads a raw model file from assets into a MappedByteBuffer for TFLite Interpreter.
     */
    public static MappedByteBuffer loadModelFile(Context context, String modelFilename) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}

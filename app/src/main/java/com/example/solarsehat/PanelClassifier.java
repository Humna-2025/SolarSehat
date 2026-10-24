package com.example.solarsehat;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class PanelClassifier {

    private static final String TAG = "SolarSehat_Classifier";
    private static final int INPUT_SIZE = 224;
    private Interpreter interpreter;
    private List<String> labels;

    public PanelClassifier(Context context, String modelFilename, List<String> labels) {
        this.labels = labels;
        try {
            if (ModelManager.isAssetFileExists(context, modelFilename)) {
                ByteBuffer modelBuffer = ModelManager.loadModelFile(context, modelFilename);
                interpreter = new Interpreter(modelBuffer);
                Log.d(TAG, "Classifier loaded successfully: " + modelFilename);
            } else {
                Log.w(TAG, "Classifier model not found in assets: " + modelFilename);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing classifier: " + e.getMessage(), e);
        }
    }

    public boolean isInitialized() {
        return interpreter != null;
    }

    public ClassifierResult classify(Bitmap bitmap) {
        if (interpreter == null || bitmap == null) {
            return new ClassifierResult("Clean", 0.0f, 0);
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resized);

        int numClasses = (labels != null && !labels.isEmpty()) ? labels.size() : 5;
        float[][] output = new float[1][numClasses];

        interpreter.run(inputBuffer, output);

        int maxIdx = 0;
        float maxProb = output[0][0];
        for (int i = 1; i < numClasses; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIdx = i;
            }
        }

        String label = (labels != null && maxIdx < labels.size()) ? labels.get(maxIdx) : "Class_" + maxIdx;
        return new ClassifierResult(label, maxProb, maxIdx);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF));
                byteBuffer.putFloat(((val >> 8) & 0xFF));
                byteBuffer.putFloat((val & 0xFF));
            }
        }
        return byteBuffer;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public static class ClassifierResult {
        private final String label;
        private final float confidence;
        private final int classIndex;

        public ClassifierResult(String label, float confidence, int classIndex) {
            this.label = label;
            this.confidence = confidence;
            this.classIndex = classIndex;
        }

        public String getLabel() { return label; }
        public float getConfidence() { return confidence; }
        public int getClassIndex() { return classIndex; }
    }
}

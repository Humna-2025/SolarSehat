package com.example.solarsehat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class YoloDetector {

    private static final String TAG = "SolarSehat_Yolo";
    private static final int INPUT_SIZE = 320;
    private static final float CONF_THRESHOLD = 0.25f;
    private static final float IOU_THRESHOLD = 0.45f;

    private Interpreter interpreter;
    private List<String> labels;

    public YoloDetector(Context context, String modelFilename, List<String> labels) {
        this.labels = labels;
        try {
            if (ModelManager.isAssetFileExists(context, modelFilename)) {
                ByteBuffer modelBuffer = ModelManager.loadModelFile(context, modelFilename);
                interpreter = new Interpreter(modelBuffer);
                Log.d(TAG, "YOLO Interpreter loaded successfully: " + modelFilename);
            } else {
                Log.w(TAG, "Model file not found in assets: " + modelFilename);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing YOLO interpreter: " + e.getMessage(), e);
        }
    }

    public boolean isInitialized() {
        return interpreter != null;
    }

    public List<Detection> detect(Bitmap bitmap) {
        if (interpreter == null || bitmap == null) {
            return new ArrayList<>();
        }

        Bitmap letterboxed = ImageUtils.letterboxBitmap(bitmap, INPUT_SIZE, INPUT_SIZE);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(letterboxed);

        int[] outShape = interpreter.getOutputTensor(0).shape();
        int numClasses = (labels != null && !labels.isEmpty()) ? labels.size() : 5;

        List<Detection> proposals = new ArrayList<>();

        if (outShape.length == 3) {
            int dim1 = outShape[1];
            int dim2 = outShape[2];

            if (dim1 < dim2) {
                // Layout [1, C, N]
                float[][][] output = new float[1][dim1][dim2];
                interpreter.run(inputBuffer, output);

                int numBoxes = dim2;
                int channels = dim1;
                for (int i = 0; i < numBoxes; i++) {
                    float cx = output[0][0][i];
                    float cy = output[0][1][i];
                    float w = output[0][2][i];
                    float h = output[0][3][i];

                    float maxClassScore = 0.0f;
                    int maxClassIdx = -1;
                    for (int c = 4; c < channels; c++) {
                        float score = output[0][c][i];
                        if (score > maxClassScore) {
                            maxClassScore = score;
                            maxClassIdx = c - 4;
                        }
                    }

                    if (maxClassScore >= CONF_THRESHOLD && maxClassIdx >= 0) {
                        RectF rect = parseBoxCoordinates(cx, cy, w, h);
                        String label = (labels != null && maxClassIdx < labels.size()) ? labels.get(maxClassIdx) : "Defect_" + maxClassIdx;
                        proposals.add(new Detection(rect, label, maxClassScore, maxClassIdx));
                    }
                }
            } else {
                // Layout [1, N, C]
                float[][][] output = new float[1][dim1][dim2];
                interpreter.run(inputBuffer, output);

                int numBoxes = dim1;
                int channels = dim2;
                for (int i = 0; i < numBoxes; i++) {
                    float cx = output[0][i][0];
                    float cy = output[0][i][1];
                    float w = output[0][i][2];
                    float h = output[0][i][3];

                    float maxClassScore = 0.0f;
                    int maxClassIdx = -1;
                    for (int c = 4; c < channels; c++) {
                        float score = output[0][i][c];
                        if (score > maxClassScore) {
                            maxClassScore = score;
                            maxClassIdx = c - 4;
                        }
                    }

                    if (maxClassScore >= CONF_THRESHOLD && maxClassIdx >= 0) {
                        RectF rect = parseBoxCoordinates(cx, cy, w, h);
                        String label = (labels != null && maxClassIdx < labels.size()) ? labels.get(maxClassIdx) : "Defect_" + maxClassIdx;
                        proposals.add(new Detection(rect, label, maxClassScore, maxClassIdx));
                    }
                }
            }
        }

        return applyNMS(proposals);
    }

    private RectF parseBoxCoordinates(float cx, float cy, float w, float h) {
        float left, top, right, bottom;
        if (cx > 1.0f || cy > 1.0f || w > 1.0f || h > 1.0f) {
            left = (cx - w / 2.0f) / INPUT_SIZE;
            top = (cy - h / 2.0f) / INPUT_SIZE;
            right = (cx + w / 2.0f) / INPUT_SIZE;
            bottom = (cy + h / 2.0f) / INPUT_SIZE;
        } else {
            left = cx - w / 2.0f;
            top = cy - h / 2.0f;
            right = cx + w / 2.0f;
            bottom = cy + h / 2.0f;
        }

        left = Math.max(0.0f, Math.min(1.0f, left));
        top = Math.max(0.0f, Math.min(1.0f, top));
        right = Math.max(0.0f, Math.min(1.0f, right));
        bottom = Math.max(0.0f, Math.min(1.0f, bottom));

        return new RectF(left, top, right, bottom);
    }

    private List<Detection> applyNMS(List<Detection> proposals) {
        List<Detection> nmsList = new ArrayList<>();
        Collections.sort(proposals, new Comparator<Detection>() {
            @Override
            public int compare(Detection d1, Detection d2) {
                return Float.compare(d2.getConfidence(), d1.getConfidence());
            }
        });

        boolean[] suppressed = new boolean[proposals.size()];
        for (int i = 0; i < proposals.size(); i++) {
            if (suppressed[i]) continue;
            Detection d1 = proposals.get(i);
            nmsList.add(d1);

            for (int j = i + 1; j < proposals.size(); j++) {
                if (suppressed[j]) continue;
                Detection d2 = proposals.get(j);
                if (calculateIoU(d1.getBox(), d2.getBox()) > IOU_THRESHOLD) {
                    suppressed[j] = true;
                }
            }
        }
        return nmsList;
    }

    private float calculateIoU(RectF a, RectF b) {
        float interLeft = Math.max(a.left, b.left);
        float interTop = Math.max(a.top, b.top);
        float interRight = Math.min(a.right, b.right);
        float interBottom = Math.min(a.bottom, b.bottom);

        float interArea = Math.max(0.0f, interRight - interLeft) * Math.max(0.0f, interBottom - interTop);
        float aArea = Math.max(0.0f, a.right - a.left) * Math.max(0.0f, a.bottom - a.top);
        float bArea = Math.max(0.0f, b.right - b.left) * Math.max(0.0f, b.bottom - b.top);

        float unionArea = aArea + bArea - interArea;
        return (unionArea > 0) ? (interArea / unionArea) : 0.0f;
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
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((val & 0xFF) / 255.0f);
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
}

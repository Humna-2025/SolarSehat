package com.example.solarsehat;

import android.graphics.RectF;
import java.io.Serializable;

/**
 * Detection data class holding bounding box coordinates, defect label, and confidence score.
 * Implements Serializable to pass detections between Activities via Intent extras.
 */
public class Detection implements Serializable {
    private final float left;
    private final float top;
    private final float right;
    private final float bottom;
    private final String label;
    private final float confidence;
    private final int classIndex;

    public Detection(RectF box, String label, float confidence, int classIndex) {
        this.left = box != null ? box.left : 0;
        this.top = box != null ? box.top : 0;
        this.right = box != null ? box.right : 0;
        this.bottom = box != null ? box.bottom : 0;
        this.label = label;
        this.confidence = confidence;
        this.classIndex = classIndex;
    }

    public RectF getBox() {
        return new RectF(left, top, right, bottom);
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getClassIndex() {
        return classIndex;
    }

    @Override
    public String toString() {
        return label + " (" + String.format("%.1f", confidence * 100) + "%)";
    }
}

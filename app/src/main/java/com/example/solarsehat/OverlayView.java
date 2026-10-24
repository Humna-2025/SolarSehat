package com.example.solarsehat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    private List<Detection> detections = new ArrayList<>();
    private Paint boxPaint;
    private Paint textPaint;
    private Paint textBgPaint;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        boxPaint = new Paint();
        boxPaint.setColor(Color.parseColor("#FFD54F")); // Sunshine Yellow
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6.0f);
        boxPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36.0f);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);

        textBgPaint = new Paint();
        textBgPaint.setColor(Color.parseColor("#37474F")); // Slate Gray
        textBgPaint.setStyle(Paint.Style.FILL);
        textBgPaint.setAntiAlias(true);
    }

    public void setDetections(List<Detection> detections) {
        this.detections = (detections != null) ? detections : new ArrayList<Detection>();
        invalidate();
    }

    public void clear() {
        this.detections.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (detections == null || detections.isEmpty()) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        for (Detection d : detections) {
            RectF box = d.getBox();
            if (box == null) continue;

            float left = box.left * viewWidth;
            float top = box.top * viewHeight;
            float right = box.right * viewWidth;
            float bottom = box.bottom * viewHeight;

            canvas.drawRect(left, top, right, bottom, boxPaint);

            String labelText = d.getLabel() + " " + String.format("%.0f%%", d.getConfidence() * 100);
            float textWidth = textPaint.measureText(labelText);
            float textHeight = textPaint.getTextSize();

            float badgeTop = Math.max(0, top - textHeight - 12);
            canvas.drawRect(left, badgeTop, left + textWidth + 16, badgeTop + textHeight + 12, textBgPaint);
            canvas.drawText(labelText, left + 8, badgeTop + textHeight + 2, textPaint);
        }
    }
}

package com.example.solarsehat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.File;
import java.util.List;

/**
 * Utility class for image decoding, downsampling, and bounding box drawing.
 * Essential to avoid OutOfMemoryError on Android camera high-resolution photos.
 */
public class ImageUtils {

    /**
     * Safely decodes a bitmap from file path with downsampling to target width/height.
     */
    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        if (filePath == null || !new File(filePath).exists()) return null;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(filePath, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Draws bounding boxes and labels on a copy of the source Bitmap for display and sharing.
     */
    public static Bitmap drawDetectionsOnBitmap(Bitmap src, List<Detection> detections) {
        if (src == null) return null;

        Bitmap mutableBitmap = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint boxPaint = new Paint();
        boxPaint.setColor(Color.parseColor("#FFD54F")); // Sunshine Yellow
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(Math.max(4f, src.getWidth() / 150f));

        Paint textBgPaint = new Paint();
        textBgPaint.setColor(Color.parseColor("#37474F")); // Slate Gray background
        textBgPaint.setStyle(Paint.Style.FILL);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(Math.max(24f, src.getWidth() / 30f));
        textPaint.setAntiAlias(true);

        if (detections != null) {
            for (Detection d : detections) {
                RectF box = d.getBox();
                canvas.drawRect(box, boxPaint);

                String text = d.getLabel() + " " + String.format("%.0f%%", d.getConfidence() * 100);
                float textWidth = textPaint.measureText(text);
                float textHeight = textPaint.getTextSize();

                RectF textBg = new RectF(box.left, box.top - textHeight - 12, box.left + textWidth + 16, box.top);
                canvas.drawRect(textBg, textBgPaint);
                canvas.drawText(text, box.left + 8, box.top - 8, textPaint);
            }
        }

        return mutableBitmap;
    }

    /**
     * Resizes a bitmap into a target square (e.g., 320x320) maintaining aspect ratio with gray 114 letterboxing padding.
     */
    public static Bitmap letterboxBitmap(Bitmap src, int targetWidth, int targetHeight) {
        if (src == null) return null;
        Bitmap result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.rgb(114, 114, 114));

        float scale = Math.min((float) targetWidth / src.getWidth(), (float) targetHeight / src.getHeight());
        int dx = Math.round((targetWidth - src.getWidth() * scale) / 2.0f);
        int dy = Math.round((targetHeight - src.getHeight() * scale) / 2.0f);

        Bitmap scaled = Bitmap.createScaledBitmap(src, Math.round(src.getWidth() * scale), Math.round(src.getHeight() * scale), true);
        canvas.drawBitmap(scaled, dx, dy, null);
        return result;
    }

    public static File createTempImageFile(android.content.Context context) {
        try {
            File storageDir = new File(context.getCacheDir(), "camera_photos");
            if (!storageDir.exists()) storageDir.mkdirs();
            return File.createTempFile("solar_scan_", ".jpg", storageDir);
        } catch (Exception e) {
            return null;
        }
    }

    public static String copyUriToCacheFile(android.content.Context context, android.net.Uri uri) {
        try {
            java.io.InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File tempFile = createTempImageFile(context);
            if (tempFile == null) return null;
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}

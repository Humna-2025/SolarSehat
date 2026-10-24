package com.example.solarsehat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class ReportDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "solarsehat_reports.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REPORTS = "reports";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date_str";
    private static final String COLUMN_PANEL = "panel_name";
    private static final String COLUMN_CONDITION = "condition";
    private static final String COLUMN_LOSS_PCT = "loss_pct";
    private static final String COLUMN_MONTHLY_LOSS = "monthly_loss";
    private static final String COLUMN_ADVICE = "advice";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_IS_HEALTHY = "is_healthy";

    public ReportDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_REPORTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_PANEL + " TEXT, " +
                COLUMN_CONDITION + " TEXT, " +
                COLUMN_LOSS_PCT + " REAL, " +
                COLUMN_MONTHLY_LOSS + " REAL, " +
                COLUMN_ADVICE + " TEXT, " +
                COLUMN_IMAGE_PATH + " TEXT, " +
                COLUMN_IS_HEALTHY + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        onCreate(db);
    }

    public long insertReport(ReportItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, item.getDateStr());
        values.put(COLUMN_PANEL, item.getPanelName());
        values.put(COLUMN_CONDITION, item.getCondition());
        values.put(COLUMN_LOSS_PCT, item.getLossPct());
        values.put(COLUMN_MONTHLY_LOSS, item.getMonthlyLossRs());
        values.put(COLUMN_ADVICE, item.getAdvice());
        values.put(COLUMN_IMAGE_PATH, item.getImagePath());
        values.put(COLUMN_IS_HEALTHY, item.isHealthy() ? 1 : 0);

        long id = db.insert(TABLE_REPORTS, null, values);
        db.close();
        return id;
    }

    public List<ReportItem> getAllReports(String filter) {
        List<ReportItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;

        if ("HEALTHY".equalsIgnoreCase(filter)) {
            selection = COLUMN_IS_HEALTHY + " = ?";
            selectionArgs = new String[]{"1"};
        } else if ("ATTENTION".equalsIgnoreCase(filter)) {
            selection = COLUMN_IS_HEALTHY + " = ?";
            selectionArgs = new String[]{"0"};
        }

        Cursor cursor = db.query(TABLE_REPORTS, null, selection, selectionArgs, null, null, COLUMN_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String panel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANEL));
                String condition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION));
                float lossPct = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_LOSS_PCT));
                double monthlyLoss = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_MONTHLY_LOSS));
                String advice = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADVICE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                boolean isHealthy = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_HEALTHY)) == 1;

                list.add(new ReportItem(id, dateStr, panel, condition, lossPct, monthlyLoss, advice, imagePath, isHealthy));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    public ReportItem getLatestReport() {
        List<ReportItem> reports = getAllReports("ALL");
        return (!reports.isEmpty()) ? reports.get(0) : null;
    }

    public int getReportCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REPORTS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
}

package com.example.bmifitnesstracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "BMI_Fitness_Tracker.db";
    private static final int DATABASE_VERSION = 2; // Incremented version

    // Table name
    public static final String TABLE_MEASUREMENTS = "measurements";

    // Column names
    public static final String COL_ID = "id";
    public static final String COL_DATE = "date";
    public static final String COL_WEIGHT_KG = "weight_kg";
    public static final String COL_HEIGHT_CM = "height_cm";
    public static final String COL_BMI_VALUE = "bmi_value";
    public static final String COL_CATEGORY = "category";
    public static final String COL_RISK_LEVEL = "risk_level";
    public static final String COL_INSIGHT_TEXT = "insight_text";
    public static final String COL_RECOMMENDATION_TEXT = "recommendation_text";

    private final Context context;
    private final String dbPath;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.dbPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        
        // Only copy if version is 1 or doesn't exist. 
        // For development, we want the code-defined table.
        if (!checkDatabase()) {
            copyDatabase();
        }
    }

    private boolean checkDatabase() {
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    private void copyDatabase() {
        try {
            InputStream inputStream = context.getAssets().open(DATABASE_NAME);
            File dbFile = new File(dbPath);
            if (dbFile.getParentFile() != null) {
                dbFile.getParentFile().mkdirs();
            }
            OutputStream outputStream = new FileOutputStream(dbPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            Log.d(TAG, "Database copied successfully from assets.");
        } catch (IOException e) {
            Log.e(TAG, "Error copying database from assets", e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MEASUREMENTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT, " +
                COL_WEIGHT_KG + " REAL, " +
                COL_HEIGHT_CM + " REAL, " +
                COL_BMI_VALUE + " REAL, " +
                COL_CATEGORY + " TEXT, " +
                COL_RISK_LEVEL + " TEXT, " +
                COL_INSIGHT_TEXT + " TEXT, " +
                COL_RECOMMENDATION_TEXT + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Drop old table and create new one with correct schema
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEASUREMENTS);
            db.execSQL("DROP TABLE IF EXISTS users"); // Cleanup old table
            onCreate(db);
        }
    }

    public long insertMeasurement(String date, double weightKg, double heightCm, double bmiValue, 
                                 String category, String riskLevel, String insight, String recommendation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_WEIGHT_KG, weightKg);
        values.put(COL_HEIGHT_CM, heightCm);
        values.put(COL_BMI_VALUE, bmiValue);
        values.put(COL_CATEGORY, category);
        values.put(COL_RISK_LEVEL, riskLevel);
        values.put(COL_INSIGHT_TEXT, insight);
        values.put(COL_RECOMMENDATION_TEXT, recommendation);
        
        long id = db.insert(TABLE_MEASUREMENTS, null, values);
      //  db.close();
        return id;
    }

    public List<Measurement> getAllMeasurements() {
        List<Measurement> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Ensure table exists before querying
        onCreate(db);

        Cursor cursor = db.query(TABLE_MEASUREMENTS, null, null, null, null, null, COL_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Measurement m = new Measurement();
                m.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                m.date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                m.weightKg = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT_KG));
                m.heightCm = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_HEIGHT_CM));
                m.bmiValue = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BMI_VALUE));
                m.category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                m.riskLevel = cursor.getString(cursor.getColumnIndexOrThrow(COL_RISK_LEVEL));
                m.insightText = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSIGHT_TEXT));
                m.recommendationText = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECOMMENDATION_TEXT));
                list.add(m);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public static class Measurement {
        public int id;
        public String date;
        public double weightKg;
        public double heightCm;
        public double bmiValue;
        public String category;
        public String riskLevel;
        public String insightText;
        public String recommendationText;
    }
}

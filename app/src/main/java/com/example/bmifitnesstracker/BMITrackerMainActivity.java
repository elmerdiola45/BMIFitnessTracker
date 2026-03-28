package com.example.bmifitnesstracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BMITrackerMainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private LinearLayout historyContainer;
    private TextView tvHistoryTrendInsight, tvCurrentHistoryBMI, tvCurrentHistoryStatus, tvProgressLabel, tvMilestoneTarget;
    private LinearProgressIndicator milestoneProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bmi_tracker_main_activity);

        dbHelper = new DatabaseHelper(this);

        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        EditText etHeight = findViewById(R.id.etHeight);
        EditText etWeight = findViewById(R.id.etWeight);
        MaterialButton btnCompute = findViewById(R.id.btnCompute);

        // Result views
        View resultLayout = findViewById(R.id.resultLayout);
        TextView tvBMIValue = findViewById(R.id.tvBMIValue);
        TextView tvBMICategory = findViewById(R.id.tvBMICategory);
        TextView tvBMILabel = findViewById(R.id.tvBMILabel);
        TextView tvInsightText = findViewById(R.id.tvInsightText);
        TextView tvRecommendationText = findViewById(R.id.tvRecommendationText);
        TextView tvCategoryLabel = findViewById(R.id.tvCategoryLabel);
        TextView tvCategoryValue = findViewById(R.id.tvCategoryValue);
        TextView tvRiskValue = findViewById(R.id.tvRiskValue);

        // History views
        historyContainer = findViewById(R.id.historyContainer);
        tvHistoryTrendInsight = findViewById(R.id.tvHistoryTrendInsight);
        tvCurrentHistoryBMI = findViewById(R.id.tvCurrentHistoryBMI);
        tvCurrentHistoryStatus = findViewById(R.id.tvCurrentHistoryStatus);
        tvProgressLabel = findViewById(R.id.tvProgressLabel);
        tvMilestoneTarget = findViewById(R.id.tvMilestoneTarget);
        milestoneProgress = findViewById(R.id.milestoneProgress);

        loadHistory();

        btnCompute.setOnClickListener(v -> {
            String heightStr = etHeight.getText().toString();
            String weightStr = etWeight.getText().toString();

            if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
                try {
                    double height = Double.parseDouble(heightStr);
                    double weight = Double.parseDouble(weightStr);

                    // Calculate BMI: weight (kg) / [height (m)]^2
                    double heightInMeters = height / 100.0;
                    double bmi = weight / (heightInMeters * heightInMeters);

                    String category;
                    String label;
                    String risk;
                    String insight;
                    String recommendation;
                    int tagColor;
                    int riskColor;

                    if (bmi < 18.5) {
                        category = "Underweight";
                        label = "LOW";
                        risk = "Moderate";
                        tagColor = Color.parseColor("#FF9800"); // Orange
                        riskColor = Color.parseColor("#FF9800");
                        insight = String.format(Locale.getDefault(), "Your BMI of %.1f indicates you are in the underweight category. This can lead to a weakened immune system and brittle bones.", bmi);
                        recommendation = "• Increase caloric intake with nutrient-dense foods\n• Focus on strength training\n• Eat more frequent, smaller meals\n• Consult a nutritionist";
                    } else if (bmi < 25) {
                        category = "Normal Weight";
                        label = "IDEAL";
                        risk = "Low";
                        tagColor = Color.parseColor("#4CAF50"); // Green
                        riskColor = Color.parseColor("#4CAF50");
                        insight = String.format(Locale.getDefault(), "Your BMI of %.1f indicates you are in the healthy weight range for your height. Maintaining a balanced diet and regular physical activity is key to long-term vitality.", bmi);
                        recommendation = "• Continue balanced diet (vegetables, fruits, protein)\n• Exercise regularly (at least 3-5 times a week)\n• Stay hydrated\n• Maintain consistent sleep schedule";
                    } else if (bmi < 30) {
                        category = "Overweight";
                        label = "OVERWEIGHT";
                        risk = "Moderate";
                        tagColor = Color.parseColor("#FF5722"); // Deep Orange
                        riskColor = Color.parseColor("#FF5722");
                        insight = String.format(Locale.getDefault(), "At %.1f, you are in the overweight category. This can increase stress on your joints and risk for metabolic health concerns. A moderate reduction in caloric intake combined with consistent activity can significantly improve your profile.", bmi);
                        recommendation = "• Reduce sugary and high-fat foods\n• Start regular exercise (cardio like walking, jogging)\n• Control portion sizes\n• Avoid soft drinks and junk food";
                    } else {
                        category = "Obese";
                        label = "OBESE";
                        risk = "High";
                        tagColor = Color.parseColor("#F44336"); // Red
                        riskColor = Color.parseColor("#F44336");
                        insight = String.format(Locale.getDefault(), "A BMI of %.1f indicates that your body weight is significantly higher than what is considered healthy for your height. This category is associated with an increased risk of chronic conditions such as Type 2 diabetes, hypertension, and cardiovascular disease.", bmi);
                        recommendation = "• Follow a structured diet plan (low calorie, balanced)\n• Engage in daily physical activity (walking is a good start)\n• Avoid processed foods and sugary drinks\n• Consider consulting a healthcare professional";
                    }

                    // Update UI
                    tvBMIValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));
                    tvBMICategory.setText(category);
                    tvBMILabel.setText(label);
                    tvBMILabel.getBackground().setTint(tagColor);
                    
                    tvInsightText.setText(insight);
                    tvRecommendationText.setText(recommendation);
                    
                    // Show Target BMI logic for overweight/obese as per screenshot
                    if (bmi >= 25) {
                        tvCategoryLabel.setText("TARGET BMI");
                        tvCategoryValue.setText("24.9");
                    } else {
                        tvCategoryLabel.setText("CATEGORY");
                        tvCategoryValue.setText(label.equals("IDEAL") ? "Optimal" : category);
                    }
                    
                    tvRiskValue.setText(risk);
                    tvRiskValue.setTextColor(riskColor);
                    
                    resultLayout.setVisibility(View.VISIBLE);

                    // Refined scrolling logic: Center the health insight card instead of scrolling to the absolute bottom
                    nestedScrollView.post(() -> {
                        int targetTop = resultLayout.getTop();
                        int scrollHeight = nestedScrollView.getHeight();
                        int targetHeight = resultLayout.getHeight();
                        int scrollToY = targetTop - (scrollHeight / 2) + (targetHeight / 2);
                        nestedScrollView.smoothScrollTo(0, Math.max(0, scrollToY));
                    });

                    // Format current date as YYYY-MM-DD
                    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    // Save measurement
                    dbHelper.insertMeasurement(currentDate, weight, height, bmi, category, risk, insight, recommendation);

                    // Reload History
                    loadHistory();

                    Toast.makeText(this, "Result computed and saved", Toast.LENGTH_SHORT).show();
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter height and weight", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHistory() {
        List<DatabaseHelper.Measurement> measurements = dbHelper.getAllMeasurements();
        historyContainer.removeAllViews();

        if (measurements.isEmpty()) {
            findViewById(R.id.historyLayout).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.historyLayout).setVisibility(View.VISIBLE);

        // Update Current History Summary (Top of History Section)
        DatabaseHelper.Measurement latest = measurements.get(0);
        tvCurrentHistoryBMI.setText(String.format(Locale.getDefault(), "%.1f", latest.bmiValue));
        tvCurrentHistoryStatus.setText(latest.category.toUpperCase());

        // Calculate Trend (Compare latest with previous record if available)
        if (measurements.size() > 1) {
            double diff = measurements.get(1).bmiValue - latest.bmiValue;
            String trend = diff >= 0 ? "decreased" : "increased";
            tvHistoryTrendInsight.setText(String.format(Locale.getDefault(), 
                "Your body mass index has %s by %.1f points since your last check.", trend, Math.abs(diff)));
        } else {
            tvHistoryTrendInsight.setText("Start tracking your BMI to see your progress trend!");
        }

        // Milestone Progress Logic (Target 21.5 as per screenshot)
        double targetBmi = 21.5;
        double startBmi = measurements.get(measurements.size()-1).bmiValue;
        double currentBmi = latest.bmiValue;
        
        int progress = 0;
        if (startBmi > targetBmi) {
            double totalNeed = startBmi - targetBmi;
            double achieved = startBmi - currentBmi;
            progress = (int) Math.max(0, Math.min(100, (achieved / totalNeed) * 100));
        }
        
        milestoneProgress.setProgress(progress);
        tvProgressLabel.setText(String.format(Locale.getDefault(), "%d%% ACHIEVED", progress));

        // Populate Past Records
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

        for (int i = 0; i < measurements.size(); i++) {
            DatabaseHelper.Measurement m = measurements.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_measurement, historyContainer, false);

            TextView tvDay = itemView.findViewById(R.id.tvItemDay);
            TextView tvMonth = itemView.findViewById(R.id.tvItemMonth);
            TextView tvBMI = itemView.findViewById(R.id.tvItemBMI);
            TextView tvSubDetail = itemView.findViewById(R.id.tvItemSubDetail);
            ImageView ivTrend = itemView.findViewById(R.id.ivTrend);

            try {
                Date date = dbFormat.parse(m.date);
                if (date != null) {
                    tvDay.setText(dayFormat.format(date));
                    tvMonth.setText(monthFormat.format(date).toUpperCase());
                }
            } catch (ParseException e) {
                tvDay.setText("--");
            }

            tvBMI.setText(String.format(Locale.getDefault(), "%.1f", m.bmiValue));
            tvSubDetail.setText(String.format(Locale.getDefault(), "%.1f KG • %s", m.weightKg, m.category.toUpperCase()));

            // Trend Icon for individual items
            if (i < measurements.size() - 1) {
                double prevBmi = measurements.get(i+1).bmiValue;
                if (m.bmiValue < prevBmi) {
                    ivTrend.setImageResource(android.R.drawable.arrow_down_float);
                    ivTrend.setColorFilter(Color.parseColor("#4CAF50"));
                } else if (m.bmiValue > prevBmi) {
                    ivTrend.setImageResource(android.R.drawable.arrow_up_float);
                    ivTrend.setColorFilter(Color.parseColor("#F44336"));
                } else {
                    ivTrend.setVisibility(View.GONE);
                }
            } else {
                ivTrend.setVisibility(View.GONE);
            }

            historyContainer.addView(itemView);
        }
    }
}

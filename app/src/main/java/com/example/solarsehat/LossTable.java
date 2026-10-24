package com.example.solarsehat;

import java.util.List;

/**
 * LossTable maps defect types to power loss estimates and calculates rupee loss.
 * Beginner-friendly helper class with viva-oriented comments.
 */
public class LossTable {

    // PLACEHOLDER loss fractions based on solar panel research studies.
    // TODO: Replace placeholder values with cited research references for final project defense.
    public static final float LOSS_CLEAN = 0.00f;             // 0% loss
    public static final float LOSS_DUSTY = 0.15f;             // 15% estimated loss
    public static final float LOSS_BIRD_DROP = 0.25f;         // 25% estimated loss
    public static final float LOSS_ELECTRICAL_DAMAGE = 0.40f; // 40% estimated loss
    public static final float LOSS_PHYSICAL_DAMAGE = 0.50f;   // 50% estimated loss

    /**
     * Normalizes class names by converting to lowercase and removing spaces, hyphens, and underscores.
     * Example: "Bird-drop", "bird_drop", "bird drop" all map to "birddrop".
     */
    public static String normalizeClassName(String name) {
        if (name == null) return "clean";
        return name.toLowerCase()
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .trim();
    }

    /**
     * Gets the loss fraction associated with a defect label.
     */
    public static float getLossFraction(String rawName) {
        String normalized = normalizeClassName(rawName);
        if (normalized.contains("dust")) {
            return LOSS_DUSTY;
        } else if (normalized.contains("bird")) {
            return LOSS_BIRD_DROP;
        } else if (normalized.contains("electrical")) {
            return LOSS_ELECTRICAL_DAMAGE;
        } else if (normalized.contains("physical")) {
            return LOSS_PHYSICAL_DAMAGE;
        } else {
            return LOSS_CLEAN;
        }
    }

    /**
     * Fuses classifier and YOLO results to determine primary condition string.
     * Primary condition = classifier top class if its confidence >= 0.5, else highest-confidence YOLO class.
     */
    public static String determinePrimaryCondition(String classifierClass, float classifierConf, List<Detection> detections) {
        if (classifierClass != null && classifierConf >= 0.5f) {
            return classifierClass;
        }
        if (detections != null && !detections.isEmpty()) {
            Detection topDetection = detections.get(0);
            for (Detection d : detections) {
                if (d.getConfidence() > topDetection.getConfidence()) {
                    topDetection = d;
                }
            }
            return topDetection.getLabel();
        }
        return "Clean";
    }

    /**
     * Calculates the overall loss fraction using Fusion logic:
     * Loss fraction = LARGEST single fraction among detected classes (never sum).
     */
    public static float calculateMaxLossFraction(String primaryCondition, List<Detection> detections) {
        float maxLoss = getLossFraction(primaryCondition);
        if (detections != null) {
            for (Detection d : detections) {
                float loss = getLossFraction(d.getLabel());
                if (loss > maxLoss) {
                    maxLoss = loss;
                }
            }
        }
        return maxLoss;
    }

    /**
     * Calculates Estimated Monthly Rupee Loss (Pakistani Rupees).
     * Monthly Loss (Rs) = system kW * sun hours * 30 days * loss fraction * rupees per unit (kWh).
     */
    public static double calculateMonthlyRupeeLoss(double systemKw, double sunHours, double ratePerUnit, float lossFraction) {
        double monthlyKwhGeneration = systemKw * sunHours * 30.0;
        return monthlyKwhGeneration * lossFraction * ratePerUnit;
    }

    /**
     * Returns clear, actionable plain-English advice based on defect condition.
     */
    public static String getCleaningAdvice(String condition) {
        String norm = normalizeClassName(condition);
        if (norm.contains("dust")) {
            return "Dust accumulation detected. Clean panel thoroughly using clean water and a soft microfiber cloth or squeegee. Clean in early morning or late evening when panels are cool.";
        } else if (norm.contains("bird")) {
            return "Bird drop contamination detected. Soak stubborn spots with warm soapy water before wiping gently. Do not scrub harshly to avoid scratching glass anti-reflective coating.";
        } else if (norm.contains("electrical")) {
            return "Hotspot or electrical flaw detected! Turn off solar inverter system safely and contact a certified solar technician immediately for electrical inspection.";
        } else if (norm.contains("physical")) {
            return "Physical crack or structural damage detected. Water ingress may cause short circuits. Do not spray water on cracked panel. Contact a professional installer for replacement.";
        } else {
            return "Panel is in optimal clean condition. No immediate cleaning or repair action required. Perform routine inspection next month.";
        }
    }
}

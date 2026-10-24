package com.example.solarsehat;

import java.io.Serializable;

public class ReportItem implements Serializable {

    private long id;
    private String dateStr;
    private String panelName;
    private String condition;
    private float lossPct;
    private double monthlyLossRs;
    private String advice;
    private String imagePath;
    private boolean isHealthy;

    public ReportItem(long id, String dateStr, String panelName, String condition, float lossPct, double monthlyLossRs, String advice, String imagePath, boolean isHealthy) {
        this.id = id;
        this.dateStr = dateStr;
        this.panelName = panelName;
        this.condition = condition;
        this.lossPct = lossPct;
        this.monthlyLossRs = monthlyLossRs;
        this.advice = advice;
        this.imagePath = imagePath;
        this.isHealthy = isHealthy;
    }

    public long getId() { return id; }
    public String getDateStr() { return dateStr; }
    public String getPanelName() { return panelName; }
    public String getCondition() { return condition; }
    public float getLossPct() { return lossPct; }
    public double getMonthlyLossRs() { return monthlyLossRs; }
    public String getAdvice() { return advice; }
    public String getImagePath() { return imagePath; }
    public boolean isHealthy() { return isHealthy; }
}

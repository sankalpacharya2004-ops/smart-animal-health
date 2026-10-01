package com.smartanimal.model;

import java.sql.Date;

public class VaccineRecommendation {
    private String vaccineName;
    private String shortName;
    private String stage;
    private String priority; // "Core", "Recommended", "Booster", "Prophylaxis"
    private int suggestedDueDays;
    private Date suggestedDate;
    private String frequency;
    private String description;
    private String defaultNotes;

    public VaccineRecommendation() {}

    public VaccineRecommendation(String vaccineName, String shortName, String stage, String priority, int suggestedDueDays, Date suggestedDate, String frequency, String description, String defaultNotes) {
        this.vaccineName = vaccineName;
        this.shortName = shortName;
        this.stage = stage;
        this.priority = priority;
        this.suggestedDueDays = suggestedDueDays;
        this.suggestedDate = suggestedDate;
        this.frequency = frequency;
        this.description = description;
        this.defaultNotes = defaultNotes;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getSuggestedDueDays() {
        return suggestedDueDays;
    }

    public void setSuggestedDueDays(int suggestedDueDays) {
        this.suggestedDueDays = suggestedDueDays;
    }

    public Date getSuggestedDate() {
        return suggestedDate;
    }

    public void setSuggestedDate(Date suggestedDate) {
        this.suggestedDate = suggestedDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultNotes() {
        return defaultNotes;
    }

    public void setDefaultNotes(String defaultNotes) {
        this.defaultNotes = defaultNotes;
    }
}

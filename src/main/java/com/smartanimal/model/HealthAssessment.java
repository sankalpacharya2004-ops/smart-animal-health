package com.smartanimal.model;

import java.sql.Timestamp;

public class HealthAssessment {
    private int assessmentId;
    private int symptomId;
    private int animalId;
    private String riskLevel; // 'Low', 'Medium', 'High'
    private String possibleCondition;
    private String recommendedAction;
    private Timestamp assessmentDate;

    public HealthAssessment() {}

    public HealthAssessment(int assessmentId, int symptomId, int animalId, String riskLevel, String possibleCondition, String recommendedAction, Timestamp assessmentDate) {
        this.assessmentId = assessmentId;
        this.symptomId = symptomId;
        this.animalId = animalId;
        this.riskLevel = riskLevel;
        this.possibleCondition = possibleCondition;
        this.recommendedAction = recommendedAction;
        this.assessmentDate = assessmentDate;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public int getSymptomId() {
        return symptomId;
    }

    public void setSymptomId(int symptomId) {
        this.symptomId = symptomId;
    }

    public int getAnimalId() {
        return animalId;
    }

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getPossibleCondition() {
        return possibleCondition;
    }

    public void setPossibleCondition(String possibleCondition) {
        this.possibleCondition = possibleCondition;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public Timestamp getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(Timestamp assessmentDate) {
        this.assessmentDate = assessmentDate;
    }
}

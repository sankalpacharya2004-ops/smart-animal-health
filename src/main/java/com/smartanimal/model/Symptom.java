package com.smartanimal.model;

import java.sql.Timestamp;

public class Symptom {
    private int symptomId;
    private int animalId;
    private boolean reducedAppetite;
    private boolean fever;
    private boolean vomiting;
    private boolean lowActivity;
    private boolean limping;
    private String otherSymptoms;
    private Timestamp recordedDate;

    public Symptom() {}

    public Symptom(int symptomId, int animalId, boolean reducedAppetite, boolean fever, boolean vomiting, boolean lowActivity, boolean limping, String otherSymptoms, Timestamp recordedDate) {
        this.symptomId = symptomId;
        this.animalId = animalId;
        this.reducedAppetite = reducedAppetite;
        this.fever = fever;
        this.vomiting = vomiting;
        this.lowActivity = lowActivity;
        this.limping = limping;
        this.otherSymptoms = otherSymptoms;
        this.recordedDate = recordedDate;
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

    public boolean isReducedAppetite() {
        return reducedAppetite;
    }

    public void setReducedAppetite(boolean reducedAppetite) {
        this.reducedAppetite = reducedAppetite;
    }

    public boolean isFever() {
        return fever;
    }

    public void setFever(boolean fever) {
        this.fever = fever;
    }

    public boolean isVomiting() {
        return vomiting;
    }

    public void setVomiting(boolean vomiting) {
        this.vomiting = vomiting;
    }

    public boolean isLowActivity() {
        return lowActivity;
    }

    public void setLowActivity(boolean lowActivity) {
        this.lowActivity = lowActivity;
    }

    public boolean isLimping() {
        return limping;
    }

    public void setLimping(boolean limping) {
        this.limping = limping;
    }

    public String getOtherSymptoms() {
        return otherSymptoms;
    }

    public void setOtherSymptoms(String otherSymptoms) {
        this.otherSymptoms = otherSymptoms;
    }

    public Timestamp getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Timestamp recordedDate) {
        this.recordedDate = recordedDate;
    }
}

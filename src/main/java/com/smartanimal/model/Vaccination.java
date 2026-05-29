package com.smartanimal.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Vaccination {
    private int vaccinationId;
    private int animalId;
    private String vaccineName;
    private Date scheduledDate;
    private Date administeredDate;
    private String status; // 'Pending', 'Completed', 'Overdue'
    private String notes;
    private Timestamp createdDate;

    public Vaccination() {}

    public Vaccination(int vaccinationId, int animalId, String vaccineName, Date scheduledDate, Date administeredDate, String status, String notes, Timestamp createdDate) {
        this.vaccinationId = vaccinationId;
        this.animalId = animalId;
        this.vaccineName = vaccineName;
        this.scheduledDate = scheduledDate;
        this.administeredDate = administeredDate;
        this.status = status;
        this.notes = notes;
        this.createdDate = createdDate;
    }

    public int getVaccinationId() {
        return vaccinationId;
    }

    public void setVaccinationId(int vaccinationId) {
        this.vaccinationId = vaccinationId;
    }

    public int getAnimalId() {
        return animalId;
    }

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Date getAdministeredDate() {
        return administeredDate;
    }

    public void setAdministeredDate(Date administeredDate) {
        this.administeredDate = administeredDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
}

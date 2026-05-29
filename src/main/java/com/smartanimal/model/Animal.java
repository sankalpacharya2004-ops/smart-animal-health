package com.smartanimal.model;

import java.sql.Timestamp;

public class Animal {
    private int animalId;
    private Integer userId;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private Double weight;
    private String animalType; // 'pet', 'farm', 'stray'
    private String ownerName;
    private String contactNumber;
    private Timestamp registrationDate;

    public Animal() {}

    public Animal(int animalId, Integer userId, String name, String species, String breed, Integer age, Double weight, String animalType, String ownerName, String contactNumber, Timestamp registrationDate) {
        this.animalId = animalId;
        this.userId = userId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.weight = weight;
        this.animalType = animalType;
        this.ownerName = ownerName;
        this.contactNumber = contactNumber;
        this.registrationDate = registrationDate;
    }

    public int getAnimalId() {
        return animalId;
    }

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }
}

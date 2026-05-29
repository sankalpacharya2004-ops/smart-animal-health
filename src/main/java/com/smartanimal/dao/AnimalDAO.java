package com.smartanimal.dao;

import com.smartanimal.model.Animal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnimalDAO {

    private Animal extractAnimal(ResultSet rs) throws SQLException {
        Animal animal = new Animal();
        animal.setAnimalId(rs.getInt("animal_id"));
        int userId = rs.getInt("user_id");
        animal.setUserId(rs.wasNull() ? null : userId);
        animal.setName(rs.getString("name"));
        animal.setSpecies(rs.getString("species"));
        animal.setBreed(rs.getString("breed"));
        int age = rs.getInt("age");
        animal.setAge(rs.wasNull() ? null : age);
        double weight = rs.getDouble("weight");
        animal.setWeight(rs.wasNull() ? null : weight);
        animal.setAnimalType(rs.getString("animal_type"));
        animal.setOwnerName(rs.getString("owner_name"));
        animal.setContactNumber(rs.getString("contact_number"));
        animal.setRegistrationDate(rs.getTimestamp("registration_date"));
        return animal;
    }

    public List<Animal> getAllAnimals() {
        List<Animal> animals = new ArrayList<>();
        String sql = "SELECT * FROM animals ORDER BY registration_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                animals.add(extractAnimal(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return animals;
    }

    public List<Animal> getAnimalsByUserId(int userId) {
        List<Animal> animals = new ArrayList<>();
        String sql = "SELECT * FROM animals WHERE user_id = ? ORDER BY registration_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    animals.add(extractAnimal(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return animals;
    }

    public Animal getAnimalById(int animalId) {
        String sql = "SELECT * FROM animals WHERE animal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, animalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAnimal(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addAnimal(Animal animal) {
        String sql = "INSERT INTO animals (user_id, name, species, breed, age, weight, animal_type, owner_name, contact_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (animal.getUserId() != null) {
                stmt.setInt(1, animal.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, animal.getName());
            stmt.setString(3, animal.getSpecies());
            stmt.setString(4, animal.getBreed());
            if (animal.getAge() != null) {
                stmt.setInt(5, animal.getAge());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            if (animal.getWeight() != null) {
                stmt.setDouble(6, animal.getWeight());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }
            stmt.setString(7, animal.getAnimalType());
            stmt.setString(8, animal.getOwnerName());
            stmt.setString(9, animal.getContactNumber());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        animal.setAnimalId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAnimal(Animal animal) {
        String sql = "UPDATE animals SET name = ?, species = ?, breed = ?, age = ?, weight = ?, animal_type = ?, owner_name = ?, contact_number = ? WHERE animal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, animal.getName());
            stmt.setString(2, animal.getSpecies());
            stmt.setString(3, animal.getBreed());
            if (animal.getAge() != null) {
                stmt.setInt(4, animal.getAge());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            if (animal.getWeight() != null) {
                stmt.setDouble(5, animal.getWeight());
            } else {
                stmt.setNull(5, Types.DECIMAL);
            }
            stmt.setString(6, animal.getAnimalType());
            stmt.setString(7, animal.getOwnerName());
            stmt.setString(8, animal.getContactNumber());
            stmt.setInt(9, animal.getAnimalId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAnimal(int animalId) {
        String sql = "DELETE FROM animals WHERE animal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, animalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

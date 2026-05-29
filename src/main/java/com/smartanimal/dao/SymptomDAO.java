package com.smartanimal.dao;

import com.smartanimal.model.Symptom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SymptomDAO {

    private Symptom extractSymptom(ResultSet rs) throws SQLException {
        Symptom symptom = new Symptom();
        symptom.setSymptomId(rs.getInt("symptom_id"));
        symptom.setAnimalId(rs.getInt("animal_id"));
        symptom.setReducedAppetite(rs.getBoolean("reduced_appetite"));
        symptom.setFever(rs.getBoolean("fever"));
        symptom.setVomiting(rs.getBoolean("vomiting"));
        symptom.setLowActivity(rs.getBoolean("low_activity"));
        symptom.setLimping(rs.getBoolean("limping"));
        symptom.setOtherSymptoms(rs.getString("other_symptoms"));
        symptom.setRecordedDate(rs.getTimestamp("recorded_date"));
        return symptom;
    }

    public boolean addSymptom(Symptom symptom) {
        String sql = "INSERT INTO symptoms (animal_id, reduced_appetite, fever, vomiting, low_activity, limping, other_symptoms) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, symptom.getAnimalId());
            stmt.setBoolean(2, symptom.isReducedAppetite());
            stmt.setBoolean(3, symptom.isFever());
            stmt.setBoolean(4, symptom.isVomiting());
            stmt.setBoolean(5, symptom.isLowActivity());
            stmt.setBoolean(6, symptom.isLimping());
            stmt.setString(7, symptom.getOtherSymptoms());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        symptom.setSymptomId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Symptom> getSymptomsByAnimalId(int animalId) {
        List<Symptom> list = new ArrayList<>();
        String sql = "SELECT * FROM symptoms WHERE animal_id = ? ORDER BY recorded_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, animalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractSymptom(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Symptom getSymptomById(int symptomId) {
        String sql = "SELECT * FROM symptoms WHERE symptom_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, symptomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractSymptom(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.smartanimal.dao;

import com.smartanimal.model.HealthAssessment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HealthAssessmentDAO {

    private HealthAssessment extractAssessment(ResultSet rs) throws SQLException {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setAssessmentId(rs.getInt("assessment_id"));
        assessment.setSymptomId(rs.getInt("symptom_id"));
        assessment.setAnimalId(rs.getInt("animal_id"));
        assessment.setRiskLevel(rs.getString("risk_level"));
        assessment.setPossibleCondition(rs.getString("possible_condition"));
        assessment.setRecommendedAction(rs.getString("recommended_action"));
        assessment.setAssessmentDate(rs.getTimestamp("assessment_date"));
        return assessment;
    }

    public boolean addAssessment(HealthAssessment assessment) {
        String sql = "INSERT INTO health_assessments (symptom_id, animal_id, risk_level, possible_condition, recommended_action) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, assessment.getSymptomId());
            stmt.setInt(2, assessment.getAnimalId());
            stmt.setString(3, assessment.getRiskLevel());
            stmt.setString(4, assessment.getPossibleCondition());
            stmt.setString(5, assessment.getRecommendedAction());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        assessment.setAssessmentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<HealthAssessment> getAssessmentsByAnimalId(int animalId) {
        List<HealthAssessment> list = new ArrayList<>();
        String sql = "SELECT * FROM health_assessments WHERE animal_id = ? ORDER BY assessment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, animalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAssessment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public HealthAssessment getAssessmentById(int assessmentId) {
        String sql = "SELECT * FROM health_assessments WHERE assessment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assessmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAssessment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HealthAssessment getAssessmentBySymptomId(int symptomId) {
        String sql = "SELECT * FROM health_assessments WHERE symptom_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, symptomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAssessment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

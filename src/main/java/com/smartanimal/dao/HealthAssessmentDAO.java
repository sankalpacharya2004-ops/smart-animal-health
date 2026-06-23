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
        
        assessment.setDoctorDiagnosis(rs.getString("doctor_diagnosis"));
        assessment.setTreatmentNotes(rs.getString("treatment_notes"));
        assessment.setPrescription(rs.getString("prescription"));
        
        int docId = rs.getInt("doctor_id");
        if (rs.wasNull()) {
            assessment.setDoctorId(null);
        } else {
            assessment.setDoctorId(docId);
        }

        try {
            assessment.setAnimalName(rs.getString("animal_name"));
        } catch (SQLException e) {
            // Ignore if column not in result set
        }

        try {
            assessment.setOwnerName(rs.getString("owner_name"));
        } catch (SQLException e) {
            // Ignore if column not in result set
        }

        try {
            assessment.setDoctorName(rs.getString("doctor_name"));
        } catch (SQLException e) {
            // Ignore if column not in result set
        }

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

    public boolean updateDoctorOverride(int assessmentId, String diagnosis, String notes, String prescription, int doctorId) {
        String sql = "UPDATE health_assessments SET doctor_diagnosis = ?, treatment_notes = ?, prescription = ?, doctor_id = ? WHERE assessment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, diagnosis);
            stmt.setString(2, notes);
            stmt.setString(3, prescription);
            stmt.setInt(4, doctorId);
            stmt.setInt(5, assessmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<HealthAssessment> getAssessmentsByAnimalId(int animalId) {
        List<HealthAssessment> list = new ArrayList<>();
        String sql = "SELECT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "WHERE h.animal_id = ? " +
                     "ORDER BY h.assessment_date DESC";
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
        String sql = "SELECT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "WHERE h.assessment_id = ?";
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
        String sql = "SELECT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "WHERE h.symptom_id = ?";
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

    public List<HealthAssessment> getAllAssessments() {
        List<HealthAssessment> list = new ArrayList<>();
        String sql = "SELECT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "ORDER BY h.assessment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(extractAssessment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HealthAssessment> getPendingConsultations() {
        List<HealthAssessment> list = new ArrayList<>();
        String sql = "SELECT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "WHERE h.doctor_diagnosis IS NULL " +
                     "ORDER BY h.assessment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(extractAssessment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HealthAssessment> getActiveHighRiskCases() {
        List<HealthAssessment> list = new ArrayList<>();
        String sql = "SELECT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "WHERE h.risk_level = 'High' AND h.doctor_diagnosis IS NULL " +
                     "ORDER BY h.assessment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(extractAssessment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HealthAssessment> getPendingConsultationsByDoctorId(int doctorId) {
        List<HealthAssessment> list = new ArrayList<>();
        String sql = "SELECT DISTINCT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "LEFT JOIN appointments ap ON a.animal_id = ap.animal_id " +
                     "LEFT JOIN health_assessments h2 ON a.animal_id = h2.animal_id " +
                     "WHERE h.doctor_diagnosis IS NULL AND (ap.doctor_id = ? OR h2.doctor_id = ?) " +
                     "ORDER BY h.assessment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, doctorId);
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

    public List<HealthAssessment> getActiveHighRiskCasesByDoctorId(int doctorId) {
        List<HealthAssessment> list = new ArrayList<>();
        String sql = "SELECT DISTINCT h.*, a.name AS animal_name, a.owner_name, u.full_name AS doctor_name " +
                     "FROM health_assessments h " +
                     "JOIN animals a ON h.animal_id = a.animal_id " +
                     "LEFT JOIN users u ON h.doctor_id = u.user_id " +
                     "LEFT JOIN appointments ap ON a.animal_id = ap.animal_id " +
                     "LEFT JOIN health_assessments h2 ON a.animal_id = h2.animal_id " +
                     "WHERE h.risk_level = 'High' AND h.doctor_diagnosis IS NULL AND (ap.doctor_id = ? OR h2.doctor_id = ?) " +
                     "ORDER BY h.assessment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, doctorId);
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
}

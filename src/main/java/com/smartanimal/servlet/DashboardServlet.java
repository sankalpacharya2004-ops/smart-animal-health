package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smartanimal.dao.DBConnection;
import com.smartanimal.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardServlet extends HttpServlet {
    private final Gson gson = new Gson();

    private User getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Unauthorized. Please log in first.");
            response.getWriter().write(gson.toJson(error));
            return null;
        }
        return (User) session.getAttribute("user");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        JsonObject dashboardData = new JsonObject();
        boolean hasGlobalAccess = "Admin".equalsIgnoreCase(user.getRole()) || "Doctor".equalsIgnoreCase(user.getRole());
        int userId = user.getUserId();

        try (Connection conn = DBConnection.getConnection()) {
            
            // 1. Total Animals
            String sqlAnimals = hasGlobalAccess ? "SELECT COUNT(*) FROM animals" : "SELECT COUNT(*) FROM animals WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAnimals)) {
                if (!hasGlobalAccess) stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        dashboardData.addProperty("totalAnimals", rs.getInt(1));
                    }
                }
            }

            // 2. Pending/Overdue Vaccinations
            String sqlVaccines = hasGlobalAccess 
                ? "SELECT COUNT(*) FROM vaccinations v JOIN animals a ON v.animal_id = a.animal_id WHERE v.status IN ('Pending', 'Overdue')"
                : "SELECT COUNT(*) FROM vaccinations v JOIN animals a ON v.animal_id = a.animal_id WHERE v.status IN ('Pending', 'Overdue') AND a.user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlVaccines)) {
                if (!hasGlobalAccess) stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        dashboardData.addProperty("pendingVaccinations", rs.getInt(1));
                    }
                }
            }

            // 3. High Risk Assessments
            String sqlAlerts = hasGlobalAccess 
                ? "SELECT COUNT(*) FROM health_assessments h JOIN animals a ON h.animal_id = a.animal_id WHERE h.risk_level = 'High'"
                : "SELECT COUNT(*) FROM health_assessments h JOIN animals a ON h.animal_id = a.animal_id WHERE h.risk_level = 'High' AND a.user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAlerts)) {
                if (!hasGlobalAccess) stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        dashboardData.addProperty("highRiskAlerts", rs.getInt(1));
                    }
                }
            }

            // 4. Doctor Specific Counts
            if (hasGlobalAccess) {
                // Active High Risk Cases (no doctor override yet)
                String sqlActiveHighRisk = "SELECT COUNT(*) FROM health_assessments WHERE risk_level = 'High' AND doctor_diagnosis IS NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sqlActiveHighRisk);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        dashboardData.addProperty("activeHighRiskCases", rs.getInt(1));
                    }
                }

                // Pending Consultations (no doctor override yet)
                String sqlPendingConsultations = "SELECT COUNT(*) FROM health_assessments WHERE doctor_diagnosis IS NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sqlPendingConsultations);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        dashboardData.addProperty("pendingConsultations", rs.getInt(1));
                    }
                }
            }

            // 5. Recent Health Assessments (Limit 5)
            JsonArray assessments = new JsonArray();
            String sqlRecentAssessments = hasGlobalAccess
                ? "SELECT h.*, a.name AS animal_name FROM health_assessments h JOIN animals a ON h.animal_id = a.animal_id ORDER BY h.assessment_date DESC LIMIT 5"
                : "SELECT h.*, a.name AS animal_name FROM health_assessments h JOIN animals a ON h.animal_id = a.animal_id WHERE a.user_id = ? ORDER BY h.assessment_date DESC LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRecentAssessments)) {
                if (!hasGlobalAccess) stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("assessmentId", rs.getInt("assessment_id"));
                        obj.addProperty("animalId", rs.getInt("animal_id"));
                        obj.addProperty("animalName", rs.getString("animal_name"));
                        obj.addProperty("riskLevel", rs.getString("risk_level"));
                        obj.addProperty("possibleCondition", rs.getString("possible_condition"));
                        obj.addProperty("recommendedAction", rs.getString("recommended_action"));
                        obj.addProperty("assessmentDate", rs.getTimestamp("assessment_date").toString());
                        
                        String docDiag = rs.getString("doctor_diagnosis");
                        obj.addProperty("doctorDiagnosis", docDiag);
                        obj.addProperty("treatmentNotes", rs.getString("treatment_notes"));
                        obj.addProperty("prescription", rs.getString("prescription"));
                        
                        int docId = rs.getInt("doctor_id");
                        if (rs.wasNull()) {
                            obj.add("doctorId", null);
                        } else {
                            obj.addProperty("doctorId", docId);
                        }
                        
                        assessments.add(obj);
                    }
                }
            }
            dashboardData.add("recentAssessments", assessments);

            // 6. Upcoming Vaccinations (Limit 5)
            JsonArray vaccines = new JsonArray();
            String sqlUpcomingVaccines = hasGlobalAccess
                ? "SELECT v.*, a.name AS animal_name FROM vaccinations v JOIN animals a ON v.animal_id = a.animal_id WHERE v.status IN ('Pending', 'Overdue') ORDER BY v.scheduled_date ASC LIMIT 5"
                : "SELECT v.*, a.name AS animal_name FROM vaccinations v JOIN animals a ON v.animal_id = a.animal_id WHERE v.status IN ('Pending', 'Overdue') AND a.user_id = ? ORDER BY v.scheduled_date ASC LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpcomingVaccines)) {
                if (!hasGlobalAccess) stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("vaccinationId", rs.getInt("vaccination_id"));
                        obj.addProperty("animalId", rs.getInt("animal_id"));
                        obj.addProperty("animalName", rs.getString("animal_name"));
                        obj.addProperty("vaccineName", rs.getString("vaccine_name"));
                        obj.addProperty("scheduledDate", rs.getDate("scheduled_date").toString());
                        obj.addProperty("status", rs.getString("status"));
                        vaccines.add(obj);
                    }
                }
            }
            dashboardData.add("upcomingVaccinations", vaccines);

            response.getWriter().write(gson.toJson(dashboardData));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Database error generating dashboard statistics: " + e.getMessage());
            response.getWriter().write(gson.toJson(error));
            e.printStackTrace();
        }
    }
}

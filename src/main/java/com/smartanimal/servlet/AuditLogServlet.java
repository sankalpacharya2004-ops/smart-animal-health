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

public class AuditLogServlet extends HttpServlet {
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

    private boolean verifyAdmin(User user, HttpServletResponse response) throws IOException {
        if (!"Admin".equalsIgnoreCase(user.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Forbidden. Administrator privileges required.");
            response.getWriter().write(gson.toJson(error));
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;
        if (!verifyAdmin(user, response)) return;

        JsonArray logArray = new JsonArray();

        String sql = "SELECT timestamp, activity_type, details FROM (" +
                     "  SELECT created_date AS timestamp, 'User Registered' AS activity_type, CONCAT('New caretaker account created: ', username, ' (', COALESCE(full_name, 'No Name'), ')') AS details FROM users " +
                     "  UNION ALL " +
                     "  SELECT registration_date AS timestamp, 'Animal Registered' AS activity_type, CONCAT('New animal profile created: ', name, ' (', species, ', ', COALESCE(breed, 'unknown'), ') owned by ', COALESCE(owner_name, 'unknown')) AS details FROM animals " +
                     "  UNION ALL " +
                     "  SELECT h.assessment_date AS timestamp, 'Diagnosis Run' AS activity_type, CONCAT('Symptom diagnosis run for ', a.name, ' (', a.species, '). Risk: ', h.risk_level, '. Possible condition: ', COALESCE(h.possible_condition, 'None'), '.') AS details FROM health_assessments h JOIN animals a ON h.animal_id = a.animal_id " +
                     "  UNION ALL " +
                     "  SELECT v.created_date AS timestamp, 'Vaccination Scheduled' AS activity_type, CONCAT('Vaccine scheduled: ', v.vaccine_name, ' for ', a.name, ' on ', v.scheduled_date, '.') AS details FROM vaccinations v JOIN animals a ON v.animal_id = a.animal_id " +
                     ") combined_logs " +
                     "ORDER BY timestamp DESC " +
                     "LIMIT 50";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                JsonObject logObj = new JsonObject();
                logObj.addProperty("timestamp", rs.getTimestamp("timestamp").toString());
                logObj.addProperty("activityType", rs.getString("activity_type"));
                logObj.addProperty("details", rs.getString("details"));
                logArray.add(logObj);
            }
            
            response.getWriter().write(gson.toJson(logArray));
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Database error fetching activity logs: " + e.getMessage());
            response.getWriter().write(gson.toJson(error));
            e.printStackTrace();
        }
    }
}

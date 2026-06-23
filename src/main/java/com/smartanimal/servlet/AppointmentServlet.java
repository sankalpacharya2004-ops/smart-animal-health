package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartanimal.dao.AppointmentDAO;
import com.smartanimal.dao.AnimalDAO;
import com.smartanimal.dao.UserDAO;
import com.smartanimal.model.Appointment;
import com.smartanimal.model.Animal;
import com.smartanimal.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class AppointmentServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();
    private final UserDAO userDAO = new UserDAO();
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

        String action = request.getParameter("action");
        if ("getDoctors".equalsIgnoreCase(action)) {
            List<User> approvedDoctors = userDAO.getApprovedDoctors();
            response.getWriter().write(gson.toJson(approvedDoctors));
            return;
        }

        // Default get: list appointments based on user role
        List<Appointment> list;
        if ("Admin".equalsIgnoreCase(user.getRole())) {
            list = appointmentDAO.getAllAppointments();
        } else if ("Doctor".equalsIgnoreCase(user.getRole())) {
            list = appointmentDAO.getAppointmentsByDoctorId(user.getUserId());
        } else {
            list = appointmentDAO.getAppointmentsByUserId(user.getUserId());
        }

        response.getWriter().write(gson.toJson(list));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        BufferedReader reader = request.getReader();
        JsonObject jsonBody = gson.fromJson(reader, JsonObject.class);
        JsonObject jsonResponse = new JsonObject();

        if (jsonBody == null || !jsonBody.has("animalId") || !jsonBody.has("doctorId") || !jsonBody.has("appointmentDate")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing required fields: animalId, doctorId, appointmentDate.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        int animalId = jsonBody.get("animalId").getAsInt();
        int doctorId = jsonBody.get("doctorId").getAsInt();
        String dateStr = jsonBody.get("appointmentDate").getAsString();
        String reason = jsonBody.has("reason") ? jsonBody.get("reason").getAsString() : "";

        // Verify animal exists
        Animal animal = animalDAO.getAnimalById(animalId);
        if (animal == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Animal not found.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        // Verify permission: User must own the animal to schedule an appointment
        if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole())) {
            if (animal.getUserId() == null || animal.getUserId() != user.getUserId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Forbidden. You do not own this animal profile.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
        }

        // Verify doctor exists and is approved doctor
        User doctor = userDAO.getUserById(doctorId);
        if (doctor == null || !"Doctor".equalsIgnoreCase(doctor.getRole()) || !doctor.isApproved()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid doctor selection.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        Timestamp appTimestamp = parseTimestamp(dateStr);
        if (appTimestamp == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid appointment date format.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        Appointment app = new Appointment();
        app.setAnimalId(animalId);
        app.setDoctorId(doctorId);
        app.setAppointmentDate(appTimestamp);
        app.setStatus("Pending");
        app.setReason(reason);

        if (appointmentDAO.addAppointment(app)) {
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Appointment scheduled successfully.");
            jsonResponse.add("appointment", gson.toJsonTree(app));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error scheduling appointment.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        BufferedReader reader = request.getReader();
        JsonObject jsonBody = gson.fromJson(reader, JsonObject.class);
        JsonObject jsonResponse = new JsonObject();

        if (jsonBody == null || !jsonBody.has("appointmentId") || !jsonBody.has("status")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing required fields: appointmentId, status.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        int appointmentId = jsonBody.get("appointmentId").getAsInt();
        String status = jsonBody.get("status").getAsString();

        if (!"Pending".equalsIgnoreCase(status) && !"Scheduled".equalsIgnoreCase(status) && !"Completed".equalsIgnoreCase(status) && !"Cancelled".equalsIgnoreCase(status)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid status value.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        Appointment existing = appointmentDAO.getAppointmentById(appointmentId);
        if (existing == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Appointment not found.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        // Verify permission:
        // Admin: can do anything
        // Doctor: can only edit if doctorId matches their user_id
        // User: can only edit if animal's owner user_id matches their user_id
        boolean allowed = false;
        if ("Admin".equalsIgnoreCase(user.getRole())) {
            allowed = true;
        } else if ("Doctor".equalsIgnoreCase(user.getRole())) {
            if (existing.getDoctorId() == user.getUserId()) {
                allowed = true;
            }
        } else { // User role
            Animal animal = animalDAO.getAnimalById(existing.getAnimalId());
            if (animal != null && animal.getUserId() != null && animal.getUserId() == user.getUserId()) {
                // Users can only mark status as 'Cancelled'
                if ("Cancelled".equalsIgnoreCase(status)) {
                    allowed = true;
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Forbidden. Users can only cancel their appointments.");
                    response.getWriter().write(gson.toJson(jsonResponse));
                    return;
                }
            }
        }

        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Forbidden. You are not authorized to update this appointment.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        if (appointmentDAO.updateAppointmentStatus(appointmentId, status)) {
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Appointment status updated to " + status + ".");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error updating appointment status.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private Timestamp parseTimestamp(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return new Timestamp(Long.parseLong(dateStr));
        } catch (NumberFormatException e) {
            try {
                String formatted = dateStr.replace("T", " ");
                if (formatted.length() == 16) {
                    formatted += ":00";
                }
                return Timestamp.valueOf(formatted);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}

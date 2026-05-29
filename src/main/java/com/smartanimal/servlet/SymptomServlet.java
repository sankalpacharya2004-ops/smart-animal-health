package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartanimal.dao.AnimalDAO;
import com.smartanimal.dao.HealthAssessmentDAO;
import com.smartanimal.dao.SymptomDAO;
import com.smartanimal.model.Animal;
import com.smartanimal.model.HealthAssessment;
import com.smartanimal.model.Symptom;
import com.smartanimal.model.User;
import com.smartanimal.util.InferenceEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class SymptomServlet extends HttpServlet {
    private final SymptomDAO symptomDAO = new SymptomDAO();
    private final HealthAssessmentDAO assessmentDAO = new HealthAssessmentDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();
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

    // Retrieve symptom history for a specific animal
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String animalIdParam = request.getParameter("animalId");
        if (animalIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Missing animalId parameter.");
            response.getWriter().write(gson.toJson(error));
            return;
        }

        try {
            int animalId = Integer.parseInt(animalIdParam);
            Animal animal = animalDAO.getAnimalById(animalId);
            if (animal == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("message", "Animal not found.");
                response.getWriter().write(gson.toJson(error));
                return;
            }

            // Secure ownership check
            if (!"Admin".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("message", "Forbidden. You do not own this animal's records.");
                response.getWriter().write(gson.toJson(error));
                return;
            }

            List<Symptom> history = symptomDAO.getSymptomsByAnimalId(animalId);
            response.getWriter().write(gson.toJson(history));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // Log symptoms, auto-run inference engine, save assessment, and return result
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        BufferedReader reader = request.getReader();
        Symptom symptom = gson.fromJson(reader, Symptom.class);
        JsonObject jsonResponse = new JsonObject();

        if (symptom == null || symptom.getAnimalId() <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing or invalid animalId.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        // Validate animal ownership
        Animal animal = animalDAO.getAnimalById(symptom.getAnimalId());
        if (animal == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Animal not found.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        if (!"Admin".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        // Save Symptom Log
        if (symptomDAO.addSymptom(symptom)) {
            // Run inference engine
            HealthAssessment assessment = InferenceEngine.generateAssessment(symptom);
            
            // Save health assessment result
            if (assessmentDAO.addAssessment(assessment)) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Symptoms logged and health assessment generated.");
                jsonResponse.add("assessment", gson.toJsonTree(assessment));
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Symptoms saved, but failed to store health assessment.");
            }
        } else {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error logging symptoms.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}

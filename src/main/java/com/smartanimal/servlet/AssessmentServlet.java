package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartanimal.dao.AnimalDAO;
import com.smartanimal.dao.HealthAssessmentDAO;
import com.smartanimal.model.Animal;
import com.smartanimal.model.HealthAssessment;
import com.smartanimal.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class AssessmentServlet extends HttpServlet {
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

    // Get health assessments (by animalId or symptomId)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String animalIdParam = request.getParameter("animalId");
        String symptomIdParam = request.getParameter("symptomId");
        JsonObject jsonResponse = new JsonObject();

        if (animalIdParam != null) {
            try {
                int animalId = Integer.parseInt(animalIdParam);
                Animal animal = animalDAO.getAnimalById(animalId);
                if (animal == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Animal not found.");
                    response.getWriter().write(gson.toJson(jsonResponse));
                    return;
                }

                // Check ownership
                if (!"Admin".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
                    response.getWriter().write(gson.toJson(jsonResponse));
                    return;
                }

                List<HealthAssessment> list = assessmentDAO.getAssessmentsByAnimalId(animalId);
                response.getWriter().write(gson.toJson(list));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } 
        else if (symptomIdParam != null) {
            try {
                int symptomId = Integer.parseInt(symptomIdParam);
                HealthAssessment assessment = assessmentDAO.getAssessmentBySymptomId(symptomId);
                if (assessment != null) {
                    Animal animal = animalDAO.getAnimalById(assessment.getAnimalId());
                    if (!"Admin".equalsIgnoreCase(user.getRole()) && (animal == null || animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
                        response.getWriter().write(gson.toJson(jsonResponse));
                        return;
                    }
                    response.getWriter().write(gson.toJson(assessment));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Health assessment not found for this symptom ID.");
                    response.getWriter().write(gson.toJson(jsonResponse));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } 
        else {
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                List<HealthAssessment> list = assessmentDAO.getAllAssessments();
                response.getWriter().write(gson.toJson(list));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Missing query parameter: animalId or symptomId.");
                response.getWriter().write(gson.toJson(jsonResponse));
            }
        }
    }
}

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

    // Get health assessments (by animalId, symptomId, pending=true, highRisk=true, or all for admin/doctor)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String animalIdParam = request.getParameter("animalId");
        String symptomIdParam = request.getParameter("symptomId");
        String pendingParam = request.getParameter("pending");
        String highRiskParam = request.getParameter("highRisk");
        JsonObject jsonResponse = new JsonObject();

        if (pendingParam != null && Boolean.parseBoolean(pendingParam)) {
            if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Forbidden. Only Doctors or Admins can view pending consultations.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
            List<HealthAssessment> list;
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                list = assessmentDAO.getPendingConsultations();
            } else {
                list = assessmentDAO.getPendingConsultationsByDoctorId(user.getUserId());
            }
            response.getWriter().write(gson.toJson(list));
        }
        else if (highRiskParam != null && Boolean.parseBoolean(highRiskParam)) {
            if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Forbidden. Only Doctors or Admins can view active high-risk cases.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
            List<HealthAssessment> list;
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                list = assessmentDAO.getActiveHighRiskCases();
            } else {
                list = assessmentDAO.getActiveHighRiskCasesByDoctorId(user.getUserId());
            }
            response.getWriter().write(gson.toJson(list));
        }
        else if (animalIdParam != null) {
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

                // Check ownership (Admin and Doctor bypass this)
                if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
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
                    if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal == null || animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
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
            if ("Admin".equalsIgnoreCase(user.getRole()) || "Doctor".equalsIgnoreCase(user.getRole())) {
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

    // Save clinical override
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        JsonObject jsonResponse = new JsonObject();

        if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Forbidden. Only Doctors or Admins can perform clinical overrides.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        try {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
            if (body == null || !body.has("assessmentId") || !body.has("doctorDiagnosis")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Invalid request. Missing assessmentId or doctorDiagnosis.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            int assessmentId = body.get("assessmentId").getAsInt();
            String doctorDiagnosis = body.get("doctorDiagnosis").getAsString();
            String treatmentNotes = body.has("treatmentNotes") ? body.get("treatmentNotes").getAsString() : "";
            String prescription = body.has("prescription") ? body.get("prescription").getAsString() : "";
            int doctorId = user.getUserId();

            boolean success = assessmentDAO.updateDoctorOverride(assessmentId, doctorDiagnosis, treatmentNotes, prescription, doctorId);
            if (success) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Clinical override saved successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Failed to save clinical override. Assessment might not exist.");
            }
            response.getWriter().write(gson.toJson(jsonResponse));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Malformed JSON request body.");
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }
}

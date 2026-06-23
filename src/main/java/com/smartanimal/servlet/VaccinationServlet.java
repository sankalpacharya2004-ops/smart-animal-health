package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonObject;
import com.smartanimal.dao.AnimalDAO;
import com.smartanimal.dao.VaccinationDAO;
import com.smartanimal.model.Animal;
import com.smartanimal.model.User;
import com.smartanimal.model.Vaccination;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

public class VaccinationServlet extends HttpServlet {
    private final VaccinationDAO vaccinationDAO = new VaccinationDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
            String str = json.getAsString();
            if (str == null || str.trim().isEmpty()) {
                return null;
            }
            try {
                return Date.valueOf(str);
            } catch (IllegalArgumentException e) {
                try {
                    return new Date(json.getAsLong());
                } catch (NumberFormatException nfe) {
                    throw new com.google.gson.JsonParseException("Failed parsing Date: " + str, e);
                }
            }
        })
        .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> 
            src == null ? com.google.gson.JsonNull.INSTANCE : new JsonPrimitive(src.toString()))
        .create();

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

    // Retrieve vaccinations for an animal
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String animalIdParam = request.getParameter("animalId");
        String idParam = request.getParameter("id");
        String doctorIdParam = request.getParameter("doctorId");
        JsonObject jsonResponse = new JsonObject();

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Vaccination v = vaccinationDAO.getVaccinationById(id);
                if (v != null) {
                    Animal animal = animalDAO.getAnimalById(v.getAnimalId());
                    if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal == null || animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Forbidden. You do not own this record.");
                        response.getWriter().write(gson.toJson(jsonResponse));
                        return;
                    }
                    response.getWriter().write(gson.toJson(v));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Record not found.");
                    response.getWriter().write(gson.toJson(jsonResponse));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } 
        else if (doctorIdParam != null) {
            try {
                int doctorId = Integer.parseInt(doctorIdParam);
                List<Vaccination> list;
                boolean isAllAnimals = "all".equalsIgnoreCase(animalIdParam) || animalIdParam == null;

                if (!isAllAnimals) {
                    int animalId = Integer.parseInt(animalIdParam);
                    Animal animal = animalDAO.getAnimalById(animalId);
                    if (animal == null) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Animal not found.");
                        response.getWriter().write(gson.toJson(jsonResponse));
                        return;
                    }

                    if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
                        response.getWriter().write(gson.toJson(jsonResponse));
                        return;
                    }

                    List<Vaccination> allAnimalVac = vaccinationDAO.getVaccinationsByAnimalId(animalId);
                    List<Vaccination> filtered = new java.util.ArrayList<>();
                    for (Vaccination v : allAnimalVac) {
                        if (v.getDoctorId() != null && v.getDoctorId() == doctorId) {
                            filtered.add(v);
                        }
                    }
                    list = filtered;
                } else {
                    if ("Admin".equalsIgnoreCase(user.getRole()) || "Doctor".equalsIgnoreCase(user.getRole())) {
                        list = vaccinationDAO.getVaccinationsByDoctorId(doctorId);
                    } else {
                        list = vaccinationDAO.getVaccinationsByDoctorIdAndUserId(doctorId, user.getUserId());
                    }
                }
                response.getWriter().write(gson.toJson(list));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
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

                if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
                    response.getWriter().write(gson.toJson(jsonResponse));
                    return;
                }

                List<Vaccination> list = vaccinationDAO.getVaccinationsByAnimalId(animalId);
                response.getWriter().write(gson.toJson(list));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } 
        else {
            if ("Admin".equalsIgnoreCase(user.getRole()) || "Doctor".equalsIgnoreCase(user.getRole())) {
                List<Vaccination> list;
                if ("Admin".equalsIgnoreCase(user.getRole())) {
                    list = vaccinationDAO.getAllVaccinationsWithDetails();
                } else {
                    list = vaccinationDAO.getVaccinationsForDoctorAnimals(user.getUserId());
                }
                response.getWriter().write(gson.toJson(list));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Missing parameter: id or animalId or doctorId.");
                response.getWriter().write(gson.toJson(jsonResponse));
            }
        }
    }

    // Schedule a new vaccination
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        BufferedReader reader = request.getReader();
        Vaccination vaccination = gson.fromJson(reader, Vaccination.class);
        JsonObject jsonResponse = new JsonObject();

        if (vaccination == null || vaccination.getAnimalId() <= 0 || vaccination.getVaccineName() == null ||
            vaccination.getVaccineName().trim().isEmpty() || vaccination.getScheduledDate() == null) {
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing required fields: animalId, vaccineName, scheduledDate.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        Animal animal = animalDAO.getAnimalById(vaccination.getAnimalId());
        if (animal == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Animal not found.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        if (vaccinationDAO.addVaccination(vaccination)) {
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Vaccination scheduled successfully.");
            jsonResponse.add("vaccination", gson.toJsonTree(vaccination));
        } else {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error scheduling vaccination.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    // Mark vaccination as completed or update details
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        BufferedReader reader = request.getReader();
        JsonObject body = gson.fromJson(reader, JsonObject.class);
        JsonObject jsonResponse = new JsonObject();

        if (body == null || !body.has("vaccinationId")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing vaccinationId.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        int vaccinationId = body.get("vaccinationId").getAsInt();
        Vaccination existing = vaccinationDAO.getVaccinationById(vaccinationId);

        if (existing == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Vaccination record not found.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        Animal animal = animalDAO.getAnimalById(existing.getAnimalId());
        if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal == null || animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        if (body.has("completeOnly") && body.get("completeOnly").getAsBoolean()) {
            // Quick action: complete vaccine
            Date adminDate = body.has("administeredDate") 
                ? Date.valueOf(body.get("administeredDate").getAsString()) 
                : new Date(System.currentTimeMillis());

            if (vaccinationDAO.completeVaccination(vaccinationId, adminDate)) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Vaccination marked as Completed.");
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Database error completing vaccination.");
            }
        } else {
            // General update
            if (body.has("vaccineName")) existing.setVaccineName(body.get("vaccineName").getAsString());
            if (body.has("scheduledDate")) existing.setScheduledDate(Date.valueOf(body.get("scheduledDate").getAsString()));
            if (body.has("notes")) existing.setNotes(body.get("notes").getAsString());
            if (body.has("doctorId")) {
                if (body.get("doctorId").isJsonNull() || body.get("doctorId").getAsString().trim().isEmpty()) {
                    existing.setDoctorId(null);
                } else {
                    existing.setDoctorId(body.get("doctorId").getAsInt());
                }
            }
            
            if (body.has("administeredDate") && !body.get("administeredDate").isJsonNull() && !body.get("administeredDate").getAsString().trim().isEmpty()) {
                existing.setAdministeredDate(Date.valueOf(body.get("administeredDate").getAsString()));
                existing.setStatus("Completed");
            } else {
                existing.setAdministeredDate(null);
                // Status will resolve during save (syncOverdue)
                existing.setStatus("Pending");
            }

            if (vaccinationDAO.updateVaccination(existing)) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Vaccination record updated successfully.");
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Database error updating vaccination record.");
            }
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    // Delete vaccination record
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String idParam = request.getParameter("id");
        JsonObject jsonResponse = new JsonObject();

        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing vaccination id parameter.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Vaccination existing = vaccinationDAO.getVaccinationById(id);
            if (existing == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Record not found.");
            } else {
                Animal animal = animalDAO.getAnimalById(existing.getAnimalId());
                if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (animal == null || animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Forbidden. You do not own this animal's records.");
                } else {
                    if (vaccinationDAO.deleteVaccination(id)) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.addProperty("message", "Vaccination record deleted successfully.");
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Database error deleting record.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid id format.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}

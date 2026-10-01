package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartanimal.dao.AnimalDAO;
import com.smartanimal.dao.VaccinationDAO;
import com.smartanimal.model.Animal;
import com.smartanimal.model.User;
import com.smartanimal.model.Vaccination;
import com.smartanimal.model.VaccineRecommendation;
import com.smartanimal.util.VaccineAdvisor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

public class AnimalServlet extends HttpServlet {
    private final AnimalDAO animalDAO = new AnimalDAO();
    private final VaccinationDAO vaccinationDAO = new VaccinationDAO();
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

    // Retrieve animal details (all or single)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if ("suggestVaccines".equalsIgnoreCase(action)) {
            String species = request.getParameter("species");
            String ageParam = request.getParameter("age");
            String animalType = request.getParameter("animalType");
            String breed = request.getParameter("breed");
            Integer age = null;
            if (ageParam != null && !ageParam.trim().isEmpty()) {
                try {
                    age = Integer.parseInt(ageParam);
                } catch (NumberFormatException ignored) {}
            }
            List<VaccineRecommendation> recs = VaccineAdvisor.getRecommendations(species, age, animalType, breed);
            response.getWriter().write(gson.toJson(recs));
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Animal animal = animalDAO.getAnimalById(id);
                if (animal != null) {
                    // Safety check: verify user permissions
                    boolean allowed = false;
                    if ("Admin".equalsIgnoreCase(user.getRole())) {
                        allowed = true;
                    } else if ("Doctor".equalsIgnoreCase(user.getRole())) {
                        allowed = animalDAO.isAnimalUnderDoctor(id, user.getUserId());
                    } else { // User role
                        allowed = (animal.getUserId() != null && animal.getUserId() == user.getUserId());
                    }

                    if (!allowed) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        JsonObject error = new JsonObject();
                        error.addProperty("success", false);
                        error.addProperty("message", "Forbidden. You do not have permission to view this profile.");
                        response.getWriter().write(gson.toJson(error));
                        return;
                    }
                    response.getWriter().write(gson.toJson(animal));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonObject error = new JsonObject();
                    error.addProperty("success", false);
                    error.addProperty("message", "Animal not found.");
                    response.getWriter().write(gson.toJson(error));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            List<Animal> list;
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                list = animalDAO.getAllAnimals();
            } else if ("Doctor".equalsIgnoreCase(user.getRole())) {
                list = animalDAO.getAnimalsByDoctorId(user.getUserId());
            } else {
                list = animalDAO.getAnimalsByUserId(user.getUserId());
            }
            response.getWriter().write(gson.toJson(list));
        }
    }

    // Create or Update animal
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        JsonObject root;
        try {
            root = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Invalid JSON payload.");
            response.getWriter().write(gson.toJson(err));
            return;
        }

        Animal animal = gson.fromJson(root, Animal.class);
        JsonObject jsonResponse = new JsonObject();

        if (animal == null || animal.getName() == null || animal.getName().trim().isEmpty() ||
            animal.getSpecies() == null || animal.getSpecies().trim().isEmpty() ||
            animal.getAnimalType() == null || animal.getAnimalType().trim().isEmpty()) {
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing required fields: name, species, animalType.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        // Enforce owner details if not provided
        if (animal.getOwnerName() == null || animal.getOwnerName().trim().isEmpty()) {
            animal.setOwnerName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        }

        int scheduledCount = 0;

        if (animal.getAnimalId() > 0) {
            // Update mode
            Animal existing = animalDAO.getAnimalById(animal.getAnimalId());
            if (existing == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Animal not found for update.");
            } else if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (existing.getUserId() == null || existing.getUserId() != user.getUserId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Forbidden. You do not own this profile.");
            } else {
                // Perform update
                existing.setName(animal.getName());
                existing.setSpecies(animal.getSpecies());
                existing.setBreed(animal.getBreed());
                existing.setAge(animal.getAge());
                existing.setWeight(animal.getWeight());
                existing.setAnimalType(animal.getAnimalType());
                existing.setOwnerName(animal.getOwnerName());
                existing.setContactNumber(animal.getContactNumber());

                if (animalDAO.updateAnimal(existing)) {
                    // Check if any vaccinations were selected for scheduling
                    if (root.has("autoScheduleVaccines") && root.get("autoScheduleVaccines").isJsonArray()) {
                        scheduledCount = scheduleVaccinesForAnimal(existing.getAnimalId(), root.getAsJsonArray("autoScheduleVaccines"));
                    }

                    List<VaccineRecommendation> recs = VaccineAdvisor.getRecommendations(existing.getSpecies(), existing.getAge(), existing.getAnimalType(), existing.getBreed());

                    jsonResponse.addProperty("success", true);
                    String msg = "Animal profile updated successfully.";
                    if (scheduledCount > 0) {
                        msg += " Added " + scheduledCount + " recommended vaccine(s) to schedule.";
                    }
                    jsonResponse.addProperty("message", msg);
                    jsonResponse.addProperty("scheduledVaccinesCount", scheduledCount);
                    jsonResponse.add("animal", gson.toJsonTree(existing));
                    jsonResponse.add("recommendations", gson.toJsonTree(recs));
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Database error updating profile.");
                }
            }
        } else {
            // Creation mode
            animal.setUserId(user.getUserId());
            if (animalDAO.addAnimal(animal)) {
                // Auto-schedule selected vaccinations
                if (root.has("autoScheduleVaccines") && root.get("autoScheduleVaccines").isJsonArray()) {
                    scheduledCount = scheduleVaccinesForAnimal(animal.getAnimalId(), root.getAsJsonArray("autoScheduleVaccines"));
                }

                List<VaccineRecommendation> recs = VaccineAdvisor.getRecommendations(animal.getSpecies(), animal.getAge(), animal.getAnimalType(), animal.getBreed());

                jsonResponse.addProperty("success", true);
                String msg = "Animal profile registered successfully!";
                if (scheduledCount > 0) {
                    msg += " Automatically scheduled " + scheduledCount + " age-appropriate vaccination(s).";
                }
                jsonResponse.addProperty("message", msg);
                jsonResponse.addProperty("scheduledVaccinesCount", scheduledCount);
                jsonResponse.add("animal", gson.toJsonTree(animal));
                jsonResponse.add("recommendations", gson.toJsonTree(recs));
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Database error registering profile.");
            }
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private int scheduleVaccinesForAnimal(int animalId, JsonArray vaccinesArray) {
        int count = 0;
        for (JsonElement elem : vaccinesArray) {
            if (!elem.isJsonObject()) continue;
            JsonObject obj = elem.getAsJsonObject();
            String vaccineName = obj.has("vaccineName") ? obj.get("vaccineName").getAsString() : null;
            if (vaccineName == null || vaccineName.trim().isEmpty()) continue;

            String dateStr = obj.has("scheduledDate") ? obj.get("scheduledDate").getAsString() : null;
            Date scheduledDate;
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                try {
                    scheduledDate = Date.valueOf(dateStr);
                } catch (IllegalArgumentException e) {
                    scheduledDate = Date.valueOf(java.time.LocalDate.now().plusDays(14));
                }
            } else {
                scheduledDate = Date.valueOf(java.time.LocalDate.now().plusDays(14));
            }

            String notes = obj.has("notes") && !obj.get("notes").isJsonNull() ? obj.get("notes").getAsString() : "Age-based recommended vaccination";

            Vaccination v = new Vaccination();
            v.setAnimalId(animalId);
            v.setVaccineName(vaccineName);
            v.setScheduledDate(scheduledDate);
            v.setStatus("Pending");
            v.setNotes(notes);

            if (vaccinationDAO.addVaccination(v)) {
                count++;
            }
        }
        return count;
    }

    // Delete animal profile
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
            jsonResponse.addProperty("message", "Missing animal id parameter.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Animal existing = animalDAO.getAnimalById(id);
            if (existing == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Animal not found.");
            } else if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Doctor".equalsIgnoreCase(user.getRole()) && (existing.getUserId() == null || existing.getUserId() != user.getUserId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Forbidden. You do not own this profile.");
            } else {
                if (animalDAO.deleteAnimal(id)) {
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Animal profile deleted successfully.");
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Database error deleting profile.");
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid animal id format.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}

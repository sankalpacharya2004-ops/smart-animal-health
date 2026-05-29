package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartanimal.dao.AnimalDAO;
import com.smartanimal.model.Animal;
import com.smartanimal.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class AnimalServlet extends HttpServlet {
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

    // Retrieve animal details (all or single)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Animal animal = animalDAO.getAnimalById(id);
                if (animal != null) {
                    // Safety check: standard users can only view their own animals
                    if (!"Admin".equalsIgnoreCase(user.getRole()) && (animal.getUserId() == null || animal.getUserId() != user.getUserId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        JsonObject error = new JsonObject();
                        error.addProperty("success", false);
                        error.addProperty("message", "Forbidden. You do not own this profile.");
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

        BufferedReader reader = request.getReader();
        Animal animal = gson.fromJson(reader, Animal.class);
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

        if (animal.getAnimalId() > 0) {
            // Update mode
            Animal existing = animalDAO.getAnimalById(animal.getAnimalId());
            if (existing == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Animal not found for update.");
            } else if (!"Admin".equalsIgnoreCase(user.getRole()) && (existing.getUserId() == null || existing.getUserId() != user.getUserId())) {
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
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Animal profile updated successfully.");
                    jsonResponse.add("animal", gson.toJsonTree(existing));
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Database error updating profile.");
                }
            }
        } else {
            // Creation mode
            animal.setUserId(user.getUserId());
            if (animalDAO.addAnimal(animal)) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Animal profile registered successfully.");
                jsonResponse.add("animal", gson.toJsonTree(animal));
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Database error registering profile.");
            }
        }

        response.getWriter().write(gson.toJson(jsonResponse));
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
            } else if (!"Admin".equalsIgnoreCase(user.getRole()) && (existing.getUserId() == null || existing.getUserId() != user.getUserId())) {
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

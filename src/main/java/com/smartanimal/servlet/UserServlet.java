package com.smartanimal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartanimal.dao.UserDAO;
import com.smartanimal.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class UserServlet extends HttpServlet {
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

    // Get list of all users (Admins only)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;
        if (!verifyAdmin(user, response)) return;

        List<User> userList = userDAO.getAllUsers();
        response.getWriter().write(gson.toJson(userList));
    }

    // Update user role (Admins only, self-demotion blocked)
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User currentUser = getAuthenticatedUser(request, response);
        if (currentUser == null) return;
        if (!verifyAdmin(currentUser, response)) return;

        BufferedReader reader = request.getReader();
        JsonObject body = gson.fromJson(reader, JsonObject.class);
        JsonObject jsonResponse = new JsonObject();

        if (body == null || !body.has("userId") || !body.has("role")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing required fields: userId, role.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        int targetUserId = body.get("userId").getAsInt();
        String targetRole = body.get("role").getAsString();

        if (!"Admin".equalsIgnoreCase(targetRole) && !"User".equalsIgnoreCase(targetRole)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid role type. Must be 'Admin' or 'User'.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        // Prevent self-demotion
        if (currentUser.getUserId() == targetUserId && !"Admin".equalsIgnoreCase(targetRole)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Operation blocked. You cannot demote your own account.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        if (userDAO.updateUserRole(targetUserId, targetRole)) {
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "User role updated successfully.");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error updating user role.");
        }
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    // Delete user account (Admins only, self-deletion blocked)
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User currentUser = getAuthenticatedUser(request, response);
        if (currentUser == null) return;
        if (!verifyAdmin(currentUser, response)) return;

        String idParam = request.getParameter("id");
        JsonObject jsonResponse = new JsonObject();

        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing user id parameter.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        try {
            int targetUserId = Integer.parseInt(idParam);

            // Prevent self-deletion
            if (currentUser.getUserId() == targetUserId) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Operation blocked. You cannot delete your own admin account.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            if (userDAO.deleteUser(targetUserId)) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "User account and all animal records removed successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "User account not found.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid user id format.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}

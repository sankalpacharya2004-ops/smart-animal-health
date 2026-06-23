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

public class AuthServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    // Check if user is logged in
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        JsonObject jsonResponse = new JsonObject();

        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            jsonResponse.addProperty("authenticated", true);
            jsonResponse.add("user", gson.toJsonTree(user));
        } else {
            jsonResponse.addProperty("authenticated", false);
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    // Handles Login, Signup, and Logout
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        // Fallback: Check body if action is not in query params
        if (action == null) {
            BufferedReader reader = request.getReader();
            JsonObject body = gson.fromJson(reader, JsonObject.class);
            if (body != null && body.has("action")) {
                action = body.get("action").getAsString();
                handleAction(action, body, request, response, jsonResponse);
                return;
            }
        }

        // Handle using query params or direct post fields
        if (action != null) {
            JsonObject body = new JsonObject();
            body.addProperty("username", request.getParameter("username"));
            body.addProperty("password", request.getParameter("password"));
            body.addProperty("fullName", request.getParameter("fullName"));
            body.addProperty("email", request.getParameter("email"));
            body.addProperty("role", request.getParameter("role"));
            body.addProperty("qualification", request.getParameter("qualification"));
            body.addProperty("address", request.getParameter("address"));
            body.addProperty("experience", request.getParameter("experience"));
            body.addProperty("phoneNo", request.getParameter("phoneNo"));
            body.addProperty("certificate", request.getParameter("certificate"));
            handleAction(action, body, request, response, jsonResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing action parameter.");
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }

    private void handleAction(String action, JsonObject data, HttpServletRequest request, HttpServletResponse response, JsonObject jsonResponse) throws IOException {
        if ("login".equalsIgnoreCase(action)) {
            String username = data.has("username") ? data.get("username").getAsString() : null;
            String password = data.has("password") ? data.get("password").getAsString() : null;

            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Username and Password are required.");
            } else {
                User user = userDAO.validateUser(username, password);
                if (user != null) {
                    if ("Doctor".equalsIgnoreCase(user.getRole()) && !user.isApproved()) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Your account is yet to be approved by the Admin.");
                    } else {
                        HttpSession session = request.getSession(true);
                        session.setAttribute("user", user);
                        jsonResponse.addProperty("success", true);
                        jsonResponse.addProperty("message", "Login successful!");
                        jsonResponse.add("user", gson.toJsonTree(user));
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Invalid username or password.");
                }
            }
        } 
        else if ("signup".equalsIgnoreCase(action)) {
            String username = data.has("username") && !data.get("username").isJsonNull() ? data.get("username").getAsString() : null;
            String password = data.has("password") && !data.get("password").isJsonNull() ? data.get("password").getAsString() : null;
            String fullName = data.has("fullName") && !data.get("fullName").isJsonNull() ? data.get("fullName").getAsString() : null;
            String email = data.has("email") && !data.get("email").isJsonNull() ? data.get("email").getAsString() : null;
            String role = data.has("role") && !data.get("role").isJsonNull() ? data.get("role").getAsString() : "User";

            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Username and Password are required.");
            } else if (userDAO.isUsernameExists(username)) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Username is already taken.");
            } else {
                User user = new User();
                user.setUsername(username);
                user.setPasswordHash(password); // Hashed inside UserDAO.registerUser
                user.setFullName(fullName);
                user.setEmail(email);
                user.setRole(role);
                
                if ("Doctor".equalsIgnoreCase(role)) {
                    String qual = data.has("qualification") && !data.get("qualification").isJsonNull() ? data.get("qualification").getAsString() : null;
                    String addr = data.has("address") && !data.get("address").isJsonNull() ? data.get("address").getAsString() : null;
                    int exp = 0;
                    if (data.has("experience") && !data.get("experience").isJsonNull()) {
                        try {
                            exp = Integer.parseInt(data.get("experience").getAsString());
                        } catch (Exception e) {}
                    }
                    String phone = data.has("phoneNo") && !data.get("phoneNo").isJsonNull() ? data.get("phoneNo").getAsString() : null;
                    String cert = data.has("certificate") && !data.get("certificate").isJsonNull() ? data.get("certificate").getAsString() : null;
                    
                    user.setQualification(qual);
                    user.setAddress(addr);
                    user.setExperience(exp);
                    user.setPhoneNo(phone);
                    user.setCertificate(cert);
                    user.setApproved(false); // Doctor registration requests require Admin approval
                } else {
                    user.setApproved(true); // Regular users are approved automatically
                }

                if (userDAO.registerUser(user)) {
                    jsonResponse.addProperty("success", true);
                    if ("Doctor".equalsIgnoreCase(role)) {
                        jsonResponse.addProperty("message", "Registration request submitted. Pending administrator approval!");
                    } else {
                        HttpSession session = request.getSession(true);
                        session.setAttribute("user", user);
                        jsonResponse.addProperty("success", true);
                        jsonResponse.addProperty("message", "Registration successful!");
                        jsonResponse.add("user", gson.toJsonTree(user));
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Registration failed due to server error.");
                }
            }
        } 
        else if ("logout".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Logged out successfully!");
        } 
        else {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid action: " + action);
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}

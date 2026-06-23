package com.smartanimal.dao;

import com.smartanimal.model.User;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

public class UserDAO {

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role, qualification, address, experience, phone_no, certificate, is_approved) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashPassword(user.getPasswordHash())); // The model contains raw password before save
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole() != null ? user.getRole() : "User");
            stmt.setString(6, user.getQualification());
            stmt.setString(7, user.getAddress());
            stmt.setInt(8, user.getExperience());
            stmt.setString(9, user.getPhoneNo());
            stmt.setString(10, user.getCertificate());
            stmt.setBoolean(11, user.isApproved());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User validateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        null, // Do not expose password hash in session object
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("created_date")
                    );
                    u.setQualification(rs.getString("qualification"));
                    u.setAddress(rs.getString("address"));
                    u.setExperience(rs.getInt("experience"));
                    u.setPhoneNo(rs.getString("phone_no"));
                    u.setCertificate(rs.getString("certificate"));
                    u.setApproved(rs.getBoolean("is_approved"));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        null,
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("created_date")
                    );
                    u.setQualification(rs.getString("qualification"));
                    u.setAddress(rs.getString("address"));
                    u.setExperience(rs.getInt("experience"));
                    u.setPhoneNo(rs.getString("phone_no"));
                    u.setCertificate(rs.getString("certificate"));
                    u.setApproved(rs.getBoolean("is_approved"));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    null, // Do not expose password hash
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getTimestamp("created_date")
                );
                u.setQualification(rs.getString("qualification"));
                u.setAddress(rs.getString("address"));
                u.setExperience(rs.getInt("experience"));
                u.setPhoneNo(rs.getString("phone_no"));
                u.setCertificate(rs.getString("certificate"));
                u.setApproved(rs.getBoolean("is_approved"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateUserRole(int userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserApproval(int userId, boolean isApproved) {
        String sql = "UPDATE users SET is_approved = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isApproved);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getApprovedDoctors() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Doctor' AND is_approved = 1 ORDER BY full_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    null,
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getTimestamp("created_date")
                );
                u.setQualification(rs.getString("qualification"));
                u.setAddress(rs.getString("address"));
                u.setExperience(rs.getInt("experience"));
                u.setPhoneNo(rs.getString("phone_no"));
                u.setCertificate(rs.getString("certificate"));
                u.setApproved(rs.getBoolean("is_approved"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

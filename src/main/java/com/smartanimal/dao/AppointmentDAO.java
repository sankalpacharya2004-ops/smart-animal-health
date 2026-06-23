package com.smartanimal.dao;

import com.smartanimal.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private Appointment extractAppointment(ResultSet rs) throws SQLException {
        Appointment app = new Appointment();
        app.setAppointmentId(rs.getInt("appointment_id"));
        app.setAnimalId(rs.getInt("animal_id"));
        app.setDoctorId(rs.getInt("doctor_id"));
        app.setAppointmentDate(rs.getTimestamp("appointment_date"));
        app.setStatus(rs.getString("status"));
        app.setReason(rs.getString("reason"));
        app.setCreatedDate(rs.getTimestamp("created_date"));

        // Join-derived fields
        app.setAnimalName(rs.getString("animal_name"));
        app.setAnimalSpecies(rs.getString("animal_species"));
        app.setAnimalBreed(rs.getString("animal_breed"));
        
        int age = rs.getInt("animal_age");
        app.setAnimalAge(rs.wasNull() ? null : age);
        
        double weight = rs.getDouble("animal_weight");
        app.setAnimalWeight(rs.wasNull() ? null : weight);

        app.setOwnerName(rs.getString("owner_name"));
        app.setOwnerContact(rs.getString("owner_contact"));
        app.setDoctorName(rs.getString("doctor_name"));
        
        return app;
    }

    public boolean addAppointment(Appointment app) {
        String sql = "INSERT INTO appointments (animal_id, doctor_id, appointment_date, status, reason) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, app.getAnimalId());
            stmt.setInt(2, app.getDoctorId());
            stmt.setTimestamp(3, app.getAppointmentDate());
            stmt.setString(4, app.getStatus() != null ? app.getStatus() : "Scheduled");
            stmt.setString(5, app.getReason());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        app.setAppointmentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAppointmentStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Appointment> getAppointmentsByUserId(int userId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "an.name AS animal_name, an.species AS animal_species, an.breed AS animal_breed, an.age AS animal_age, an.weight AS animal_weight, " +
                     "an.owner_name AS owner_name, an.contact_number AS owner_contact, " +
                     "d.full_name AS doctor_name " +
                     "FROM appointments a " +
                     "JOIN animals an ON a.animal_id = an.animal_id " +
                     "JOIN users d ON a.doctor_id = d.user_id " +
                     "WHERE an.user_id = ? " +
                     "ORDER BY a.appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppointment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "an.name AS animal_name, an.species AS animal_species, an.breed AS animal_breed, an.age AS animal_age, an.weight AS animal_weight, " +
                     "an.owner_name AS owner_name, an.contact_number AS owner_contact, " +
                     "d.full_name AS doctor_name " +
                     "FROM appointments a " +
                     "JOIN animals an ON a.animal_id = an.animal_id " +
                     "JOIN users d ON a.doctor_id = d.user_id " +
                     "WHERE a.doctor_id = ? " +
                     "ORDER BY a.appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppointment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "an.name AS animal_name, an.species AS animal_species, an.breed AS animal_breed, an.age AS animal_age, an.weight AS animal_weight, " +
                     "an.owner_name AS owner_name, an.contact_number AS owner_contact, " +
                     "d.full_name AS doctor_name " +
                     "FROM appointments a " +
                     "JOIN animals an ON a.animal_id = an.animal_id " +
                     "JOIN users d ON a.doctor_id = d.user_id " +
                     "ORDER BY a.appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(extractAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Appointment getAppointmentById(int appointmentId) {
        String sql = "SELECT a.*, " +
                     "an.name AS animal_name, an.species AS animal_species, an.breed AS animal_breed, an.age AS animal_age, an.weight AS animal_weight, " +
                     "an.owner_name AS owner_name, an.contact_number AS owner_contact, " +
                     "d.full_name AS doctor_name " +
                     "FROM appointments a " +
                     "JOIN animals an ON a.animal_id = an.animal_id " +
                     "JOIN users d ON a.doctor_id = d.user_id " +
                     "WHERE a.appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAppointment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

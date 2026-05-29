package com.smartanimal.dao;

import com.smartanimal.model.Vaccination;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VaccinationDAO {

    private Vaccination extractVaccination(ResultSet rs) throws SQLException {
        Vaccination vaccination = new Vaccination();
        vaccination.setVaccinationId(rs.getInt("vaccination_id"));
        vaccination.setAnimalId(rs.getInt("animal_id"));
        vaccination.setVaccineName(rs.getString("vaccine_name"));
        vaccination.setScheduledDate(rs.getDate("scheduled_date"));
        vaccination.setAdministeredDate(rs.getDate("administered_date"));
        vaccination.setStatus(rs.getString("status"));
        vaccination.setNotes(rs.getString("notes"));
        vaccination.setCreatedDate(rs.getTimestamp("created_date"));
        return vaccination;
    }

    // Runs a status update check to automatically mark pending vaccinations as overdue
    private void syncVaccinationStatuses(Connection conn) {
        String sql = "UPDATE vaccinations SET status = 'Overdue' WHERE status = 'Pending' AND scheduled_date < CURDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addVaccination(Vaccination vaccination) {
        String sql = "INSERT INTO vaccinations (animal_id, vaccine_name, scheduled_date, administered_date, status, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            syncVaccinationStatuses(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, vaccination.getAnimalId());
                stmt.setString(2, vaccination.getVaccineName());
                stmt.setDate(3, vaccination.getScheduledDate());
                stmt.setDate(4, vaccination.getAdministeredDate());
                
                // If scheduled date is in the past and no admin date is set, default to Overdue, else Pending
                String finalStatus = vaccination.getStatus();
                if (finalStatus == null || finalStatus.equals("Pending")) {
                    if (vaccination.getScheduledDate().before(new java.util.Date()) && vaccination.getAdministeredDate() == null) {
                        finalStatus = "Overdue";
                    } else {
                        finalStatus = "Pending";
                    }
                }
                stmt.setString(5, finalStatus);
                stmt.setString(6, vaccination.getNotes());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            vaccination.setVaccinationId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Vaccination> getVaccinationsByAnimalId(int animalId) {
        List<Vaccination> list = new ArrayList<>();
        String sql = "SELECT * FROM vaccinations WHERE animal_id = ? ORDER BY scheduled_date ASC";
        try (Connection conn = DBConnection.getConnection()) {
            syncVaccinationStatuses(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, animalId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(extractVaccination(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Vaccination getVaccinationById(int vaccinationId) {
        String sql = "SELECT * FROM vaccinations WHERE vaccination_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            syncVaccinationStatuses(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vaccinationId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return extractVaccination(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateVaccination(Vaccination vaccination) {
        String sql = "UPDATE vaccinations SET vaccine_name = ?, scheduled_date = ?, administered_date = ?, status = ?, notes = ? WHERE vaccination_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            syncVaccinationStatuses(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, vaccination.getVaccineName());
                stmt.setDate(2, vaccination.getScheduledDate());
                stmt.setDate(3, vaccination.getAdministeredDate());
                stmt.setString(4, vaccination.getStatus());
                stmt.setString(5, vaccination.getNotes());
                stmt.setInt(6, vaccination.getVaccinationId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean completeVaccination(int vaccinationId, Date administeredDate) {
        String sql = "UPDATE vaccinations SET administered_date = ?, status = 'Completed' WHERE vaccination_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, administeredDate);
            stmt.setInt(2, vaccinationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteVaccination(int vaccinationId) {
        String sql = "DELETE FROM vaccinations WHERE vaccination_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vaccinationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Vaccination> getAllVaccinationsWithDetails() {
        List<Vaccination> list = new ArrayList<>();
        String sql = "SELECT v.*, a.name AS animal_name, a.owner_name FROM vaccinations v JOIN animals a ON v.animal_id = a.animal_id ORDER BY v.scheduled_date ASC";
        try (Connection conn = DBConnection.getConnection()) {
            syncVaccinationStatuses(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vaccination v = extractVaccination(rs);
                    v.setAnimalName(rs.getString("animal_name"));
                    v.setOwnerName(rs.getString("owner_name"));
                    list.add(v);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

package dao;

import config.DatabaseConnection;
import model.Tipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipeDAO {

    // Ambil semua tipe (tanpa pencarian)
    public List<Tipe> getAllTipe() {
        return searchTipe("");
    }

    // Pencarian tipe berdasarkan keyword
    public List<Tipe> getAllTipe(String keyword) {
        return searchTipe(keyword != null ? keyword : "");
    }

    // Method utama pencarian
    private List<Tipe> searchTipe(String keyword) {
        List<Tipe> list = new ArrayList<>();
        String sql = "SELECT id_tipe, nama_tipe, keterangan FROM tipe WHERE " +
                     "LOWER(nama_tipe) LIKE LOWER(?) " +
                     "OR LOWER(keterangan) LIKE LOWER(?) " +
                     "ORDER BY nama_tipe";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Tipe tipe = new Tipe();
                tipe.setIdTipe(rs.getLong("id_tipe"));
                tipe.setNamaTipe(rs.getString("nama_tipe"));
                tipe.setKeterangan(rs.getString("keterangan"));
                list.add(tipe);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createTipe(Tipe tipe) {
        String sql = "INSERT INTO tipe (nama_tipe, keterangan, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe.getNamaTipe());
            ps.setString(2, tipe.getKeterangan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTipe(Tipe tipe) {
        String sql = "UPDATE tipe SET nama_tipe = ?, keterangan = ? created_at = NOW() WHERE id_tipe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe.getNamaTipe());
            ps.setString(2, tipe.getKeterangan());
            ps.setLong(3, tipe.getIdTipe());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTipe(Long idTipe) {
        String sql = "DELETE FROM tipe WHERE id_tipe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipe);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
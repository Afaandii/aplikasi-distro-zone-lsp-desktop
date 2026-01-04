package dao;

import config.DatabaseConnection;
import model.Warna;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarnaDAO {

    // Ambil semua warna (tanpa keyword)
    public List<Warna> getAllWarna() {
        return searchWarna("");
    }

    // Pencarian warna berdasarkan keyword
    public List<Warna> getAllWarna(String keyword) {
        return searchWarna(keyword != null ? keyword : "");
    }

    // Method inti pencarian
    private List<Warna> searchWarna(String keyword) {
        List<Warna> list = new ArrayList<>();
        String sql = "SELECT id_warna, nama_warna, keterangan FROM warna WHERE " +
                     "LOWER(nama_warna) LIKE LOWER(?) " +
                     "OR LOWER(keterangan) LIKE LOWER(?) " +
                     "ORDER BY nama_warna";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Warna warna = new Warna();
                warna.setIdWarna(rs.getLong("id_warna"));
                warna.setNamaWarna(rs.getString("nama_warna"));
                warna.setKeterangan(rs.getString("keterangan"));
                list.add(warna);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createWarna(Warna warna) {
        String sql = "INSERT INTO warna (nama_warna, keterangan, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, warna.getNamaWarna());
            ps.setString(2, warna.getKeterangan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateWarna(Warna warna) {
        String sql = "UPDATE warna SET nama_warna = ?, keterangan = ?, updated_at = NOW() WHERE id_warna = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, warna.getNamaWarna());
            ps.setString(2, warna.getKeterangan());
            ps.setLong(3, warna.getIdWarna());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteWarna(Long idWarna) {
        String sql = "DELETE FROM warna WHERE id_warna = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idWarna);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
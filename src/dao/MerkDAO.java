package dao;

import config.DatabaseConnection;
import model.Merk;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MerkDAO {

    // Ambil semua merk (tanpa pencarian)
    public List<Merk> getAllMerk() {
        return searchMerk(""); // gunakan searchMerk dengan keyword kosong
    }

    // Pencarian merk berdasarkan keyword
    public List<Merk> getAllMerk(String keyword) {
        return searchMerk(keyword != null ? keyword : "");
    }

    // Method utama pencarian (dipakai oleh keduanya)
    private List<Merk> searchMerk(String keyword) {
        List<Merk> list = new ArrayList<>();
        String sql = "SELECT * FROM merk WHERE " +
                     "LOWER(nama_merk) LIKE LOWER(?) " +
                     "OR LOWER(keterangan) LIKE LOWER(?) " +
                     "ORDER BY nama_merk ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Merk m = new Merk();
                m.setIdMerk(rs.getLong("id_merk"));
                m.setNamaMerk(rs.getString("nama_merk"));
                m.setKeterangan(rs.getString("keterangan"));
                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Create
    public boolean createMerk(Merk merk) {
        String sql = "INSERT INTO merk (nama_merk, keterangan, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, merk.getNamaMerk());
            stmt.setString(2, merk.getKeterangan());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update
    public boolean updateMerk(Merk merk) {
        String sql = "UPDATE merk SET nama_merk = ?, keterangan = ?, updated_at = NOW() WHERE id_merk = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, merk.getNamaMerk());
            stmt.setString(2, merk.getKeterangan());
            stmt.setLong(3, merk.getIdMerk());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteMerk(Long idMerk) {
        String sql = "DELETE FROM merk WHERE id_merk = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idMerk);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
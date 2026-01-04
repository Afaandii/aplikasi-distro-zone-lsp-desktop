package dao;

import config.DatabaseConnection;
import model.Ukuran;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UkuranDAO {

    // Ambil semua ukuran (tanpa keyword)
    public List<Ukuran> getAllUkuran() {
        return searchUkuran("");
    }

    // Pencarian ukuran berdasarkan keyword
    public List<Ukuran> getAllUkuran(String keyword) {
        return searchUkuran(keyword != null ? keyword : "");
    }

    // Method inti pencarian
    private List<Ukuran> searchUkuran(String keyword) {
        List<Ukuran> list = new ArrayList<>();
        String sql = "SELECT id_ukuran, nama_ukuran, keterangan FROM ukuran WHERE " +
                     "LOWER(nama_ukuran) LIKE LOWER(?) " +
                     "OR LOWER(keterangan) LIKE LOWER(?) " +
                     "ORDER BY nama_ukuran";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ukuran ukuran = new Ukuran();
                ukuran.setIdUkuran(rs.getLong("id_ukuran"));
                ukuran.setNamaUkuran(rs.getString("nama_ukuran"));
                ukuran.setKeterangan(rs.getString("keterangan"));
                list.add(ukuran);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createUkuran(Ukuran ukuran) {
        String sql = "INSERT INTO ukuran (nama_ukuran, keterangan, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ukuran.getNamaUkuran());
            ps.setString(2, ukuran.getKeterangan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUkuran(Ukuran ukuran) {
        String sql = "UPDATE ukuran SET nama_ukuran = ?, keterangan = ?, created_at = NOW() WHERE id_ukuran = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ukuran.getNamaUkuran());
            ps.setString(2, ukuran.getKeterangan());
            ps.setLong(3, ukuran.getIdUkuran());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUkuran(Long idUkuran) {
        String sql = "DELETE FROM ukuran WHERE id_ukuran = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUkuran);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
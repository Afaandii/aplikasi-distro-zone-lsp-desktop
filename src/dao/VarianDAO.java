package dao;

import config.DatabaseConnection;
import model.Varian;
import model.Produk;
import model.Ukuran;
import model.Warna;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VarianDAO {

    // Ambil semua varian dengan relasi (nama produk, ukuran, warna)
    public List<Varian> getAllVarian() {
        List<Varian> list = new ArrayList<>();
        String sql = "SELECT " +
                "v.id_varian, v.id_produk, v.id_ukuran, v.id_warna, v.stok_kaos, " +
                "p.nama_kaos AS nama_produk, " +
                "u.nama_ukuran, " +
                "w.nama_warna, " +
                "v.created_at, v.updated_at " +
                "FROM varian v " +
                "JOIN produk p ON v.id_produk = p.id_produk " +
                "JOIN ukuran u ON v.id_ukuran = u.id_ukuran " +
                "JOIN warna w ON v.id_warna = w.id_warna " +
                "ORDER BY p.nama_kaos, u.nama_ukuran, w.nama_warna";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Varian varian = new Varian();
                varian.setIdVarian(rs.getLong("id_varian"));
                varian.setIdProduk(rs.getLong("id_produk"));
                varian.setIdUkuran(rs.getLong("id_ukuran"));
                varian.setIdWarna(rs.getLong("id_warna"));
                varian.setStokKaos(rs.getLong("stok_kaos"));
                varian.setCreatedAt(rs.getTimestamp("created_at"));
                varian.setUpdatedAt(rs.getTimestamp("updated_at"));

                // Relasi
                varian.setNamaProduk(rs.getString("nama_produk")); // Tambahkan di model
                varian.setNamaUkuran(rs.getString("nama_ukuran"));
                varian.setNamaWarna(rs.getString("nama_warna"));

                list.add(varian);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Ambil varian berdasarkan id_produk
    public List<Varian> getVarianByProduk(Long idProduk) {
        List<Varian> list = new ArrayList<>();
        String sql = "SELECT " +
                "v.id_varian, v.id_produk, v.id_ukuran, v.id_warna, v.stok_kaos, " +
                "p.nama_kaos AS nama_produk, " +
                "u.nama_ukuran, " +
                "w.nama_warna, " +
                "v.created_at, v.updated_at " +
                "FROM varian v " +
                "JOIN produk p ON v.id_produk = p.id_produk " +
                "JOIN ukuran u ON v.id_ukuran = u.id_ukuran " +
                "JOIN warna w ON v.id_warna = w.id_warna " +
                "WHERE v.id_produk = ? " +
                "ORDER BY u.nama_ukuran, w.nama_warna";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idProduk);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Varian varian = new Varian();
                    varian.setIdVarian(rs.getLong("id_varian"));
                    varian.setIdProduk(rs.getLong("id_produk"));
                    varian.setIdUkuran(rs.getLong("id_ukuran"));
                    varian.setIdWarna(rs.getLong("id_warna"));
                    varian.setStokKaos(rs.getLong("stok_kaos"));
                    varian.setCreatedAt(rs.getTimestamp("created_at"));
                    varian.setUpdatedAt(rs.getTimestamp("updated_at"));

                    varian.setNamaProduk(rs.getString("nama_produk"));
                    varian.setNamaUkuran(rs.getString("nama_ukuran"));
                    varian.setNamaWarna(rs.getString("nama_warna"));

                    list.add(varian);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Tambah varian baru
    public boolean createVarian(Varian varian) {
        String sql = "INSERT INTO varian (id_produk, id_ukuran, id_warna, stok_kaos, created_at) " +
                     "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, varian.getIdProduk());
            ps.setLong(2, varian.getIdUkuran());
            ps.setLong(3, varian.getIdWarna());
            ps.setLong(4, varian.getStokKaos());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        varian.setIdVarian(generatedKeys.getLong(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update stok varian (tidak boleh ubah id_produk, id_ukuran, id_warna)
    public boolean updateVarian(Varian varian) {
        String sql = "UPDATE varian SET stok_kaos = ?, updated_at = NOW() " +
                     "WHERE id_varian = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, varian.getStokKaos());
            ps.setLong(2, varian.getIdVarian());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hapus varian
    public boolean deleteVarian(Long idVarian) {
        String sql = "DELETE FROM varian WHERE id_varian = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idVarian);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Misal: masih dipakai di keranjang/transaksi
            e.printStackTrace();
            return false;
        }
    }

    // Cek apakah kombinasi (id_produk, id_ukuran, id_warna) sudah ada
    public boolean isVarianExists(Long idProduk, Long idUkuran, Long idWarna, Long excludeIdVarian) {
        String sql = "SELECT COUNT(*) FROM varian WHERE " +
                     "id_produk = ? AND id_ukuran = ? AND id_warna = ?";

        if (excludeIdVarian != null) {
            sql += " AND id_varian != ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idProduk);
            ps.setLong(2, idUkuran);
            ps.setLong(3, idWarna);

            if (excludeIdVarian != null) {
                ps.setLong(4, excludeIdVarian);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
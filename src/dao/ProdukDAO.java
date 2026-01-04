package dao;

import config.DatabaseConnection;
import model.Produk;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Tipe;

public class ProdukDAO {
    
    // CREATE
    public boolean tambahProduk(Produk produk) {
        String sql = "INSERT INTO produk (id_merk, id_tipe, nama_kaos, harga_jual, harga_pokok, " +
                     "deskripsi, spesifikasi, berat, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, produk.getIdMerk());
            pstmt.setLong(2, produk.getIdTipe());
            pstmt.setString(3, produk.getNamaKaos());
            pstmt.setLong(4, produk.getHargaJual());
            pstmt.setLong(5, produk.getHargaPokok());
            pstmt.setString(6, produk.getDeskripsi());
            pstmt.setString(7, produk.getSpesifikasi());
            pstmt.setBigDecimal(8, produk.getBerat());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    produk.setIdProduk(rs.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // READ ALL
    public List<Produk> getAllProduk() {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT p.*, m.nama_merk, t.nama_tipe " +
                     "FROM produk p " +
                     "LEFT JOIN merk m ON p.id_merk = m.id_merk " +
                     "LEFT JOIN tipe t ON p.id_tipe = t.id_tipe " +
                     "ORDER BY p.id_produk DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Produk p = new Produk();
                p.setIdProduk(rs.getLong("id_produk"));
                p.setIdMerk(rs.getLong("id_merk"));
                p.setIdTipe(rs.getLong("id_tipe"));
                p.setNamaKaos(rs.getString("nama_kaos"));
                p.setHargaJual(rs.getLong("harga_jual"));
                p.setHargaPokok(rs.getLong("harga_pokok"));
                p.setDeskripsi(rs.getString("deskripsi"));
                p.setSpesifikasi(rs.getString("spesifikasi"));
                p.setBerat(rs.getBigDecimal("berat"));
                p.setNamaMerk(rs.getString("nama_merk"));
                p.setNamaTipe(rs.getString("nama_tipe"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                p.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Produk> getAllProduk(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
        return getAllProduk(); // ambil semua
    }
    return searchProduk(keyword); // pakai search
}

    
    // READ BY ID
    public Produk getProdukById(Long id) {
        String sql = "SELECT p.*, m.nama_merk, t.nama_tipe " +
                     "FROM produk p " +
                     "LEFT JOIN merk m ON p.id_merk = m.id_merk " +
                     "LEFT JOIN tipe t ON p.id_tipe = t.id_tipe " +
                     "WHERE p.id_produk = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Produk p = new Produk();
                p.setIdProduk(rs.getLong("id_produk"));
                p.setIdMerk(rs.getLong("id_merk"));
                p.setIdTipe(rs.getLong("id_tipe"));
                p.setNamaKaos(rs.getString("nama_kaos"));
                p.setHargaJual(rs.getLong("harga_jual"));
                p.setHargaPokok(rs.getLong("harga_pokok"));
                p.setDeskripsi(rs.getString("deskripsi"));
                p.setSpesifikasi(rs.getString("spesifikasi"));
                p.setBerat(rs.getBigDecimal("berat"));
                p.setNamaMerk(rs.getString("nama_merk"));
                p.setNamaTipe(rs.getString("nama_tipe"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                p.setUpdatedAt(rs.getTimestamp("updated_at"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // UPDATE
    public boolean updateProduk(Produk produk) {
        String sql = "UPDATE produk SET id_merk=?, id_tipe=?, nama_kaos=?, harga_jual=?, " +
                     "harga_pokok=?, deskripsi=?, spesifikasi=?, berat=?, updated_at=CURRENT_TIMESTAMP " +
                     "WHERE id_produk=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, produk.getIdMerk());
            pstmt.setLong(2, produk.getIdTipe());
            pstmt.setString(3, produk.getNamaKaos());
            pstmt.setLong(4, produk.getHargaJual());
            pstmt.setLong(5, produk.getHargaPokok());
            pstmt.setString(6, produk.getDeskripsi());
            pstmt.setString(7, produk.getSpesifikasi());
            pstmt.setBigDecimal(8, produk.getBerat());
            pstmt.setLong(9, produk.getIdProduk());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // DELETE
    public boolean deleteProduk(Long id) {
        String sql = "DELETE FROM produk WHERE id_produk=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // SEARCH
    public List<Produk> searchProduk(String keyword) {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT p.*, m.nama_merk, t.nama_tipe " +
                     "FROM produk p " +
                     "LEFT JOIN merk m ON p.id_merk = m.id_merk " +
                     "LEFT JOIN tipe t ON p.id_tipe = t.id_tipe " +
                     "WHERE LOWER(p.nama_kaos) LIKE LOWER(?) " +
                     "OR LOWER(m.nama_merk) LIKE LOWER(?) " +
                     "OR LOWER(t.nama_tipe) LIKE LOWER(?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Produk p = new Produk();
                p.setIdProduk(rs.getLong("id_produk"));
                p.setIdMerk(rs.getLong("id_merk"));
                p.setIdTipe(rs.getLong("id_tipe"));
                p.setNamaKaos(rs.getString("nama_kaos"));
                p.setHargaJual(rs.getLong("harga_jual"));
                p.setHargaPokok(rs.getLong("harga_pokok"));
                p.setNamaMerk(rs.getString("nama_merk"));
                p.setNamaTipe(rs.getString("nama_tipe"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                p.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // GET TOTAL STOK
    public Long getTotalStokProduk(Long idProduk) {
        String sql = "SELECT COALESCE(SUM(stok_kaos), 0) FROM varian WHERE id_produk = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, idProduk);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }
    
    public List<Tipe> getAllTipe() {
        List<Tipe> list = new ArrayList<>();
        String sql = "SELECT id_tipe, nama_tipe, keterangan FROM tipe ORDER BY nama_tipe";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
}
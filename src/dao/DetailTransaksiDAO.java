package dao;

import config.DatabaseConnection;
import model.DetailTransaksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailTransaksiDAO {
    
    // Create detail transaksi
    public boolean createDetailTransaksi(DetailTransaksi detail) {
        String sql = "INSERT INTO detail_transaksi (id_transaksi, id_produk, jumlah, harga_satuan, subtotal, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, detail.getIdTransaksi());
            stmt.setLong(2, detail.getIdProduk());
            stmt.setLong(3, detail.getJumlah());
            stmt.setLong(4, detail.getHargaSatuan());
            stmt.setLong(5, detail.getSubtotal());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get detail by transaksi
    public List<DetailTransaksi> getDetailByTransaksi(Long idTransaksi) {
        List<DetailTransaksi> details = new ArrayList<>();
        String sql = "SELECT dt.*, p.nama_kaos FROM detail_transaksi dt " +
                     "JOIN produk p ON dt.id_produk = p.id_produk " +
                     "WHERE dt.id_transaksi = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idTransaksi);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                DetailTransaksi detail = new DetailTransaksi();
                detail.setIdDetailTransaksi(rs.getLong("id_detail_transaksi"));
                detail.setIdTransaksi(rs.getLong("id_transaksi"));
                detail.setIdProduk(rs.getLong("id_produk"));
                detail.setJumlah(rs.getLong("jumlah"));
                detail.setHargaSatuan(rs.getLong("harga_satuan"));
                detail.setSubtotal(rs.getLong("subtotal"));
                detail.setNamaProduk(rs.getString("nama_kaos"));
                detail.setCreatedAt(rs.getTimestamp("created_at"));
                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
    
    public Long getTotalSubtotalByTransaksi(Long idTransaksi) {
        String sql = "SELECT COALESCE(SUM(subtotal), 0) as total FROM detail_transaksi WHERE id_transaksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idTransaksi);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
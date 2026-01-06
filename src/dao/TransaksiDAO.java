package dao;

import config.DatabaseConnection;
import model.Transaksi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {
    
    // Create transaction
    public Long createTransaksi(Transaksi transaksi) {
        String sql = "INSERT INTO transaksi (id_customer, id_kasir, kode_transaksi, total, " +
                     "metode_pembayaran, status_transaksi, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW()) RETURNING id_transaksi";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, transaksi.getIdCustomer());
            stmt.setLong(2, transaksi.getIdKasir());
            stmt.setString(3, transaksi.getKodeTransaksi());
            stmt.setLong(4, transaksi.getTotal());
            stmt.setString(5, transaksi.getMetodePembayaran());
            stmt.setString(6, transaksi.getStatusTransaksi());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id_transaksi");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Get transactions by kasir with date filter
    public List<Transaksi> getTransaksiByKasir(Long idKasir, Date startDate, Date endDate) {
        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT t.*, uc.nama as nama_customer, uk.nama as nama_kasir " +
                     "FROM transaksi t " +
                     "LEFT JOIN users uc ON t.id_customer = uc.id_user " +
                     "LEFT JOIN users uk ON t.id_kasir = uk.id_user " +
                     "WHERE t.id_kasir = ? AND DATE(t.created_at) BETWEEN ? AND ? " +
                     "ORDER BY t.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idKasir);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaksi transaksi = mapResultSetToTransaksi(rs);
                transaksiList.add(transaksi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transaksiList;
    }
    
    // Get all transactions with date filter (Admin)
    public List<Transaksi> getAllTransaksi(Date startDate, Date endDate) {
        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT t.*, uc.nama as nama_customer, uk.nama as nama_kasir " +
                     "FROM transaksi t " +
                     "LEFT JOIN users uc ON t.id_customer = uc.id_user " +
                     "LEFT JOIN users uk ON t.id_kasir = uk.id_user " +
                     "WHERE DATE(t.created_at) BETWEEN ? AND ? " +
                     "ORDER BY t.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaksi transaksi = mapResultSetToTransaksi(rs);
                transaksiList.add(transaksi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transaksiList;
    }
    
    // Get sales report
    public Long getTotalPenjualan(Long idKasir, Date startDate, Date endDate) {
        String sql = idKasir != null 
            ? "SELECT COALESCE(SUM(total), 0) as total FROM transaksi " +
              "WHERE id_kasir = ? AND status_transaksi = 'selesai' " +
              "AND DATE(created_at) BETWEEN ? AND ?"
            : "SELECT COALESCE(SUM(total), 0) as total FROM transaksi " +
              "WHERE status_transaksi = 'selesai' AND DATE(created_at) BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (idKasir != null) {
                stmt.setLong(1, idKasir);
                stmt.setDate(2, startDate);
                stmt.setDate(3, endDate);
            } else {
                stmt.setDate(1, startDate);
                stmt.setDate(2, endDate);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }
    
    // Get profit/loss report
    public Long getLabaRugi(Date startDate, Date endDate) {
        String sql = "SELECT COALESCE(SUM(dt.jumlah * (dt.harga_satuan - p.harga_pokok)), 0) as laba " +
                     "FROM detail_transaksi dt " +
                     "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                     "JOIN produk p ON dt.id_produk = p.id_produk " +
                     "WHERE t.status_transaksi = 'selesai' AND DATE(t.created_at) BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("laba");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }
    
    // Get transaction count
    public int getJumlahTransaksi(Long idKasir, Date startDate, Date endDate) {
        String sql = idKasir != null 
            ? "SELECT COUNT(*) as jumlah FROM transaksi " +
              "WHERE id_kasir = ? AND status_transaksi = 'selesai' " +
              "AND DATE(created_at) BETWEEN ? AND ?"
            : "SELECT COUNT(*) as jumlah FROM transaksi " +
              "WHERE status_transaksi = 'selesai' AND DATE(created_at) BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (idKasir != null) {
                stmt.setLong(1, idKasir);
                stmt.setDate(2, startDate);
                stmt.setDate(3, endDate);
            } else {
                stmt.setDate(1, startDate);
                stmt.setDate(2, endDate);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("jumlah");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Helper method to map ResultSet to Transaksi
    private Transaksi mapResultSetToTransaksi(ResultSet rs) throws SQLException {
        Transaksi transaksi = new Transaksi();
        transaksi.setIdTransaksi(rs.getLong("id_transaksi"));
        transaksi.setIdCustomer(rs.getLong("id_customer"));
        transaksi.setIdKasir(rs.getLong("id_kasir"));
        transaksi.setKodeTransaksi(rs.getString("kode_transaksi"));
        transaksi.setTotal(rs.getLong("total"));
        transaksi.setMetodePembayaran(rs.getString("metode_pembayaran"));
        transaksi.setStatusTransaksi(rs.getString("status_transaksi"));
        transaksi.setNamaCustomer(rs.getString("nama_customer"));
        transaksi.setNamaKasir(rs.getString("nama_kasir"));
        transaksi.setCreatedAt(rs.getTimestamp("created_at"));
        return transaksi;
    }
    
    
    public boolean updateStatusTransaksi(Long idTransaksi, String status) {
        String sql = "UPDATE transaksi SET status_transaksi = ?, updated_at = NOW() WHERE id_transaksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, idTransaksi);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
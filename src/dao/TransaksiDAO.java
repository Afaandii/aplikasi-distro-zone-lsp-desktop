package dao;

import config.DatabaseConnection;
import java.math.BigDecimal;
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
            
            if (transaksi.getIdCustomer() == null) {
                stmt.setNull(1, Types.BIGINT);
            } else {
                stmt.setLong(1, transaksi.getIdCustomer());
            }
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
    
    public List<Transaksi> getTransaksiByKasir(Long idKasir, Date startDate, Date endDate) {
        return getTransaksiByKasir(idKasir, startDate, endDate, "Semua");
    }
    
    // Get transactions by kasir with date filter
    public List<Transaksi> getTransaksiByKasir(Long idKasir, Date startDate, Date endDate, String metodePembayaran) {
        List<Transaksi> transaksiList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, uc.nama as nama_customer, uk.nama as nama_kasir " +
            "FROM transaksi t " +
            "LEFT JOIN users uc ON t.id_customer = uc.id_user " +
            "LEFT JOIN users uk ON t.id_kasir = uk.id_user " +
            "WHERE t.id_kasir = ? AND DATE(t.created_at) BETWEEN ? AND ? "
        );

        if (!"Semua".equals(metodePembayaran)) {
            sql.append("AND t.metode_pembayaran = ? ");
        }

        sql.append("ORDER BY t.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setLong(1, idKasir);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);

            if (!"Semua".equals(metodePembayaran)) {
                stmt.setString(4, metodePembayaran);
            }

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
    public Long getTotalPenjualan(Long idKasir, Date startDate, Date endDate, String metodePembayaran) {
        // Bangun SQL dinamis berdasarkan apakah idKasir ada atau tidak
        StringBuilder sql = new StringBuilder(
            "SELECT COALESCE(SUM(total), 0) FROM transaksi " +
            "WHERE status_transaksi = 'selesai' " +
            "AND DATE(created_at) BETWEEN ? AND ? "
        );

        // List untuk menampung parameter agar urutannya tidak berantakan
        List<Object> parameters = new ArrayList<>();

        // JIKA idKasir TIDAK NULL, baru tambahkan filter ke query
        if (idKasir != null) {
            sql.append("AND id_kasir = ? ");
            parameters.add(idKasir);
        }

        if (!"Semua".equals(metodePembayaran)) {
            sql.append("AND metode_pembayaran = ? ");
            parameters.add(metodePembayaran);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameter Tanggal (Index selalu mulai dari 1)
            int paramIndex = 1;
            stmt.setDate(paramIndex++, startDate);
            stmt.setDate(paramIndex++, endDate);

            // Set parameter tambahan (jika ada idKasir atau metodePembayaran)
            for (Object param : parameters) {
                if (param instanceof Long) {
                    stmt.setLong(paramIndex++, (Long) param);
                } else if (param instanceof String) {
                    stmt.setString(paramIndex++, (String) param);
                }
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    // Overload lama
    public Long getTotalPenjualan(Long idKasir, Date startDate, Date endDate) {
        return getTotalPenjualan(idKasir, startDate, endDate, "Semua");
    }
    
    // Get profit/loss report
    public Long getLabaRugi(Date startDate, Date endDate) {
        String sql = """
            SELECT 
                SUM(t.total) AS total_penjualan,
                SUM(dt.jumlah * p.harga_pokok) AS total_hpp
            FROM transaksi t
            JOIN detail_transaksi dt ON t.id_transaksi = dt.id_transaksi
            JOIN produk p ON dt.id_produk = p.id_produk
            WHERE t.status_transaksi = 'selesai'
              AND t.created_at BETWEEN ? AND ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BigDecimal totalPenjualanBD = rs.getObject("total_penjualan", BigDecimal.class);
                BigDecimal totalHPPBD = rs.getObject("total_hpp", BigDecimal.class);

                Long totalPenjualan = (totalPenjualanBD != null) ? totalPenjualanBD.longValue() : 0L;
                Long totalHPP = (totalHPPBD != null) ? totalHPPBD.longValue() : 0L;

                return totalPenjualan - totalHPP;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }
    
    // Get transaction count
    public int getJumlahTransaksi(Long idKasir, Date startDate, Date endDate, String metodePembayaran) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) as jumlah FROM transaksi " +
            "WHERE id_kasir = ? AND status_transaksi = 'selesai' " +
            "AND DATE(created_at) BETWEEN ? AND ? "
        );

        if (!"Semua".equals(metodePembayaran)) {
            sql.append("AND metode_pembayaran = ? ");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setLong(1, idKasir);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);

            int paramIndex = 4;
            if (!"Semua".equals(metodePembayaran)) {
                stmt.setString(paramIndex++, metodePembayaran);
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
    
    public int getJumlahTransaksi(Long idKasir, Date startDate, Date endDate) {
        return getJumlahTransaksi(idKasir, startDate, endDate, "Semua");
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
    
    public List<String> getDistinctKasir() {
        List<String> kasirs = new ArrayList<>();
        String sql = """
            SELECT DISTINCT uk.nama as nama_kasir
            FROM transaksi t
            JOIN users uk ON t.id_kasir = uk.id_user
            ORDER BY uk.nama
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nama = rs.getString("nama_kasir");
                if (nama != null) kasirs.add(nama.trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kasirs;
    }
    
    public List<String> getDistinctMetodePembayaran() {
        List<String> metodes = new ArrayList<>();
        String sql = "SELECT DISTINCT metode_pembayaran FROM transaksi WHERE metode_pembayaran IS NOT NULL AND metode_pembayaran != '' ORDER BY metode_pembayaran";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String metode = rs.getString("metode_pembayaran");
                if (metode != null) metodes.add(metode.trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metodes;
    }
}
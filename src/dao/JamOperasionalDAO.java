package dao;

import config.DatabaseConnection;
import model.JamOperasional;

import java.sql.*;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JamOperasionalDAO {

    // Check if app can be accessed now (for 'web')
    public boolean isOperasionalNow() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        String hari = getDayName(dayOfWeek);
        LocalTime currentTime = now.toLocalTime();

        String sql = "SELECT jam_buka, jam_tutup, status FROM jam_operasional " +
                     "WHERE LOWER(hari) = LOWER(?) AND tipe_layanan = 'web'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hari);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                if (!"buka".equalsIgnoreCase(status)) {
                    return false;
                }

                Time jamBuka = rs.getTime("jam_buka");
                Time jamTutup = rs.getTime("jam_tutup");

                LocalTime openTime = jamBuka.toLocalTime();
                LocalTime closeTime = jamTutup.toLocalTime();

                return !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get operational info for today (for 'web')
    public String getOperasionalInfo() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        String hari = getDayName(dayOfWeek);

        String sql = "SELECT jam_buka, jam_tutup, status FROM jam_operasional " +
                     "WHERE LOWER(hari) = LOWER(?) AND tipe_layanan = 'web'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hari);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                Time jamBuka = rs.getTime("jam_buka");
                Time jamTutup = rs.getTime("jam_tutup");

                if ("tutup".equalsIgnoreCase(status)) {
                    return "Toko tutup hari ini (" + hari + ")";
                }

                return String.format("Jam Operasional: %s - %s",
                    jamBuka.toString().substring(0, 5),
                    jamTutup.toString().substring(0, 5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Jam operasional tidak tersedia";
    }

    // Update operational hours (dengan tipe_layanan)
    public boolean updateJamOperasional(String tipeLayanan, String hari, Time jamBuka, Time jamTutup, String status) {
        String sql = "UPDATE jam_operasional SET jam_buka = ?, jam_tutup = ?, status = ?, updated_at = NOW() " +
                     "WHERE LOWER(hari) = LOWER(?) AND tipe_layanan = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTime(1, jamBuka);
            stmt.setTime(2, jamTutup);
            stmt.setString(3, status);
            stmt.setString(4, hari);
            stmt.setString(5, tipeLayanan);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get ALL operational hours (semua tipe_layanan)
    public List<JamOperasional> getAllJamOperasional() {
        List<JamOperasional> list = new ArrayList<>();
        String sql = "SELECT * FROM jam_operasional ORDER BY " +
                     "tipe_layanan, " +
                     "CASE hari " +
                     "WHEN 'Senin' THEN 1 " +
                     "WHEN 'Selasa' THEN 2 " +
                     "WHEN 'Rabu' THEN 3 " +
                     "WHEN 'Kamis' THEN 4 " +
                     "WHEN 'Jumat' THEN 5 " +
                     "WHEN 'Sabtu' THEN 6 " +
                     "WHEN 'Minggu' THEN 7 END";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JamOperasional jam = new JamOperasional();
                jam.setIdJamOperasional(rs.getLong("id_jam_operasional"));
                jam.setTipeLayanan(rs.getString("tipe_layanan"));
                jam.setHari(rs.getString("hari"));
                jam.setJamBuka(rs.getTime("jam_buka"));
                jam.setJamTutup(rs.getTime("jam_tutup"));
                jam.setStatus(rs.getString("status"));
                jam.setCreatedAt(rs.getTimestamp("created_at"));
                jam.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(jam);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper method to get day name in Indonesian
    private String getDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Senin";
            case TUESDAY: return "Selasa";
            case WEDNESDAY: return "Rabu";
            case THURSDAY: return "Kamis";
            case FRIDAY: return "Jumat";
            case SATURDAY: return "Sabtu";
            case SUNDAY: return "Minggu";
            default: return "";
        }
    }
    
    /**
    * Cek apakah toko buka untuk layanan offline (desktop) — ambil dari DB
    */
    public boolean isJamOperasionalOffline() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        String hari = getDayName(dayOfWeek);
        LocalTime currentTime = now.toLocalTime();

        String sql = "SELECT jam_buka, jam_tutup, status FROM jam_operasional " +
                     "WHERE LOWER(hari) = LOWER(?) AND tipe_layanan = 'store'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hari);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                if (!"buka".equalsIgnoreCase(status)) {
                    return false;
                }

                Time jamBuka = rs.getTime("jam_buka");
                Time jamTutup = rs.getTime("jam_tutup");

                LocalTime openTime = jamBuka.toLocalTime();
                LocalTime closeTime = jamTutup.toLocalTime();

                return !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default: tutup
    }
    
    /**
    * Cek jam operasional offline + kembalikan pesan alasan
    */
    public String checkJamOperasionalOfflineMessage() {
     LocalDateTime now = LocalDateTime.now();
     DayOfWeek dayOfWeek = now.getDayOfWeek();
     String hari = getDayName(dayOfWeek);
     LocalTime currentTime = now.toLocalTime();

     String sql = "SELECT jam_buka, jam_tutup, status FROM jam_operasional " +
                  "WHERE LOWER(hari) = LOWER(?) AND tipe_layanan = 'store'";

     try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {

         stmt.setString(1, hari);
         ResultSet rs = stmt.executeQuery();

         if (rs.next()) {
             String status = rs.getString("status");
             Time jamBuka = rs.getTime("jam_buka");
             Time jamTutup = rs.getTime("jam_tutup");

             // 🔴 KHUSUS HARI LIBUR (Senin)
             if ("libur".equalsIgnoreCase(status)) {
                 return "Mohon Maaf, toko hari ini sedang libur!\nSilakan kembali esok hari.";
             }

             LocalTime openTime = jamBuka.toLocalTime();
             LocalTime closeTime = jamTutup.toLocalTime();

             // 🔴 DI LUAR JAM OPERASIONAL
             if ("tutup".equalsIgnoreCase(status) || currentTime.isBefore(openTime) || currentTime.isAfter(closeTime)) {
                 return "Mohon Maaf, toko tidak beroperasi di luar jam operasional.\n" +
                        "Jam buka: 10:00 - 20:00";
             }

             // ✅ TOKO BUKA
             return null;
         }

     } catch (SQLException e) {
         e.printStackTrace();
         return "Gagal memeriksa jam operasional toko.";
     }

     return "Jam operasional toko belum diatur.";
    }
    
    /**
    * Ambil status toko OFFLINE (store) HARI INI dari database
    * @return "BUKA", "TUTUP", atau "LIBUR"
    */
   public String getStatusTokoOfflineHariIni() {
       LocalDateTime now = LocalDateTime.now();
       DayOfWeek dayOfWeek = now.getDayOfWeek();
       String hari = getDayName(dayOfWeek);

       String sql = "SELECT status FROM jam_operasional " +
                    "WHERE LOWER(hari) = LOWER(?) AND tipe_layanan = 'store'";

       try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

           stmt.setString(1, hari);
           ResultSet rs = stmt.executeQuery();

           if (rs.next()) {
               return rs.getString("status").toUpperCase();
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }

       return "TUTUP"; // fallback aman
   }

}
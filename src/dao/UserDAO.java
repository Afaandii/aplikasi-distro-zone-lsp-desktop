package dao;

import config.DatabaseConnection;
import config.SupabaseStorageConfig;
import java.io.File;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    // Login
    public User login(String username, String password) {
        String sql = "SELECT u.*, r.nama_role FROM users u " +
                     "JOIN roles r ON u.id_role = r.id_role " +
                     "WHERE u.username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    User user = new User();
                    user.setIdUser(rs.getLong("id_user"));
                    user.setIdRole(rs.getLong("id_role"));
                    user.setNama(rs.getString("nama"));
                    user.setUsername(rs.getString("username"));
                    user.setNik(rs.getString("nik"));
                    user.setAlamat(rs.getString("alamat"));
                    user.setKota(rs.getString("kota"));
                    user.setNoTelp(rs.getString("no_telp"));
                    user.setFotoProfile(rs.getString("foto_profile"));
                    user.setNamaRole(rs.getString("nama_role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Get all users with search
    public List<User> getAllUsers(String searchTerm) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.nama_role FROM users u " +
                     "JOIN roles r ON u.id_role = r.id_role " +
                     "WHERE LOWER(u.nama) LIKE ? OR LOWER(u.username) LIKE ? " +
                     "OR LOWER(u.nik) LIKE ? ORDER BY u.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String search = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getLong("id_user"));
                user.setIdRole(rs.getLong("id_role"));
                user.setNama(rs.getString("nama"));
                user.setUsername(rs.getString("username"));
                user.setNik(rs.getString("nik"));
                user.setAlamat(rs.getString("alamat"));
                user.setKota(rs.getString("kota"));
                user.setNoTelp(rs.getString("no_telp"));
                user.setFotoProfile(rs.getString("foto_profile"));
                user.setNamaRole(rs.getString("nama_role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    // Ubah method createUser menjadi:
    public boolean createUser(User user, File photoFile) {
        String sql = "INSERT INTO users (id_role, nama, username, password, nik, alamat, kota, no_telp, foto_profile, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, user.getIdRole());
            stmt.setString(2, user.getNama());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            stmt.setString(5, user.getNik());
            stmt.setString(6, user.getAlamat());
            stmt.setString(7, user.getKota());
            stmt.setString(8, user.getNoTelp());

            // Handle foto profile
            String fotoUrl = null;
            if (photoFile != null) {
                fotoUrl = uploadProfilePhoto(photoFile);
            }
            if (fotoUrl == null) {
                fotoUrl = "default.jpg"; // Default jika upload gagal atau tidak ada
            }
            stmt.setString(9, fotoUrl);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update user
    public boolean updateUser(User user, File photoFile) {
        String sql = "UPDATE users SET id_role = ?, nama = ?, username = ?, nik = ?, " +
                     "alamat = ?, kota = ?, no_telp = ?, foto_profile = ?, updated_at = NOW() " +
                     "WHERE id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, user.getIdRole());
            stmt.setString(2, user.getNama());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getNik());
            stmt.setString(5, user.getAlamat());
            stmt.setString(6, user.getKota());
            stmt.setString(7, user.getNoTelp());

            // Handle foto profile
            String fotoUrl = user.getFotoProfile(); // default: jangan ubah jika tidak upload
            if (photoFile != null) {
                String newUrl = uploadProfilePhoto(photoFile);
                if (newUrl != null) {
                    fotoUrl = newUrl;
                }
            }
            stmt.setString(8, fotoUrl);

            stmt.setLong(9, user.getIdUser());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update password
    public boolean updatePassword(Long idUser, String newPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = NOW() WHERE id_user = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            stmt.setLong(2, idUser);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Check username exists
    public boolean isUsernameExists(String username, Long excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND id_user != ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setLong(2, excludeUserId != null ? excludeUserId : 0);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
    * Upload foto profil ke Supabase Storage dan kembalikan URL public
    * @param file File gambar
    * @return URL public jika sukses, null jika gagal
    */
   public String uploadProfilePhoto(File file) {
       // Ambil ekstensi asli
        String originalName = file.getName();
        String extension = "";
        int lastDotIndex = originalName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalName.substring(lastDotIndex).toLowerCase();
        }

        // Generate nama aman (tanpa spasi, tanda kurung, dll.)
        String safeName = "user_" + System.currentTimeMillis() + "_" + 
            originalName.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("_+", "_");

        // Pastikan ekstensi tetap ada
        if (!extension.isEmpty() && !safeName.endsWith(extension)) {
            safeName += extension;
        }

        String filePath = "user/" + safeName;

       try {
           // Gunakan SupabaseStorageUtil yang sudah kamu buat
           if (SupabaseStorageConfig.uploadFile(file, filePath)) {
               // Return URL public
               return SupabaseStorageConfig.getPublicUrl(filePath);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
   }
   
   // Tambahkan di dalam UserDAO.java
    public User getUserById(Long idUser) {
        String sql = "SELECT u.*, r.nama_role FROM users u " +
                     "JOIN roles r ON u.id_role = r.id_role " +
                     "WHERE u.id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idUser);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getLong("id_user"));
                user.setIdRole(rs.getLong("id_role"));
                user.setNama(rs.getString("nama"));
                user.setUsername(rs.getString("username"));
                user.setNik(rs.getString("nik"));
                user.setAlamat(rs.getString("alamat"));
                user.setKota(rs.getString("kota"));
                user.setNoTelp(rs.getString("no_telp"));
                user.setFotoProfile(rs.getString("foto_profile"));
                user.setNamaRole(rs.getString("nama_role"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    // Ganti method deleteUser yang lama dengan ini
    public boolean deleteUser(Long idUser) {
        // 1. Ambil user untuk dapatkan foto_profile
        User user = getUserById(idUser);
        if (user == null) {
            return false;
        }

        // 2. Hapus file di Supabase (jika bukan default)
        String fotoUrl = user.getFotoProfile();
        if (fotoUrl != null && !fotoUrl.equals("default.jpg")) {
            try {
                // Ekstrak path dari URL
                String filePath = SupabaseStorageConfig.extractFilePath(fotoUrl);
                if (filePath != null) {
                    SupabaseStorageConfig.deleteFile(filePath);
                }
                SupabaseStorageConfig.deleteFile(filePath);
            } catch (Exception e) {
                System.err.println("Gagal menghapus file dari Supabase: " + e.getMessage());
                // Lanjutkan hapus user meskipun gagal hapus file
            }
        }

        // 3. Hapus dari database
        String sql = "DELETE FROM users WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idUser);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
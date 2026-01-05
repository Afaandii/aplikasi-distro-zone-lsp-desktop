package dao;

import config.DatabaseConnection;
import config.SupabaseStorageConfig;
import java.io.File;
import model.FotoProduk;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FotoProdukDAO {
    
    // Get all foto produk with joins
    public List<FotoProduk> getAllFotoProduk() {
        List<FotoProduk> fotoList = new ArrayList<>();
        String sql = "SELECT fp.*, p.nama_kaos, w.nama_warna " +
                     "FROM foto_produk fp " +
                     "JOIN produk p ON fp.id_produk = p.id_produk " +
                     "LEFT JOIN warna w ON fp.id_warna = w.id_warna " +
                     "ORDER BY fp.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FotoProduk foto = new FotoProduk();
                foto.setIdFotoProduk(rs.getLong("id_foto_produk"));
                foto.setIdProduk(rs.getLong("id_produk"));
                foto.setIdWarna(rs.getLong("id_warna"));
                foto.setUrlFoto(rs.getString("url_foto"));
                foto.setNamaProduk(rs.getString("nama_kaos"));
                foto.setNamaWarna(rs.getString("nama_warna"));
                foto.setCreatedAt(rs.getTimestamp("created_at"));
                foto.setUpdatedAt(rs.getTimestamp("updated_at"));
                fotoList.add(foto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fotoList;
    }
    
    // Get foto by produk
    public List<FotoProduk> getFotoByProduk(Long idProduk) {
        List<FotoProduk> fotoList = new ArrayList<>();
        String sql = "SELECT fp.*, p.nama_kaos, w.nama_warna " +
                     "FROM foto_produk fp " +
                     "JOIN produk p ON fp.id_produk = p.id_produk " +
                     "LEFT JOIN warna w ON fp.id_warna = w.id_warna " +
                     "WHERE fp.id_produk = ? " +
                     "ORDER BY fp.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idProduk);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FotoProduk foto = new FotoProduk();
                foto.setIdFotoProduk(rs.getLong("id_foto_produk"));
                foto.setIdProduk(rs.getLong("id_produk"));
                foto.setIdWarna(rs.getLong("id_warna"));
                foto.setUrlFoto(rs.getString("url_foto"));
                foto.setNamaProduk(rs.getString("nama_kaos"));
                foto.setNamaWarna(rs.getString("nama_warna"));
                foto.setCreatedAt(rs.getTimestamp("created_at"));
                fotoList.add(foto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fotoList;
    }
    
    // Search foto produk
    public List<FotoProduk> searchFotoProduk(String keyword) {
        List<FotoProduk> fotoList = new ArrayList<>();
        String sql = "SELECT fp.*, p.nama_kaos, w.nama_warna " +
                     "FROM foto_produk fp " +
                     "JOIN produk p ON fp.id_produk = p.id_produk " +
                     "LEFT JOIN warna w ON fp.id_warna = w.id_warna " +
                     "WHERE LOWER(p.nama_kaos) LIKE ? OR LOWER(w.nama_warna) LIKE ? " +
                     "ORDER BY fp.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String search = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FotoProduk foto = new FotoProduk();
                foto.setIdFotoProduk(rs.getLong("id_foto_produk"));
                foto.setIdProduk(rs.getLong("id_produk"));
                foto.setIdWarna(rs.getLong("id_warna"));
                foto.setUrlFoto(rs.getString("url_foto"));
                foto.setNamaProduk(rs.getString("nama_kaos"));
                foto.setNamaWarna(rs.getString("nama_warna"));
                foto.setCreatedAt(rs.getTimestamp("created_at"));
                fotoList.add(foto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fotoList;
    }
    
    // Ubah parameter: tambahkan File photoFile
    public boolean createFotoProduk(FotoProduk foto, File photoFile) {
        String sql = "INSERT INTO foto_produk (id_produk, id_warna, url_foto, created_at) " +
                     "VALUES (?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, foto.getIdProduk());
            stmt.setLong(2, foto.getIdWarna());

            // Handle foto
            String urlFoto = null;
            if (photoFile != null) {
                urlFoto = uploadFotoProduk(photoFile, foto.getIdProduk());
            }
            if (urlFoto == null) {
                urlFoto = ""; // atau default placeholder
            }
            stmt.setString(3, urlFoto);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update foto produk
    // Ubah parameter: tambahkan File photoFile
    public boolean updateFotoProduk(FotoProduk foto, File photoFile) {
        String sql = "UPDATE foto_produk SET id_produk = ?, id_warna = ?, url_foto = ?, updated_at = NOW() " +
                     "WHERE id_foto_produk = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, foto.getIdProduk());
            stmt.setLong(2, foto.getIdWarna());

            // Handle foto
            String urlFoto = foto.getUrlFoto(); // default: jangan ubah jika tidak upload
            if (photoFile != null) {
                String newUrl = uploadFotoProduk(photoFile, foto.getIdProduk());
                if (newUrl != null) {
                    urlFoto = newUrl;
                }
            }
            stmt.setString(3, urlFoto);

            stmt.setLong(4, foto.getIdFotoProduk());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete foto produk
    public boolean deleteFotoProduk(Long idFotoProduk) {
    // 1. Ambil data foto untuk dapatkan url_foto
        FotoProduk foto = getFotoById(idFotoProduk);
        if (foto == null) {
            return false;
        }

        // 2. Hapus file di Supabase (jika ada)
        String urlFoto = foto.getUrlFoto();
        if (urlFoto != null && !urlFoto.trim().isEmpty()) {
            try {
                // Ekstrak path dari URL
                String filePath = SupabaseStorageConfig.extractFilePath(urlFoto);
                if (filePath != null) {
                    SupabaseStorageConfig.deleteFotoProduk(filePath);
                }
            } catch (Exception e) {
                System.err.println("Gagal menghapus file dari Supabase: " + e.getMessage());
            }
        }

        // 3. Hapus dari database
        String sql = "DELETE FROM foto_produk WHERE id_foto_produk = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idFotoProduk);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get foto by ID
    public FotoProduk getFotoById(Long idFotoProduk) {
        String sql = "SELECT fp.*, p.nama_kaos, w.nama_warna " +
                     "FROM foto_produk fp " +
                     "JOIN produk p ON fp.id_produk = p.id_produk " +
                     "LEFT JOIN warna w ON fp.id_warna = w.id_warna " +
                     "WHERE fp.id_foto_produk = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idFotoProduk);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                FotoProduk foto = new FotoProduk();
                foto.setIdFotoProduk(rs.getLong("id_foto_produk"));
                foto.setIdProduk(rs.getLong("id_produk"));
                foto.setIdWarna(rs.getLong("id_warna"));
                foto.setUrlFoto(rs.getString("url_foto"));
                foto.setNamaProduk(rs.getString("nama_kaos"));
                foto.setNamaWarna(rs.getString("nama_warna"));
                foto.setCreatedAt(rs.getTimestamp("created_at"));
                return foto;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
    * Upload foto produk ke Supabase Storage dan kembalikan URL public
    * @param file File gambar
    * @param idProduk ID produk (untuk nama file)
    * @return URL public jika sukses, null jika gagal
    */
   public String uploadFotoProduk(File file, Long idProduk) {
       // Ambil ekstensi asli
       String originalName = file.getName();
       String extension = "";
       int lastDotIndex = originalName.lastIndexOf('.');
       if (lastDotIndex > 0) {
           extension = originalName.substring(lastDotIndex).toLowerCase();
       }

       // Generate nama aman
       String safeName = "produk_" + idProduk + "_" + 
           System.currentTimeMillis() + "_" + 
           originalName.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("_+", "_");

       // Pastikan ekstensi tetap ada
       if (!extension.isEmpty() && !safeName.endsWith(extension)) {
           safeName += extension;
       }

       // ✅ Simpan di folder "foto-produk/"
       String filePath = "foto-produk/" + safeName;

       try {
           if (SupabaseStorageConfig.uploadFotoProduk(file, filePath)) {
               return SupabaseStorageConfig.getPublicUrlFotoProduk(filePath);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
   }
}
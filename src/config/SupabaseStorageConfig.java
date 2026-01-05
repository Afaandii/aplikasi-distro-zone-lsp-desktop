package config;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.http.client.methods.HttpDelete;

public class SupabaseStorageConfig {

    private static final String SUPABASE_URL = "https://cvjzktqyesjuopsfbdcl.supabase.co"; // Ganti dengan URL-mu
    private static final String SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImN2anprdHF5ZXNqdW9wc2ZiZGNsIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NTQxOTg2OSwiZXhwIjoyMDgwOTk1ODY5fQ.Cq0n3lyvhKLqAZgoW-qGI2uUw3ovaJEIQ5eEDa1P1Cg";
    private static final String BUCKET_NAME = "user-profile";
    
    
    private static String sanitizeFileName(String originalName) {
        String ext = "";

        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex != -1) {
            ext = originalName.substring(dotIndex);
        }

        return "user_" + System.currentTimeMillis() + ext;
    }


    /**
     * Upload file ke Supabase Storage
     * @param file File yang akan diupload
     * @param filePath Path di dalam bucket (misal: "user/123.jpg")
     * @return true jika sukses
     */
    public static boolean uploadFile(File file, String filePath) {
        String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost uploadFile = new HttpPost(url);
            uploadFile.setHeader("apikey", SERVICE_ROLE_KEY);
            uploadFile.setHeader("Authorization", "Bearer " + SERVICE_ROLE_KEY);

            String mimeType = Files.probeContentType(file.toPath());
                if (mimeType == null) {
                    mimeType = "image/jpeg";
                }

                HttpEntity multipartEntity = MultipartEntityBuilder.create()
                    .addBinaryBody(
                        "file",
                        file,
                        ContentType.create(mimeType),
                        file.getName()
                    )
                    .build();


            uploadFile.setEntity(multipartEntity);

            HttpResponse response = httpClient.execute(uploadFile);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                System.out.println("✅ Upload sukses: " + filePath);
                return true;
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.err.println("❌ Upload gagal (" + statusCode + "): " + responseBody);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Dapatkan URL public dari file
     * @param filePath Path di dalam bucket (misal: "user/123.jpg")
     * @return URL public
     */
    public static String getPublicUrl(String filePath) {
        try {
            // Encode path agar aman
            String encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
                .replace("+", "%20") // jangan ubah spasi jadi +
                .replace("%2F", "/"); // jangan encode slash
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + encodedPath;
        } catch (Exception e) {
            e.printStackTrace();
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + filePath;
        }
    }
    
    // Tambahkan di SupabaseStorageConfig.java
    public static boolean deleteFile(String filePath) {
        String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete deleteReq = new HttpDelete(url);
            deleteReq.setHeader("apikey", SERVICE_ROLE_KEY);
            deleteReq.setHeader("Authorization", "Bearer " + SERVICE_ROLE_KEY);

            HttpResponse response = httpClient.execute(deleteReq);
            int statusCode = response.getStatusLine().getStatusCode();

            return statusCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Tambahkan di SupabaseStorageConfig.java
    public static String extractFilePath(String publicUrl) {
        String prefix = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/";
        if (publicUrl.startsWith(prefix)) {
            return publicUrl.substring(prefix.length());
        }
        return null;
    }
    
    
    /**
    * Upload foto produk ke Supabase Storage
    * @param file File gambar
    * @param filePath Path di dalam bucket (misal: "foto-produk/123.jpg")
    * @return true jika sukses
    */
   public static boolean uploadFotoProduk(File file, String filePath) {
       String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

       try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
           HttpPost uploadFile = new HttpPost(url);
           uploadFile.setHeader("apikey", SERVICE_ROLE_KEY);
           uploadFile.setHeader("Authorization", "Bearer " + SERVICE_ROLE_KEY);

           // Deteksi MIME type
           String mimeType = "image/jpeg";
           String fileName = file.getName().toLowerCase();
           if (fileName.endsWith(".png")) {
               mimeType = "image/png";
           } else if (fileName.endsWith(".gif")) {
               mimeType = "image/gif";
           } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
               mimeType = "image/jpeg";
           }

           HttpEntity multipartEntity = MultipartEntityBuilder.create()
               .addBinaryBody("file", file, ContentType.parse(mimeType), filePath)
               .build();

           uploadFile.setEntity(multipartEntity);

           HttpResponse response = httpClient.execute(uploadFile);
           int statusCode = response.getStatusLine().getStatusCode();

           if (statusCode == 200) {
               System.out.println("✅ Upload sukses: " + filePath);
               return true;
           } else {
               String responseBody = EntityUtils.toString(response.getEntity());
               System.err.println("❌ Upload gagal (" + statusCode + "): " + responseBody);
               return false;
           }

       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }

    /**
     * Dapatkan URL public dari file foto produk
     * @param filePath Path di dalam bucket (misal: "foto-produk/123.jpg")
     * @return URL public
     */
    public static String getPublicUrlFotoProduk(String filePath) {
        try {
            String encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
                .replace("+", "%20")
                .replace("%2F", "/");
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + encodedPath;
        } catch (Exception e) {
            e.printStackTrace();
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + filePath;
        }
    }
    
    // Helper method: Ekstrak path dari URL
    private String extractFilePathFromUrl(String publicUrl) {
        String prefix = SupabaseStorageConfig.SUPABASE_URL + "/storage/v1/object/public/" + SupabaseStorageConfig.BUCKET_NAME + "/";
        if (publicUrl != null && publicUrl.startsWith(prefix)) {
            return publicUrl.substring(prefix.length());
        }
        return null;
    }
    
    /**
    * Hapus file foto produk dari Supabase Storage
    * @param filePath Path di dalam bucket (misal: "foto-produk/123.jpg")
    * @return true jika sukses
    */
   public static boolean deleteFotoProduk(String filePath) {
       String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

       try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
           HttpDelete deleteReq = new HttpDelete(url);
           deleteReq.setHeader("apikey", SERVICE_ROLE_KEY);
           deleteReq.setHeader("Authorization", "Bearer " + SERVICE_ROLE_KEY);

           HttpResponse response = httpClient.execute(deleteReq);
           int statusCode = response.getStatusLine().getStatusCode();

           return statusCode == 200;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }
}
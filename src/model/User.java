package model;

import java.sql.Timestamp;

public class User {
    private Long idUser;
    private Long idRole;
    private String nama;
    private String username;
    private String password;
    private String nik;
    private String alamat;
    private String kota;
    private String noTelp;
    private String fotoProfile;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Role info
    private String namaRole;
    
    // Constructors
    public User() {}
    
    public User(Long idUser, Long idRole, String nama, String username, String nik, 
                String alamat, String kota, String noTelp, String fotoProfile, String namaRole) {
        this.idUser = idUser;
        this.idRole = idRole;
        this.nama = nama;
        this.username = username;
        this.nik = nik;
        this.alamat = alamat;
        this.kota = kota;
        this.noTelp = noTelp;
        this.fotoProfile = fotoProfile;
        this.namaRole = namaRole;
    }
    
    // Getters and Setters
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }
    
    public Long getIdRole() { return idRole; }
    public void setIdRole(Long idRole) { this.idRole = idRole; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }
    
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    
    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }
    
    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }
    
    public String getFotoProfile() { return fotoProfile; }
    public void setFotoProfile(String fotoProfile) { this.fotoProfile = fotoProfile; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNamaRole() { return namaRole; }
    public void setNamaRole(String namaRole) { this.namaRole = namaRole; }
}
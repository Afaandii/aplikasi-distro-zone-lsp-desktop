package model;

import java.sql.Timestamp;

public class Merk {
    private Long idMerk;
    private String namaMerk;
    private String keterangan;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    public Merk() {}
    
    public Merk(Long idMerk, String namaMerk, String keterangan) {
        this.idMerk = idMerk;
        this.namaMerk = namaMerk;
        this.keterangan = keterangan;
    }
    
    // Getters and Setters
    public Long getIdMerk() { return idMerk; }
    public void setIdMerk(Long idMerk) { this.idMerk = idMerk; }
    
    public String getNamaMerk() { return namaMerk; }
    public void setNamaMerk(String namaMerk) { this.namaMerk = namaMerk; }
    
    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return namaMerk;
    }
}
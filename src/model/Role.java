package model;

import java.sql.Timestamp;

public class Role {
    private Long idRole;
    private String namaRole;
    private String keterangan;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    public Role() {}
    
    public Long getIdRole() { return idRole; }
    public void setIdRole(Long idRole) { this.idRole = idRole; }
    
    public String getNamaRole() { return namaRole; }
    public void setNamaRole(String namaRole) { this.namaRole = namaRole; }
    
    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return namaRole;
    }
}
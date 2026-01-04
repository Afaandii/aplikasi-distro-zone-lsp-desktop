package model;

import java.sql.Time;
import java.sql.Timestamp;

public class JamOperasional {
    private Long idJamOperasional;
    private String tipeLayanan;
    private String hari;
    private Time jamBuka;
    private Time jamTutup;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    public JamOperasional() {}
    
    public Long getIdJamOperasional() { return idJamOperasional; }
    public void setIdJamOperasional(Long idJamOperasional) { this.idJamOperasional = idJamOperasional; }
    
    public String getTipeLayanan() { return tipeLayanan; }
    public void setTipeLayanan(String tipeLayanan) { this.tipeLayanan = tipeLayanan; }
    
    public String getHari() { return hari; }
    public void setHari(String hari) { this.hari = hari; }
    
    public Time getJamBuka() { return jamBuka; }
    public void setJamBuka(Time jamBuka) { this.jamBuka = jamBuka; }
    
    public Time getJamTutup() { return jamTutup; }
    public void setJamTutup(Time jamTutup) { this.jamTutup = jamTutup; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
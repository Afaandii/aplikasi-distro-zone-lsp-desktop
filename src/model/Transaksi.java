package model;

import java.sql.Timestamp;

public class Transaksi {
    private Long idTransaksi;
    private Long idCustomer;
    private Long idKasir;
    private String kodeTransaksi;
    private Long total;
    private String metodePembayaran;
    private String statusTransaksi;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional info
    private String namaCustomer;
    private String namaKasir;
    
    // Constructors
    public Transaksi() {}
    
    public Transaksi(Long idTransaksi, String kodeTransaksi, Long total, 
                     String metodePembayaran, String statusTransaksi, 
                     String namaCustomer, String namaKasir, Timestamp createdAt) {
        this.idTransaksi = idTransaksi;
        this.kodeTransaksi = kodeTransaksi;
        this.total = total;
        this.metodePembayaran = metodePembayaran;
        this.statusTransaksi = statusTransaksi;
        this.namaCustomer = namaCustomer;
        this.namaKasir = namaKasir;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(Long idTransaksi) { this.idTransaksi = idTransaksi; }
    
    public Long getIdCustomer() { return idCustomer; }
    public void setIdCustomer(Long idCustomer) { this.idCustomer = idCustomer; }
    
    public Long getIdKasir() { return idKasir; }
    public void setIdKasir(Long idKasir) { this.idKasir = idKasir; }
    
    public String getKodeTransaksi() { return kodeTransaksi; }
    public void setKodeTransaksi(String kodeTransaksi) { this.kodeTransaksi = kodeTransaksi; }
    
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    
    public String getStatusTransaksi() { return statusTransaksi; }
    public void setStatusTransaksi(String statusTransaksi) { this.statusTransaksi = statusTransaksi; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNamaCustomer() { return namaCustomer; }
    public void setNamaCustomer(String namaCustomer) { this.namaCustomer = namaCustomer; }
    
    public String getNamaKasir() { return namaKasir; }
    public void setNamaKasir(String namaKasir) { this.namaKasir = namaKasir; }
}
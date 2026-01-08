package model;

public class Warna {
    private Long idWarna;
    private String namaWarna;
    private String keterangan;

    public Warna() {
    }

    public Warna(Long idWarna, String namaWarna, String keterangan) {
        this.idWarna = idWarna;
        this.namaWarna = namaWarna;
        this.keterangan = keterangan;
    }

    public Long getIdWarna() {
        return idWarna;
    }

    public void setIdWarna(Long idWarna) {
        this.idWarna = idWarna;
    }

    public String getNamaWarna() {
        return namaWarna;
    }

    public void setNamaWarna(String namaWarna) {
        this.namaWarna = namaWarna;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    @Override
    public String toString() {
        return namaWarna;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warna warna = (Warna) o;
        return idWarna != null && idWarna.equals(warna.idWarna);
    }

    @Override
    public int hashCode() {
        return idWarna != null ? idWarna.hashCode() : 0;
    }
}

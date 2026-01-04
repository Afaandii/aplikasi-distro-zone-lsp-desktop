package model;

public class Ukuran {
    private Long idUkuran;
    private String namaUkuran;
    private String keterangan;

    public Ukuran() {
    }

    public Ukuran(Long idUkuran, String namaUkuran, String keterangan) {
        this.idUkuran = idUkuran;
        this.namaUkuran = namaUkuran;
        this.keterangan = keterangan;
    }

    public Long getIdUkuran() {
        return idUkuran;
    }

    public void setIdUkuran(Long idUkuran) {
        this.idUkuran = idUkuran;
    }

    public String getNamaUkuran() {
        return namaUkuran;
    }

    public void setNamaUkuran(String namaUkuran) {
        this.namaUkuran = namaUkuran;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    @Override
    public String toString() {
        return namaUkuran;
    }
}

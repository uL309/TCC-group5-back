package puc.airtrack.airtrack.tipoMotor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TipoMotorDTO {
        @JsonProperty("id")
    private Integer id;

    @JsonProperty("marca")
    private String marca;

    @JsonProperty("modelo")
    private String modelo;

    @JsonProperty("tbo")
    private int tbo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getTbo() {
        return tbo;
    }

    public void setTbo(int tbo) {
        this.tbo = tbo;
    }
}

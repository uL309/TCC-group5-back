package puc.airtrack.airtrack.Motor;

import java.time.LocalDate;

public class MotorDTO {
    private int id;
    private String marca;
    private LocalDate data_cadastro;
    private Boolean status;

    private String serie_motor;
    private int horas_operacao;
    private String modelo;

    private int tbo;

    private String cliente_cpf;
    private String cliente_nome;

    public String getCliente_nome() {
        return cliente_nome;
    }

    public void setCliente_nome(String cliente_nome) {
        this.cliente_nome = cliente_nome;
    }

    public int getId(){ return id; }



    public void setId(int id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public LocalDate getData_cadastro() {
        return data_cadastro;
    }

    public void setData_cadastro(LocalDate data_cadastro) {
        this.data_cadastro = data_cadastro;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getSerie_motor() {
        return serie_motor;
    }

    public void setSerie_motor(String serie_motor) {
        this.serie_motor = serie_motor;
    }

    public int getHoras_operacao() {
        return horas_operacao;
    }

    public void setHoras_operacao(int horas_operacao) {
        this.horas_operacao = horas_operacao;
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

    public String getCliente_cpf() {
        return cliente_cpf;
    }

    public void setCliente_cpf(String cliente_cpf) {
        this.cliente_cpf = cliente_cpf;
    }

    public void setTbo(int tbo) {
        this.tbo = tbo;
    }

}


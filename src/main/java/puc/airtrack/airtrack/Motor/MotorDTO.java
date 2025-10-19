package puc.airtrack.airtrack.Motor;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto de transferência de dados para Motor de Aeronave")
public class MotorDTO {
    
    @Schema(description = "ID único do motor (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private int id;
    
    @Schema(description = "Marca do fabricante do motor", example = "Pratt & Whitney", required = true)
    private String marca;
    
    @Schema(description = "Data de cadastro do motor no sistema", example = "2025-01-15", required = true)
    private LocalDate data_cadastro;
    
    @Schema(description = "Status operacional do motor (true = ativo, false = inativo)", example = "true", required = true)
    private Boolean status;

    @Schema(description = "Número de série único do motor", example = "PCE-123456", required = true)
    private String serie_motor;
    
    @Schema(description = "Total de horas de operação acumuladas", example = "850", required = true)
    private int horas_operacao;
    
    @Schema(description = "Modelo específico do motor", example = "PT6A-60A", required = true)
    private String modelo;

    @Schema(description = "Time Between Overhaul (TBO) - Horas até próxima revisão geral", example = "3600", required = true)
    private int tbo;

    @Schema(description = "CPF do cliente proprietário do motor", example = "123.456.789-00")
    private String cliente_cpf;
    
    @Schema(description = "Nome do cliente proprietário", example = "Aviação Executiva Ltda")
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


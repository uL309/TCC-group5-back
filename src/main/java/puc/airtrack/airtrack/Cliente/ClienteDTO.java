package puc.airtrack.airtrack.Cliente;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Schema(description = "Dados do Cliente - Empresa ou pessoa física que possui motores de aeronaves cadastrados")
public class ClienteDTO {
    @JsonProperty("id")
    @Schema(description = "ID único do cliente (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @JsonProperty("cpf")
    @Schema(description = "CPF (pessoa física) ou CNPJ (pessoa jurídica) do cliente", example = "123.456.789-00", required = true)
    private String cpf;

    @JsonProperty("name")
    @Schema(description = "Nome completo ou razão social do cliente", example = "Aviação Executiva Ltda", required = true)
    private String name;

    @JsonProperty("email")
    @Schema(description = "E-mail de contato do cliente", example = "contato@aviacaoexecutiva.com.br")
    private String email;

    @JsonProperty("contato")
    @Schema(description = "Telefone de contato do cliente", example = "(11) 98765-4321")
    private String contato;

    @JsonProperty("status")
    @Schema(description = "Status do cliente (true=ativo, false=inativo)", example = "true")
    private Boolean status;
}
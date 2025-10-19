package puc.airtrack.airtrack.Fornecedor;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Schema(description = "Dados do Fornecedor - Empresa que fornece peças e componentes para manutenção de motores")
public class FornecedorDTO {

    @JsonProperty("cnpj")
    @Schema(description = "CNPJ do fornecedor (identificador único)", example = "12.345.678/0001-90", required = true)
    private String id;

    @JsonProperty("name")
    @Schema(description = "Razão social do fornecedor", example = "Parts Supply Aviation", required = true)
    private String name;

    @JsonProperty("email")
    @Schema(description = "E-mail de contato do fornecedor", example = "vendas@partssupply.com.br")
    private String email;

    @JsonProperty("contato")
    @Schema(description = "Telefone de contato do fornecedor", example = "(11) 3456-7890")
    private String contato;

    @JsonProperty("categoria")
    @Schema(description = "Categoria do fornecedor", example = "Peças e Componentes", allowableValues = {"Peças e Componentes", "Serviços", "Ferramentas", "Materiais", "Outros"})
    private String categoria;

    @JsonProperty("status")
    @Schema(description = "Status do fornecedor (true=ativo, false=inativo)", example = "true")
    private Boolean status; 
}

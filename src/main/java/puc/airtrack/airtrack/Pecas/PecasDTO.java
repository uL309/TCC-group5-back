package puc.airtrack.airtrack.Pecas;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Schema(description = "Dados da Peça - Componente ou peça de reposição para manutenção de motores")
public class PecasDTO {
    @Schema(description = "ID único da peça (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private int id;
    
    @JsonProperty("nome")
    @Schema(description = "Nome descritivo da peça", example = "Filtro de Óleo PT6A", required = true)
    private String nome;
    
    @JsonProperty("num_serie")
    @Schema(description = "Número de série ou código da peça", example = "FLT-PT6A-001", required = true)
    private String numSerie;

    @JsonProperty("data_aquisicao")
    @Schema(description = "Data de aquisição da peça", example = "2025-10-15", type = "string", format = "date")
    private LocalDate dataAquisicao;

    @JsonProperty("status")
    @Schema(description = "Status da peça (true=ativa, false=inativa)", example = "true")
    private Boolean status;

    @JsonProperty("categoria")
    @Schema(description = "Categoria da peça", example = "Filtros", allowableValues = {"Filtros", "Rolamentos", "Velas", "Selos", "Componentes Elétricos", "Ferramentas", "Outros"})
    private String categoria;

    @JsonProperty("id_engenheiro")
    @Schema(description = "ID do engenheiro responsável pela peça", example = "1")
    private int id_engenheiro;

    @JsonProperty("valor")
    @Schema(description = "Valor unitário da peça em reais (R$)", example = "450.00")
    private Float valor;

    @JsonProperty("fornecedor")
    @Schema(description = "CNPJ do fornecedor da peça", example = "12.345.678/0001-90")
    private String fornecedorId;

    @JsonProperty("fornecedor_nome")
    @Schema(description = "Nome do fornecedor (somente leitura)", example = "Parts Supply Aviation", accessMode = Schema.AccessMode.READ_ONLY)
    private String fornecedorNome;
}
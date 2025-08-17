package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum OrdemStatus {
    PENDENTE(0),
    ANDAMENTO(1),
    CONCLUIDA(2);

    private final int status;

    OrdemStatus(int status) {
        this.status = status;
    }

    @JsonProperty("status_descricao")
    public String getStatusDescricao() {
        return switch (status) {
            case 0 -> "Pendente";
            case 1 -> "Andamento";
            case 2 -> "Concluída";
            default -> "Desconhecido";
        };
    }
}


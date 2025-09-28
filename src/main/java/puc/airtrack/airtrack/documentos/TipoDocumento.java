package puc.airtrack.airtrack.documentos;

/**
 * Tipos de documentos suportados pelo sistema
 */
public enum TipoDocumento {
    
    /**
     * Manual da Organização de Manutenção
     */
    MANUAL_ORGANIZACAO_MANUTENCAO("Manual da Organização de Manutenção", "MOM"),
    
    /**
     * Manual de Controle da Qualidade
     */
    MANUAL_CONTROLE_QUALIDADE("Manual de Controle da Qualidade", "MCQ");

    private final String descricao;
    private final String sigla;

    TipoDocumento(String descricao, String sigla) {
        this.descricao = descricao;
        this.sigla = sigla;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSigla() {
        return sigla;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
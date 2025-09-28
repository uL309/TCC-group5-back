package puc.airtrack.airtrack.documentos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para resposta de documento
 */
@Data
public class DocumentoResponseDTO {
    
    private Long id;
    private TipoDocumento tipo;
    private String tipoDescricao;
    private String nomeOriginal;
    private Long tamanhoArquivo;
    private String tamanhoFormatado;
    private String tipoConteudo;
    private Integer versao;
    private Boolean ativo;
    private String observacoes;
    private String usuarioUpload;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacao;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    public DocumentoResponseDTO(Documento documento) {
        this.id = documento.getId();
        this.tipo = documento.getTipo();
        this.tipoDescricao = documento.getTipo().getDescricao();
        this.nomeOriginal = documento.getNomeOriginal();
        this.tamanhoArquivo = documento.getTamanhoArquivo();
        this.tamanhoFormatado = formatarTamanhoArquivo(documento.getTamanhoArquivo());
        this.tipoConteudo = documento.getTipoConteudo();
        this.versao = documento.getVersao();
        this.ativo = documento.getAtivo();
        this.observacoes = documento.getObservacoes();
        this.usuarioUpload = documento.getUsuarioUpload();
        this.dataCriacao = documento.getDataCriacao();
        this.dataAtualizacao = documento.getDataAtualizacao();
    }

    private String formatarTamanhoArquivo(Long tamanhoBytes) {
        if (tamanhoBytes == null || tamanhoBytes == 0) {
            return "0 B";
        }
        
        String[] unidades = {"B", "KB", "MB", "GB"};
        double tamanho = tamanhoBytes;
        int unidadeIndex = 0;
        
        while (tamanho >= 1024 && unidadeIndex < unidades.length - 1) {
            tamanho /= 1024;
            unidadeIndex++;
        }
        
        return String.format("%.1f %s", tamanho, unidades[unidadeIndex]);
    }
}
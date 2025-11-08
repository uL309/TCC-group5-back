package puc.airtrack.airtrack.documentos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade para gerenciar documentos do sistema (Manuais)
 */
@Entity
@Getter
@Setter
@Table(name = "Documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @NotNull(message = "Tipo do documento é obrigatório")
    private TipoDocumento tipo;

    @Column(name = "nome_original", nullable = false)
    @NotBlank(message = "Nome original do arquivo é obrigatório")
    private String nomeOriginal;

    @Column(name = "nome_azure", nullable = false)
    @NotBlank(message = "Nome do arquivo no Azure é obrigatório")
    private String nomeAzure;

    @Column(name = "url_azure", nullable = false, length = 1000)
    @NotBlank(message = "URL do Azure é obrigatória")
    private String urlAzure;

    @Column(name = "tamanho_arquivo", nullable = false)
    @NotNull(message = "Tamanho do arquivo é obrigatório")
    private Long tamanhoArquivo;

    @Column(name = "tipo_conteudo")
    private String tipoConteudo;

    @Column(name = "versao", nullable = false)
    @NotNull(message = "Versão é obrigatória")
    private Integer versao = 1;

    @Column(name = "ativo", nullable = false)
    @NotNull
    private Boolean ativo = true;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_upload", nullable = false)
    @NotBlank(message = "Usuário que fez upload é obrigatório")
    private String usuarioUpload;

    /**
     * Construtor padrão
     */
    public Documento() {
    }

    /**
     * Construtor com parâmetros principais
     */
    public Documento(TipoDocumento tipo, String nomeOriginal, String nomeAzure, 
                    String urlAzure, Long tamanhoArquivo, String tipoConteudo, 
                    String usuarioUpload) {
        this.tipo = tipo;
        this.nomeOriginal = nomeOriginal;
        this.nomeAzure = nomeAzure;
        this.urlAzure = urlAzure;
        this.tamanhoArquivo = tamanhoArquivo;
        this.tipoConteudo = tipoConteudo;
        this.usuarioUpload = usuarioUpload;
        this.versao = 1;
        this.ativo = true;
    }

    /**
     * Incrementa a versão do documento
     */
    public void incrementarVersao() {
        this.versao++;
    }

    /**
     * Desativa o documento
     */
    public void desativar() {
        this.ativo = false;
    }

    /**
     * Ativa o documento
     */
    public void ativar() {
        this.ativo = true;
    }

    @Override
    public String toString() {
        return "Documento{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", nomeOriginal='" + nomeOriginal + '\'' +
                ", versao=" + versao +
                ", ativo=" + ativo +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
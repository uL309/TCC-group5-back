package puc.airtrack.airtrack.documentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para operações com documentos
 */
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /**
     * Busca um documento ativo por tipo
     * @param tipo Tipo do documento
     * @return Optional do documento ativo
     */
    Optional<Documento> findByTipoAndAtivoTrue(TipoDocumento tipo);

    /**
     * Busca o documento mais recente (maior versão) ativo por tipo
     * @param tipo Tipo do documento
     * @return Optional do documento mais recente
     */
    @Query("SELECT d FROM Documento d WHERE d.tipo = :tipo AND d.ativo = true ORDER BY d.versao DESC, d.dataCriacao DESC")
    Optional<Documento> findLatestByTipo(@Param("tipo") TipoDocumento tipo);

    /**
     * Verifica se existe um documento ativo de determinado tipo
     * @param tipo Tipo do documento
     * @return true se existe, false caso contrário
     */
    boolean existsByTipoAndAtivoTrue(TipoDocumento tipo);

    /**
     * Desativa todos os documentos de um tipo específico
     * @param tipo Tipo do documento
     * @return Número de documentos desativados
     */
    @Modifying
    @Query("UPDATE Documento d SET d.ativo = false WHERE d.tipo = :tipo AND d.ativo = true")
    int desativarTodosDoTipo(@Param("tipo") TipoDocumento tipo);

    /**
     * Busca todos os documentos de um tipo específico (ativos e inativos)
     * ordenados por versão decrescente
     * @param tipo Tipo do documento
     * @return Lista de documentos ordenada por versão
     */
    @Query("SELECT d FROM Documento d WHERE d.tipo = :tipo ORDER BY d.versao DESC, d.dataCriacao DESC")
    java.util.List<Documento> findAllByTipoOrderByVersaoDesc(@Param("tipo") TipoDocumento tipo);

    /**
     * Busca todos os documentos ativos ordenados por tipo e versão
     * @return Lista de documentos ativos
     */
    @Query("SELECT d FROM Documento d WHERE d.ativo = true ORDER BY d.tipo, d.versao DESC")
    java.util.List<Documento> findAllActiveOrderByTipoAndVersao();

    /**
     * Obtém a próxima versão para um tipo de documento
     * @param tipo Tipo do documento
     * @return Próximo número de versão
     */
    @Query("SELECT COALESCE(MAX(d.versao), 0) + 1 FROM Documento d WHERE d.tipo = :tipo")
    Integer getNextVersion(@Param("tipo") TipoDocumento tipo);

    /**
     * Busca documento pelo nome do arquivo no Azure
     * @param nomeAzure Nome do arquivo no Azure
     * @return Optional do documento
     */
    Optional<Documento> findByNomeAzure(String nomeAzure);

    /**
     * Conta quantos documentos ativos existem por tipo
     * @param tipo Tipo do documento
     * @return Quantidade de documentos ativos
     */
    int countByTipoAndAtivoTrue(TipoDocumento tipo);
}
package puc.airtrack.airtrack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configurações específicas do banco de dados
 */
@Configuration
public class DbConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Configuração personalizada do DataSource para aumentar o tamanho máximo de pacotes
     */
    @Bean
    public DataSource dataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(driverClassName);
        
        // Ajusta URL para permitir pacotes maiores e uso de UTF-8
        if (url.contains("?")) {
            url += "&characterEncoding=utf8&useUnicode=true";
        } else {
            url += "?characterEncoding=utf8&useUnicode=true";
        }
        
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        
        return dataSourceBuilder.build();
    }

    /**
     * Configura o JdbcTemplate para usar o DataSource personalizado
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Aumenta o timeout para operações grandes
        jdbcTemplate.setQueryTimeout(30); // 30 segundos
        
        return jdbcTemplate;
    }
    
    /**
     * Executa SQL para atualizar colunas no banco após inicialização
     */
    @Bean
    public boolean updateLogColumns(JdbcTemplate jdbcTemplate) {
        try {
            // Altera todas as colunas de dados de log para LONGTEXT se ainda não forem
            jdbcTemplate.execute("ALTER TABLE logs_ordem_servico MODIFY COLUMN requestData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_ordem_servico MODIFY COLUMN responseData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_cliente MODIFY COLUMN requestData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_cliente MODIFY COLUMN responseData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_fornecedor MODIFY COLUMN requestData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_fornecedor MODIFY COLUMN responseData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_pecas MODIFY COLUMN requestData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_pecas MODIFY COLUMN responseData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_motor MODIFY COLUMN requestData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_motor MODIFY COLUMN responseData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_user MODIFY COLUMN requestData LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE logs_user MODIFY COLUMN responseData LONGTEXT");
            return true;
        } catch (Exception e) {
            // Ignora erros se as colunas já existirem ou estiverem corretas
            return false;
        }
    }
}

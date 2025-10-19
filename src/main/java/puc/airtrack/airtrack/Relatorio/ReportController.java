package puc.airtrack.airtrack.Relatorio;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/report")
@Tag(name = "Relatórios", description = "Geração de relatórios PDF com dados do sistema")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Para consultar o banco

    @Operation(
        summary = "Exportar relatório em PDF",
        description = "Gera um relatório em PDF com todas as ordens de serviço usando serviço Node.js externo para renderização."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro ao gerar PDF")
    })
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReportAsPdf() {
        try {
            // Consulta os dados da ordem de serviço
            String sql = "SELECT id, data_abertura, tempo_usado, tempo_estimado, status FROM CabecalhoOrdem";
            List<Map<String, Object>> ordens = jdbcTemplate.queryForList(sql);

            // Converter os dados em JSON para enviar para o Node.js
            String jsonDados = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(ordens);

            // Chamar o endpoint Node.js que gera PDF com AI
            String puppeteerUrl = "http://localhost:3001/generate-pdf?data=" + URLEncoder.encode(jsonDados, "UTF-8");
            RestTemplate restTemplate = new RestTemplate();
            byte[] pdfBytes = restTemplate.getForObject(puppeteerUrl, byte[].class);

            // Configurações de resposta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio.pdf");
            System.out.println("PDF gerado com sucesso, tamanho: " + pdfBytes.length + " bytes");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Erro ao gerar PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro ao gerar PDF: " + e.getMessage()).getBytes());
        }
    }
}

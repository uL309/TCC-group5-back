package puc.airtrack.airtrack.OrdemDeServico;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import puc.airtrack.airtrack.services.AzureBlobStorageService;

@RestController
@RequestMapping("/ordem")
public class CabecalhoOrdemController {
    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    private LinhaOrdemService linhaOrdemService;
    @Autowired
    private CabecalhoOrdemService cabecalhoOrdemService;
    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @PostMapping("/create")
    public ResponseEntity<String> createCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.createCabecalho(dto);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.updateCabecalho(dto);
    }

    @GetMapping("/get")
    public ResponseEntity<CabecalhoOrdemDTO> getCabecalho(@RequestParam int id) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(id);
        if (opt.isPresent()) {
            CabecalhoOrdem entity = opt.get();
            CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
            dto.setId(entity.getId());
            dto.setDataAbertura(entity.getDataAbertura());
            dto.setDataFechamento(entity.getDataFechamento());
            dto.setDescricao(entity.getDescricao());
            dto.setTipo(entity.getTipo());
            dto.setTempoUsado(entity.getTempoUsado());
            dto.setTempoEstimado(entity.getTempoEstimado());
            dto.setStatus(entity.getStatus().getStatus());
            dto.setValorHora(entity.getValorHora());
            if (entity.getCliente() != null) {
                dto.setClienteId(entity.getCliente().getCpf());
                dto.setClienteNome(entity.getCliente().getName());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
                dto.setMotorNome(entity.getNumSerieMotor().getSerie_motor());
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
                dto.setSupervisorNome(entity.getSupervisor().getName());
            }
            dto.setLinhas(linhaOrdemService.findByCabecalhoId(id));
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/list")
    public ResponseEntity<List<CabecalhoOrdemDTO>> getAllCabecalhos() {
        List<CabecalhoOrdem> list = cabecalhoOrdemRepository.findAllByOrderByIdDesc();
        List<CabecalhoOrdemDTO> dtos = new ArrayList<>();
        for (CabecalhoOrdem entity : list) {
            CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
            dto.setId(entity.getId());
            dto.setDataAbertura(entity.getDataAbertura());
            dto.setDataFechamento(entity.getDataFechamento());
            dto.setDescricao(entity.getDescricao());
            dto.setTipo(entity.getTipo());
            dto.setTempoUsado(entity.getTempoUsado());
            dto.setTempoEstimado(entity.getTempoEstimado());
            dto.setStatus(entity.getStatus().getStatus());
            dto.setValorHora(entity.getValorHora());
            if (entity.getCliente() != null) {
                dto.setClienteId(entity.getCliente().getCpf());
                dto.setClienteNome(entity.getCliente().getName());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
                dto.setMotorNome(entity.getNumSerieMotor().getSerie_motor());
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
                dto.setSupervisorNome(entity.getSupervisor().getName());
            }
            System.out.println("Adding CabecalhoOrdemDTO: " + dto);
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCabecalho(@RequestParam int id) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(id);
        if (opt.isPresent()) {
            cabecalhoOrdemRepository.deleteById(id);
            return ResponseEntity.ok("CabecalhoOrdem deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CabecalhoOrdem not found");
    }

    @PutMapping("/atualizar-status")
    public ResponseEntity<String> atualizarStatus(
            @RequestParam int cabecalhoId,
            @RequestParam int status) {
        return cabecalhoOrdemService.atualizarStatusCabecalho(cabecalhoId, status);
    }
    
    /**
     * Faz upload de um anexo para uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param file Arquivo a ser anexado
     * @return URL do arquivo no Azure Blob Storage
     */
    @PostMapping("/{cabecalhoId}/anexos")
    public ResponseEntity<Map<String, String>> uploadAnexo(
            @PathVariable int cabecalhoId,
            @RequestParam("file") MultipartFile file) {
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            // Usa o ID da ordem como prefixo para organização dos arquivos
            String prefix = "ordem_" + cabecalhoId;
            String fileUrl = azureBlobStorageService.uploadFile(file, prefix);
            
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("message", "Anexo adicionado com sucesso à ordem de serviço " + cabecalhoId);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Falha ao fazer upload do anexo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lista todos os anexos de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @return Lista de nomes de arquivos anexados à ordem
     */
    @GetMapping("/{cabecalhoId}/anexos")
    public ResponseEntity<?> listarAnexos(@PathVariable int cabecalhoId) {
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        String prefix = "ordem_" + cabecalhoId;
        List<String> arquivos = azureBlobStorageService.listFilesWithPrefix(prefix);
        
        List<Map<String, String>> resultados = new ArrayList<>();
        for (String nomeArquivo : arquivos) {
            Map<String, String> arquivoInfo = new HashMap<>();
            arquivoInfo.put("nome", nomeArquivo);
            arquivoInfo.put("url", azureBlobStorageService.getFileUrl(nomeArquivo));
            resultados.add(arquivoInfo);
        }
        
        return ResponseEntity.ok(resultados);
    }
    
    /**
     * Obtém o URL para download de um anexo específico
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param nomeArquivo Nome do arquivo a ser baixado
     * @return URL para download do arquivo
     */
    @GetMapping("/{cabecalhoId}/anexos/{nomeArquivo}")
    public ResponseEntity<?> downloadAnexo(
            @PathVariable int cabecalhoId,
            @PathVariable String nomeArquivo) {
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Verifica se o arquivo existe
        if (!azureBlobStorageService.fileExists(nomeArquivo)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Arquivo não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Retorna a URL para download direto
        String downloadUrl = azureBlobStorageService.getFileUrl(nomeArquivo);
        
        Map<String, String> response = new HashMap<>();
        response.put("downloadUrl", downloadUrl);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Exclui um anexo específico de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param nomeArquivo Nome do arquivo a ser excluído
     * @return Mensagem de confirmação
     */
    @DeleteMapping("/{cabecalhoId}/anexos/{nomeArquivo}")
    public ResponseEntity<?> excluirAnexo(
            @PathVariable int cabecalhoId,
            @PathVariable String nomeArquivo) {
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Verifica se o arquivo existe
        if (!azureBlobStorageService.fileExists(nomeArquivo)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Arquivo não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Exclui o arquivo
        azureBlobStorageService.deleteFile(nomeArquivo);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Arquivo excluído com sucesso");
        return ResponseEntity.ok(response);
    }
}

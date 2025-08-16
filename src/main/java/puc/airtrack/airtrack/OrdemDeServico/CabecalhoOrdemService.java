package puc.airtrack.airtrack.OrdemDeServico;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Motor.MotorRepository;

@Service
public class CabecalhoOrdemService {

    @Autowired CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    ClienteRepo clienteRepo;
    @Autowired
    MotorRepository motorRepository;
    @Autowired
    UserService userService;
    @Autowired LinhaOrdemService linhaOrdemService;

    public ResponseEntity<String> createCabecalho(CabecalhoOrdemDTO dto){
        if (dto != null) {
            CabecalhoOrdem entity = new CabecalhoOrdem();
            entity.setDataAbertura(dto.getDataAbertura());
            entity.setDataFechamento(dto.getDataFechamento());
            entity.setDescricao(dto.getDescricao());
            entity.setTempoUsado(dto.getTempoUsado());
            entity.setStatus(dto.getStatus());
            entity.setValorHora(dto.getValorHora());
            if (dto.getClienteId() != null) {
                entity.setCliente(clienteRepo.findByCpf(dto.getClienteId()).orElse(null));
            }
            if (dto.getMotorId() != null) {
                try {
                    int motorId = Integer.parseInt(dto.getMotorId());
                    entity.setNumSerieMotor(motorRepository.findById(motorId).orElse(null));
                } catch (NumberFormatException e) {
                    entity.setNumSerieMotor(null);
                }
            }
            if (dto.getSupervisorId() != null) {
                try {
                    int supervisorId = Integer.parseInt(dto.getSupervisorId());
                    entity.setSupervisor(userService.findById(supervisorId));
                } catch (NumberFormatException e) {
                    entity.setSupervisor(null);
                }
            }
            cabecalhoOrdemRepository.save(entity);
            for (LinhaOrdemDTO linha : dto.getLinhas()) {
                linha.setOrdemId(entity.getId());
                linhaOrdemService.create(linha);
            }
            URI location = URI.create("/ordem/get?id=" + entity.getId());
            return ResponseEntity.created(location).body("CabecalhoOrdem created successfully");
        }
        return ResponseEntity.badRequest().body("Invalid data");
    }

    @Transactional
    public ResponseEntity<String> updateCabecalho(CabecalhoOrdemDTO dto) {
        if (dto != null && dto.getId() != null) {
            Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(dto.getId());
            if (opt.isPresent()) {
                CabecalhoOrdem entity = opt.get();

                entity.setDataAbertura(dto.getDataAbertura());
                entity.setDataFechamento(dto.getDataFechamento());
                entity.setDescricao(dto.getDescricao());
                entity.setTempoUsado(dto.getTempoUsado());
                entity.setStatus(dto.getStatus());
                entity.setTempoUsado(dto.getTempoUsado());
                entity.setValorHora(dto.getValorHora());

                if (dto.getClienteId() != null) {
                    entity.setCliente(clienteRepo.findByCpf(dto.getClienteId()).orElse(null));
                }

                if (dto.getMotorId() != null) {
                    try {
                        int motorId = Integer.parseInt(dto.getMotorId());
                        entity.setNumSerieMotor(motorRepository.findById(motorId).orElse(null));
                    } catch (NumberFormatException e) {
                        entity.setNumSerieMotor(null);
                    }
                }

                if (dto.getSupervisorId() != null) {
                    try {
                        int supervisorId = Integer.parseInt(dto.getSupervisorId());
                        entity.setSupervisor(userService.findById(supervisorId));
                    } catch (NumberFormatException e) {
                        entity.setSupervisor(null);
                    }
                }

                linhaOrdemService.deleteAllByOrdemId(entity.getId());
                cabecalhoOrdemRepository.save(entity);

                for (LinhaOrdemDTO linha : dto.getLinhas()) {
                    linha.setOrdemId(entity.getId()); // garante vinculação
                    linhaOrdemService.create(linha);
                }

                return ResponseEntity.ok("CabecalhoOrdem updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CabecalhoOrdem not found");
    }

    @Transactional
    public ResponseEntity<String> atualizarStatusCabecalho(int cabecalhoId, int novoStatus) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (opt.isPresent()) {
            CabecalhoOrdem entity = opt.get();
            entity.setStatus(novoStatus);
            cabecalhoOrdemRepository.save(entity);
            return ResponseEntity.ok("Status atualizado com sucesso");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cabeçalho não encontrado");
    }
}
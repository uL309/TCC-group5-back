package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Pecas.PecasRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LinhaOrdemService {

    @Autowired
    private LinhaOrdemRepository linhaOrdemRepository;
    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    private PecasRepository pecasRepository;
    @Autowired
    private UserService userService;

    public Optional<LinhaOrdemDTO> findDtoById(int id) {
        return linhaOrdemRepository.findById(id).map(this::convertToDTO);
    }

    public LinhaOrdemDTO convertToDTO(LinhaOrdem entity) {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setId(entity.getId());
        dto.setQuantidade(entity.getQuantidade());
        dto.setTempoGasto(entity.getTempoGasto());
        if (entity.getOrdem() != null) {
            dto.setOrdemId(entity.getOrdem().getId());
        }
        if (entity.getPeca() != null) {
            dto.setPecaId(entity.getPeca().getId());
            dto.setPecaNome(entity.getPeca().getNome());
        }
        if (entity.getEngenheiro() != null) {
            dto.setEngenheiroId(String.valueOf(entity.getEngenheiro().getId()));
        }
        return dto;
    }

    public List<LinhaOrdemDTO> findAllDto() {
        return linhaOrdemRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LinhaOrdemDTO> findByCabecalhoId(Integer cabecalhoId) {
        return linhaOrdemRepository.findByOrdem_Id(cabecalhoId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteAllByOrdemId(Integer ordemId) {
        linhaOrdemRepository.deleteByOrdem_Id(ordemId);
    }


    public LinhaOrdemDTO create(LinhaOrdemDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO inválido");
        }

        LinhaOrdem entity = new LinhaOrdem();
        entity.setQuantidade(dto.getQuantidade());
        entity.setTempoGasto(dto.getTempoGasto());

        if (dto.getOrdemId() != null) {
            cabecalhoOrdemRepository.findById(dto.getOrdemId())
                    .ifPresent(entity::setOrdem);
        }

        if (dto.getPecaId() != null) {
            pecasRepository.findById(dto.getPecaId())
                    .ifPresent(entity::setPeca);
        }

        if (dto.getEngenheiroId() != null) {
            try {
                int engenheiroId = Integer.parseInt(dto.getEngenheiroId());
                entity.setEngenheiro(userService.findById(engenheiroId));
            } catch (NumberFormatException e) {
                entity.setEngenheiro(null);
            }
        }

        LinhaOrdem saved = linhaOrdemRepository.save(entity);
        CabecalhoOrdem cabecalho = saved.getOrdem();
        if (cabecalho != null) {
            cabecalho.setStatus(1);
            cabecalhoOrdemRepository.save(cabecalho);
        } else {
            throw new IllegalArgumentException("Ordem não encontrada para a Linha de Ordem");
        }
        return convertToDTO(saved);
    }
}

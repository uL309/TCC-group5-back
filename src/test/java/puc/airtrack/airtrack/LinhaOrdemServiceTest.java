package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdem;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;
import puc.airtrack.airtrack.Pecas.Pecas;
import puc.airtrack.airtrack.Pecas.PecasRepository;

@ExtendWith(MockitoExtension.class)
public class LinhaOrdemServiceTest {

    @InjectMocks
    private LinhaOrdemService service;

    @Mock
    private LinhaOrdemRepository linhaOrdemRepository;
    @Mock
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Mock
    private PecasRepository pecasRepository;
    @Mock
    private UserService userService;

    private LinhaOrdem entity;
    private CabecalhoOrdem cabecalho;
    private Pecas peca;

    @BeforeEach
    void setup() {
        entity = new LinhaOrdem();
        entity.setId(1);
        // quantidade no seu DTO/entity é ArrayList<Integer> — usar ArrayList via Arrays.asList -> new ArrayList(...)
        entity.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(2)));
        entity.setTempoGasto(1.5f);

        cabecalho = new CabecalhoOrdem();
        cabecalho.setId(10);

        peca = new Pecas();
        peca.setId(5);
        peca.setNome("PecaX");
    }

    @Test
    void findDtoById_found() {
        when(linhaOrdemRepository.findById(1)).thenReturn(Optional.of(entity));
        entity.setOrdem(cabecalho);
        entity.setPeca(peca);
        entity.setEngenheiro(null);

        Optional<LinhaOrdemDTO> opt = service.findDtoById(1);
        assertTrue(opt.isPresent());
        LinhaOrdemDTO dto = opt.get();
        assertEquals(1, dto.getId());
        assertEquals(entity.getTempoGasto(), dto.getTempoGasto());
        assertEquals(10, dto.getOrdemId());
        assertEquals("PecaX", dto.getPecaNome());
    }

    @Test
    void findDtoById_notFound() {
        when(linhaOrdemRepository.findById(99)).thenReturn(Optional.empty());
        Optional<LinhaOrdemDTO> opt = service.findDtoById(99);
        assertTrue(opt.isEmpty());
    }

    @Test
    void findAllDto_and_findByCabecalhoId() {
        LinhaOrdem e2 = new LinhaOrdem();
        e2.setId(2);
        when(linhaOrdemRepository.findAll()).thenReturn(Arrays.asList(entity, e2));
        var all = service.findAllDto();
        assertEquals(2, all.size());

        when(linhaOrdemRepository.findByOrdem_Id(10)).thenReturn(Arrays.asList(entity));
        var byCab = service.findByCabecalhoId(10);
        assertEquals(1, byCab.size());
    }

    @Test
    void deleteAllByOrdemId_delegatesToRepo() {
        service.deleteAllByOrdemId(20);
        verify(linhaOrdemRepository).deleteByOrdem_Id(20);
    }

    @Test
    void create_nullDto_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.create(null));
    }

    @Test
    void create_success_withOrdemPecaAndEngenheiro() {
        // preparar DTO com valores compatíveis
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(3)));
        dto.setTempoGasto(2.0f);
        dto.setOrdemId(10);
        dto.setPecaId(5);
        dto.setEngenheiroId("7");

        // mocks: find ordem, peca e engenheiro
        when(cabecalhoOrdemRepository.findById(10)).thenReturn(Optional.of(cabecalho));
        when(pecasRepository.findById(5)).thenReturn(Optional.of(peca));
        User eng = new User(); eng.setId(7);
        when(userService.findById(7)).thenReturn(eng);

        // salvar deve retornar entity with ordem set so service passes the ordem check
        when(linhaOrdemRepository.save(any(LinhaOrdem.class))).thenAnswer(inv -> {
            LinhaOrdem toSave = inv.getArgument(0);
            if (toSave.getId() == 0) toSave.setId(99);
            // ensure ordem is present (repository might return same instance)
            if (toSave.getOrdem() == null) toSave.setOrdem(cabecalho);
            return toSave;
        });

        LinhaOrdemDTO saved = service.create(dto);
        assertNotNull(saved);
        assertEquals(99, saved.getId());
        // engenheiro id in DTO should reflect the User id set on entity (convertToDTO uses eng id string)
        // since service sets engenheiro from userService, convertToDTO will store its id as string
        assertEquals("7", saved.getEngenheiroId());
    }

    @Test
    void create_invalidEngenheiroId_setsNullEngenheiroInResult() {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(1)));
        dto.setTempoGasto(0.5f);
        dto.setOrdemId(10);
        dto.setEngenheiroId("not-a-number");

        when(cabecalhoOrdemRepository.findById(10)).thenReturn(Optional.of(cabecalho));
        when(linhaOrdemRepository.save(any(LinhaOrdem.class))).thenAnswer(inv -> {
            LinhaOrdem toSave = inv.getArgument(0);
            if (toSave.getId() == 0) toSave.setId(77);
            if (toSave.getOrdem() == null) toSave.setOrdem(cabecalho);
            return toSave;
        });

        LinhaOrdemDTO result = service.create(dto);
        assertNotNull(result);
        assertNull(result.getEngenheiroId());
    }

    @Test
    void create_savedWithoutOrdem_throws() {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(1)));
        dto.setTempoGasto(0.5f);
        dto.setOrdemId(10);

        // cabecalho not found so entity.or dem will remain null after save
        when(cabecalhoOrdemRepository.findById(10)).thenReturn(Optional.empty());
        when(linhaOrdemRepository.save(any(LinhaOrdem.class))).thenAnswer(inv -> {
            LinhaOrdem toSave = inv.getArgument(0);
            if (toSave.getId() == 0) toSave.setId(88);
            // do NOT set ordem -> simulate repo behavior
            return toSave;
        });

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertTrue(ex.getMessage().contains("Ordem não encontrada"));
    }
}

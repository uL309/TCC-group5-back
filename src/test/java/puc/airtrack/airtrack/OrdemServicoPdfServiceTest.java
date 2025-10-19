package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;
import puc.airtrack.airtrack.services.OrdemServicoPdfService;

@ExtendWith(MockitoExtension.class)
public class OrdemServicoPdfServiceTest {

    @InjectMocks
    private OrdemServicoPdfService service;

    @Mock
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;

    @Mock
    private LinhaOrdemService linhaOrdemService;

    @Mock
    private UserService userService;

    @Test
    void gerarPdfOrdemServico_orderNotFound_throws() {
        int ordemId = 123;
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> service.gerarPdfOrdemServico(ordemId));
        assertTrue(ex.getMessage().contains("Ordem de serviço não encontrada"));
    }

    @Test
    void gerarPdfOrdemServico_linesServiceThrows_generatesPdf() throws Exception {
        int ordemId = 10;
        CabecalhoOrdem cab = mock(CabecalhoOrdem.class);
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));
        // linha service lança -> service deve continuar com lista vazia
        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenThrow(new RuntimeException("db fail"));

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "PDF byte array should not be empty");
    }

    @Test
    void gerarPdfOrdemServico_success_withEngineerName_andTotals() throws Exception {
        int ordemId = 5;
        CabecalhoOrdem cab = mock(CabecalhoOrdem.class);
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));

        // stubs do cabeçalho para que métodos privados adicionem informações
        when(cab.getId()).thenReturn(ordemId);
        when(cab.getDescricao()).thenReturn("Descrição teste");
        when(cab.getDataAbertura()).thenReturn("2023-01-01");
        when(cab.getTempoUsado()).thenReturn(3.5f);      // usar float
        when(cab.getValorHora()).thenReturn(120.0f);     // usar float

        // linha com dados completos
        LinhaOrdemDTO linha = mock(LinhaOrdemDTO.class);
        when(linha.getPecaNome()).thenReturn("Peça A");
        when(linha.getQuantidade()).thenReturn(new java.util.ArrayList<>(List.of(2))); // garantir ArrayList<Integer>
        when(linha.getEngenheiroId()).thenReturn("1");
        when(linha.getTempoGasto()).thenReturn(2.00f);

        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of(linha));

        // userService retorna o engenheiro
        User u = new User();
        u.setName("Engenheiro X");
        when(userService.findById(1)).thenReturn(u);

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "PDF must be generated");
    }

    @Test
    void gerarPdfOrdemServico_lineGettersThrow_handlesExceptionsAndGeneratesPdf() throws Exception {
        int ordemId = 77;
        CabecalhoOrdem cab = mock(CabecalhoOrdem.class);
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));
        when(cab.getId()).thenReturn(ordemId);
        when(cab.getDescricao()).thenReturn("Teste excecao");

        // linha que lança exceção ao acessar quantidade e tempo
        LinhaOrdemDTO linha = mock(LinhaOrdemDTO.class);
        when(linha.getPecaNome()).thenReturn(null);
        when(linha.getQuantidade()).thenThrow(new RuntimeException("q fail"));
        when(linha.getEngenheiroId()).thenReturn("badId");
        when(linha.getTempoGasto()).thenThrow(new RuntimeException("tempo fail"));

        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of(linha));
        // userService.findById vai lançar NumberFormatException no parse -> service trata e retorna texto "Não identificado"
        // evitar UnnecessaryStubbingException se o serviço nem invocar userService.findById
        lenient().when(userService.findById(anyInt())).thenThrow(new RuntimeException("user fail"));

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}

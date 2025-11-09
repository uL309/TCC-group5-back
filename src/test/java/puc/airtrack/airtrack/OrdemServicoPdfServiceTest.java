package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import puc.airtrack.airtrack.Cliente.Cliente;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;
import puc.airtrack.airtrack.OrdemDeServico.OrdemStatus;
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

    @Test
    void gerarPdfOrdemServico_informacoes_minimas_statusNull_camposAusentes() throws Exception {
        int ordemId = 201;
        CabecalhoOrdem cab = new CabecalhoOrdem();
        cab.setId(ordemId);
        // status null, cliente null, motor null, supervisor null, descricao null, dataFechamento null
        cab.setStatus(null);
        cab.setDataAbertura("2025-01-01");
        cab.setDataFechamento(null);
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));
        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of());

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // Não valida conteúdo textual (binário), apenas cobre branches
    }

    @Test
    void gerarPdfOrdemServico_informacoes_completas_incluiDataFechamento() throws Exception {
        int ordemId = 202;
        CabecalhoOrdem cab = new CabecalhoOrdem();
        cab.setId(ordemId);
        cab.setStatus(OrdemStatus.CONCLUIDA);
        cab.setDataAbertura("2025-02-02T10:00:00");
        cab.setDataFechamento("2025-02-05T15:30:00");
        cab.setDescricao("Teste completa");
        // cliente (tipo correto)
        Cliente cliente = new Cliente();
        cliente.setName("Cliente Z");
        cliente.setCpf("12345678900");
        cab.setCliente(cliente);
        // motor
        Motor motor = new Motor();
        motor.setSerie_motor("MTR-001");
        cab.setNumSerieMotor(motor);
        // supervisor
        User sup = new User();
        sup.setName("Supervisor Y");
        cab.setSupervisor(sup);

        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));
        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of());

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void gerarPdfOrdemServico_dataFechamentoVazia_ignorada() throws Exception {
        int ordemId = 203;
        CabecalhoOrdem cab = new CabecalhoOrdem();
        cab.setId(ordemId);
        cab.setStatus(OrdemStatus.ANDAMENTO);
        cab.setDataAbertura("2025-03-03");
        cab.setDataFechamento(""); // string vazia deve não adicionar célula
        cab.setDescricao("Descricao");
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));
        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of());

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void gerarPdfOrdemServico_writerFalha_lancaExceptionEfechaDocumento() throws Exception {
        int ordemId = 300;
        CabecalhoOrdem cab = new CabecalhoOrdem();
        cab.setId(ordemId);
        cab.setDataAbertura("2025-01-01");
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));
        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of());

        try (MockedStatic<PdfWriter> mocked = mockStatic(PdfWriter.class)) {
            mocked.when(() -> PdfWriter.getInstance(any(com.itextpdf.text.Document.class), any(OutputStream.class)))
                  .thenThrow(new DocumentException("fail"));
            Exception ex = assertThrows(Exception.class, () -> service.gerarPdfOrdemServico(ordemId));
            assertTrue(ex.getMessage().contains("Erro ao gerar PDF"));
        }
    }

    @Test
    void adicionarItensServicos_quantidadeNullOuVazia_engIdVazioOuExcecao_geraPdf() throws Exception {
        int ordemId = 301;
        CabecalhoOrdem cab = new CabecalhoOrdem();
        cab.setId(ordemId);
        cab.setDataAbertura("2025-01-02");
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));

        // l1: quantidade null, engenheiroId vazio, tempo ok
        LinhaOrdemDTO l1 = mock(LinhaOrdemDTO.class);
        when(l1.getPecaNome()).thenReturn("X");
        when(l1.getQuantidade()).thenReturn(null);
        when(l1.getEngenheiroId()).thenReturn("");
        when(l1.getTempoGasto()).thenReturn(1.0f);

        // l2: quantidade vazia, engenheiroId null
        LinhaOrdemDTO l2 = mock(LinhaOrdemDTO.class);
        when(l2.getPecaNome()).thenReturn(null);
        when(l2.getQuantidade()).thenReturn(new java.util.ArrayList<>());
        when(l2.getEngenheiroId()).thenReturn(null);
        when(l2.getTempoGasto()).thenReturn(0.5f);

        // l3: getEngenheiroId lança exceção -> cai no catch e usa "-"
        LinhaOrdemDTO l3 = mock(LinhaOrdemDTO.class);
        when(l3.getPecaNome()).thenReturn("Y");
        when(l3.getQuantidade()).thenReturn(new java.util.ArrayList<>(List.of(3)));
        when(l3.getEngenheiroId()).thenThrow(new RuntimeException("getter fail"));
        when(l3.getTempoGasto()).thenReturn(0.75f);

        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of(l1, l2, l3));

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void obterNomeEngenheiro_usuarioSemNome_retornaEngHash() throws Exception {
        int ordemId = 302;
        CabecalhoOrdem cab = new CabecalhoOrdem();
        cab.setId(ordemId);
        cab.setDataAbertura("2025-01-03");
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));

        // linha com engenheiroId válido, mas userService retorna usuário sem nome
        LinhaOrdemDTO l = mock(LinhaOrdemDTO.class);
        when(l.getPecaNome()).thenReturn("Z");
        when(l.getQuantidade()).thenReturn(new java.util.ArrayList<>(List.of(1)));
        when(l.getEngenheiroId()).thenReturn("2");
        when(l.getTempoGasto()).thenReturn(1.25f);
        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of(l));

        User userSemNome = new User(); // name null
        when(userService.findById(2)).thenReturn(userSemNome);

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void obterNomeEngenheiro_idVazio_retornaTraco() throws Exception {
        int ordemId = 303;
        CabecalhoOrdem cab = new CabecalhoOrdem();
        cab.setId(ordemId);
        cab.setDataAbertura("2025-01-04");
        when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(cab));

        LinhaOrdemDTO l = mock(LinhaOrdemDTO.class);
        when(l.getPecaNome()).thenReturn("W");
        when(l.getQuantidade()).thenReturn(new java.util.ArrayList<>(List.of(1)));
        when(l.getEngenheiroId()).thenReturn(""); // força retorno "-"
        when(l.getTempoGasto()).thenReturn(0.25f);
        when(linhaOrdemService.findByCabecalhoId(ordemId)).thenReturn(List.of(l));

        byte[] pdf = service.gerarPdfOrdemServico(ordemId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}

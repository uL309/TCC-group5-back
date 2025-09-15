package puc.airtrack.airtrack.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserService;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerar PDFs da Ordem de Serviço
 */
@Service
public class OrdemServicoPdfService {

    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    
    @Autowired
    private LinhaOrdemService linhaOrdemService;
    
    @Autowired
    private UserService userService;

    /**
     * Gera um PDF com todos os detalhes da ordem de serviço
     *
     * @param ordemId ID da ordem de serviço
     * @return Array de bytes com o conteúdo do PDF
     * @throws Exception se ocorrer algum erro na geração do PDF
     */
    public byte[] gerarPdfOrdemServico(int ordemId) throws Exception {
        System.out.println("Gerando PDF para ordem de serviço ID: " + ordemId);
        
        // Buscar a ordem pelo ID
        Optional<CabecalhoOrdem> optOrdem = cabecalhoOrdemRepository.findById(ordemId);
        if (optOrdem.isEmpty()) {
            System.err.println("Ordem de serviço não encontrada: " + ordemId);
            throw new Exception("Ordem de serviço não encontrada: " + ordemId);
        }
        
        CabecalhoOrdem cabecalho = optOrdem.get();
        List<LinhaOrdemDTO> linhas;
        try {
            linhas = linhaOrdemService.findByCabecalhoId(ordemId);
            System.out.println("Encontradas " + linhas.size() + " linhas para a ordem");
        } catch (Exception e) {
            System.err.println("Erro ao buscar linhas da ordem: " + e.getMessage());
            linhas = new ArrayList<>();  // Lista vazia em caso de erro
        }
        
        // Criar documento PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Adicionar cabeçalho com logo e título
            adicionarCabecalhoPdf(document);
            
            // Adicionar informações da ordem
            adicionarInformacoesOrdem(document, cabecalho);
            
            // Adicionar lista de itens/serviços
            adicionarItensServicos(document, linhas);
            
            // Adicionar totais e resumo financeiro
            adicionarTotaisOrdem(document, cabecalho, linhas);
            
            // Adicionar rodapé com informações de contato
            adicionarRodapePdf(document);
            
            document.close();
            System.out.println("PDF gerado com sucesso");
            
        } catch (Exception e) {
            System.err.println("Erro na geração do PDF: " + e.getMessage());
            e.printStackTrace();
            
            // Garantir que o documento esteja fechado em caso de erro
            if (document != null && document.isOpen()) {
                document.close();
            }
            
            throw new Exception("Erro ao gerar PDF: " + e.getMessage());
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Adiciona o cabeçalho do PDF com logo e título
     */
    private void adicionarCabecalhoPdf(Document document) throws DocumentException {
        Paragraph titulo = new Paragraph("ORDEM DE SERVIÇO", 
                new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        Paragraph subtitulo = new Paragraph("AIRTRACK MANUTENÇÃO DE AERONAVES", 
                new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL));
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitulo);
        
        // Adicionar espaço após o cabeçalho
        document.add(new Paragraph(" "));
    }
    
    /**
     * Adiciona as informações básicas da ordem de serviço
     */
    private void adicionarInformacoesOrdem(Document document, CabecalhoOrdem ordem) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        
        // Número da ordem
        adicionarCelulaDados(table, "Ordem Nº:", String.valueOf(ordem.getId()), boldFont, normalFont);
        
        // Status da ordem
        String statusTexto = "Não definido";
        if (ordem.getStatus() != null) {
            statusTexto = ordem.getStatus().getStatusDescricao();
        }
        adicionarCelulaDados(table, "Status:", statusTexto, boldFont, normalFont);
        
        // Data de abertura
        adicionarCelulaDados(table, "Data Abertura:", ordem.getDataAbertura(), boldFont, normalFont);
        
        // Data de fechamento (se houver)
        if (ordem.getDataFechamento() != null && !ordem.getDataFechamento().isEmpty()) {
            adicionarCelulaDados(table, "Data Fechamento:", ordem.getDataFechamento(), boldFont, normalFont);
        }
        
        // Cliente
        if (ordem.getCliente() != null) {
            adicionarCelulaDados(table, "Cliente:", ordem.getCliente().getName(), boldFont, normalFont);
            adicionarCelulaDados(table, "CPF/CNPJ:", ordem.getCliente().getCpf(), boldFont, normalFont);
        }
        
        // Motor
        if (ordem.getNumSerieMotor() != null) {
            adicionarCelulaDados(table, "Motor:", ordem.getNumSerieMotor().getSerie_motor(), boldFont, normalFont);
        }
        
        // Supervisor
        if (ordem.getSupervisor() != null) {
            adicionarCelulaDados(table, "Supervisor:", ordem.getSupervisor().getName(), boldFont, normalFont);
        }
        
        // Descrição
        PdfPCell cellLabel = new PdfPCell(new Phrase("Descrição:", boldFont));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellLabel);
        
        PdfPCell cellValue = new PdfPCell(new Phrase(ordem.getDescricao(), normalFont));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setColspan(3);
        table.addCell(cellValue);
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    /**
     * Adiciona uma célula com label e valor à tabela
     */
    private void adicionarCelulaDados(PdfPTable table, String label, String valor, Font labelFont, Font valorFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellLabel);
        
        PdfPCell cellValue = new PdfPCell(new Phrase(valor != null ? valor : "", valorFont));
        cellValue.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellValue);
    }
    
    /**
     * Adiciona a tabela de itens e serviços da ordem
     */
    private void adicionarItensServicos(Document document, List<LinhaOrdemDTO> linhas) throws DocumentException {
        Paragraph titulo = new Paragraph("ITENS E SERVIÇOS", 
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        titulo.setAlignment(Element.ALIGN_LEFT);
        document.add(titulo);
        
        PdfPTable table = new PdfPTable(new float[] {1, 4, 1, 2, 2});
        table.setWidthPercentage(100);
        
        // Cabeçalho da tabela
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        
        PdfPCell headerCell1 = new PdfPCell(new Phrase("Item", headerFont));
        headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell1.setPadding(5);
        table.addCell(headerCell1);
        
        PdfPCell headerCell2 = new PdfPCell(new Phrase("Descrição", headerFont));
        headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell2.setPadding(5);
        table.addCell(headerCell2);
        
        PdfPCell headerCell3 = new PdfPCell(new Phrase("Qtde", headerFont));
        headerCell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell3.setPadding(5);
        table.addCell(headerCell3);
        
        PdfPCell headerCell4 = new PdfPCell(new Phrase("Engenheiro", headerFont));
        headerCell4.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell4.setPadding(5);
        table.addCell(headerCell4);
        
        PdfPCell headerCell5 = new PdfPCell(new Phrase("Tempo (h)", headerFont));
        headerCell5.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell5.setPadding(5);
        table.addCell(headerCell5);
        
        // Dados da tabela
        Font contentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        int contador = 1;
        
        for (LinhaOrdemDTO linha : linhas) {
            // Item número
            PdfPCell cell1 = new PdfPCell(new Phrase(String.valueOf(contador++), contentFont));
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setPadding(5);
            table.addCell(cell1);
            
            // Descrição da peça
            String descricao = linha.getPecaNome() != null ? linha.getPecaNome() : "-";
            PdfPCell cell2 = new PdfPCell(new Phrase(descricao, contentFont));
            cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell2.setPadding(5);
            table.addCell(cell2);
            
            // Quantidade
            String quantidade = "1";
            try {
                if (linha.getQuantidade() != null && linha.getQuantidade().size() > 0) {
                    quantidade = String.valueOf(linha.getQuantidade().get(0));
                }
            } catch (Exception e) {
                // Em caso de erro, usa o valor padrão "1"
                System.err.println("Erro ao obter quantidade: " + e.getMessage());
            }
            PdfPCell cell3 = new PdfPCell(new Phrase(quantidade, contentFont));
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setPadding(5);
            table.addCell(cell3);
            
            // Engenheiro - Obter o nome do engenheiro a partir do ID
            String engenheiroNome = "-";
            try {
                if (linha.getEngenheiroId() != null && !linha.getEngenheiroId().isEmpty()) {
                    // Usar userService para buscar o nome do engenheiro
                    engenheiroNome = obterNomeEngenheiro(linha.getEngenheiroId());
                    System.out.println("Nome do engenheiro para linha " + contador + ": " + engenheiroNome);
                }
            } catch (Exception e) {
                System.err.println("Erro ao obter engenheiro para linha " + contador + ": " + e.getMessage());
            }
            PdfPCell cell4 = new PdfPCell(new Phrase(engenheiroNome, contentFont));
            cell4.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell4.setPadding(5);
            table.addCell(cell4);
            
            // Tempo gasto
            DecimalFormat df = new DecimalFormat("#,##0.00");
            String tempoFormatado;
            try {
                tempoFormatado = df.format(linha.getTempoGasto());
            } catch (Exception e) {
                tempoFormatado = "0,00";
                System.err.println("Erro ao formatar tempo: " + e.getMessage());
            }
            PdfPCell cell5 = new PdfPCell(new Phrase(tempoFormatado, contentFont));
            cell5.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell5.setPadding(5);
            table.addCell(cell5);
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    /**
     * Adiciona o resumo financeiro e totais da ordem
     */
    private void adicionarTotaisOrdem(Document document, CabecalhoOrdem ordem, List<LinhaOrdemDTO> linhas) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font valorFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        
        // Total de horas
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String tempoFormatado;
        try {
            tempoFormatado = df.format(ordem.getTempoUsado());
        } catch (Exception e) {
            tempoFormatado = "0,00";
            System.err.println("Erro ao formatar tempo total: " + e.getMessage());
        }
        adicionarCelulaTotal(table, "Tempo Total (horas):", tempoFormatado, labelFont, valorFont);
        
        // Valor da hora
        if (ordem.getValorHora() != null) {
            try {
                String valorHoraFormatado = df.format(ordem.getValorHora());
                adicionarCelulaTotal(table, "Valor/Hora (R$):", valorHoraFormatado, labelFont, valorFont);
                
                // Valor total
                float valorTotal = ordem.getTempoUsado() * ordem.getValorHora();
                Font totalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                adicionarCelulaTotal(table, "VALOR TOTAL (R$):", df.format(valorTotal), totalFont, totalFont);
            } catch (Exception e) {
                System.err.println("Erro ao calcular valores financeiros: " + e.getMessage());
                adicionarCelulaTotal(table, "Valor/Hora (R$):", "0,00", labelFont, valorFont);
                adicionarCelulaTotal(table, "VALOR TOTAL (R$):", "0,00", labelFont, valorFont);
            }
        } else {
            adicionarCelulaTotal(table, "Valor/Hora (R$):", "Não informado", labelFont, valorFont);
            adicionarCelulaTotal(table, "VALOR TOTAL (R$):", "Não calculado", labelFont, valorFont);
        }
        
        document.add(table);
    }
    
    /**
     * Obtém o nome do engenheiro pelo ID
     * 
     * @param engenheiroId ID do engenheiro
     * @return Nome do engenheiro ou texto padrão se não encontrado
     */
    private String obterNomeEngenheiro(String engenheiroId) {
        if (engenheiroId == null || engenheiroId.isEmpty()) {
            return "-";
        }
        
        try {
            // Busca os dados do usuário através do ID
            int id = Integer.parseInt(engenheiroId);
            User engenheiro = userService.findById(id);
            if (engenheiro != null && engenheiro.getName() != null) {
                return engenheiro.getName();
            }
            return "Eng. #" + engenheiroId;
        } catch (Exception e) {
            System.err.println("Erro ao buscar engenheiro: " + e.getMessage());
            return "Não identificado";
        }
    }
    
    /**
     * Adiciona uma célula na tabela de totais
     */
    private void adicionarCelulaTotal(PdfPTable table, String label, String valor, Font labelFont, Font valorFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLabel.setPadding(5);
        table.addCell(cellLabel);
        
        PdfPCell cellValue = new PdfPCell(new Phrase(valor, valorFont));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValue.setPadding(5);
        table.addCell(cellValue);
    }
    
    /**
     * Adiciona o rodapé ao PDF
     */
    private void adicionarRodapePdf(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        
        Paragraph rodape = new Paragraph("Documento gerado em " + 
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), 
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
        rodape.setAlignment(Element.ALIGN_CENTER);
        document.add(rodape);
        
        Paragraph empresa = new Paragraph("AIRTRACK MANUTENÇÃO DE AERONAVES", 
                new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD));
        empresa.setAlignment(Element.ALIGN_CENTER);
        document.add(empresa);
        
        Paragraph contato = new Paragraph("contato@airtrack.com.br | (31) 3333-4444", 
                new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL));
        contato.setAlignment(Element.ALIGN_CENTER);
        document.add(contato);
    }
}
const express = require('express');
const { OpenAI } = require("openai");
const app = express();
const client = new OpenAI({ apiKey: "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" });
const puppeteer = require('puppeteer');

// Função para formatar a análise da IA com quebras de parágrafo
function formatAnaliseAI(text) {
  // Substitui **texto** por <strong>texto</strong>
  text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

  const paragraphs = text.split(/\n\n+/);
  let formattedHtml = '';
  paragraphs.forEach(paragraph => {
    paragraph = paragraph.trim();
    if (!paragraph) return;
    if (paragraph.startsWith('###')) {
      formattedHtml += `<h3>${paragraph.replace(/^###\s*/, '')}</h3>`;
    } else if (paragraph.startsWith('####')) {
      formattedHtml += `<h4>${paragraph.replace(/^####\s*/, '')}</h4>`;
    } else if (paragraph.startsWith('#')) {
      formattedHtml += `<h2>${paragraph.replace(/^#\s*/, '')}</h2>`;
    } else if (paragraph.startsWith('-') || paragraph.startsWith('*')) {
      const items = paragraph.split(/\n(?=[-*])/);
      formattedHtml += '<ul>';
      items.forEach(item => {
        formattedHtml += `<li>${item.replace(/^[-*]\s*/, '')}</li>`;
      });
      formattedHtml += '</ul>';
    } else {
      formattedHtml += `<p>${paragraph}</p>`;
    }
  });
  return formattedHtml;
}
app.get('/generate-pdf', async (req, res) => {
  try {
    const dataJson = req.query.data;
    if (!dataJson) {
      return res.status(400).send('Parâmetro "data" não enviado');
    }
    let dados;
    try {
      dados = JSON.parse(decodeURIComponent(dataJson));
    } catch (e) {
      return res.status(400).send('JSON inválido');
    }

    // Gerar análise com AI
     const completion = await client.chat.completions.create({
       model: "gpt-4o-mini",
      messages: [
         { role: "system", content: "Você é um supervisor do sistema. Crie análises com formatação clara usando títulos (###), subtítulos (####), parágrafos com quebras de linha duplas entre eles, e listas com marcadores quando apropriado. Organize o conteúdo em seções bem definidas." },
        { role: "user", content: `Analise essa tabela com Ordens de Serviço (Quando aparecer o valor 0 no Status é Pendente, 1 é em andamento e 2 é concluida) e gere um resumo executivo sobre cada mês da data_abertura, apresentando: QUantidade de ordens que estão pendente, Em andamento e concluída, uma média do tempo usado (trazendo só o resultado), e listando as ordens de serviço que o tempo usado ficou maior que o estimado colocando o status da ordem ao lado, bem formatado com títulos, subtítulos e parágrafos separados: ${JSON.stringify(dados)}` }
       ]
     });

     const analiseAI = completion.choices[0].message.content;


    // Montar HTML para PDF
    let tabelaOrdemHtml = `<table border="1" style="border-collapse: collapse; width: 100%;">`;
    tabelaOrdemHtml += `<tr>
      <th>ID</th>
      <th>Cliente</th>
      <th>Motor</th>
      <th>Data Abertura</th>
      <th>Data Fechamento</th>
      <th>Tempo Usado</th>
      <th>Valor Hora</th>
      <th>Valor Total</th>
      <th>Status</th>
    </tr>`;
dados.forEach(row => {
  tabelaOrdemHtml += `<tr>
    <td>${row.id}</td>
    <td>${row.cliente}</td>
    <td>${row.motor}</td>
    <td>${row.data_abertura}</td>
    <td>${row.data_fechamento}</td>
    <td>${row.tempo_usado}</td>
    <td>${row.valor_hora}</td>
    <td>${row.valor_total}</td>
    <td>${row.status}</td>
  </tr>`;
});
    tabelaOrdemHtml += `</table>`;

    const html = `
      <html>
        <head>
          <style>
            body { font-family: Arial; padding: 20px; }
            h1 { color: #0069FF; }
            h2 { color: #0069FF; margin-top: 20px; font-size: 22px; }
            h3 { color: #333; margin-top: 16px; font-size: 18px; }
            h4 { color: #444; margin-top: 14px; font-size: 16px; }
            p { margin: 10px 0; line-height: 1.5; }
            table, th, td { border: 1px solid black; border-collapse: collapse; padding: 5px; }
            th { background-color: #f2f2f2; }
            ul { margin: 10px 0; }
            li { margin-bottom: 5px; }
            .ai { margin-top: 30px; }
          </style>
        </head>
        <body>
          <div class="ai">
            <h2>Resumo de dados da tabela de Ordem de Serviço</h2>
            ${formatAnaliseAI(analiseAI)}
          </div>
        </body>
      </html>
    `;

    const browser = await puppeteer.launch({ headless: "new", args: ['--no-sandbox'] });
    const page = await browser.newPage();
    await page.setContent(html, { waitUntil: 'networkidle0' });
    const pdf = await page.pdf({ format: 'A4', printBackground: true });
    await browser.close();

    res.set({
  'Content-Type': 'application/pdf',
  'Content-Disposition': 'attachment; filename="relatorio.pdf"',
  });
  res.end(pdf);


  } catch (err) {
    console.error(err);
    res.status(500).send('Erro ao gerar PDF: ' + err.message);
  }
});

app.listen(3001, () => console.log('Puppeteer server running on port 3001'));

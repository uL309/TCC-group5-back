const express = require('express');
const { OpenAI } = require("openai");
const app = express();
const client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY }); // use variáveis de ambiente
const puppeteer = require('puppeteer');

// Função para formatar a análise da IA com quebras de parágrafo
function formatAnaliseAI(text) {
  // Negrito para <strong>
  text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

  // Split por linhas
  const paragraphs = text.split(/\n\n+/);
  let formattedHtml = '';
  paragraphs.forEach(paragraph => {
    paragraph = paragraph.trim();
    if (!paragraph) return;

    if (paragraph.startsWith('## ')) {
      formattedHtml += `<h2>${paragraph.replace(/^##\s*/, '')}</h2>`;
    } else if (paragraph.startsWith('# ')) {
      formattedHtml += `<h2>${paragraph.replace(/^#\s*/, '')}</h2>`;
    } else if (paragraph.startsWith('-') || paragraph.startsWith('*')) {
      // lista
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

    // Instrução clara para IA gerar exatamente no formato desejado
    const completion = await client.chat.completions.create({
      model: "gpt-4o",
      messages: [
        {
          role: "system",
          content: `Você é um supervisor do sistema. GERE o resumo executivo de ordens de serviço EXATAMENTE no formato abaixo, usando títulos "# Mês" e subtítulos, listas com "- ", parágrafos com quebras duplas:
Resumo de dados da tabela de Ordem de Serviço
Resumo Executivo de Ordens de Serviço por Mês
Este relatório analisa as Ordens de Serviço com base nos dados fornecidos, destacando a
quantidade e o status das ordens, a média de tempo utilizado e as ordens em que o tempo utilizado
excedeu o estimado.
Para cada mês, apresente:
- Quantidade de Ordens (Pendente, Em Andamento, Concluída)
- Média do Tempo Usado (apenas o resultado)
- Ordens com Tempo Usado maior que o Estimado, com ID e status
Finalize com 'Considerações Finais'.`
        },
        {
          role: "user",
          content: `Dados: ${JSON.stringify(dados)}`
        }
      ]
    });

    const analiseAI = completion.choices[0].message.content;

    // HTML do relatório
    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <style>
            body { font-family: Arial, sans-serif; padding: 20px; }
            h1 { color: #0069FF; }
            h2 { color: #0069FF; margin-top: 20px; font-size: 22px; }
            p { margin: 10px 0; line-height: 1.5; }
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

app.listen(3001, () => console.log('Servidor rodando na porta 3001'));

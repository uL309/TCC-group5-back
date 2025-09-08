const express = require('express');
const { OpenAI } = require("openai");
const app = express();
const client = new OpenAI({ apiKey: "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" });
const puppeteer = require('puppeteer');
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
         { role: "system", content: "Você é um analista de negócios." },
        { role: "user", content: `Analise estes dados de vendas e gere um resumo executivo: ${JSON.stringify(dados)}` }
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
            table, th, td { border: 1px solid black; border-collapse: collapse; padding: 5px; }
            th { background-color: #f2f2f2; }
            .ai { margin-top: 20px; font-style: italic; }
          </style>
        </head>
        <body>
          <h1>Relatório de Vendas</h1>
          ${tabelaOrdemHtml}
          <div class="ai">
            <h2>Análise AI</h2>
            <p>${analiseAI}</p>
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
    res.send(pdf);

  } catch (err) {
    console.error(err);
    res.status(500).send('Erro ao gerar PDF: ' + err.message);
  }
});

app.listen(3001, () => console.log('Puppeteer server running on port 3001'));

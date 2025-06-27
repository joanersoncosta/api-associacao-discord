document.getElementById("discordForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const botao = this.querySelector("button");
  const textoOriginal = "VALIDAR";

  botao.disabled = true;
  botao.innerHTML = `<span class="loading-spinner">⏳</span> Aguarde...`;

  const urlAtual = window.location.href;
  const partes = urlAtual.split("/");
  const nome = partes[partes.length - 3];
  const idDiscord = partes[partes.length - 2];
  const token = document.getElementById("token").value;

  const payload = { nome, idDiscord, token };

  try {
    const resposta = await fetch("/api/associacao/associar-discord", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const respostaTexto = await resposta.text();

    if (resposta.ok) {
      if (respostaTexto.includes("já foi")) {
        const sucessoEl = document.getElementById("mensagemSucesso");
        sucessoEl.innerText = "Seu token já foi validado!";
        sucessoEl.style.display = "block";
        document.getElementById("mensagemErro").style.display = "none";
      }

      setTimeout(() => {
        window.location.href = "/api/formulario/resposta-token-discord";
      }, 1700);
    } else {
      mostrarErroDepoisDeDelay(respostaTexto);
    }
  } catch (erro) {
    mostrarErroDepoisDeDelay(erro.message);
  }

  function mostrarErroDepoisDeDelay(mensagem) {
    setTimeout(() => {
      const erroEl = document.getElementById("mensagemErro");
      const texto = extrairMensagemErro(mensagem);
      document.getElementById("erroTexto").innerText = texto;
      erroEl.style.display = "flex";
      document.getElementById("mensagemSucesso").style.display = "none";

      botao.disabled = false;
      botao.innerHTML = textoOriginal;
    }, 1700);
  }
});

function extrairMensagemErro(mensagem) {
  try {
    const json = JSON.parse(mensagem);
    return json.message || "erro inesperado ao validar token.";
  } catch {
    return mensagem.includes("Usuario já foi associado")
      ? "Seu token já foi validado!"
      : mensagem.toLowerCase().replace("error:", "").trim();
  }
}

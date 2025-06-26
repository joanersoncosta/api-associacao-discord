document.getElementById("discordForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const urlAtual = window.location.href;
  const partes = urlAtual.split("/");
  const nome = partes[partes.length - 3];
  const idDiscord = partes[partes.length - 2];
  const token = document.getElementById("token").value.trim();

  const payload = { nome, idDiscord, token };

  try {
    const resposta = await fetch("/api/associacao/associar-discord", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const respostaTexto = await resposta.text();

    if (resposta.ok) {
      const sucessoEl = document.getElementById("mensagemSucesso");
      sucessoEl.innerText = respostaTexto.includes("já foi") 
        ? "Seu token já foi validado!" 
        : "Sua conta do Discord foi associada com sucesso.";
      sucessoEl.style.display = "block";

      document.getElementById("mensagemErro").style.display = "none";

      setTimeout(() => {
        window.location.href = "/api/formulario/resposta-token-discord";
      }, 1200);
    } else {
      throw new Error(respostaTexto);
    }
  } catch (erro) {
    const mensagemErro = extrairMensagemErro(erro.message);
    const erroEl = document.getElementById("mensagemErro");
    const textoErro = document.getElementById("erroTexto");

    textoErro.innerText = mensagemErro;
    erroEl.style.display = "flex";
    document.getElementById("mensagemSucesso").style.display = "none";
  }
});

function extrairMensagemErro(mensagem) {
  try {
    const json = JSON.parse(mensagem);
    return json.message || "Erro inesperado ao validar token.";
  } catch {
    return mensagem.includes("Usuario já foi associado")
      ? "Seu token já foi validado!"
      : mensagem.toLowerCase().replace("error:", "").trim();
  }
}

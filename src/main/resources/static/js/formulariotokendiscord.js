document.getElementById("discordForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const urlAtual = window.location.href;
  const partes = urlAtual.split("/");
  const nome = partes[partes.length - 3];
  const idDiscord = partes[partes.length - 2];
  const token = document.getElementById("token").value;

  const payload = {
    nome: nome,
    idDiscord: idDiscord,
    token: token
  };

  try {
    const resposta = await fetch("/api/associacao/associar-discord", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    const respostaTexto = await resposta.text();

    if (resposta.ok) {
      const sucessoEl = document.getElementById("mensagemSucesso");
      if (respostaTexto.includes("já foi associado") || respostaTexto.includes("já foi validado")) {
        sucessoEl.innerText = "Seu token já foi validado!";
      } else {
        sucessoEl.innerText = "Sua conta do Discord foi associada com sucesso.";
      }
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
    erroEl.innerText = mensagemErro;
    erroEl.style.display = "block";
    document.getElementById("mensagemSucesso").style.display = "none";
  }
});

function extrairMensagemErro(mensagem) {
  try {
    const json = JSON.parse(mensagem);
    return json.message || "erro inesperado ao validar token.";
  } catch {
    return mensagem.includes("Usuario já foi associado") ? 
      "Seu token já foi validado!" : 
      mensagem.toLowerCase().replace("error:", "").trim();
  }
}

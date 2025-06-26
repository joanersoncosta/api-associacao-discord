const MENSAGENS = {
  SUCESSO_ASSOCIACAO: 'Token associado com sucesso!',
  ERRO_ASSOCIACAO: 'Erro ao associar token. Verifique os dados e tente novamente.',
  ERRO_TECNICO: 'Erro técnico. Tente novamente mais tarde ou entre em contato com o suporte.'
};

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById("discordForm");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    await enviarTokenDiscord();
  });
});

async function enviarTokenDiscord() {
  const urlParams = new URLSearchParams(window.location.search);
  const nome = urlParams.get('nome');
  const idDiscord = urlParams.get('id');
  const token = document.getElementById("token").value.trim();

  if (!nome || !idDiscord) {
    mostrarMensagem('erro', 'Link inválido: nome ou id ausente na URL.');
    return;
  }

  if (!token) {
    mostrarMensagem('erro', 'Por favor, insira seu token.');
    return;
  }

  const dados = {
    nome,
    idDiscord,
    token
  };

  const url = "localhost:8080/api/associacao/associar-discord";

  const telaEscura = document.getElementById('blackScreen');
  const loader = document.querySelector('.loader');
  if (telaEscura) telaEscura.style.display = "flex";
  if (loader) loader.style.display = "flex";

  try {
    const resposta = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(dados)
    });

    if (!resposta.ok) {
      const erroApi = await resposta.json();
      throw new Error(erroApi.message || MENSAGENS.ERRO_ASSOCIACAO);
    }

    mostrarMensagem('sucesso', MENSAGENS.SUCESSO_ASSOCIACAO);
  } catch (erro) {
    console.error('Erro ao associar:', erro);
    mostrarMensagem('erro', erro.message || MENSAGENS.ERRO_TECNICO);
  } finally {
    if (loader) loader.style.display = "none";
  }
}

function mostrarMensagem(tipo, texto) {
  const modalFalha = document.getElementById('modal-erro');
  const modalSucesso = document.getElementById('modal-sucess');
  const messageErro = document.getElementById('mensagemStatusErro');
  const messageSucesso = document.getElementById('mensagemStatusSucesso');

  if (tipo === 'sucesso') {
    if (modalSucesso && messageSucesso) {
      messageSucesso.textContent = texto;
      modalSucesso.style.display = 'flex';
    } else {
      alert(texto);
    }
  } else {
    if (modalFalha && messageErro) {
      messageErro.textContent = texto;
      modalFalha.style.display = 'flex';
    } else {
      alert(texto);
    }
  }
}

const streamConfig = document.getElementById("notificacoesStream");
const listaNotificacoes = document.getElementById("listaNotificacoes");

if (streamConfig && window.EventSource) {
    const baseUrl = streamConfig.getAttribute("data-url");
    const token = streamConfig.getAttribute("data-token");
    const tipo = streamConfig.getAttribute("data-tipo");
    const id = streamConfig.getAttribute("data-id");

    if (baseUrl && token && tipo && id) {
        const url = baseUrl
            + "/notificacoes/stream?token=" + encodeURIComponent(token)
            + "&destinatarioTipo=" + encodeURIComponent(tipo)
            + "&destinatarioId=" + encodeURIComponent(id);

        const source = new EventSource(url);

        source.addEventListener("notificacao", function (event) {
            const notificacao = JSON.parse(event.data);
            adicionarNotificacao(notificacao);
        });
    }
}

function adicionarNotificacao(notificacao) {
    if (!listaNotificacoes) {
        return;
    }

    const item = document.createElement("div");
    item.className = "notificacao-item";

    const data = document.createElement("strong");
    data.textContent = formatarData(notificacao.criadaEm);

    const mensagem = document.createElement("span");
    mensagem.textContent = notificacao.mensagem;

    item.appendChild(data);
    item.appendChild(mensagem);
    listaNotificacoes.prepend(item);

    const card = listaNotificacoes.closest(".card-painel");
    const emptyMessage = card ? card.querySelector(".mensagem-vazia") : null;
    if (emptyMessage) {
        emptyMessage.style.display = "none";
    }
}

function formatarData(value) {
    if (!value || value.length < 10) {
        return "";
    }
    return value.substring(8, 10) + "/" + value.substring(5, 7) + "/" + value.substring(0, 4);
}

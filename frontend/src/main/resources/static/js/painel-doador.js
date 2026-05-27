/* =========================
   Modal de cadastro de doação
   ========================= */

const botaoAbrir = document.getElementById("abrirModalDoacao");
const botaoFechar = document.getElementById("fecharModalDoacao");
const modalDoacao = document.getElementById("modalDoacao");

const camposFisico = document.querySelector(".campos-fisico");
const camposDinheiro = document.querySelector(".campos-dinheiro");
const opcoesTipo = document.querySelectorAll("input[name='tipoDoacao']");

const campoItem = document.getElementById("item");
const campoQuantidade = document.getElementById("quantidade");
const campoUnidade = document.getElementById("unidade");
const campoValor = document.getElementById("valor");
const formularioDoacao = document.querySelector(".modal-doacao");
const botaoEnviarDoacao = document.querySelector(".botao-enviar-doacao");


/* Abre o modal principal ao clicar em "Oferecer doação" */
botaoAbrir.addEventListener("click", function () {
    modalDoacao.classList.add("ativo");
});

/* Fecha o modal principal no botão X */
botaoFechar.addEventListener("click", function () {
    modalDoacao.classList.remove("ativo");
    formularioDoacao.classList.remove("formulario-tentou-enviar");
});

/*
 * Marca que o usuário tentou enviar o formulário.
 * O CSS usa essa classe para exibir mensagens de erro apenas após essa tentativa.
 */

botaoEnviarDoacao.addEventListener("click", function () {
    formularioDoacao.classList.add("formulario-tentou-enviar");
});

/* Mostra apenas os campos relacionados ao tipo de doação selecionado */
opcoesTipo.forEach(function (opcao) {
    opcao.addEventListener("change", function () {
        if (opcao.value === "fisico") {
            camposFisico.style.display = "block";
            camposDinheiro.style.display = "none";

            campoItem.required = true;
            campoQuantidade.required = true;
            campoUnidade.required = true;
            campoValor.required = false;
        }

        if (opcao.value === "dinheiro") {
            camposFisico.style.display = "none";
            camposDinheiro.style.display = "block";

            campoItem.required = false;
            campoQuantidade.required = false;
            campoUnidade.required = false;
            campoValor.required = true;
        }
    });
});

/* =========================
   Modal de confirmação de exclusão
   ========================= */

const modalConfirmacao = document.getElementById("modalConfirmacaoExclusao");
const botoesExcluir = document.querySelectorAll(".botao-excluir-doacao");
const botaoCancelarExclusao = document.getElementById("cancelarExclusao");
const inputIdDoacaoExcluir = document.getElementById("idDoacaoExcluir");
const nomeDoacaoExcluir = document.getElementById("nomeDoacaoExcluir");

/*
   A exclusão real é feita pelo backend.
   Aqui o JavaScript só preenche o id da doação e abre o modal de confirmação.
*/
botoesExcluir.forEach(function (botao) {
    botao.addEventListener("click", function () {
        const id = botao.getAttribute("data-id");
        const item = botao.getAttribute("data-item");

        inputIdDoacaoExcluir.value = id;
        nomeDoacaoExcluir.textContent = item;

        modalConfirmacao.classList.add("ativo");
    });
});

/* Fecha o modal de confirmação sem excluir */
botaoCancelarExclusao.addEventListener("click", function () {
    modalConfirmacao.classList.remove("ativo");
});

/* =========================
   Modal de detalhes da doação
   ========================= */

const modalDetalhes = document.getElementById("modalDetalhesDoacao");
const botoesVisualizar = document.querySelectorAll(".botao-visualizar-doacao");
const fecharDetalhes = document.getElementById("fecharDetalhesDoacao");

const detalheStatus = document.getElementById("detalheStatus");
const detalheItem = document.getElementById("detalheItem");
const detalheDestino = document.getElementById("detalheDestino");
const detalheMensagem = document.getElementById("detalheMensagem");
const detalheData = document.getElementById("detalheData");
const detalheObservacao = document.getElementById("detalheObservacao");
let pedidoSelecionado = {};

function separarMensagemEContato(texto) {
    const conteudo = texto || "";
    const partes = conteudo.split("|").map(function (parte) {
        return parte.trim();
    }).filter(Boolean);
    const mensagem = partes.find(function (parte) {
        return !parte.startsWith("Contato:")
            && !parte.startsWith("Nome:")
            && !parte.startsWith("E-mail:")
            && !parte.startsWith("Telefone:");
    });

    return mensagem || conteudo.replace(/Contato:.*$/, "").trim();
}

/*
   Os dados exibidos no modal vêm dos atributos data-* montados pelo Thymeleaf.
   Isso evita outra requisição apenas para visualizar informações já presentes na tela.
*/
botoesVisualizar.forEach(function (botao) {
    botao.addEventListener("click", function () {
        const status = botao.getAttribute("data-status");
        const destino = botao.getAttribute("data-destino");
        const mensagem = botao.getAttribute("data-mensagem");

        detalheStatus.textContent = status;
        detalheItem.textContent = botao.getAttribute("data-item");
        detalheDestino.textContent = destino;
        detalheMensagem.textContent = separarMensagemEContato(mensagem) || "Sem mensagem informada.";
        detalheData.textContent = botao.getAttribute("data-data");

        /*
           Reseta as classes do status para aplicar somente a cor correspondente
           ao status atual da doação.
        */
        detalheStatus.className = "status";

        if (status === "Solicitada") {
            detalheStatus.classList.add("solicitada");
            detalheObservacao.textContent = "Aguardando " + destino + " confirmar o recebimento...";
        }

        if (status === "Confirmada") {
            detalheStatus.classList.add("confirmada");
            detalheObservacao.textContent = "ONG aceitou. Hora de combinar a entrega.";
        }

        if (status === "Entregue") {
            detalheStatus.classList.add("entregue");
            detalheObservacao.textContent = "Doação entregue. Visualização disponível para consulta.";
        }

        if (status === "Recusada") {
            detalheStatus.classList.add("recusada");
            detalheObservacao.textContent = "A ONG recusou a doa\u00e7\u00e3o.";
        }

        modalDetalhes.classList.add("ativo");
    });
});

/* Fecha o modal de detalhes */
fecharDetalhes.addEventListener("click", function () {
    modalDetalhes.classList.remove("ativo");
});

/* =========================
   Fluxo para atender pedido de ONG
   ========================= */

const botoesAtenderPedido = document.querySelectorAll(".botao-atender-pedido");
const modalDetalhesPedido = document.getElementById("modalDetalhesPedido");
const fecharDetalhesPedido = document.getElementById("fecharDetalhesPedido");
const abrirAtendimentoPedido = document.getElementById("abrirAtendimentoPedido");

const detalhePedidoOng = document.getElementById("detalhePedidoOng");
const detalhePedidoCidade = document.getElementById("detalhePedidoCidade");
const detalhePedidoItem = document.getElementById("detalhePedidoItem");
const detalhePedidoQuantidade = document.getElementById("detalhePedidoQuantidade");
const detalhePedidoDescricao = document.getElementById("detalhePedidoDescricao");
const detalhePedidoData = document.getElementById("detalhePedidoData");

const modalAtenderPedido = document.getElementById("modalAtenderPedido");
const fecharAtenderPedido = document.getElementById("fecharAtenderPedido");
const formularioAtenderPedido = document.querySelector(".modal-atender-pedido");
const atenderPedidoId = document.getElementById("atenderPedidoId");
const atenderPedidoOng = document.getElementById("atenderPedidoOng");
const atenderPedidoItem = document.getElementById("atenderPedidoItem");
const atenderPedidoQuantidade = document.getElementById("atenderPedidoQuantidade");
const atenderPedidoDescricao = document.getElementById("atenderPedidoDescricao");

botoesAtenderPedido.forEach(function (botao) {
    botao.addEventListener("click", function () {
        pedidoSelecionado = {
            id: botao.getAttribute("data-id"),
            ong: botao.getAttribute("data-ong"),
            cidade: botao.getAttribute("data-cidade"),
            item: botao.getAttribute("data-item"),
            quantidade: botao.getAttribute("data-quantidade"),
            descricao: botao.getAttribute("data-descricao"),
            data: botao.getAttribute("data-data")
        };

        detalhePedidoOng.textContent = pedidoSelecionado.ong;
        detalhePedidoCidade.textContent = pedidoSelecionado.cidade || "Não informada";
        detalhePedidoItem.textContent = pedidoSelecionado.item;
        detalhePedidoQuantidade.textContent = pedidoSelecionado.quantidade || "Não informada.";
        detalhePedidoDescricao.textContent = pedidoSelecionado.descricao || "Sem descrição informada.";
        detalhePedidoData.textContent = pedidoSelecionado.data;

        modalDetalhesPedido.classList.add("ativo");
    });
});

if (fecharDetalhesPedido && modalDetalhesPedido) {
    fecharDetalhesPedido.addEventListener("click", function () {
        modalDetalhesPedido.classList.remove("ativo");
    });
}

if (abrirAtendimentoPedido && modalAtenderPedido) {
    abrirAtendimentoPedido.addEventListener("click", function () {
        atenderPedidoId.value = pedidoSelecionado.id;
        atenderPedidoOng.textContent = pedidoSelecionado.ong;
        atenderPedidoItem.textContent = pedidoSelecionado.item;
        atenderPedidoQuantidade.textContent = pedidoSelecionado.quantidade || "Não informada.";
        atenderPedidoDescricao.textContent = pedidoSelecionado.descricao || "Sem descrição informada.";

        modalDetalhesPedido.classList.remove("ativo");
        modalAtenderPedido.classList.add("ativo");
    });
}

if (fecharAtenderPedido && modalAtenderPedido) {
    fecharAtenderPedido.addEventListener("click", function () {
        modalAtenderPedido.classList.remove("ativo");
        formularioAtenderPedido.classList.remove("formulario-tentou-enviar");
    });
}

if (formularioAtenderPedido) {
    formularioAtenderPedido.addEventListener("submit", function () {
        formularioAtenderPedido.classList.add("formulario-tentou-enviar");
    });
}

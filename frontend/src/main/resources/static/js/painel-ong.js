const botaoAbrirPedido = document.getElementById("abrirModalPedido");
const botaoFecharPedido = document.getElementById("fecharModalPedido");
const modalPedido = document.getElementById("modalPedido");
const formularioPedido = document.querySelector(".modal-doacao");
const botaoEnviarPedido = document.querySelector(".botao-enviar-doacao");
const opcoesTipoPedido = document.querySelectorAll("input[name='tipoPedido']");
const camposPedidoFisico = document.querySelector(".campos-pedido-fisico");
const camposPedidoDinheiro = document.querySelector(".campos-pedido-dinheiro");

const pedidoItem = document.getElementById("pedidoItem");
const pedidoQuantidade = document.getElementById("pedidoQuantidade");
const pedidoDescricao = document.getElementById("pedidoDescricao");

const pedidoFisicoItem = document.getElementById("pedidoFisicoItem");
const pedidoFisicoQuantidade = document.getElementById("pedidoFisicoQuantidade");
const pedidoFisicoUnidade = document.getElementById("pedidoFisicoUnidade");
const pedidoFisicoDescricao = document.getElementById("pedidoFisicoDescricao");

const pedidoDinheiroValor = document.getElementById("pedidoDinheiroValor");
const pedidoDinheiroFinalidade = document.getElementById("pedidoDinheiroFinalidade");
const pedidoDinheiroDescricao = document.getElementById("pedidoDinheiroDescricao");

function alternarObrigatoriedade(campos, obrigatorio) {
    campos.forEach(function (campo) {
        if (campo) {
            campo.required = obrigatorio;
        }
    });
}

function limparModalPedido() {
    if (!formularioPedido) {
        return;
    }

    formularioPedido.reset();
    formularioPedido.classList.remove("formulario-tentou-enviar");

    if (camposPedidoFisico) {
        camposPedidoFisico.style.display = "none";
    }

    if (camposPedidoDinheiro) {
        camposPedidoDinheiro.style.display = "none";
    }

    alternarObrigatoriedade([pedidoFisicoItem, pedidoFisicoQuantidade, pedidoFisicoUnidade], false);
    alternarObrigatoriedade([pedidoDinheiroValor, pedidoDinheiroFinalidade], false);

    if (pedidoItem) {
        pedidoItem.value = "";
    }

    if (pedidoQuantidade) {
        pedidoQuantidade.value = "";
    }

    if (pedidoDescricao) {
        pedidoDescricao.value = "";
    }
}

function alternarTipoPedido(tipo) {
    const pedidoFisico = tipo === "fisico";
    const pedidoDinheiro = tipo === "dinheiro";

    if (camposPedidoFisico) {
        camposPedidoFisico.style.display = pedidoFisico ? "block" : "none";
    }

    if (camposPedidoDinheiro) {
        camposPedidoDinheiro.style.display = pedidoDinheiro ? "block" : "none";
    }

    alternarObrigatoriedade([pedidoFisicoItem, pedidoFisicoQuantidade, pedidoFisicoUnidade], pedidoFisico);
    alternarObrigatoriedade([pedidoDinheiroValor, pedidoDinheiroFinalidade], pedidoDinheiro);
}

opcoesTipoPedido.forEach(function (opcao) {
    opcao.addEventListener("change", function () {
        alternarTipoPedido(opcao.value);
    });

    opcao.addEventListener("click", function () {
        alternarTipoPedido(opcao.value);
    });
});

if (botaoAbrirPedido && botaoFecharPedido && modalPedido) {
    botaoAbrirPedido.addEventListener("click", function () {
        limparModalPedido();
        modalPedido.classList.add("ativo");
    });

    botaoFecharPedido.addEventListener("click", function () {
        modalPedido.classList.remove("ativo");
        limparModalPedido();
    });
}

if (botaoEnviarPedido && formularioPedido) {
    botaoEnviarPedido.addEventListener("click", function () {
        formularioPedido.classList.add("formulario-tentou-enviar");
    });
}

if (formularioPedido) {
    formularioPedido.addEventListener("submit", function () {
        const tipoSelecionado = document.querySelector("input[name='tipoPedido']:checked");

        if (!tipoSelecionado) {
            return;
        }

        if (tipoSelecionado.value === "fisico") {
            const quantidade = pedidoFisicoQuantidade.value.trim();
            const unidade = pedidoFisicoUnidade.value.trim();

            pedidoItem.value = pedidoFisicoItem.value.trim();
            pedidoQuantidade.value = unidade ? `${quantidade} ${unidade}` : quantidade;
            pedidoDescricao.value = pedidoFisicoDescricao.value.trim();
        }

        if (tipoSelecionado.value === "dinheiro") {
            pedidoItem.value = pedidoDinheiroFinalidade.value.trim();
            pedidoQuantidade.value = pedidoDinheiroValor.value.trim();
            pedidoDescricao.value = pedidoDinheiroDescricao.value.trim();
        }
    });
}

/* =========================
   Modal de confirmação de exclusão de pedido
   ========================= */

const modalConfirmacaoExclusaoPedido = document.getElementById("modalConfirmacaoExclusaoPedido");
const botoesExcluirPedido = document.querySelectorAll(".botao-excluir-pedido");
const botaoCancelarExclusaoPedido = document.getElementById("cancelarExclusaoPedido");
const inputIdPedidoExcluir = document.getElementById("idPedidoExcluir");
const nomePedidoExcluir = document.getElementById("nomePedidoExcluir");

botoesExcluirPedido.forEach(function (botao) {
    botao.addEventListener("click", function () {
        inputIdPedidoExcluir.value = botao.getAttribute("data-id");
        nomePedidoExcluir.textContent = botao.getAttribute("data-item");
        modalConfirmacaoExclusaoPedido.classList.add("ativo");
    });
});

if (botaoCancelarExclusaoPedido && modalConfirmacaoExclusaoPedido) {
    botaoCancelarExclusaoPedido.addEventListener("click", function () {
        modalConfirmacaoExclusaoPedido.classList.remove("ativo");
    });
}

function aplicarStatus(elemento, status) {
    elemento.className = "status";

    if (status === "Disponível" || status === "Disponivel") {
        elemento.classList.add("disponivel");
    }

    if (status === "Solicitada" || status === "Pendente") {
        elemento.classList.add("pendente");
    }

    if (status === "Confirmada") {
        elemento.classList.add("confirmada");
    }

    if (status === "Entregue") {
        elemento.classList.add("entregue");
    }

    if (status === "Removido") {
        elemento.classList.add("removido");
    }

    if (status === "Recusada") {
        elemento.classList.add("recusada");
    }
}

/* =========================
   Modal de detalhes da doação
   ========================= */

const modalDetalhesDoacao = document.getElementById("modalDetalhesDoacaoOng");
const fecharDetalhesDoacao = document.getElementById("fecharDetalhesDoacaoOng");
const botoesVisualizarDoacao = document.querySelectorAll(".botao-visualizar-doacao-ong");

const detalheDoacaoStatus = document.getElementById("detalheDoacaoStatus");
const detalheDoacaoItem = document.getElementById("detalheDoacaoItem");
const detalheDoacaoOrigem = document.getElementById("detalheDoacaoOrigem");
const detalheDoacaoMensagem = document.getElementById("detalheDoacaoMensagem");
const detalheDoacaoData = document.getElementById("detalheDoacaoData");
const detalheDoacaoContatoNome = document.getElementById("detalheDoacaoContatoNome");
const detalheDoacaoContatoEmail = document.getElementById("detalheDoacaoContatoEmail");
const detalheDoacaoContatoTelefone = document.getElementById("detalheDoacaoContatoTelefone");
const contatoDoadorBloco = document.getElementById("contatoDoadorBloco");
const tituloContatoDoador = document.getElementById("tituloContatoDoador");
const detalheDoacaoObservacao = document.getElementById("detalheDoacaoObservacao");
const acoesDoacaoSolicitada = document.getElementById("acoesDoacaoSolicitada");
const botaoAbrirConfirmacaoRecusa = document.getElementById("abrirConfirmacaoRecusa");
const modalConfirmacaoRecusa = document.getElementById("modalConfirmacaoRecusa");
const fecharConfirmacaoRecusa = document.getElementById("fecharConfirmacaoRecusa");
const botaoMarcarEntregue = document.getElementById("abrirConfirmacaoEntrega");
const modalConfirmacaoEntrega = document.getElementById("modalConfirmacaoEntrega");
const fecharConfirmacaoEntrega = document.getElementById("fecharConfirmacaoEntrega");
const idDoacaoRecusar = document.getElementById("idDoacaoRecusar");
const idDoacaoConfirmar = document.getElementById("idDoacaoConfirmar");
const idDoacaoEntregar = document.getElementById("idDoacaoEntregar");

function separarMensagemEContato(texto) {
    const conteudo = texto || "";
    const partes = conteudo.split("|").map(function (parte) {
        return parte.trim();
    }).filter(Boolean);

    const resultado = {
        mensagem: conteudo.trim(),
        nome: "",
        email: "",
        telefone: ""
    };

    partes.forEach(function (parte) {
        if (parte.startsWith("Contato:")) {
            const contato = parte.replace("Contato:", "").split(",").map(function (item) {
                return item.trim();
            });
            resultado.nome = contato[0] || "";
            resultado.email = contato[1] || "";
            resultado.telefone = contato[2] || "";
        } else if (parte.startsWith("Nome:")) {
            resultado.nome = parte.replace("Nome:", "").trim();
        } else if (parte.startsWith("E-mail:")) {
            resultado.email = parte.replace("E-mail:", "").trim();
        } else if (parte.startsWith("Telefone:")) {
            resultado.telefone = parte.replace("Telefone:", "").trim();
        } else if (!parte.startsWith("Contato")) {
            resultado.mensagem = parte;
        }
    });

    if (!resultado.nome && !resultado.email && !resultado.telefone) {
        resultado.mensagem = conteudo.trim();
    }

    return resultado;
}

botoesVisualizarDoacao.forEach(function (botao) {
    botao.addEventListener("click", function () {
        const id = botao.getAttribute("data-id");
        const status = botao.getAttribute("data-status");
        const mensagem = botao.getAttribute("data-mensagem");
        const dadosMensagem = separarMensagemEContato(mensagem);

        detalheDoacaoStatus.textContent = status;
        detalheDoacaoItem.textContent = botao.getAttribute("data-item");
        detalheDoacaoOrigem.textContent = botao.getAttribute("data-origem");
        detalheDoacaoMensagem.textContent = dadosMensagem.mensagem || "Sem mensagem informada.";
        detalheDoacaoData.textContent = botao.getAttribute("data-data");
        detalheDoacaoContatoNome.textContent = dadosMensagem.nome || "Não informado.";
        detalheDoacaoContatoEmail.textContent = dadosMensagem.email || "Não informado.";
        detalheDoacaoContatoTelefone.textContent = dadosMensagem.telefone || "Não informado.";

        if (dadosMensagem.nome || dadosMensagem.email || dadosMensagem.telefone) {
            contatoDoadorBloco.classList.add("ativo");
            tituloContatoDoador.style.display = "block";
        } else {
            contatoDoadorBloco.classList.remove("ativo");
            tituloContatoDoador.style.display = "none";
        }

        aplicarStatus(detalheDoacaoStatus, status);
        idDoacaoRecusar.value = id;
        idDoacaoConfirmar.value = id;
        idDoacaoEntregar.value = id;
        acoesDoacaoSolicitada.style.display = "none";
        botaoMarcarEntregue.style.display = "none";

        if (status === "Disponível" || status === "Disponivel") {
            detalheDoacaoObservacao.textContent = "Esta doação está disponível para sua ONG acompanhar.";
        }

        if (status === "Pendente") {
            detalheDoacaoObservacao.textContent = "Revise a oferta do doador e escolha se deseja aceitar.";
            acoesDoacaoSolicitada.style.display = "flex";
        }

        if (status === "Confirmada") {
            detalheDoacaoObservacao.textContent = "Doação confirmada. Combine os próximos passos com o doador.";
            botaoMarcarEntregue.style.display = "block";
        }

        if (status === "Entregue") {
            detalheDoacaoObservacao.textContent = "Doação entregue. Os detalhes ficam disponíveis para consulta.";
        }

        if (status === "Recusada") {
            detalheDoacaoObservacao.textContent = "Doação recusada. Esta oferta fica disponível apenas para consulta.";
        }

        modalDetalhesDoacao.classList.add("ativo");
    });
});

if (fecharDetalhesDoacao && modalDetalhesDoacao) {
    fecharDetalhesDoacao.addEventListener("click", function () {
        modalDetalhesDoacao.classList.remove("ativo");
    });
}

if (botaoAbrirConfirmacaoRecusa && modalConfirmacaoRecusa) {
    botaoAbrirConfirmacaoRecusa.addEventListener("click", function () {
        modalDetalhesDoacao.classList.remove("ativo");
        modalConfirmacaoRecusa.classList.add("ativo");
    });
}

if (fecharConfirmacaoRecusa && modalConfirmacaoRecusa) {
    fecharConfirmacaoRecusa.addEventListener("click", function () {
        modalConfirmacaoRecusa.classList.remove("ativo");
    });
}

if (botaoMarcarEntregue && modalConfirmacaoEntrega) {
    botaoMarcarEntregue.addEventListener("click", function () {
        modalDetalhesDoacao.classList.remove("ativo");
        modalConfirmacaoEntrega.classList.add("ativo");
    });
}

if (fecharConfirmacaoEntrega && modalConfirmacaoEntrega) {
    fecharConfirmacaoEntrega.addEventListener("click", function () {
        modalConfirmacaoEntrega.classList.remove("ativo");
    });
}

/* =========================
   Modal de detalhes do pedido
   ========================= */

const modalDetalhesPedido = document.getElementById("modalDetalhesPedidoOng");
const fecharDetalhesPedido = document.getElementById("fecharDetalhesPedidoOng");
const botoesVisualizarPedido = document.querySelectorAll(".botao-visualizar-pedido-ong");

const detalhePedidoStatus = document.getElementById("detalhePedidoStatus");
const detalhePedidoItem = document.getElementById("detalhePedidoItem");
const detalhePedidoQuantidade = document.getElementById("detalhePedidoQuantidade");
const detalhePedidoDescricao = document.getElementById("detalhePedidoDescricao");
const detalhePedidoData = document.getElementById("detalhePedidoData");
const detalhePedidoObservacao = document.getElementById("detalhePedidoObservacao");

botoesVisualizarPedido.forEach(function (botao) {
    botao.addEventListener("click", function () {
        const status = botao.getAttribute("data-status");
        const descricao = botao.getAttribute("data-descricao");

        detalhePedidoStatus.textContent = status;
        detalhePedidoItem.textContent = botao.getAttribute("data-item");
        detalhePedidoQuantidade.textContent = botao.getAttribute("data-quantidade") || "Não informada.";
        detalhePedidoDescricao.textContent = descricao || "Sem descrição informada.";
        detalhePedidoData.textContent = botao.getAttribute("data-data");

        aplicarStatus(detalhePedidoStatus, status);

        if (status === "Disponível" || status === "Disponivel") {
            detalhePedidoObservacao.textContent = "Seu pedido está visível para doadores encontrarem.";
        }

        if (status === "Solicitada" || status === "Pendente") {
            detalhePedidoObservacao.textContent = "Existe uma doação relacionada aguardando confirmação.";
        }

        if (status === "Confirmada") {
            detalhePedidoObservacao.textContent = "Pedido confirmado. Acompanhe a combinação da entrega.";
        }

        if (status === "Entregue") {
            detalhePedidoObservacao.textContent = "Pedido atendido. Os detalhes ficam disponíveis para consulta.";
        }

        modalDetalhesPedido.classList.add("ativo");
    });
});

if (fecharDetalhesPedido && modalDetalhesPedido) {
    fecharDetalhesPedido.addEventListener("click", function () {
        modalDetalhesPedido.classList.remove("ativo");
    });
}

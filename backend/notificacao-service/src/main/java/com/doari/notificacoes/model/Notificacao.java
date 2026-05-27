package com.doari.notificacoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "destinatario_tipo", nullable = false)
    private DestinatarioTipo destinatarioTipo;

    @Column(name = "destinatario_id", nullable = false)
    private Long destinatarioId;

    @Column(name = "doacao_id")
    private Long doacaoId;

    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(name = "mensagem", nullable = false, length = 1000)
    private String mensagem;

    @Column(name = "lida", nullable = false)
    private boolean lida;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm = Instant.now();

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public DestinatarioTipo getDestinatarioTipo() {
        return destinatarioTipo;
    }

    public void setDestinatarioTipo(DestinatarioTipo destinatarioTipo) {
        this.destinatarioTipo = destinatarioTipo;
    }

    public Long getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(Long destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public Long getDoacaoId() {
        return doacaoId;
    }

    public void setDoacaoId(Long doacaoId) {
        this.doacaoId = doacaoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}

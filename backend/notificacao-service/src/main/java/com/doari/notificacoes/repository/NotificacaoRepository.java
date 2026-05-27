package com.doari.notificacoes.repository;

import com.doari.notificacoes.model.DestinatarioTipo;
import com.doari.notificacoes.model.Notificacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByDestinatarioTipoAndDestinatarioIdOrderByCriadaEmDesc(
            DestinatarioTipo destinatarioTipo,
            Long destinatarioId
    );
}

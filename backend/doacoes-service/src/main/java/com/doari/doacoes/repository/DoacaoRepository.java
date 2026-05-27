package com.doari.doacoes.repository;

import com.doari.doacoes.model.Doacao;
import com.doari.doacoes.model.StatusItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    List<Doacao> findByUsuarioIdAndStatus(Long usuarioId, StatusItem status);

    List<Doacao> findByUsuarioIdAndStatusNot(Long usuarioId, StatusItem status);

    List<Doacao> findByUsuarioIdAndPedidoIdIsNotNullAndStatusNot(Long usuarioId, StatusItem status);

    List<Doacao> findByOngIdAndStatusNot(Long ongId, StatusItem status);

    List<Doacao> findByStatus(StatusItem status);

    List<Doacao> findByItemIgnoreCaseAndStatus(String item, StatusItem status);
}

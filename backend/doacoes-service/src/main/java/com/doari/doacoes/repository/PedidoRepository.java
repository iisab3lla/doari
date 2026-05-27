package com.doari.doacoes.repository;

import com.doari.doacoes.model.Pedido;
import com.doari.doacoes.model.StatusItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByOngIdAndStatus(Long ongId, StatusItem status);

    List<Pedido> findByOngIdAndStatusNot(Long ongId, StatusItem status);

    List<Pedido> findByStatus(StatusItem status);

    List<Pedido> findByItemIgnoreCaseAndStatus(String item, StatusItem status);
}

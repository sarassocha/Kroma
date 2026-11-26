package com.kroma.ecommerce.model.repository;

import com.kroma.ecommerce.model.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findAllByOrderByFechaPedidoDesc();
    List<Pedido> findByClienteIdOrDatosClienteCorreoIgnoreCaseOrderByFechaPedidoDesc(String clienteId, String correo);
    List<Pedido> findByDatosClienteCorreoIgnoreCaseOrderByFechaPedidoDesc(String correo);
}

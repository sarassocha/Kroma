package com.kroma.ecommerce.model.repository;

import com.kroma.ecommerce.model.GarantiaTono;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GarantiaTonoRepository extends MongoRepository<GarantiaTono, String> {
    List<GarantiaTono> findAllByOrderByFechaSolicitudDesc();
    List<GarantiaTono> findByClienteIdOrCorreoClienteIgnoreCaseOrderByFechaSolicitudDesc(String clienteId, String correo);
    List<GarantiaTono> findByCorreoClienteIgnoreCaseOrderByFechaSolicitudDesc(String correo);
}

package com.kroma.ecommerce.model.repository;

import com.kroma.ecommerce.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductoRepository extends MongoRepository<Producto, String> {
    List<Producto> findByActivoTrueOrderByDestacadoDescFechaCreacionDesc();
    List<Producto> findAllByOrderByFechaCreacionDesc();
}

package com.kroma.ecommerce.model.repository;

import com.kroma.ecommerce.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
}

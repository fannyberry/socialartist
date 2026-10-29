package com.sally.socialartist.repository;

import com.sally.socialartist.model.Publicacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    // EntityGraph trae el usuario en el mismo SELECT (evita una consulta por publicacion)
    @EntityGraph(attributePaths = "usuario")
    List<Publicacion> findAllByOrderByFechaPublicacionDescIdDesc();
}

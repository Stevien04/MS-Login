package app.repository;

import app.model.Administrativo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministrativoRepository extends JpaRepository<Administrativo, Integer> {

    @EntityGraph(attributePaths = {"escuela"})
    Optional<Administrativo> findByUsuario_IdUsuario(Integer usuarioId);
}
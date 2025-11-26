package app.repository;

import java.util.Optional;
import app.model.Estudiante;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
    @EntityGraph(attributePaths = {"escuela"})
    Optional<Estudiante> findByUsuario_IdUsuario(Integer idUsuario);

@EntityGraph(attributePaths = {"escuela"})
Optional<Estudiante> findByCodigoIgnoreCase(String codigo);
}
package app.repository;

import java.util.Optional;
import app.model.UsuarioAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioAuthRepository extends JpaRepository<UsuarioAuth, Integer> {
    Optional<UsuarioAuth> findByCorreoU(String correoU);
    Optional<UsuarioAuth> findByCorreoUIgnoreCaseOrUsuario_NumDoc(String correoU, String numDoc);
    Optional<UsuarioAuth> findBySesionToken(String sesionToken);

    Optional<UsuarioAuth> findByUsuario_IdUsuario(Integer idUsuario);
}
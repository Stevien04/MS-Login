package app.service;

import app.dto.LoginRequest;
import app.dto.LoginResponse;
import app.dto.LogoutRequest;
import app.dto.PerfilResponse;
import app.dto.SessionValidationRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import app.model.Usuario;
import app.model.UsuarioAuth;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import app.model.Estudiante;
import app.repository.EstudianteRepository;
import app.model.Administrativo;
import app.repository.UsuarioAuthRepository;
import app.repository.AdministrativoRepository;

@Service
public class AuthService {

    private static final long SESSION_INACTIVITY_MINUTES = 20L;
    private static final Set<Integer> ACADEMIC_ROLES = Set.of(1, 2);
    private static final Set<Integer> ADMINISTRATIVE_ROLES = Set.of(3, 4);

    private final UsuarioAuthRepository authRepository;
    private final EstudianteRepository estudianteRepository;
    private final AdministrativoRepository administrativoRepository;

    public AuthService(UsuarioAuthRepository authRepository,
                       EstudianteRepository estudianteRepository,
                       AdministrativoRepository administrativoRepository) {
        this.authRepository = authRepository;
        this.estudianteRepository = estudianteRepository;
        this.administrativoRepository = administrativoRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            return LoginResponse.failure("La solicitud de autenticación es inválida.", HttpStatus.BAD_REQUEST);
        }

        String identifier = request.identifier();
        String password = request.getPassword() != null ? request.getPassword().trim() : "";
        String tipoLogin = request.normalizedTipoLogin();

        if (!StringUtils.hasText(identifier)) {
            return LoginResponse.failure(
                    "Debes ingresar tu código universitario o correo electrónico.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!StringUtils.hasText(password)) {
            return LoginResponse.failure("Debes ingresar tu contraseña institucional.", HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(tipoLogin)) {
            return LoginResponse.failure("Debes seleccionar un tipo de acceso válido.", HttpStatus.BAD_REQUEST);
        }

        boolean isAcademicLogin = "academic".equals(tipoLogin);
        boolean isAdministrativeLogin = "administrative".equals(tipoLogin);

        if (!isAcademicLogin && !isAdministrativeLogin) {
            return LoginResponse.failure("El tipo de acceso seleccionado no es válido.", HttpStatus.BAD_REQUEST);
        }

        Optional<UsuarioAuth> authOpt = authRepository
                .findByCorreoUIgnoreCaseOrUsuario_NumDoc(identifier, identifier);

        if (authOpt.isEmpty()) {
            return LoginResponse.failure(
                    "No encontramos una cuenta vinculada a las credenciales proporcionadas.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        UsuarioAuth auth = authOpt.get();

        if (!StringUtils.hasText(auth.password)) {
            return LoginResponse.failure(
                    "Las credenciales almacenadas son inválidas. Contacta al administrador.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        String storedPassword;
        try {
            storedPassword = new String(Base64.getDecoder().decode(auth.password), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return LoginResponse.failure(
                    "No fue posible validar tus credenciales. Contacta al administrador.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        if (!storedPassword.equals(password)) {
            return LoginResponse.failure("La contraseña es incorrecta. Intenta nuevamente.", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = auth.usuario;
        if (usuario == null) {
            return LoginResponse.failure(
                    "Tu cuenta no tiene un perfil asociado. Contacta al administrador.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        Integer rolId = (usuario.rol != null) ? usuario.rol.idRol : null;
        if (rolId == null) {
            return LoginResponse.failure(
                    "Tu cuenta no tiene un rol asignado. Contacta al administrador.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (isAcademicLogin && !ACADEMIC_ROLES.contains(rolId)) {
            return LoginResponse.failure(
                    "Acceso denegado. El portal académico es exclusivo para estudiantes y docentes.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (isAdministrativeLogin && !ADMINISTRATIVE_ROLES.contains(rolId)) {
            return LoginResponse.failure(
                    "Acceso denegado. El portal administrativo es exclusivo para personal autorizado.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (usuario.estado != null && usuario.estado != 1) {
            return LoginResponse.failure(
                    "Tu cuenta se encuentra inactiva. Contacta al administrador.",
                    HttpStatus.FORBIDDEN
            );
        }

        boolean hadSessionData = StringUtils.hasText(auth.sesionToken);
        if (isSessionActive(auth)) {
            return LoginResponse.failure(
                    "Tu cuenta ya tiene una sesión activa. Cierra la sesión anterior antes de iniciar una nueva.",
                    HttpStatus.CONFLICT
            );
        }

        if (hadSessionData && !StringUtils.hasText(auth.sesionToken)) {
            authRepository.save(auth);
        }

        String loginType = isAcademicLogin ? "academic" : "administrative";
        PerfilResponse perfil = construirPerfil(auth, usuario, loginType, rolId, identifier);

        if (perfil == null) {
            return LoginResponse.failure(
                    "No fue posible construir tu perfil. Contacta al administrador.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        String token = iniciarNuevaSesion(auth, loginType);
        String message = isAcademicLogin
                ? "Login académico exitoso"
                : "Login administrativo exitoso";

        return LoginResponse.success(message, token, perfil);
    }

    @Transactional
    public LoginResponse validateSession(SessionValidationRequest request) {
        if (request == null) {
            return LoginResponse.failure("La solicitud de validación es inválida.", HttpStatus.BAD_REQUEST);
        }

        String token = request.normalizedToken();
        if (!StringUtils.hasText(token)) {
            return LoginResponse.failure("Debes proporcionar un token de sesión válido.", HttpStatus.BAD_REQUEST);
        }

        Optional<UsuarioAuth> authOpt = authRepository.findBySesionToken(token);
        if (authOpt.isEmpty()) {
            return LoginResponse.failure("La sesión no es válida o ha expirado.", HttpStatus.UNAUTHORIZED);
        }

        UsuarioAuth auth = authOpt.get();
        boolean hadSessionData = StringUtils.hasText(auth.sesionToken);
        if (!isSessionActive(auth)) {
            if (hadSessionData && !StringUtils.hasText(auth.sesionToken)) {
                authRepository.save(auth);
            }
            return LoginResponse.failure("La sesión ha expirado. Inicia sesión nuevamente.", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = auth.usuario;
        if (usuario == null) {
            limpiarSesion(auth);
            authRepository.save(auth);
            return LoginResponse.failure(
                    "Tu cuenta no tiene un perfil asociado. Contacta al administrador.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        Integer rolId = (usuario.rol != null) ? usuario.rol.idRol : null;
        if (rolId == null) {
            limpiarSesion(auth);
            authRepository.save(auth);
            return LoginResponse.failure(
                    "Tu cuenta no tiene un rol asignado. Contacta al administrador.",
                    HttpStatus.FORBIDDEN
            );
        }

        String storedLoginType = StringUtils.hasText(auth.sesionTipo)
                ? auth.sesionTipo.trim().toLowerCase()
                : "";
        boolean isAcademicLogin = "academic".equals(storedLoginType);
        boolean isAdministrativeLogin = "administrative".equals(storedLoginType);

        if (!isAcademicLogin && !isAdministrativeLogin) {
            limpiarSesion(auth);
            authRepository.save(auth);
            return LoginResponse.failure(
                    "La sesión almacenada es inválida. Inicia sesión nuevamente.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (isAcademicLogin && !ACADEMIC_ROLES.contains(rolId)) {
            limpiarSesion(auth);
            authRepository.save(auth);
            return LoginResponse.failure(
                    "Acceso denegado para el portal académico.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (isAdministrativeLogin && !ADMINISTRATIVE_ROLES.contains(rolId)) {
            limpiarSesion(auth);
            authRepository.save(auth);
            return LoginResponse.failure(
                    "Acceso denegado para el portal administrativo.",
                    HttpStatus.FORBIDDEN
            );
        }

        PerfilResponse perfil = construirPerfil(
                auth,
                usuario,
                isAcademicLogin ? "academic" : "administrative",
                rolId,
                usuario.numDoc
        );

        if (perfil == null) {
            limpiarSesion(auth);
            authRepository.save(auth);
            return LoginResponse.failure(
                    "No fue posible reconstruir tu perfil. Inicia sesión nuevamente.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        auth.sesionExpira = LocalDateTime.now().plusMinutes(SESSION_INACTIVITY_MINUTES);
        auth.ultimoLogin = LocalDateTime.now();
        authRepository.save(auth);

        return LoginResponse.success("Sesión válida", auth.sesionToken, perfil);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        if (request == null) return;
        Integer usuarioId = request.getUsuarioId();
        if (usuarioId == null) return;

        authRepository.findByUsuario_IdUsuario(usuarioId).ifPresent(auth -> {
            String providedToken = request.normalizedToken();
            if (StringUtils.hasText(providedToken)
                    && StringUtils.hasText(auth.sesionToken)
                    && !auth.sesionToken.equals(providedToken)) {
                return;
            }
            limpiarSesion(auth);
            authRepository.save(auth);
        });
    }

    private PerfilResponse construirPerfil(UsuarioAuth auth,
                                           Usuario usuario,
                                           String loginType,
                                           Integer rolId,
                                           String identifier) {
        if (auth == null || usuario == null || !StringUtils.hasText(loginType)) {
            return null;
        }

        boolean academicLogin = "academic".equalsIgnoreCase(loginType);

        PerfilResponse perfil = new PerfilResponse();
        perfil.id = usuario.idUsuario != null ? usuario.idUsuario.toString() : null;
        perfil.codigo = usuario.numDoc;
        perfil.email = auth.correoU;
        perfil.nombres = usuario.nombre;
        perfil.apellidos = usuario.apellido;
        perfil.rol = (usuario.rol != null) ? usuario.rol.nombre : null;
        perfil.tipoLogin = loginType;
        perfil.avatarUrl = null;

        if (rolId != null && rolId == 2) {
            Optional<Estudiante> estudianteOpt = buscarEstudiante(usuario, identifier);
            estudianteOpt.ifPresent(estudiante -> {
                if (StringUtils.hasText(estudiante.codigo)) {
                    perfil.codigo = estudiante.codigo;
                }
                if (estudiante.escuela != null) {
                    if (estudiante.escuela.idEscuela != null) {
                        perfil.escuelaId = estudiante.escuela.idEscuela;
                    }
                    if (StringUtils.hasText(estudiante.escuela.nombre)) {
                        perfil.escuelaNombre = estudiante.escuela.nombre;
                    }
                }
            });
        }

        if (rolId != null && ADMINISTRATIVE_ROLES.contains(rolId)) {
            administrativoRepository.findByUsuario_IdUsuario(usuario.idUsuario)
                    .ifPresent(administrativo -> asignarEscuelaAdministrador(perfil, administrativo));
        }

        if (!StringUtils.hasText(perfil.codigo)) {
            perfil.codigo = usuario.numDoc;
        }

        if (academicLogin && !StringUtils.hasText(perfil.tipoLogin)) {
            perfil.tipoLogin = "academic";
        }

        return perfil;
    }

    private String iniciarNuevaSesion(UsuarioAuth auth, String loginType) {
        if (auth == null) return null;
        String token = UUID.randomUUID().toString();
        auth.sesionToken = token;
        auth.sesionExpira = LocalDateTime.now().plusMinutes(SESSION_INACTIVITY_MINUTES);
        auth.sesionTipo = loginType;
        auth.ultimoLogin = LocalDateTime.now();
        authRepository.save(auth);
        return token;
    }

    private boolean isSessionActive(UsuarioAuth auth) {
        if (auth == null) return false;
        if (!StringUtils.hasText(auth.sesionToken)) {
            limpiarSesion(auth);
            return false;
        }
        LocalDateTime expiresAt = auth.sesionExpira;
        if (expiresAt == null) {
            limpiarSesion(auth);
            return false;
        }
        if (expiresAt.isAfter(LocalDateTime.now())) return true;
        limpiarSesion(auth);
        return false;
    }

    private void limpiarSesion(UsuarioAuth auth) {
        if (auth == null) return;
        auth.sesionToken = null;
        auth.sesionExpira = null;
        auth.sesionTipo = null;
    }

    private void asignarEscuelaAdministrador(PerfilResponse perfil, Administrativo administrativo) {
        if (perfil == null || administrativo == null) return;
        if (administrativo.escuela != null) {
            if (administrativo.escuela.idEscuela != null) {
                perfil.escuelaId = administrativo.escuela.idEscuela;
            }
            if (StringUtils.hasText(administrativo.escuela.nombre)) {
                perfil.escuelaNombre = administrativo.escuela.nombre;
            }
        }
    }

    private Optional<Estudiante> buscarEstudiante(Usuario usuario, String identifier) {
        if (usuario == null) return Optional.empty();

        Optional<Estudiante> estudianteOpt = Optional.empty();

        if (usuario.idUsuario != null) {
            estudianteOpt = estudianteRepository.findByUsuario_IdUsuario(usuario.idUsuario);
        }

        if (estudianteOpt.isPresent()) return estudianteOpt;

        if (StringUtils.hasText(identifier)) {
            String trimmed = identifier.trim();
            estudianteOpt = estudianteRepository.findByCodigoIgnoreCase(trimmed);
        }

        return estudianteOpt;
    }
}

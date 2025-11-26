package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_auth")
public class UsuarioAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAuth")
    public Integer idAuth;

    @OneToOne
    @JoinColumn(name = "IdUsuario", nullable = false, unique = true)
    public Usuario usuario;

    @Column(name = "CorreoU", nullable = false, unique = true, length = 30)
    public String correoU;

    @Column(name = "Password", nullable = false, length = 255)
    public String password;

    @Column(name = "UltimoLogin")
    public LocalDateTime ultimoLogin;

    @Column(name = "SesionToken", length = 255)
    public String sesionToken;

    @Column(name = "SesionExpira")
    public LocalDateTime sesionExpira;

    @Column(name = "SesionTipo", length = 20)
    public String sesionTipo;
}
package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdUsuario")
    public Integer idUsuario;

    @Column(name = "Nombre", nullable = false, length = 30)
    public String nombre;

    @Column(name = "Apellido", nullable = false, length = 30)
    public String apellido;

    @Column(name = "NumDoc", nullable = false, unique = true, length = 20)
    public String numDoc;

    @ManyToOne
    @JoinColumn(name = "Rol", nullable = false)
    public Rol rol;

    @Column(name = "Estado", nullable = false)
    public Integer estado = 1;

    @Column(name = "FechaRegistro")
    public LocalDateTime fechaRegistro;
}
package app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "administrativo")
public class Administrativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAdministrativo")
    public Integer idAdministrativo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUsuario", nullable = false, unique = true)
    public Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Escuela")
    public Escuela escuela;

    @Column(name = "Turno")
    public String turno;

    @Column(name = "Extension")
    public String extension;

    @Column(name = "FechaIncorporacion")
    public java.time.LocalDate fechaIncorporacion;
}
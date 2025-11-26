package app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol")
public class Rol {
    @Id
    @Column(name = "IdRol")
    public Integer idRol;

    @Column(name = "Nombre", nullable = false, unique = true, length = 15)
    public String nombre;
}
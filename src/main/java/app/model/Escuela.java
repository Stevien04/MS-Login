package app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "escuela")
public class Escuela {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdEscuela")
    public Integer idEscuela;

    @Column(name = "Nombre", nullable = false)
    public String nombre;

    @Column(name = "IdFacultad")
    public Integer facultadId;
}
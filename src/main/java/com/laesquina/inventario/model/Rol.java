package com.laesquina.inventario.model;

import jakarta.persistence.*;

/**
 * Entidad Rol: perfil de permisos del usuario.
 * Valores: ROLE_ADMIN y ROLE_OPERADOR (Spring Security usa el prefijo ROLE_).
 * Tabla: rol. Un rol se asigna a varios usuarios.
 */
@Entity
@Table(name = "rol")
public class Rol {

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String OPERADOR = "ROLE_OPERADOR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public Rol() {
    }

    public Rol(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

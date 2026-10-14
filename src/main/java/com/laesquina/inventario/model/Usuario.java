package com.laesquina.inventario.model;

import jakarta.persistence.*;

/**
 * Entidad Usuario: persona que inicia sesion en el sistema.
 * La contrasena se guarda cifrada con BCrypt (nunca en texto plano).
 * Esta entidad NO se expone por la API, para no revelar la contrasena cifrada.
 * Tabla: usuario. Cada usuario tiene un rol.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Hash BCrypt (60 caracteres, empieza con $2a$).
    @Column(nullable = false, length = 100)
    private String password;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    public Usuario() {
    }

    public Usuario(String username, String password, Rol rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}

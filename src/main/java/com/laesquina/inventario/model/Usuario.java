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

    @Column(length = 80)
    private String nombre;

    @Column(length = 80)
    private String apellidos;

    // Nombre del archivo de la foto (se guarda en una carpeta del servidor, no en la BD).
    @Column(length = 150)
    private String foto;

    // Permite desactivar una cuenta sin borrarla (conserva su historial de movimientos).
    @Column(nullable = false)
    private boolean activo = true;

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

    public Usuario(String username, String password, Rol rol, String nombre, String apellidos) {
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.nombre = nombre;
        this.apellidos = apellidos;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
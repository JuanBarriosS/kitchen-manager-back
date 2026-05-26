package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "menu")
public class Menu {
    @Id
    private String id;
    private String nombre;
    private String categoria;
    private double precio;
    private boolean disponible;
    private String descripcion;  
    
    public Menu() {}
   
    public Menu(String nombre, String categoria, double precio, boolean disponible, String descripcion) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.disponible = disponible;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

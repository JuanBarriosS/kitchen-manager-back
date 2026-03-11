package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pedidos")

public class Pedido {
    @Id
    private String id;
    
    private Long clienteId;

    private List<Map<String, Object>> itemsSeleccionados;
    
    private LocalDateTime fecha;

    private String nombreCliente;

    private String fuente;

    private float total;

    private String estado = "recibido";

    public Pedido() {
    }

    public Pedido(List<Map<String, Object>> itemsSeleccionados, LocalDateTime fecha, String nombreCliente, Long clienteId, String fuente, float total) {
        this.itemsSeleccionados = itemsSeleccionados;
        this.fecha = fecha;
        this.nombreCliente = nombreCliente;
        this.clienteId = clienteId;
        this.fuente = fuente;
        this.total = total;
    }

    public List<Map<String, Object>> getItemsSeleccionados() { return itemsSeleccionados; }

    public void setItemsSeleccionados(List<Map<String, Object>> itemsSeleccionados) { this.itemsSeleccionados = itemsSeleccionados; }

    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }
    
    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    
}
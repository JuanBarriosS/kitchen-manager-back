package com.example.demo.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "ventas")
public class Venta {

    @Id
    private String id;

    private String pedidoId;
    private String nombreCliente;
    private String fuente;
    private float total;
    private LocalDateTime fecha;
    private String meseroAsignado;

    private List<Map<String, Object>> itemsVendidos;

    public Venta() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }

    public float getTotal() { return total; }
    public void setTotal(float total) { this.total = total; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public List<Map<String, Object>> getItemsVendidos() { return itemsVendidos; }
    public void setItemsVendidos(List<Map<String, Object>> itemsVendidos) { this.itemsVendidos = itemsVendidos; }

    public String getMeseroAsignado() { return meseroAsignado; }
    public void setMeseroAsignado(String meseroAsignado) { this.meseroAsignado = meseroAsignado; }
}

package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Menu;
import com.example.demo.model.Pedido;
import com.example.demo.model.Venta;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.VentaRepository;

@Service
public class KitchenContextService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private MenuRepository menuRepository;

    public String construirContexto() {
        List<Venta> ventas = ventaRepository.findAll();
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<Menu> menu = menuRepository.findAll();

        // Resumen de ventas
        double totalVentas = ventas.stream().mapToDouble(Venta::getTotal).sum();
        Map<String, Long> porCanal = ventas.stream()
            .collect(Collectors.groupingBy(v -> v.getFuente() != null ? v.getFuente() : "Otro", Collectors.counting()));

        // Platos más vendidos
        Map<String, Long> porPlato = new HashMap<>();
        ventas.forEach(v -> {
            if (v.getItemsVendidos() != null)
                v.getItemsVendidos().forEach(item -> {
                    String nombre = item.getOrDefault("nombre", "").toString();
                    porPlato.merge(nombre, 1L, Long::sum);
                });
        });

        StringBuilder ctx = new StringBuilder();
        ctx.append("=== DATOS ACTUALES DE KITCHEN MANAGER ===\n\n");

        ctx.append("RESUMEN DE VENTAS:\n");
        ctx.append("- Total de ventas registradas: ").append(ventas.size()).append("\n");
        ctx.append("- Ingresos totales: $").append(String.format("%.0f", totalVentas)).append(" COP\n");
        ctx.append("- Ventas por canal: ").append(porCanal).append("\n\n");

        ctx.append("PLATOS MÁS VENDIDOS:\n");
        porPlato.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(e -> ctx.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append(" pedidos\n"));

        ctx.append("\nMENÚ ACTUAL (").append(menu.size()).append(" platos):\n");
        menu.forEach(p -> ctx.append("- ").append(p.getNombre())
            .append(" | ").append(p.getCategoria())
            .append(" | $").append(p.getPrecio())
            .append(p.isDisponible() ? " | disponible" : " | agotado").append("\n"));

        ctx.append("\nPEDIDOS ACTIVOS AHORA: ").append(pedidos.size()).append("\n");
        pedidos.forEach(p -> ctx.append("- ").append(p.getNombreCliente())
            .append(" | ").append(p.getEstado())
            .append(" | $").append(p.getTotal()).append("\n"));

        return ctx.toString();
    }
}

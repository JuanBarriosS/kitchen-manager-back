package com.example.demo.controller;

import java.util.*;
import java.util.stream.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.model.Menu;
import com.example.demo.model.Pedido;
import com.example.demo.model.QrToken;
import com.example.demo.model.Usuarios;
import com.example.demo.model.Venta;
import com.example.demo.model.Empleados;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.QrTokenRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.VentaRepository;
import com.example.demo.security.JwtService;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;


@RestController
@CrossOrigin(origins = {"http://localhost:5173",
    "https://kitchen-manager-front.vercel.app"})

public class DemoController {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private QrTokenRepository qrTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuarios loginRequest) {
        try {
            // validarSpring Security automaticamente si el usuario existe y la contraseña coincide
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Si los datos son correctos s busca al usuario para generar su token.
            Usuarios usuario = usuarioRepository.findByUsername(loginRequest.getUsername());

            // generar el Token JWT usando JwtService
            String jwt = jwtService.generateToken(usuario);

            // respuesta para el frontend React
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Login exitoso");
            respuesta.put("token", jwt); // token
            respuesta.put("usuario", usuario.getUsername());
            respuesta.put("roles", usuario.getRoles());

            return ResponseEntity.ok(respuesta);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }
    }

    @PostMapping("/admin/agregarUsuario")
    public ResponseEntity<?> agregarUsuario(@RequestBody Usuarios usuarios) {
        if (usuarios == null) {
            return ResponseEntity.status(400).body("Datos de usuario no válidos");
        }
        // encrptar la contraseña antes de guardar
        String passwordEncriptada = passwordEncoder.encode(usuarios.getPassword());
        usuarios.setPassword(passwordEncriptada);
        usuarioRepository.save(usuarios);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario registrado con éxito con contraseña encriptada");
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/admin/agregarEmpleado")
    public ResponseEntity<?> agregarEmpleado(@RequestBody Empleados empleado) {
        if (empleado == null) {
            return ResponseEntity.status(400).body("Datos de empleados no válidos");
        }
        // encrptar la contraseña antes de guardar
        String passwordEncriptada = passwordEncoder.encode(empleado.getPassword());
        empleado.setPassword(passwordEncriptada);
        empleadoRepository.save(empleado);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Empleadp registrado con éxito con contraseña encriptada");
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/admin/Usuarios")
    public ResponseEntity<?> obtenerEmpleados() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/admin/Meseros")
    public ResponseEntity<?> obtenerMeseros() {
        return ResponseEntity.ok(empleadoRepository.findAll());
    }

    @GetMapping("/admin/verMenu")
    public ResponseEntity<?> obtenerProductos() {
        return ResponseEntity.ok(menuRepository.findAll());
    }
    @GetMapping("/empleado/verMenu")
    public ResponseEntity<?> obtenerProductosII() {
        return ResponseEntity.ok(menuRepository.findAll());
    }

    @PostMapping("/admin/agregarMenu")
    public ResponseEntity<?> agregarProducto(@RequestBody Menu menu) {
        Menu savedMenu = menuRepository.save(menu);
        return ResponseEntity.ok(savedMenu);
    }

    @PostMapping("/empleado/registrarPedido")
    public ResponseEntity<?> registrarPedido(@RequestBody Pedido pedido) {
        pedido.setFecha(LocalDateTime.now());
        pedidoRepository.save(pedido);
        return ResponseEntity.ok(pedido);//
    }

    @GetMapping("/admin/verPedidos")
    public ResponseEntity<?> obtenerPedidos() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }

    @GetMapping("/empleado/pedidos")
    public ResponseEntity<?> obtenerPedidosII() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }

    @PatchMapping("/empleado/pedido/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id, @RequestBody Map<String, String> body) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEstado(body.get("estado"));
            pedidoRepository.save(pedido);
            return ResponseEntity.ok("Estado actualizado");
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/pedido/{id}")
    public ResponseEntity<?> eliminarPedido(@PathVariable String id) {
        pedidoRepository.deleteById(id);
        return ResponseEntity.ok("Pedido eliminado");
    }

    @PostMapping("/empleado/facturar/{pedidoId}")
public ResponseEntity<?> facturar(@PathVariable String pedidoId) {
    return pedidoRepository.findById(pedidoId).map(pedido -> {
        Venta venta = new Venta();
        venta.setPedidoId(pedidoId);
        venta.setNombreCliente(pedido.getNombreCliente());
        venta.setFuente(pedido.getFuente());
        venta.setItemsVendidos(pedido.getItemsSeleccionados());
        venta.setTotal(pedido.getTotal());

        String mesero = pedido.getMeseroAsignado();
        if (mesero == null || mesero.trim().isEmpty()) {
            mesero = "No asignado";
        }
        venta.setMeseroAsignado(mesero);
        pedido.setFecha(LocalDateTime.now());
        ventaRepository.save(venta);
        pedidoRepository.deleteById(pedidoId);
        return ResponseEntity.ok(venta);
    }).orElse(ResponseEntity.notFound().build());
}

    @GetMapping("/empleado/ventas")
    public ResponseEntity<?> verVentas() {
        return ResponseEntity.ok(ventaRepository.findAll());
    }

    @GetMapping("/admin/ventas")
    public ResponseEntity<?> verVentasII() {
        return ResponseEntity.ok(ventaRepository.findAll());
    }

    @PutMapping("/admin/menu/{id}")
    public ResponseEntity<?> editarMenu(@PathVariable String id, @RequestBody Menu body) {
        return menuRepository.findById(id).map(m -> {
            m.setNombre(body.getNombre());
            m.setCategoria(body.getCategoria());
            m.setPrecio(body.getPrecio());
            m.setDisponible(body.isDisponible());
            menuRepository.save(m);
            return ResponseEntity.ok(m);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/menu/{id}")
    public ResponseEntity<?> eliminarMenu(@PathVariable String id) {
        menuRepository.deleteById(id);
        return ResponseEntity.ok("Producto eliminado");
    }

    @DeleteMapping("/admin/usuario/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable String id) {
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado");
    }

    @DeleteMapping("/admin/empleado/{id}")
    public ResponseEntity<?> eliminarEmpleado(@PathVariable String id) {
        empleadoRepository.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(true);
    }

    @GetMapping("/clientes/verMenu")
    public ResponseEntity<?> obtenerProductosIII() {
        return ResponseEntity.ok(menuRepository.findAll());
    }

    @PostMapping("/clientes/registrarPedido")
    public ResponseEntity<?> registrarPedidoII(@RequestBody Pedido pedido) {
        pedido.setFecha(LocalDateTime.now());
        pedidoRepository.save(pedido);
        return ResponseEntity.ok("Pedido registrado");
    }

    @GetMapping("/cocina/pedidos")
    public ResponseEntity<?> obtenerPedidosCocina() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }

    @GetMapping("/seguimiento/{id}")
    public ResponseEntity<?> seguimientoPedido(@PathVariable String id) {
        return pedidoRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/qrs")
    public ResponseEntity<?> obtenerQrs() {
        return ResponseEntity.ok(qrTokenRepository.findAll());
    }

    @PostMapping("/admin/qrs")
    public ResponseEntity<?> crearQr(@RequestBody QrToken qr) {
        qr.setToken(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        qr.setActivo(true);
        qrTokenRepository.save(qr);
        return ResponseEntity.ok(qr);
    }

    @PatchMapping("/admin/qrs/{id}/estado")
    public ResponseEntity<?> toggleQr(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        return qrTokenRepository.findById(id).map(qr -> {
            qr.setActivo(body.get("activo"));
            qrTokenRepository.save(qr);
            return ResponseEntity.ok(qr);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/qrs/{id}")
    public ResponseEntity<?> eliminarQr(@PathVariable String id) {
        qrTokenRepository.deleteById(id);
        return ResponseEntity.ok("QR eliminado");
    }

    @GetMapping("/menu/{token}")
    public ResponseEntity<?> menuPublico(@PathVariable String token) {
        return qrTokenRepository.findByToken(token).map(qr -> {
            if (!qr.isActivo()) {
                return ResponseEntity.status(403).body((Object)"QR inactivo");
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("qr", qr);
            resp.put("menu", menuRepository.findAll()
                .stream().filter(m -> m.isDisponible()).toList());
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.status(404).body((Object)"QR no encontrado"));
    }

    @PostMapping("/menu/{token}/pedido")
    public ResponseEntity<?> pedidoDesdeQr(@PathVariable String token, @RequestBody Pedido pedido) {
        return qrTokenRepository.findByToken(token).map(qr -> {
            if (!qr.isActivo()) return ResponseEntity.status(403).body((Object)"QR inactivo");
            pedido.setFecha(LocalDateTime.now());
            pedido.setFuente("Presencial");
            pedidoRepository.save(pedido);
            return ResponseEntity.ok((Object)pedido);
        }).orElse(ResponseEntity.status(404).body((Object)"QR no encontrado"));
    }

    @PostMapping("/admin/upload")
    public ResponseEntity<?> subirImagen(@RequestParam("file") MultipartFile file) {
        try {
            String carpeta = System.getProperty("user.home") + "/kitchen-images/";
            Files.createDirectories(Paths.get(carpeta));

            String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

            Path destino = Paths.get(carpeta + nombreArchivo);
            Files.write(destino, file.getBytes());

            Map<String, String> resp = new HashMap<>();
            resp.put("url", "/imagenes/" + nombreArchivo);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al subir imagen");
        }
    }

    @GetMapping("/meseros/disponibles")
    public ResponseEntity<?> meserosDisponibles() {
        return ResponseEntity.ok(
            empleadoRepository.findAll().stream()
                .map(m -> Map.of("id", m.getId(), "nombre", m.getUsername()))
                .toList()
        );}

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API is alive!");
    }

    @GetMapping("/admin/exportar-weka")
    public ResponseEntity<?> exportarWeka(HttpServletResponse response) throws Exception {
        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment; filename=ventas_kitchen.arff");
    
        List<Venta> ventas = ventaRepository.findAll();
    
        // ── PASO 1: recolectar platos únicos con sus frecuencias ──
        Map<String, Integer> frecuenciaPlatos = new LinkedHashMap<>();
        for (Venta v : ventas) {
            if (v.getItemsVendidos() == null) continue;
            for (Map<String, Object> item : v.getItemsVendidos()) {
                Object nom = item.get("nombre");
                if (nom == null) continue;
                String plato = limpiar(nom.toString());
                int cant = ((Number) item.getOrDefault("cantidad", 1)).intValue();
                frecuenciaPlatos.merge(plato, cant, Integer::sum);
            }
        }
    
        if (frecuenciaPlatos.isEmpty()) {
            response.getWriter().write("% No hay ventas registradas todavia");
            return null;
        }
    
        // Lista de platos únicos para declarar el atributo nominal
        List<String> platosUnicos = new ArrayList<>(frecuenciaPlatos.keySet());
    
        // ── PASO 2: calcular umbrales baja/media/alta ──
        // Recogemos TODAS las cantidades individuales para percentiles
        List<Integer> cantidades = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.getItemsVendidos() == null) continue;
            for (Map<String, Object> item : v.getItemsVendidos()) {
                int c = ((Number) item.getOrDefault("cantidad", 1)).intValue();
                cantidades.add(c);
            }
        }
        Collections.sort(cantidades);
    
        // percentil 33 y 66
        int p33 = cantidades.get(Math.max(0, (int)(cantidades.size() * 0.33) - 1));
        int p66 = cantidades.get(Math.max(0, (int)(cantidades.size() * 0.66) - 1));
        // Si todos son iguales evitamos que todo sea la misma clase
        if (p33 == p66) { p33 = 1; p66 = 2; }
    
        // ── PASO 3: construir cabecera ARFF ──
        StringBuilder sb = new StringBuilder();
        sb.append("@relation demanda_platos\n\n");
    
        // Atributo plato — nominal con los valores reales
        sb.append("@attribute plato {");
        sb.append(platosUnicos.stream()
            .map(p -> "'" + p + "'")
            .collect(Collectors.joining(",")));
        sb.append("}\n");
    
        sb.append("@attribute dia_semana " +
            "{Lunes,Martes,Miercoles,Jueves,Viernes,Sabado,Domingo}\n");
        sb.append("@attribute hora_dia " +
            "{manana,mediodia,tarde,noche}\n");
        sb.append("@attribute fuente " +
            "{Presencial,Rappi,UberEats,WhatsApp,DiDiFood,Otro}\n");
    
        // Clase — lo que queremos predecir
        sb.append("@attribute demanda {baja,media,alta}\n\n");
        sb.append("@data\n");
    
        // ── PASO 4: generar filas de datos ──
        String[] diasNombre = {
            "Domingo","Lunes","Martes","Miercoles",
            "Jueves","Viernes","Sabado"
        };
    
        for (Venta v : ventas) {
            if (v.getItemsVendidos() == null || v.getFecha() == null) continue;
    
            // extraer contexto temporal de esta venta
            LocalDateTime fecha = v.getFecha();
            String dia     = diasNombre[fecha.getDayOfWeek().getValue() % 7];
            String horaDia = getHoraDia(fecha.getHour());
            String fuente  = normalizarFuente(v.getFuente());
    
            for (Map<String, Object> item : v.getItemsVendidos()) {
                Object nom = item.get("nombre");
                if (nom == null) continue;
    
                String plato = limpiar(nom.toString());
                if (!frecuenciaPlatos.containsKey(plato)) continue;
    
                int cantidad = ((Number) item.getOrDefault("cantidad", 1)).intValue();
    
                // clasificar cantidad en baja/media/alta
                String demanda;
                if      (cantidad <= p33) demanda = "baja";
                else if (cantidad <= p66) demanda = "media";
                else                      demanda = "alta";
    
                sb.append(String.format("'%s',%s,%s,%s,%s\n",
                    plato, dia, horaDia, fuente, demanda));
            }
        }
    
        response.getWriter().write(sb.toString());
        return null;
    }
    
    // ── helpers ──
    private String limpiar(String s) {
        return s.trim()
                .replace("'",  "")
                .replace("\"", "")
                .replace(",",  "");
    }
    
    private String getHoraDia(int hora) {
        if (hora >= 6  && hora < 12) return "manana";
        if (hora >= 12 && hora < 15) return "mediodia";
        if (hora >= 15 && hora < 20) return "tarde";
        return "noche";
    }
    
    private String normalizarFuente(String f) {
        if (f == null) return "Otro";
        switch (f.trim()) {
            case "Rappi":      return "Rappi";
            case "Uber Eats":  return "UberEats";
            case "WhatsApp":   return "WhatsApp";
            case "DiDi Food":  return "DiDiFood";
            case "Presencial": return "Presencial";
            default:           return "Otro";
        }
    }
}

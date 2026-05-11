package com.example.demo.service;

import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.*;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrediccionService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private MenuRepository  menuRepository;

    private Classifier modelo;
    private Instances  estructura;
    private boolean    modeloCargado = false;

    // Los mismos platos con los que se entrenó el modelo
    private List<String> platosDelModelo = new ArrayList<>();

    @PostConstruct
    public void inicializar() {
        try {
            InputStream is = getClass().getClassLoader()
                .getResourceAsStream("modelo_demanda.model");

            if (is == null) {
                System.out.println(
                    "⚠️  modelo_demanda.model no está en resources — " +
                    "usando predicción por frecuencia histórica");
                return;
            }

            // Leer modelo
            modelo = (Classifier) SerializationHelper.read(is);

            // Reconstruir lista de platos desde ventas históricas
            // (tiene que ser IGUAL a la que se usó para entrenar)
            Set<String> platosSet = new LinkedHashSet<>();
            ventaRepository.findAll().forEach(v -> {
                if (v.getItemsVendidos() == null) return;
                v.getItemsVendidos().forEach(item -> {
                    Object nom = item.get("nombre");
                    if (nom != null) platosSet.add(limpiar(nom.toString()));
                });
            });
            platosDelModelo = new ArrayList<>(platosSet);

            // Construir estructura de Instances
            estructura = construirEstructura(platosDelModelo);
            modeloCargado = true;
            System.out.println("✅ Modelo Weka cargado — " +
                platosDelModelo.size() + " platos");

        } catch (Exception e) {
            System.err.println("❌ Error cargando modelo: " + e.getMessage());
        }
    }

    private Instances construirEstructura(List<String> platos) {
        ArrayList<Attribute> atts = new ArrayList<>();

        atts.add(new Attribute("plato", platos));

        atts.add(new Attribute("dia_semana", Arrays.asList(
            "Lunes","Martes","Miercoles","Jueves",
            "Viernes","Sabado","Domingo")));

        atts.add(new Attribute("hora_dia", Arrays.asList(
            "manana","mediodia","tarde","noche")));

        atts.add(new Attribute("fuente", Arrays.asList(
            "Presencial","Rappi","UberEats","WhatsApp","DiDiFood","Otro")));

        atts.add(new Attribute("demanda", Arrays.asList(
            "baja","media","alta")));

        Instances ds = new Instances("demanda_platos", atts, 0);
        ds.setClassIndex(4);
        return ds;
    }

    public List<Map<String, Object>> predecirAhora() {
        if (!modeloCargado) return predecirPorFrecuencia();

        LocalDateTime ahora = LocalDateTime.now();
        String dia     = getDia(ahora);
        String horaDia = getHoraDia(ahora.getHour());

        List<Map<String, Object>> resultado = new ArrayList<>();

        menuRepository.findAll().forEach(plato -> {
            if (!plato.isDisponible()) return;

            String nombreLimpio = limpiar(plato.getNombre());
            Map<String, Object> entrada = new HashMap<>();
            entrada.put("plato",    plato.getNombre());
            entrada.put("precio",   plato.getPrecio());
            entrada.put("categoria",plato.getCategoria());

            // verificar que el plato estaba en los datos de entrenamiento
            if (!platosDelModelo.contains(nombreLimpio)) {
                entrada.put("demanda",  "sin datos");
                entrada.put("score",    0.0);
                resultado.add(entrada);
                return;
            }

            try {
                Instance inst = new DenseInstance(5);
                inst.setDataset(estructura);
                inst.setValue(0, nombreLimpio);
                inst.setValue(1, dia);
                inst.setValue(2, horaDia);
                inst.setValue(3, "Presencial");
                inst.setMissing(4);

                // distribución de probabilidades
                double[] probs = modelo.distributionForInstance(inst);
                int claseIdx   = (int) modelo.classifyInstance(inst);
                String clase   = estructura.classAttribute().value(claseIdx);

                // score: peso mayor a "alta"
                double score = probs[0]*1 + probs[1]*2 + probs[2]*3;

                entrada.put("demanda",   clase);
                entrada.put("probBaja",  (int) Math.round(probs[0]*100));
                entrada.put("probMedia", (int) Math.round(probs[1]*100));
                entrada.put("probAlta",  (int) Math.round(probs[2]*100));
                entrada.put("score",     Math.round(score*100.0)/100.0);
                entrada.put("dia",       dia);
                entrada.put("hora",      horaDia);

            } catch (Exception e) {
                entrada.put("demanda", "sin datos");
                entrada.put("score",   0.0);
            }
            resultado.add(entrada);
        });

        resultado.sort((a, b) -> Double.compare(
            (double) b.getOrDefault("score", 0.0),
            (double) a.getOrDefault("score", 0.0)));
        return resultado;
    }

    // Fallback cuando no hay modelo: frecuencia histórica real
    private List<Map<String, Object>> predecirPorFrecuencia() {
        Map<String, Integer> freq = new LinkedHashMap<>();

        ventaRepository.findAll().forEach(v -> {
            if (v.getItemsVendidos() == null) return;
            v.getItemsVendidos().forEach(item -> {
                String nom  = item.getOrDefault("nombre","").toString();
                int    cant = ((Number) item.getOrDefault("cantidad",1)).intValue();
                freq.merge(nom, cant, Integer::sum);
            });
        });

        // calcular umbrales sobre los totales por plato
        List<Integer> totales = new ArrayList<>(freq.values());
        Collections.sort(totales);
        int p33 = totales.isEmpty() ? 1 :
            totales.get((int)(totales.size()*0.33));
        int p66 = totales.isEmpty() ? 2 :
            totales.get((int)(totales.size()*0.66));

        List<Map<String, Object>> lista = new ArrayList<>();
        freq.forEach((nombre, total) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("plato",   nombre);
            m.put("score",   (double) total);
            m.put("demanda", total > p66 ? "alta"
                           : total > p33 ? "media" : "baja");
            m.put("nota", "historial");
            lista.add(m);
        });
        lista.sort((a,b) -> Double.compare(
            (double)b.get("score"), (double)a.get("score")));
        return lista;
    }

    private String getDia(LocalDateTime f) {
        String[] dias = {"Domingo","Lunes","Martes","Miercoles",
                         "Jueves","Viernes","Sabado"};
        return dias[f.getDayOfWeek().getValue() % 7];
    }

    private String getHoraDia(int h) {
        if (h >= 6  && h < 12) return "manana";
        if (h >= 12 && h < 15) return "mediodia";
        if (h >= 15 && h < 20) return "tarde";
        return "noche";
    }

    private String limpiar(String s) {
        return s.trim().replace("'","").replace("\"","").replace(",","");
    }

    public boolean isModeloCargado() { return modeloCargado; }
}

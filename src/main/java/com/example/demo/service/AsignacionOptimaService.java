package com.example.demo.service;

import com.example.demo.dto.AsignacionResultado;
import com.example.demo.model.Pedido;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AsignacionOptimaService {

    private static final int TIEMPO_ATENCION_DEFAULT = 10;
    private static final int TIEMPO_DISPONIBLE_DEFAULT = 30;
    private static final int MAX_MESAS_DEFAULT = 4;

    public List<AsignacionResultado> asignar(List<Pedido> pedidosActivos,
                                             List<String> nombresMeseros) {

        int n = pedidosActivos.size();
        int m = nombresMeseros.size();
        if (n == 0 || m == 0) return List.of();

        LocalDateTime ahora = LocalDateTime.now();

        int[] prioridad = new int[n];
        for (int i = 0; i < n; i++) {
            LocalDateTime fecha = pedidosActivos.get(i).getFecha();
            long minutos = fecha == null ? 0 : Duration.between(fecha, ahora).toMinutes();
            prioridad[i] = minutos < 15 ? 1 : (minutos <= 30 ? 2 : 3);
        }

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        // Crear las variables
        Variable[][] x = new Variable[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x[i][j] = model.newVariable("x_" + i + "_" + j).binary().weight(-prioridad[i]);
            }
        }

        // R1: cada mesa a lo sumo un mesero
        for (int i = 0; i < n; i++) {
            var r1 = model.addExpression("R1_" + i).upper(1);
            for (int j = 0; j < m; j++) r1.set(x[i][j], 1);
        }

        // R2 + R3: capacidad de cada mesero
        for (int j = 0; j < m; j++) {
            var r2 = model.addExpression("R2_" + j).upper(TIEMPO_DISPONIBLE_DEFAULT);
            var r3 = model.addExpression("R3_" + j).upper(MAX_MESAS_DEFAULT);
            for (int i = 0; i < n; i++) {
                r2.set(x[i][j], TIEMPO_ATENCION_DEFAULT);
                r3.set(x[i][j], 1);
            }
        }

        Optimisation.Result result = model.minimise();
        if (!result.getState().isFeasible()) return List.of();

        List<AsignacionResultado> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (x[i][j].getValue() != null && x[i][j].getValue().intValue() == 1) {
                    out.add(new AsignacionResultado(
                        pedidosActivos.get(i).getId(),
                        nombresMeseros.get(j),
                        prioridad[i]
                    ));
                }
            }
        }
        return out;
    }
}

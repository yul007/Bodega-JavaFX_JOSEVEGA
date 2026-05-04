//Yuli
package com.bodega.service;

import com.bodega.dao.KardexDAO;
import com.bodega.model.KardexRegistro;
import com.bodega.model.MovimientoKardex;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Prepara consultas del Kardex valorado para tablas, reportes y defensa. */
public class KardexService {

    private static final int MONEY_SCALE = 2;

    private final KardexDAO kardexDAO;

    public KardexService() {
        this.kardexDAO = new KardexDAO();
    }

    public List<KardexRegistro> consultarPorProducto(int idProducto) throws SQLException {
        validarProducto(idProducto);
        return convertirRegistros(kardexDAO.listarPorProducto(idProducto));
    }

    public List<KardexRegistro> consultarPorProductoYFechas(int idProducto,
            LocalDate desde, LocalDate hasta) throws SQLException {
        validarProducto(idProducto);
        validarRangoFechas(desde, hasta);
        return convertirRegistros(kardexDAO.listarPorProductoYFechas(idProducto, desde, hasta));
    }

    public BigDecimal obtenerValorInventarioActual() throws SQLException {
        return escalaMoneda(kardexDAO.calcularValorInventarioActual());
    }

    private List<KardexRegistro> convertirRegistros(List<MovimientoKardex> movimientos) {
        List<KardexRegistro> registros = new ArrayList<>();
        for (MovimientoKardex movimiento : movimientos) {
            registros.add(convertirRegistro(movimiento));
        }
        return registros;
    }

    private KardexRegistro convertirRegistro(MovimientoKardex movimiento) {
        BigDecimal cantidad;
        BigDecimal costoUnitario;
        BigDecimal valorMovimiento;

        if ("ENTRADA".equalsIgnoreCase(movimiento.getTipo())) {
            cantidad = valorSeguro(movimiento.getCantidadEntrada());
            costoUnitario = valorSeguro(movimiento.getCostoUnitarioEntrada());
            valorMovimiento = valorSeguro(movimiento.getValorEntrada());
        } else if ("SALIDA".equalsIgnoreCase(movimiento.getTipo())) {
            cantidad = valorSeguro(movimiento.getCantidadSalida());
            costoUnitario = valorSeguro(movimiento.getCostoUnitarioSalida());
            valorMovimiento = valorSeguro(movimiento.getValorSalida());
        } else {
            cantidad = valorSeguro(movimiento.getCantidadEntrada())
                    .subtract(valorSeguro(movimiento.getCantidadSalida()));
            costoUnitario = BigDecimal.ZERO;
            valorMovimiento = valorSeguro(movimiento.getValorEntrada())
                    .subtract(valorSeguro(movimiento.getValorSalida()));
        }

        return new KardexRegistro(
                movimiento.getFecha(),
                movimiento.getTipo(),
                cantidad,
                escalaMoneda(costoUnitario),
                escalaMoneda(valorMovimiento),
                valorSeguro(movimiento.getSaldoCantidad()),
                escalaMoneda(movimiento.getSaldoValor()),
                movimiento.getReferencia(),
                movimiento.getLote() == null ? "" : movimiento.getLote().getCodigoLote(),
                movimiento.getObservacion());
    }

    private void validarProducto(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un producto valido para consultar Kardex.");
        }
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar fecha desde y fecha hasta.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private BigDecimal escalaMoneda(BigDecimal valor) {
        return valorSeguro(valor).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

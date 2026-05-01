# agents.md - Agentes y responsabilidades del proyecto

Este documento organiza el trabajo como si el proyecto fuera desarrollado por un equipo pequeno. Cada agente representa un rol con responsabilidades claras. Puede usarse con IA, entre companeros o como guia personal para dividir tareas.

## 1. Agente Coordinador del Proyecto

Objetivo: mantener coherencia entre requisitos, rubrica, stack y entregables.

Responsabilidades:

- Verificar que el sistema use Java 17, JavaFX 21/22, Maven, MySQL 8 y JDBC.
- Mantener la arquitectura MVC.
- Confirmar que todos los modulos se conecten al flujo principal.
- Revisar que el proyecto final incluya `.java`, `.fxml`, `.sql`, recursos y documentacion.
- Preparar explicacion para la defensa.
- Controlar que no se prometa PDF como obligatorio si el stack final exige TXT/CSV.

Checklist:

- Hay login y menu.
- Hay CRUD completo.
- Hay MySQL con tablas relacionadas.
- Hay Kardex valorado.
- Hay FIFO real.
- Hay dashboard y reportes.
- Hay al menos una API integrada o demostrable.
- Hay musica/fondo/estilo para creatividad.

## 2. Agente de Base de Datos

Objetivo: disenar y mantener la base `bodega_db`.

Responsabilidades:

- Crear y ejecutar `base_de_datos_sugerida.sql`.
- Ajustar tablas para FIFO real:
  - `lote.cantidad_disponible`.
  - `detalle_salida.id_lote` opcional.
- Definir claves primarias, foraneas e indices.
- Crear datos de prueba para categorias, proveedores, productos, lotes, clientes y ventas.
- Escribir consultas para dashboard y reportes.

Tablas bajo su cuidado:

- `categoria`
- `proveedor`
- `producto`
- `lote`
- `cliente`
- `nota_salida`
- `detalle_salida`
- `movimiento_kardex`

Consultas clave:

```sql
SELECT * FROM lote
WHERE id_producto = ?
  AND cantidad_disponible > 0
  AND activo = TRUE
ORDER BY fecha_ingreso ASC, id_lote ASC;
```

```sql
SELECT p.nombre, p.stock_actual, p.stock_minimo
FROM producto p
WHERE p.activo = TRUE
  AND p.stock_actual <= p.stock_minimo;
```

```sql
SELECT p.nombre, SUM(ds.cantidad) AS total_vendido
FROM detalle_salida ds
JOIN producto p ON p.id_producto = ds.id_producto
JOIN nota_salida ns ON ns.id_salida = ds.id_salida
WHERE ns.estado = 'completada'
GROUP BY p.id_producto, p.nombre
ORDER BY total_vendido DESC
LIMIT 5;
```

Criterio de exito:

- La base permite explicar relaciones en defensa.
- Las salidas no rompen el stock.
- El Kardex puede reconstruirse desde `movimiento_kardex`.

## 3. Agente Backend Java / DAO

Objetivo: implementar acceso a datos robusto usando JDBC.

Responsabilidades:

- Crear `DatabaseConnection.java` como singleton o factory.
- Usar `PreparedStatement` en todas las consultas.
- Cerrar recursos con try-with-resources.
- Manejar transacciones para operaciones compuestas:
  - guardar lote + actualizar stock + insertar Kardex.
  - guardar nota + detalle + descontar lotes + actualizar stock + insertar Kardex.
- Crear DAOs por entidad.
- No poner SQL pesado dentro de controladores.

Clases esperadas:

- `CategoriaDAO`
- `ProductoDAO`
- `ProveedorDAO`
- `LoteDAO`
- `ClienteDAO`
- `NotaSalidaDAO`
- `KardexDAO`

Reglas:

- Las excepciones SQL se registran y se muestran al usuario como mensajes claros.
- Las claves generadas se recuperan con `Statement.RETURN_GENERATED_KEYS`.
- Las operaciones de venta usan `connection.setAutoCommit(false)`.

Criterio de exito:

- Si falla una venta a la mitad, la transaccion se revierte.
- Los controladores llaman DAOs/servicios, no manipulan SQL directamente.

## 4. Agente de Modelos y POO

Objetivo: asegurar que el proyecto demuestre abstraccion, encapsulamiento, colecciones y buenas practicas.

Responsabilidades:

- Crear clases de modelo con atributos privados.
- Implementar constructores, getters/setters y `toString()` cuando ayude a ComboBox.
- Usar `BigDecimal` para dinero.
- Usar `LocalDate` para fechas.
- Usar colecciones como `ObservableList`, `List` y mapas cuando corresponda.
- Crear clases auxiliares para resultados de FIFO y reportes.

Modelos:

- `Categoria`
- `Producto`
- `Proveedor`
- `Lote`
- `Cliente`
- `NotaSalida`
- `DetalleSalida`
- `MovimientoKardex`
- `ResultadoFIFO`
- `DetalleFIFO`

Mejora POO opcional:

- Clase abstracta `Reporte`.
- Subclases `ReporteKardex`, `ReporteVentas`, `ReporteStockBajo`.
- Interfaz `Exportable`.

Criterio de exito:

- El profesor puede identificar encapsulamiento, abstraccion, colecciones y posible polimorfismo.

## 5. Agente FIFO / Kardex

Objetivo: implementar la logica principal del proyecto.

Responsabilidades:

- Crear `FIFOInventoryService`.
- Validar stock antes de vender.
- Seleccionar lotes disponibles ordenados por fecha.
- Calcular costo total de salida.
- Descontar `cantidad_disponible` de cada lote.
- Insertar movimientos Kardex.
- Devolver detalle explicable para UI y defensa.

Pseudoflujo:

```text
cantidadPendiente = cantidadSolicitada
costoTotal = 0

para cada lote disponible ordenado por fecha:
  tomar = min(cantidadPendiente, lote.cantidadDisponible)
  costoTotal += tomar * lote.costoUnitario
  lote.cantidadDisponible -= tomar
  guardar detalle FIFO
  cantidadPendiente -= tomar
  si cantidadPendiente == 0: terminar

si cantidadPendiente > 0:
  error stock insuficiente
```

Reglas:

- Nunca descontar stock si la venta no se puede completar.
- Usar transacciones.
- Cada salida debe poder explicarse con los lotes consumidos.

Criterio de exito:

- Con dos lotes de costos diferentes, la venta usa primero el lote antiguo.
- El Kardex muestra saldo de cantidad y saldo de valor correcto.

## 6. Agente JavaFX / FXML

Objetivo: construir pantallas funcionales, limpias y conectadas a controladores.

Responsabilidades:

- Crear FXML para:
  - `login.fxml`
  - `menu.fxml`
  - `productos.fxml`
  - `lotes.fxml`
  - `salidas.fxml`
  - `kardex.fxml`
  - `clientes.fxml`
  - `reportes.fxml`
  - `dashboard.fxml`
- Usar controles adecuados:
  - `TableView`
  - `TextField`
  - `PasswordField`
  - `ComboBox`
  - `DatePicker`
  - `Button`
  - `Label`
  - `CheckBox` o `ToggleButton`
  - `BarChart`, `PieChart` o `LineChart`
- Definir `fx:id` claros y consistentes.
- Conectar `onAction` con metodos de controladores.
- Cargar estilos desde `styles.css`.

Reglas visuales:

- No saturar pantallas.
- Mantener espaciado consistente.
- Usar colores de alerta para stock bajo.
- Usar tablas para datos repetidos y modales/dialogos para formularios.

Criterio de exito:

- Todas las pantallas abren sin error de FXML.
- Las tablas muestran datos reales.
- El usuario entiende el flujo sin explicacion externa.

## 7. Agente Controladores

Objetivo: unir UI, servicios y DAOs.

Responsabilidades:

- `LoginController`: validar ingreso, controlar musica y abrir menu.
- `MenuController`: navegar entre pantallas.
- `ProductoController`: CRUD, busqueda, alertas de stock.
- `LoteController`: registrar compras, cargar productos/proveedores.
- `SalidaController`: crear nota, agregar detalles, calcular totales, ejecutar FIFO.
- `KardexController`: filtrar historial y exportar.
- `ReporteController`: generar CSV/TXT.
- `DashboardController`: cargar estadisticas y graficos.

Reglas:

- No duplicar logica de negocio en controladores.
- Validar campos antes de llamar servicios.
- Mostrar alertas claras con `Alert`.
- Refrescar tablas despues de guardar, editar o eliminar.

Criterio de exito:

- Cada boton de las pantallas adjuntas tiene una accion real o mensaje controlado.

## 8. Agente Reportes y Exportacion

Objetivo: generar archivos locales utiles para la microempresa.

Responsabilidades:

- Crear `ReporteService`.
- Exportar a CSV y TXT.
- Crear carpeta `reportes/` si no existe.
- Sanitizar nombres de archivo.
- Usar fechas en nombres.
- Permitir reportes de:
  - Kardex por producto.
  - Ventas por periodo.
  - Compras por proveedor.
  - Stock bajo.
  - Valor de inventario.

Criterio de exito:

- El archivo se crea localmente.
- El usuario recibe mensaje con ruta.
- El CSV puede abrirse en Excel.

## 9. Agente APIs

Objetivo: integrar servicios externos como mejora de creatividad e investigacion.

Responsabilidades QR:

- Crear `QrCodeService`.
- Codificar datos con `URLEncoder`.
- Mostrar QR en `ImageView`.
- Usar producto, SKU o lote como contenido.

Responsabilidades Email:

- Crear `EmailService`.
- Leer configuracion SMTP desde `config.properties`.
- Enviar alerta por stock bajo.
- Enviar reporte diario como texto o adjunto.
- Manejar errores de red sin cerrar la app.

Criterio de exito:

- QR se ve en pantalla para un producto real.
- Email se puede probar con Mailtrap o SendGrid, o queda desactivado con mensaje claro si no hay credenciales.

## 10. Agente Diseno, Musica y Recursos

Objetivo: subir puntuacion en creatividad y presentacion visual.

Responsabilidades:

- Crear `styles.css`.
- Definir paleta, tamanos, botones, tablas y tarjetas.
- Agregar imagen decorativa o fondo suave en login/menu.
- Agregar archivo de audio en `resources/audio/`.
- Crear `MusicPlayer`.
- Confirmar que la musica se puede activar/desactivar.

Criterio de exito:

- La UI se ve coherente.
- La musica no molesta ni bloquea.
- Los recursos estan incluidos en el ZIP final.

## 11. Agente QA / Defensa

Objetivo: probar el sistema y preparar la explicacion.

Responsabilidades:

- Ejecutar flujo completo:
  1. Login.
  2. Crear categoria.
  3. Crear proveedor.
  4. Crear producto.
  5. Crear cliente.
  6. Registrar lote 1.
  7. Registrar lote 2 con costo diferente.
  8. Vender una cantidad que consuma ambos lotes.
  9. Revisar Kardex.
  10. Exportar reporte.
  11. Revisar dashboard.
- Probar errores:
  - Producto sin nombre.
  - Venta sin stock.
  - Fecha vacia.
  - Conexion DB incorrecta.
- Preparar respuestas de defensa:
  - Que es MVC en este proyecto.
  - Como se conecta Java con MySQL.
  - Como funciona FIFO.
  - Por que se usa Kardex.
  - Donde estan los CRUD.
  - Que hacen las APIs.

Criterio de exito:

- El demo se puede repetir sin fallos.
- Cada integrante puede explicar una parte tecnica.


## 12. Agente de Calidad

**Responsable de:** revisar que el proyecto cumple cada criterio de la rúbrica antes de la entrega. Este agente se ejecuta **al final**, cuando el proyecto esté completo.

### Tareas obligatorias para cumplir la rúbrica

#### 🟢 Revisar MVC (Criterio 3 — hasta 2.0 pts)
- [ ] Ningún controlador contiene SQL directo (solo llama DAOs)
- [ ] Los FXML tienen `fx:controller` apuntando al controlador correcto
- [ ] Paquetes separados y sin dependencias cruzadas incorrectas
- [ ] Nombres de variables y métodos en un solo idioma

#### 🟡 Revisar CRUD (Criterio 4 — hasta 2.0 pts)
- [ ] CRUD completo y funcional para productos, categorias, proveedores y clientes
- [ ] Gestion funcional de lotes/compras y notas de salida/ventas
- [ ] `TableView` se recarga después de cada operación (crear/actualizar/eliminar)
- [ ] Confirmación con `Alert` antes de eliminar
- [ ] Validaciones visibles al usuario antes de insertar en DB
- [ ] Las entidades con historial se inactivan con `activo = false` en lugar de borrarse fisicamente
- [ ] Las relaciones producto-categoria, lote-producto-proveedor, salida-cliente y detalle-salida-producto se respetan

#### 🔴 Revisar Claridad del Código (Criterio 3 compartido)
- [ ] Cada clase tiene comentario de una línea describiendo su propósito
- [ ] Cada método no trivial tiene comentario de su responsabilidad
- [ ] Sin bloques de código muerto o comentado, a menos que sea explicatorio o visualmente explicatorio
- [ ] Indentación y formato consistente 
- [ ] Sin `System.out.println` de debug en la versión final
- [ ] Costos, precios, IVA, utilidad y saldos valorados usan `BigDecimal`; nunca `double` ni `float`
- [ ] Las cantidades fisicas solo usan tipos numericos definidos por el modelo y no se mezclan con dinero
- [ ] Los controladores no contienen calculos de Kardex ni FIFO; delegan en servicios

#### 🟣 Revisar Creatividad y Presentación (Criterio 5 — hasta 2.0 pts)
- [ ] CSS aplicado globalmente (no estilos inline en FXML)
- [ ] El estilo es consistente, elegante, e intuitivo en toda la app.
- [ ] Música de fondo inicia al abrir el Dashboard y tiene botón de mute
- [ ] Hay un efecto de sonido cuando el usuario hace una accion importante como guardar una compra, generar una venta o inactivar un producto.
- [ ] Dashboard, reportes y alertas de stock bajo muestran datos reales de la base

## 13. Orden recomendado de trabajo entre agentes

1. Coordinador define alcance final.
2. Base de Datos crea schema y datos de prueba.
3. Backend Java crea conexion y DAOs.
4. Modelos POO crea entidades.
5. FIFO/Kardex implementa reglas centrales.
6. JavaFX/FXML crea pantallas.
7. Controladores conectan vistas con servicios.
8. Reportes exporta CSV/TXT.
9. APIs agrega QR/email.
10. Diseno/Musica pule creatividad.
11. QA/Defensa valida y prepara presentacion.
12. Agente de Calidad

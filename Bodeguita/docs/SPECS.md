# specs.md - Sistema de Gestion de Bodega para Microempresas

## 1. Vision del proyecto

El sistema sera una aplicacion de escritorio para que una microempresa controle el flujo completo de mercancia que entra y sale de su bodega. La aplicacion no solo mostrara cuantas unidades existen, sino tambien cuanto costo cada lote de compra y que utilidad se obtiene cuando se vende.

El eje tecnico del proyecto es un Kardex valorado con metodo FIFO. Cada entrada de mercancia registra cantidad, costo unitario, proveedor, fecha y referencia de factura. Cada salida descuenta primero los lotes mas antiguos, calcula el costo real de venta y deja trazabilidad en el historial.

## 2. Stack tecnologico obligatorio

| Capa | Tecnologia |
|---|---|
| Lenguaje | Java 17 |
| UI | JavaFX 21 o 22 + FXML + Scene Builder |
| Build | Maven 3.9+ |
| Base de datos | MySQL 8 + MySQL Workbench |
| Conexion DB | JDBC con MySQL Connector/J 8 |
| Reportes | Exportacion local a `.txt` y `.csv` |
| Sonido / musica | `javafx.scene.media.MediaPlayer` |
| APIs opcionales | QR Google Charts y Email via SMTP Mailtrap/SendGrid |

Version recomendada de plugin:

```xml
<plugin>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-maven-plugin</artifactId>
  <version>0.0.8</version>
</plugin>
```

Dependencias esperadas:

```xml
<dependency>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-controls</artifactId>
  <version>21</version>
</dependency>
<dependency>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-fxml</artifactId>
  <version>21</version>
</dependency>
<dependency>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-media</artifactId>
  <version>21</version>
</dependency>
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <version>8.4.0</version>
</dependency>
```

Para email, si se implementa:

```xml
<dependency>
  <groupId>com.sun.mail</groupId>
  <artifactId>jakarta.mail</artifactId>
  <version>2.0.1</version>
</dependency>
```

## 3. Alcance funcional

### 3.1 Modulos principales

1. Login
   - Pantalla inicial con usuario, contrasena, boton ingresar, boton salir y control de musica.
   - Credenciales minimas para la defensa: usuario `admin`, contrasena configurada localmente o validada desde tabla de usuarios si se decide ampliar.
   - Al autenticar correctamente, abre el menu principal.

2. Menu principal
   - Acceso a Productos, Lotes, Salidas, Kardex, Clientes, Reportes y Dashboard.
   - Debe mostrar el usuario activo.
   - Navegacion consistente entre pantallas.

3. Productos
   - CRUD completo de productos.
   - Campos: codigo de barras o SKU, nombre, descripcion, categoria, stock minimo, stock actual, unidad de medida, activo.
   - Busqueda por nombre o codigo.
   - Indicadores visuales de stock: verde suficiente, amarillo igual al minimo, rojo bajo minimo.
   - Acciones esperadas: Nuevo, Buscar, Editar, Eliminar/inactivar, Ver Lotes, Nueva Salida.

4. Categorias
   - CRUD simple o gestion integrada desde productos.
   - Campos: nombre, descripcion.
   - Se usa para clasificar productos y filtrar reportes.

5. Proveedores
   - CRUD completo o minimo crear/listar/editar.
   - Campos: RUC, nombre, contacto, telefono, email, direccion, activo.
   - Se usa al registrar lotes de compra.

6. Clientes
   - CRUD completo.
   - Campos: identificacion, nombre, telefono, email, direccion, activo.
   - Se usa al emitir notas de salida.

7. Lotes / compras
   - Registro de entradas de mercancia.
   - Campos: producto, proveedor, codigo de lote, cantidad, costo unitario, fecha ingreso, fecha vencimiento opcional, factura referencia.
   - Al guardar:
     - Aumenta `producto.stock_actual`.
     - Crea registro en `lote`.
     - Crea movimiento de Kardex tipo `ENTRADA`.
     - Actualiza saldo de cantidad y saldo valor.
   - La cantidad disponible del lote debe poder descontarse por FIFO. Si la tabla base solo guarda `cantidad`, se recomienda agregar `cantidad_disponible`.

8. Notas de salida / ventas
   - Creacion de factura o nota de salida.
   - Campos cabecera: cliente, fecha, numero factura, subtotal, IVA, total, estado, observaciones.
   - Detalle: producto, cantidad, precio unitario, subtotal.
   - Al generar:
     - Valida stock suficiente.
     - Aplica FIFO para determinar costo unitario real y costo total.
     - Descuenta cantidades de los lotes mas antiguos disponibles.
     - Disminuye `producto.stock_actual`.
     - Crea `nota_salida`.
     - Crea uno o varios registros de detalle si una misma linea consume mas de un lote.
     - Crea movimiento de Kardex tipo `SALIDA`.
   - La utilidad debe calcularse como `subtotal_venta - costo_total_fifo`.
   - Debe evitar ventas si el stock disponible es insuficiente.

9. Kardex valorado
   - Consulta historica por producto y rango de fechas.
   - Columnas minimas: fecha, tipo, referencia, cantidad, costo unitario, saldo cantidad, saldo valor.
   - Debe explicar o representar salidas FIFO que consumen uno o varios lotes.
   - Resumen inferior: stock actual y valor actual del inventario.
   - Exportacion obligatoria: CSV y TXT local.

10. Dashboard
   - Vista grafica de estado actual.
   - Indicadores minimos:
     - Valor total del inventario.
     - Productos con stock bajo.
     - Top 5 productos mas vendidos.
     - Ventas de ultimos 30 dias.
   - Controles: actualizar, refrescar, exportar reporte, musica ON/OFF.
   - Usar graficos JavaFX: `BarChart`, `PieChart`, `LineChart` o listas con barras visuales.

11. Reportes
   - Tipos:
     - Kardex completo por producto.
     - Notas de salida por periodo.
     - Compras a proveedores.
     - Productos con stock bajo.
     - Valor de inventario actual.
   - Filtros: fecha desde, fecha hasta, producto/proveedor/cliente cuando aplique.
   - Exportar a `.csv` y `.txt`.
   - PDF queda como mejora opcional porque el stack definido exige TXT/CSV.

12. Musica y sonido
   - Implementar clase `MusicPlayer`.
   - Reproducir musica de fondo desde `src/main/resources/audio/`.
   - Boton de activar/desactivar en login y dashboard.
   - El volumen debe ser moderado y no bloquear la app si el archivo no existe.

## 4. Reglas de negocio

### 4.1 FIFO

FIFO significa que los primeros lotes que entran son los primeros que salen. Ejemplo:

- Lote 101: 100 unidades a 1.50, ingreso 15/03.
- Lote 102: 50 unidades a 1.55, ingreso 28/03.
- Venta de 120 unidades:
  - 100 salen del lote 101.
  - 20 salen del lote 102.
  - Costo para el negocio: `(100 * 1.50) + (20 * 1.55) = 181.00`.

La aplicacion debe registrar esa trazabilidad en el Kardex. Si una venta consume varios lotes, la salida puede guardarse en varias filas internas o en una fila con costo promedio ponderado de esa salida, pero debe conservarse el detalle suficiente para explicar el costo.

### 4.2 Stock

- `stock_actual` nunca puede ser negativo.
- `stock_minimo` activa alertas visuales y de email.
- Entradas suman stock.
- Salidas restan stock.
- Anulacion de nota de salida debe ser opcional. Si se implementa, debe revertir stock y Kardex de forma controlada, no borrar historia.

### 4.3 Valores monetarios

- Usar `BigDecimal` en Java para costos, precios, IVA, totales y saldos valorados.
- La base de datos debe usar `DECIMAL(10,2)`.
- Nunca usar `double` ni `float` para dinero en logica de negocio.
- Los calculos de Kardex, FIFO, utilidad, IVA, precios y costos deben hacerse siempre con `BigDecimal`.
- Si se manejan cantidades fisicas fraccionarias como kg o litros, deben estar separadas claramente de los valores monetarios.

### 4.4 Validaciones

- Nombre de producto obligatorio.
- Producto debe tener categoria.
- Lote requiere producto, proveedor, cantidad mayor que cero y costo unitario mayor que cero.
- Venta requiere cliente, fecha, numero factura unico y al menos un detalle.
- No permitir eliminar fisicamente productos con movimientos. Usar `activo = false`.
- Email debe tener formato basico si se usa API de correo.
- Fechas no deben quedar vacias.

## 5. Base de datos

Base sugerida: `bodega_db`.

Tablas principales:

| Tabla | Proposito |
|---|---|
| `categoria` | Clasificacion de productos |
| `proveedor` | Datos de quienes venden mercancia a la microempresa |
| `producto` | Catalogo y stock actual |
| `lote` | Entradas con costo historico |
| `cliente` | Personas o empresas que compran |
| `nota_salida` | Cabecera de venta |
| `detalle_salida` | Productos vendidos |
| `movimiento_kardex` | Historial valorado de entradas y salidas |

Mejora recomendada sobre el SQL adjunto:

```sql
ALTER TABLE lote ADD COLUMN cantidad_disponible DOUBLE NOT NULL DEFAULT 0;
ALTER TABLE detalle_salida ADD COLUMN id_lote INT NULL;
ALTER TABLE detalle_salida ADD CONSTRAINT fk_detalle_lote
  FOREIGN KEY (id_lote) REFERENCES lote(id_lote);
```

Razon: para FIFO real se necesita saber cuanto queda disponible en cada lote. Tambien conviene conocer que lote financio el costo de cada salida.

Indices recomendados:

```sql
CREATE INDEX idx_producto_nombre ON producto(nombre);
CREATE INDEX idx_lote_producto_fecha ON lote(id_producto, fecha_ingreso);
CREATE INDEX idx_kardex_producto_fecha ON movimiento_kardex(id_producto, fecha);
CREATE INDEX idx_salida_fecha ON nota_salida(fecha_emision);
```

## 6. Arquitectura MVC

Estructura Maven recomendada:

```text
src/main/java/com/bodegamaster/
  App.java
  db/DatabaseConnection.java
  model/Categoria.java
  model/Producto.java
  model/Proveedor.java
  model/Lote.java
  model/Cliente.java
  model/NotaSalida.java
  model/DetalleSalida.java
  model/MovimientoKardex.java
  dao/CategoriaDAO.java
  dao/ProductoDAO.java
  dao/ProveedorDAO.java
  dao/LoteDAO.java
  dao/ClienteDAO.java
  dao/NotaSalidaDAO.java
  dao/KardexDAO.java
  service/FIFOInventoryService.java
  service/ReporteService.java
  service/QrCodeService.java
  service/EmailService.java
  util/MusicPlayer.java
  util/AlertHelper.java
  controller/LoginController.java
  controller/MenuController.java
  controller/ProductoController.java
  controller/LoteController.java
  controller/SalidaController.java
  controller/KardexController.java
  controller/ReporteController.java
  controller/DashboardController.java
src/main/resources/com/bodegamaster/view/
  login.fxml
  menu.fxml
  productos.fxml
  lotes.fxml
  salidas.fxml
  kardex.fxml
  clientes.fxml
  reportes.fxml
  dashboard.fxml
src/main/resources/css/styles.css
src/main/resources/audio/
src/main/resources/images/
```

Responsabilidades:

- Model: entidades con atributos, constructores, getters/setters y validaciones basicas.
- DAO: consultas SQL con `PreparedStatement`, mapeo de `ResultSet`, transacciones cuando hay operaciones compuestas.
- Service: reglas de negocio, FIFO, reportes, APIs, email, calculos.
- Controller: conecta FXML con servicios, carga tablas, valida formularios y muestra mensajes.
- View/FXML: estructura visual hecha con Scene Builder o FXML manual.

## 7. Flujo de aplicacion

1. Usuario ingresa en login.
2. El sistema abre menu principal.
3. Usuario crea categorias, proveedores, productos y clientes si no existen.
4. Usuario registra lotes de compra.
5. El sistema aumenta stock y registra entrada en Kardex.
6. Usuario registra nota de salida.
7. El sistema valida stock y aplica FIFO.
8. El sistema registra venta, descuenta stock, descuenta lotes y registra salida en Kardex.
9. Usuario consulta Kardex por producto.
10. Usuario revisa dashboard y exporta reportes.

## 8. Pantallas esperadas

### Login

- Titulo: Sistema Bodega.
- Campos: usuario, contrasena.
- Botones: Ingresar, Salir.
- Toggle de musica.

### Menu

- Nombre de app: Bodega Master.
- Usuario activo.
- Botones grandes:
  - Productos.
  - Lotes.
  - Salidas.
  - Kardex.
  - Clientes.
  - Reportes.
  - Dashboard.

### Productos

- Buscador.
- Tabla con ID, nombre, categoria, stock, stock minimo.
- Botones: Nuevo, Buscar, Editar, Eliminar, Ver Lotes, Nueva Salida.
- Modal de edicion con nombre, categoria, stock minimo, unidad.

### Lotes

- Tabla con ID, producto, proveedor, cantidad, costo, fecha.
- Formulario nuevo lote con producto, proveedor, cantidad, costo unitario, fecha ingreso, vencimiento opcional, factura.
- Mensaje visible: al guardar se actualiza stock y Kardex.

### Salidas

- Cliente, fecha, numero factura.
- Tabla detalle con producto, cantidad, precio, subtotal, acciones.
- Totales: subtotal, IVA 12%, total.
- Botones: Generar factura, Calcular costo FIFO, Cancelar.

### Kardex

- Filtros: producto, fecha desde, fecha hasta.
- Tabla: fecha, tipo, cantidad, costo unitario, saldo cantidad, valor saldo.
- Notas FIFO cuando una salida consume mas de un lote.
- Exportar CSV/TXT.

### Reportes

- Radio buttons o ComboBox para tipo de reporte.
- Fechas desde/hasta.
- Botones exportar CSV, exportar TXT, imprimir opcional.

### Dashboard

- Valor inventario.
- Productos con stock bajo.
- Top 5 productos mas vendidos.
- Ventas ultimos 30 dias.
- Botones actualizar, refrescar, exportar reporte, musica ON/OFF.

## 9. Integracion de APIs

### 9.1 API QR Google Charts

Objetivo: generar imagen QR para producto, SKU, lote o ubicacion de estante.

URL base:

```text
https://chart.googleapis.com/chart?chs=150x150&cht=qr&chl=DATOS_ENCODED
```

Implementacion sugerida:

- Clase: `QrCodeService`.
- Metodo: `Image generarQrProducto(Producto producto)`.
- Datos QR: codigo de barras/SKU, nombre y/o ID.
- JavaFX puede cargar la imagen directamente:

```java
Image qr = new Image(qrUrl);
qrImageView.setImage(qr);
```

Uso en UI:

- Boton "Ver QR" en productos.
- Modal con QR del producto.
- Opcion de guardar o imprimir etiqueta como mejora.

### 9.2 API Email Mailtrap / SendGrid

Objetivo: enviar alertas y reportes sin que el administrador revise la app todo el tiempo.

Casos:

- Alerta de stock bajo.
- Reporte diario de entradas y salidas.
- Resumen de productos criticos.

Implementacion sugerida:

- Clase: `EmailService`.
- Metodo: `enviarAlertaStockBajo(Producto producto)`.
- Metodo: `enviarReporteDiario(String destinatario, File archivoCsv)`.
- Credenciales por archivo `config.properties` no subido a entrega publica o variables de entorno.

Configuracion ejemplo:

```properties
smtp.host=smtp.mailtrap.io
smtp.port=2525
smtp.user=usuario
smtp.password=clave
mail.from=bodega@microempresa.local
mail.admin=admin@microempresa.local
```

Seguridad:

- No escribir claves reales en el codigo fuente.
- Si no hay conexion a internet, la app debe seguir funcionando y mostrar mensaje controlado.

## 10. Reportes y archivos

Los reportes se exportaran en carpeta local `reportes/`.

Nombres sugeridos:

- `kardex_producto_YYYYMMDD.csv`
- `kardex_producto_YYYYMMDD.txt`
- `stock_bajo_YYYYMMDD.csv`
- `ventas_periodo_YYYYMMDD_YYYYMMDD.csv`
- `valor_inventario_YYYYMMDD.txt`

Formato CSV:

```text
fecha,tipo,producto,cantidad,costo_unitario,saldo_cantidad,saldo_valor,referencia
2026-04-30,ENTRADA,Coca-Cola 3L,100,1.50,100,150.00,FAC-001
```

Formato TXT:

```text
REPORTE DE KARDEX
Producto: Coca-Cola 3L
Periodo: 2026-04-01 a 2026-04-30

Fecha       Tipo      Cantidad  Costo U.  Saldo Cant.  Saldo Valor
2026-04-30  ENTRADA   100       1.50      100          150.00
```

## 11. Diseno visual

La interfaz debe ser clara, ordenada y defendible:

- Paleta recomendada: azul oscuro para encabezados, verde para acciones positivas, rojo para alertas, gris claro para fondos.
- Usar `styles.css` global.
- Tablas con encabezados visibles.
- Formularios alineados.
- Botones con texto claro e iconos si se dispone de recursos.
- Fondo decorativo suave o imagen de bodega en login/menu, sin afectar legibilidad.
- Tipografias coherentes y tamanos consistentes.
- Estados visuales: exito, error, advertencia, stock bajo.

## 12. Criterios de aceptacion

El proyecto se considera listo si:

- Compila con Maven en Java 17.
- Abre la app JavaFX sin errores.
- Conecta con MySQL 8.
- Permite CRUD de productos, proveedores, clientes y categorias.
- Registra lotes y actualiza stock.
- Registra ventas y descuenta por FIFO.
- Kardex muestra entradas, salidas y saldos valorados.
- Dashboard muestra datos reales desde la base.
- Exporta reportes CSV/TXT.
- Tiene musica opcional controlable.
- Integra al menos una API de forma demostrable; idealmente QR y email.
- El codigo mantiene MVC claro.
- La defensa puede explicar la BD, relaciones, consultas, FIFO, MVC y validaciones.

## 13. Relacion con la rubrica

| Criterio | Como se cumple |
|---|---|
| POO | Modelos encapsulados, servicios, DAO, herencia opcional para reportes, colecciones en tablas y calculos FIFO |
| JavaFX | Pantallas FXML con tablas, formularios, botones, charts y navegacion |
| MVC | Paquetes separados `model`, `view/fxml`, `controller`, `dao`, `service` |
| Funcionalidad | CRUD completo, MySQL, FIFO, Kardex, reportes, dashboard |
| Diseno y creatividad | CSS, fondo decorativo, musica, APIs QR/email, graficos dinamicos |

## 14. Prioridad de implementacion

1. Crear proyecto Maven + JavaFX.
2. Crear base de datos y conexion JDBC.
3. Implementar modelos y DAO.
4. Implementar CRUD de categorias, proveedores, productos y clientes.
5. Implementar registro de lotes.
6. Implementar FIFO y salidas.
7. Implementar Kardex valorado.
8. Implementar reportes CSV/TXT.
9. Implementar dashboard.
10. Agregar musica, estilos y APIs.
11. Probar flujo completo con datos de ejemplo.

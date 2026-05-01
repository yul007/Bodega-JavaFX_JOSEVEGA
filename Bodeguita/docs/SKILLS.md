# skills.md - Habilidades necesarias y guia de aprendizaje

Este documento resume las habilidades tecnicas que el equipo debe dominar para construir y defender el Sistema de Gestion de Bodega para Microempresas.

## 1. Java 17 y POO

Habilidades:

- Crear clases, atributos privados, constructores y metodos.
- Usar encapsulamiento con getters y setters.
- Aplicar abstraccion separando modelos, servicios y controladores.
- Usar colecciones: `List`, `ArrayList`, `ObservableList`.
- Usar `BigDecimal` para dinero.
- Evitar siempre `double` y `float` para precios, costos, IVA, utilidad y saldos de Kardex.
- Usar `LocalDate` para fechas.
- Manejar excepciones con `try/catch`.

Aplicacion en el proyecto:

- Modelos: `Producto`, `Lote`, `Cliente`, `NotaSalida`.
- Servicios: `FIFOInventoryService`, `ReporteService`, `EmailService`.
- Colecciones: listas de productos, lotes, detalles de venta y movimientos de Kardex.

Evidencia para la rubrica:

- Clases organizadas en paquetes.
- Atributos privados.
- Nombres claros.
- Metodos pequenos y entendibles.

## 2. JavaFX 21/22

Habilidades:

- Crear interfaces con FXML.
- Conectar FXML con controladores mediante `fx:controller`.
- Usar `TableView` y `TableColumn`.
- Usar `TextField`, `PasswordField`, `ComboBox`, `DatePicker`, `Button`, `Label`.
- Cargar escenas desde `FXMLLoader`.
- Aplicar CSS.
- Mostrar alertas con `Alert`.
- Usar graficos JavaFX para dashboard.

Aplicacion en el proyecto:

- Login con usuario, contrasena y musica.
- Menu principal con acceso a modulos.
- Pantallas CRUD con tablas y formularios.
- Dashboard con indicadores y graficos.

Checklist minimo:

- Cada FXML carga sin errores.
- Cada boton importante tiene `onAction`.
- Las tablas usan `ObservableList`.
- Los campos se limpian despues de guardar.
- Los errores se muestran con mensajes claros.

## 3. Scene Builder y FXML

Habilidades:

- Disenar pantallas con layouts: `BorderPane`, `VBox`, `HBox`, `GridPane`, `AnchorPane`.
- Asignar `fx:id`.
- Vincular eventos.
- Ajustar alineacion, margenes y espaciado.
- Exportar FXML compatible con JavaFX 21/22.

Aplicacion en el proyecto:

- Construir pantallas tomando como base los archivos:
  - `loginFXML.txt`
  - `menuFXML.txt`
  - `productosFXML.txt`
  - `lotesFXML.txt`
  - `salidasFXML.txt`
  - `kardexFXML.txt`
  - `reportesFXML.txt`
  - `DashboardFXML.txt`

Consejo:

- Empezar con una pantalla simple funcional y luego mejorar estilos.
- No dejar ids genericos como `button1` o `textField2`.

## 4. Maven

Habilidades:

- Crear proyecto Maven.
- Configurar `pom.xml`.
- Agregar dependencias JavaFX, MySQL y Jakarta Mail.
- Ejecutar la app con `mvn javafx:run`.
- Organizar recursos en `src/main/resources`.

Aplicacion en el proyecto:

- Proyecto reproducible para entrega ZIP/RAR.
- Dependencias centralizadas.
- Separacion clara entre codigo y recursos.

Checklist:

- `pom.xml` compila con Java 17.
- `javafx-maven-plugin` version `0.0.8`.
- Incluye modulo `javafx-media` si hay musica.
- Incluye MySQL Connector/J.

## 5. MySQL 8 y MySQL Workbench

Habilidades:

- Crear base de datos.
- Crear tablas con claves primarias y foraneas.
- Insertar datos de prueba.
- Escribir consultas `SELECT`, `INSERT`, `UPDATE`, `DELETE`.
- Usar `JOIN`.
- Entender relaciones uno-a-muchos.

Aplicacion en el proyecto:

- Base `bodega_db`.
- Relacion producto-categoria.
- Relacion lote-producto-proveedor.
- Relacion nota_salida-cliente.
- Relacion detalle_salida-nota_salida-producto.
- Kardex conectado a producto.

Consultas que se deben poder explicar:

- Listar productos con categoria.
- Buscar lotes disponibles de un producto.
- Consultar Kardex por producto y fecha.
- Calcular productos con stock bajo.
- Calcular top 5 productos vendidos.
- Calcular valor de inventario.

## 6. JDBC

Habilidades:

- Abrir conexion con `DriverManager`.
- Usar `PreparedStatement`.
- Leer datos con `ResultSet`.
- Cerrar recursos con try-with-resources.
- Manejar transacciones con `commit` y `rollback`.

Aplicacion en el proyecto:

- `DatabaseConnection.java`.
- DAOs por entidad.
- Transacciones en compras y ventas.

Checklist:

- No concatenar datos de usuario en SQL.
- No dejar conexiones abiertas.
- Recuperar IDs generados.
- Mostrar errores entendibles.

## 7. CRUD

Habilidades:

- Crear registros.
- Listar registros.
- Actualizar registros.
- Eliminar o inactivar registros.
- Validar datos antes de guardar.

Aplicacion en el proyecto:

- Productos.
- Categorias.
- Proveedores.
- Clientes.
- Lotes.
- Notas de salida.

Regla de proyecto:

- En entidades con historial, preferir `activo = false` en lugar de borrar fisicamente.

## 8. Kardex valorado

Habilidades:

- Entender entradas y salidas de inventario.
- Registrar cantidad, costo unitario, saldo cantidad y saldo valor.
- Mantener historial ordenado por fecha.
- Explicar el valor actual del inventario.

Aplicacion en el proyecto:

- Cada lote crea movimiento `ENTRADA`.
- Cada venta crea movimiento `SALIDA`.
- El usuario puede filtrar por producto y rango de fechas.
- El reporte exportado debe coincidir con la tabla de la app.

Formula base:

```text
saldo_cantidad = saldo_anterior_cantidad + entradas - salidas
saldo_valor = saldo_anterior_valor + valor_entradas - costo_salidas_fifo
```

## 9. FIFO

Habilidades:

- Ordenar lotes por fecha de ingreso.
- Consumir primero el lote mas antiguo.
- Dividir una salida si necesita mas de un lote.
- Calcular costo total real.
- Mantener cantidades disponibles por lote.

Aplicacion en el proyecto:

- Clase `FIFOInventoryService`.
- Clase `ResultadoFIFO`.
- Clase `DetalleFIFO`.

Ejemplo que se debe dominar:

```text
Lote 101: 100 unidades a 1.50
Lote 102: 50 unidades a 1.55
Venta: 120 unidades

Costo:
100 * 1.50 = 150.00
20 * 1.55 = 31.00
Total costo = 181.00
```

Pregunta probable de defensa:

```text
Por que no se usa simplemente el costo promedio?
```

Respuesta sugerida:

```text
Porque el proyecto busca valorar el inventario por historial real de compras. FIFO permite saber de que lotes salieron los productos y calcula la utilidad usando el costo historico de cada lote.
```

## 10. Reportes CSV/TXT

Habilidades:

- Crear archivos locales.
- Escribir lineas con `BufferedWriter` o `Files.write`.
- Dar formato a fechas y dinero.
- Separar columnas con coma en CSV.
- Crear reportes legibles en TXT.

Aplicacion en el proyecto:

- Kardex por producto.
- Ventas por periodo.
- Compras por proveedor.
- Productos con stock bajo.
- Valor de inventario actual.

Checklist:

- Carpeta `reportes/` creada automaticamente.
- Nombre de archivo incluye fecha.
- Mensaje al usuario confirma exportacion.
- CSV abre en Excel.

## 11. Dashboard y graficos

Habilidades:

- Calcular estadisticas desde SQL.
- Mostrar KPIs con `Label`.
- Mostrar graficos con JavaFX.
- Refrescar datos.

Aplicacion en el proyecto:

- Valor inventario.
- Productos con stock bajo.
- Top 5 productos vendidos.
- Ventas ultimos 30 dias.

Consultas necesarias:

- Suma de valor disponible por lote.
- Productos donde `stock_actual <= stock_minimo`.
- Agrupacion de ventas por producto.
- Agrupacion de ventas por fecha.

## 12. API QR

Habilidades:

- Construir URLs.
- Codificar texto con `URLEncoder`.
- Cargar imagen remota en JavaFX con `Image`.
- Mostrar imagen en `ImageView`.

Aplicacion en el proyecto:

- Generar QR para producto, lote o ubicacion.
- Mostrar QR desde la ficha del producto.

URL:

```text
https://chart.googleapis.com/chart?chs=150x150&cht=qr&chl=DATOS
```

Checklist:

- El QR cambia segun el producto.
- Si no hay internet, se muestra mensaje controlado.

## 13. API Email

Habilidades:

- Configurar SMTP.
- Usar Jakarta Mail.
- Leer credenciales desde archivo o variables.
- Enviar mensajes simples.
- Manejar errores de autenticacion o red.

Aplicacion en el proyecto:

- Alerta de stock bajo.
- Envio de reporte diario.

Checklist:

- No guardar claves reales en codigo.
- Probar con Mailtrap o SendGrid.
- La app sigue funcionando si email falla.

## 14. Musica con MediaPlayer

Habilidades:

- Cargar archivo de audio desde recursos.
- Crear `Media` y `MediaPlayer`.
- Reproducir, pausar y detener.
- Controlar volumen.

Aplicacion en el proyecto:

- Musica opcional en login y dashboard.
- Boton ON/OFF.

Checklist:

- El audio esta en `src/main/resources/audio/`.
- No se rompe la app si falta el archivo.
- Volumen moderado.

## 15. Diseno visual

Habilidades:

- Crear CSS para JavaFX.
- Usar colores coherentes.
- Alinear formularios.
- Disenar tablas legibles.
- Usar imagen decorativa sin tapar texto.

Aplicacion en el proyecto:

- Login y menu con identidad visual de bodega.
- Productos con indicadores de stock.
- Dashboard con tarjetas o paneles claros.

Checklist:

- Pantallas limpias y consistentes.
- Botones con acciones claras.
- No hay texto cortado.
- No hay controles superpuestos.

## 16. Pruebas

Habilidades:

- Probar flujo feliz.
- Probar validaciones.
- Probar errores de conexion.
- Comparar resultados esperados con resultados reales.

Casos minimos:

1. Crear producto nuevo.
2. Crear dos lotes del mismo producto con costos distintos.
3. Vender cantidad que use ambos lotes.
4. Verificar costo FIFO.
5. Verificar stock actual.
6. Verificar Kardex.
7. Exportar reporte.
8. Revisar dashboard.
9. Probar stock insuficiente.
10. Probar alerta de stock bajo.

## 17. Defensa del proyecto

Cada integrante debe poder explicar:

- Que problema resuelve la aplicacion.
- Cuales son las pantallas principales.
- Como funciona MVC en el proyecto.
- Como se conecta Java con MySQL.
- Que tablas existen y como se relacionan.
- Como funciona FIFO.
- Como se genera el Kardex valorado.
- Donde esta el CRUD completo.
- Como se exportan reportes.
- Como se integran QR y email.
- Como se usa musica/fondo/estilos para creatividad.

Guion breve:

```text
Nuestro sistema ayuda a una microempresa a controlar entradas y salidas de bodega. Cada compra se registra como lote con costo historico. Cuando se vende, el sistema aplica FIFO para descontar primero los lotes mas antiguos y calcular el costo real de venta. Todo queda registrado en un Kardex valorado. La aplicacion esta hecha en JavaFX con FXML, usa MySQL mediante JDBC y sigue arquitectura MVC. Tambien incluye dashboard, reportes CSV/TXT, musica opcional e integracion con QR/email.
```

## 18. Checklist final antes de entregar

- Proyecto abre con `mvn javafx:run`.
- Java 17 configurado.
- MySQL 8 funcionando.
- Script SQL incluido.
- FXML incluido.
- CSS incluido.
- Imagenes y audio incluidos si se usan.
- CRUD probado.
- FIFO probado.
- Kardex probado.
- Reportes probados.
- Dashboard probado.
- APIs probadas o documentadas con fallback.
- ZIP/RAR contiene codigo fuente, FXML, SQL y recursos.
- El equipo entiende el codigo generado o asistido por IA.

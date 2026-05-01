-- Sistema de Gestion de Bodega para Microempresas
-- MySQL 8.x
-- Ejecutar completo para recrear la base de datos de prueba.

CREATE DATABASE IF NOT EXISTS bodega_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bodega_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS movimiento_kardex;
DROP TABLE IF EXISTS detalle_salida;
DROP TABLE IF EXISTS nota_salida;
DROP TABLE IF EXISTS lote;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS proveedor;
DROP TABLE IF EXISTS categoria;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE categoria (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  descripcion VARCHAR(255) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE proveedor (
  id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
  ruc VARCHAR(20) NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  contacto VARCHAR(120) NULL,
  telefono VARCHAR(30) NULL,
  email VARCHAR(120) NULL,
  direccion VARCHAR(255) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_proveedor_ruc UNIQUE (ruc),
  CONSTRAINT uk_proveedor_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  identificacion VARCHAR(20) NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  telefono VARCHAR(30) NULL,
  email VARCHAR(120) NULL,
  direccion VARCHAR(255) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_cliente_identificacion UNIQUE (identificacion),
  CONSTRAINT uk_cliente_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  id_categoria INT NOT NULL,
  sku VARCHAR(50) NOT NULL,
  codigo_barras VARCHAR(80) NULL,
  nombre VARCHAR(120) NOT NULL,
  descripcion VARCHAR(255) NULL,
  unidad_medida VARCHAR(20) NOT NULL DEFAULT 'unidad',
  stock_minimo DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  stock_actual DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  precio_venta DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_producto_sku UNIQUE (sku),
  CONSTRAINT uk_producto_codigo_barras UNIQUE (codigo_barras),
  CONSTRAINT fk_producto_categoria
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_producto_stock_minimo CHECK (stock_minimo >= 0),
  CONSTRAINT chk_producto_stock_actual CHECK (stock_actual >= 0),
  CONSTRAINT chk_producto_precio_venta CHECK (precio_venta >= 0)
) ENGINE=InnoDB;

CREATE TABLE lote (
  id_lote INT AUTO_INCREMENT PRIMARY KEY,
  id_producto INT NOT NULL,
  id_proveedor INT NOT NULL,
  codigo_lote VARCHAR(80) NOT NULL,
  cantidad DECIMAL(12,3) NOT NULL,
  cantidad_disponible DECIMAL(12,3) NOT NULL,
  costo_unitario DECIMAL(10,2) NOT NULL,
  fecha_ingreso DATE NOT NULL,
  fecha_vencimiento DATE NULL,
  factura_referencia VARCHAR(80) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_lote_producto_codigo UNIQUE (id_producto, codigo_lote),
  CONSTRAINT fk_lote_producto
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_lote_proveedor
    FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_lote_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_lote_cantidad_disponible CHECK (cantidad_disponible >= 0 AND cantidad_disponible <= cantidad),
  CONSTRAINT chk_lote_costo_unitario CHECK (costo_unitario > 0)
) ENGINE=InnoDB;

CREATE TABLE nota_salida (
  id_salida INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NOT NULL,
  numero_factura VARCHAR(80) NOT NULL,
  fecha_emision DATE NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  iva DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  costo_total_fifo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  utilidad DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  estado ENUM('pendiente', 'completada', 'anulada') NOT NULL DEFAULT 'completada',
  observaciones VARCHAR(255) NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_nota_salida_numero_factura UNIQUE (numero_factura),
  CONSTRAINT fk_nota_salida_cliente
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_nota_salida_subtotal CHECK (subtotal >= 0),
  CONSTRAINT chk_nota_salida_iva CHECK (iva >= 0),
  CONSTRAINT chk_nota_salida_total CHECK (total >= 0),
  CONSTRAINT chk_nota_salida_costo_fifo CHECK (costo_total_fifo >= 0)
) ENGINE=InnoDB;

CREATE TABLE detalle_salida (
  id_detalle_salida INT AUTO_INCREMENT PRIMARY KEY,
  id_salida INT NOT NULL,
  id_producto INT NOT NULL,
  id_lote INT NULL,
  cantidad DECIMAL(12,3) NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  costo_unitario_fifo DECIMAL(10,2) NOT NULL,
  costo_total_fifo DECIMAL(12,2) NOT NULL,
  utilidad DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_detalle_salida_nota
    FOREIGN KEY (id_salida) REFERENCES nota_salida(id_salida)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_detalle_salida_producto
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_detalle_salida_lote
    FOREIGN KEY (id_lote) REFERENCES lote(id_lote)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_detalle_salida_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_detalle_salida_precio CHECK (precio_unitario >= 0),
  CONSTRAINT chk_detalle_salida_subtotal CHECK (subtotal >= 0),
  CONSTRAINT chk_detalle_salida_costo_unitario CHECK (costo_unitario_fifo >= 0),
  CONSTRAINT chk_detalle_salida_costo_total CHECK (costo_total_fifo >= 0)
) ENGINE=InnoDB;

CREATE TABLE movimiento_kardex (
  id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
  id_producto INT NOT NULL,
  id_lote INT NULL,
  id_salida INT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo ENUM('ENTRADA', 'SALIDA', 'AJUSTE') NOT NULL,
  referencia VARCHAR(120) NOT NULL,
  cantidad_entrada DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  costo_unitario_entrada DECIMAL(10,2) NULL,
  valor_entrada DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  cantidad_salida DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  costo_unitario_salida DECIMAL(10,2) NULL,
  valor_salida DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  saldo_cantidad DECIMAL(12,3) NOT NULL,
  saldo_valor DECIMAL(12,2) NOT NULL,
  observacion VARCHAR(255) NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_kardex_producto
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_kardex_lote
    FOREIGN KEY (id_lote) REFERENCES lote(id_lote)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_kardex_salida
    FOREIGN KEY (id_salida) REFERENCES nota_salida(id_salida)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_kardex_cantidad_entrada CHECK (cantidad_entrada >= 0),
  CONSTRAINT chk_kardex_valor_entrada CHECK (valor_entrada >= 0),
  CONSTRAINT chk_kardex_cantidad_salida CHECK (cantidad_salida >= 0),
  CONSTRAINT chk_kardex_valor_salida CHECK (valor_salida >= 0),
  CONSTRAINT chk_kardex_saldo_cantidad CHECK (saldo_cantidad >= 0),
  CONSTRAINT chk_kardex_saldo_valor CHECK (saldo_valor >= 0),
  CONSTRAINT chk_kardex_movimiento_no_vacio CHECK (cantidad_entrada > 0 OR cantidad_salida > 0 OR tipo = 'AJUSTE')
) ENGINE=InnoDB;

CREATE INDEX idx_producto_nombre ON producto(nombre);
CREATE INDEX idx_producto_categoria ON producto(id_categoria, activo);
CREATE INDEX idx_lote_producto_fecha ON lote(id_producto, fecha_ingreso, id_lote);
CREATE INDEX idx_lote_disponible_fifo ON lote(id_producto, activo, cantidad_disponible, fecha_ingreso, id_lote);
CREATE INDEX idx_salida_fecha ON nota_salida(fecha_emision);
CREATE INDEX idx_salida_cliente_fecha ON nota_salida(id_cliente, fecha_emision);
CREATE INDEX idx_detalle_producto ON detalle_salida(id_producto);
CREATE INDEX idx_detalle_lote ON detalle_salida(id_lote);
CREATE INDEX idx_kardex_producto_fecha ON movimiento_kardex(id_producto, fecha);
CREATE INDEX idx_kardex_lote ON movimiento_kardex(id_lote);

INSERT INTO categoria (id_categoria, nombre, descripcion) VALUES
  (1, 'Bebidas', 'Gaseosas, aguas, jugos y bebidas listas para vender'),
  (2, 'Abarrotes', 'Productos secos de consumo diario'),
  (3, 'Limpieza', 'Productos de aseo para hogar y negocio');

INSERT INTO proveedor (id_proveedor, ruc, nombre, contacto, telefono, email, direccion) VALUES
  (1, '0999999999001', 'Distribuidora Andina S.A.', 'Maria Perez', '0991112222', 'ventas@andina.example', 'Av. Principal 123'),
  (2, '0998887776001', 'Comercial La Bodega', 'Carlos Mena', '0983334444', 'pedidos@labodega.example', 'Calle Mercado 45'),
  (3, '0995554443001', 'Proveedor Limpio S.A.', 'Lucia Torres', '0975556666', 'contacto@limpio.example', 'Zona Industrial 8');

INSERT INTO cliente (id_cliente, identificacion, nombre, telefono, email, direccion) VALUES
  (1, '0912345678', 'Tienda San Jose', '0992223333', 'compras@sanjose.example', 'Barrio Central'),
  (2, '0923456789', 'MiniMarket La Esquina', '0994445555', 'admin@laesquina.example', 'Av. Norte 22'),
  (3, '0934567890', 'Cliente Mostrador', '0990000000', 'mostrador@bodega.local', 'Venta directa');

INSERT INTO producto (
  id_producto,
  id_categoria,
  sku,
  codigo_barras,
  nombre,
  descripcion,
  unidad_medida,
  stock_minimo,
  stock_actual,
  precio_venta
) VALUES
  (1, 1, 'BEB-COCA-3L', '7861000000011', 'Coca-Cola 3L', 'Bebida gaseosa familiar', 'unidad', 20.000, 30.000, 2.25),
  (2, 2, 'ABA-ARROZ-1KG', '7861000000028', 'Arroz Flor 1kg', 'Arroz blanco grano largo', 'unidad', 30.000, 80.000, 1.20),
  (3, 3, 'LIM-DETER-1KG', '7861000000035', 'Detergente 1kg', 'Detergente en polvo multiuso', 'unidad', 15.000, 40.000, 2.80),
  (4, 1, 'BEB-AGUA-625', '7861000000042', 'Agua 625ml', 'Agua purificada personal', 'unidad', 50.000, 45.000, 0.45);

INSERT INTO lote (
  id_lote,
  id_producto,
  id_proveedor,
  codigo_lote,
  cantidad,
  cantidad_disponible,
  costo_unitario,
  fecha_ingreso,
  fecha_vencimiento,
  factura_referencia
) VALUES
  (1, 1, 1, 'COCA-2026-03-15', 100.000, 0.000, 1.50, '2026-03-15', '2026-09-15', 'FAC-COMP-001'),
  (2, 1, 1, 'COCA-2026-03-28', 50.000, 30.000, 1.55, '2026-03-28', '2026-09-28', 'FAC-COMP-002'),
  (3, 2, 2, 'ARROZ-2026-04-01', 80.000, 80.000, 0.82, '2026-04-01', '2027-04-01', 'FAC-COMP-003'),
  (4, 3, 3, 'DETER-2026-04-10', 40.000, 40.000, 1.95, '2026-04-10', '2027-04-10', 'FAC-COMP-004'),
  (5, 4, 1, 'AGUA-2026-04-12', 45.000, 45.000, 0.25, '2026-04-12', '2026-10-12', 'FAC-COMP-005');

INSERT INTO nota_salida (
  id_salida,
  id_cliente,
  numero_factura,
  fecha_emision,
  subtotal,
  iva,
  total,
  costo_total_fifo,
  utilidad,
  estado,
  observaciones
) VALUES
  (1, 1, 'VENTA-001', '2026-04-30', 270.00, 32.40, 302.40, 181.00, 89.00, 'completada', 'Venta de prueba que consume dos lotes por FIFO');

INSERT INTO detalle_salida (
  id_detalle_salida,
  id_salida,
  id_producto,
  id_lote,
  cantidad,
  precio_unitario,
  subtotal,
  costo_unitario_fifo,
  costo_total_fifo,
  utilidad
) VALUES
  (1, 1, 1, 1, 100.000, 2.25, 225.00, 1.50, 150.00, 75.00),
  (2, 1, 1, 2, 20.000, 2.25, 45.00, 1.55, 31.00, 14.00);

INSERT INTO movimiento_kardex (
  id_movimiento,
  id_producto,
  id_lote,
  id_salida,
  fecha,
  tipo,
  referencia,
  cantidad_entrada,
  costo_unitario_entrada,
  valor_entrada,
  cantidad_salida,
  costo_unitario_salida,
  valor_salida,
  saldo_cantidad,
  saldo_valor,
  observacion
) VALUES
  (1, 1, 1, NULL, '2026-03-15 09:00:00', 'ENTRADA', 'FAC-COMP-001', 100.000, 1.50, 150.00, 0.000, NULL, 0.00, 100.000, 150.00, 'Ingreso lote antiguo para prueba FIFO'),
  (2, 1, 2, NULL, '2026-03-28 09:00:00', 'ENTRADA', 'FAC-COMP-002', 50.000, 1.55, 77.50, 0.000, NULL, 0.00, 150.000, 227.50, 'Ingreso lote nuevo para prueba FIFO'),
  (3, 1, 1, 1, '2026-04-30 10:30:00', 'SALIDA', 'VENTA-001', 0.000, NULL, 0.00, 100.000, 1.50, 150.00, 50.000, 77.50, 'Salida FIFO: se consume completamente el lote antiguo'),
  (4, 1, 2, 1, '2026-04-30 10:31:00', 'SALIDA', 'VENTA-001', 0.000, NULL, 0.00, 20.000, 1.55, 31.00, 30.000, 46.50, 'Salida FIFO: se consumen 20 unidades del segundo lote'),
  (5, 2, 3, NULL, '2026-04-01 09:00:00', 'ENTRADA', 'FAC-COMP-003', 80.000, 0.82, 65.60, 0.000, NULL, 0.00, 80.000, 65.60, 'Ingreso inicial arroz'),
  (6, 3, 4, NULL, '2026-04-10 09:00:00', 'ENTRADA', 'FAC-COMP-004', 40.000, 1.95, 78.00, 0.000, NULL, 0.00, 40.000, 78.00, 'Ingreso inicial detergente'),
  (7, 4, 5, NULL, '2026-04-12 09:00:00', 'ENTRADA', 'FAC-COMP-005', 45.000, 0.25, 11.25, 0.000, NULL, 0.00, 45.000, 11.25, 'Ingreso inicial agua con stock bajo');

-- Sistema de Gestion de Bodega para Microempresas
-- MySQL 8.x
-- Script estructural idempotente: crea la base y las tablas solo si no existen.

CREATE DATABASE IF NOT EXISTS bodega_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bodega_db;

CREATE TABLE IF NOT EXISTS categoria (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  descripcion VARCHAR(255) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS proveedor (
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

CREATE TABLE IF NOT EXISTS cliente (
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

CREATE TABLE IF NOT EXISTS producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  id_categoria INT NOT NULL,
  sku VARCHAR(50) NOT NULL,
  codigo_barras VARCHAR(80) NULL,
  nombre VARCHAR(120) NOT NULL,
  descripcion VARCHAR(255) NULL,
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

CREATE TABLE IF NOT EXISTS lote (
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

CREATE TABLE IF NOT EXISTS nota_salida (
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

CREATE TABLE IF NOT EXISTS detalle_salida (
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

CREATE TABLE IF NOT EXISTS movimiento_kardex (
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

CREATE INDEX IF NOT EXISTS idx_producto_nombre ON producto(nombre);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(id_categoria, activo);
CREATE INDEX IF NOT EXISTS idx_lote_producto_fecha ON lote(id_producto, fecha_ingreso, id_lote);
CREATE INDEX IF NOT EXISTS idx_lote_disponible_fifo ON lote(id_producto, activo, cantidad_disponible, fecha_ingreso, id_lote);
CREATE INDEX IF NOT EXISTS idx_salida_fecha ON nota_salida(fecha_emision);
CREATE INDEX IF NOT EXISTS idx_salida_cliente_fecha ON nota_salida(id_cliente, fecha_emision);
CREATE INDEX IF NOT EXISTS idx_detalle_producto ON detalle_salida(id_producto);
CREATE INDEX IF NOT EXISTS idx_detalle_lote ON detalle_salida(id_lote);
CREATE INDEX IF NOT EXISTS idx_kardex_producto_fecha ON movimiento_kardex(id_producto, fecha);
CREATE INDEX IF NOT EXISTS idx_kardex_lote ON movimiento_kardex(id_lote);

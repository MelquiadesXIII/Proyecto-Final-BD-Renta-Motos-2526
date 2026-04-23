-- =============================================
-- 1. NOMENCLADORES (TABLAS INDEPENDIENTES)
-- =============================================

CREATE TABLE municipio (
    id_municipio     SERIAL PRIMARY KEY,
    nombre_municipio VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE color (
    id_color     SERIAL PRIMARY KEY,
    nombre_color VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE marca (
    id_marca     SERIAL PRIMARY KEY,
    nombre_marca VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE sexo (
    id_sexo     SERIAL PRIMARY KEY,
    nombre_sexo VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE situacion (
    id_situacion     SERIAL PRIMARY KEY,
    nombre_situacion VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE forma_pago (
    id_forma_pago     SERIAL PRIMARY KEY,
    nombre_forma_pago VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id_usuario       SERIAL PRIMARY KEY,
    nombre_usuario   VARCHAR(50)  NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    gmail            VARCHAR(100) NOT NULL UNIQUE
);

-- =============================================
-- 2. TABLAS DEPENDIENTES DE PRIMER NIVEL
-- =============================================

CREATE TABLE modelo (
    id_modelo     SERIAL PRIMARY KEY,
    id_marca      INT NOT NULL REFERENCES marca(id_marca),
    nombre_modelo VARCHAR(100) NOT NULL,
    UNIQUE (id_marca, nombre_modelo)
);

CREATE TABLE cliente (
    id_cliente       SERIAL PRIMARY KEY,
    ci_cliente       CHAR(11)     NOT NULL UNIQUE,
    nombre_cliente   VARCHAR(100) NOT NULL,
    primer_apellido  VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    edad             INT          NOT NULL,
    id_sexo          INT          NOT NULL REFERENCES sexo(id_sexo),
    numero_contacto  VARCHAR(20)  NOT NULL,
    id_municipio     INT          NOT NULL REFERENCES municipio(id_municipio),
    id_usuario       INT          NOT NULL REFERENCES usuario(id_usuario)
);

CREATE TABLE moto (
    id_moto            SERIAL PRIMARY KEY,
    matricula_moto     VARCHAR(10)   NOT NULL UNIQUE,
    id_modelo          INT           NOT NULL REFERENCES modelo(id_modelo),
    id_situacion       INT           NOT NULL REFERENCES situacion(id_situacion) DEFAULT 1,
    id_color           INT           NOT NULL REFERENCES color(id_color),
    cant_km_recorridos NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- =============================================
-- 3. TABLA PRINCIPAL (CONTRATO)
-- =============================================

CREATE TABLE contrato (
    id_contrato      SERIAL PRIMARY KEY,
    fecha_inicio     DATE           NOT NULL,
    id_moto          INT            NOT NULL REFERENCES moto(id_moto),
    id_cliente       INT            NOT NULL REFERENCES cliente(id_cliente) ON DELETE CASCADE,
    id_forma_pago    INT            NOT NULL REFERENCES forma_pago(id_forma_pago),
    fecha_fin        DATE           NOT NULL,
    dias_prorroga    INT            NOT NULL DEFAULT 0,
    seguro_adicional BOOLEAN        NOT NULL DEFAULT FALSE,
    tarifa_normal    NUMERIC(10,2)  NOT NULL,
    tarifa_prorroga  NUMERIC(10,2)  NOT NULL,
    fecha_entrega    DATE,                           
    cant_km_salida   NUMERIC(10,2)  NOT NULL,        
    cant_km_llegada  NUMERIC(10,2)  NULL,            
    UNIQUE (fecha_inicio, id_moto)
);
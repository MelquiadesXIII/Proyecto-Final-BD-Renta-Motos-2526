-- Tipos ENUM
CREATE TYPE tipo_sexo AS ENUM ('masculino', 'femenino');
CREATE TYPE tipo_forma_pago AS ENUM ('efectivo', 'cheque', 'credito');
CREATE TYPE tipo_situacion AS ENUM ('disponible', 'alquilada', 'taller');

-- Tablas de nomencladores (sin dependencias)
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

-- Tablas con dependencias de primer nivel
CREATE TABLE modelo (
    id_modelo     SERIAL PRIMARY KEY,
    id_marca      INT NOT NULL REFERENCES marca(id_marca),
    nombre_modelo VARCHAR(100) NOT NULL,
    UNIQUE (id_marca, nombre_modelo)
);

CREATE TABLE cliente (
    ci_cliente         CHAR(11)     PRIMARY KEY,
    nombre_cliente     VARCHAR(100) NOT NULL,
    primer_apellido    VARCHAR(100) NOT NULL,
    segundo_apellido   VARCHAR(100),
    edad               INT          NOT NULL,
    sexo               tipo_sexo    NOT NULL,
    numero_contacto    VARCHAR(20)  NOT NULL,
    id_municipio       INT          NOT NULL REFERENCES municipio(id_municipio)
);

CREATE TABLE moto (
    matricula_moto    VARCHAR(10) PRIMARY KEY,
    id_modelo         INT         NOT NULL REFERENCES modelo(id_modelo),
    situacion         tipo_situacion NOT NULL DEFAULT 'disponible',
    id_color          INT         NOT NULL REFERENCES color(id_color),
    cant_km_recorridos NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- Tabla principal con dependencias de segundo nivel
CREATE TABLE contrato (
    fecha_inicio      DATE        NOT NULL,
    matricula_moto    VARCHAR(10) NOT NULL REFERENCES moto(matricula_moto),
    ci_cliente        CHAR(11)    NOT NULL REFERENCES cliente(ci_cliente) ON DELETE CASCADE,
    forma_pago        tipo_forma_pago NOT NULL,
    fecha_fin         DATE        NOT NULL,
    dias_prorroga     INT         NOT NULL DEFAULT 0,
    seguro_adicional  BOOLEAN     NOT NULL DEFAULT FALSE,
    tarifa_normal     NUMERIC(10,2) NOT NULL,
    tarifa_prorroga   NUMERIC(10,2) NOT NULL,
    fecha_entrega     DATE,
    PRIMARY KEY (fecha_inicio, matricula_moto)
);

-- Trigger: al crear un contrato, la moto pasa a estado "alquilada"
CREATE OR REPLACE FUNCTION set_moto_alquilada()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE moto SET situacion = 'alquilada'
    WHERE matricula_moto = NEW.matricula_moto;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_moto_alquilada
AFTER INSERT ON contrato
FOR EACH ROW EXECUTE FUNCTION set_moto_alquilada();

-- Trigger: no permitir alquilar moto que no esté disponible
CREATE OR REPLACE FUNCTION check_moto_disponible()
RETURNS TRIGGER AS $$
DECLARE
    v_situacion tipo_situacion;
BEGIN
    SELECT situacion INTO v_situacion
    FROM moto
    WHERE matricula_moto = NEW.matricula_moto;

    IF v_situacion <> 'disponible' THEN
        RAISE EXCEPTION 'La moto % no está disponible (estado: %)', NEW.matricula_moto, v_situacion;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_disponible
BEFORE INSERT ON contrato
FOR EACH ROW EXECUTE FUNCTION check_moto_disponible();

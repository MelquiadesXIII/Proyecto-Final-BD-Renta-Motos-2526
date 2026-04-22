-- ============================================================
-- V5: Migración de ENUMs a tablas lookup relacionales
-- ============================================================

-- ============================================================
-- PASO 1: Crear tablas de catálogo
-- ============================================================

CREATE TABLE sexo (
    id_sexo SERIAL PRIMARY KEY,
    nombre  VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE situacion (
    id_situacion SERIAL PRIMARY KEY,
    nombre       VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE forma_pago (
    id_forma_pago SERIAL PRIMARY KEY,
    nombre        VARCHAR(20) UNIQUE NOT NULL
);

-- ============================================================
-- PASO 2: Insertar valores de catálogo
-- ============================================================

INSERT INTO sexo (nombre) VALUES ('masculino'), ('femenino');

INSERT INTO situacion (nombre) VALUES ('disponible'), ('alquilada'), ('taller');

INSERT INTO forma_pago (nombre) VALUES ('efectivo'), ('cheque'), ('credito');

-- ============================================================
-- PASO 3: Agregar columnas FK (nullable temporalmente)
-- ============================================================

ALTER TABLE cliente  ADD COLUMN id_sexo       INT REFERENCES sexo(id_sexo);
ALTER TABLE moto     ADD COLUMN id_situacion  INT REFERENCES situacion(id_situacion);
ALTER TABLE contrato ADD COLUMN id_forma_pago INT REFERENCES forma_pago(id_forma_pago);

-- ============================================================
-- PASO 4: Migrar datos existentes (ENUM → ID)
-- ============================================================

UPDATE cliente  c  SET id_sexo       = s.id_sexo       FROM sexo       s  WHERE s.nombre = c.sexo::TEXT;
UPDATE moto     m  SET id_situacion  = si.id_situacion  FROM situacion  si WHERE si.nombre = m.situacion::TEXT;
UPDATE contrato co SET id_forma_pago = fp.id_forma_pago FROM forma_pago fp WHERE fp.nombre = co.forma_pago::TEXT;

-- ============================================================
-- PASO 5: Establecer NOT NULL en columnas FK ya migradas
-- ============================================================

ALTER TABLE cliente  ALTER COLUMN id_sexo       SET NOT NULL;
ALTER TABLE moto     ALTER COLUMN id_situacion  SET NOT NULL;
ALTER TABLE contrato ALTER COLUMN id_forma_pago SET NOT NULL;

-- ============================================================
-- PASO 6: Reescribir stored procedures que usan tipos ENUM
--         (deben reescribirse ANTES de DROP TYPE)
-- ============================================================

-- Procedimiento: insertar_cliente_si_no_existe
-- Antes usaba: sexo tipo_sexo
-- Ahora usa:   sexo_nombre VARCHAR(20) y resuelve el ID internamente
CREATE OR REPLACE PROCEDURE insertar_cliente_si_no_existe(
    ci           CHAR(11),
    nom          VARCHAR(100),
    primer_ape   VARCHAR(100),
    segundo_ape  VARCHAR(100),
    edad_val     INT,
    sexo_nombre  VARCHAR(20),
    num_cont     VARCHAR(20),
    id_mun       INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_sexo INT;
BEGIN
    SELECT id_sexo INTO v_id_sexo
    FROM sexo
    WHERE nombre = sexo_nombre;

    IF v_id_sexo IS NULL THEN
        RAISE EXCEPTION 'El sexo "%" no existe en la tabla de catálogo.', sexo_nombre;
    END IF;

    INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, id_sexo, numero_contacto, id_municipio)
    VALUES (ci, nom, primer_ape, segundo_ape, edad_val, v_id_sexo, num_cont, id_mun)
    ON CONFLICT (ci_cliente) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'El cliente % de ci: % insertado correctamente.', nom, ci;
    ELSE
        RAISE NOTICE 'El cliente % de ci: % ya existía, no se insertó.', nom, ci;
    END IF;
END;
$$;

-- Procedimiento: insertar_contrato_si_no_existe
-- Antes usaba: forma tipo_forma_pago
-- Ahora usa:   forma_nombre VARCHAR(20) y resuelve el ID internamente
CREATE OR REPLACE PROCEDURE insertar_contrato_si_no_existe(
    fec_ini        DATE,
    mat            VARCHAR(10),
    ci_cli         CHAR(11),
    forma_nombre   VARCHAR(20),
    fec_fin        DATE,
    tarifa_norm    NUMERIC(10,2),
    dias_pro       INT DEFAULT 0,
    seguro_adic    BOOLEAN DEFAULT FALSE,
    tarifa_pro     NUMERIC(10,2) DEFAULT 0,
    fec_ent        DATE DEFAULT NULL,
    km_sal         NUMERIC(10,2) DEFAULT 0,
    km_lle         NUMERIC(10,2) DEFAULT 0
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_forma_pago INT;
BEGIN
    SELECT id_forma_pago INTO v_id_forma_pago
    FROM forma_pago
    WHERE nombre = forma_nombre;

    IF v_id_forma_pago IS NULL THEN
        RAISE EXCEPTION 'La forma de pago "%" no existe en la tabla de catálogo.', forma_nombre;
    END IF;

    INSERT INTO contrato (
        fecha_inicio, matricula_moto, ci_cliente, id_forma_pago,
        fecha_fin, dias_prorroga, seguro_adicional,
        tarifa_normal, tarifa_prorroga, fecha_entrega,
        cant_km_salida, cant_km_llegada
    )
    VALUES (
        fec_ini, mat, ci_cli, v_id_forma_pago,
        fec_fin, dias_pro, seguro_adic,
        tarifa_norm, tarifa_pro, fec_ent,
        km_sal, km_lle
    )
    ON CONFLICT (fecha_inicio, matricula_moto) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'Contrato de moto % con fecha % fue insertado correctamente.', mat, fec_ini;
    ELSE
        RAISE NOTICE 'El contrato de la moto % con fecha % no se insertó porque ya existía.', mat, fec_ini;
    END IF;
END;
$$;

-- ============================================================
-- PASO 7: Reescribir funciones de trigger
--         (deben reescribirse ANTES de DROP TYPE tipo_situacion)
-- ============================================================

-- Trigger: al crear un contrato, la moto pasa a estado "alquilada"
CREATE OR REPLACE FUNCTION set_moto_alquilada()
RETURNS TRIGGER AS $$
DECLARE
    v_id_situacion_alquilada INT;
BEGIN
    SELECT id_situacion INTO v_id_situacion_alquilada
    FROM situacion
    WHERE nombre = 'alquilada';

    UPDATE moto SET id_situacion = v_id_situacion_alquilada
    WHERE matricula_moto = NEW.matricula_moto;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: no permitir alquilar moto que no esté disponible
CREATE OR REPLACE FUNCTION check_moto_disponible()
RETURNS TRIGGER AS $$
DECLARE
    v_situacion_nombre VARCHAR(20);
BEGIN
    SELECT si.nombre INTO v_situacion_nombre
    FROM moto m
    JOIN situacion si ON m.id_situacion = si.id_situacion
    WHERE m.matricula_moto = NEW.matricula_moto;

    IF v_situacion_nombre <> 'disponible' THEN
        RAISE EXCEPTION 'La moto % no está disponible (estado: %)', NEW.matricula_moto, v_situacion_nombre;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- PASO 8: Eliminar columnas ENUM antiguas
-- ============================================================

ALTER TABLE cliente  DROP COLUMN sexo;
ALTER TABLE moto     DROP COLUMN situacion;
ALTER TABLE contrato DROP COLUMN forma_pago;

-- ============================================================
-- PASO 9: Eliminar tipos ENUM (ya sin dependencias)
-- ============================================================

DROP TYPE tipo_sexo;
DROP TYPE tipo_situacion;
DROP TYPE tipo_forma_pago;

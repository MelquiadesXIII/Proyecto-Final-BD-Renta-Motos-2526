-- V37__historico_ingresos_al_borrar.sql

-- 1. Crear tabla para almacenar el histórico de los ingresos de contratos finalizados
CREATE TABLE IF NOT EXISTS historico_ingresos (
    id_contrato INT PRIMARY KEY,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    fecha_entrega DATE NOT NULL,
    id_moto INT NOT NULL,
    id_modelo INT NOT NULL,
    id_marca INT NOT NULL,
    id_forma_pago INT NOT NULL,
    id_municipio INT,
    dias_prorroga INT NOT NULL,
    importe NUMERIC(10,2) NOT NULL
);

-- 2. Migrar los contratos finalizados existentes a la tabla histórica
INSERT INTO historico_ingresos (
    id_contrato,
    fecha_inicio,
    fecha_fin,
    fecha_entrega,
    id_moto,
    id_modelo,
    id_marca,
    id_forma_pago,
    id_municipio,
    dias_prorroga,
    importe
)
SELECT 
    c.id_contrato,
    c.fecha_inicio,
    c.fecha_fin,
    c.fecha_entrega,
    c.id_moto,
    m.id_modelo,
    mo.id_marca,
    c.id_forma_pago,
    cli.id_municipio,
    c.dias_prorroga,
    ((c.fecha_fin - c.fecha_inicio + 1) * c.tarifa_normal) +
    (CASE
        WHEN c.fecha_entrega > c.fecha_fin THEN (c.fecha_entrega - c.fecha_fin) * c.tarifa_prorroga
        ELSE 0
     END)
FROM contrato c
JOIN moto m ON c.id_moto = m.id_moto
JOIN modelo mo ON m.id_modelo = mo.id_modelo
LEFT JOIN cliente cli ON c.id_cliente = cli.id_cliente
WHERE c.fecha_entrega IS NOT NULL
ON CONFLICT (id_contrato) DO NOTHING;

-- 3. Crear Trigger para mantener actualizada la tabla histórica al crear/modificar contratos
CREATE OR REPLACE FUNCTION actualizar_historico_ingresos()
RETURNS TRIGGER AS $$
DECLARE
    v_id_modelo INT;
    v_id_marca INT;
    v_id_municipio INT;
    v_importe NUMERIC(10,2);
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        IF NEW.fecha_entrega IS NOT NULL THEN
            -- Obtener relaciones
            SELECT m.id_modelo, mo.id_marca, cli.id_municipio
            INTO v_id_modelo, v_id_marca, v_id_municipio
            FROM moto m
            JOIN modelo mo ON m.id_modelo = mo.id_modelo
            LEFT JOIN cliente cli ON cli.id_cliente = NEW.id_cliente
            WHERE m.id_moto = NEW.id_moto;

            -- Calcular el importe
            v_importe := ((NEW.fecha_fin - NEW.fecha_inicio + 1) * NEW.tarifa_normal) +
                         (CASE
                             WHEN NEW.fecha_entrega > NEW.fecha_fin THEN (NEW.fecha_entrega - NEW.fecha_fin) * NEW.tarifa_prorroga
                             ELSE 0
                          END);

            -- Insertar o actualizar. Si id_municipio es nulo (por borrado de cliente), conservamos el anterior si existe
            INSERT INTO historico_ingresos (
                id_contrato, fecha_inicio, fecha_fin, fecha_entrega,
                id_moto, id_modelo, id_marca, id_forma_pago,
                id_municipio, dias_prorroga, importe
            )
            VALUES (
                NEW.id_contrato, NEW.fecha_inicio, NEW.fecha_fin, NEW.fecha_entrega,
                NEW.id_moto, v_id_modelo, v_id_marca, NEW.id_forma_pago,
                v_id_municipio, NEW.dias_prorroga, v_importe
            )
            ON CONFLICT (id_contrato) DO UPDATE SET
                fecha_inicio = EXCLUDED.fecha_inicio,
                fecha_fin = EXCLUDED.fecha_fin,
                fecha_entrega = EXCLUDED.fecha_entrega,
                id_moto = EXCLUDED.id_moto,
                id_modelo = EXCLUDED.id_modelo,
                id_marca = EXCLUDED.id_marca,
                id_forma_pago = EXCLUDED.id_forma_pago,
                id_municipio = COALESCE(EXCLUDED.id_municipio, historico_ingresos.id_municipio),
                dias_prorroga = EXCLUDED.dias_prorroga,
                importe = EXCLUDED.importe;
        ELSE
            -- Si ya no está finalizado, lo quitamos
            DELETE FROM historico_ingresos WHERE id_contrato = NEW.id_contrato;
        END IF;
    END IF;
    -- En DELETE no hacemos nada para conservar el registro histórico.
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_actualizar_historico_ingresos ON contrato;
CREATE TRIGGER trg_actualizar_historico_ingresos
AFTER INSERT OR UPDATE ON contrato
FOR EACH ROW
EXECUTE FUNCTION actualizar_historico_ingresos();

-- 4. Redefinir las funciones de reporte para consumir del histórico persistente

CREATE OR REPLACE FUNCTION ingreso_mes(p_mes INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos
    WHERE fecha_entrega <= CURRENT_DATE
      AND EXTRACT(YEAR FROM fecha_entrega) = EXTRACT(YEAR FROM CURRENT_DATE)
      AND EXTRACT(MONTH FROM fecha_entrega) = p_mes;
$$;

CREATE OR REPLACE FUNCTION ingreso_anual()
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos
    WHERE fecha_entrega <= CURRENT_DATE
      AND EXTRACT(YEAR FROM fecha_entrega) = EXTRACT(YEAR FROM CURRENT_DATE);
$$;

CREATE OR REPLACE FUNCTION cant_motos_mod_y_marca(p_id_marca INT, p_id_modelo INT)
RETURNS BIGINT
LANGUAGE sql
AS $$
    SELECT COUNT(DISTINCT id_moto)
    FROM historico_ingresos
    WHERE id_modelo = p_id_modelo
      AND id_marca = p_id_marca
      AND fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION cant_dias_totales_alquilado_marc_mod(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT SUM(
        (fecha_fin - fecha_inicio + 1) +
        CASE WHEN fecha_entrega > fecha_fin THEN (fecha_entrega - fecha_fin) ELSE 0 END
    )
    FROM historico_ingresos
    WHERE id_modelo = p_id_modelo
      AND id_marca = p_id_marca
      AND fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION ingresos_tarjeta_mod_marca(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos h
    JOIN forma_pago fp ON h.id_forma_pago = fp.id_forma_pago
    WHERE h.id_modelo = p_id_modelo
      AND h.id_marca = p_id_marca
      AND h.fecha_entrega <= CURRENT_DATE
      AND fp.nombre_forma_pago = 'Tarjeta de crédito';
$$;

CREATE OR REPLACE FUNCTION ingresos_cheque_mod_marca(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos h
    JOIN forma_pago fp ON h.id_forma_pago = fp.id_forma_pago
    WHERE h.id_modelo = p_id_modelo
      AND h.id_marca = p_id_marca
      AND h.fecha_entrega <= CURRENT_DATE
      AND fp.nombre_forma_pago = 'Cheque';
$$;

CREATE OR REPLACE FUNCTION ingresos_efectivo_mod_marca(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos h
    JOIN forma_pago fp ON h.id_forma_pago = fp.id_forma_pago
    WHERE h.id_modelo = p_id_modelo
      AND h.id_marca = p_id_marca
      AND h.fecha_entrega <= CURRENT_DATE
      AND fp.nombre_forma_pago = 'Efectivo';
$$;

CREATE OR REPLACE FUNCTION total_ingresos_marca(p_id_marca INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos
    WHERE id_marca = p_id_marca
      AND fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION total_ingresos_general()
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos
    WHERE fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION resumen_contratos_por_marcas_modelos()
RETURNS TABLE (
    "Fecha"                 DATE,
    "Marca"                 TEXT,
    "Modelo"                TEXT,
    "Cantidad de motos"     BIGINT,
    "Días totales"          NUMERIC,
    "Ingresos tarjeta"      NUMERIC(10,2),
    "Ingresos cheque"       NUMERIC(10,2),
    "Ingresos efectivo"     NUMERIC(10,2),
    "Total ingresos marca"  NUMERIC(10,2),
    "Total general ingresos" NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        CURRENT_DATE,
        ma.nombre_marca,
        mo.nombre_modelo,
        cant_motos_mod_y_marca(ma.id_marca, mo.id_modelo),
        cant_dias_totales_alquilado_marc_mod(ma.id_marca, mo.id_modelo),
        ingresos_tarjeta_mod_marca(ma.id_marca, mo.id_modelo),
        ingresos_cheque_mod_marca(ma.id_marca, mo.id_modelo),
        ingresos_efectivo_mod_marca(ma.id_marca, mo.id_modelo),
        total_ingresos_marca(ma.id_marca),
        total_ingresos_general()
    FROM marca ma
    JOIN modelo mo ON mo.id_marca = ma.id_marca
    WHERE EXISTS (
        SELECT 1
        FROM historico_ingresos h
        WHERE h.id_modelo = mo.id_modelo
          AND h.fecha_entrega <= CURRENT_DATE
    )
    ORDER BY ma.nombre_marca, mo.nombre_modelo;
END;
$$;

CREATE OR REPLACE FUNCTION cant_dias_alquilados_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(
        (fecha_fin - fecha_inicio + 1)
    ), 0)
    FROM historico_ingresos
    WHERE (id_municipio = p_id_municipio OR (id_municipio IS NULL AND p_id_municipio IS NULL))
      AND id_modelo = p_id_modelo
      AND id_marca = p_id_marca
      AND fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION cant_dias_prorroga_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(dias_prorroga), 0)
    FROM historico_ingresos
    WHERE (id_municipio = p_id_municipio OR (id_municipio IS NULL AND p_id_municipio IS NULL))
      AND id_modelo = p_id_modelo
      AND id_marca = p_id_marca
      AND fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION valor_efectivo_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(
        CASE WHEN fp.nombre_forma_pago = 'Efectivo'
             THEN importe
             ELSE 0 END
    ), 0)
    FROM historico_ingresos h
    JOIN forma_pago fp ON h.id_forma_pago = fp.id_forma_pago
    WHERE (h.id_municipio = p_id_municipio OR (h.id_municipio IS NULL AND p_id_municipio IS NULL))
      AND h.id_modelo = p_id_modelo
      AND h.id_marca = p_id_marca
      AND h.fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION valor_total_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(importe), 0)
    FROM historico_ingresos
    WHERE (id_municipio = p_id_municipio OR (id_municipio IS NULL AND p_id_municipio IS NULL))
      AND id_modelo = p_id_modelo
      AND id_marca = p_id_marca
      AND fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION resumen_contratos_por_municipios()
RETURNS TABLE (
    "Fecha"                DATE,
    "Municipio"            VARCHAR(100),
    "Marca"                VARCHAR(100),
    "Modelo"               VARCHAR(100),
    "Días alquilados"      NUMERIC,
    "Días de prórroga"     NUMERIC,
    "Valor en efectivo"    NUMERIC(10,2),
    "Valor total general"  NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT
        CURRENT_DATE,
        COALESCE(muni.nombre_municipio, 'Municipio Desconocido')::VARCHAR(100),
        ma.nombre_marca::VARCHAR(100),
        mo.nombre_modelo::VARCHAR(100),
        COALESCE(cant_dias_alquilados_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo), 0)::NUMERIC,
        COALESCE(cant_dias_prorroga_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo), 0)::NUMERIC,
        COALESCE(valor_efectivo_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo), 0)::NUMERIC(10,2),
        COALESCE(valor_total_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo), 0)::NUMERIC(10,2)
    FROM historico_ingresos h
    LEFT JOIN municipio muni ON h.id_municipio = muni.id_municipio
    JOIN modelo mo ON h.id_modelo = mo.id_modelo
    JOIN marca ma ON mo.id_marca = ma.id_marca
    WHERE h.fecha_entrega <= CURRENT_DATE
    ORDER BY 2, 3, 4;
END;
$$;

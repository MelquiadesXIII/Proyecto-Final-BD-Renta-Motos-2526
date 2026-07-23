-- V36__mantener_contratos_finalizados.sql

-- 1. Cambiar la restricción de la llave foránea para que al borrar el cliente
--    se establezca en NULL en lugar de CASCADE (esto conservará los contratos finalizados).
ALTER TABLE contrato DROP CONSTRAINT contrato_id_cliente_fkey;
ALTER TABLE contrato ALTER COLUMN id_cliente DROP NOT NULL;
ALTER TABLE contrato ADD CONSTRAINT contrato_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE SET NULL;

-- 2. Trigger para asegurar que los contratos activos sí se eliminen cuando
--    se elimina un cliente (porque una moto no puede quedar alquilada por un cliente que no existe).
CREATE OR REPLACE FUNCTION eliminar_contratos_activos_cliente()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM contrato WHERE id_cliente = OLD.id_cliente AND fecha_entrega IS NULL;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_eliminar_contratos_activos ON cliente;

CREATE TRIGGER trg_eliminar_contratos_activos
BEFORE DELETE ON cliente
FOR EACH ROW
EXECUTE FUNCTION eliminar_contratos_activos_cliente();

-- 3. Actualizar los reportes que hacen JOIN con cliente para que muestren "Cliente Eliminado"
--    en caso de que el cliente haya sido borrado pero su contrato siga existiendo.

CREATE OR REPLACE FUNCTION listado_contratos()
RETURNS TABLE (
    "Nombre del cliente"       TEXT,
    "Matrícula"                VARCHAR(10),
    "Marca"                    VARCHAR(100),
    "Modelo"                   VARCHAR(100),
    "Forma de pago"            VARCHAR(20),
    "Fecha inicio"             DATE,
    "Fecha fin"                DATE,
    "Prórroga (días)"          INTEGER,
    "Seguro adicional"         TEXT,
    "Importe total"            NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(cl.nombre_cliente || ' ' || cl.primer_apellido || ' ' || COALESCE(cl.segundo_apellido, ''), 'Cliente Eliminado'),
        m.matricula_moto,
        ma.nombre_marca,
        mo.nombre_modelo,
        fp.nombre_forma_pago,
        c.fecha_inicio,
        c.fecha_fin,
        c.dias_prorroga,
        CASE WHEN c.seguro_adicional THEN 'Sí' ELSE 'No' END,
        calcular_monto_contrato(c.id_contrato)
    FROM contrato c
    LEFT JOIN cliente cl      ON c.id_cliente = cl.id_cliente
    JOIN moto m           ON c.id_moto = m.id_moto
    JOIN modelo mo        ON m.id_modelo = mo.id_modelo
    JOIN marca ma         ON mo.id_marca = ma.id_marca
    JOIN forma_pago fp    ON c.id_forma_pago = fp.id_forma_pago
    ORDER BY c.id_contrato;  
END;
$$;


CREATE OR REPLACE FUNCTION lista_incumplidores()
RETURNS TABLE (
    "Fecha actual"              DATE,
    "Nombres y apellidos"       TEXT,
    "Fecha fin del contrato"    DATE,
    "Fecha de entrega"          DATE
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        CURRENT_DATE,
        COALESCE(cl.nombre_cliente || ' ' || cl.primer_apellido || ' ' || COALESCE(cl.segundo_apellido, ''), 'Cliente Eliminado'),
        c.fecha_fin,
        c.fecha_entrega
    FROM contrato c
    LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente
    WHERE c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega > c.fecha_fin
    ORDER BY c.id_contrato;
END;
$$;


-- Para resumen_contratos_por_municipios, actualizamos las funciones auxiliares
-- para considerar contratos con id_cliente NULL bajo un "Municipio Desconocido".

CREATE OR REPLACE FUNCTION cant_dias_alquilados_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(
        (c.fecha_fin - c.fecha_inicio + 1)
    ), 0)
    FROM contrato c
    LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE (cl.id_municipio = p_id_municipio OR (cl.id_municipio IS NULL AND p_id_municipio IS NULL))
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION cant_dias_prorroga_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(c.dias_prorroga), 0)
    FROM contrato c
    LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE (cl.id_municipio = p_id_municipio OR (cl.id_municipio IS NULL AND p_id_municipio IS NULL))
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
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
             THEN calcular_monto_contrato(c.id_contrato)
             ELSE 0 END
    ), 0)
    FROM contrato c
    LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN forma_pago fp ON c.id_forma_pago = fp.id_forma_pago
    WHERE (cl.id_municipio = p_id_municipio OR (cl.id_municipio IS NULL AND p_id_municipio IS NULL))
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

CREATE OR REPLACE FUNCTION valor_total_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(calcular_monto_contrato(c.id_contrato)), 0)
    FROM contrato c
    LEFT JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE (cl.id_municipio = p_id_municipio OR (cl.id_municipio IS NULL AND p_id_municipio IS NULL))
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
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
    FROM contrato c
    LEFT JOIN cliente cli ON c.id_cliente = cli.id_cliente
    LEFT JOIN municipio muni ON cli.id_municipio = muni.id_municipio
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN marca ma ON mo.id_marca = ma.id_marca
    WHERE c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE
    ORDER BY 2, 3, 4;
END;
$$;

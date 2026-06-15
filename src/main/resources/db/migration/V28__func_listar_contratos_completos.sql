-- Primero elimina la versión anterior
DROP FUNCTION IF EXISTS listar_contratos_completos();

CREATE OR REPLACE FUNCTION listar_contratos_completos()
RETURNS TABLE (
    id_contrato             INT,
    fecha_inicio            DATE,
    id_moto                 INT,
    matricula_moto          VARCHAR,
    marca_moto              VARCHAR,
    modelo_moto             VARCHAR,
    id_cliente              INT,
    ci_cliente              CHAR(11),
    nombre_completo_cliente TEXT,
    fecha_fin               DATE,
    id_forma_pago           INT,
    nombre_forma_pago       VARCHAR,
    dias_prorroga           INT,
    seguro_adicional        BOOLEAN,
    tarifa_normal           NUMERIC,
    tarifa_prorroga         NUMERIC,
    fecha_entrega           DATE,
    cant_km_salida          NUMERIC,
    cant_km_llegada         NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT co.id_contrato,
           co.fecha_inicio,
           co.id_moto,
           m.matricula_moto,
           ma.nombre_marca,
           mo.nombre_modelo,
           co.id_cliente,
           cl.ci_cliente,
           cl.nombre_cliente || ' ' || cl.primer_apellido AS nombre_completo_cliente,
           co.fecha_fin,
           co.id_forma_pago,
           fp.nombre_forma_pago,
           co.dias_prorroga,
           co.seguro_adicional,
           co.tarifa_normal,
           co.tarifa_prorroga,
           co.fecha_entrega,
           co.cant_km_salida,
           co.cant_km_llegada
    FROM contrato co
    JOIN cliente cl    ON co.id_cliente = cl.id_cliente
    JOIN moto m        ON co.id_moto = m.id_moto
    JOIN modelo mo     ON m.id_modelo = mo.id_modelo
    JOIN marca ma      ON mo.id_marca = ma.id_marca
    JOIN forma_pago fp ON co.id_forma_pago = fp.id_forma_pago
    ORDER BY co.fecha_inicio DESC;
END;
$$;
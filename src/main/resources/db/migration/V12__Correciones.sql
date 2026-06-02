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
        ma.nombre_marca::TEXT,
        mo.nombre_modelo::TEXT,
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
        FROM contrato c
        JOIN moto m ON c.id_moto = m.id_moto
        WHERE m.id_modelo = mo.id_modelo
          AND c.fecha_entrega IS NOT NULL
          AND c.fecha_entrega <= CURRENT_DATE
    )
    ORDER BY ma.nombre_marca, mo.nombre_modelo;
END;
$$;
-- Elimina "Fecha" e "Ingreso total anual" del resultado de listado_ingresos_anuales.
-- El total anual se calcula en el controller sumando los ingresos mensuales,
-- evitando así que aparezca repetido en las 12 filas.

DROP FUNCTION IF EXISTS listado_ingresos_anuales();

CREATE OR REPLACE FUNCTION listado_ingresos_anuales()
RETURNS TABLE (
    "Mes"             TEXT,
    "Ingreso mensual" NUMERIC(10,2)
)
LANGUAGE sql
AS $$
    SELECT
        TRIM(TO_CHAR(TO_DATE(s.mes::text, 'MM'), 'Month')) AS nombre_mes,
        ingreso_mes(s.mes)
    FROM generate_series(1, 12) AS s(mes)
    ORDER BY s.mes;
$$;

-- Traduce el nombre del mes a español explícitamente

DROP FUNCTION IF EXISTS listado_ingresos_anuales();

CREATE OR REPLACE FUNCTION listado_ingresos_anuales()
RETURNS TABLE (
    "Mes"             TEXT,
    "Ingreso mensual" NUMERIC(10,2)
)
LANGUAGE sql
AS $$
    SELECT
        CASE s.mes
            WHEN 1 THEN 'Enero'
            WHEN 2 THEN 'Febrero'
            WHEN 3 THEN 'Marzo'
            WHEN 4 THEN 'Abril'
            WHEN 5 THEN 'Mayo'
            WHEN 6 THEN 'Junio'
            WHEN 7 THEN 'Julio'
            WHEN 8 THEN 'Agosto'
            WHEN 9 THEN 'Septiembre'
            WHEN 10 THEN 'Octubre'
            WHEN 11 THEN 'Noviembre'
            WHEN 12 THEN 'Diciembre'
        END AS nombre_mes,
        ingreso_mes(s.mes)
    FROM generate_series(1, 12) AS s(mes)
    ORDER BY s.mes;
$$;

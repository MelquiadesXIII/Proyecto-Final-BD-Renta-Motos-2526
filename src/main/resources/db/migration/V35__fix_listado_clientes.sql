-- =====================================================
-- FUNCION: listado completo de clientes
-- =====================================================
-- Se sobreescribe la función para que cuente todos los contratos 
-- (activos y finalizados), no solo los finalizados.
CREATE OR REPLACE FUNCTION listado_clientes()
RETURNS TABLE (
    "Fecha de hoy" DATE,
    "Municipio" VARCHAR(100),
    "Nombre" VARCHAR(100),
    "CI" CHAR(11),
    "Cantidad de Contratos contratados" INTEGER,
    "Total de Dinero gastado" NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        CURRENT_DATE,
        m.nombre_municipio,
        c.nombre_cliente,
        c.ci_cliente,
        CAST((SELECT COUNT(*) FROM contrato WHERE id_cliente = c.id_cliente) AS INTEGER),
        dinero_gastado(c.id_cliente)
    FROM cliente c
    JOIN municipio m ON m.id_municipio = c.id_municipio
    ORDER BY m.nombre_municipio, c.nombre_cliente;
END;
$$;

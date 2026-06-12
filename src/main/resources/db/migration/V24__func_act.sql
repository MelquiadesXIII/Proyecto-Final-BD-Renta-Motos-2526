CREATE OR REPLACE FUNCTION obtener_motos_libres(p_fecha_inicio DATE, p_fecha_fin DATE)
RETURNS TABLE(
    id_moto INT,
    matricula_moto VARCHAR(10),
    nombre_marca VARCHAR(100),
    nombre_modelo VARCHAR(100),
    nombre_color VARCHAR(50)
)
LANGUAGE sql
AS $$
    SELECT m.id_moto, m.matricula_moto, ma.nombre_marca, mo.nombre_modelo, c.nombre_color
    FROM moto m
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN marca ma ON mo.id_marca = ma.id_marca
    JOIN color c ON m.id_color = c.id_color
    WHERE NOT EXISTS (
        SELECT 1
        FROM contrato co
        WHERE co.id_moto = m.id_moto
          AND co.fecha_entrega IS NULL
          AND (co.fecha_inicio, co.fecha_fin) OVERLAPS (p_fecha_inicio, p_fecha_fin)
    )
    ORDER BY ma.nombre_marca, mo.nombre_modelo;
$$;
-- Excluye contratos ya finalizados (fecha_entrega IS NOT NULL) al buscar motos libres.
-- Un contrato finalizado no debe bloquear la disponibilidad de la moto.
DROP FUNCTION IF EXISTS obtener_motos_libres(DATE, DATE);

CREATE OR REPLACE FUNCTION obtener_motos_libres(p_fecha_inicio DATE, p_fecha_fin DATE)
RETURNS TABLE (
    id_moto INT,
    matricula_moto VARCHAR,
    nombre_marca VARCHAR,
    nombre_modelo VARCHAR,
    nombre_color VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT m.id_moto,
           m.matricula_moto,
           ma.nombre_marca,
           mo.nombre_modelo,
           c.nombre_color
    FROM moto m
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN marca ma ON mo.id_marca = ma.id_marca
    JOIN color c ON m.id_color = c.id_color
    WHERE NOT EXISTS (
        SELECT 1 FROM contrato co
        WHERE co.id_moto = m.id_moto
          AND co.fecha_entrega IS NULL
          AND co.fecha_inicio <= p_fecha_fin
          AND co.fecha_fin   >= p_fecha_inicio
    )
    AND m.id_situacion NOT IN (
        SELECT id_situacion FROM situacion WHERE nombre_situacion = 'Taller'
    );
END;
$$;

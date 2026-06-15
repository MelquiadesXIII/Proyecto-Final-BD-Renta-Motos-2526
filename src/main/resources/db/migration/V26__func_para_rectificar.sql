
DROP FUNCTION IF EXISTS obtener_motos_libres(date, date);
CREATE OR REPLACE FUNCTION obtener_motos_libres(fecha_inicio DATE, fecha_fin DATE)
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
          AND co.fecha_inicio <= fecha_fin
          AND co.fecha_fin >= fecha_inicio
    )
    AND m.id_situacion NOT IN (
        SELECT id_situacion FROM situacion WHERE nombre_situacion = 'taller'
    );
END;
$$;
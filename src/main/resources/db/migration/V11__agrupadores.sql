-- Función que devuelve el nombre de todos los colores
CREATE OR REPLACE FUNCTION obtener_colores()
RETURNS TABLE (nombre_color VARCHAR)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT c.nombre_color
    FROM Color c;
END;
$$;

-- SELECT * FROM obtener_colores();


-- Funcion que devuelve los detalles de las motos
CREATE OR REPLACE FUNCTION obtener_detalles_motos()
RETURNS TABLE (
    matricula_moto   VARCHAR,
    nombre_modelo    VARCHAR,
    nombre_situacion VARCHAR,
    nombre_color     VARCHAR,
    cant_km_recorridos INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT m.matricula_moto,
           mo.nombre_modelo,
           s.nombre_situacion,
           co.nombre_color,
           m.cant_km_recorridos
    FROM Moto m
    JOIN color co     ON m.id_color = co.id_color
    JOIN modelo mo    ON m.id_modelo = mo.id_modelo
    JOIN situacion s  ON m.id_situacion = s.id_situacion;
END;
$$;

-- SELECT * FROM obtener_detalles_motos();
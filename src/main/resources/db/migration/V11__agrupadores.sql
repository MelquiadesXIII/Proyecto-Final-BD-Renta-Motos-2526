-- Función que devuelve el nombre de todos los colores
CREATE OR REPLACE FUNCTION obtener_colores()
RETURNS TABLE (nombre_color VARCHAR)
LANGUAGE SQL
AS $$
    SELECT nombre_color FROM Color;
$$;

-- SELECT * FROM obtener_colores();


-- Funcion que devuelve los detalles de las motos
CREATE OR REPLACE FUNCTION obtener_detalles_motos()
RETURNS TABLE (
    matricula_moto  VARCHAR,
    nombre_modelo   VARCHAR,
    nombre_situacion VARCHAR,
    nombre_color    VARCHAR,
    cant_km_recorridos INTEGER
)
LANGUAGE SQL
AS $$
    SELECT m.matricula_moto,
           model.nombre_modelo,
           sit.nombre_situacion,
           col.nombre_color,
           m.cant_km_recorridos
    FROM Moto m
    JOIN color col     ON m.id_color = col.id_color
    JOIN modelo model  ON m.id_modelo = model.id_modelo
    JOIN situacion sit ON m.id_situacion = sit.id_situacion;
$$;

-- SELECT * FROM obtener_detalles_motos();
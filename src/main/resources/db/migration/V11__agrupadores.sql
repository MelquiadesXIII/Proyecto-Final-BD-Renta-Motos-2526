
DROP FUNCTION obtener_colores();
-- Función que devuelve el nombre de todos los colores
CREATE OR REPLACE FUNCTION obtener_colores()
RETURNS TABLE (
id_color INTEGER,
nombre_color VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM Color;
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

-- Función para obtener todas las marcas
CREATE OR REPLACE FUNCTION obtener_marcas()
RETURNS TABLE(id_marca INT, nombre_marca VARCHAR)
LANGUAGE sql
AS $$
    SELECT id_marca, nombre_marca FROM marca ORDER BY nombre_marca;
$$;

-- Función para obtener modelos por marca
CREATE OR REPLACE FUNCTION obtener_modelos_por_marca(p_id_marca INT)
RETURNS TABLE(id_modelo INT, id_marca INT, nombre_modelo VARCHAR)
LANGUAGE sql
AS $$
    SELECT id_modelo, id_marca, nombre_modelo
    FROM modelo
    WHERE id_marca = p_id_marca
    ORDER BY nombre_modelo;
$$;

-- Función para obtener un modelo por su ID
CREATE OR REPLACE FUNCTION obtener_modelo_por_id(p_id_modelo INT)
RETURNS TABLE(id_modelo INT, id_marca INT, nombre_modelo VARCHAR)
LANGUAGE sql
AS $$
    SELECT id_modelo, id_marca, nombre_modelo
    FROM modelo
    WHERE id_modelo = p_id_modelo;
$$;

-- Función para obtener una marca por su ID
CREATE OR REPLACE FUNCTION obtener_marca_por_id(p_id_marca INT)
RETURNS TABLE(id_marca INT, nombre_marca VARCHAR)
LANGUAGE sql
AS $$
    SELECT id_marca, nombre_marca
    FROM marca
    WHERE id_marca = p_id_marca;
$$;

-- Función para obtener id_color a partir del nombre
CREATE OR REPLACE FUNCTION obtener_id_color_por_nombre(p_nombre VARCHAR)
RETURNS INT
LANGUAGE sql
AS $$
    SELECT id_color FROM color WHERE nombre_color = p_nombre;
$$;

-- Función para obtener el color a partir del id
CREATE OR REPLACE FUNCTION obtener_nombre_color_por_id(p_id INT)
RETURNS VARCHAR
LANGUAGE sql
AS $$
    SELECT nombre_color FROM color WHERE id_color = p_id;
$$;

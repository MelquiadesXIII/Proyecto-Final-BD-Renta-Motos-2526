CREATE OR REPLACE FUNCTION existe_marca(p_nombre VARCHAR)
RETURNS BOOLEAN
LANGUAGE sql
AS $$
    SELECT EXISTS (SELECT 1 FROM marca WHERE LOWER(nombre_marca) = LOWER(p_nombre));
$$;

CREATE OR REPLACE FUNCTION existe_modelo(p_id_marca INTEGER, p_nombre VARCHAR)
RETURNS BOOLEAN
LANGUAGE sql
AS $$
    SELECT EXISTS (
        SELECT 1 FROM modelo
        WHERE id_marca = p_id_marca AND LOWER(nombre_modelo) = LOWER(p_nombre)
    );
$$;

CREATE OR REPLACE FUNCTION insertar_marca(p_nombre VARCHAR)
RETURNS INTEGER
LANGUAGE sql
AS $$
    INSERT INTO marca (nombre_marca) VALUES (p_nombre) RETURNING id_marca;
$$;

CREATE OR REPLACE FUNCTION insertar_modelo(p_id_marca INTEGER, p_nombre VARCHAR)
RETURNS INTEGER
LANGUAGE sql
AS $$
    INSERT INTO modelo (id_marca, nombre_modelo) VALUES (p_id_marca, p_nombre) RETURNING id_modelo;
$$;
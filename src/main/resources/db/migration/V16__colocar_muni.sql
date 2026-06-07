CREATE OR REPLACE FUNCTION listar_municipios()
RETURNS TABLE(id_municipio INT, nombre_municipio VARCHAR(100))
LANGUAGE sql
AS $$
    SELECT id_municipio, nombre_municipio FROM municipio ORDER BY nombre_municipio;
$$;
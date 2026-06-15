-- Primero elimina la versión anterior para que no haya conflicto de tipos
DROP FUNCTION IF EXISTS listar_motos_completas();

CREATE OR REPLACE FUNCTION listar_motos_completas()
RETURNS TABLE (
    id_moto            INT,
    matricula_moto     VARCHAR,
    id_marca           INT,
    nombre_marca       VARCHAR,
    id_modelo          INT,
    nombre_modelo      VARCHAR,
    id_color           INT,
    nombre_color       VARCHAR,
    id_situacion       INT,
    nombre_situacion   VARCHAR,
    cant_km_recorridos NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT m.id_moto,
           m.matricula_moto,
           ma.id_marca,
           ma.nombre_marca,
           mo.id_modelo,
           mo.nombre_modelo,
           c.id_color,
           c.nombre_color,
           s.id_situacion,
           s.nombre_situacion,
           m.cant_km_recorridos
    FROM moto m
    JOIN modelo mo     ON m.id_modelo = mo.id_modelo
    JOIN marca ma      ON mo.id_marca = ma.id_marca
    JOIN color c       ON m.id_color = c.id_color
    JOIN situacion s   ON m.id_situacion = s.id_situacion;
END;
$$;
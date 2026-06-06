-- La funcion mas dura del planeta ... Esta preciosa ...
CREATE OR REPLACE FUNCTION buscar_clientes_por_texto(p_texto TEXT)
RETURNS SETOF cliente
LANGUAGE sql
AS $$
    SELECT * FROM cliente
    WHERE
        CAST(id_cliente AS TEXT) LIKE '%' || p_texto || '%'
        OR ci_cliente LIKE '%' || p_texto || '%'
        OR nombre_cliente ILIKE '%' || p_texto || '%'
        OR primer_apellido ILIKE '%' || p_texto || '%'
        OR segundo_apellido ILIKE '%' || p_texto || '%'
    ORDER BY nombre_cliente, primer_apellido
    LIMIT 30;
$$;


CREATE OR REPLACE FUNCTION motos_disponibles_entre(fecha_inicio DATE, fecha_fin DATE)
RETURNS SETOF moto
LANGUAGE sql
AS $$
    SELECT m.*
    FROM moto m
    WHERE m.id_moto NOT IN (
        SELECT c.id_moto
        FROM contrato c
        WHERE c.fecha_entrega IS NULL -- aún no ha sido devuelta
           OR (c.fecha_inicio, c.fecha_fin) OVERLAPS (fecha_inicio, fecha_fin)
    )
    ORDER BY m.matricula_moto;
$$;

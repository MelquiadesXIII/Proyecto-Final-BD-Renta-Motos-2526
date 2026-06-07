CREATE OR REPLACE FUNCTION actualizar_usuario(
    p_id INTEGER,
    p_nombre VARCHAR,
    p_password VARCHAR,
    p_gmail VARCHAR,
    p_es_admin BOOLEAN
) RETURNS VOID
LANGUAGE sql
AS $$
    UPDATE usuario
    SET nombre_usuario = p_nombre,
        password = p_password,
        gmail = p_gmail,
        es_admin = p_es_admin
    WHERE id_usuario = p_id;
$$;

CREATE OR REPLACE FUNCTION existe_moto_con_modelo(p_id_modelo INTEGER)
RETURNS BOOLEAN
LANGUAGE sql AS $$
    SELECT EXISTS (SELECT 1 FROM moto WHERE id_modelo = p_id_modelo);
$$;

CREATE OR REPLACE FUNCTION existen_modelos_con_marca(p_id_marca INTEGER)
RETURNS BOOLEAN
LANGUAGE sql AS $$
    SELECT EXISTS (SELECT 1 FROM modelo WHERE id_marca = p_id_marca);
$$;


CREATE OR REPLACE FUNCTION existen_motos_con_marca(p_id_marca INTEGER)
RETURNS BOOLEAN
LANGUAGE sql AS $$
    SELECT EXISTS (
        SELECT 1 FROM moto m
        JOIN modelo mo ON m.id_modelo = mo.id_modelo
        WHERE mo.id_marca = p_id_marca
    );
$$;
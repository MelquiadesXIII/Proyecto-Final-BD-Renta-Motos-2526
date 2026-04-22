CREATE OR REPLACE PROCEDURE eliminar_cliente_y_contratos(
    p_ci_cliente CHAR(11)
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM cliente WHERE ci_cliente = p_ci_cliente;
    IF FOUND THEN
        RAISE NOTICE 'Cliente con CI % y todos sus contratos eliminados.', p_ci_cliente;
    ELSE
        RAISE NOTICE 'No se encontró cliente con CI %.', p_ci_cliente;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE eliminar_contrato(
    p_fecha_inicio DATE,
    p_id_moto      INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM contrato
    WHERE fecha_inicio = p_fecha_inicio
      AND id_moto = p_id_moto;

    IF FOUND THEN
        RAISE NOTICE 'Contrato de moto ID % en fecha % eliminado.', p_id_moto, p_fecha_inicio;
    ELSE
        RAISE NOTICE 'No existía contrato para moto ID % en fecha %, no se eliminó nada.', p_id_moto, p_fecha_inicio;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE eliminar_contratos_cliente_fecha(
    p_fecha_inicio DATE,
    p_id_cliente   INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM contrato
    WHERE fecha_inicio = p_fecha_inicio
      AND id_cliente = p_id_cliente;

    IF FOUND THEN
        RAISE NOTICE 'Los contratos del cliente ID % con fecha % fueron eliminados.', p_id_cliente, p_fecha_inicio;
    ELSE
        RAISE NOTICE 'El cliente ID % no tenía contratos con fecha %, no se eliminó nada.', p_id_cliente, p_fecha_inicio;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE eliminar_moto(
    p_id_moto INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_existe BOOLEAN;
BEGIN
    
    SELECT EXISTS (SELECT 1 FROM moto WHERE id_moto = p_id_moto) INTO v_existe;
    
    IF v_existe THEN
        DELETE FROM contrato WHERE id_moto = p_id_moto;
        DELETE FROM moto WHERE id_moto = p_id_moto;
        RAISE NOTICE 'Moto ID % eliminada (y sus contratos asociados).', p_id_moto;
    ELSE
        RAISE NOTICE 'Moto ID % no encontrada.', p_id_moto;
    END IF;
END;
$$;

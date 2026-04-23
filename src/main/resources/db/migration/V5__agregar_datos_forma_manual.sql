/*
CREATE OR REPLACE PROCEDURE insertar_usuario_si_no_existe(
    p_nombre_usuario VARCHAR(50),
    p_password       VARCHAR(255),
    p_gmail          VARCHAR(100)
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO usuario (nombre_usuario, password, gmail)
    VALUES (p_nombre_usuario, p_password, p_gmail)
    ON CONFLICT (nombre_usuario) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'El Usuario "%" fue insertado correctamente.', p_nombre_usuario;
    ELSE
        RAISE NOTICE 'El usuario "%" ya existía en la base de datos, no se insertó.', p_nombre_usuario;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE insertar_cliente_si_no_existe(
    p_ci           CHAR(11),
    p_nombre       VARCHAR(100),
    p_primer_ape   VARCHAR(100),
    p_segundo_ape  VARCHAR(100),
    p_edad         INT,
    p_id_sexo      INT,
    p_num_contacto VARCHAR(20),
    p_id_municipio INT,
    p_id_usuario   INT          -- nuevo parámetro
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, segundo_apellido,
                         edad, id_sexo, numero_contacto, id_municipio, id_usuario)
    VALUES (p_ci, p_nombre, p_primer_ape, p_segundo_ape,
            p_edad, p_id_sexo, p_num_contacto, p_id_municipio, p_id_usuario)
    ON CONFLICT (ci_cliente) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'Cliente % (CI %) insertado correctamente.', p_nombre, p_ci;
    ELSE
        RAISE NOTICE 'El cliente con CI % ya existía, no se insertó.', p_ci;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE insertar_moto_si_no_existe(
    p_matricula      VARCHAR(10),
    p_id_modelo      INT,
    p_id_color       INT,
    p_id_situacion   INT,
    p_km_inicial     NUMERIC(10,2) DEFAULT 0
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO moto (matricula_moto, id_modelo, id_color, id_situacion, cant_km_recorridos)
    VALUES (p_matricula, p_id_modelo, p_id_color, p_id_situacion, p_km_inicial)
    ON CONFLICT (matricula_moto) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'Moto matrícula % insertada correctamente.', p_matricula;
    ELSE
        RAISE NOTICE 'La moto con matrícula % ya existía, no se insertó.', p_matricula;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE insertar_contrato_si_no_existe(
    p_fecha_inicio     DATE,
    p_id_moto          INT,
    p_id_cliente       INT,
    p_id_forma_pago    INT,
    p_fecha_fin        DATE,
    p_tarifa_normal    NUMERIC(10,2),
    p_dias_prorroga    INT DEFAULT 0,
    p_seguro_adicional BOOLEAN DEFAULT FALSE,
    p_tarifa_prorroga  NUMERIC(10,2) DEFAULT 0,
    p_fecha_entrega    DATE DEFAULT NULL,
    p_km_salida        NUMERIC(10,2) DEFAULT 0,
    p_km_llegada       NUMERIC(10,2) DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO contrato (
        fecha_inicio, id_moto, id_cliente, id_forma_pago,
        fecha_fin, dias_prorroga, seguro_adicional,
        tarifa_normal, tarifa_prorroga, fecha_entrega,
        cant_km_salida, cant_km_llegada
    )
    VALUES (
        p_fecha_inicio, p_id_moto, p_id_cliente, p_id_forma_pago,
        p_fecha_fin, p_dias_prorroga, p_seguro_adicional,
        p_tarifa_normal, p_tarifa_prorroga, p_fecha_entrega,
        p_km_salida, p_km_llegada
    )
    ON CONFLICT (fecha_inicio, id_moto) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'Contrato para moto ID % en fecha % insertado correctamente.', p_id_moto, p_fecha_inicio;
    ELSE
        RAISE NOTICE 'El contrato para moto ID % en fecha % ya existía, no se insertó.', p_id_moto, p_fecha_inicio;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE insertar_marca_si_no_existe(
    p_nombre_marca VARCHAR(100)
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO marca (nombre_marca)
    VALUES (p_nombre_marca)
    ON CONFLICT (nombre_marca) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'Marca "%" insertada correctamente.', p_nombre_marca;
    ELSE
        RAISE NOTICE 'La marca "%" ya existía, no se insertó.', p_nombre_marca;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE insertar_modelo_si_no_existe(
    p_nombre_marca  VARCHAR(100),
    p_nombre_modelo VARCHAR(100)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_marca INT;
BEGIN
    SELECT id_marca INTO v_id_marca
    FROM marca
    WHERE nombre_marca = p_nombre_marca;

    IF v_id_marca IS NULL THEN
        RAISE EXCEPTION 'La marca "%" no existe. Primero insértela.', p_nombre_marca;
    END IF;

    INSERT INTO modelo (id_marca, nombre_modelo)
    VALUES (v_id_marca, p_nombre_modelo)
    ON CONFLICT (id_marca, nombre_modelo) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'Modelo "%" de marca "%" insertado correctamente.', p_nombre_modelo, p_nombre_marca;
    ELSE
        RAISE NOTICE 'El modelo "%" de marca "%" ya existía, no se insertó.', p_nombre_modelo, p_nombre_marca;
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE insertar_color_si_no_existe(
    p_nombre_color VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_nombre_normalizado VARCHAR(50);
BEGIN
    -- Normalizar: primera letra mayúscula, resto minúsculas
    v_nombre_normalizado := INITCAP(p_nombre_color);

    INSERT INTO color (nombre_color)
    VALUES (v_nombre_normalizado)
    ON CONFLICT (nombre_color) DO NOTHING;

    IF FOUND THEN
        RAISE NOTICE 'Color "%" insertado correctamente.', v_nombre_normalizado;
    ELSE
        RAISE NOTICE 'El color "%" (normalizado como "%") ya existía, no se insertó.', p_nombre_color, v_nombre_normalizado;
    END IF;
END;
$$;
*/
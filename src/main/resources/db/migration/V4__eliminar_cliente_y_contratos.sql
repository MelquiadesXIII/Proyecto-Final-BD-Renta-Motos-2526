
-- Explicacion de como funciona el procedimiento de eliminar la de la tabla
-- Se le manda el CI como parametro y se busca de la lista de contratos
-- todos los contratos que tuvo el Cliente y se elimina de la BD,
-- despues se elimina el CLiente de la BD

CREATE OR REPLACE PROCEDURE eliminar_cliente_y_contratos(ci CHARACTER(11))
LANGUAGE plpgsql
AS $$
BEGIN
   
	DELETE FROM Contrato 
		WHERE ci_cliente = ci;
		
    DELETE FROM Cliente 
		WHERE ci_cliente = ci;
		
END;
$$;

--------- Insertar Datos Automaticamente ----------

CREATE OR REPLACE PROCEDURE inicializar_datos()
LANGUAGE plpgsql
AS $$
DECLARE
    filas_insertadas INT;
BEGIN

-------Municipios-----
    INSERT INTO municipio (nombre_municipio)
    VALUES
        ('Playa'), ('Plaza de la Revolución'), ('Centro Habana'), ('La Habana Vieja'),
        ('Regla'), ('La Habana del Este'), ('Guanabacoa'), ('San Miguel del Padrón'),
        ('Diez de Octubre'), ('Cerro'), ('Marianao'), ('La Lisa'),
        ('Boyeros'), ('Arroyo Naranjo'), ('Cotorro')
    ON CONFLICT (nombre_municipio) DO NOTHING;

-------Colores-----
    INSERT INTO color (nombre_color)
    VALUES
        ('rojo'), ('azul'), ('negro'), ('blanco'), ('gris'), ('verde'), ('amarillo')
    ON CONFLICT (nombre_color) DO NOTHING;

-------Marcas-----
    INSERT INTO marca (nombre_marca)
    VALUES ('Yadea'), ('NIU'), ('Kuba'), ('Zongshen')
    ON CONFLICT (nombre_marca) DO NOTHING;

-------Modelos-----
    INSERT INTO modelo (id_marca, nombre_modelo)
    SELECT m.id_marca, v.nombre
    FROM (VALUES
        ('Yadea', 'T9'), ('Yadea', 'C1S'),
        ('NIU', 'NQi Sport'), ('Kuba', 'Forza'), ('Zongshen', 'ZS125')
    ) AS v(nombre_marca, nombre)
    JOIN marca m ON m.nombre_marca = v.nombre_marca
    ON CONFLICT (id_marca, nombre_modelo) DO NOTHING;

/*
-------Clientes-----
    INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, sexo, numero_contacto, id_municipio)
    VALUES
        ('99010112345', 'Juan', 'Pérez', 'González', 28, 'masculino', '55512345', 1),
        ('88020254321', 'María', 'Rodríguez', 'López', 34, 'femenino', '55567890', 4),
        ('77030398765', 'Carlos', 'Martínez', NULL, 22, 'masculino', '55511223', 10),
        ('96040411223', 'Ana', 'Hernández', 'Díaz', 45, 'femenino', '55544556', 7)
    ON CONFLICT (ci_cliente) DO NOTHING;

-------Motos-----
    INSERT INTO moto (matricula_moto, id_modelo, situacion, id_color, cant_km_recorridos)
    SELECT v.matricula, m.id_modelo, v.situacion::tipo_situacion, c.id_color, v.km
    FROM (VALUES
        ('B1234', 'Yadea', 'T9', 'rojo', 'disponible', 12500.5),
        ('C5678', 'NIU', 'NQi Sport', 'azul', 'disponible', 8900.0),
        ('A9012', 'Yadea', 'C1S', 'negro', 'disponible', 150.0),
        ('D3456', 'Zongshen', 'ZS125', 'blanco', 'disponible', 22000.75),
        ('E7890', 'Kuba', 'Forza', 'gris', 'taller', 5000.0)
    ) AS v(matricula, marca_nombre, modelo_nombre, color_nombre, situacion, km)
    JOIN marca ma ON ma.nombre_marca = v.marca_nombre
    JOIN modelo m ON m.id_marca = ma.id_marca AND m.nombre_modelo = v.modelo_nombre
    JOIN color c ON c.nombre_color = v.color_nombre
    ON CONFLICT (matricula_moto) DO NOTHING;

-------Contratos-----
    WITH ins AS (
        INSERT INTO contrato (
            fecha_inicio, matricula_moto, ci_cliente, forma_pago,
            fecha_fin, dias_prorroga, seguro_adicional,
            tarifa_normal, tarifa_prorroga, fecha_entrega,
            cant_km_salida, cant_km_llegada
        )
        VALUES (
            '2026-04-10', 'B1234', '99010112345', 'efectivo',
            '2026-04-15', 2, TRUE,
            25.00, 30.00, NULL,
            12500.5, 0
        )
        ON CONFLICT (fecha_inicio, matricula_moto) DO NOTHING
        RETURNING 1 AS inserted
    )
    SELECT COUNT(*) INTO filas_insertadas FROM ins;
    
    IF filas_insertadas > 0 THEN
        RAISE NOTICE 'Contrato 1 insertado (Juan - B1234)';
    END IF;

    WITH ins AS (
        INSERT INTO contrato (
            fecha_inicio, matricula_moto, ci_cliente, forma_pago,
            fecha_fin, dias_prorroga, seguro_adicional,
            tarifa_normal, tarifa_prorroga, fecha_entrega,
            cant_km_salida, cant_km_llegada
        )
        VALUES (
            '2026-04-12', 'C5678', '88020254321', 'credito',
            '2026-04-14', 0, FALSE,
            30.00, 0, NULL,
            8900.0, 0
        )
        ON CONFLICT (fecha_inicio, matricula_moto) DO NOTHING
        RETURNING 1 AS inserted
    )
    SELECT COUNT(*) INTO filas_insertadas FROM ins;
    
    IF filas_insertadas > 0 THEN
        RAISE NOTICE 'Contrato 2 insertado (María - C5678)';
    END IF;

    WITH ins AS (
        INSERT INTO contrato (
            fecha_inicio, matricula_moto, ci_cliente, forma_pago,
            fecha_fin, dias_prorroga, seguro_adicional,
            tarifa_normal, tarifa_prorroga, fecha_entrega,
            cant_km_salida, cant_km_llegada
        )
        VALUES (
            '2026-04-01', 'A9012', '77030398765', 'cheque',
            '2026-04-05', 1, FALSE,
            20.00, 20.00, '2026-04-06',
            150.0, 350.0
        )
        ON CONFLICT (fecha_inicio, matricula_moto) DO NOTHING
        RETURNING 1 AS inserted
    )
    SELECT COUNT(*) INTO filas_insertadas FROM ins;
    
    IF filas_insertadas > 0 THEN
        UPDATE moto SET situacion = 'disponible', cant_km_recorridos = 350.0
        WHERE matricula_moto = 'A9012';
        RAISE NOTICE 'Contrato 3 insertado y moto A9012 actualizada a disponible';
    END IF;

    WITH ins AS (
        INSERT INTO contrato (
            fecha_inicio, matricula_moto, ci_cliente, forma_pago,
            fecha_fin, dias_prorroga, seguro_adicional,
            tarifa_normal, tarifa_prorroga, fecha_entrega,
            cant_km_salida, cant_km_llegada
        )
        VALUES (
            '2026-03-25', 'D3456', '96040411223', 'efectivo',
            '2026-03-30', 3, TRUE,
            28.00, 35.00, '2026-04-02',
            22000.75, 22500.0
        )
        ON CONFLICT (fecha_inicio, matricula_moto) DO NOTHING
        RETURNING 1 AS inserted
    )
    SELECT COUNT(*) INTO filas_insertadas FROM ins;
    
    IF filas_insertadas > 0 THEN
        UPDATE moto SET situacion = 'disponible', cant_km_recorridos = 22500.0
        WHERE matricula_moto = 'D3456';
        RAISE NOTICE 'Contrato 4 insertado y moto D3456 actualizada a disponible';
    END IF;

    RAISE NOTICE 'Procedimiento de inicialización completado. Los datos existentes no fueron modificados.';
END;
$$;



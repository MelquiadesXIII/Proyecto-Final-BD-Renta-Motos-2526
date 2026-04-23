/*
-- AGREGAR PRIMEROS DATOS DE FORMA AUTOMATICA --

INSERT INTO municipio (nombre_municipio) VALUES
    ('Playa'), ('Plaza de la Revolución'), ('Centro Habana'), ('La Habana Vieja'),
    ('Regla'), ('La Habana del Este'), ('Guanabacoa'), ('San Miguel del Padrón'),
    ('Diez de Octubre'), ('Cerro'), ('Marianao'), ('La Lisa'),
    ('Boyeros'), ('Arroyo Naranjo'), ('Cotorro')
ON CONFLICT (nombre_municipio) DO NOTHING;

INSERT INTO color (nombre_color) VALUES
    ('Rojo'), ('Azul'), ('Negro'), ('Blanco'), ('Gris'), ('Verde'), ('Amarillo')
ON CONFLICT (nombre_color) DO NOTHING;

INSERT INTO marca (nombre_marca) VALUES
    ('Yadea'), ('NIU'), ('Kuba'), ('Zongshen')
ON CONFLICT (nombre_marca) DO NOTHING;

INSERT INTO sexo (nombre_sexo) VALUES
    ('Masculino'), ('Femenino')
ON CONFLICT (nombre_sexo) DO NOTHING;

INSERT INTO situacion (nombre_situacion) VALUES
    ('Disponible'), ('Alquilada'), ('Taller')
ON CONFLICT (nombre_situacion) DO NOTHING;

INSERT INTO forma_pago (nombre_forma_pago) VALUES
    ('Efectivo'), ('Tarjeta')
ON CONFLICT (nombre_forma_pago) DO NOTHING;

INSERT INTO modelo (id_marca, nombre_modelo) VALUES
    (1, 'T9'), (1, 'C1S'), (2, 'NQi Sport'), (3, 'Forza'), (4, 'ZS125')
ON CONFLICT (id_marca, nombre_modelo) DO NOTHING;


CREATE OR REPLACE PROCEDURE inicializar_datos()
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_municipio_playa INT;
    v_id_municipio_habana_vieja INT;
    v_id_municipio_cerro INT;
    v_id_municipio_guanabacoa INT;
    v_id_color_rojo INT;
    v_id_color_azul INT;
    v_id_color_negro INT;
    v_id_color_blanco INT;
    v_id_color_gris INT;
    v_id_situacion_disponible INT;
    v_id_situacion_taller INT;
    v_id_forma_efectivo INT;
    v_id_forma_credito INT;
    v_id_forma_cheque INT;
    v_id_sexo_m INT;
    v_id_sexo_f INT;
    v_id_marca_yadea INT;
    v_id_marca_niu INT;
    v_id_marca_kuba INT;
    v_id_marca_zongshen INT;
    v_id_modelo_t9 INT;
    v_id_modelo_c1s INT;
    v_id_modelo_nqi INT;
    v_id_modelo_forza INT;
    v_id_modelo_zs125 INT;
    -- IDs de usuarios
    v_id_usuario_juan INT;
    v_id_usuario_maria INT;
    v_id_usuario_carlos INT;
    v_id_usuario_ana INT;
    v_id_cliente_juan INT;
    v_id_cliente_maria INT;
    v_id_cliente_carlos INT;
    v_id_cliente_ana INT;
    v_id_moto_b1234 INT;
    v_id_moto_c5678 INT;
    v_id_moto_a9012 INT;
    v_id_moto_d3456 INT;
    v_id_moto_e7890 INT;
BEGIN

    -- Insertar nomencladores básicos (si no existen)
    INSERT INTO municipio (nombre_municipio) VALUES
        ('Playa'), ('Plaza de la Revolución'), ('Centro Habana'), ('La Habana Vieja'),
        ('Regla'), ('La Habana del Este'), ('Guanabacoa'), ('San Miguel del Padrón'),
        ('Diez de Octubre'), ('Cerro'), ('Marianao'), ('La Lisa'),
        ('Boyeros'), ('Arroyo Naranjo'), ('Cotorro')
    ON CONFLICT (nombre_municipio) DO NOTHING;

    INSERT INTO color (nombre_color) VALUES
        ('rojo'), ('azul'), ('negro'), ('blanco'), ('gris'), ('verde'), ('amarillo')
    ON CONFLICT (nombre_color) DO NOTHING;

    INSERT INTO marca (nombre_marca) VALUES
        ('Yadea'), ('NIU'), ('Kuba'), ('Zongshen')
    ON CONFLICT (nombre_marca) DO NOTHING;

    INSERT INTO sexo (nombre_sexo) VALUES
        ('masculino'), ('femenino')
    ON CONFLICT (nombre_sexo) DO NOTHING;

    INSERT INTO situacion (nombre_situacion) VALUES
        ('disponible'), ('alquilada'), ('taller')
    ON CONFLICT (nombre_situacion) DO NOTHING;

    INSERT INTO forma_pago (nombre_forma_pago) VALUES
        ('efectivo'), ('cheque'), ('credito')
    ON CONFLICT (nombre_forma_pago) DO NOTHING;

    -- Crear usuarios de prueba
    INSERT INTO usuario (nombre_usuario, password, gmail) VALUES
        ('juanp', 'pass123', 'juan.perez@gmail.com'),
        ('mariar', 'pass456', 'maria.rodriguez@gmail.com'),
        ('carlosm', 'pass789', 'carlos.martinez@gmail.com'),
        ('anah', 'pass000', 'ana.hernandez@gmail.com')
    ON CONFLICT (nombre_usuario) DO NOTHING;

    SELECT id_usuario INTO v_id_usuario_juan FROM usuario WHERE nombre_usuario = 'juanp';
    SELECT id_usuario INTO v_id_usuario_maria FROM usuario WHERE nombre_usuario = 'mariar';
    SELECT id_usuario INTO v_id_usuario_carlos FROM usuario WHERE nombre_usuario = 'carlosm';
    SELECT id_usuario INTO v_id_usuario_ana FROM usuario WHERE nombre_usuario = 'anah';

    -- Obtener IDs necesarios
    SELECT id_municipio INTO v_id_municipio_playa FROM municipio WHERE nombre_municipio = 'Playa';
    SELECT id_municipio INTO v_id_municipio_habana_vieja FROM municipio WHERE nombre_municipio = 'La Habana Vieja';
    SELECT id_municipio INTO v_id_municipio_cerro FROM municipio WHERE nombre_municipio = 'Cerro';
    SELECT id_municipio INTO v_id_municipio_guanabacoa FROM municipio WHERE nombre_municipio = 'Guanabacoa';

    SELECT id_color INTO v_id_color_rojo FROM color WHERE nombre_color = 'rojo';
    SELECT id_color INTO v_id_color_azul FROM color WHERE nombre_color = 'azul';
    SELECT id_color INTO v_id_color_negro FROM color WHERE nombre_color = 'negro';
    SELECT id_color INTO v_id_color_blanco FROM color WHERE nombre_color = 'blanco';
    SELECT id_color INTO v_id_color_gris FROM color WHERE nombre_color = 'gris';

    SELECT id_situacion INTO v_id_situacion_disponible FROM situacion WHERE nombre_situacion = 'disponible';
    SELECT id_situacion INTO v_id_situacion_taller FROM situacion WHERE nombre_situacion = 'taller';

    SELECT id_forma_pago INTO v_id_forma_efectivo FROM forma_pago WHERE nombre_forma_pago = 'efectivo';
    SELECT id_forma_pago INTO v_id_forma_credito FROM forma_pago WHERE nombre_forma_pago = 'credito';
    SELECT id_forma_pago INTO v_id_forma_cheque FROM forma_pago WHERE nombre_forma_pago = 'cheque';

    SELECT id_sexo INTO v_id_sexo_m FROM sexo WHERE nombre_sexo = 'masculino';
    SELECT id_sexo INTO v_id_sexo_f FROM sexo WHERE nombre_sexo = 'femenino';

    SELECT id_marca INTO v_id_marca_yadea FROM marca WHERE nombre_marca = 'Yadea';
    SELECT id_marca INTO v_id_marca_niu FROM marca WHERE nombre_marca = 'NIU';
    SELECT id_marca INTO v_id_marca_kuba FROM marca WHERE nombre_marca = 'Kuba';
    SELECT id_marca INTO v_id_marca_zongshen FROM marca WHERE nombre_marca = 'Zongshen';

    -- Insertar modelos (si no existen)
    INSERT INTO modelo (id_marca, nombre_modelo) VALUES
        (v_id_marca_yadea, 'T9'),
        (v_id_marca_yadea, 'C1S'),
        (v_id_marca_niu, 'NQi Sport'),
        (v_id_marca_kuba, 'Forza'),
        (v_id_marca_zongshen, 'ZS125')
    ON CONFLICT (id_marca, nombre_modelo) DO NOTHING;

    SELECT id_modelo INTO v_id_modelo_t9 FROM modelo WHERE id_marca = v_id_marca_yadea AND nombre_modelo = 'T9';
    SELECT id_modelo INTO v_id_modelo_c1s FROM modelo WHERE id_marca = v_id_marca_yadea AND nombre_modelo = 'C1S';
    SELECT id_modelo INTO v_id_modelo_nqi FROM modelo WHERE id_marca = v_id_marca_niu AND nombre_modelo = 'NQi Sport';
    SELECT id_modelo INTO v_id_modelo_forza FROM modelo WHERE id_marca = v_id_marca_kuba AND nombre_modelo = 'Forza';
    SELECT id_modelo INTO v_id_modelo_zs125 FROM modelo WHERE id_marca = v_id_marca_zongshen AND nombre_modelo = 'ZS125';

    -- Insertar clientes (si no existen) con su usuario correspondiente
    INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, id_sexo, numero_contacto, id_municipio, id_usuario)
    VALUES
        ('99010112345', 'Juan', 'Pérez', 'González', 28, v_id_sexo_m, '55512345', v_id_municipio_playa, v_id_usuario_juan),
        ('88020254321', 'María', 'Rodríguez', 'López', 34, v_id_sexo_f, '55567890', v_id_municipio_habana_vieja, v_id_usuario_maria),
        ('77030398765', 'Carlos', 'Martínez', NULL, 22, v_id_sexo_m, '55511223', v_id_municipio_cerro, v_id_usuario_carlos),
        ('96040411223', 'Ana', 'Hernández', 'Díaz', 45, v_id_sexo_f, '55544556', v_id_municipio_guanabacoa, v_id_usuario_ana)
    ON CONFLICT (ci_cliente) DO NOTHING;

    SELECT id_cliente INTO v_id_cliente_juan FROM cliente WHERE ci_cliente = '99010112345';
    SELECT id_cliente INTO v_id_cliente_maria FROM cliente WHERE ci_cliente = '88020254321';
    SELECT id_cliente INTO v_id_cliente_carlos FROM cliente WHERE ci_cliente = '77030398765';
    SELECT id_cliente INTO v_id_cliente_ana FROM cliente WHERE ci_cliente = '96040411223';

    -- Insertar motos (si no existen)
    INSERT INTO moto (matricula_moto, id_modelo, id_color, id_situacion, cant_km_recorridos)
    VALUES
        ('B1234', v_id_modelo_t9, v_id_color_rojo, v_id_situacion_disponible, 12500.5),
        ('C5678', v_id_modelo_nqi, v_id_color_azul, v_id_situacion_disponible, 8900.0),
        ('A9012', v_id_modelo_c1s, v_id_color_negro, v_id_situacion_disponible, 150.0),
        ('D3456', v_id_modelo_zs125, v_id_color_blanco, v_id_situacion_disponible, 22000.75),
        ('E7890', v_id_modelo_forza, v_id_color_gris, v_id_situacion_taller, 5000.0)
    ON CONFLICT (matricula_moto) DO NOTHING;

    SELECT id_moto INTO v_id_moto_b1234 FROM moto WHERE matricula_moto = 'B1234';
    SELECT id_moto INTO v_id_moto_c5678 FROM moto WHERE matricula_moto = 'C5678';
    SELECT id_moto INTO v_id_moto_a9012 FROM moto WHERE matricula_moto = 'A9012';
    SELECT id_moto INTO v_id_moto_d3456 FROM moto WHERE matricula_moto = 'D3456';
    SELECT id_moto INTO v_id_moto_e7890 FROM moto WHERE matricula_moto = 'E7890';

    -- Insertar contratos (respetando disponibilidad)
    -- Contrato 1: Juan con B1234 (futuro)
    INSERT INTO contrato (
        fecha_inicio, id_moto, id_cliente, id_forma_pago,
        fecha_fin, dias_prorroga, seguro_adicional,
        tarifa_normal, tarifa_prorroga, fecha_entrega,
        cant_km_salida, cant_km_llegada
    )
    SELECT '2026-04-10', v_id_moto_b1234, v_id_cliente_juan, v_id_forma_efectivo,
           '2026-04-15', 2, TRUE,
           25.00, 30.00, NULL,
           12500.5, NULL
    WHERE EXISTS (SELECT 1 FROM moto WHERE id_moto = v_id_moto_b1234 AND id_situacion = v_id_situacion_disponible)
    ON CONFLICT (fecha_inicio, id_moto) DO NOTHING;

    -- Contrato 2: María con C5678 (futuro)
    INSERT INTO contrato (
        fecha_inicio, id_moto, id_cliente, id_forma_pago,
        fecha_fin, dias_prorroga, seguro_adicional,
        tarifa_normal, tarifa_prorroga, fecha_entrega,
        cant_km_salida, cant_km_llegada
    )
    SELECT '2026-04-12', v_id_moto_c5678, v_id_cliente_maria, v_id_forma_credito,
           '2026-04-14', 0, FALSE,
           30.00, 0, NULL,
           8900.0, NULL
    WHERE EXISTS (SELECT 1 FROM moto WHERE id_moto = v_id_moto_c5678 AND id_situacion = v_id_situacion_disponible)
    ON CONFLICT (fecha_inicio, id_moto) DO NOTHING;

    -- Contrato 3: Carlos con A9012 (ya finalizado)
    INSERT INTO contrato (
        fecha_inicio, id_moto, id_cliente, id_forma_pago,
        fecha_fin, dias_prorroga, seguro_adicional,
        tarifa_normal, tarifa_prorroga, fecha_entrega,
        cant_km_salida, cant_km_llegada
    )
    SELECT '2026-04-01', v_id_moto_a9012, v_id_cliente_carlos, v_id_forma_cheque,
           '2026-04-05', 1, FALSE,
           20.00, 20.00, '2026-04-06',
           150.0, 350.0
    WHERE EXISTS (SELECT 1 FROM moto WHERE id_moto = v_id_moto_a9012 AND id_situacion = v_id_situacion_disponible)
    ON CONFLICT (fecha_inicio, id_moto) DO NOTHING;

    -- Actualizar km y estado de moto A9012 manualmente (el trigger no se ejecuta en inserts con fecha_entrega ya puesta)
    UPDATE moto SET cant_km_recorridos = 350.0, id_situacion = v_id_situacion_disponible
    WHERE id_moto = v_id_moto_a9012;

    -- Contrato 4: Ana con D3456 (finalizado)
    INSERT INTO contrato (
        fecha_inicio, id_moto, id_cliente, id_forma_pago,
        fecha_fin, dias_prorroga, seguro_adicional,
        tarifa_normal, tarifa_prorroga, fecha_entrega,
        cant_km_salida, cant_km_llegada
    )
    SELECT '2026-03-25', v_id_moto_d3456, v_id_cliente_ana, v_id_forma_efectivo,
           '2026-03-30', 3, TRUE,
           28.00, 35.00, '2026-04-02',
           22000.75, 22500.0
    WHERE EXISTS (SELECT 1 FROM moto WHERE id_moto = v_id_moto_d3456 AND id_situacion = v_id_situacion_disponible)
    ON CONFLICT (fecha_inicio, id_moto) DO NOTHING;

    UPDATE moto SET cant_km_recorridos = 22500.0, id_situacion = v_id_situacion_disponible
    WHERE id_moto = v_id_moto_d3456;

    RAISE NOTICE 'Procedimiento de inicialización completado.';
END;
$$;
*/

-- =============================================
-- Tabla para generación secuencial de IDs
-- Cada fila almacena el último ID generado
-- para el tipo de entidad indicado.
-- =============================================

CREATE TABLE id_secuencia (
    nombre_clase VARCHAR(50) PRIMARY KEY,
    ultimo_id    INT NOT NULL DEFAULT 0
);

-- Sembrar con el máximo ID actual de cada entidad
-- para que el generador no colisione con datos existentes.
INSERT INTO id_secuencia (nombre_clase, ultimo_id) VALUES
    ('Cliente',   (SELECT COALESCE(MAX(id_cliente),   0) FROM cliente)),
    ('Moto',      (SELECT COALESCE(MAX(id_moto),      0) FROM moto)),
    ('Contrato',  (SELECT COALESCE(MAX(id_contrato),  0) FROM contrato)),
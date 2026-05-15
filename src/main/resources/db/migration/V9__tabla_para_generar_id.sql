-- =============================================
-- Tabla para generación secuencial de IDs
-- Cada fila almacena el último ID generado
-- para el tipo de entidad indicado.
-- O sea, que lo que hace es lo siguiente:
-- coge y busca cual es el ultimo ID que tienes registrado
-- pongamos que el ultimo id registrado es 9
-- pos el se encargaara de poner el nuevo id como 10
-- esto lo que hace es que no tengas que cargar, la BD 
-- hasta que crees el objeto, lo que hace que sea mas rapido.
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
    ('Contrato',  (SELECT COALESCE(MAX(id_contrato),  0) FROM contrato));


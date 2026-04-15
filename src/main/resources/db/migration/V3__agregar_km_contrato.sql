-- Agregar columnas de kilometraje de salida y llegada al contrato
ALTER TABLE contrato
    ADD COLUMN cant_km_salida  NUMERIC(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN cant_km_llegada NUMERIC(10,2) NOT NULL DEFAULT 0;

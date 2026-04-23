CREATE OR REPLACE PROCEDURE registrar_devolucion(
    p_id_contrato      INT,
    p_fecha_entrega    DATE,
    p_cant_km_llegada  NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_km_salida    NUMERIC(10,2);
    v_fecha_inicio DATE;
BEGIN
    -- Obtener el kilometraje de salida y la fecha de inicio del contrato
    SELECT cant_km_salida, fecha_inicio
    INTO v_km_salida, v_fecha_inicio
    FROM contrato
    WHERE id_contrato = p_id_contrato;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'No existe el contrato con ID %', p_id_contrato;
    END IF;

    -- Validar que la fecha de entrega no sea anterior a la fecha de inicio del contrato
    IF p_fecha_entrega < v_fecha_inicio THEN
        RAISE EXCEPTION 'La fecha de entrega (%) no puede ser anterior a la fecha de inicio del contrato (%)',
                        p_fecha_entrega, v_fecha_inicio;
    END IF;

    -- Validar que la fecha de entrega no sea futura
    IF p_fecha_entrega > CURRENT_DATE THEN
        RAISE EXCEPTION 'La fecha de entrega (%) no puede ser posterior a la fecha actual', p_fecha_entrega;
    END IF;

    -- Validar que el kilometraje de llegada no sea menor que el de salida
    IF p_cant_km_llegada < v_km_salida THEN
        RAISE EXCEPTION 'Kilometraje de llegada (%) no puede ser menor que el de salida (%)',
                        p_cant_km_llegada, v_km_salida;
    END IF;

    -- Actualizar el contrato con los datos de devolución
    UPDATE contrato
    SET fecha_entrega    = p_fecha_entrega,
        cant_km_llegada  = p_cant_km_llegada
    WHERE id_contrato = p_id_contrato;

    RAISE NOTICE 'Devolución registrada correctamente. Kilómetros del contrato: % -> %',
                 v_km_salida, p_cant_km_llegada;
END;
$$;

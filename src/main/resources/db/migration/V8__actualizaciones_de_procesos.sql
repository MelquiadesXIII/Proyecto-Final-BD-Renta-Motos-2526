

--=======================================
-- Actualizacion de la forma de pago
--=======================================

INSERT INTO forma_pago (nombre_forma_pago) VALUES
    ('Cheque')
ON CONFLICT (nombre_forma_pago) DO NOTHING;

--========================================
-- Actualizacoin de la funcion de km
--=======================================

CREATE OR REPLACE FUNCTION actualizar_km_al_entregar()
RETURNS TRIGGER AS $$
DECLARE
    km_recorridos_en_contrato NUMERIC(10,2);
    id_situacion_disponible   INT;
BEGIN
    IF NEW.fecha_entrega IS NOT NULL AND OLD.fecha_entrega IS NULL THEN
        
        -- 1. Validaciones existentes
        IF NEW.fecha_entrega < OLD.fecha_inicio THEN
            RAISE EXCEPTION 'La fecha de entrega (%) no puede ser anterior a la fecha de inicio (%)',
                            NEW.fecha_entrega, OLD.fecha_inicio;
        END IF;

        IF NEW.fecha_entrega > CURRENT_DATE THEN
            RAISE EXCEPTION 'La fecha de entrega (%) no puede ser posterior a la fecha actual', NEW.fecha_entrega;
        END IF;

        IF NEW.cant_km_llegada IS NULL THEN
            RAISE EXCEPTION 'Debe especificar el kilometraje de llegada antes de registrar la entrega';
        END IF;

        IF NEW.cant_km_llegada < NEW.cant_km_salida THEN
            RAISE EXCEPTION 'Kilometraje de llegada (%) menor que el de salida (%)',
                            NEW.cant_km_llegada, NEW.cant_km_salida;
        END IF;

        -- 2. Actualizar kilómetros de la moto
        km_recorridos_en_contrato := NEW.cant_km_llegada - NEW.cant_km_salida;
        IF km_recorridos_en_contrato > 0 THEN
            UPDATE moto
            SET cant_km_recorridos = cant_km_recorridos + km_recorridos_en_contrato
            WHERE id_moto = NEW.id_moto;
        END IF;

        -- 3. Poner la moto como disponible
        SELECT id_situacion INTO id_situacion_disponible
        FROM situacion WHERE nombre_situacion = 'disponible';
        
        UPDATE moto
        SET id_situacion = id_situacion_disponible
        WHERE id_moto = NEW.id_moto;

        -- 4. (NUEVO) Calcular días de prórroga
        NEW.dias_prorroga := CASE 
                                WHEN NEW.fecha_entrega > OLD.fecha_fin 
                                THEN NEW.fecha_entrega - OLD.fecha_fin
                                ELSE 0
                             END;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================
--  Recalcular días de prórroga de todos los contratos
--     que ya tienen fecha de entrega, según la diferencia
--     entre fecha_entrega y fecha_fin.
-- =============================================

UPDATE contrato
SET dias_prorroga = CASE
    WHEN fecha_entrega IS NOT NULL
         AND fecha_entrega > fecha_fin
    THEN fecha_entrega - fecha_fin
    ELSE 0
END;

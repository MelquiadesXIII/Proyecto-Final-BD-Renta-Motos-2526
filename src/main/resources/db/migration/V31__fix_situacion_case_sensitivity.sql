
-- Fix 1: actualizar_km_al_entregar usaba 'disponible' (minúscula) pero la tabla
--        situacion tiene 'Disponible' (mayúscula inicial), causando NULL → violación NOT NULL.
--
-- Fix 2: check_moto_disponible comprobaba el campo id_situacion de la moto (estado físico
--        actual) en lugar de verificar solapamiento de fechas con contratos existentes.
--        Eso impedía crear un segundo contrato aunque sus fechas no chocasen, porque la moto
--        ya tenía id_situacion = 'Alquilada' por el contrato activo anterior.

CREATE OR REPLACE FUNCTION actualizar_km_al_entregar()
RETURNS TRIGGER AS $$
DECLARE
    km_recorridos_en_contrato NUMERIC(10,2);
    id_situacion_disponible   INT;
BEGIN
    IF NEW.fecha_entrega IS NOT NULL AND OLD.fecha_entrega IS NULL THEN

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

        km_recorridos_en_contrato := NEW.cant_km_llegada - NEW.cant_km_salida;
        IF km_recorridos_en_contrato > 0 THEN
            UPDATE moto
            SET cant_km_recorridos = cant_km_recorridos + km_recorridos_en_contrato
            WHERE id_moto = NEW.id_moto;
        END IF;

        SELECT id_situacion INTO id_situacion_disponible
        FROM situacion WHERE nombre_situacion = 'Disponible';

        UPDATE moto
        SET id_situacion = id_situacion_disponible
        WHERE id_moto = NEW.id_moto;

        NEW.dias_prorroga := CASE
                                WHEN NEW.fecha_entrega > OLD.fecha_fin
                                THEN NEW.fecha_entrega - OLD.fecha_fin
                                ELSE 0
                             END;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION check_moto_disponible()
RETURNS TRIGGER AS $$
BEGIN
    -- Bloquear si existe algún contrato que solape con el período solicitado
    IF EXISTS (
        SELECT 1 FROM contrato
        WHERE id_moto = NEW.id_moto
          AND (fecha_inicio, fecha_fin) OVERLAPS (NEW.fecha_inicio, NEW.fecha_fin)
    ) THEN
        RAISE EXCEPTION 'La moto con ID % ya tiene un contrato en el período % – %',
                        NEW.id_moto, NEW.fecha_inicio, NEW.fecha_fin;
    END IF;

    -- Bloquear si la moto está en taller (no disponible físicamente)
    IF EXISTS (
        SELECT 1 FROM moto m
        JOIN situacion s ON m.id_situacion = s.id_situacion
        WHERE m.id_moto = NEW.id_moto
          AND s.nombre_situacion = 'Taller'
    ) THEN
        RAISE EXCEPTION 'La moto con ID % está en el taller y no puede ser alquilada', NEW.id_moto;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

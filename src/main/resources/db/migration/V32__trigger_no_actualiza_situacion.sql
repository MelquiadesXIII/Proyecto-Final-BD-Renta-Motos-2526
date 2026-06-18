
-- El trigger actualizar_km_al_entregar hacía un SELECT sobre la tabla situacion
-- por nombre ('disponible') para obtener el id y luego hacer UPDATE moto SET id_situacion.
-- Ese SELECT retornaba NULL por diferencia de capitalización, rompiendo el UPDATE.
--
-- Esa actualización de id_situacion es redundante: ContratoService.finalizarContrato()
-- ya llama a motoDAO.cambiarEstado(idMoto, Situacion.DISPONIBLE) que usa el id directamente.
-- Se elimina del trigger para evitar el conflicto.

CREATE OR REPLACE FUNCTION actualizar_km_al_entregar()
RETURNS TRIGGER AS $$
DECLARE
    km_recorridos_en_contrato NUMERIC(10,2);
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

        NEW.dias_prorroga := CASE
                                WHEN NEW.fecha_entrega > OLD.fecha_fin
                                THEN NEW.fecha_entrega - OLD.fecha_fin
                                ELSE 0
                             END;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- También corregir check_moto_disponible para que use solapamiento de fechas
-- en lugar del estado físico id_situacion (permite múltiples contratos no solapados).
CREATE OR REPLACE FUNCTION check_moto_disponible()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM contrato
        WHERE id_moto = NEW.id_moto
          AND (fecha_inicio, fecha_fin) OVERLAPS (NEW.fecha_inicio, NEW.fecha_fin)
    ) THEN
        RAISE EXCEPTION 'La moto con ID % ya tiene un contrato en el período % – %',
                        NEW.id_moto, NEW.fecha_inicio, NEW.fecha_fin;
    END IF;

    IF EXISTS (
        SELECT 1 FROM moto m
        JOIN situacion s ON m.id_situacion = s.id_situacion
        WHERE m.id_moto = NEW.id_moto
          AND LOWER(s.nombre_situacion) = 'taller'
    ) THEN
        RAISE EXCEPTION 'La moto con ID % está en el taller y no puede ser alquilada', NEW.id_moto;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

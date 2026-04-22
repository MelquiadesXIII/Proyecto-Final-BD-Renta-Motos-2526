-- FUNCIONES PARA EL TRIGGER de MOTO_ALQUILADA

REATE OR REPLACE FUNCTION set_moto_alquilada()
RETURNS TRIGGER AS $$
DECLARE
    v_id_situacion_alquilada INT;
BEGIN
    SELECT id_situacion INTO v_id_situacion_alquilada
    FROM situacion WHERE nombre_situacion = 'alquilada';

    UPDATE moto
    SET id_situacion = v_id_situacion_alquilada
    WHERE id_moto = NEW.id_moto;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_moto_alquilada
AFTER INSERT ON contrato
FOR EACH ROW EXECUTE FUNCTION set_moto_alquilada();



CREATE OR REPLACE FUNCTION check_moto_disponible()
RETURNS TRIGGER AS $$
DECLARE
    v_nombre_situacion VARCHAR(20);
BEGIN
    SELECT s.nombre_situacion INTO v_nombre_situacion
    FROM moto m
    JOIN situacion s ON m.id_situacion = s.id_situacion
    WHERE m.id_moto = NEW.id_moto;

    IF v_nombre_situacion <> 'disponible' THEN
        RAISE EXCEPTION 'La moto con ID % no está disponible (estado: %)', NEW.id_moto, v_nombre_situacion;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_disponible
BEFORE INSERT ON contrato
FOR EACH ROW EXECUTE FUNCTION check_moto_disponible();


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
        FROM situacion WHERE nombre_situacion = 'disponible';
        
        UPDATE moto
        SET id_situacion = id_situacion_disponible
        WHERE id_moto = NEW.id_moto;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_actualizar_km_entrega
BEFORE UPDATE ON contrato
FOR EACH ROW
WHEN (OLD.fecha_entrega IS NULL AND NEW.fecha_entrega IS NOT NULL)
EXECUTE FUNCTION actualizar_km_al_entregar();








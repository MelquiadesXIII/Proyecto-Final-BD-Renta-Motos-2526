-- =============================================================================
-- V34: Reglas de negocio del alquiler reforzadas a nivel de base de datos.
--
-- Contexto: migraciones previas dejaron el sistema sin garantías a nivel SQL:
--   * V21 eliminó set_moto_alquilada() CON CASCADE, borrando el trigger que
--     ponía la moto en 'Alquilada' al crear un contrato.
--   * V19 eliminó el trigger trg_check_disponible (nunca se volvió a crear),
--     dejando de validar el estado de la moto al alquilar.
-- Resultado: las reglas solo vivían (parcialmente) en la capa de servicios.
--
-- Esta migración restablece y centraliza las reglas en la propia base de datos
-- (de forma robusta ante mayúsculas/minúsculas en la tabla situacion):
--   (3) Al alquilar una moto, su estado pasa a 'Alquilada'.
--   (4) No se permite alquilar una moto cuyo estado no sea 'Disponible'.
--   (+) Al borrar un contrato activo, la moto vuelve a 'Disponible' (mantiene
--       la coherencia del estado tras eliminar contratos, incluso en la
--       eliminación en cascada al borrar un cliente).
--   (+) obtener_motos_libres() solo ofrece motos realmente 'Disponible'.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- (4) BEFORE INSERT: impedir alquilar una moto que no esté 'Disponible'
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION check_moto_disponible()
RETURNS TRIGGER AS $$
DECLARE
    v_nombre_situacion VARCHAR(20);
BEGIN
    -- Solo se valida al crear un contrato activo (sin fecha de entrega).
    -- Las cargas históricas/finalizadas (fecha_entrega ya informada) se omiten.
    IF NEW.fecha_entrega IS NULL THEN
        SELECT s.nombre_situacion INTO v_nombre_situacion
        FROM moto m
        JOIN situacion s ON m.id_situacion = s.id_situacion
        WHERE m.id_moto = NEW.id_moto;

        IF v_nombre_situacion IS NULL THEN
            RAISE EXCEPTION 'La moto con ID % no existe', NEW.id_moto;
        END IF;

        IF LOWER(v_nombre_situacion) <> 'disponible' THEN
            RAISE EXCEPTION 'La moto con ID % no está disponible (estado actual: %)',
                            NEW.id_moto, v_nombre_situacion;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_check_disponible ON contrato;
CREATE TRIGGER trg_check_disponible
BEFORE INSERT ON contrato
FOR EACH ROW EXECUTE FUNCTION check_moto_disponible();

-- -----------------------------------------------------------------------------
-- (3) AFTER INSERT: al alquilar, la moto pasa a 'Alquilada'
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_moto_alquilada()
RETURNS TRIGGER AS $$
DECLARE
    v_id_alquilada INT;
BEGIN
    -- Un contrato insertado ya finalizado (fecha_entrega informada) no debe
    -- dejar la moto como 'Alquilada'.
    IF NEW.fecha_entrega IS NULL THEN
        SELECT id_situacion INTO v_id_alquilada
        FROM situacion WHERE LOWER(nombre_situacion) = 'alquilada';

        UPDATE moto
        SET id_situacion = v_id_alquilada
        WHERE id_moto = NEW.id_moto;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_moto_alquilada ON contrato;
CREATE TRIGGER trg_moto_alquilada
AFTER INSERT ON contrato
FOR EACH ROW EXECUTE FUNCTION set_moto_alquilada();

-- -----------------------------------------------------------------------------
-- (+) AFTER DELETE: al borrar un contrato activo, liberar la moto
--     ('Disponible'). Cubre tanto el borrado directo de un contrato como la
--     eliminación en cascada de contratos al borrar un cliente.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION liberar_moto_al_borrar_contrato()
RETURNS TRIGGER AS $$
DECLARE
    v_id_disponible INT;
BEGIN
    -- Solo libera la moto si el contrato borrado estaba activo. Un contrato
    -- finalizado ya había devuelto la moto a 'Disponible'.
    IF OLD.fecha_entrega IS NULL THEN
        SELECT id_situacion INTO v_id_disponible
        FROM situacion WHERE LOWER(nombre_situacion) = 'disponible';

        UPDATE moto
        SET id_situacion = v_id_disponible
        WHERE id_moto = OLD.id_moto;
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_liberar_moto_al_borrar_contrato ON contrato;
CREATE TRIGGER trg_liberar_moto_al_borrar_contrato
AFTER DELETE ON contrato
FOR EACH ROW EXECUTE FUNCTION liberar_moto_al_borrar_contrato();

-- -----------------------------------------------------------------------------
-- (+) obtener_motos_libres(): coherente con la regla (4), solo motos cuyo
--     estado físico sea 'Disponible'.
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS obtener_motos_libres(DATE, DATE);

CREATE OR REPLACE FUNCTION obtener_motos_libres(p_fecha_inicio DATE, p_fecha_fin DATE)
RETURNS TABLE (
    id_moto INT,
    matricula_moto VARCHAR,
    nombre_marca VARCHAR,
    nombre_modelo VARCHAR,
    nombre_color VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT m.id_moto,
           m.matricula_moto,
           ma.nombre_marca,
           mo.nombre_modelo,
           c.nombre_color
    FROM moto m
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN marca ma ON mo.id_marca = ma.id_marca
    JOIN color c ON m.id_color = c.id_color
    WHERE m.id_situacion IN (
        SELECT id_situacion FROM situacion WHERE LOWER(nombre_situacion) = 'disponible'
    )
    AND NOT EXISTS (
        SELECT 1 FROM contrato co
        WHERE co.id_moto = m.id_moto
          AND co.fecha_entrega IS NULL
          AND co.fecha_inicio <= p_fecha_fin
          AND co.fecha_fin   >= p_fecha_inicio
    );
END;
$$;

-- Bug fix: las funciones de disponibilidad y el trigger de validación
-- excluían motos aunque sus contratos asociados estuvieran finalizados
-- (fecha_entrega IS NOT NULL). Solo contratos activos (sin fecha_entrega)
-- deben bloquear una moto para un nuevo contrato.
--
-- Tres componentes corregidos:
--   1. obtener_motos_libres()      - usada por el combo de motos en el formulario de contrato
--   2. motos_disponibles_entre()   - usada por la misma ruta alternativa en MotoDAO
--   3. check_moto_disponible()     - trigger BEFORE INSERT en contrato

-- ----------------------------------------------------------------
-- 1. obtener_motos_libres: agregar filtro de contrato activo
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION obtener_motos_libres(p_fecha_inicio DATE, p_fecha_fin DATE)
RETURNS TABLE (
    id_moto       INT,
    matricula_moto VARCHAR,
    nombre_marca  VARCHAR,
    nombre_modelo VARCHAR,
    nombre_color  VARCHAR
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
    JOIN marca  ma ON mo.id_marca  = ma.id_marca
    JOIN color  c  ON m.id_color   = c.id_color
    WHERE NOT EXISTS (
        SELECT 1 FROM contrato co
        WHERE co.id_moto        = m.id_moto
          AND co.fecha_entrega IS NULL           -- solo contratos activos
          AND co.fecha_inicio   <= p_fecha_fin
          AND co.fecha_fin      >= p_fecha_inicio
    )
    AND m.id_situacion NOT IN (
        SELECT id_situacion FROM situacion WHERE nombre_situacion = 'Taller'
    );
END;
$$;

-- ----------------------------------------------------------------
-- 2. motos_disponibles_entre: corregir lógica OR → AND con solapamiento
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION motos_disponibles_entre(fecha_inicio DATE, fecha_fin DATE)
RETURNS SETOF moto
LANGUAGE sql
AS $$
    SELECT m.*
    FROM moto m
    WHERE m.id_moto NOT IN (
        SELECT c.id_moto
        FROM contrato c
        WHERE c.fecha_entrega IS NULL           -- solo contratos activos
          AND (c.fecha_inicio, c.fecha_fin) OVERLAPS (fecha_inicio, fecha_fin)
    )
    ORDER BY m.matricula_moto;
$$;

-- ----------------------------------------------------------------
-- 3. check_moto_disponible: excluir contratos finalizados del solapamiento
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION check_moto_disponible()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM contrato
        WHERE id_moto       = NEW.id_moto
          AND fecha_entrega IS NULL             -- solo contratos activos
          AND (fecha_inicio, fecha_fin) OVERLAPS (NEW.fecha_inicio, NEW.fecha_fin)
    ) THEN
        RAISE EXCEPTION 'La moto con ID % ya tiene un contrato activo en el período % – %',
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

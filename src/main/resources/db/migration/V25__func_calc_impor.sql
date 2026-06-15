CREATE OR REPLACE FUNCTION calcular_monto_contrato(p_id_contrato INTEGER)
RETURNS NUMERIC(10,2)
LANGUAGE plpgsql
AS $$
DECLARE
    monto      NUMERIC(10,2) := 0;
    seguro     BOOLEAN;
    dias_base  INTEGER;
    dias_ret   INTEGER := 0;
    tarifa_n   NUMERIC(10,2);
    tarifa_p   NUMERIC(10,2);
    fecha_fin  DATE;
    fecha_ent  DATE;
BEGIN
    -- Obtener los datos del contrato
    SELECT c.fecha_fin - c.fecha_inicio + 1,
           c.tarifa_normal,
           c.tarifa_prorroga,
           c.seguro_adicional,
           c.fecha_fin,
           c.fecha_entrega
    INTO dias_base, tarifa_n, tarifa_p, seguro, fecha_fin, fecha_ent
    FROM contrato c
    WHERE c.id_contrato = p_id_contrato;

    -- Solo calcular si el contrato existe
    IF dias_base IS NOT NULL THEN
        -- Duplicar tarifas si tiene seguro
        IF seguro THEN
            tarifa_n := tarifa_n * 2;
            tarifa_p := tarifa_p * 2;
        END IF;

        -- Días normales
        monto := dias_base * tarifa_n;

        -- Días de retraso (solo si ya se entregó y fue tarde)
        IF fecha_ent IS NOT NULL AND fecha_ent > fecha_fin THEN
            dias_ret := fecha_ent - fecha_fin;
            monto := monto + (dias_ret * tarifa_p);
        END IF;
    END IF;

    RETURN monto;
END;
$$;
-- IRE COLOCANDO LOS REPORTES AQUI --

-- ESTO de abajo es para que la zona horaria sea la de cuba.
SET timezone = 'America/Havana';

--Listado de los clientes:
-- Fecha (fecha en que se muestra el reporte)
-- Y, para cada municipio:
-- Municipio
-- Y, para cada cliente de ese municipio:
-- Nombre del cliente
-- Número de identificación
-- Cantidad de veces que el cliente ha alquilado motos (hasta la fecha)
-- Valor total de los alquileres del cliente hasta la fecha





-- =====================================================
-- FUNCION AUXILIAR: Cantidad de contratos terminados
-- =====================================================
CREATE OR REPLACE FUNCTION cantidad_de_contratos_terminados(p_id_cliente INTEGER)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
    total INTEGER;
BEGIN
    SELECT COUNT(*) INTO total
    FROM Contrato 
    WHERE id_cliente = p_id_cliente 
      AND fecha_entrega <= CURRENT_DATE;
    RETURN total;
END;
$$;

-- =====================================================
-- FUNCION PRINCIPAL (escalar): contar contratos terminados de un cliente
-- =====================================================
CREATE OR REPLACE FUNCTION contar_contratos_terminados(p_id_cliente INTEGER)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN cantidad_de_contratos_terminados(p_id_cliente);
END;
$$;

-- =====================================================
-- FUNCION AUXILIAR: calcular monto de un contrato individual
-- =====================================================
CREATE OR REPLACE FUNCTION calcular_monto_contrato(p_id_contrato INTEGER)
RETURNS NUMERIC(10,2)
LANGUAGE plpgsql
AS $$
DECLARE
    monto NUMERIC(10,2);
BEGIN
    SELECT
        ((fecha_fin - fecha_inicio + 1) * tarifa_normal) +
        (CASE
            WHEN fecha_entrega > fecha_fin THEN (fecha_entrega - fecha_fin) * tarifa_prorroga
            ELSE 0
         END)
    INTO monto
    FROM contrato
    WHERE id_contrato = p_id_contrato
      AND fecha_entrega IS NOT NULL
      AND fecha_entrega <= CURRENT_DATE;

    RETURN COALESCE(monto, 0);
END;
$$;

-- =====================================================
-- FUNCION PRINCIPAL: dinero total gastado por un cliente
-- =====================================================
CREATE OR REPLACE FUNCTION dinero_gastado(p_id_cliente INTEGER)
RETURNS NUMERIC(10,2)
LANGUAGE plpgsql
AS $$
DECLARE
    total NUMERIC(10,2);
BEGIN
    SELECT SUM(calcular_monto_contrato(id_contrato))
    INTO total
    FROM contrato
    WHERE id_cliente = p_id_cliente
      AND fecha_entrega IS NOT NULL
      AND fecha_entrega <= CURRENT_DATE;

    RETURN COALESCE(total, 0);
END;
$$;

-- =====================================================
-- FUNCION: listado completo de clientes
-- =====================================================
CREATE OR REPLACE FUNCTION listado_clientes()
RETURNS TABLE (
    "Fecha de hoy" DATE,
    "Municipio" VARCHAR(100),
    "Nombre" VARCHAR(100),
    "CI" CHAR(11),
    "Cantidad de Contratos contratados" INTEGER,
    "Total de Dinero gastado" NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        CURRENT_DATE,
        m.nombre_municipio,
        c.nombre_cliente,
        c.ci_cliente,
        contar_contratos_terminados(c.id_cliente),
        dinero_gastado(c.id_cliente)
    FROM cliente c
    JOIN municipio m ON m.id_municipio = c.id_municipio
    ORDER BY m.nombre_municipio, c.nombre_cliente;
END;
$$;

-- Como usar:
-- SELECT * FROM listado_clientes();


--Listado de las motos:
-- Fecha (fecha en que se muestra el reporte)
-- Y, para cada moto:
-- Matrícula de la moto
-- Marca
-- Modelo
-- Color
-- Cantidad de kilómetros recorridos



CREATE OR REPLACE FUNCTION reporte_motos()
RETURNS TABLE(
    fecha_reporte DATE,
    matricula_moto VARCHAR,
    marca VARCHAR,
    modelo VARCHAR,
    color VARCHAR,
    cant_km_recorridos NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        CURRENT_DATE,
        m.matricula_moto,
        mar.nombre_marca,
        mode.nombre_modelo,
        c.nombre_color,
        m.cant_km_recorridos
    FROM moto m
    JOIN modelo mode ON m.id_modelo = mode.id_modelo
    JOIN marca mar ON mode.id_marca = mar.id_marca
    JOIN color c ON m.id_color = c.id_color;
END;
$$;

-- COMO USAR:
-- SELECT * FROM reporte_motos();





--Listado de los contratos:
-- Nombre del cliente
-- Matrícula
-- Marca
-- Modelo
-- Forma de pago
-- Fecha de inicio del contrato
-- Fecha de fin del contrato
-- Prórroga (cantidad de días)
-- Seguro adicional (sí o no)
-- Importe total

CREATE OR REPLACE FUNCTION listado_contratos()
RETURNS TABLE (
    "Nombre del cliente"       TEXT,
    "Matrícula"                VARCHAR(10),
    "Marca"                    VARCHAR(100),
    "Modelo"                   VARCHAR(100),
    "Forma de pago"            VARCHAR(20),
    "Fecha inicio"             DATE,
    "Fecha fin"                DATE,
    "Prórroga (días)"          INTEGER,
    "Seguro adicional"         TEXT,
    "Importe total"            NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        cl.nombre_cliente || ' ' || cl.primer_apellido || ' ' || COALESCE(cl.segundo_apellido, ''),
        m.matricula_moto,
        ma.nombre_marca,
        mo.nombre_modelo,
        fp.nombre_forma_pago,
        c.fecha_inicio,
        c.fecha_fin,
        c.dias_prorroga,
        CASE WHEN c.seguro_adicional THEN 'Sí' ELSE 'No' END,
        calcular_monto_contrato(c.id_contrato)
    FROM contrato c
    JOIN cliente cl      ON c.id_cliente = cl.id_cliente
    JOIN moto m           ON c.id_moto = m.id_moto
    JOIN modelo mo        ON m.id_modelo = mo.id_modelo
    JOIN marca ma         ON mo.id_marca = ma.id_marca
    JOIN forma_pago fp    ON c.id_forma_pago = fp.id_forma_pago
    ORDER BY c.id_contrato;  
END;
$$;

-- COMO USAR:
-- SELECT * FROM listado_contratos();





--Listado de la situación de las motos:
-- Fecha (fecha en que se muestra el reporte)
-- Y, para cada moto:
-- Matrícula- Marca
-- Situación
-- En caso de estar alquilada, fecha de fin del contrato




CREATE OR REPLACE FUNCTION reporte_motos()
RETURNS TABLE(
    fecha_reporte      DATE,
    matricula_marca    TEXT,
    situacion          VARCHAR(20),
    fecha_fin_contrato DATE
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        CURRENT_DATE,
        m.matricula_moto || ' - ' || ma.nombre_marca,
        CASE
            WHEN c.id_contrato IS NOT NULL THEN 'Alquilada'
            ELSE s.nombre_situacion
        END,
        c.fecha_fin   -- NULL si no hay contrato activo
    FROM moto m
    JOIN modelo mo     ON m.id_modelo = mo.id_modelo
    JOIN marca ma      ON mo.id_marca = ma.id_marca
    JOIN situacion s   ON m.id_situacion = s.id_situacion
    LEFT JOIN contrato c
        ON c.id_moto = m.id_moto
        AND c.fecha_inicio <= CURRENT_DATE
        AND c.fecha_entrega IS NULL
    ORDER BY m.matricula_moto;
END;
$$;

-- COMO USAR:
-- SELECT * FROM reporte_motos();



-- Listado de clientes incumplidores del contrato:
-- Fecha actual (fecha en que se muestra el reporte)
-- Nombres y apellidos del cliente
-- Fecha de fin del contrato
-- Fecha de entrega de la moto




CREATE OR REPLACE FUNCTION lista_incumplidores()
RETURNS TABLE (
    "Fecha actual"              DATE,
    "Nombres y apellidos"       TEXT,
    "Fecha fin del contrato"    DATE,
    "Fecha de entrega"          DATE
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        CURRENT_DATE,
        cl.nombre_cliente || ' ' || cl.primer_apellido || ' ' || COALESCE(cl.segundo_apellido, ''),
        c.fecha_fin,
        c.fecha_entrega
    FROM contrato c
    JOIN cliente cl ON c.id_cliente = cl.id_cliente
    WHERE c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega > c.fecha_fin
    ORDER BY c.id_contrato;
END;
$$;

-- COMO USAR:
-- SELECT * FROM lista_incumplidores();



--Resumen de contratos por marcas y modelos:
-- Fecha (fecha en que se muestra el reporte)
-- Y, para cada marca:
-- Marca
-- Y, para cada modelo:
-- Modelo
-- Cantidad de motos (de esa marca y modelo)
-- Cantidad de días totales alquilados
-- Ingresos por concepto de tarjetas de crédito
-- Ingresos por concepto de cheques
-- Ingresos por concepto de efectivo
-- Totales de ingresos por marca
-- Total general de ingresos

-- =============================================
-- FUNCIONES AUXILIARES 
-- =============================================

-- Cantidad de motos distintas de esa marca y modelo
CREATE OR REPLACE FUNCTION cant_motos_mod_y_marca(p_id_marca INT, p_id_modelo INT)
RETURNS BIGINT
LANGUAGE sql
AS $$
    SELECT COUNT(DISTINCT m.id_moto)
    FROM contrato c
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

-- DIas totales alquilados para esa marca y modelo
CREATE OR REPLACE FUNCTION cant_dias_totales_alquilado_marc_mod(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT SUM(
        (c.fecha_fin - c.fecha_inicio + 1) +
        CASE WHEN c.fecha_entrega > c.fecha_fin THEN (c.fecha_entrega - c.fecha_fin) ELSE 0 END
    )
    FROM contrato c
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

-- Ingresos por tarjeta para esa marca y modelo
CREATE OR REPLACE FUNCTION ingresos_tarjeta_mod_marca(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(calcular_monto_contrato(c.id_contrato)), 0)
    FROM contrato c
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN forma_pago fp ON c.id_forma_pago = fp.id_forma_pago
    WHERE mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE
      AND fp.nombre_forma_pago = 'Tarjeta de crédito';
$$;

-- Ingresos por cheque para esa marca y modelo
CREATE OR REPLACE FUNCTION ingresos_cheque_mod_marca(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(calcular_monto_contrato(c.id_contrato)), 0)
    FROM contrato c
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN forma_pago fp ON c.id_forma_pago = fp.id_forma_pago
    WHERE mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE
      AND fp.nombre_forma_pago = 'Cheque';
$$;

-- Ingresos por efectivo para esa marca y modelo
CREATE OR REPLACE FUNCTION ingresos_efectivo_mod_marca(p_id_marca INT, p_id_modelo INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(calcular_monto_contrato(c.id_contrato)), 0)
    FROM contrato c
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN forma_pago fp ON c.id_forma_pago = fp.id_forma_pago
    WHERE mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE
      AND fp.nombre_forma_pago = 'Efectivo';
$$;

-- Total de ingresos de una marca
CREATE OR REPLACE FUNCTION total_ingresos_marca(p_id_marca INT)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(calcular_monto_contrato(c.id_contrato)), 0)
    FROM contrato c
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

-- Total de ingresos 
CREATE OR REPLACE FUNCTION total_ingresos_general()
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(calcular_monto_contrato(c.id_contrato)), 0)
    FROM contrato c
    WHERE c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

--================================================
-- FUNCION PRINCIPAL:
--================================================

CREATE OR REPLACE FUNCTION resumen_contratos_por_marcas_modelos()
RETURNS TABLE (
    "Fecha"                 DATE,
    "Marca"                 TEXT,
    "Modelo"                TEXT,
    "Cantidad de motos"     BIGINT,
    "Días totales"          NUMERIC,
    "Ingresos tarjeta"      NUMERIC(10,2),
    "Ingresos cheque"       NUMERIC(10,2),
    "Ingresos efectivo"     NUMERIC(10,2),
    "Total ingresos marca"  NUMERIC(10,2),
    "Total general ingresos" NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        CURRENT_DATE,
        ma.nombre_marca,
        mo.nombre_modelo,
        cant_motos_mod_y_marca(ma.id_marca, mo.id_modelo),
        cant_dias_totales_alquilado_marc_mod(ma.id_marca, mo.id_modelo),
        ingresos_tarjeta_mod_marca(ma.id_marca, mo.id_modelo),
        ingresos_cheque_mod_marca(ma.id_marca, mo.id_modelo),
        ingresos_efectivo_mod_marca(ma.id_marca, mo.id_modelo),
        total_ingresos_marca(ma.id_marca),
        total_ingresos_general()
    FROM marca ma
    JOIN modelo mo ON mo.id_marca = ma.id_marca
    WHERE EXISTS (
        SELECT 1
        FROM contrato c
        JOIN moto m ON c.id_moto = m.id_moto
        WHERE m.id_modelo = mo.id_modelo
          AND c.fecha_entrega IS NOT NULL
          AND c.fecha_entrega <= CURRENT_DATE
    )
    ORDER BY ma.nombre_marca, mo.nombre_modelo;
END;
$$;




-- =============================================
-- FUNCIONES AUXILIARES 
-- =============================================

-- Dias base alquilados 
CREATE OR REPLACE FUNCTION cant_dias_alquilados_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(
        (c.fecha_fin - c.fecha_inicio + 1)
    ), 0)
    FROM contrato c
    JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE cl.id_municipio = p_id_municipio
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

-- Dias de prorroga
CREATE OR REPLACE FUNCTION cant_dias_prorroga_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(c.dias_prorroga), 0)
    FROM contrato c
    JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE cl.id_municipio = p_id_municipio
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

-- Valor en efectivo
CREATE OR REPLACE FUNCTION valor_efectivo_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(
        CASE WHEN fp.nombre_forma_pago = 'Efectivo'
             THEN calcular_monto_contrato(c.id_contrato)
             ELSE 0 END
    ), 0)
    FROM contrato c
    JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN forma_pago fp ON c.id_forma_pago = fp.id_forma_pago
    WHERE cl.id_municipio = p_id_municipio
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

-- Valor total general
CREATE OR REPLACE FUNCTION valor_total_mun_marca_modelo(
    p_id_municipio INT,
    p_id_marca INT,
    p_id_modelo INT
)
RETURNS NUMERIC(10,2)
LANGUAGE sql
AS $$
    SELECT COALESCE(SUM(calcular_monto_contrato(c.id_contrato)), 0)
    FROM contrato c
    JOIN cliente cl ON c.id_cliente = cl.id_cliente
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    WHERE cl.id_municipio = p_id_municipio
      AND mo.id_modelo = p_id_modelo
      AND mo.id_marca = p_id_marca
      AND c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE;
$$;

-- =============================================
-- FUNCION PRINCIPAL 
-- =============================================
CREATE OR REPLACE FUNCTION resumen_contratos_por_municipios()
RETURNS TABLE (
    "Fecha"                DATE,
    "Municipio"            VARCHAR(100),
    "Marca"                VARCHAR(100),
    "Modelo"               VARCHAR(100),
    "Días alquilados"      NUMERIC,
    "Días de prórroga"     NUMERIC,
    "Valor en efectivo"    NUMERIC(10,2),
    "Valor total general"  NUMERIC(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT
        CURRENT_DATE,
        muni.nombre_municipio,
        ma.nombre_marca,
        mo.nombre_modelo,
        cant_dias_alquilados_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo),
        cant_dias_prorroga_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo),
        valor_efectivo_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo),
        valor_total_mun_marca_modelo(muni.id_municipio, ma.id_marca, mo.id_modelo)
    FROM contrato c
    JOIN cliente cli ON c.id_cliente = cli.id_cliente
    JOIN municipio muni ON cli.id_municipio = muni.id_municipio
    JOIN moto m ON c.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN marca ma ON mo.id_marca = ma.id_marca
    WHERE c.fecha_entrega IS NOT NULL
      AND c.fecha_entrega <= CURRENT_DATE
    ORDER BY muni.nombre_municipio, ma.nombre_marca, mo.nombre_modelo;
END;
$$;

-- Como se usa:
-- SELECT * FROM resumen_contratos_por_municipios();










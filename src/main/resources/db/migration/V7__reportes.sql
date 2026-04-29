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



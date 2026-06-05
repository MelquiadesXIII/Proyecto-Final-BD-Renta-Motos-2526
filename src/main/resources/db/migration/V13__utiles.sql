-- Es para listar los clientes XD
CREATE OR REPLACE FUNCTION listar_clientes_con_usuario()
RETURNS TABLE (
    id_cliente INT,
    id_usuario INT,
    ci_cliente CHAR(11),
    nombre_completo TEXT,
    numero_contacto VARCHAR(15),
    nombre_municipio VARCHAR(100),
    nombre_usuario VARCHAR(50),
    gmail VARCHAR(100),
    cantidad_contratos BIGINT
)
LANGUAGE sql
AS $$
    SELECT
        c.id_cliente,
        c.id_usuario,
        c.ci_cliente,
        c.nombre_cliente || ' ' || c.primer_apellido || ' ' || COALESCE(c.segundo_apellido, ''),
        c.numero_contacto,
        m.nombre_municipio,
        u.nombre_usuario,
        u.gmail,
        COUNT(co.id_contrato)
    FROM cliente c
    JOIN municipio m ON c.id_municipio = m.id_municipio
    JOIN usuario u ON c.id_usuario = u.id_usuario
    LEFT JOIN contrato co ON co.id_cliente = c.id_cliente
    GROUP BY c.id_cliente, c.id_usuario, c.ci_cliente, m.nombre_municipio, u.nombre_usuario, u.gmail
    ORDER BY c.nombre_cliente, c.primer_apellido;
$$;


-- Buscar cliente por ID
CREATE OR REPLACE FUNCTION buscar_cliente_por_id(p_id_cliente INTEGER)
RETURNS SETOF cliente
LANGUAGE sql
AS $$
    SELECT * FROM cliente WHERE id_cliente = p_id_cliente;
$$;

-- Buscar cliente por ID de usuario
CREATE OR REPLACE FUNCTION buscar_cliente_por_id_usuario(p_id_usuario INTEGER)
RETURNS SETOF cliente
LANGUAGE sql
AS $$
    SELECT * FROM cliente WHERE id_usuario = p_id_usuario;
$$;


CREATE OR REPLACE FUNCTION mis_contratos(p_id_cliente INTEGER)
RETURNS TABLE (
    id_contrato INT,
    matricula_moto VARCHAR(10),
    marca VARCHAR(100),
    modelo VARCHAR(100),
    fecha_inicio DATE,
    fecha_fin DATE,
    estado TEXT,
    importe NUMERIC(10,2)
)
LANGUAGE sql
AS $$
    SELECT
        co.id_contrato,
        m.matricula_moto,
        ma.nombre_marca,
        mo.nombre_modelo,
        co.fecha_inicio,
        co.fecha_fin,
        CASE WHEN co.fecha_entrega IS NOT NULL THEN 'Finalizado' ELSE 'Activo' END,
        calcular_monto_contrato(co.id_contrato)
    FROM contrato co
    JOIN moto m ON co.id_moto = m.id_moto
    JOIN modelo mo ON m.id_modelo = mo.id_modelo
    JOIN marca ma ON mo.id_marca = ma.id_marca
    WHERE co.id_cliente = p_id_cliente
    ORDER BY co.fecha_inicio DESC;
$$;

-- Obtengo el nombre del municipio
CREATE OR REPLACE FUNCTION obtener_nombre_municipio(p_id_municipio INTEGER)
RETURNS VARCHAR(100)
LANGUAGE sql
AS $$
    SELECT nombre_municipio FROM municipio WHERE id_municipio = p_id_municipio;
$$;

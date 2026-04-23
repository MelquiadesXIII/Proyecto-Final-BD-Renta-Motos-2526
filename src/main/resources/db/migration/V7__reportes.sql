-- IRE COLOCANDO LOS REPORTES AQUI --
/*


--Listado de los clientes:
- Fecha (fecha en que se muestra el reporte)
- Y, para cada municipio:
- Municipio
- Y, para cada cliente de ese municipio:
- Nombre del cliente
- Número de identificación
- Cantidad de veces que el cliente ha alquilado motos (hasta la fecha)
- Valor total de los alquileres del cliente hasta la fecha







Listado de los contratos:
- Nombre del cliente
- Matrícula
- Marca
- Modelo
- Forma de pago
- Fecha de inicio del contrato
- Fecha de fin del contrato
- Prórroga (cantidad de días)
- Seguro adicional (sí o no)
- Importe total


Listado de la situación de las motos:
- Fecha (fecha en que se muestra el reporte)
- Y, para cada moto:
- Matrícula- Marca
- Situación
- En caso de estar alquilada, fecha de fin del contrato



Listado de clientes incumplidores del contrato:
- Fecha actual (fecha en que se muestra el reporte)
- Nombres y apellidos del cliente
- Fecha de fin del contrato
- Fecha de entrega de la moto
Resumen de contratos por marcas y modelos:
- Fecha (fecha en que se muestra el reporte)
- Y, para cada marca:
- Marca
- Y, para cada modelo:
- Modelo
- Cantidad de motos (de esa marca y modelo)
- Cantidad de días totales alquilados
- Ingresos por concepto de tarjetas de crédito
- Ingresos por concepto de cheques
- Ingresos por concepto de efectivo
- Totales de ingresos por marca
- Total general de ingresos
Resumen de contratos por municipios:
- Fecha (fecha en que se muestra el reporte)
- Y, para cada municipio:
- Municipio
- Y, para cada marca y modelo:
- Cantidad de días alquilados
- Cantidad de días de prórroga
- Valor total en efectivo
- Valor total general
Listado de ingresos del año:
- Fecha (fecha en que se muestra el reporte)
- Ingreso total anual
- Y, para cada mes:
- Nombre del mes
- Ingreso mensual
El sistema debe garantizar lo siguiente:
- Tener integridad relacional en toda la base de datos.
- Gestionar (insertar, modificar y eliminar) cada una de sus entidades.
- Que no se repitan los nombres de los nomencladores.
- Que cuando una moto se alquile, automáticamente su estado pase a ser Alquilado.
- No permitir alquilar una moto cuyo estado no sea Disponible.
- Que cuando se elimine un cliente del sistema se eliminen también todos sus contratos.

*/


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

SELECT * FROM reporte_motos();





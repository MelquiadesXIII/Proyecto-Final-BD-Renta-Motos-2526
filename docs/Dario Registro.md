=========================================== Registro de Trabajo ==================================


================== 18 de abril =======================


Se actualizó el conjunto de procedimientos almacenados con nuevas funcionalidades de eliminación. Respecto a la versión anterior, se han incorporado los siguientes procedimientos:

    eliminar_contrato(fecha_inicio, ci_cliente, matricula): elimina un contrato específico identificado por su fecha de inicio, el CI del cliente y la matrícula de la moto. Emite un mensaje de éxito o de ausencia del registro.

    eliminar_contratos_cliente_fecha(fecha_inicio, ci_cliente): elimina todos los contratos de un cliente que coincidan con una fecha de inicio determinada. Útil para corregir registros múltiples erróneos en una misma fecha.

    eliminar_moto(matricula): elimina una moto de la base de datos, borrando previamente todos los contratos asociados a su matrícula (por la restricción de clave foránea definida en ON DELETE CASCADE o mediante un DELETE explícito en el procedimiento). Informa el resultado de la operación.

Estos procedimientos complementan las capacidades de gestión del sistema, permitiendo ahora eliminar contratos individuales, contratos de un cliente en una fecha concreta, y motos (con su historial de contratos). Todos incluyen mensajes RAISE NOTICE para confirmar la acción o indicar que no se encontraron registros para eliminar.


==================== 17 de abril =====================

Se crearon varios procedimientos almacenados en PostgreSQL para facilitar la gestión de datos en el sistema de alquiler de motos:

    inicializar_datos(): procedimiento que inserta automáticamente valores iniciales en las tablas municipio, color, marca, y sus relaciones, evitando duplicados mediante ON CONFLICT DO NOTHING.

    Inserción individual segura:

        insertar_cliente_si_no_existe: agrega un cliente nuevo verificando que su CI no esté registrado previamente.

        insertar_moto_si_no_existe: registra una moto nueva con matrícula, modelo, color, situación y kilometraje, sin duplicar matrículas.

        insertar_contrato_si_no_existe: crea un contrato de alquiler validando la unicidad por fecha de inicio y matrícula, actualizando opcionalmente el estado de la moto.

        insertar_marca_si_no_existe: añade una nueva marca de moto si no existe ya.

        insertar_color_si_no_existe: incorpora un color nuevo a la tabla color.

    Eliminación en cascada:

        eliminar_cliente_y_contratos(ci): elimina primero todos los contratos asociados a un cliente y luego el registro del cliente, manteniendo la integridad referencial.

Todos los procedimientos incluyen mensajes informativos mediante RAISE NOTICE para confirmar si la operación se realizó o si el dato ya existía. Estos bloques facilitan la interacción desde una aplicación Java, delegando las validaciones previas a la capa de negocio.



# Entidades

## Cliente

- id_cliente (Llave primaria, serial)
- ci_cliente (Única)
- nombre_cliente
- primer_apellido
- segundo_apellido
- edad
- id_sexo (Llave foránea)
- numero_contacto
- id_municipio (Llave foránea)

---

## Sexo

- id_sexo (Llave primaria)
- nombre_sexo

---

## Municipio

- id_municipio (Llave primaria)
- nombre_municipio

---

## Moto

- id_moto (Llave primaria, serial)
- matricula_moto (Única)
- id_modelo (Llave foránea)
- id_situacion (Llave foránea)
- id_color (Llave foránea)
- cant_km_recorridos

---

## Situacion

- id_situacion (Llave primaria)
- nombre_situacion

---

## Color

- id_color (Llave primaria)
- nombre_color

---

## Modelo

- id_modelo (Llave primaria)
- id_marca (Llave foránea)
- nombre_modelo

---

## Marca

- id_marca (Llave primaria)
- nombre_marca

---

## FormaPago

- id_forma_pago (Llave primaria)
- nombre_forma_pago

---

## Contrato

- fecha_inicio (Llave primaria compuesta)
- id_moto (Llave primaria compuesta, Llave foránea)
- id_cliente (Llave foránea)
- id_forma_pago (Llave foránea)
- fecha_fin
- dias_prorroga
- seguro_adicional [true, false]
- tarifa_normal (Valor global de la empresa)
- tarifa_prorroga (Valor global de la empresa)
- fecha_entrega
- cant_km_salida
- cant_km_llegada

---

# Dependencias funcionales

## Dependencias directas

> id_cliente --> ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, id_sexo, numero_contacto, id_municipio

> ci_cliente --> id_cliente

> id_sexo --> nombre_sexo

> id_municipio --> nombre_municipio

> id_moto --> matricula_moto, id_modelo, id_situacion, id_color, cant_km_recorridos

> matricula_moto --> id_moto

> id_situacion --> nombre_situacion

> id_color --> nombre_color

> id_modelo --> id_marca, nombre_modelo

> id_marca --> nombre_marca

> id_forma_pago --> nombre_forma_pago

> (fecha_inicio, id_moto) --> id_cliente, id_forma_pago, fecha_fin, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega, cant_km_salida, cant_km_llegada

## Dependencias transitivas

> id_cliente --> nombre_municipio, nombre_sexo

> id_moto --> nombre_modelo, id_marca, nombre_marca, nombre_color, nombre_situacion

> id_modelo --> nombre_marca

# Cardinalidad

- 1:N

SEXO-CLIENTE (un sexo puede estar asociado a varios clientes; un cliente pertenece a un solo sexo)

MUNICIPIO-CLIENTE (un municipio tiene varios clientes; un cliente pertenece a un solo municipio)

MARCA-MODELO (una marca puede tener muchos modelos; un modelo pertenece a una sola marca)

MODELO-MOTO (un modelo puede estar en muchas motos; una moto tiene un solo modelo)

SITUACION-MOTO (una situación puede aplicarse a muchas motos; una moto tiene una sola situación)

COLOR-MOTO (un color puede estar en muchas motos; una moto tiene un solo color)

CLIENTE-CONTRATO (un cliente puede tener muchos contratos; un contrato pertenece a un solo cliente)

MOTO-CONTRATO (una moto puede tener varios contratos en fechas distintas)

FORMA_PAGO-CONTRATO (una forma de pago puede estar en muchos contratos; un contrato tiene una sola forma de pago)

# 1FN

CONTRATO(<u>fecha_inicio</u>, <u>id_moto</u>, matricula_moto, id_modelo, id_situacion, nombre_situacion, id_color, nombre_color, cant_km_recorridos, id_marca, nombre_modelo, nombre_marca, id_cliente, ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, id_sexo, nombre_sexo, numero_contacto, id_municipio, nombre_municipio, id_forma_pago, nombre_forma_pago, fecha_fin, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega, cant_km_salida, cant_km_llegada)

# 2FN (Eliminar dependencias parciales)

MOTO(<u>id_moto</u>, matricula_moto, id_modelo, id_situacion, id_marca, nombre_modelo, nombre_marca, id_color, nombre_color, cant_km_recorridos)

CLIENTE(<u>id_cliente</u>, ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, id_sexo, nombre_sexo, numero_contacto, id_municipio, nombre_municipio)

FORMA_PAGO(<u>id_forma_pago</u>, nombre_forma_pago)

CONTRATO(<u>fecha_inicio</u>, <u>id_moto</u>, id_cliente, id_forma_pago, fecha_fin, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega, cant_km_salida, cant_km_llegada)

# 3FN (Eliminar dependencias transitivas)

MOTO(<u>id_moto</u>, matricula_moto, id_modelo, id_situacion, id_color, cant_km_recorridos)

SITUACION(<u>id_situacion</u>, nombre_situacion)

COLOR(<u>id_color</u>, nombre_color)

MODELO(<u>id_modelo</u>, id_marca, nombre_modelo)

MARCA(<u>id_marca</u>, nombre_marca)

CLIENTE(<u>id_cliente</u>, ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, id_sexo, numero_contacto, id_municipio)

SEXO(<u>id_sexo</u>, nombre_sexo)

MUNICIPIO(<u>id_municipio</u>, nombre_municipio)

FORMA_PAGO(<u>id_forma_pago</u>, nombre_forma_pago)

CONTRATO(<u>fecha_inicio</u>, <u>id_moto</u>, id_cliente, id_forma_pago, fecha_fin, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega, cant_km_salida, cant_km_llegada)

---

## Notas de modelado

- `ci_cliente` y `matricula_moto` dejan de ser llaves primarias y pasan a ser atributos únicos de negocio.
- `id_cliente` e `id_moto` son identificadores numéricos (serial) y llaves primarias técnicas.
- `sexo`, `forma_pago` y `situacion` se modelan como entidades/nomencladores con ID numérico y relación por llave foránea.

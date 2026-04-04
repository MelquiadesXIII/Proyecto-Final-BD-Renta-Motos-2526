# Entidades

## Cliente

- nombre_cliente
- primer_apellido
- segundo_apellido
- ci_cliente (Llave Primaria)
- edad
- sexo (ENUM: masculino, femenino)
- numero_de_contacto
- id_municipio (Llave foranea)

---

## Municipio

- id_municipio
- nombre_municipio

---

## Moto

- matricula_moto (Llave primaria)
- id_modelo (Llave foranea)
- situacion (ENUM: disponible, alquilada, taller)
- cant_km_recorridos
- id_color (Llave foranea)

---

## Color

- id_color (Llave primaria)
- nombre_color

## Modelo

- id_modelo (Llave primaria)
- id_marca (Llave foranea)
- nombre_modelo

---

## Marca

- id_marca (Llave primaria)
- nombre_marca

---

## Contrato

- fecha_inicio (Llave primaria)
- fecha_fin
- matricula_moto (Llave primaria)
- ci_cliente (Llave foranea)
- forma_pago (ENUM: efectivo, cheque, credito)
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

> ci_cliente --> nombre_cliente, primer_apellido, segundo_apellido, edad, sexo, numero_contacto, id_municipio

> id_municipio --> nombre_municipio

> matricula_moto --> id_modelo, situacion, id_color, cant_km_recorridos

> id_color --> nombre_color

> id_modelo --> id_marca, nombre_modelo

> id_marca --> nombre_marca

> (fecha_inicio, matricula_moto) --> fecha_fin, ci_cliente, forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega

## Dependencias transitivas

> ci_cliente --> nombre_municipio

> matricula_moto --> nombre_modelo, id_marca, nombre_marca, nombre_color

> id_modelo --> nombre_marca

# Cardinalidad

- 1:N

MUNICIPIO-CLIENTE (un municipio tiene varios clientes, un cliente solo pertenece a un municipio)

MARCA-MODELO (una marca puede tener muchos modelos, un modelo solo le pertenece a una marca)

MODELO-MOTO (un modelo lo pueden tener muchas motos, pero una moto solo tiene un modelo)

CLIENTE-CONTRATO (un cliente puede tener muchos contratos y un contrato le pertenece a un solo cliente)

MOTO-CONTRATO (una moto puede tener varios contratos en dias distintos)

COLOR-MOTO (una moto tiene un solo color, pero un color puede estar en muchas motos)

# 1FN

CONTRATO(<u>fecha_inicio</u>, <u>matricula_moto</u>, nombre_cliente, primer_apellido, segundo_apellido, ci_cliente, edad, sexo, numero_contacto, id_municipio, nombre_municipio, id_modelo, situacion, id_color, nombre_color, cant_km_recorridos, id_marca, nombre_modelo, nombre_marca, fecha_fin, forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega)

# 2FN (Eliminar dependencias parciales)

MOTO(<u>matricula_moto</u>, id_modelo, situacion, id_marca, nombre_modelo, nombre_marca, id_color, nombre_color, cant_km_recorridos)

CONTRATO(<u>fecha_inicio</u>, <u>matricula_moto</u>, nombre_cliente, primer_apellido, segundo_apellido, ci_cliente, edad, sexo, numero_contacto, id_municipio, nombre_municipio, fecha_fin, forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega)

# 3FN (Eliminar dependencias transitivas)

MOTO(<u>matricula_moto</u>, id_modelo, situacion, id_color, cant_km_recorridos)

COLOR(<u>id_color</u>, nombre_color)

MODELO(<u>id_modelo</u>, id_marca, nombre_modelo)

MARCA(<u>id_marca</u>, nombre_marca)

CLIENTE(nombre_cliente, primer_apellido, segundo_apellido, <u>ci_cliente</u>, edad, sexo, numero_contacto, id_municipio)

MUNICIPIO(<u>id_municipio</u>, nombre_municipio)

CONTRATO(<u>fecha_inicio</u>, <u>matricula_moto</u>, ci_cliente, fecha_fin, forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega)

---

## Notas sobre ENUMs

Los siguientes atributos se implementan como tipos ENUM en PostgreSQL:
- **sexo**: tipo_sexo ('masculino', 'femenino')
- **forma_pago**: tipo_forma_pago ('efectivo', 'cheque', 'credito')
- **situacion**: tipo_situacion ('disponible', 'alquilada', 'taller')

Estos no son entidades independientes sino valores enumerados directamente en las columnas.

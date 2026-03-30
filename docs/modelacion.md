# Entidades

## Cliente

- nombre_cliente
- primer_apellido
- segundo_apellido
- ci_cliente (Llave Primaria)
- edad
- id_sexo (Llave foranea)
- numero_de_contacto
- id_municipio (Llave foranea)

---

## Sexo

- id_sexo (Llave primaria)
- nombre_sexo [masculino, femenino]

---

## Municipio

- id_municipio
- nombre_municipio

---

## Moto

- matricula_moto (Llave primaria)
- id_modelo (Llave foranea)
- id_situacion (Llave foranea)
- cant_km_recorridos
- id_color (Llave foranea)

---

## Situacion

- id_situacion (Llave primaria)
- nombre_situacion [taller, alquilada, disponible]

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

## FormaPago

- id_forma_pago (Llave primaria)
- nombre_forma_pago [efectivo, cheque, credito]

---

## Contrato

- fecha_inicio (Llave primaria)
- fecha_fin
- matricula_moto (Llave primaria)
- ci_cliente (Llave foranea)
- id_forma_pago (Llave foranea)
- dias_prorroga
- seguro_adicional [true, false]
- tarifa_normal (Valor global de la empresa)
- tarifa_prorroga (Valor global de la empresa)
- fecha_entrega

---

# Dependencias funcionales

## Dependencias directas

> ci_cliente --> nombre_cliente, primer_apellido, segundo_apellido, edad, id_sexo, numero_contacto, id_municipio

> id_municipio --> nombre_municipio

> matricula_moto --> id_modelo, id_situacion, id_color, cant_km_recorridos

> id_color --> nombre_color

> id_situacion --> nombre_situacion

> id_sexo --> nombre_sexo

> id_modelo --> id_marca, nombre_modelo

> id_marca --> nombre_marca

> id_forma_pago --> nombre_forma_pago

> (fecha_inicio, matricula_moto) --> fecha_fin, ci_cliente, id_forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega

## Dependencias transitivas

> ci_cliente --> nombre_municipio, nombre_sexo

> matricula_moto --> nombre_modelo, id_marca, nombre_marca, nombre_color, nombre_situacion

> (fecha_inicio, matricula_moto) --> nombre_forma_pago

> id_modelo --> nombre_marca

# Cardinalidad

- 1:N

MUNICIPIO-CLIENTE (un municipio tiene varios clientes, un cliente solo pertenece a un municipio)

MARCA-MODELO (una marca puede tener muchos modelos, un modelo solo le pertenece a una marca)

MODELO-MOTO (un modelo lo pueden tener muchas motos, pero una moto solo tiene un modelo)

CLIENTE-CONTRATO (un cliente puede tener muchos contratos y un contrato le pertenece a un solo cliente)

MOTO-CONTRATO (una moto puede tener varios contratos en dias distintos)

# 1FN

CONTRATO(<u>fecha_inicio</u>, <u>matricula_moto</u>, nombre_cliente, primer_apellido, segundo_apellido, ci_cliente, edad, id_sexo, nombre_sexo, numero_contacto, id_municipio, nombre_municipio, id_modelo, id_situacion, nombre_situacion, id_color, nombre_color, cant_km_recorridos, id_marca, nombre_modelo, nombre_marca, fecha_fin, id_forma_pago, nombre_forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega)

# 2FN (Eliminar dependencias parciales)

MOTO(<u>matricula_moto</u>, id_modelo, id_situacion, nombre_situacion, id_marca, nombre_modelo, nombre_marca, id_color, nombre_color, cant_km_recorridos)

CONTRATO(<u>fecha_inicio</u>, <u>matricula_moto</u>, nombre_cliente, primer_apellido, segundo_apellido, ci_cliente, edad, id_sexo, nombre_sexo, numero_contacto, id_municipio, nombre_municipio, fecha_fin, id_forma_pago, nombre_forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega)

# 3FN (Eliminar dependencias transitivas)

MOTO(<u>matricula_moto</u>, id_modelo, id_situacion, id_color, cant_km_recorridos)

COLOR(<u>id_color</u>, nombre_color)

SITUACION(<u>id_situacion</u>, nombre_situacion)

MODELO(<u>id_modelo</u>, id_marca, nombre_modelo)

MARCA(<u>id_marca</u>, nombre_marca)

CLIENTE(nombre_cliente, primer_apellido, segundo_apellido, <u>ci_cliente</u>, edad, id_sexo, numero_contacto, id_municipio)

SEXO(<u>id_sexo</u>, nombre_sexo)

MUNICIPIO(<u>id_municipio</u>, nombre_municipio)

FORMA_PAGO(<u>id_forma_pago</u>, nombre_forma_pago)

CONTRATO(<u>fecha_inicio</u>, <u>matricula_moto</u>, ci_cliente, fecha_fin, id_forma_pago, dias_prorroga, seguro_adicional, tarifa_normal, tarifa_prorroga, fecha_entrega)

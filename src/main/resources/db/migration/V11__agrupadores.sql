-- SELECT nombre_color FROM Color;

SELECT matricula_moto, model.nombre_modelo, sit.nombre_situacion, col.nombre_color, cant_km_recorridos
FROM Moto m
JOIN color col ON m.id_color = col.id_color
JOIN modelo model ON m.id_modelo = model.id_modelo
JOIN situacion sit ON m.id_situacion = sit.id_situacion;

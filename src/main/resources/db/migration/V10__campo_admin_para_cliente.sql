ALTER TABLE usuario ADD COLUMN es_admin BOOLEAN NOT NULL DEFAULT false;


INSERT INTO usuario (nombre_usuario, password, gmail, es_admin)
VALUES ('Dario_Admin', 'Admin123', 'admindario@motos.cu', true)
VALUES ('DarelL_Admin', 'Admin123', 'admindarell@motos.cu', true)
VALUES ('Lian_Admin', 'Admin123', 'adminlian@motos.cu', true)
ON CONFLICT (gmail) DO NOTHING;


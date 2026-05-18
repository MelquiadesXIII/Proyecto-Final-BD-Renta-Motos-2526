ALTER TABLE usuario ADD COLUMN IF NOT EXISTS es_admin BOOLEAN NOT NULL DEFAULT false;

INSERT INTO usuario (nombre_usuario, password, gmail, es_admin) VALUES
('Dario_Admin', 'Admin123', 'admindario@motos.cu', true),
('DarelL_Admin', 'Admin123', 'admindarell@motos.cu', true),
('Lian_Admin', 'Admin123', 'adminlian@motos.cu', true)
ON CONFLICT (gmail) DO NOTHING;
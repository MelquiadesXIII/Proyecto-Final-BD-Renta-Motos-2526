
INSERT INTO forma_pago (nombre_forma_pago) VALUES
    ('Cheque')
ON CONFLICT (nombre_forma_pago) DO NOTHING;


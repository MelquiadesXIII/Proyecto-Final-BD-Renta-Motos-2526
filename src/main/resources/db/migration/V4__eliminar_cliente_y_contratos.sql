CREATE OR REPLACE PROCEDURE eliminar_cliente_y_contratos(ci INT)
LANGUAGE plpgsql
AS $$
BEGIN
   
	DELETE FROM Contrato 
		WHERE ci_cliente = ci;
		
    DELETE FROM Cliente 
		WHERE ci_cliente = ci;
		
		
END;
$$;
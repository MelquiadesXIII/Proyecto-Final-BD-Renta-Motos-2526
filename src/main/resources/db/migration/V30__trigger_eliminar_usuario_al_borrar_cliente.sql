-- Al borrar un cliente, se elimina automáticamente su usuario asociado.
-- El trigger se ejecuta AFTER DELETE para que la FK cliente.id_usuario ya no exista
-- cuando se intente borrar el registro en usuario.

CREATE OR REPLACE FUNCTION eliminar_usuario_al_borrar_cliente()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM usuario WHERE id_usuario = OLD.id_usuario;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_eliminar_usuario_al_borrar_cliente ON cliente;

CREATE TRIGGER trg_eliminar_usuario_al_borrar_cliente
AFTER DELETE ON cliente
FOR EACH ROW EXECUTE FUNCTION eliminar_usuario_al_borrar_cliente();

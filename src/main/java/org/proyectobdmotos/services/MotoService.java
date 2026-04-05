package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.MotoDAO;

public class MotoService {

    private final MotoDAO motoDAO;

    public MotoService(MotoDAO motoDAO) {
        this.motoDAO = motoDAO;
    }

    public MotoDAO getMotoDAO() {
        return motoDAO;
    }
}

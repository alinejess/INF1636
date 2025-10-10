package model;

import java.util.*;

class Tabuleiro {
    private List<Casa> casas;

    public Tabuleiro() {
        casas = new ArrayList<>();
        inicializarCasas();
    }

    private void inicializarCasas() {
        casas.add(new Casa("Início"));
        casas.add(new Propriedade("Av. Paulista", 200, 50));
        casas.add(new Prisao("Vá para a prisão"));
        // etc.
    }

    public Casa getCasa(int posicao) {
        return casas.get(posicao % casas.size());
    }

    public int getTotalCasas() {
        return casas.size();
    }
}

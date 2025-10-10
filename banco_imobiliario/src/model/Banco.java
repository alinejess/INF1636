package model;

import java.util.*;

class Banco {
    private int saldo;
    private List<Propriedade> propriedades;

    public Banco() {
        saldo = 200000;
        propriedades = new ArrayList<>();
    }

    public void comprarPropriedade(Propriedade p) {
        saldo -= p.getValorTotal();
        p.setDono(null);
        propriedades.add(p);
    }

    public int getSaldo() {
        return saldo;
    }
}

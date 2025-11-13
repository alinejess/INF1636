package model;

final class Banco {
    public int saldo;
    
    Banco(int saldoInicial) { this.saldo = saldoInicial; }
    
    void receber(int valor) { this.saldo += valor; }
    
    /** Tenta debitar do banco. Retorna true se conseguiu. */
    boolean pagar(int valor) {
        if (valor < 0) valor = -valor;
        if (saldo >= valor) { saldo -= valor; return true; }
        // regra simples: banco nunca “quebra”; se quiser travar, retorne false
        saldo -= valor; // permite saldo negativo (ajuste se desejar)
        return true;
    }

    int getSaldo() { return saldo; }
}

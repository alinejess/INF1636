package model;

/** Classe base de qualquer casa do tabuleiro. */
abstract class Casa {
    final String nome;

    Casa(String nome) { this.nome = nome; }
    
    public String getNome() { return nome; }
}

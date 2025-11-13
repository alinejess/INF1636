package view;

import java.awt.Color;

public final class CoresJogador {

    private CoresJogador() {} // utilitário

    /** Enum com rótulo, índice do arquivo de pino (pin{indice}.png) e a cor RGB. */
    public enum CorPino {
        VERMELHO("Vermelho", 0, new Color(220,  60,  60)),
        AZUL     ("Azul",     1, new Color( 60, 120, 220)),
        LARANJA  ("Laranja",  2, new Color(255, 170,  61)),
        AMARELO  ("Amarelo",  3, new Color(255, 222,  33)),
        ROXO     ("Roxo",     4, new Color(170,  80, 190)),
        CINZA    ("Cinza",    5, new Color(137, 137, 137));

        private final String rotulo;
        private final int indicePino;
        private final Color cor;

        CorPino(String rotulo, int indicePino, Color cor) {
            this.rotulo = rotulo;
            this.indicePino = indicePino;
            this.cor = cor;
        }

        public String getRotulo()    { return rotulo; }
        public int getIndicePino()   { return indicePino; }
        public Color getCor()        { return cor; }

        /** Caminho padrão do pino correspondente (dentro de src/imagens/). */
        public String arquivoPino() { return "pinos/pin" + indicePino + ".png"; }

        /** Normaliza índices fora do intervalo. */
        public static CorPino porIndice(int i) {
            CorPino[] v = values();
            return v[Math.floorMod(i, v.length)];
        }
        
        public static CorPino porRotulo(String rotulo) {
            if (rotulo == null) return CorPino.VERMELHO;
            for (CorPino c : CorPino.values()) {
                if (c.getRotulo().equalsIgnoreCase(rotulo)) return c;
            }
            return CorPino.VERMELHO;
        }
        
        public static CorPino porNomeJogador(String nome) {
            if (nome == null) return CorPino.VERMELHO;
            String lower = nome.toLowerCase();
            for (CorPino c : CorPino.values()) {
                if (lower.contains(c.getRotulo().toLowerCase())) return c;
            }
            return CorPino.VERMELHO;
        }
        
        public static java.awt.Color corPorNomeJogador(String nome) {
            return porNomeJogador(nome).getCor();
        }
        
        public static String arquivoPinoPorNomeJogador(String nome) {
            return porNomeJogador(nome).arquivoPino(); // ex.: "pinos/pin3.png"
        }

        @Override public String toString() { return rotulo; }
    }

    public static Color corPorIndice(int idx) {
        return CorPino.porIndice(idx).getCor();
    }
}

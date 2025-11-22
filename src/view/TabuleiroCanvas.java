package view;

import model.GameModelo;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import controller.ControladorJogo;

class TabuleiroCanvas extends Canvas {

    private static final long serialVersionUID = 1L;

    private final GameModelo modelo;
    private final ControladorJogo controlador; 

    // Recursos
    private final Image imgTabuleiro;
    
    // dados
    private int ultimoD1 = 0, ultimoD2 = 0;
    private Image face1, face2;
    
    // pinos
    private final java.util.List<Image> imgPinos = new java.util.ArrayList<Image>(6);
    private String painelTitulo = null;
    private final java.util.List<String> painelLinhas = new java.util.ArrayList<String>();

    // Layout geral
    private final int alturaTopo = 96;   // faixa superior (dados/indicação do jogador)
    private final int margem    = 28;    // margem externa do canvas

    // Ajustes finos (pode calibrar)
    private static final double MARGEM_IMAGEM = 0.018; // 1,8% para dentro da imagem (compensa borda do PNG)
    private static final double FATOR_CANTO   = 0.148; // C = 14,8% do lado (cantos um pouco menores)
    private static final double FATOR_ARESTA  = 0.82;  // T = 82% de C (arestas mais finas)
    
    private GameModelo.CartaSorteRevesInfo ultimaCarta; // snapshot da carta aplicada
    private Image imagemCarta;           // imagem atual a desenhar
    private String origemCarta = null;   // "sorte" | "propriedade" | null
    private String idCartaAtual = null;  // ex.: "chance14.png"
    private String nomePropAtual = null; // ex.: "Av. Atlântica"

    private static final java.util.Map<String, String> MAPA_COMPANHIAS;
    static {
        java.util.Map<String, String> mapa = new java.util.HashMap<String, String>();
        mapa.put("Companhia Ferroviária", "companhias/company1.png");
        mapa.put("Companhia de Viação", "companhias/company2.png");
        mapa.put("Companhia de Táxi", "companhias/company3.png");
        mapa.put("Companhia de Navegação", "companhias/company4.png");
        mapa.put("Companhia de Aviação", "companhias/company5.png");
        mapa.put("Companhia de Táxi Aéreo", "companhias/company6.png");
        MAPA_COMPANHIAS = java.util.Collections.unmodifiableMap(mapa);
    }

    TabuleiroCanvas(GameModelo modelo, ControladorJogo controlador) {
        this.modelo = modelo;
        this.controlador = controlador; // NOVO
        setBackground(new Color(240, 248, 248));
        setFocusable(true);
        imgTabuleiro = RecursosImagem.get().carregar("tabuleiro.png");
    }
    
    
    public void definirDados(int d1, int d2) {
        this.ultimoD1 = d1;
        this.ultimoD2 = d2;
        this.face1 = carregarFace(d1);
        this.face2 = carregarFace(d2);
    }
    
    private Image carregarFace(int v) {
        if (v < 1 || v > 6) return null;
        return RecursosImagem.get().carregar("dados/die_face_" + v + ".png");
    }
    
    void registrarCarta(String idImagem) {
        this.idCartaAtual = idImagem;
        if (idImagem != null) {
            this.imagemCarta = RecursosImagem.get().carregar("sorteReves/" + idImagem);
            if (this.imagemCarta == null) {
                System.out.println("[IMG] NÃO encontrada: sorteReves/" + idImagem);
            }
        } else {
            this.imagemCarta = null;
        }
        repaint();
    }
    
    void registrarCartaSorte(String idImagem) {
        this.idCartaAtual = idImagem;
        this.nomePropAtual = null;
        this.origemCarta = (idImagem != null ? "sorte" : null);

        if (idImagem != null) {
            // OBS: caminho é relativo à pasta src/imagens/
            this.imagemCarta = RecursosImagem.get().carregar("sorteReves/" + idImagem);
            if (this.imagemCarta == null) {
                System.out.println("[IMG] NÃO encontrada: sorteReves/" + idImagem);
            }
        } else {
            this.imagemCarta = null;
        }
        repaint();
    }

    // PROPRIEDADE: registra e carrega carta da propriedade (mesmo nome do território)
    void registrarCartaPropriedade(String nomePropriedade) {
    	this.idCartaAtual = null;
        this.nomePropAtual = nomePropriedade;
        this.idCartaAtual = null;
        this.origemCarta = (nomePropriedade != null ? "propriedade" : null);
        this.imagemCarta = null;
        
        if (nomePropriedade == null) { repaint(); return; }

     // 1) tentativas diretas
        String[] candidatos = new String[] {
            "territorios/" + nomePropriedade + ".png",
            "territorios/" + nomePropriedade + ".PNG"
        };

        // 2) fallback com normalização (remove acentos e troca espaços por _)
        candidatos = concat(candidatos, new String[] {
            "territorios/" + nomePropriedade + ".png",
            "territorios/" + nomePropriedade + ".PNG"
        });

        for (String path : candidatos) {
            Image img = RecursosImagem.get().carregar(path);
            if (img != null) {
                this.imagemCarta = img;
                System.out.println("[PROP] exibindo carta: " + path);
                repaint();
                return;
            } else {
                System.out.println("[PROP] NÃO encontrada: " + path);
            }
        }
        repaint();
    }

    void registrarCartaCompanhia(String nomeCompanhia) {
        this.idCartaAtual = null;
        this.nomePropAtual = nomeCompanhia;
        this.origemCarta = (nomeCompanhia != null ? "companhia" : null);
        this.imagemCarta = null;

        if (nomeCompanhia == null) {
            repaint();
            return;
        }

        String caminho = MAPA_COMPANHIAS.get(nomeCompanhia);
        if (caminho != null) {
            Image img = RecursosImagem.get().carregar(caminho);
            if (img != null) {
                this.imagemCarta = img;
            } else {
                System.out.println("[COMP] NÃO encontrada: " + caminho);
            }
        } else {
            System.out.println("[COMP] Sem mapeamento para: " + nomeCompanhia);
        }
        repaint();
    }
    
    private static String[] concat(String[] a, String[] b) {
        String[] r = new String[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
    
    void limparCarta() {
        this.idCartaAtual = null;
        this.nomePropAtual = null;
        this.origemCarta = null;
        this.imagemCarta = null;
        repaint();
    }

    void atualizarInformacoesJogador(GameModelo.VisaoJogador jogador, GameModelo.VisaoCasa visaoCasa) {
        if (jogador == null) {
            limparInformacoesJogador();
            return;
        }
        this.painelTitulo = jogador.nome;
        this.painelLinhas.clear();
        painelLinhas.add("Saldo: $" + jogador.saldo);
        painelLinhas.add("Posição: " + jogador.posicao);
        painelLinhas.add("Na prisão: " + (jogador.naPrisao ? "Sim" : "Não"));
        if (jogador.temCartaSaidaDaPrisao) {
            painelLinhas.add("Carta: Sair da prisão");
        }
        painelLinhas.add(" ");
        painelLinhas.add("Propriedades:");
        if (jogador.propriedades == null || jogador.propriedades.isEmpty()) {
            painelLinhas.add("  — nenhuma");
        } else {
            for (String prop : jogador.propriedades) {
                painelLinhas.add("  • " + prop);
            }
        }
        painelLinhas.add(" ");
        painelLinhas.add("Companhias:");
        if (jogador.companhias == null || jogador.companhias.isEmpty()) {
            painelLinhas.add("  — nenhuma");
        } else {
            for (String comp : jogador.companhias) {
                painelLinhas.add("  • " + comp);
            }
        }
        if (visaoCasa != null && visaoCasa.nome != null) {
            painelLinhas.add(" ");
            painelLinhas.add("Casa atual: " + visaoCasa.nome);
            if ("PROPRIEDADE".equals(visaoCasa.tipo)) {
                if (visaoCasa.preco != null) painelLinhas.add("  Preço: $" + visaoCasa.preco);
                String dono = (visaoCasa.proprietario == null ? "Disponível" : visaoCasa.proprietario);
                painelLinhas.add("  Dono: " + dono);
                if (visaoCasa.hotel != null && visaoCasa.hotel.booleanValue()) {
                    painelLinhas.add("  Construções: HOTEL");
                } else {
                    int casas = (visaoCasa.casas != null ? visaoCasa.casas.intValue() : 0);
                    painelLinhas.add("  Casas: " + casas);
                }
            } else if ("COMPANHIA".equals(visaoCasa.tipo)) {
                if (visaoCasa.preco != null) painelLinhas.add("  Preço: $" + visaoCasa.preco);
                String dono = (visaoCasa.proprietario == null ? "Disponível" : visaoCasa.proprietario);
                painelLinhas.add("  Dono: " + dono);
            }
        }
        repaint();
    }

    void limparInformacoesJogador() {
        this.painelTitulo = null;
        this.painelLinhas.clear();
        repaint();
    }
    
    private java.awt.Image escolherPinoPorNome(String nome) {
        String arquivo = CoresJogador.CorPino.arquivoPinoPorNomeJogador(nome);
        return RecursosImagem.get().carregar(arquivo);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            
            String nomeAtual = "";
            try { nomeAtual = modelo.obterJogadorDaVez().nome; } catch (Throwable ignored) {}

            view.CoresJogador.CorPino corAtual = view.CoresJogador.CorPino.porNomeJogador(nomeAtual);

            g2.setColor(corAtual.getCor());
            g2.fillRect(0, 0, w, alturaTopo);

            g2.setColor(java.awt.Color.white);
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
            g2.drawString("Jogador da vez: Jogador " + corAtual.getRotulo(), 16, 30);
            
            // exibição da ordem dos jogadores na faixa
            java.util.List<model.GameModelo.VisaoJogador> js = modelo.obterJogadores();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < js.size(); i++) {
                if (i > 0) sb.append("  \u2192  ");
                String n = js.get(i).nome;
                String rot = view.CoresJogador.CorPino.porNomeJogador(n).getRotulo();
                if (n.equals(nomeAtual)) sb.append("[Jogador ").append(rot).append("]");
                else                     sb.append("Jogador ").append(rot);
            }
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
            g2.drawString("Ordem: " + sb, 16, 52);
                                  
            // faces dos dados
            int box = alturaTopo - 20;
            int y0  = (alturaTopo - box) / 2; 
            int x2  = w - 16 - box;
            int x1  = x2 - 12 - box;
            
            this.face1 = RecursosImagem.get().carregar("dados/die_face_" + ultimoD1 + ".png");
            this.face2 = RecursosImagem.get().carregar("dados/die_face_" + ultimoD2 + ".png");
            
            if (face1 != null) g2.drawImage(face1, x1, y0, box, box, this);
            if (face2 != null) g2.drawImage(face2, x2, y0, box, box, this);

            // Área do tabuleiro (cheia) + área útil (um pouco menor)
            Rectangle area = calcularAreaTabuleiro(w, h);
            Rectangle areaUtil = areaUtilDoTabuleiro(area);

            // Desenha a imagem de fundo (área cheia)
            if (imgTabuleiro != null) {
                g2.drawImage(imgTabuleiro, area.x, area.y, area.width, area.height, this);
            } else {
                g2.setColor(new Color(230, 238, 238));
                g2.fillRect(areaUtil.x, areaUtil.y, areaUtil.width, areaUtil.height);
            }

            desenharPinos(g2, areaUtil);
            
            desenharCartaCentral(g2, areaUtil);
            desenharPainelInformacoes(g2, area);
            
//         // ===== CARTA SORTE/REVÉS =====
//            if (imagemCarta != null) {                
//                desenharCartaCentral(g2, area);
//            }

        } finally { 
            g2.dispose();
        }
        
    }
    
    private void desenharCartaCentral(Graphics2D g2, Rectangle areaTabuleiro) {
        if (imagemCarta == null) return;

        // Área central “por dentro” do tabuleiro para não encostar nas casas
        int pad = Math.max(8, (int)(areaTabuleiro.width * 0.04));
        int maxW = areaTabuleiro.width  - 2 * pad;
        int maxH = areaTabuleiro.height - 2 * pad;

        // Tamanho natural da imagem
        int iw = Math.max(1, imagemCarta.getWidth(this));
        int ih = Math.max(1, imagemCarta.getHeight(this));

        // Mantém tamanho real se couber; senão, reduz proporcionalmente
        double sx = (double) maxW / iw;
        double sy = (double) maxH / ih;
        double s  = Math.min(1.0, Math.min(sx, sy));

        int dw = (int) Math.round(iw * s);
        int dh = (int) Math.round(ih * s);

        int cx = areaTabuleiro.x + (areaTabuleiro.width  - dw) / 2;
        int cy = areaTabuleiro.y + (areaTabuleiro.height - dh) / 2;

        g2.drawImage(imagemCarta, cx, cy, dw, dh, this);
    }
    
    /** Desenha todos os pinos ativos na casa correspondente, distribuindo em 6 pistas. */
    /** Desenha pinos usando tamanho nativo, reduzindo apenas se não couber na casa. */
    private void desenharPinos(Graphics2D g2, Rectangle areaUtil) {
        java.util.List<GameModelo.VisaoJogador> js = modelo.obterJogadores();

        for (GameModelo.VisaoJogador vj : js) {
            if (!vj.ativo) continue;

            int casa  = ((vj.posicao % 40) + 40) % 40;
            int pista = vj.indice % 6;

            Rectangle r = retanguloDaCasa(areaUtil, casa);
            Point p = pontoDaPista(r, casa, pista);

            // dentro de desenharPinos(...)
            java.awt.Image img = escolherPinoPorNome(vj.nome);

            // Image img = escolherPino(vj.indice);
            if (img != null) {
                int iw = img.getWidth(this);
                int ih = img.getHeight(this);
                if (iw <= 0 || ih <= 0) continue;

                // Desenha SEM escala: tamanho nativo, centralizado no ponto da pista
                int x = p.x - iw / 2;
                int y = p.y - ih / 2;
                g2.drawImage(img, x, y, iw, ih, this);
            } else {
                // fallback simples se a imagem não for encontrada
                int s = 22; // tamanho fixo
                g2.setColor(CoresJogador.corPorIndice(vj.indice));
                g2.fillOval(p.x - s/2, p.y - s/2, s, s);
                g2.setColor(Color.BLACK);
                g2.drawOval(p.x - s/2, p.y - s/2, s, s);
            }
        }
    }
    
    private void desenharPropriedade(Graphics2D g2, Rectangle area, Rectangle areaUtil) {
        GameModelo.VisaoCasa v = modelo.obterCasaAtual();
        if (v == null || !"PROPRIEDADE".equals(v.tipo)) return;

        // área livre à direita do tabuleiro
        int compW = getWidth();
        int pad   = Math.max(8, (int)(compW * 0.01));
        int rightX = area.x + area.width + pad;
        int rightW = compW - rightX - pad;
        int rightH = area.height;

        if (rightW < 120 || rightH < 120) return; // sem espaço útil

        // tenta carregar a imagem do território (nome.png) uma vez por repintada
        Image imgTerr = null;
        if (v.nome != null) {
            imgTerr = RecursosImagem.get().carregar("territorios/" + v.nome + ".png");
        }
        
        desenharCartaCentral(g2, areaUtil);

//        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
//        g2.drawString("Preço: " + (v.preco != null ? v.preco : "-"), rightX + 12, cursorY); cursorY += 16;
//        g2.drawString("Custo da casa: " + (v.custoCasa != null ? v.custoCasa : "-"), rightX + 12, cursorY); cursorY += 16;
//
//        String dono = (v.proprietario == null ? "— sem dono —" : v.proprietario);
//        if (v.indiceProprietario != null) {
//            Color corDono = CoresJogador.corPorIndice(v.indiceProprietario.intValue());
//            g2.setColor(corDono);
//            g2.fillRect(rightX + 12, cursorY - 11, 10, 10);
//            g2.setColor(new Color(20, 30, 40));
//            g2.drawString("Proprietário: " + dono, rightX + 28, cursorY);
//        } else {
//            g2.drawString("Proprietário: " + dono, rightX + 12, cursorY);
//        }
//        cursorY += 18;
//
//        String hc = "Construções: " + (v.hotel != null && v.hotel.booleanValue() ? "HOTEL" :
//                ("casas = " + (v.casas != null ? v.casas : 0)));
//        g2.drawString(hc, rightX + 12, cursorY);
    }


    private Image escolherPino(int indice) {
        view.CoresJogador.CorPino c = view.CoresJogador.CorPino.porIndice(indice);
        return RecursosImagem.get().carregar(c.arquivoPino()); // "pinos/pinX.png"
    }

    
    /**
     * Centro do pino para uma pista (0..5) dentro do retângulo da casa.
     * - Nas horizontais (topo/base): 6 pistas distribuídas na ALTURA (Y).
     * - Nas verticais (esq/dir):     6 pistas distribuídas na LARGURA (X).
     * - Nos cantos: grade 3x2 (6 posições).
     */
    private Point pontoDaPista(Rectangle r, int casa, int pista) {
        // cantos: 3 colunas x 2 linhas
        if (casa == 0 || casa == 10 || casa == 20 || casa == 30) {
            int cols = 3, rows = 2;
            int col = pista % cols;
            int row = pista / cols;
            double cw = r.width  / (double) cols;
            double rh = r.height / (double) rows;
            int cx = (int) Math.round(r.x + (col + 0.5) * cw);
            int cy = (int) Math.round(r.y + (row + 0.5) * rh);
            return new Point(cx, cy);
        }

        boolean horizontal = r.width > r.height; // topo/base
        if (horizontal) {
            // distribui no eixo Y (altura da faixa), X no centro
            double step = r.height / 7.0;        // margem ~1/7 em cima e embaixo
            int cy = (int) Math.round(r.y + step * (pista + 1));
            int cx = r.x + r.width / 2;
            return new Point(cx, cy);
        } else {
            // distribui no eixo X (largura da faixa), Y no centro
            double step = r.width / 7.0;
            int cx = (int) Math.round(r.x + step * (pista + 1));
            int cy = r.y + r.height / 2;
            return new Point(cx, cy);
        }
    }

    // helper simples
    private static final class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }


    /* ========================== GEOMETRIA ========================== */

    private Rectangle calcularAreaTabuleiro(int w, int h) {
        int hDisp = h - alturaTopo;
        int lado = Math.min(w - 2 * margem, hDisp - 2 * margem);
        if (lado < 100) lado = Math.min(w, hDisp);
        int x = (w - lado) / 2;
        int y = alturaTopo + (hDisp - lado) / 2;
        return new Rectangle(x, y, lado, lado);
    }

    private Rectangle areaUtilDoTabuleiro(Rectangle area) {
        int dx = (int) Math.round(area.width  * MARGEM_IMAGEM);
        int dy = (int) Math.round(area.height * MARGEM_IMAGEM);
        return new Rectangle(area.x + dx, area.y + dy, area.width - 2 * dx, area.height - 2 * dy);
    }

    private int tamanhoCanto(Rectangle area) {
        return Math.max(8, (int) Math.round(area.width * FATOR_CANTO));
    }

    private int espessuraAresta(Rectangle area) {
        int C = tamanhoCanto(area);
        return Math.max(4, (int) Math.round(C * FATOR_ARESTA));
    }

    private int larguraArestaBase(Rectangle area) {
        int C = tamanhoCanto(area);
        int restante = area.width - 2 * C;
        return Math.max(4, restante / 9);
    }

    private int restoAresta(Rectangle area) {
        int C = tamanhoCanto(area);
        int restante = area.width - 2 * C;
        return Math.max(0, restante - 9 * larguraArestaBase(area));
    }

    private int somaLarguras(int n, Rectangle area) {
        if (n <= 0) return 0;
        int base = larguraArestaBase(area);
        int resto = restoAresta(area);
        int extra = Math.min(n, resto);
        return n * base + extra;
    }

    private Rectangle retanguloDaCasa(Rectangle area, int i) {
        int C = tamanhoCanto(area);
        int T = espessuraAresta(area);
        int E = larguraArestaBase(area);

        int x, y, w, h;

        if (i == 0)  { x = area.x + area.width  - C; y = area.y + area.height - C; w = C; h = C; return new Rectangle(x, y, w, h); }
        if (i == 10) { x = area.x;               y = area.y + area.height - C; w = C; h = C; return new Rectangle(x, y, w, h); }
        if (i == 20) { x = area.x;               y = area.y;                    w = C; h = C; return new Rectangle(x, y, w, h); }
        if (i == 30) { x = area.x + area.width  - C; y = area.y;               w = C; h = C; return new Rectangle(x, y, w, h); }

        if (i >= 1 && i <= 9) {
            int k = i;
            int largura = E + (k <= restoAresta(area) ? 1 : 0);
            x = area.x + area.width - C - somaLarguras(k, area);
            y = area.y + area.height - T;
            w = largura; h = T;
            return new Rectangle(x, y, w, h);
        }

        if (i >= 11 && i <= 19) {
            int k = i - 10;
            int altura = E + (k <= restoAresta(area) ? 1 : 0);
            x = area.x;
            y = area.y + area.height - C - somaLarguras(k, area);
            w = T;
            h = altura;
            return new Rectangle(x, y, w, h);
        }

        if (i >= 21 && i <= 29) {
            int k = i - 20;
            int largura = E + (k <= restoAresta(area) ? 1 : 0);
            x = area.x + C + somaLarguras(k - 1, area);
            y = area.y;
            w = largura; h = T;
            return new Rectangle(x, y, w, h);
        }

        int k = i - 30;
        int altura = E + (k <= restoAresta(area) ? 1 : 0);
        x = area.x + area.width - T;
        y = area.y + C + somaLarguras(k - 1, area);
        w = T; h = altura;
        return new Rectangle(x, y, w, h);
    }

    private void desenharPainelInformacoes(Graphics2D g2, Rectangle areaTabuleiro) {
        if (painelTitulo == null || painelLinhas.isEmpty()) return;
        int pad = Math.max(12, (int)(getWidth() * 0.015));
        int disponivel = areaTabuleiro.x - pad;
        int panelWidth = Math.min(260, disponivel);
        if (panelWidth < 150) return;
        int panelX = areaTabuleiro.x - panelWidth - pad;
        int panelY = areaTabuleiro.y;
        int panelHeight = areaTabuleiro.height;
        g2.setColor(new Color(250, 248, 240, 230));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);
        g2.setColor(new Color(200, 200, 200));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(painelTitulo, panelX + 16, panelY + 28);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int cursorY = panelY + 52;
        for (String linha : painelLinhas) {
            if (" ".equals(linha)) { cursorY += 8; continue; }
            g2.drawString(linha, panelX + 12, cursorY);
            cursorY += 18;
            if (cursorY > panelY + panelHeight - 20) break;
        }
    }
}

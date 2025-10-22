package ui;

import controller.GameFacade;
import model.Casa;
import model.Jogador;
import model.Propriedade;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class BoardPanel extends JPanel {
    private final GameFacade facade;
    private int d1 = 0, d2 = 0;
    private Propriedade overlayProperty = null;

    public BoardPanel(GameFacade facade) {
        this.facade = facade;
        setOpaque(true);
        setBackground(new Color(34, 34, 34)); // fundo escuro p/ destacar
    }

    public void setDice(int d1, int d2) { this.d1 = d1; this.d2 = d2; }
    public void showPropertyOverlay(Propriedade p) { this.overlayProperty = p; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int pad = 16;

        // área do tabuleiro
        int boardW = Math.min(w - 280 - pad * 2, 940);
        int boardH = Math.min(h - pad * 2, 720);
        int bx = pad, by = pad;

        // fundo do tabuleiro (alto contraste)
        Shape outer = new RoundRectangle2D.Double(bx, by, boardW, boardH, 28, 28);
        g2.setColor(new Color(0x2E7D32)); // verde “feltro”
        g2.fill(outer);
        g2.setStroke(new BasicStroke(4f));
        g2.setColor(new Color(0x1B5E20));
        g2.draw(outer);

        // info de diagnóstico visível
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        int n = 0;
        try { n = Math.max(0, facade.getTotalCasas()); } catch (Exception ignored) {}
        g2.drawString("Casas no tabuleiro: " + n, bx + 16, by + 24);
        g2.drawString("Jogador atual: " + facade.getJogadorAtual().getNome(), bx + 16, by + 44);

        // 6 pistas (faixas) para piões
        int lanes = 6;
        for (int i = 0; i < lanes; i++) {
            float t = (i + 1) / (float) (lanes + 1);
            int inset = (int) (t * 60);
            Shape lane = new RoundRectangle2D.Double(
                    bx + inset, by + inset,
                    boardW - 2 * inset, boardH - 2 * inset,
                    24, 24);
            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(1.8f));
            g2.draw(lane);
        }

        // se não houver casas ainda, avisa bem grande
        if (n <= 0) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
            g2.setColor(Color.WHITE);
            g2.drawString("⚠ Tabuleiro com 0 casas — verifique Tabuleiro.getTotalCasas()", bx + 16, by + 80);
        } else {
            // marcações das casas no perímetro (pontinhos + índice)
            Point center = new Point(bx + boardW / 2, by + boardH / 2);
            int rx = (boardW - 90) / 2;
            int ry = (boardH - 90) / 2;

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            for (int idx = 0; idx < n; idx++) {
                double ang = (Math.PI * 2 * idx) / n - Math.PI / 2;
                int cx = center.x + (int) (rx * Math.cos(ang));
                int cy = center.y + (int) (ry * Math.sin(ang));
                g2.setColor(new Color(255, 255, 255, 180));
                g2.fillOval(cx - 4, cy - 4, 8, 8);
                g2.setColor(Color.BLACK);
                g2.drawString("#" + idx, cx - 10, cy - 10);
            }

            // desenha piões (alto contraste)
            drawPawns(g2, center, rx, ry, lanes);
        }

        // área dos dados (bem visível)
        paintDiceArea(g2, w - 260, by, 240, 160);

        // cartão da propriedade (se houver)
        if (overlayProperty != null) {
            paintPropertyOverlay(g2, overlayProperty, bx + boardW - 300, by + 180, 280, 360);
        }

        g2.dispose();
    }

    private void paintDiceArea(Graphics2D g2, int x, int y, int w, int h) {
        Color playerColor = facade.getColorFor(facade.getJogadorAtual().getNome());
        g2.setColor(new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), 140));
        g2.fillRoundRect(x, y, w, h, 18, 18);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(x, y, w, h, 18, 18);

        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString("Jogador da vez: " + facade.getJogadorAtual().getNome(), x + 14, y + 22);

        // imagens dos dados (fallback desenhado)
        Image i1 = Resources.getInstance().getDice(Math.max(1, d1));
        Image i2 = Resources.getInstance().getDice(Math.max(1, d2));
        int dx = x + 20, dy = y + 40, size = 54;
        if (i1 != null) g2.drawImage(i1, dx, dy, size, size, null); else drawDieFallback(g2, dx, dy, size, Math.max(1, d1));
        if (i2 != null) g2.drawImage(i2, dx + size + 18, dy, size, size, null); else drawDieFallback(g2, dx + size + 18, dy, size, Math.max(1, d2));
    }

    private void drawDieFallback(Graphics2D g2, int x, int y, int s, int value) {
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, s, s, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x, y, s, s, 10, 10);
        int r = 6, cx = x + s / 2, cy = y + s / 2, off = s / 4;
        switch (value) {
            case 1: g2.fillOval(cx - r, cy - r, 2 * r, 2 * r); break;
            case 2:
                g2.fillOval(cx - off - r, cy - off - r, 2 * r, 2 * r);
                g2.fillOval(cx + off - r, cy + off - r, 2 * r, 2 * r); break;
            case 3:
                g2.fillOval(cx - off - r, cy - off - r, 2 * r, 2 * r);
                g2.fillOval(cx + off - r, cy + off - r, 2 * r, 2 * r);
                g2.fillOval(cx - r, cy - r, 2 * r, 2 * r); break;
            case 4:
                g2.fillOval(cx - off - r, cy - off - r, 2 * r, 2 * r);
                g2.fillOval(cx + off - r, cy - off - r, 2 * r, 2 * r);
                g2.fillOval(cx - off - r, cy + off - r, 2 * r, 2 * r);
                g2.fillOval(cx + off - r, cy + off - r, 2 * r, 2 * r); break;
            case 5:
                drawDieFallback(g2, x, y, s, 4);
                g2.fillOval(cx - r, cy - r, 2 * r, 2 * r); break;
            default: // 6
                g2.fillOval(cx - off - r, cy - off - r, 2 * r, 2 * r);
                g2.fillOval(cx + off - r, cy - off - r, 2 * r, 2 * r);
                g2.fillOval(cx - off - r, cy, 2 * r, 2 * r);
                g2.fillOval(cx + off - r, cy, 2 * r, 2 * r);
                g2.fillOval(cx - off - r, cy + off - r, 2 * r, 2 * r);
                g2.fillOval(cx + off - r, cy + off - r, 2 * r, 2 * r); break;
        }
    }

    private void drawPawns(Graphics2D g2, Point center, int rx, int ry, int lanes) {
        List<Jogador> js = facade.getJogadores();
        int n = Math.max(1, facade.getTotalCasas());
        int pawnSize = 18;

        for (int idx = 0; idx < js.size(); idx++) {
            Jogador j = js.get(idx);
            if (!j.isAtivo()) continue;

            int tile = facade.getPosicao(j);
            double ang = (Math.PI * 2 * tile) / n - Math.PI / 2;

            int lane = idx % lanes;
            double t = (lane + 1) / (double) (lanes + 1);
            int px = center.x + (int)((rx - 15) * Math.cos(ang) * t);
            int py = center.y + (int)((ry - 15) * Math.sin(ang) * t);

            Color c = facade.getColorFor(j.getNome());
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 230));
            g2.fillOval(px - pawnSize / 2, py - pawnSize / 2, pawnSize, pawnSize);
            g2.setColor(Color.BLACK);
            g2.drawOval(px - pawnSize / 2, py - pawnSize / 2, pawnSize, pawnSize);

            if (facade.isPreso(j)) {
                g2.setColor(new Color(0, 0, 0, 160));
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(px - 9, py - 9, 18, 18, 220, 100);
                g2.drawRect(px - 6, py - 2, 12, 10);
            }
        }
    }

    private void paintPropertyOverlay(Graphics2D g2, Propriedade p, int x, int y, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRoundRect(x, y, w, h, 14, 14);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 14, 14);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Propriedade", x + 14, y + 26);

        facade.getOwner(p).ifPresent(owner -> {
            Color c = facade.getColorFor(owner.getNome());
            g2.setColor(c);
            g2.fillRoundRect(x + 14, y + 36, w - 28, 18, 10, 10);
        });

        Image cardImg = Resources.getInstance().getCardByOriginalName(p.getNome());
        int imgH = 180, imgW = w - 40;
        int imgX = x + 20, imgY = y + 64;
        if (cardImg != null) {
            double scale = Math.min(imgW / (double) cardImg.getWidth(null), imgH / (double) cardImg.getHeight(null));
            int dw = (int) (cardImg.getWidth(null) * scale);
            int dh = (int) (cardImg.getHeight(null) * scale);
            int ox = imgX + (imgW - dw) / 2;
            int oy = imgY + (imgH - dh) / 2;
            g2.drawImage(cardImg, ox, oy, dw, dh, null);
        } else {
            g2.setColor(new Color(245, 245, 245));
            g2.fillRoundRect(imgX, imgY, imgW, imgH, 10, 10);
            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(imgX, imgY, imgW, imgH, 10, 10);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("(sem imagem: " + p.getNome() + ")", imgX + 12, imgY + imgH / 2);
        }

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 13f));
        int ty = y + 64 + imgH + 26;
        g2.drawString("Nome: " + p.getNome(), x + 16, ty); ty += 22;
        g2.drawString("Preço: $ " + p.getPreco(), x + 16, ty); ty += 22;
        g2.drawString("Casas: " + p.getCasas() + (p.isHotel() ? "  (Hotel)" : ""), x + 16, ty);
    }
}

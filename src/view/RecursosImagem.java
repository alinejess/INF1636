package view;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public final class RecursosImagem {

    private static final RecursosImagem INST = new RecursosImagem();
    public static RecursosImagem get() { return INST; }

    private final Map<String, Image> cache = new HashMap<String, Image>();

    private RecursosImagem() {}

    public Image carregar(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.isEmpty()) return null;
        Image img = cache.get(nomeArquivo);
        if (img != null) return img; 

        // 1) tenta classpath: /imagens/<nome>
        try {
            URL url = Thread.currentThread()
                            .getContextClassLoader()
                            .getResource("imagens/" + nomeArquivo);
            if (url != null) {
                BufferedImage bi = ImageIO.read(url);
                cache.put(nomeArquivo, bi);
                return bi;
            }
        } catch (IOException ignored) {}

        // 2) tenta arquivo relativo comum (ex.: "src/imagens/tabuleiro.png" já copiado para bin)
        try {
            File f1 = new File("src/imagens/" + nomeArquivo);
            if (f1.exists()) {
                BufferedImage bi = ImageIO.read(f1);
                cache.put(nomeArquivo, bi);
                return bi;
            }
            File f2 = new File(nomeArquivo);
            if (f2.exists()) {
                BufferedImage bi = ImageIO.read(f2);
                cache.put(nomeArquivo, bi);
                return bi;
            }
        } catch (IOException ignored) {}

        // 3) fallback: tenta Toolkit (lazy-loading)
        Image tk = Toolkit.getDefaultToolkit().getImage(nomeArquivo);
        cache.put(nomeArquivo, tk);
        return tk;
    }
}

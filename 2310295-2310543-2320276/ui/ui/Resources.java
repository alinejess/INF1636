package ui;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache de imagens (dados, piões, cartas/territórios/companhias).
 * Suporta os nomes originais do ZIP:
 *   - /img/dados/die_face_1.png ... die_face_6.png
 *   - /img/pinos/pin0.png ... pinN.png
 *   - /img/territorios/<Nome Original>.(png|jpg|jpeg)
 *   - /img/companhias/company1.png ... company6.png (ou nomes originais)
 *   - /img/cards/<...>.* (compatibilidade)
 */
public class Resources {
    private static final Resources INSTANCE = new Resources();
    private final Map<String, Image> cache = new ConcurrentHashMap<>();

    private static final String[] CARD_FOLDERS = {
            "/img/territorios/",   // nomes originais do ZIP
            "/img/territórios/",   // caso tenha acento na pasta
            "/img/companhias/",
            "/img/cards/"          // compat
    };

    private Resources() {}
    public static Resources getInstance() { return INSTANCE; }

    /** Carrega uma imagem do classpath. */
    public Image get(String path) {
        return cache.computeIfAbsent(path, p -> {
            try {
                URL url = Resources.class.getResource(p);
                if (url == null) return null;
                BufferedImage img = ImageIO.read(url);
                return img;
            } catch (IOException e) {
                return null;
            }
        });
    }

    /** Dados: cobre os nomes originais do ZIP e variações comuns. */
    public Image getDice(int value) {
        List<String> candidates = Arrays.asList(
                "/img/dados/die_face_" + value + ".png",   // ZIP original
                "/img/dice_" + value + ".png",
                "/img/dado_" + value + ".png",
                "/img/dados/dado_" + value + ".png",
                "/img/dados/dice_" + value + ".png"
        );
        return firstExisting(candidates);
    }

    /** Pinos por índice (opcional, caso queira usar imagem de pino). */
    public Image getPawn(int idx) {
        List<String> candidates = Arrays.asList(
                "/img/pinos/pin" + idx + ".png",
                "/img/pins/pin" + idx + ".png"
        );
        return firstExisting(candidates);
    }

    /** Compat antiga: nome sanitizado. */
    public Image getCard(String sanitizedName) {
        List<String> tries = new ArrayList<>();
        for (String base : CARD_FOLDERS) {
            tries.add(base + sanitizedName + ".png");
            tries.add(base + sanitizedName + ".jpg");
            tries.add(base + sanitizedName + ".jpeg");
            // também tenta sem extensão
            tries.add(base + sanitizedName);
        }
        return firstExisting(tries);
    }

    /** NOVO: tenta carregar usando o NOME ORIGINAL da propriedade (com acentos/espaços). */
    public Image getCardByOriginalName(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) return null;

        String name = originalName.trim();
        List<String> candidates = new ArrayList<>();

        // 1) tenta exatamente como veio + extensões mais comuns
        for (String base : CARD_FOLDERS) {
            if (hasKnownExt(name)) {
                candidates.add(base + name);
            } else {
                candidates.add(base + name + ".png");
                candidates.add(base + name + ".jpg");
                candidates.add(base + name + ".jpeg");
            }
        }

        // 2) sem acentos
        String noAccents = stripAccents(name);
        if (!noAccents.equals(name)) {
            for (String base : CARD_FOLDERS) {
                if (hasKnownExt(noAccents)) {
                    candidates.add(base + noAccents);
                } else {
                    candidates.add(base + noAccents + ".png");
                    candidates.add(base + noAccents + ".jpg");
                    candidates.add(base + noAccents + ".jpeg");
                }
            }
        }

        // 3) sanitizado (compatibilidade)
        String sanitized = noAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        for (String base : CARD_FOLDERS) {
            candidates.add(base + sanitized + ".png");
            candidates.add(base + sanitized + ".jpg");
            candidates.add(base + sanitized + ".jpeg");
        }

        // 4) mapeamentos fixos gerados do ZIP (se quiser forçar extensão/caminho)
        String fixed = CardIndex.CARD_FILES.get(name);
        if (fixed != null) {
            candidates.add(0, fixed); // prioriza mapeamento exato
        }

        return firstExisting(candidates);
    }

    // ---------------- helpers ----------------
    private Image firstExisting(List<String> candidates) {
        for (String c : candidates) {
            Image img = get(c);
            if (img != null) return img;
        }
        return null;
    }

    private static boolean hasKnownExt(String s) {
        String x = s.toLowerCase(Locale.ROOT);
        return x.endsWith(".png") || x.endsWith(".jpg") || x.endsWith(".jpeg");
    }

    private static String stripAccents(String s) {
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}+", "");
    }
}

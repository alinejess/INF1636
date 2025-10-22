package ui;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mapeia o nome EXATO da propriedade (p.getNome()) -> caminho do arquivo no classpath.
 * Pode deixar vazio; os fallbacks do Resources continuam funcionando.
 * Adicione linhas com CARD_FILES.put("Nome da Propriedade", "/img/territorios/Nome da Propriedade.png");
 */
public final class CardIndex {
    private CardIndex() {}

    public static final Map<String, String> CARD_FILES = new LinkedHashMap<>();

    static {
        // Exemplos (adicione os seus conforme os arquivos que você tem):
        // CARD_FILES.put("Copacabana", "/img/territorios/Copacabana.png");
        // CARD_FILES.put("Av. Rebouças", "/img/territorios/Av. Rebouças.png");
        // CARD_FILES.put("company1", "/img/companhias/company1.png");
    }
}

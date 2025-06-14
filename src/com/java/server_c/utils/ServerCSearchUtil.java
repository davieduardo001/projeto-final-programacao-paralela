package server_c.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class ServerCSearchUtil {

    private static final String JSON_FILE_PATH = "/com/java/server_c/data/dados_servidor_c.json";
    private JSONArray knowledgeBase;

    public ServerCSearchUtil() {
        loadKnowledgeBase();
    }

    private void loadKnowledgeBase() {
        try (InputStream inputStream = ServerCSearchUtil.class.getResourceAsStream(JSON_FILE_PATH)) {
            if (inputStream == null) {
                System.err.println("Servidor C (ServerCSearchUtil): ERRO CRÍTICO - Não foi possível encontrar o arquivo JSON: " + JSON_FILE_PATH);
                knowledgeBase = new JSONArray(); 
                return;
            }
            String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (jsonText.isEmpty()) {
                System.err.println("Servidor C (ServerCSearchUtil): Padrão de pesquisa é nulo ou vazio.");
                knowledgeBase = new JSONArray();
            } else if (jsonText.startsWith("[")) {
                knowledgeBase = new JSONArray(jsonText);
                System.out.println("Servidor C (ServerCSearchUtil): Base de conhecimento JSON (JSONArray) carregada. Registros: " + knowledgeBase.length());
            } else {
                System.err.println("Servidor C (ServerCSearchUtil): ERRO - O arquivo JSON não é um JSONArray válido. Conteúdo inicial: " + jsonText.substring(0, Math.min(jsonText.length(), 100)));
                knowledgeBase = new JSONArray();
            }
        } catch (IOException | org.json.JSONException e) {
            System.err.println("Servidor C (ServerCSearchUtil): Erro ao carregar ou parsear o arquivo JSON: " + e.getMessage());
            knowledgeBase = new JSONArray();
        }
    }

    /**
     * Performs a naive string search for the given pattern in the text.
     * 
     * @param text    the text to search in
     * @param pattern the pattern to search for
     * @return true if the pattern is found, false otherwise
     */
    private boolean naiveStringSearch(String text, String pattern) {
        if (text == null || pattern == null || text.isEmpty() || pattern.isEmpty()) {
            return false;
        }
        int n = text.length();
        int m = pattern.length();

        if (m > n) {
            return false;
        }

        // Direct character-by-character comparison (naive search)
        for (int i = 0; i <= n - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break;
                }
            }
            if (j == m) {
                return true; // Pattern found
            }
        }
        return false; // Pattern not found
    }

    /**
     * Finds all items in the knowledge base that match the given pattern.
     * 
     * @param clientMessagePattern the pattern to search for
     * @return a JSONArray of matching items
     */
    public JSONArray findPatternInKnowledgeBase(String clientMessagePattern) {
        JSONArray foundItems = new JSONArray();
        if (knowledgeBase == null || knowledgeBase.isEmpty()) {
            System.out.println("Servidor C (ServerCSearchUtil): Base de conhecimento (JSONArray) está vazia ou não carregada."); 
            return foundItems; // Retorna um JSONArray vazio
        }

        System.out.println("Servidor C (ServerCSearchUtil): Buscando por '" + clientMessagePattern + "' em " + knowledgeBase.length() + " registros.");

        String lowerClientMessagePattern = clientMessagePattern.toLowerCase();

        for (int i = 0; i < knowledgeBase.length(); i++) {
            try {
                JSONObject item = knowledgeBase.getJSONObject(i);
                String title = item.optString("title", "");
                String abstractText = item.optString("abstract", "");

                // Using naiveStringSearch for case-insensitive check
                boolean titleMatch = naiveStringSearch(title.toLowerCase(), lowerClientMessagePattern);
                boolean abstractMatch = naiveStringSearch(abstractText.toLowerCase(), lowerClientMessagePattern);

                if (titleMatch || abstractMatch) {
                    JSONObject matchDetail = new JSONObject();
                    matchDetail.put("title", title);
                    matchDetail.put("abstract", abstractText);
                    foundItems.put(matchDetail);
                    // Log matches Server B's format, using index 'i'
                    System.out.println("Servidor C (ServerCSearchUtil): Padrão encontrado no item " + i + " (Título: " + title + "). Adicionado ao resultado.");
                }
            } catch (org.json.JSONException e) {
                System.err.println("Servidor C (ServerCSearchUtil): Erro ao processar item do JSONArray na posição " + i + ": " + e.getMessage());
                // Continua para o próximo item
            }
        }

        if (foundItems.isEmpty()) {
            System.out.println("Servidor C (JsonSearchUtil): Nenhum item encontrado para o padrão '" + clientMessagePattern + "'.");
        }
        return foundItems;
    }
}

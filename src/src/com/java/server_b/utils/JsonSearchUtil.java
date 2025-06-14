package server_b.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonSearchUtil {

    private static final String JSON_FILE_PATH = "/server_b/data/dados_servidor_b.json";
    private JSONArray knowledgeBase;

    public JsonSearchUtil() {
        loadKnowledgeBase();
    }

    private void loadKnowledgeBase() {
        try (InputStream inputStream = JsonSearchUtil.class.getResourceAsStream(JSON_FILE_PATH)) {
            if (inputStream == null) {
                System.err.println("Servidor B (JsonSearchUtil): ERRO CRÍTICO - Não foi possível encontrar o arquivo JSON: " + JSON_FILE_PATH + ". Verifique o caminho e se o arquivo está no classpath.");
                knowledgeBase = new JSONArray(); 
                return;
            }
            // Lê todo o stream para uma String para inspecionar seu início
            String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();

            if (jsonText.isEmpty()) {
                System.err.println("Servidor B (JsonSearchUtil): ERRO - O arquivo JSON está vazio.");
                knowledgeBase = new JSONArray(); // Fallback
            } else if (jsonText.startsWith("{")) {
                try {
                                    // Este caso idealmente não deveria acontecer se o arquivo for um array, conforme esperado pelo novo design.
                // No entanto, se for um objeto, não podemos atribuir diretamente ao JSONArray. Registra o erro e usa um array vazio.
                System.err.println("Servidor B (JsonSearchUtil): ERRO - O arquivo JSON é um JSONObject, mas um JSONArray é esperado. Conteúdo inicial: " + jsonText.substring(0, Math.min(jsonText.length(), 200)));
                knowledgeBase = new JSONArray(); // Fallback
                } catch (org.json.JSONException e) {
                    System.err.println("Servidor B (JsonSearchUtil): Erro ao parsear JSONObject: " + e.getMessage() + ". Conteúdo inicial (até 200 chars): " + jsonText.substring(0, Math.min(jsonText.length(), 200)));
                    knowledgeBase = new JSONArray(); // Fallback
                }
            } else if (jsonText.startsWith("[")) {
                // O arquivo é um array JSON, processa como JSONArray.
                                try {
                    knowledgeBase = new JSONArray(jsonText);
                    System.out.println("Servidor B (JsonSearchUtil): Base de conhecimento JSON (JSONArray) carregada com sucesso. Número de registros: " + knowledgeBase.length());
                } catch (org.json.JSONException e) {
                    System.err.println("Servidor B (JsonSearchUtil): Erro ao parsear JSONArray: " + e.getMessage() + ". Conteúdo inicial (até 200 chars): " + jsonText.substring(0, Math.min(jsonText.length(), 200)));
                    knowledgeBase = new JSONArray(); // Fallback
                }
            } else {
                // O arquivo não é um objeto ou array JSON válido em seu início.
                System.err.println("Servidor B (JsonSearchUtil): ERRO - O arquivo JSON não começa com '{' ou '['. Conteúdo inicial (até 200 chars): " + jsonText.substring(0, Math.min(jsonText.length(), 200)));
                knowledgeBase = new JSONArray(); // Fallback
            }
        } catch (IOException ioEx) {
            System.err.println("Servidor B (JsonSearchUtil): Erro de I/O ao ler o arquivo JSON: " + ioEx.getMessage());
            knowledgeBase = new JSONArray(); // Fallback
        } catch (org.json.JSONException jsonEx) {
            // Este catch específico para JSONException durante o new JSONObject(tokener) inicial pode ser substituído pela verificação da string,
            // mas foi mantido por segurança ou caso o uso direto do tokener seja restabelecido em outro lugar para JSONObject.
            System.err.println("Servidor B (JsonSearchUtil): Erro ao parsear o arquivo JSON (JSONException): " + jsonEx.getMessage());
            knowledgeBase = new JSONArray(); // Fallback
        } catch (Exception e) {
            System.err.println("Servidor B (JsonSearchUtil): Erro ao carregar ou parsear o arquivo JSON: " + e.getMessage());
            e.printStackTrace();
            knowledgeBase = new JSONArray(); 
        }
    }

    private boolean naiveStringSearch(String text, String pattern) {
        if (text == null || pattern == null || text.isEmpty() || pattern.isEmpty()) {
            return false;
        }
        int n = text.length();
        int m = pattern.length();

        if (m > n) {
            return false;
        }

        for (int i = 0; i <= n - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break;
                }
            }
            if (j == m) {
                return true;
            }
        }
        return false;
    }

    public JSONArray findMessagePatternInKnowledgeBase(String clientMessagePattern) {
        JSONArray foundItems = new JSONArray();
        if (knowledgeBase == null || knowledgeBase.isEmpty()) {
            System.out.println("Servidor B (JsonSearchUtil): Base de conhecimento (JSONArray) está vazia ou não carregada.");
            return foundItems; // Retorna um JSONArray vazio
        }

        System.out.println("Servidor B (JsonSearchUtil): Buscando por '" + clientMessagePattern + "' em " + knowledgeBase.length() + " registros.");

        for (int i = 0; i < knowledgeBase.length(); i++) {
            try {
                JSONObject item = knowledgeBase.getJSONObject(i);
                String title = item.optString("title", "");
                String abstractText = item.optString("abstract", "");
                String label = item.optString("label", ""); // Embora não usado na busca, pode ser útil para o resultado

                // Usando naiveStringSearch para verificar se o padrão está no título ou no resumo
                boolean titleMatch = naiveStringSearch(title.toLowerCase(), clientMessagePattern.toLowerCase());
                boolean abstractMatch = naiveStringSearch(abstractText.toLowerCase(), clientMessagePattern.toLowerCase());

                if (titleMatch || abstractMatch) {
                    JSONObject matchDetail = new JSONObject();
                    matchDetail.put("label", label);
                    matchDetail.put("title", title);
                    matchDetail.put("abstract", abstractText); // Add abstract to the response
                    foundItems.put(matchDetail);
                    System.out.println("Servidor B (JsonSearchUtil): Padrão encontrado no item " + i + " (Título: " + title + "). Adicionado ao resultado.");
                }
            } catch (org.json.JSONException e) {
                System.err.println("Servidor B (JsonSearchUtil): Erro ao processar item do JSONArray na posição " + i + ": " + e.getMessage());
                // Continua para o próximo item
            }
        }

        if (foundItems.isEmpty()) {
            System.out.println("Servidor B (JsonSearchUtil): Nenhum item encontrado para o padrão '" + clientMessagePattern + "'.");
        }
        return foundItems;
    }
}

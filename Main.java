package server_b;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import server_b.utils.JsonSearchUtil; // Importa a classe utilitária
import org.json.JSONArray;

public class Main {
    public static void main(String[] args) {
        int port = 4002; // Porta para o Servidor B
        System.out.println("Servidor B (server_b.Main) iniciado na porta " + port);

        // Instancia o JsonSearchUtil uma vez, para carregar a base de conhecimento
        JsonSearchUtil jsonSearchUtil = new JsonSearchUtil();

        try (ServerSocket serverSocketB = new ServerSocket(port)) {
            while (true) { // Mantém o Servidor B em execução para aceitar múltiplas mensagens
                try (Socket clientConnection = serverSocketB.accept(); // Esta será a conexão do Servidor A
                     InputStreamReader isr = new InputStreamReader(clientConnection.getInputStream());
                     BufferedReader reader = new BufferedReader(isr);
                     PrintWriter writer = new PrintWriter(clientConnection.getOutputStream(), true)) {

                    String clientHostAddress = clientConnection.getInetAddress().getHostAddress();
                    System.out.println("Servidor B: Conexão aceita de: " + clientHostAddress);
                    
                    String receivedMessage = reader.readLine();
                    if (receivedMessage != null) {
                        System.out.println("Servidor B - Conteúdo da mensagem recebida: \"" + receivedMessage + "\"");

                        // Procura o padrão da mensagem na base de conhecimento JSON
                        JSONArray foundItemsArray = jsonSearchUtil.findMessagePatternInKnowledgeBase(receivedMessage); // Busca na base de conhecimento

                        String responseToA;
                        if (foundItemsArray.isEmpty()) {
                            responseToA = "MSG_RECEBIDA_POR_B: '" + receivedMessage + "'. Nenhum padrão correspondente encontrado.";
                        } else {
                            // Envia o JSONArray como uma string.
                            // Server A precisará parsear esta string de volta para um JSONArray ou encaminhar como está.
                            responseToA = "MSG_RECEBIDA_POR_B: '" + receivedMessage + "'. JSON_DATA: " + foundItemsArray.toString();
                        }
                        writer.println(responseToA); // Envia a resposta formatada para o Servidor A
                    } else {
                        System.out.println("Servidor B: Nenhuma mensagem recebida ou cliente (" + clientHostAddress + ") desconectou prematuramente.");
                    }
                } catch (IOException e) {
                    System.err.println("Servidor B: Erro de I/O ao comunicar com o cliente conectado: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Servidor B: Não foi possível iniciar o servidor na porta " + port + ". Erro: " + e.getMessage());
        }
    }
}

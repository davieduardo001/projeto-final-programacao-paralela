package server_c;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import server_c.utils.ServerCSearchUtil;
import org.json.JSONArray;

public class Main {
    public static void main(String[] args) {
        int port = 4003; 
        System.out.println("Servidor C (server_c.Main) iniciado na porta " + port);

        ServerCSearchUtil kmpSearchUtil = new ServerCSearchUtil();

        try (ServerSocket serverSocketC = new ServerSocket(port)) {
            while (true) { // Mantém o Servidor C em execução para aceitar múltiplas mensagens
                try (Socket clientConnection = serverSocketC.accept(); // Esta será a conexão do Servidor B
                     InputStreamReader isr = new InputStreamReader(clientConnection.getInputStream());
                     BufferedReader reader = new BufferedReader(isr);
                     PrintWriter writer = new PrintWriter(clientConnection.getOutputStream(), true)) {

                    String clientHostAddress = clientConnection.getInetAddress().getHostAddress();
                    System.out.println("Servidor C: Conexão aceita de: " + clientHostAddress);
                    
                    String receivedMessage = reader.readLine();
                    if (receivedMessage != null) {
                        System.out.println("Servidor C - Conteúdo da mensagem recebida: \"" + receivedMessage + "\"");

                        // Procura o padrão da mensagem na base de conhecimento JSON usando KMP
                        JSONArray foundItemsArray = kmpSearchUtil.findPatternInKnowledgeBase(receivedMessage);

                        // Constrói a resposta para o Servidor A
                        String responseToA;
                        if (foundItemsArray != null && !foundItemsArray.isEmpty()) {
                            responseToA = "JSON_DATA_C:" + foundItemsArray.toString();
                        } else {
                            responseToA = "JSON_DATA_C:[]"; // Envia um array JSON vazio se nada for encontrado
                        }
                        System.out.println("Servidor C - Enviando para Servidor A: " + responseToA);
                        writer.println(responseToA);
                    } else {
                        System.out.println("Servidor C: Nenhuma mensagem recebida ou cliente (" + clientHostAddress + ") desconectou prematuramente.");
                    }
                } catch (IOException e) {
                    System.err.println("Servidor C: Erro de I/O ao comunicar com o cliente conectado: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Servidor C: Não foi possível iniciar o servidor na porta " + port + ". Erro: " + e.getMessage());
        }
    }
}

package server_a.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerBConnector {

    private String serverBHost = "localhost";
    private int serverBPort = 4002; // Porta do Servidor B

    /**
     * Conecta-se ao Servidor B, envia uma mensagem e retorna a resposta do Servidor B.
     * @param messageToSend A mensagem a ser enviada para o Servidor B.
     * @return A resposta recebida do Servidor B, ou uma mensagem de erro em caso de falha.
     */
    public String forwardMessageToServerB(String messageToSend) {
        System.out.println("Servidor A (ServerBConnector): Tentando conectar ao Servidor B em " + serverBHost + ":" + serverBPort);
        try (Socket socketToServerB = new Socket(serverBHost, serverBPort);
             PrintWriter writerToServerB = new PrintWriter(socketToServerB.getOutputStream(), true);
             BufferedReader readerFromServerB = new BufferedReader(new InputStreamReader(socketToServerB.getInputStream()))) {

            System.out.println("Servidor A (ServerBConnector): Conectado ao Servidor B.");
            
            // Envia a mensagem para o Servidor B
            writerToServerB.println(messageToSend);
            System.out.println("Servidor A (ServerBConnector): Mensagem ('" + messageToSend + "') enviada para o Servidor B.");

            // Lê a resposta do Servidor B
            String responseFromServerB = readerFromServerB.readLine();
            if (responseFromServerB != null) {
                System.out.println("Servidor A (ServerBConnector): Resposta recebida do Servidor B: \"" + responseFromServerB + "\"");
            } else {
                System.out.println("Servidor A (ServerBConnector): Nenhuma resposta recebida do Servidor B ou Servidor B desconectou.");
                responseFromServerB = "ERRO_RESPOSTA_B: Nenhuma resposta do Servidor B.";
            }
            return responseFromServerB;

        } catch (UnknownHostException e) {
            System.err.println("Servidor A (ServerBConnector): Host desconhecido ao tentar conectar ao Servidor B - " + serverBHost + ". Erro: " + e.getMessage());
            return "ERRO_CONEXAO_B: Host desconhecido " + serverBHost;
        } catch (IOException e) {
            System.err.println("Servidor A (ServerBConnector): Erro de I/O ao comunicar com o Servidor B (" + serverBHost + ":" + serverBPort + "). Erro: " + e.getMessage());
            return "ERRO_IO_B: " + e.getMessage();
        }
    }
}

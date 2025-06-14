package server_a.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerCConnector {

    private String serverCHost = "localhost";
    private int serverCPort = 4003; // Porta do Servidor C

    /**
     * Conecta-se ao Servidor C, envia uma mensagem e retorna a resposta do Servidor C.
     * @param messageToSend A mensagem a ser enviada para o Servidor C.
     * @return A resposta recebida do Servidor C, ou uma mensagem de erro em caso de falha.
     */
    public String sendMessageToServerC(String messageToSend) {
        System.out.println("Servidor A (ServerCConnector): Tentando conectar ao Servidor C em " + serverCHost + ":" + serverCPort);
        try (Socket socketToServerC = new Socket(serverCHost, serverCPort);
             PrintWriter writerToServerC = new PrintWriter(socketToServerC.getOutputStream(), true);
             BufferedReader readerFromServerC = new BufferedReader(new InputStreamReader(socketToServerC.getInputStream()))) {

            System.out.println("Servidor A (ServerCConnector): Conectado ao Servidor C.");
            
            // Envia a mensagem para o Servidor C
            writerToServerC.println(messageToSend);
            System.out.println("Servidor A (ServerCConnector): Mensagem ('" + messageToSend + "') enviada para o Servidor C.");

            // Lê a resposta do Servidor C
            String responseFromServerC = readerFromServerC.readLine();
            if (responseFromServerC != null) {
                System.out.println("Servidor A (ServerCConnector): Resposta recebida do Servidor C: \"" + responseFromServerC + "\"");
            } else {
                System.out.println("Servidor A (ServerCConnector): Nenhuma resposta recebida do Servidor C ou Servidor C desconectou.");
                responseFromServerC = "ERRO_RESPOSTA_C: Nenhuma resposta do Servidor C.";
            }
            return responseFromServerC;

        } catch (UnknownHostException e) {
            System.err.println("Servidor A (ServerCConnector): Host desconhecido ao tentar conectar ao Servidor C - " + serverCHost + ". Erro: " + e.getMessage());
            return "ERRO_CONEXAO_C: Host desconhecido " + serverCHost;
        } catch (IOException e) {
            System.err.println("Servidor A (ServerCConnector): Erro de I/O ao comunicar com o Servidor C (" + serverCHost + ":" + serverCPort + "). Erro: " + e.getMessage());
            return "ERRO_IO_C: " + e.getMessage();
        }
    }
}

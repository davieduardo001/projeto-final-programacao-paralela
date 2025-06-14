package server_a;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import server_a.utils.ServerBConnector;
import server_a.utils.ServerCConnector;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;


public class Main {
    public static void main(String[] args) {
        int port = 3001;
        System.out.println("Servidor A (server_a.Main) multithread iniciado na porta " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aguardando conexões de clientes...");

            while (true) { // Loop para aceitar múltiplas conexões
                try {
                    Socket clientSocket = serverSocket.accept(); // Bloqueia até uma conexão ser feita
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                    
                    // Cria uma nova thread para lidar com este cliente
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão do cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor na porta " + port + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado no servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Servidor finalizando.");
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
             BufferedReader reader = new BufferedReader(isr);
             OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
             BufferedWriter writerToClient = new BufferedWriter(osw)) {

            // Lê uma linha de texto enviada pelo cliente.
            // Esta linha é tratada como a mensagem a ser encaminhada.
            String receivedMessage = reader.readLine();

            if (receivedMessage != null) {
                System.out.println("Thread " + Thread.currentThread().getId() + ": Mensagem recebida de " + clientSocket.getInetAddress().getHostAddress() + ": \"" + receivedMessage + "\"");
                System.out.println("Thread " + Thread.currentThread().getId() + ": Encaminhando mensagem para o Servidor B...");
                ServerBConnector bConnector = new ServerBConnector();
                String rawResponseFromServerB = bConnector.forwardMessageToServerB(receivedMessage);
                System.out.println("Thread " + Thread.currentThread().getId() + ": Resposta Bruta do Servidor B: '" + rawResponseFromServerB + "'");

                String processedResponseB = processServerBResponse(rawResponseFromServerB);
                System.out.println("Thread " + Thread.currentThread().getId() + ": Resposta Processada do Servidor B (para cliente): '" + processedResponseB + "'");

                System.out.println("Thread " + Thread.currentThread().getId() + ": Encaminhando mesma mensagem para o Servidor C...");
                ServerCConnector cConnector = new ServerCConnector();
                String responseFromServerC = cConnector.sendMessageToServerC(receivedMessage);
                System.out.println("Thread " + Thread.currentThread().getId() + ": Resposta do Servidor C: '" + responseFromServerC + "'");

                // Combina as respostas e envia para o cliente original
                String combinedResponse = processedResponseB + "||END_OF_B_DATA||" + responseFromServerC;
                // Adicionando cor amarela para a resposta
                String finalResponseToClient = "\u001B[33m" + combinedResponse + "\u001B[0m";
                writerToClient.write(finalResponseToClient);
                writerToClient.newLine();
                writerToClient.flush();
                System.out.println("Thread " + Thread.currentThread().getId() + ": Resposta combinada ('" + combinedResponse + "') enviada para " + clientSocket.getInetAddress().getHostAddress());
            } else {
                System.out.println("Thread " + Thread.currentThread().getId() + ": Nenhuma mensagem recebida de " + clientSocket.getInetAddress().getHostAddress() + " ou o cliente desconectou.");
            }

        } catch (IOException e) {
            System.err.println("Thread " + Thread.currentThread().getId() + ": Erro de I/O no ClientHandler com " + clientSocket.getInetAddress().getHostAddress() + ": " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Thread " + Thread.currentThread().getId() + ": Erro ao fechar o socket do cliente " + clientSocket.getInetAddress().getHostAddress() + ": " + e.getMessage());
            }
            System.out.println("Thread " + Thread.currentThread().getId() + ": Conexão com " + clientSocket.getInetAddress().getHostAddress() + " encerrada.");
        }
    }

    private String processServerBResponse(String rawResponse) {
        final String jsonPrefix = "JSON_DATA:";
        int jsonStartIndex = rawResponse.indexOf(jsonPrefix);
        if (jsonStartIndex != -1) {
            String jsonDataString = rawResponse.substring(jsonStartIndex + jsonPrefix.length()).trim();
            // Prefix the raw JSON data and forward it to the client
            return "JSON_DATA_B:" + jsonDataString;
        }
        // Return a prefixed empty JSON array if no valid data is found
        return "JSON_DATA_B:[]";
    }
}

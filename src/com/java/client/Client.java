package com.java.client; 

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 3001;
        String messageToSend;

        if (args.length > 0) {
            messageToSend = String.join(" ", args); // Concatena todos os argumentos com espaço
        } else {
            messageToSend = "Olá do Cliente Java!";
            System.out.println("Nenhuma mensagem fornecida como argumento. Usando mensagem padrão: \"" + messageToSend + "\"");
        }

        System.out.println("Tentando conectar a " + hostname + ":" + port + " para enviar a mensagem: \"" + messageToSend + "\"");

        try (Socket socket = new Socket(hostname, port);
             OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
             BufferedWriter writer = new BufferedWriter(osw);
             InputStreamReader isr = new InputStreamReader(socket.getInputStream());
             BufferedReader readerFromServer = new BufferedReader(isr)) {

            System.out.println("Conectado ao servidor.");

            writer.write(messageToSend);
            writer.newLine(); // O servidor espera uma linha (readLine)
            writer.flush();   // Garante que a mensagem seja enviada

            System.out.println("Mensagem enviada para o servidor.");

            // Lê a resposta do servidor
            String serverResponse = readerFromServer.readLine();
            if (serverResponse != null) {
                System.out.println("Resposta Recebida do Servidor A: " + serverResponse);
                processAndSaveDataFromServerA(serverResponse, messageToSend);
            } else {
                System.out.println("Nenhuma resposta recebida do servidor.");
            }

        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        } finally {
            System.out.println("Cliente finalizando.");
        }
    }

        private static void processAndSaveDataFromServerA(String serverResponse, String originalQuery) {
        String cleanResponse = serverResponse.replaceAll("\\u001B\\[[;\\d]*m", "");

        String separator = "||END_OF_B_DATA||";
        String[] parts = cleanResponse.split(Pattern.quote(separator), 2);

        String serverBData = (parts.length > 0) ? parts[0].trim() : "";
        String serverCData = (parts.length > 1) ? parts[1].trim() : "";

        // Process Server B Data (JSON to CSV)
        String jsonDataMarkerB = "JSON_DATA_B:";
        if (serverBData.startsWith(jsonDataMarkerB)) {
            String rawJsonPayloadB = serverBData.substring(jsonDataMarkerB.length());
            processJsonAndSaveAsCsv(rawJsonPayloadB, originalQuery, "server_b");
        } else {
            System.out.println("Resposta do Servidor B não reconhecida ou não continha dados JSON válidos: " + serverBData);
        }

        // Process Server C Data (JSON to CSV)
        String jsonDataMarkerC = "JSON_DATA_C:";
        if (serverCData.startsWith(jsonDataMarkerC)) {
            String rawJsonPayloadC = serverCData.substring(jsonDataMarkerC.length());
            processJsonAndSaveAsCsv(rawJsonPayloadC, originalQuery, "server_c");
        } else {
            System.out.println("Resposta do Servidor C não reconhecida ou não continha dados JSON válidos: " + serverCData);
        }
    }

    private static void processJsonAndSaveAsCsv(String rawJsonPayload, String originalQuery, String serverIdentifier) {
        if (rawJsonPayload == null || rawJsonPayload.trim().isEmpty() || rawJsonPayload.trim().equals("[]")) {
            System.out.println("Dados JSON do " + serverIdentifier + " estavam vazios. Nenhum arquivo salvo.");
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(rawJsonPayload);
            if (jsonArray.length() == 0) {
                System.out.println("Dados JSON do " + serverIdentifier + " resultaram em um array vazio. Nenhum arquivo salvo.");
                return;
            }

            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("\"Title\",\"Abstract\"\n"); // CSV Header

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String title = jsonObject.optString("title", "N/A").replace("\"", "\"\"");
                String abstractText = jsonObject.optString("abstract", "N/A").replace("\"", "\"\"");
                
                csvBuilder.append("\"").append(title).append("\",\"");
                csvBuilder.append(abstractText).append("\"\n");
            }
            
            saveCsvStringToFile(csvBuilder.toString(), originalQuery, serverIdentifier);

        } catch (JSONException e) {
            System.err.println("Erro ao fazer parse do JSON do " + serverIdentifier + ": " + e.getMessage());
            System.err.println("JSON Data que causou o erro: " + rawJsonPayload);
        }
    }

    private static void saveCsvStringToFile(String csvData, String originalQuery, String serverIdentifier) {
        try {
            Path resultsDir = Paths.get("client_results");
            if (!Files.exists(resultsDir)) {
                Files.createDirectories(resultsDir);
                System.out.println("Diretório 'client_results' criado.");
            }

            String sanitizedQuery = originalQuery.replaceAll("[^a-zA-Z0-9_.-]", "_").substring(0, Math.min(originalQuery.length(), 30));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "results_" + serverIdentifier + "_" + timestamp + "_query_" + sanitizedQuery + ".csv";
            Path filePath = resultsDir.resolve(fileName);

            Files.write(filePath, csvData.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Dados CSV de " + serverIdentifier + " salvos em: " + filePath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Erro ao salvar resultados CSV no arquivo: " + e.getMessage());
        }
    }
}

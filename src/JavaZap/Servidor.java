package JavaZap;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class Servidor {
    private int port;
    private HashMap<String, Socket> clientes; // Armazena as conexões dos clientes
    private HashMap<String, String> nomesClientes; // Armazena o nome de cada cliente

    public Servidor() throws IOException {
        this.port = ConfigConexao.SERVER_PORT; // Usando ConfigConexao
        clientes = new HashMap<>();
        nomesClientes = new HashMap<>();
        iniciar();
    }

    private void iniciar() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor multithread iniciado na porta: " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

            // Recebe o identificador do cliente
            Scanner input = new Scanner(clientSocket.getInputStream());
            String clientId = input.nextLine(); // Lê o identificador do cliente

            // Recebe o nome do cliente
            String clienteName = input.nextLine(); // Lê o nome do cliente

            // Adiciona o cliente à HashMap
            clientes.put(clientId, clientSocket);
            nomesClientes.put(clientId, clienteName);

            // Cria uma nova thread para cada cliente
            new Thread(() -> {
                try {
                    Scanner inputClient = new Scanner(clientSocket.getInputStream());
                    PrintStream output = new PrintStream(clientSocket.getOutputStream());

                    while (true) {
                        if (inputClient.hasNextLine()) {
                            String message = inputClient.nextLine();
                            System.out.println("Cliente " + clientId + ": " + message);

                            // Envia a mensagem para todos os clientes conectados
                            for (String id : clientes.keySet()) {
                                if (!id.equals(clientId)) { // Não envia para o próprio cliente
                                    Socket targetSocket = clientes.get(id);
                                    PrintStream targetOutput = new PrintStream(targetSocket.getOutputStream());

                                    // Obtem data e hora atuais
                                    Date now = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    String currentDateTime = formatter.format(now);

                                    // Envia a mensagem com o nome do cliente e data/hora
                                    // **REPARE QUE O ID DO CLIENTE FOI REMOVIDO DA MENSAGEM**
                                    targetOutput.println(nomesClientes.get(clientId) + ": " + currentDateTime + ": " + message);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Erro na comunicação com o cliente " + clientId + ": " + e.getMessage());
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        System.err.println("Erro ao fechar a conexão com o cliente " + clientId + ": " + e.getMessage());
                    }
                    // Remove o cliente da HashMap
                    clientes.remove(clientId);
                    nomesClientes.remove(clientId);
                }
            }).start();
        }
    }

    // public static void main(String[] args) throws IOException {
    //     new Servidor(12000);
    // }
}
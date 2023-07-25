package server;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import server.server.ClientHandler;

public class server {
    private static Map<String, String> key_value_store = new HashMap<>();  // Dicionário para armazenar os pares chave-valor
    private static final String LEADER_IP = "127.0.0.1";
    private static final int LEADER_PORT = 10097;
    private String server_IP;
    private int server_Port;
    private boolean isMaster;
    private static ServerSocket serverSocket;

    public server(String server_IP, int server_Port) throws IOException {
        this.server_IP = server_IP;
        this.server_Port = server_Port;
        this.isMaster = this.server_IP.equals(this.LEADER_IP) && this.server_Port == this.LEADER_PORT;
        this.serverSocket = new ServerSocket(this.server_Port);
        this.key_value_store = new HashMap<>();
        
        System.out.println("Servidor iniciado em IP: " + this.server_IP + " Porta: " + this.server_Port);
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            while (true) {
                try {
                    //Aceita a mensagem do client...
                    Socket clientSocket = serverSocket.accept();
                    Thread thread = new Thread(() -> serverListen(clientSocket));
                    thread.start();

                    //Aloca um espaço na memória para receber e enviar mensagens do Client
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    //Recebe as mensagens do client...
                    String request = reader.readLine();
                    String[] requestParts = request.split(" ");
                    
                    //Valida se a mensagem é um PUT
                    if (requestParts[0].equals("PUT")) {

                        // Se o server for o LEADER, então ele deve:
                        //if (clientSocket.getInetAddress().getHostAddress().equals(LEADER_IP) && clientSocket.getPort() == LEADER_PORT) {
                        if (requestParts[6].equals(LEADER_IP) && requestParts[7].equals(LEADER_PORT)) {
                            String key = requestParts[1];
                            String value = requestParts[2];
                            String timestamp_client = requestParts[3];
                            String client_IP = requestParts[4];
                            String client_Port = requestParts[5];
                            synchronized (key_value_store) {
                                key_value_store.put(key, value + ", " + timestamp_client + ", " + client_IP + ", " + client_Port);
                            }

                            //Replica os dados para os demais servidores...

                            //Response que o server dá para o client
                            writer.write("PUT_OK\n");

                        } else {
                            //Response que o server dá para o client
                            writer.write("PUT_ERROR: Only the leader server can perform PUT operations.\n");
                        }

                    } else if (requestParts[0].equals("GET")) {
                        String key = requestParts[1];
                        String value;
                        synchronized (key_value_store) {
                            value = key_value_store.getOrDefault(key, "Key not found");
                        }
                        //Response que o server dá para o client
                        writer.write(value + "\n");
                    }

                    writer.flush();
                    clientSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server listening on port 5000");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
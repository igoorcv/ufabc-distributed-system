package serverGPT;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import client.client.Message;

public class serverGPT {
    private Map<String, Item> key_value_store = new HashMap<>();
    //private HashMap<String, HashMap<String, Object>> key_value_store;
    private static final String LEADER_IP = "127.0.0.1";
    private static final String SLAVE_IP_1 = "127.0.0.1";
    private static final String SLAVE_IP_2 = "127.0.0.1";
    private static final int LEADER_PORT = 10097;
    private static final int SLAVE_PORT_1 = 10098;
    private static final int SLAVE_PORT_2 = 10099;
    private String server_IP;
    private int server_Port;
    private static boolean isMaster;
    private static ServerSocket serverSocket;

    public serverGPT(String server_IP, int server_Port) throws IOException {
        this.server_IP = server_IP;
        this.server_Port = server_Port;
        serverGPT.isMaster = this.server_IP.equals(LEADER_IP) && this.server_Port == LEADER_PORT;
        //serverGPT.serverSocket = new ServerSocket(this.server_Port, 50, InetAddress.getByName(this.server_IP));
        this.key_value_store = new HashMap<>();

        System.out.println("Servidor iniciado em IP: " + this.server_IP + " Porta: " + this.server_Port);
    }
    
    public class Item {
        private String valueItem;
        private long timestampItem;

        public Item(String valueItem, long timestampItem) {
            this.valueItem = valueItem;
            this.timestampItem = timestampItem;
        }

        public String getValueItem() {
            return valueItem;
        }

        public long getTimestampItem() {
            return timestampItem;
        }
    }

    static class ClientHandler implements Runnable {
        private Map<String, Item> key_value_store; // Referência para o dicionário do servidor

        public ClientHandler(Socket clientSocket, Map<String, Item> key_value_store) {
            this.key_value_store = key_value_store;
        }

        private String GET_SERVER_PRINT(InetAddress client_IP, int client_Port, String key, String value, long timestamp, long itemTimestamp) {
            return "[GET] Client IP: " + client_IP.getHostAddress() + ", Client Port: " + client_Port + ", Key: " + key +
                    ", Value: " + value + ", Request Timestamp: " + timestamp + ", Item Timestamp: " + itemTimestamp;
        }

        private String PUT_SERVER_MASTER_PRINT(InetAddress client_IP, int client_Port, String key, String value) {
            HashMap<String, Object> item = key_value_store.get(key);
            key_value_store.put(key, Item);
            return "[PUT] Leader Server (Master) - Client IP: " + client_IP.getHostAddress() + ", Client Port: " + client_Port +
                    ", Key: " + key + ", Value: " + value;
        }

        private String REPLICATION_SERVER_MASTER_PRINT(InetAddress client_IP, int client_Port, String key, long timestamp) {
            return "[REPLICATION] Leader Server (Master) - Client IP: " + client_IP.getHostAddress() + ", Client Port: " + client_Port +
                    ", Key: " + key + ", Timestamp: " + timestamp;
        }

        private String PUT_SERVER_SLAVE_PRINT(String key, String value) {
            return "[PUT] Slave Server - Key: " + key + ", Value: " + value;
        }

        private String REPLICATION_SERVER_SLAVE_PRINT(String key, String value, long timestamp) {
            return "[REPLICATION] Slave Server - Key: " + key + ", Value: " + value + ", Timestamp: " + timestamp;
        }

        private String PUT_SERVER_MASTER_ERROR_PRINT(InetAddress clientIP, int clientPort, String key) {
            return "[REPLICATION] Slave Server Error - Key: " + key + ", ClientIP: " + clientIP + ", ClientPort: " + clientPort;
        }

        private String ERROR() { 
            return null;
        }

        private void serverListen(Socket clientSocket) {
            try {
                // Cria um ObjectInputStream para ler os dados do cliente
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                // Lê o objeto Message enviado pelo cliente
                Message receivedMessage = (Message) objectInputStream.readObject();

                // Acessar os campos da mensagem recebida. Pego os métodos do client.Message
                String type = receivedMessage.getType();
                String key = receivedMessage.getKey();
                String value = receivedMessage.getValue();
                String valueItem = Item.getValueItem();
                long timestamp = receivedMessage.getTimestamp();
                //long timestampItem = Item.getTimestampItem();
                InetAddress client_IP = receivedMessage.getClientIP();
                int client_Port = receivedMessage.getClientPort();
                String server_IP = receivedMessage.getServerIP();
                int server_Port = receivedMessage.getServerPort();
            

                // Verifica se a requisição é do tipo GET
                if (type.equals("GET")) {
                    // Obtém o item com a chave especificada na requisição
                    Item item = key_value_store.get(key);

                    // Caso o item não exista
                    if (item == null) {
                        // Imprime informações sobre a requisição GET
                        System.out.println(
                                GET_SERVER_PRINT(
                                        client_IP,
                                        client_Port,
                                        key,
                                        null,
                                        timestamp,
                                        timestampItem
                                )
                        );

                        // Cria uma mensagem de resposta do tipo GET_NULL
                        Message response = new Message(
                                "GET_NULL",
                                key,
                                value,
                                timestamp,
                                client_IP,
                                client_Port,
                                server_IP,
                                server_Port
                        );

                        // Envia a resposta ao cliente
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();

                    } else if (timestampItem >= timestamp) {
                        // Imprime informações sobre a requisição GET
                        System.out.println(
                                GET_SERVER_PRINT(
                                        client_IP,
                                        client_Port,
                                        key,
                                        valueItem,
                                        timestamp,
                                        timestampItem
                                )
                        );

                        // Cria uma mensagem de resposta do tipo GET_OK com o valor e timestamp do item
                        Message response = new Message(
                                "GET_OK",
                                key,
                                valueItem,
                                timestampItem,
                                client_IP,
                                client_Port,
                                server_IP,
                                server_Port
                        );

                        // Envia a resposta ao cliente
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();
                    } else {
                        // Imprime informações sobre a requisição GET com uma mensagem de erro
                        System.out.println(
                                GET_SERVER_PRINT(
                                        client_IP,
                                        client_Port,
                                        key,
                                        ERROR(),
                                        timestamp,
                                        timestampItem
                                )
                        );

                        // Cria uma mensagem de resposta do tipo ERROR com uma mensagem de erro
                        Message response = new Message(
                                "ERROR",
                                key,
                                ERROR(),
                                timestamp,
                                client_IP,
                                client_Port,
                                server_IP,
                                server_Port
                        );

                        // Envia a resposta ao cliente
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        objectOutputStream.writeObject(response);
                        objectOutputStream.flush();
                    }

                    // Fecha a conexão com o cliente
                    clientSocket.close();

                    // Verifica se a requisição é do tipo PUT
                } else if (type.equals("PUT")) {
                    if (isMaster) { // Caso este servidor seja o LEADER
                        // Imprime no console do Server as informações sobre a requisição PUT
                        System.out.println(
                                PUT_SERVER_MASTER_PRINT(
                                        client_IP, 
                                        client_Port, 
                                        key, 
                                        value
                                )
                        );

                        // Obtém o item com a chave especificada na requisição
                        Item item = key_value_store.get(key);

                        if (item == null) { // Se o item do PUT não existe no server, então:
                            // Define o timestamp como 0
                            int ts = 0;

                            // Adiciona o item ao dicionário de arquivos com o valor e timestamp especificados
                            key_value_store.put(key, (Item) new serverGPT.Item(value, ts));

                            // Cria uma mensagem de REPLICATION
                            Message replication = new Message(
                                    "REPLICATION",
                                    key,
                                    value,
                                    ts,
                                    client_IP,
                                    client_Port,
                                    server_IP,
                                    server_Port
                            );

                            // Conecta ao SERVER 1 escravo e envia a mensagem de REPLICATION
                            Socket s1 = new Socket(SLAVE_IP_1, SLAVE_PORT_1);
                            ObjectOutputStream out1 = new ObjectOutputStream(s1.getOutputStream());
                            out1.writeObject(replication);
                            out1.flush();
                            s1.close();

                            // Conecta ao SERVER 2 escravo e envia a mensagem de REPLICATION
                            Socket s2 = new Socket(SLAVE_IP_2, SLAVE_PORT_2);
                            ObjectOutputStream out2 = new ObjectOutputStream(s2.getOutputStream());
                            out2.writeObject(replication);
                            out2.flush();
                            s2.close();

                            // Imprime informações sobre a REPLICATION
                            System.out.println(
                                    REPLICATION_SERVER_MASTER_PRINT(
                                            client_IP,
                                            client_Port,
                                            key,
                                            timestamp
                                    )
                            );

                            // Cria uma response do tipo PUT_OK
                            Message response = new Message(
                                    "PUT_OK",
                                    key,
                                    value,
                                    ts,
                                    client_IP,
                                    client_Port,
                                    server_IP,
                                    server_Port
                            );

                            // Envia a response ao Client.java
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            objectOutputStream.writeObject(response);
                            objectOutputStream.flush();

                            // Caso o timestamp do item seja igual ao timestamp da requisição
                        } else if (timestampItem <= timestamp) { // Se ts do PUT é maior do que o ts do item, então:

                            // Atualiza o item no dicionário com o novo Value e Timestamp
                            key_value_store.put(key, (Item) new serverGPT.Item(value, timestamp + 1));

                            // Cria uma mensagem de REPLICATION
                            Message replication = new Message(
                                    "REPLICATION",
                                    key,
                                    value,
                                    timestamp + 1,
                                    client_IP,
                                    client_Port,
                                    server_IP,
                                    server_Port
                            );

                            // Conecta ao SERVER 1 escravo e envia a mensagem de REPLICATION
                            Socket s1 = new Socket(SLAVE_IP_1, SLAVE_PORT_1);
                            ObjectOutputStream out1 = new ObjectOutputStream(s1.getOutputStream());
                            out1.writeObject(replication);
                            out1.flush();
                            s1.close();

                            // Conecta ao SERVER 2 escravo e envia a mensagem de REPLICATION
                            Socket s2 = new Socket(SLAVE_IP_2, SLAVE_PORT_2);
                            ObjectOutputStream out2 = new ObjectOutputStream(s2.getOutputStream());
                            out2.writeObject(replication);
                            out2.flush();
                            s2.close();

                            // Imprime informações sobre a REPLICATION
                            System.out.println(
                                    REPLICATION_SERVER_MASTER_PRINT(
                                            client_IP,
                                            client_Port,
                                            key,
                                            timestamp + 1
                                    )
                            );

                            // Cria um response do tipo PUT_OK
                            Message response = new Message(
                                    "PUT_OK",
                                    key,
                                    value,
                                    timestamp + 1,
                                    client_IP,
                                    client_Port,
                                    server_IP,
                                    server_Port
                            );

                            // Envia a response ao Client.java
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            objectOutputStream.writeObject(response);
                            objectOutputStream.flush();

                            // Caso o timestamp do item seja maior que o timestamp da requisição
                        } else if (timestampItem > timestamp) {// Se o ts do PUT é menor do que o ts do item, então:
                            // Imprime informações sobre a requisição PUT
                            System.out.println(
                                    PUT_SERVER_MASTER_ERROR_PRINT(
                                            client_IP,
                                            client_Port,
                                            key
                                    )
                            );

                            // Cria um response tipo PUT_ERROR
                            Message response = new Message(
                                    "PUT_ERROR",
                                    receivedMessage.getKey(),
                                    valueItem,
                                    timestampItem,
                                    client_IP,
                                    client_Port,
                                    server_IP,
                                    server_Port
                            );

                            // Envia a resposta ao Client.java
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            objectOutputStream.writeObject(response);
                            objectOutputStream.flush();

                        } else { // Se o item do PUT existe no server, então:
                            // Atualiza o item no dicionário de arquivos com o valor e timestamp especificados
                            key_value_store.put(key, (Item) new serverGPT.Item(value, timestamp));

                            // Cria uma mensagem de replicação com os dados da requisição PUT
                            Message replication = new Message(
                                    "REPLICATION",
                                    key,
                                    value,
                                    timestamp,
                                    client_IP,
                                    client_Port,
                                    server_IP,
                                    server_Port
                            );

                            // Conecta ao primeiro servidor escravo e envia a mensagem de replicação
                            Socket s1 = new Socket(SLAVE_IP_1, SLAVE_PORT_1);
                            ObjectOutputStream out1 = new ObjectOutputStream(s1.getOutputStream());
                            out1.writeObject(replication);
                            out1.flush();
                            s1.close();

                            // Conecta ao segundo servidor escravo e envia a mensagem de replicação
                            Socket s2 = new Socket(SLAVE_IP_2, SLAVE_PORT_2);
                            ObjectOutputStream out2 = new ObjectOutputStream(s2.getOutputStream());
                            out2.writeObject(replication);
                            out2.flush();
                            s2.close();

                            // Imprime informações sobre a replicação
                            System.out.println(
                                    REPLICATION_SERVER_MASTER_PRINT(
                                        client_IP,
                                        client_Port,
                                        key,
                                        timestamp
                                    )
                            );

                            // Cria uma mensagem de resposta do tipo PUT_OK com os dados da requisição PUT
                            Message response = new Message(
                                    "PUT_OK",
                                    key,
                                    value,
                                    timestamp,
                                    client_IP,
                                    client_Port,
                                    server_IP,
                                    server_Port
                            );

                            // Envia a resposta ao cliente
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            objectOutputStream.writeObject(response);
                            objectOutputStream.flush();
                        }
                    } else { // Caso este servidor não seja o LEADER
                        // Imprime informações sobre a requisição PUT
                        System.out.println(PUT_SERVER_SLAVE_PRINT(key, value));
                        // Conecta ao servidor líder (master) e envia a requisição PUT recebida
                        Socket s = new Socket(LEADER_IP, LEADER_PORT);
                        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                        out.writeObject(receivedMessage);
                        out.flush();

                        // Recebe a resposta do servidor líder (master)
                        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                        Message response = (Message) in.readObject();
                        s.close();

                        // Verifica se a resposta é do tipo PUT_OK
                        if (response.getType().equals("PUT_OK")) {
                            // Cria uma mensagem de resposta do tipo PUT_OK com os dados da resposta do servidor líder (master)
                            Message putResponse = new Message(
                                    "PUT_OK",
                                    response.getKey(),
                                    response.getValue(),
                                    response.getTimestamp(),
                                    response.getClientIP(),
                                    response.getClientPort(),
                                    server_IP,
                                    server_Port
                            );

                            // Envia a resposta ao cliente
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            objectOutputStream.writeObject(putResponse);
                            objectOutputStream.flush();
                        }
                    }

                    // Fecha a conexão com o cliente
                    clientSocket.close();

                    // Verifica se a requisição é do tipo REPLICATION
                } else if (type.equals("REPLICATION")) {
                    // Imprime informações sobre a replicação
                    System.out.println(
                            REPLICATION_SERVER_SLAVE_PRINT(
                                    key, value, timestamp
                            )
                    );

                    // Adiciona o item ao dicionário de arquivos com o valor e timestamp especificados
                    key_value_store.put(key, (Item) new serverGPT.Item(value, timestamp));

                    // Cria uma mensagem de resposta do tipo REPLICATION_OK com os dados da requisição REPLICATION
                    Message response = new Message(
                            "REPLICATION_OK",
                            key,
                            value,
                            timestamp,
                            client_IP,
                            client_Port,
                            server_IP,
                            server_Port
                    );

                    // Envia a resposta ao cliente
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();

                    // Feche o socket do cliente após processar a requisição
                    clientSocket.close();

                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Internal server error: " + e.getMessage());
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void run() {
            while (true) {
                try {
                    // Aceita a mensagem do client...
                    Socket clientSocket = serverSocket.accept();

                    // Cria uma nova Thread para lidar com o Client
                    Thread thread = new Thread(() -> serverListen(clientSocket));
                    thread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            
            serverGPT server = new serverGPT("127.0.0.1", 10097);
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket, server.key_value_store)).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
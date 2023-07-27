package exServer;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import client.client.Message;
import exServer.exServer.ClientHandler;

public class exServer {
    private static Map<String, String> key_value_store = new HashMap<>();  // Dicionário para armazenar os pares chave-valor
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

    public exServer(String server_IP, int server_Port) throws IOException {
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
            long timestamp = receivedMessage.getTimestamp();
            InetAddress client_IP = receivedMessage.getClientIP();
            Integer client_Port = receivedMessage.getClientPort();
            String server_IP = receivedMessage.getServerIP();
            Integer server_Port = receivedMessage.getServerPort();

            // Verifica se a requisição é do tipo GET
            if (receivedMessage.getType().equals("GET")) {
                
                // Obtém o item com a chave especificada na requisição
                Item item = files.get(receivedMessage.getKey());

                // Caso o item não exista
                if (item == null) {
                    // Imprime informações sobre a requisição GET
                    System.out.println(
                        GET_SERVER_PRINT(
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            receivedMessage.getKey(),
                            "NULL",
                            receivedMessage.getTimestamp(),
                            0
                        )
                    );

                    // Cria uma mensagem de resposta do tipo GET_NULL
                    Message response = new Message(
                        "GET_NULL",
                        receivedMessage.getKey(),
                        null,
                        null,
                        receivedMessage.getClientIP(),
                        receivedMessage.getClientPort(),
                        this.IP,
                        this.port
                    );

                    // Envia a resposta ao cliente
                    Message responseMessage = new Message("GET_OK", key, value, timestamp, client_IP, server_Port, server_IP, client_Port);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    objectOutputStream.writeObject(responseMessage);
                    objectOutputStream.flush();

                } else if (item.getTimestamp() >= request.getTimestamp()) {
                    // Imprime informações sobre a requisição GET
                    System.out.println(
                        GET_SERVER_PRINT(
                            request.getcIP(),
                            request.getcPort(),
                            request.getKey(),
                            item.getValue(),
                            request.getTimestamp(),
                            item.getTimestamp()
                        )
                    );

                    // Cria uma mensagem de resposta do tipo GET_OK com o valor e timestamp do item
                    Message response = new Message(
                        "GET_OK",
                        request.getKey(),
                        item.getValue(),
                        item.getTimestamp(),
                        request.getcIP(),
                        request.getcPort(),
                        this.IP,
                        this.port
                    );

                    // Envia a resposta ao cliente
                    PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                    out.println(codifyMessage(response));
                } else {
                    // Imprime informações sobre a requisição GET com uma mensagem de erro
                    System.out.println(
                        GET_SERVER_PRINT(
                            request.getcIP(),
                            request.getcPort(),
                            request.getKey(),
                            ERROR(),
                            request.getTimestamp(),
                            item.getTimestamp()
                        )
                    );

                    // Cria uma mensagem de resposta do tipo ERROR com uma mensagem de erro
                    Message response = new Message(
                        "ERROR",
                        request.getKey(),
                        ERROR(),
                        null,
                        request.getcIP(),
                        request.getcPort(),
                        this.IP,
                        this.port
                    );

                    // Envia a resposta ao cliente
                    PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                    out.println(codifyMessage(response));
                }

                // Fecha a conexão com o cliente
                c.close();

            // Verifica se a requisição é do tipo PUT
            } else if (receivedMessage.getType().equals("PUT")) {
                
                if (isMaster = true) { // Caso este servidor seja o LEADER
                   
                    // Imprime no console do Sever as informações sobre a requisição PUT
                    System.out.println(
                        PUT_SERVER_MASTER_PRINT(
                            receivedMessage.getClientIP(), receivedMessage.getClientPort(), receivedMessage.getKey(), receivedMessage.getValue()
                        )
                    );

                    // Obtém o item com a chave especificada na requisição
                    String item = key_value_store.get(receivedMessage.getKey());

                    if (item == null) { // Se o item do PUT não existe no server, então:
                        // Define o timestamp como 0
                        int ts = 0;

                        // Adiciona o item ao dicionário de arquivos com o valor e timestamp especificados
                        key_value_store.put(receivedMessage.getKey(), receivedMessage.getValue(), ts);

                        // Cria uma mensagem de REPLICATION
                        Message replication = new Message(
                            "REPLICATION",
                            receivedMessage.getKey(),
                            receivedMessage.getValue(),
                            ts,
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            this.IP,
                            this.port
                        );

                        // Conecta ao SERVER 1 escravo e envia a mensagem de REPLICATION
                        Socket s1 = new Socket(SLAVE_IP_1, SLAVE_PORT_1);
                        PrintWriter out1 = new PrintWriter(s1.getOutputStream(), true);
                        out1.println(codifyMessage(replication));
                        BufferedReader in1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
                        String jsonStrReceived1 = in1.readLine();
                        s1.close();

                        // Conecta ao SERVER 2 escravo e envia a mensagem de REPLICATION
                        Socket s2 = new Socket(SLAVE_IP_2, SLAVE_PORT_2);
                        PrintWriter out2 = new PrintWriter(s2.getOutputStream(), true);
                        out2.println(codifyMessage(replication));
                        BufferedReader in2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
                        String jsonStrReceived2 = in2.readLine();
                        s2.close();

                        // Imprime informações sobre a REPLICATION
                        System.out.println(
                            REPLICATION_SERVER_MASTER_PRINT(
                                receivedMessage.getClientIP(),
                                receivedMessage.getClientPort(),
                                receivedMessage.getKey(),
                                receivedMessage.getTimestamp()
                            )
                        );

                        // Cria uma response do tipo PUT_OK
                        Message response = new Message(
                            "PUT_OK",
                            receivedMessage.getKey(),
                            receivedMessage.getValue(),
                            ts,
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            this.IP,
                            this.port
                        );

                        // Envia a response ao Client.java
                        PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                        out.println(codifyMessage(response));

                    // Caso o timestamp do item seja igual ao timestamp da requisição
                    } else if (item.getTimestamp() <= receivedMessage.getTimestamp()) { // Se ts do PUT é maior do que o ts do item, então:
                        
                        // Atualiza o item no dicionário com o novo Value e Timestamp
                        key_value_store.put(receivedMessage.getKey(), new Item(receivedMessage.getValue(), receivedMessage.getTimestamp() + 1));

                        // Cria uma mensagem de REPLICATION
                        Message replication = new Message(
                            "REPLICATION",
                            receivedMessage.getKey(),
                            receivedMessage.getValue(),
                            receivedMessage.getTimestamp() + 1,
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            this.IP,
                            this.port
                        );

                        // Conecta ao SERVER 1 escravo e envia a mensagem de REPLICATION
                        Socket s1 = new Socket(SLAVE_IP_1, SLAVE_PORT_1);
                        PrintWriter out1 = new PrintWriter(s1.getOutputStream(), true);
                        out1.println(codifyMessage(replication));
                        BufferedReader in1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
                        String jsonStrReceived1 = in1.readLine();
                        s1.close();

                        // Conecta ao SERVER 2 escravo e envia a mensagem de REPLICATION
                        Socket s2 = new Socket(SLAVE_IP_2, SLAVE_PORT_2);
                        PrintWriter out2 = new PrintWriter(s2.getOutputStream(), true);
                        out2.println(codifyMessage(replication));
                        BufferedReader in2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
                        String jsonStrReceived2 = in2.readLine();
                        s2.close();

                        // Imprime informações sobre a REPLICATION
                        System.out.println(
                            REPLICATION_SERVER_MASTER_PRINT(
                                receivedMessage.getClientIP(),
                                receivedMessage.getClientPort(),
                                receivedMessage.getKey(),
                                receivedMessage.getTimestamp() + 1
                            )
                        );

                        // Cria um response do tipo PUT_OK
                        Message response = new Message(
                            "PUT_OK",
                            receivedMessage.getKey(),
                            receivedMessage.getValue(),
                            receivedMessage.getTimestamp() + 1,
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            this.IP,
                            this.port
                        );

                        // Envia a response ao Client.java
                        PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                        out.println(codifyMessage(response));

                    // Caso o timestamp do item seja maior que o timestamp da requisição
                    } else if (item.getTimestamp() > receivedMessage.getTimestamp()) {// Se o ts do PUT é menor do que o ts do item, então:
                        // Imprime informações sobre a requisição PUT
                        System.out.println(
                            PUT_SERVER_MASTER_ERROR_PRINT(
                                receivedMessage.getClientIP(),
                                receivedMessage.getClientPort(),
                                receivedMessage.getKey()
                            )
                        );

                        // Cria um response tipo PUT_ERROR
                        Message response = new Message(
                            "PUT_ERROR",
                            receivedMessage.getKey(),
                            item.getValue(),
                            item.getTimestamp(),
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            this.IP,
                            this.port
                        );

                        // Envia a resposta ao Client.java
                        PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                        out.println(codifyMessage(response));

                    } else { //Se o item do PUT existe no server, então:
                        // Atualiza o item no dicionário de arquivos com o valor e timestamp especificados
                        key_value_store.put(receivedMessage.getKey(), new Item(receivedMessage.getValue(), receivedMessage.getTimestamp()));

                        // Cria uma mensagem de replicação com os dados da requisição PUT
                        Message replication = new Message(
                            "REPLICATION",
                            receivedMessage.getKey(),
                            receivedMessage.getValue(),
                            receivedMessage.getTimestamp(),
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            this.IP,
                            this.port
                        );

                        // Conecta ao primeiro servidor escravo e envia a mensagem de replicação
                        Socket s1 = new Socket(SLAVE_IP_1, SLAVE_PORT_1);
                        PrintWriter out1 = new PrintWriter(s1.getOutputStream(), true);
                        out1.println(codifyMessage(replication));
                        BufferedReader in1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
                        String jsonStrReceived1 = in1.readLine();
                        s1.close();

                        // Conecta ao segundo servidor escravo e envia a mensagem de replicação
                        Socket s2 = new Socket(SLAVE_IP_2, SLAVE_PORT_2);
                        PrintWriter out2 = new PrintWriter(s2.getOutputStream(), true);
                        out2.println(codifyMessage(replication));
                        BufferedReader in2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
                        String jsonStrReceived2 = in2.readLine();
                        s2.close();

                        // Imprime informações sobre a replicação
                        System.out.println(
                            REPLICATION_SERVER_MASTER_PRINT(
                                receivedMessage.getClientIP(),
                                receivedMessage.getClientPort(),
                                receivedMessage.getKey(),
                                receivedMessage.getTimestamp()
                            )
                        );

                        // Cria uma mensagem de resposta do tipo PUT_OK com os dados da requisição PUT
                        Message response = new Message(
                            "PUT_OK",
                            receivedMessage.getKey(),
                            receivedMessage.getValue(),
                            receivedMessage.getTimestamp(),
                            receivedMessage.getClientIP(),
                            receivedMessage.getClientPort(),
                            this.IP,
                            this.port
                        );

                        // Envia a resposta ao cliente
                        PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                        out.println(codifyMessage(response));
                    }
                } else { // Caso este servidor não seja o LEADER
                    // Imprime informações sobre a requisição PUT
                    System.out.println(PUT_SERVER_SLAVE_PRINT(receivedMessage.getKey(), receivedMessage.getValue()));
                    // Conecta ao servidor líder (master) e envia a requisição PUT recebida
                    Socket s = new Socket(LEADER_IP, LEADER_PORT);
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println(codifyMessage(receivedMessage));
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String jsonStrReceived = in.readLine();
                    s.close();

                    // Decodifica a resposta do servidor líder (master)
                    Message response = decodifyMessage(jsonStrReceived);

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
                            this.IP,
                            this.port
                        );

                        // Envia a resposta ao cliente
                        PrintWriter outClient = new PrintWriter(c.getOutputStream(), true);
                        outClient.println(codifyMessage(putResponse));
                    }
                }

                // Fecha a conexão com o cliente
                c.close();

            // Verifica se a requisição é do tipo REPLICATION
            } else if (receivedMessage.getType().equals("REPLICATION")) {
                // Imprime informações sobre a replicação
                System.out.println(
                    REPLICATION_SERVER_SLAVE_PRINT(
                        request.getKey(), request.getValue(), request.getTimestamp()
                    )
                );

                // Adiciona o item ao dicionário de arquivos com o valor e timestamp especificados
                files.put(request.getKey(), new Item(request.getValue(), request.getTimestamp()));

                // Cria uma mensagem de resposta do tipo REPLICATION_OK com os dados da requisição REPLICATION
                Message response = new Message(
                    "REPLICATION_OK",
                    request.getKey(),
                    request.getValue(),
                    request.getTimestamp(),
                    request.getcIP(),
                    request.getcPort(),
                    this.IP,
                    this.port
                );

                // Envia a resposta ao cliente
                PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                out.println(codifyMessage(response));

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
                    //Aceita a mensagem do client...
                    Socket clientSocket = serverSocket.accept();

                    //Cria uma nova Thread para lidar com o Client
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
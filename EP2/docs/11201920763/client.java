package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class client {
    private String IP;
    private int port;
    private String[][] servers;
    private ServerSocket s;
    private Map<Integer, Map<String, Object>> files = new HashMap<>();

    // Método construtor da classe Client
    public client(String IP, int port, String s1_IP, int s1_port, String s2_IP, int s2_port, String s3_IP, int s3_port) {
        this.IP = IP;
        this.port = port;

        // Armazena os IPs e portas dos servidores em uma matriz
        this.servers = new String[][] {
            { s1_IP, String.valueOf(s1_port) },
            { s2_IP, String.valueOf(s2_port) },
            { s3_IP, String.valueOf(s3_port) }
        };

        try {
            // Cria um socket e vincula-o ao endereço IP e porta fornecidos
            s = new ServerSocket(port, 0, InetAddress.getByName(IP));
            // Obtém a porta real em que o socket está ouvindo e armazena no atributo port
            this.port = s.getLocalPort();
            // Imprime informações sobre o cliente iniciado
            System.out.println("Client started at IP: " + IP + " Port: " + port);
        } catch (IOException e) {
            System.out.println("Error starting the client: " + e.getMessage());
            System.exit(1);
        }
    }

    // Método para obter um servidor aleatório da lista de servidores
    private String[] getRandomServer() {
        Random rand = new Random();
        return servers[rand.nextInt(servers.length)];
    }

    // Método para baixar um arquivo do servidor
    public void get(int key) {
        try {
            String[] server = getRandomServer();
            Socket s = new Socket(server[0], Integer.parseInt(server[1]));

            int ts = 0;
            Map<String, Object> item = files.get(key);

            if (item != null) {
                ts = (int) item.get("timestamp");
            }

            // Cria o objeto que será enviado ao servidor
            Map<String, Object> message = new HashMap<>();
            message.put("type", "GET");
            message.put("key", key);
            message.put("timestamp", ts);
            message.put("c_IP", IP);
            message.put("c_port", port);
            message.put("s_IP", server[0]);
            message.put("s_port", Integer.parseInt(server[1]));

            // Envia a mensagem ao servidor
            sendMessage(s, message);

            // Recebe a resposta do servidor
            Map<String, Object> response = receiveMessage(s);

            if ("GET_OK".equals(response.get("type"))) {
                int newTs = (int) response.get("timestamp");
                String value = (String) response.get("value");
                // Atualiza a estrutura de dados com os dados recebidos do servidor
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("value", value);
                itemMap.put("timestamp", newTs);
                files.put(key, itemMap);
                System.out.println(GET_CLIENT_PRINT(server[0], Integer.parseInt(server[1]), key, value, ts, newTs));
            } else if ("GET_NULL".equals(response.get("type"))) {
                System.out.println(GET_NULL_CLIENT_PRINT(key));
            } else if ("ERROR".equals(response.get("type"))) {
                System.out.println(response.get("value"));
            } else {
                System.out.println("Error to communicate with server");
            }

            s.close();
        } catch (IOException e) {
            System.out.println("Internal error: " + e.getMessage());
            try {
                s.close();
            } catch (IOException ex) {
                System.out.println("Error closing the socket: " + ex.getMessage());
            }
            System.exit(1);
        }
    }

    // Método para registrar um arquivo no servidor
    public void put(int key, String value) {
        try {
            String[] server = getRandomServer();
            Socket s = new Socket(server[0], Integer.parseInt(server[1]));

            int ts = 0;
            Map<String, Object> item = files.get(key);

            if (item != null) {
                ts = (int) item.get("timestamp");
            }

            // Cria o objeto que será enviado ao servidor
            Map<String, Object> message = new HashMap<>();
            message.put("type", "PUT");
            message.put("key", key);
            message.put("value", value);
            message.put("timestamp", ts);
            message.put("c_IP", IP);
            message.put("c_port", port);
            message.put("s_IP", server[0]);
            message.put("s_port", Integer.parseInt(server[1]));

            // Envia a mensagem ao servidor
            sendMessage(s, message);

            // Recebe a resposta do servidor
            Map<String, Object> response = receiveMessage(s);

            if ("PUT_OK".equals(response.get("type"))) {
                int newTs = (int) response.get("timestamp");
                // Atualiza a estrutura de dados com os dados recebidos do servidor

                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("value", value);
                itemMap.put("timestamp", newTs);
                files.put(key, itemMap);

                System.out.println(PUT_CLIENT_PRINT(key, value, newTs, server[0], Integer.parseInt(server[1])));
            } else if ("PUT_ERROR".equals(response.get("type"))) {
                System.out.println(PUT_CLIENT_PRINT_ERROR(key, value, ts, server[0], Integer.parseInt(server[1]), (int) response.get("timestamp")));
            } else {
                System.out.println("Error to communicate with server");
            }

            s.close();
        } catch (IOException e) {
            System.out.println("Internal error: " + e.getMessage());
            try {
                s.close();
            } catch (IOException ex) {
                System.out.println("Error closing the socket: " + ex.getMessage());
            }
            System.exit(1);
        }
    }

    // Método para enviar uma mensagem ao servidor
    private void sendMessage(Socket socket, Map<String, Object> message) throws IOException {
        String messageStr = mapToString(message);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(messageStr.getBytes());
    }

    // Método para receber uma mensagem do servidor
    private Map<String, Object> receiveMessage(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        String messageStr = new String(buffer, 0, bytesRead);
        return stringToMap(messageStr);
    }

    // Método para converter um Map em uma String
    private String mapToString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (String key : map.keySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"" + key + "\":");
            Object value = map.get(key);
            if (value instanceof String) {
                sb.append("\"" + value + "\"");
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // Método para converter uma String em um Map
    private Map<String, Object> stringToMap(String str) {
        Map<String, Object> map = new HashMap<>();
        int idx = 1;
        String key = "", value = "";
        while (idx < str.length()) {
            if (str.charAt(idx) == '\"') {
                int endIdx = str.indexOf('\"', idx + 1);
                if (endIdx > idx) {
                    if (key.isEmpty()) {
                        key = str.substring(idx + 1, endIdx);
                    } else {
                        value = str.substring(idx + 1, endIdx);
                        map.put(key, value);
                        key = value = "";
                    }
                    idx = endIdx + 2;
                }
            } else {
                int endIdx = str.indexOf(',', idx);
                if (endIdx == -1) {
                    endIdx = str.length() - 1;
                }
                int val = Integer.parseInt(str.substring(idx, endIdx));
                if (key.isEmpty()) {
                    key = String.valueOf(val);
                } else {
                    map.put(key, val);
                    key = "";
                }
                idx = endIdx + 1;
            }
        }
        return map;
    }

    // Método para executar o cliente
    public void run() {
        Scanner scanner = new Scanner(System.in);
        // Entra em um loop onde o usuário pode escolher entre diferentes ações relacionadas ao cliente
        while (true) {
            // Solicita ao usuário que insira a opção desejada:
            // "PUT" para registrar um arquivo no servidor
            // "GET" para baixar um arquivo do servidor
            System.out.println("Choose an option: PUT or GET");
            String choice = scanner.nextLine();

            // Com base na opção escolhida, são solicitados os parâmetros relevantes e os métodos apropriados do cliente são chamados
            if ("PUT".equalsIgnoreCase(choice)) {
                System.out.println("Enter the key to register:");
                int key = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter the value to register:");
                String value = scanner.nextLine();
                // Chama o método put do cliente com a chave e o valor fornecidos
                put(key, value);
            } else if ("GET".equalsIgnoreCase(choice)) {
                System.out.println("Enter the key to search:");
                int key = Integer.parseInt(scanner.nextLine());
                // Chama o método get do cliente com a chave fornecida
                get(key);
            } else {
                // Se uma opção inválida for fornecida, o loop é encerrado
                System.out.println("Ending process");
                scanner.close();
                System.exit(0);
            }
        }
    }

    // Restante do código...
    // Método para imprimir informações sobre uma requisição GET realizada pelo cliente
    public String GET_CLIENT_PRINT(String s_IP, int s_port, int key, String value, int c_timestamp, int s_timestamp) {
        return String.format("GET key:%d value:%s obtido do servidor %s:%d, meu timestamp %d e do servidor %d.",
                key, value, s_IP, s_port, c_timestamp, s_timestamp);
    }

    // Método para imprimir informações sobre uma requisição PUT realizada pelo cliente
    public String PUT_CLIENT_PRINT(int key, String value, int s_timestamp, String s_IP, int s_port) {
        return String.format("PUT_OK key:%d value:%s timestamp:%d realizada no servidor %s:%d.",
                key, value, s_timestamp, s_IP, s_port);
    }

    // Método para imprimir informações sobre uma requisição PUT que não foi realizada
    public String PUT_CLIENT_PRINT_ERROR(int key, String value, int c_timestamp, String s_IP, int s_port, int s_timestamp) {
        return String.format("PUT_ERROR key:%d value:%s timestamp:%d realizada no servidor %s:%d com timestamp: %d.",
                key, value, c_timestamp, s_IP, s_port, s_timestamp);
    }

    // Método para imprimir informações sobre uma requisição GET que não encontrou o item especificado
    public String GET_NULL_CLIENT_PRINT(int key) {
        return String.format("GET key:%d não encontrado.", key);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Solicita ao usuário para iniciar um cliente
        System.out.println("Type INIT to start a Client");
        String choice = scanner.nextLine();

        // Verifica se a escolha do usuário é para iniciar um cliente
        if ("INIT".equalsIgnoreCase(choice) || choice.isEmpty()) {
            // Solicita ao usuário para inserir o IP do cliente
            System.out.println("Enter the Client IP:");
            String c_IP = scanner.nextLine();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (c_IP.isEmpty()) {
                c_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do cliente
            int c_port = 0;
            while (true) {
                System.out.println("Enter the Client port:");
                String c_port_input = scanner.nextLine();
                // Se o usuário não inserir uma porta, o padrão será 0
                if (c_port_input.isEmpty()) {
                    break;
                }
                // Verifica se a porta inserida é um número inteiro
                if (c_port_input.matches("\\d+")) {
                    c_port = Integer.parseInt(c_port_input);
                    break;
                } else {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("The Client port should be either an integer or empty.");
                }
            }

            // Solicita ao usuário para inserir o IP do servidor 1
            System.out.println("Enter the Server 1 IP:");
            String s1_IP = scanner.nextLine();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (s1_IP.isEmpty()) {
                s1_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do servidor 1
            int s1_port = 10097;
            while (true) {
                System.out.println("Enter the Server 1 port:");
                String s1_port_input = scanner.nextLine();
                // Se o usuário não inserir uma porta, o padrão será 10097
                if (s1_port_input.isEmpty()) {
                    break;
                }
                // Verifica se a porta inserida é um número inteiro
                if (s1_port_input.matches("\\d+")) {
                    s1_port = Integer.parseInt(s1_port_input);
                    break;
                } else {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("The Server 1 port should be either an integer or empty.");
                }
            }

            // Solicita ao usuário para inserir o IP do servidor 2
            System.out.println("Enter the Server 2 IP:");
            String s2_IP = scanner.nextLine();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (s2_IP.isEmpty()) {
                s2_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do servidor 2
            int s2_port = 10098;
            while (true) {
                System.out.println("Enter the Server 2 port:");
                String s2_port_input = scanner.nextLine();
                // Se o usuário não inserir uma porta, o padrão será 10098
                if (s2_port_input.isEmpty()) {
                    break;
                }
                // Verifica se a porta inserida é um número inteiro
                if (s2_port_input.matches("\\d+")) {
                    s2_port = Integer.parseInt(s2_port_input);
                    break;
                } else {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("The Server 2 port should be either an integer or empty.");
                }
            }

            // Solicita ao usuário para inserir o IP do servidor 3
            System.out.println("Enter the Server 3 IP:");
            String s3_IP = scanner.nextLine();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (s3_IP.isEmpty()) {
                s3_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do servidor 3
            int s3_port = 10099;
            while (true) {
                System.out.println("Enter the Server 3 port:");
                String s3_port_input = scanner.nextLine();
                // Se o usuário não inserir uma porta, o padrão será 10099
                if (s3_port_input.isEmpty()) {
                    break;
                }
                // Verifica se a porta inserida é um número inteiro
                if (s3_port_input.matches("\\d+")) {
                    s3_port = Integer.parseInt(s3_port_input);
                    break;
                } else {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("The Server 3 port should be either an integer or empty.");
                }
            }

            // Cria uma instância da classe Client com os IPs e portas do cliente e servidores
            client client = new client(c_IP, c_port, s1_IP, s1_port, s2_IP, s2_port, s3_IP, s3_port);
            // Executa o método run da instância de Client criada acima
            client.run();
        } else {
            // Caso a escolha do usuário não seja para iniciar um cliente, encerra o processo.
            System.out.println("Ending process");
            System.exit(0);
        }
    }
}

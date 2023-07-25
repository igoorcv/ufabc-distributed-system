import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

class Message {
    // Lista de tipos de mensagens permitidos
    private static final List<String> ALLOWED_TYPES = Arrays.asList("GET", "GET_OK", "GET_NULL", "PUT", "PUT_OK", "PUT_ERROR", "REPLICATION", "REPLICATION_OK", "ERROR");

    private String type;
    private int key;
    private String value;
    private int timestamp;
    private String c_IP;
    private int c_port;
    private String s_IP;
    private int s_port;

    public Message(String type, int key, String value, int timestamp, String c_IP, int c_port, String s_IP, int s_port) {
        // Valida o tipo da mensagem e define o atributo type
        this.type = validateType(type);
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
        this.c_IP = c_IP;
        this.c_port = c_port;
        this.s_IP = s_IP;
        this.s_port = s_port;
    }

    public String getType() {
        return type;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getC_IP() {
        return c_IP;
    }

    public int getC_port() {
        return c_port;
    }

    public String getS_IP() {
        return s_IP;
    }

    public int getS_port() {
        return s_port;
    }

    // Método para validar o tipo da mensagem
    private static String validateType(String type) {
        // Verifica se o tipo fornecido está na lista de tipos permitidos
        if (!ALLOWED_TYPES.contains(type)) {
            // Caso contrário, lança uma exceção com uma mensagem de erro
            throw new IllegalArgumentException("Invalid message type. Allowed types: " + String.join(", ", ALLOWED_TYPES));
        }
        return type;
    }

}

class client {
    private String IP;
    private List<ServerInfo> servers;
    private ServerSocket serverSocket;
    private Map<Integer, Map<String, Object>> files;

    private class ServerInfo {
        String IP;
        int port;

        ServerInfo(String IP, int port) {
            this.IP = IP;
            this.port = port;
        }
    }

    public client(String IP, int port, String s1_IP, int s1_port, String s2_IP, int s2_port, String s3_IP, int s3_port) throws IOException {
        this.IP = IP;
        this.servers = new ArrayList<>();
        this.servers.add(new ServerInfo(s1_IP, s1_port));
        this.servers.add(new ServerInfo(s2_IP, s2_port));
        this.servers.add(new ServerInfo(s3_IP, s3_port));
        this.serverSocket = new ServerSocket(port);
        this.files = new HashMap<>();

        System.out.println("Client started at IP: " + this.IP + " Port: " + port);
    }

    public ServerInfo getRandomServer() {
        Random random = new Random();
        return servers.get(random.nextInt(servers.size()));
    }

    public void get(int key) {
        // Implementação do método get
        try {
            ServerInfo server = getRandomServer();
            Socket s = new Socket(server.IP, server.port);
            int ts = 0;
            Map<String, Object> item = files.get(key);
            if (item != null) {
                ts = (int) item.get("timestamp");
            }
            Message message = new Message("GET", key, null, ts, IP, serverSocket.getLocalPort(), server.IP, server.port);
            // Implemente o restante do método get
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void put(int key, String value) {
        // Implementação do método put
        try {
            ServerInfo server = getRandomServer();
            Socket s = new Socket(server.IP, server.port);
            int ts = 0;
            Map<String, Object> item = files.get(key);
            if (item != null) {
                ts = (int) item.get("timestamp");
            }
            Message message = new Message("PUT", key, value, ts, IP, serverSocket.getLocalPort(), server.IP, server.port);
            // Implemente o restante do método put
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(() -> serverListen(clientSocket));
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void serverListen(Socket clientSocket) {
        try {
            // Implementação do método serverListen
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Outros métodos auxiliares...
}

public class exClient {
    public static void main(String[] args) throws IOException {
        // Solicita ao usuário para iniciar um cliente
        System.out.println("Digite INIT para iniciar o cliente");
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim().toLowerCase();

        // Verifica se a escolha do usuário é para iniciar um cliente
        if (choice.equals("init") || choice.equals("")) {
            // Solicita ao usuário para inserir o IP do cliente
            System.out.println("Digite o IP do cliente:");
            String c_IP = scanner.nextLine().trim();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (c_IP.isEmpty()) {
                c_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do cliente
            int c_port;
            while (true) {
                System.out.println("Digite a porta do cliente:");
                String c_port_input = scanner.nextLine().trim();
                // Verifica se a porta inserida é um número inteiro
                try {
                    c_port = Integer.parseInt(c_port_input);
                    break;
                } catch (NumberFormatException ex) {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("A porta do cliente deve ser um número inteiro ou vazia.");
                }
            }

            // Solicita ao usuário para inserir o IP do servidor 1
            System.out.println("Digite o IP do servidor 1:");
            String s1_IP = scanner.nextLine().trim();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (s1_IP.isEmpty()) {
                s1_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do servidor 1
            int s1_port;
            while (true) {
                System.out.println("Digite a porta do servidor 1:");
                String s1_port_input = scanner.nextLine().trim();
                // Verifica se a porta inserida é um número inteiro
                try {
                    s1_port = Integer.parseInt(s1_port_input);
                    break;
                } catch (NumberFormatException ex) {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("A porta do servidor deve ser um número inteiro ou vazia.");
                }
            }

            // Solicita ao usuário para inserir o IP do servidor 2
            System.out.println("Digite o IP do servidor 2:");
            String s2_IP = scanner.nextLine().trim();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (s2_IP.isEmpty()) {
                s2_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do servidor 2
            int s2_port;
            while (true) {
                System.out.println("Digite a porta do servidor 2:");
                String s2_port_input = scanner.nextLine().trim();
                // Verifica se a porta inserida é um número inteiro
                try {
                    s2_port = Integer.parseInt(s2_port_input);
                    break;
                } catch (NumberFormatException ex) {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("A porta do servidor deve ser um número inteiro ou vazia.");
                }
            }

            // Solicita ao usuário para inserir o IP do servidor 3
            System.out.println("Digite o IP do servidor 3:");
            String s3_IP = scanner.nextLine().trim();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (s3_IP.isEmpty()) {
                s3_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do servidor 3
            int s3_port;
            while (true) {
                System.out.println("Digite a porta do servidor 3:");
                String s3_port_input = scanner.nextLine().trim();
                // Verifica se a porta inserida é um número inteiro
                try {
                    s3_port = Integer.parseInt(s3_port_input);
                    break;
                } catch (NumberFormatException ex) {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("A porta do servidor deve ser um número inteiro ou vazia.");
                }
            }

            // Cria uma instância da classe Client com os IPs e portas do cliente e servidores
            Client c = new Client(c_IP, c_port, s1_IP, s1_port, s2_IP, s2_port, s3_IP, s3_port);

            // Executa o método run da instância de Client criada acima
            c.run();
        } else {
            // Caso a escolha do usuário não seja para iniciar um cliente, encerra o processo.
            System.out.println("Encerrando o processo.");
            scanner.close();
            System.exit(0);
        }
    }
}
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class server123 {
    private String IP;
    private int port;
    private String masterIP;
    private int masterPort;
    private boolean isMaster;
    private ServerSocket serverSocket;
    private Map<String, Map<String, Object>> files;

    public server123(String IP, int port, String s1_IP, int s1_port, String s2_IP, int s2_port, String master_IP, int master_port) throws IOException {
        this.IP = IP;
        this.port = port;
        this.masterIP = master_IP;
        this.masterPort = master_port;
        this.isMaster = this.IP.equals(this.masterIP) && this.port == this.masterPort;
        this.serverSocket = new ServerSocket(this.port);
        this.files = new HashMap<>();
        
        System.out.println("Servidor iniciado em IP: " + this.IP + " Porta: " + this.port);
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
            // A implementação do método server_listen vai aqui...

            // Feche o socket do cliente após processar a requisição
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

    // Outros métodos para processar requisições, imprimir informações e tratar erros...
}

public class exServer {
    public static void main(String[] args) throws IOException {
        // Solicita ao usuário para iniciar um servidor
        System.out.println("Digite INIT para iniciar o servidor");
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim().toLowerCase();

        // Verifica se a escolha do usuário é para iniciar um servidor
        if (choice.equals("init") || choice.equals("")) {
            // Solicita ao usuário para inserir o IP do servidor
            System.out.println("Digite o IP do seu servidor:");
            String s_IP = scanner.nextLine().trim();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (s_IP.isEmpty()) {
                s_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do servidor
            int s_port;
            while (true) {
                System.out.println("Digite a porta do seu servidor:");
                String s_port_input = scanner.nextLine().trim();
                // Verifica se a porta inserida é um número inteiro
                try {
                    s_port = Integer.parseInt(s_port_input);
                    break;
                } catch (NumberFormatException ex) {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("A porta do servidor deve ser um número inteiro ou vazia.");
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

            // Solicita ao usuário para inserir o IP do líder (master)
            System.out.println("Digite o IP do líder (master):");
            String master_IP = scanner.nextLine().trim();
            // Se o usuário não inserir um IP, o padrão será 127.0.0.1
            if (master_IP.isEmpty()) {
                master_IP = "127.0.0.1";
            }

            // Solicita ao usuário para inserir a porta do líder (master)
            int master_port;
            while (true) {
                System.out.println("Digite a porta do líder (master):");
                String master_port_input = scanner.nextLine().trim();
                // Verifica se a porta inserida é um número inteiro
                try {
                    master_port = Integer.parseInt(master_port_input);
                    break;
                } catch (NumberFormatException ex) {
                    // Caso contrário, informa ao usuário que a porta deve ser um número inteiro ou vazia
                    System.out.println("A porta do servidor deve ser um número inteiro ou vazia.");
                }
            }

            // Cria uma instância da classe Server com os IPs e portas dos servidores e líder (master)
            server123 server = new server123(s_IP, s_port, s1_IP, s1_port, s2_IP, s2_port, master_IP, master_port);
            // Executa o método run da instância de Server criada acima
            server.run();
        } else {
            // Caso a escolha do usuário não seja para iniciar um servidor, encerra o processo.
            System.out.println("Encerrando o processo.");
        }
    }
}

package client;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class client {
    private static String[] serverIPs;
    private static int[] serverPorts;
    private static String client_IP;
    private static int client_Port;
    private static Random random = new Random();

 // Classe Message dentro da classe Client
    public static class Message implements Serializable {
        private String type;
        private String key;
        private String value;
        private long timestamp;
        private InetAddress client_IP;
        private int client_Port;
        private String server_IP;
        private int server_Port;

        public Message(String type, String key, String value, long timestamp, InetAddress client_IP, int client_Port, String server_IP, int server_Port) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.timestamp = timestamp;
            this.client_IP = client_IP;
            this.client_Port = client_Port;
            this.server_IP = server_IP;
            this.server_Port = server_Port;
        }

        public String getType() {
            return type;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public InetAddress getClientIP() {
            return client_IP;
        }
    
        public int getClientPort() {
            return client_Port;
        }
    
        public String getServerIP() {
            return server_IP;
        }
    
        public int getServerPort() {
            return server_Port;
        }
	}
	
	private static void initializeServers() {
        serverIPs = new String[]{"127.0.0.1", "127.0.0.1", "127.0.0.1"}; // Insira os IPs dos três servidores aqui
        serverPorts = new int[]{10097, 10098, 10099}; // Insira as portas dos três servidores aqui

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            client_IP = localHost.getHostAddress();
            client_Port = 8080;
            System.out.println("Client started at IP: " + client_IP + " Port: " + client_Port);

        } catch (UnknownHostException e) {
            System.err.println("Não foi possível obter o endereço IP do cliente.");
            e.printStackTrace();
        }
    }
	
	private static void sendPutRequest(String key, String value) {
        try {
            //Define o Server que será enviado...
            String server_IP = getRandomServerIP();
            int server_Port = getRandomServerPort();

            //Abre o socket de envio...
            Socket socket = new Socket(server_IP, server_Port);

            //Define timestamp, client_IP e client_Port
            long timestamp = System.currentTimeMillis();
            InetAddress client_IP = InetAddress.getLocalHost();
            client_Port = 8080;

            //Monta a mensagem que será enviada para o Server
            Message sendMessage = new Message("PUT", key, value, timestamp, client_IP, client_Port, server_IP, server_Port);

            //Empacota a mensagem como ObjectOutputStream e envia para o Server 
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(sendMessage);
            objectOutputStream.flush();

            //Desempacota a resposta do Server como ObjectInputStream
            String response = objectInputStream.readUTF();
            System.out.println("Server response: " + response);

            //Encerra contato com Server
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendGetRequest(String key) {
        try {
            //Define o Server que será enviado...
            String server_IP = getRandomServerIP();
            int server_Port = getRandomServerPort();

            //Abre o socket de envio...
            Socket socket = new Socket(server_IP, server_Port);

            //Define timestamp, client_IP e client_Port
            long timestamp = System.currentTimeMillis();
            InetAddress client_IP = InetAddress.getLocalHost();
            client_Port = 8080;
            
            //Monta a mensagem que será enviada para o Server
            Message sendMessage = new Message("GET", key, null, timestamp, client_IP, client_Port, server_IP, server_Port);

            //Empacota a mensagem como ObjectOutputStream e envia para o Server
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(sendMessage);
            objectOutputStream.flush();

            //Desempacota a resposta do Server como ObjectInputStream
            String response = objectInputStream.readUTF();
            System.out.println("Server response: " + response);

            //Encerra contato com Server
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRandomServerIP() {
        int randomIndex = random.nextInt(serverIPs.length);
        return serverIPs[randomIndex];
    }

    private static int getRandomServerPort() {
        int randomIndex = random.nextInt(serverPorts.length);
        return serverPorts[randomIndex];
    }

	public static void main(String[] args) {
        initializeServers();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Exibe o menu de opções
            System.out.println("\n--- Menu ---");
            System.out.println("1. INIT");
            System.out.println("2. PUT");
            System.out.println("3. GET");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            // Verifica se o próximo valor é um número inteiro
            if (scanner.hasNextInt()) {
                int option = scanner.nextInt();
                scanner.nextLine(); // Consumir a quebra de linha após o próximo valor

                switch (option) {
                    case 1:
                        initializeServers();
                        break;

                    case 2:
						System.out.print("---PUT---");
                        System.out.print("Key: ");
                        String key = scanner.nextLine();
                        System.out.print("Value: ");
                        String value = scanner.nextLine();
                        sendPutRequest(key, value);
                        break;

                    case 3:
						System.out.print("---GET---");
                        System.out.print("Key: ");
                        String searchKey = scanner.nextLine();
                        sendGetRequest(searchKey);
                        break;

                    case 0:
                        scanner.close();
                        System.exit(0);

                    default:
                        System.out.println("Opção inválida. Por favor, escolha novamente.");
                }

            } else {

				System.out.println("Entrada inválida. Por favor, digite um número inteiro.");
                scanner.nextLine(); // Consumir a entrada inválida
            }
        }
    }
}
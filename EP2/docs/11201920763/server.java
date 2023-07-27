package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import message.message;

public class server {
    private String IP;
    private int port;
    private String master_IP;
    private int master_port;
    private boolean isMaster;
    private ServerSocket serverSocket;
    private HashMap<String, HashMap<String, Object>> files;

    public server(String IP, int port, String s1_IP, int s1_port, String s2_IP, int s2_port, String master_IP, int master_port) {
        this.IP = IP;
        this.port = port;
        this.master_IP = master_IP;
        this.master_port = master_port;
        this.isMaster = this.IP.equals(master_IP) && this.port == master_port;
        this.files = new HashMap<>();

        try {
            serverSocket = new ServerSocket(this.port);
            System.out.println("Server started at IP: " + this.IP + " Port: " + this.port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        server_listen(clientSocket);
                    }
                });
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void server_listen(Socket clientSocket) {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);

            if (bytesRead > 0) {
                String json_str_received = new String(buffer, 0, bytesRead);
                message request = decodify_message(json_str_received);

                // Acessar os campos da mensagem recebida. Pego os métodos do client.Message
                String type = request.getType();
                String key = request.getKey();
                String value = request.getValue();
                //String valueItem = Item.getValueItem();
                long timestamp = request.getTimestamp();
                //long timestampItem = Item.getTimestampItem();
                String client_IP = request.getC_IP();
                int client_Port = request.getC_port();
                String server_IP = request.getServerIP();
                int server_Port = request.getServerPort();

                if (request.getType().equals("GET")) {
                    handle_GET_request(outputStream, request);
                } else if (request.getType().equals("PUT")) {
                    handle_PUT_request(outputStream, request);
                } else if (request.getType().equals("REPLICATION")) {
                    handle_REPLICATION_request(outputStream, request);
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle_GET_request(OutputStream outputStream, message request) throws IOException {
        String key = request.getKey();
        HashMap<String, Object> item = files.get(key);

        if (item == null) {
            String responseMessage = GET_SERVER_PRINT(request.getC_IP(), request.getC_port(), key, "NULL", request.getTimestamp(), 0);
            System.out.println(responseMessage);

            message response = new message("GET_NULL", key, null, (Long) null, request.getC_IP(), request.getC_port(), IP, port);
            outputStream.write(codify_message(response).getBytes());
        } else if ((long) item.get("timestamp") >= request.getTimestamp()) {
            String responseMessage = GET_SERVER_PRINT(request.getC_IP(), request.getC_port(), key, item.get("value").toString(), request.getTimestamp(), (long) item.get("timestamp"));
            System.out.println(responseMessage);

            message response = new message("GET_OK", key, item.get("value").toString(), request.getTimestamp(), request.getC_IP(), request.getC_port(), IP, port);
            outputStream.write(codify_message(response).getBytes());
        } else {
            String responseMessage = GET_SERVER_PRINT(request.getC_IP(), request.getC_port(), key, ERROR(), request.getTimestamp(), (long) item.get("timestamp"));
            System.out.println(responseMessage);

            message response = new message("ERROR", key, ERROR(), (Long) null, request.getC_IP(), request.getC_port(), IP, port);
            outputStream.write(codify_message(response).getBytes());
        }
    }

    private void handle_PUT_request(OutputStream outputStream, message request) throws IOException {
        String key = request.getKey();
        HashMap<String, Object> item = files.get(key);

        if (isMaster) {
            if (item == null) {
                long ts = 0;
                files.put(key, new HashMap<>());
                files.get(key).put("value", request.getValue());
                files.get(key).put("timestamp", ts);

                message replication = new message("REPLICATION", key, request.getValue(), ts, request.getC_IP(), request.getC_port(), IP, port);
                replicate_to_slaves(replication);

                String responseMessage = PUT_SERVER_MASTER_PRINT(request.getC_IP(), request.getC_port(), key, request.getValue());
                System.out.println(responseMessage);

                message response = new message("PUT_OK", key, request.getValue(), ts, request.getC_IP(), request.getC_port(), IP, port);
                outputStream.write(codify_message(response).getBytes());
            } else if ((long) item.get("timestamp") <= request.getTimestamp()) {
                files.get(key).put("value", request.getValue());
                files.get(key).put("timestamp", request.getTimestamp() + 1);

                message replication = new message("REPLICATION", key, request.getValue(), request.getTimestamp() + 1, request.getC_IP(), request.getC_port(), IP, port);
                replicate_to_slaves(replication);

                String responseMessage = PUT_SERVER_MASTER_PRINT(request.getC_IP(), request.getC_port(), key, request.getValue());
                System.out.println(responseMessage);

                message response = new message("PUT_OK", key, request.getValue(), request.getTimestamp() + 1, request.getC_IP(), request.getC_port(), IP, port);
                outputStream.write(codify_message(response).getBytes());
            } else {
                String responseMessage = PUT_SERVER_MASTER_ERROR_PRINT(request.getC_IP(), request.getC_port(), key);
                System.out.println(responseMessage);

                message response = new message("PUT_ERROR", key, item.get("value"), request.getC_IP(), request.getC_port(), IP, port);
                outputStream.write(codify_message(response).getBytes());
            }
        } else {
            String responseMessage = PUT_SERVER_SLAVE_PRINT(request.getC_IP(), request.getC_port(), key, request.getValue());
            System.out.println(responseMessage);

            Socket masterSocket = new Socket(master_IP, master_port);
            OutputStream masterOutputStream = masterSocket.getOutputStream();
            masterOutputStream.write(codify_message(request).getBytes());

            byte[] masterBuffer = new byte[1024];
            int masterBytesRead = masterSocket.getInputStream().read(masterBuffer);
            if (masterBytesRead > 0) {
                String masterJsonStrReceived = new String(masterBuffer, 0, masterBytesRead);
                message masterResponse = decodify_message(masterJsonStrReceived);
                if (masterResponse.getType().equals("PUT_OK")) {
                    String slaveResponseMessage = PUT_SERVER_MASTER_PRINT(masterResponse.getC_IP(), masterResponse.getC_port(), key, request.getValue());
                    System.out.println(slaveResponseMessage);

                    message slaveResponse = new message("PUT_OK", key, request.getValue(), request.getTimestamp(), request.getC_IP(), request.getC_port(), IP, port);
                    outputStream.write(codify_message(slaveResponse).getBytes());
                }
            }
            masterSocket.close();
        }
    }

    private void handle_REPLICATION_request(OutputStream outputStream, message request) throws IOException {
        String key = request.getKey();
        files.put(key, new HashMap<>());
        files.get(key).put("value", request.getValue());
        files.get(key).put("timestamp", request.getTimestamp());

        String responseMessage = REPLICATION_SERVER_SLAVE_PRINT(key, request.getValue(), request.getTimestamp());
        System.out.println(responseMessage);

        message response = new message("REPLICATION_OK", key, request.getValue(), request.getTimestamp(), request.getC_IP(), request.getC_port(), IP, port);
        outputStream.write(codify_message(response).getBytes());
    }

    private void replicate_to_slaves(message replication) {
        for (String slave : new String[]{"127.0.0.1" + ":" + 10098, "127.0.0.1" + ":" + 10099}) {
            try {
                String[] parts = slave.split(":");
                Socket slaveSocket = new Socket(parts[0], Integer.parseInt(parts[1]));
                OutputStream slaveOutputStream = slaveSocket.getOutputStream();
                slaveOutputStream.write(codify_message(replication).getBytes());
                slaveSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private message decodify_message(String json_str_received) {
        // Implementação do decodify_message
        try {
            // Quebre a string JSON em pares chave-valor usando ',' como delimitador
            String[] keyValuePairs = json_str_received.split(",");

            // Crie um mapa para armazenar as chaves e valores
            Map<String, String> jsonMap = new HashMap<>();

            // Preencha o mapa com os pares chave-valor
            for (String pair : keyValuePairs) {
                String[] entry = pair.split(":");
                String key = entry[0].trim();
                String value = entry[1].trim();
                jsonMap.put(key, value);
            }

            // Construa o objeto messagePy a partir do mapa
            String type = jsonMap.get("type");
            String key = jsonMap.get("key");
            String value = jsonMap.get("value");
            int timestamp = Integer.parseInt(jsonMap.get("timestamp"));
            String c_IP = jsonMap.get("c_IP");
            int c_port = Integer.parseInt(jsonMap.get("c_port"));
            String s_IP = jsonMap.get("s_IP");
            int s_port = Integer.parseInt(jsonMap.get("s_port"));

            return new message(type, key, value, timestamp, c_IP, c_port, s_IP, s_port);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String codify_message(message message) {
        // Implementação do codify_message
        try {
            // Construa uma string JSON manualmente concatenando os campos do objeto messagePy
            String json = "{" +
                    "\"type\":\"" + message.getType() + "\"," +
                    "\"key\":\"" + message.getKey() + "\"," +
                    "\"value\":\"" + message.getValue() + "\"," +
                    "\"timestamp\":" + message.getTimestamp() + "," +
                    "\"c_IP\":\"" + message.getC_IP() + "\"," +
                    "\"c_port\":" + message.getC_port() + "," +
                    "\"s_IP\":\"" + message.getServerIP() + "\"," +
                    "\"s_port\":" + message.getServerPort() +
                    "}";
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Methods for printing
    private String REPLICATION_SERVER_SLAVE_PRINT(String key, Object value, Object timestamp) {
        return "REPLICATION key:" + key + " value:" + value + " timestamp:" + timestamp + ".";
    }

    private String PUT_SERVER_SLAVE_PRINT(String c_IP, int c_port, String key, Object value) {
        return "Encaminhando PUT key:" + key + " value:" + value + ".";
    }

    private String PUT_SERVER_MASTER_PRINT(String c_IP, int c_port, String key, Object value) {
        return "Cliente " + c_IP + ":" + c_port + " PUT key:" + key + " value:" + value + ".";
    }

    private String PUT_SERVER_MASTER_ERROR_PRINT(String c_IP, int c_port, String key) {
        return "Cliente " + c_IP + ":" + c_port + " tentou inserir um valor desatualizado na key:" + key;
    }

    private String GET_SERVER_PRINT(String c_IP, int c_port, String key, Object value, Object c_timestamp, Object s_timestamp) {
        return "Cliente " + c_IP + ":" + c_port + " GET key:" + key + " ts:" + c_timestamp + ". Meu ts é " + s_timestamp + ", portanto devolvendo " + value + ".";
    }

    private String ERROR() {
        return "TRY_OTHER_SERVER_OR_LATER";
    }

    // Class Message is not included here to avoid repetition.
    // It should be added to the same Java file or in a separate file as needed.

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Ask user to start a server
        System.out.print("type INIT to start a Server\n");
        String choice = scanner.nextLine();

        // Parse user input to start the server
        if ("INIT".equalsIgnoreCase(choice) || choice.isEmpty() || "init".equalsIgnoreCase(choice)) {
            System.out.print("Enter your own server IP:\n");
            String s_IP = scanner.nextLine();

            System.out.print("Enter your own server port:\n");
            int s_port = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            System.out.print("Enter the server 1 IP:\n");
            String s1_IP = scanner.nextLine();

            System.out.print("Enter the server 1 port:\n");
            int s1_port = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            System.out.print("Enter the server 2 IP:\n");
            String s2_IP = scanner.nextLine();

            System.out.print("Enter the server 2 port:\n");
            int s2_port = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            System.out.print("Enter the master IP:\n");
            String master_IP = scanner.nextLine();

            System.out.print("Enter the master port:\n");
            int master_port = scanner.nextInt();

            new server(s_IP, s_port, s1_IP, s1_port, s2_IP, s2_port, master_IP, master_port);
        } else {
            // If the user choice is not to start a server, exit the process.
            System.out.println("Ending process");
            System.exit(0);
        }
    }
}

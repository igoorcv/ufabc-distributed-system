import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class server {
    private String IP;
    private int port;
    private ServerSocket serverSocket;
    private Map<String, String[]> peers;

    public server(String IP, int port) {
        this.IP = IP;
        this.port = port;
        this.peers = new HashMap<>();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started at IP: " + IP + " Port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new PeerHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
        	System.out.println("TESTE 1");
            e.printStackTrace();
        }
    }

    private class PeerHandler implements Runnable {
        private Socket clientSocket;

        public PeerHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                String request = new String(buffer, 0, bytesRead);
                String[] data = request.split("\\s+");

                if (data[0].equals("JOIN")) {
                    String[] files = String.join(" ", Arrays.copyOfRange(data, 3, data.length))
                            .replace("[", "")
                            .replace("]", "")
                            .replace("\"", "")
                            .replace(",", "")
                            .replace("'", "")
                            .split(" ");

                    peers.put(data[2], new String[] { data[1], data[2], String.join(" ", files) });
                    System.out.printf("Peer %s:%s adicionado com arquivos %s%n", data[1], data[2],
                            Arrays.toString(files));

                    String response = "JOIN_OK";
                    outputStream.write(response.getBytes());

                } else if (data[0].equals("SEARCH")) {
                    String searchData = data[1];
                    String p_IP = clientSocket.getInetAddress().getHostAddress();
                    int p_port = clientSocket.getPort();
                    System.out.printf("Peer %s:%s solicitou arquivo %s%n", p_IP, p_port, searchData);

                    String response = "SEARCH_OK";
                    List<String> matchingPeers = new ArrayList<>();

                    for (String[] peerData : peers.values()) {
                        String[] files = peerData[2].split(" ");
                        for (String file : files) {
                            if (file.equals(searchData)) {
                                String peer = peerData[0] + ":" + peerData[1];
                                matchingPeers.add(peer);
                                break;
                            }
                        }
                    }

                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("matching_peers", matchingPeers);
                    responseMap.put("response", response);
                    String responseData = responseMap.toString();
                    outputStream.write(responseData.getBytes());

                } else if (data[0].equals("UPDATE")) {
                    String[] array = Arrays.copyOfRange(data, 3, data.length);
                    System.arraycopy(data, 3, array, 0, data.length - 3); //Correção array como parametro abaixo
                    peers.put(data[2], new String[] { data[1], data[2]});

                    String response = "UPDATE_OK";
                    outputStream.write(response.getBytes());
                }

                clientSocket.close();

            } catch (IOException e) {
            	System.out.println("TESTE 2");
                e.printStackTrace();
            }
        }
    }
}
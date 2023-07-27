package message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class message {

    // Lista de tipos de mensagens permitidos
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "GET",
        "GET_OK",
        "GET_NULL",
        "PUT",
        "PUT_OK",
        "PUT_ERROR",
        "REPLICATION",
        "REPLICATION_OK",
        "ERROR"
    );

    private String key;
    private String value;
    private long timestamp;
    private String type;
    private String c_IP;
    private int c_port;
    private String s_IP;
    private int s_port;

    // Método construtor da classe Message
    public message(String type, String key, String value, long timestamp, String c_IP, int c_port, String s_IP, int s_port) {
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
        // Valida o tipo da mensagem e define o atributo type
        this.type = validateType(type);
        this.c_IP = c_IP;
        this.c_port = c_port;
        this.s_IP = s_IP;
        this.s_port = s_port;
    }

    public message(String type, String key2, Object object, String c_IP2, int c_port2, String iP, int port) {
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

    public String getC_IP() {
        return c_IP;
    }

    public int getC_port() {
        return c_port;
    }

    public String getServerIP() {
        return s_IP;
    }

    public int getServerPort() {
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

    // Função para codificar uma mensagem em bytes para ser enviada através de um socket
    public static byte[] codifyMessage(message message) throws IOException {
        // Serializa a mensagem usando um ObjectOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(message);
        }
        byte[] serializedMessage = byteArrayOutputStream.toByteArray();

        // Obtém o tamanho da mensagem serializada em bytes e converte para um inteiro de 4 bytes em ordem big-endian
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(serializedMessage.length);
        byte[] messageLength = bb.array();

        // Retorna a concatenação do tamanho da mensagem e da mensagem serializada
        return concatenateArrays(messageLength, serializedMessage);
    }

    // Função para decodificar uma mensagem recebida em bytes através de um socket
    public static message decodifyMessage(byte[] message) throws IOException, ClassNotFoundException {
        // Obtém o tamanho da mensagem a partir dos primeiros 4 bytes e converte para um inteiro
        ByteBuffer bb = ByteBuffer.wrap(message, 0, 4);
        bb.order(ByteOrder.BIG_ENDIAN);
        int messageLength = bb.getInt();

        // Obtém a mensagem serializada a partir dos próximos bytes até o tamanho da mensagem
        byte[] serializedMessage = Arrays.copyOfRange(message, 4, 4 + messageLength);

        // Desserializa a mensagem usando um ObjectInputStream e retorna o objeto Message resultante
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedMessage))) {
            return (message) objectInputStream.readObject();
        }
    }

    // Função auxiliar para concatenar dois arrays de bytes
    private static byte[] concatenateArrays(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] result = new byte[aLen + bLen];
        System.arraycopy(a, 0, result, 0, aLen);
        System.arraycopy(b, 0, result, aLen, bLen);
        return result;
    }
}
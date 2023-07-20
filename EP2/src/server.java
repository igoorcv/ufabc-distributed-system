import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Interface remota para transferência de arquivos
interface FileTransfer extends java.rmi.Remote {
	
    // Método para baixar um arquivo do peer remoto
    byte[] downloadFile(String peerName, String fileName, String ip, int porta) throws RemoteException;

    // Método para procurar por arquivos em peers remotos
    List<String> search(String fileName, String ip, int porta) throws RemoteException;

    // Método para um peer remoto se juntar ao servidor central
    boolean join(String peerName, String fileName, String ip, int porta) throws RemoteException;

    // Método para atualizar um arquivo no servidor central
    boolean update(String filename) throws RemoteException;

    // Método para receber um arquivo enviado por outro peer
    void receiveFile(String peerName, String fileName, byte[] fileData) throws RemoteException;
}

// Classe para armazenar informações sobre um peer
class PeerInfo {
    private String name;
    private List<String> files;

    public PeerInfo(String name) {
        this.name = name;
        this.files = new ArrayList<>();
    }

    // Obtém o nome do arquivo
    public String getFileName(String filename) {
        // String name = new String(filename.toUpperCase());
        return name;
    }

    // Obtém o nome do peer
    public String getName(String peername) {
        String name = new String(peername.toUpperCase());
        return name;
    }

    // Obtém o nome do peer
    public String getPeerName(String peername) {
        String name = new String(peername.toUpperCase());
        return name;
    }

    // Obtém a lista de arquivos do peer
    public List<String> getFiles() {
        return files;
    }

    // Adiciona um arquivo à lista de arquivos do peer
    public void addFile(String fileName) {
        files.add(fileName);
    }

    // Verifica se o peer possui um arquivo específico
    public boolean hasFile(String fileName) {
        return files.contains(fileName);
    }
}

// Implementação da interface remota FileTransfer
class FileTransferImpl extends UnicastRemoteObject implements FileTransfer {
    private List<PeerInfo> peers;
    private Map<String, String> nameFiles;

    // Obtém um peer pelo nome
    private PeerInfo getPeerByName(String peerName) {
        for (PeerInfo peer : peers) {
            if (peer.getPeerName(peerName).equals(peerName)) {
                return peer;
            }
        }
        return null; // Retorna null caso o peer não seja encontrado
    }

    protected FileTransferImpl() throws RemoteException {
        super();
        peers = new ArrayList<>();
        nameFiles = new HashMap<>();
    }

    // Método para um peer remoto se juntar ao servidor central
    public boolean put(String peerName, String fileName, String ip, int porta) throws RemoteException {
        System.out.println("Peer " + ip + ":" + porta + " adicionado com arquivos " + fileName + ".");
        PeerInfo peer = getPeerByName(peerName);

        if (peer != null) {
            System.out.println("Arquivos não adicionados.");
            return false;
        } else {
            PeerInfo newPeer = new PeerInfo(peerName);
            newPeer.addFile(fileName);
            peers.add(newPeer);
            nameFiles.put(peerName, fileName);
        }

        return true;
    }

    // Método para procurar por arquivos em peers remotos
    public List<String> get(String fileName, String ip, int porta) throws RemoteException {
        System.out.println("Peer " + ip + ":" + porta + " solicitou arquivo " + fileName + ".");
        List<String> peersWithFile = new ArrayList<>();

        for (PeerInfo peer : peers) {
            if (peer.hasFile(fileName)) {
                peersWithFile.add(peer.getFileName(fileName));
            }
        }
        return peersWithFile;
    }

    // Método para baixar um arquivo do peer remoto
    public byte[] downloadFile(String peerName, String fileName, String ip, int porta) throws RemoteException {
        File file = new File(peerName + "_" + fileName);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileData = new byte[(int) file.length()];
            fileInputStream.read(fileData);
            fileInputStream.close();

            return fileData;
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    // Método para receber um arquivo enviado por outro peer
    public void receiveFile(String peerName, String fileName, byte[] fileData) throws RemoteException {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(peerName + "_" + fileName);
            fileOutputStream.write(fileData);
            fileOutputStream.close();
        } catch (Exception e) {
            System.err.println("Erro ao receber o arquivo: " + e.toString());
            e.printStackTrace();
        }

        System.out.println("Arquivo " + fileName + " recebido do peer " + peerName);
    }
}

public class server {
    public static void main(String[] args) throws NotBoundException {
        try {
            // Inicializar o registro RMI
            Registry registry = LocateRegistry.createRegistry(1099);

            // Criar uma instância do objeto remoto
            FileTransfer fileTransfer = new FileTransferImpl();

            // Vincular o objeto remoto ao registro
            registry.rebind("FileTransfer", fileTransfer);

            System.out.println("Servidor iniciado. Aguardando requisições...");
            System.out.println();
        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.toString());
            e.printStackTrace();
        }
    }
}

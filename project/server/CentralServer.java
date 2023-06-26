import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

interface FileTransfer extends java.rmi.Remote {
    byte[] downloadFile(String filename) throws RemoteException;
    boolean join(String peerName) throws RemoteException;
    boolean search(String filename) throws RemoteException;
    void update(String filename) throws RemoteException;
}

class Peer {
    private String name;
    private List<String> files;

    public Peer(String name) {
        this.name = name;
        this.files = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addFile(String fileName) {
        files.add(fileName);
    }

    public boolean hasFile(String fileName) {
        return files.contains(fileName);
    }
}

class FileTransferImpl extends UnicastRemoteObject implements FileTransfer {
    private List<String> peers;
    
    private Peer getPeerByName(String peerName) {
        for (Peer peer : peers) {
            if (peer.getName().equals(peerName)) {
                return peer;
            }
        }
        return null; // Retorna null caso o peer não seja encontrado
    }

    protected FileTransferImpl() throws RemoteException {
        super();
        //this.peers = new ArrayList<>(); //Armazena as informações dos peers que se juntam ao servidor.
        peers = new ArrayList<>();
    }

    public byte[] downloadFile(String filename) throws RemoteException {
        // Lógica para download do arquivo
        return null;
    }
    
    //JOIN
    public boolean join(String peerName) throws RemoteException {
    	System.out.println("Peer " + peerName + " se juntou ao servidor.");
        if (peers.contains(peerName)) {
            return false;
        } else {
            peers.add(peerName);
            return true;
        }
    }

    //SEARCH
    public boolean search(String filename) throws RemoteException {
        // Lógica para busca do arquivo
    	List<String> peersWithFile = new ArrayList<>();

        for (String peerName : peers) {
        	Peer peer = getPeerByName(peerName);
            if (peer.hasFile(filename)) {
                peersWithFile.add(peerName);
            }
        }

        if (!peersWithFile.isEmpty()) {
            String result = "Arquivo encontrado nos seguintes peers: ";
            result += String.join(", ", peersWithFile);
            return true;
        } else {
            return false;
        }
    }

    //UPDATE
    public void update(String filename) throws RemoteException {
        // Lógica para atualização do arquivo
    }
}

public class CentralServer {
	
    public static void main(String[] args) {
        try {
            // Inicializar o registro RMI
            Registry registry = LocateRegistry.createRegistry(1099);

            // Criar uma instância do objeto remoto
            FileTransfer fileTransfer = new FileTransferImpl();

            // Vincular o objeto remoto ao registro
            registry.rebind("FileTransfer", fileTransfer);

            System.out.println("Servidor iniciado. Aguardando requisições...");

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

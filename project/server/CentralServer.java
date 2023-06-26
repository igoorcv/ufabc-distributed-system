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

class FileTransferImpl extends UnicastRemoteObject implements FileTransfer {
    private List<String> peers;

    protected FileTransferImpl() throws RemoteException {
        super();
        //this.peers = new ArrayList<>(); //Armazena as informações dos peers que se juntam ao servidor.
        peers = new ArrayList<>();
    }

    public byte[] downloadFile(String filename) throws RemoteException {
        // Lógica para download do arquivo
        return null;
    }
    
    /*
    public void join(String peerName) throws RemoteException {
        System.out.println("Peer " + peerName + " se juntou ao servidor.");
        this.peers.add(peerName);
        
        if (peers.contains(peerName)){
        	return "JOIN_OK";
        	
        } else {
        	peers.add(peerName);
        	return "JOIN_OK";
        }
        
    }
    */
    
    public boolean join(String peerName) throws RemoteException {
    	System.out.println("Peer " + peerName + " se juntou ao servidor.");
        if (peers.contains(peerName)) {
            return false;
        } else {
            peers.add(peerName);
            return true;
        }
    }

    public boolean search(String filename) throws RemoteException {
        // Lógica para busca do arquivo
        return false;
    }

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

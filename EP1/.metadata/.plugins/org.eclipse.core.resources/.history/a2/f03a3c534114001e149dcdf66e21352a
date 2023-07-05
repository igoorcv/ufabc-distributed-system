import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface FileTransfer extends java.rmi.Remote {
	byte[] downloadFile(String filename) throws RemoteException;

	boolean join(String peerName, String fileName) throws RemoteException;

	// boolean search(String fileName) throws RemoteException;
	List<String> search(String fileName) throws RemoteException;

	void update(String filename) throws RemoteException;
}

class PeerInfo {
	private String name;
	private List<String> files;

	public PeerInfo(String name) {
		this.name = name;
		this.files = new ArrayList<>();
	}

	public String getFileName(String filename) {
		// String name = new String(filename.toUpperCase());
		return name;
	}

	public String getName(String peername) {
		String name = new String(peername.toUpperCase());
		return name;
	}

	public String getPeerName(String peername) {
		String name = new String(peername.toUpperCase());
		return name;
	}

	public List<String> getFiles() {
		return files;
	}

	public void addFile(String fileName) {
		files.add(fileName);
	}

	public boolean hasFile(String fileName) {
		return files.contains(fileName);
	}
}

class FileTransferImpl extends UnicastRemoteObject implements FileTransfer {
	private List<PeerInfo> peers;
	private Map<String, String> nameFiles;

	protected FileTransferImpl() throws RemoteException {
		super();
		peers = new ArrayList<>();
		nameFiles = new HashMap<>();
	}

	public byte[] downloadFile(String filename) throws RemoteException {
		// Lógica para download do arquivo
		return null;
	}

	public boolean join(String peerName, String fileName) throws RemoteException {
		System.out.println("Peer " + peerName + " se juntou ao servidor com o arquivo " + fileName);
		//System.out.println();
		PeerInfo peer = getPeerByName(peerName);

		if (peer != null) {
			System.out.println("não adicionei, gatinho");
			return false;

		} else {

			PeerInfo newPeer = new PeerInfo(peerName);
			newPeer.addFile(fileName);
			peers.add(newPeer);
			nameFiles.put(peerName, fileName);
			// return true;
		}
		//Não dá pra listar tudo aqui dentro, apenas chamando por fora. O que dá pra fazer é listar o que é cadastrado.
		// Imprimir a lista de peers e nameFiles
		/*
		System.out.println("--- Lista de Peers ---");
		for (PeerInfo p : peers) {
			System.out.println("Peer: " + p.getPeerName(peerName));
			System.out.println("Arquivo: " + nameFiles.get(p.getFileName(fileName)));
			System.out.println();
			break;
		}
		*/

		return true;
	}
	
	

	public List<String> search(String fileName) throws RemoteException {
		// public boolean search(String fileName) throws RemoteException {
		List<String> peersWithFile = new ArrayList<>();

		for (PeerInfo peer : peers) {
			if (peer.hasFile(fileName)) {
				peersWithFile.add(peer.getFileName(fileName));
			}
		}

		// return !peersWithFile.isEmpty();

		return peersWithFile;
	}

	public void update(String filename) throws RemoteException {
		// Lógica para atualização do arquivo
	}

	private PeerInfo getPeerByName(String peerName) {
		for (PeerInfo peer : peers) {
			if (peer.getPeerName(peerName).equals(peerName)) {
				return peer;
			}
		}
		return null; // Retorna null caso o peer não seja encontrado
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

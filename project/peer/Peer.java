import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.io.IOException;


public class Peer {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 1099;
	// String serverHost = "localhost"; // substitua pelo endereço do servidor
	// int serverPort = 1099; // substitua pela porta do servidor

	public static void main(String[] args) {
		// Obter o endereço IP
		String ip = "";
		try {
		    InetAddress address = InetAddress.getLocalHost();
		    ip = address.getHostAddress();
		} catch (UnknownHostException e) {
		    e.printStackTrace();
		}

		// Obter a porta
		int porta = 0;
		try (ServerSocket socket = new ServerSocket(0)) {
		    porta = socket.getLocalPort();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		try {
			// Obtém a referência para o registro RMI do servidor
			Registry registry = LocateRegistry.getRegistry(SERVER_HOST, SERVER_PORT);
			// Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);

			// Obtém a referência para o objeto remoto FileTransfer no servidor
			FileTransfer fileTransfer = (FileTransfer) registry.lookup("FileTransfer");

			// Cria um objeto Scanner para receber as entradas do usuário
			Scanner scanner = new Scanner(System.in);

			while (true) {
				// Exibe o menu de opções
				System.out.println("\n--- Menu ---");
				System.out.println("1. JOIN");
				System.out.println("2. SEARCH");
				System.out.println("3. UPDATE");
				System.out.println("0. Sair");
				System.out.print("Escolha uma opção: ");
				int option = scanner.nextInt();
				List<String> extensoesValidas = List.of(".doc", ".pdf", ".txt", "mp3", "mp4"); // Exemplo de extensões válidas

				switch (option) {
				case 1:
					// Requisição JOIN
					System.out.print("Digite o nome do peer para se juntar: ");
					String peerName = scanner.next();
					System.out.print("Digite o nome do arquivo a ser enviado: ");
					String fileName = scanner.next();

				    boolean extensaoValida1 = false;
					for (String extensao : extensoesValidas) {
						if (fileName.endsWith(extensao)) {
							extensaoValida1 = true;
							break;
						}
					}

					if (extensaoValida1) {
						fileTransfer.join(peerName, fileName);

						FileTransfer server1 = (FileTransfer) registry.lookup("FileTransfer");
						boolean joinResult = server1.join(peerName, fileName);
						if (joinResult == true) { //JOIN_OK
							System.out.println();
							System.out.println("Sou peer " + ip + " : " + porta + " com arquivos " + fileName + ".");
						} else if (joinResult == false) {
							System.out.println();
							System.out.println("JOIN_NOK"); //JOIN_NOK
						}
						break;

					} else {
						System.out.println("Erro: O nome do arquivo não possui uma extensão válida ou a extensão utilizada não é suportada.");
						break;
					}

				case 2:
					// Requisição SEARCH
					System.out.print("Digite o nome do arquivo a ser pesquisado: ");
					String filename = scanner.next();
					
					boolean extensaoValida2 = false;
					for (String extensao : extensoesValidas) {
						if (filename.endsWith(extensao)) {
							extensaoValida2 = true;
							break;
						}
					}
					
					if (extensaoValida2) {
						fileTransfer.search(filename);
						System.out.println("Requisição SEARCH enviada ao servidor.");

						FileTransfer server2 = (FileTransfer) registry.lookup("FileTransfer");

						List<String> searchResult = server2.search(filename);

						if (searchResult.isEmpty()) {
							System.out.println("Arquivo não encontrado em nenhum peer.");
						} else {
							System.out.println("Arquivo encontrado nos seguintes peers: " + searchResult);
						}
						break;
						
					} else {
						System.out.println("Erro: O nome do arquivo não possui uma extensão válida ou a extensão utilizada não é suportada.");
						break;
					}

				case 3:
					// Requisição UPDATE
					System.out.print("Digite o nome do arquivo a ser atualizado: ");
					String updatedFilename = scanner.next();
					fileTransfer.update(updatedFilename);
					System.out.println("Requisição UPDATE enviada ao servidor.");
					break;

				case 0:
					// Sair
					System.out.println("Encerrando o peer.");
					scanner.close();
					System.exit(0);

				default:
					System.out.println("Opção inválida. Tente novamente.");
					break;
				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}
}

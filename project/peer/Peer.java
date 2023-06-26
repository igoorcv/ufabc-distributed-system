import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Peer {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 1099;
	//String serverHost = "localhost"; // substitua pelo endereço do servidor
	//int serverPort = 1099; // substitua pela porta do servidor

	public static void main(String[] args) {
		try {
			// Obtém a referência para o registro RMI do servidor
			Registry registry = LocateRegistry.getRegistry(SERVER_HOST, SERVER_PORT);
			//Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);

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

				switch (option) {
				case 1:
					// Requisição JOIN
					System.out.print("Digite o nome do peer para se juntar: ");
					String peerName = scanner.next();
					fileTransfer.join(peerName);
					System.out.println("Requisição JOIN enviada ao servidor.");
					
					FileTransfer server = (FileTransfer) registry.lookup("FileTransfer");
					boolean joinResult = server.join(peerName);
			        if (joinResult == false) {
			            System.out.println("JOIN_OK");
			        } else if (joinResult == true) {
			            System.out.println("JOIN_NOK");
			        }

					break;

				case 2:
					// Requisição SEARCH
					System.out.print("Digite o nome do arquivo a ser pesquisado: ");
					String filename = scanner.next();
					boolean found = fileTransfer.search(filename);
					if (found) {
						System.out.println("O arquivo está disponível no servidor.");
					} else {
						System.out.println("O arquivo não foi encontrado no servidor.");
					}
					break;

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

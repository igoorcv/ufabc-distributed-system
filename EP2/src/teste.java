import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
    
public class teste {
    public static void main(String[] args) {
        // Defina as constantes SERVER_A_PORT, SERVER_B_PORT e SERVER_C_PORT como inteiros representando as portas dos servidores
        int SERVER_A_PORT = 8080;
        int SERVER_B_PORT = 9090;
        int SERVER_C_PORT = 7070;

        // Crie a lista de portas de servidores
        //List<Integer> serversList = new ArrayList<>(Arrays.asList(SERVER_A_PORT, SERVER_B_PORT, SERVER_C_PORT));
        int[] serversList = {SERVER_A_PORT, SERVER_B_PORT, SERVER_C_PORT};
        Random random = new Random();
		int randomIndex = random.nextInt(serversList.length); // Gere um índice aleatório entre 0 (inclusive) e o tamanho da matriz (exclusive)
		int randomValue = serversList[randomIndex]; // Acesse o valor aleatório na matriz usando o índice gerado
		//int randomServer = random.serversList;

        // Teste imprimindo a lista de portas de servidores
        System.out.println(serversList);
        System.out.println(randomIndex);
        System.out.println(randomValue);

    }
}
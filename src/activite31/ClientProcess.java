package activite31;

import commun.Calculatrice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Thread dédié à UN client : il communique avec lui de façon indépendante des autres clients. */
public class ClientProcess extends Thread {
    private final Socket socket;
    private final int numero;

    public ClientProcess(Socket socket, int numero) {
        super("ClientProcess-" + numero);                     // nom du thread, visible dans les logs
        this.socket = socket;
        this.numero = numero;
    }

    @Override
    public void run() {
        String prefixe = "[" + getName() + "] ";
        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Le serveur envoie au client son numéro d'ordre
            out.println("Bienvenue ! Vous êtes le client n°" + numero);

            String ligne;
            while ((ligne = in.readLine()) != null) {
                if (ligne.trim().equalsIgnoreCase("fin")) break;
                String reponse = Calculatrice.traiterLigne(ligne);
                System.out.println(prefixe + "Reçu \"" + ligne.trim() + "\" -> " + reponse);
                out.println(reponse);
            }
        } catch (IOException e) {
            System.out.println(prefixe + "connexion interrompue : " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignore) { }
            System.out.println("Client n°" + numero + " déconnecté");
        }
    }
}

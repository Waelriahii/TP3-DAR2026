package activite31;

import commun.Calculatrice;
import commun.Reseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Activité 3-1 : client Java (même principe que l'activité 2-1) qui lit son numéro d'ordre à la connexion. */
public class Client {
    public static void main(String[] args) {
        try {
            Scanner clavier = new Scanner(System.in);
            System.out.print("Adresse IP du serveur (Entrée = localhost) : ");
            String saisie = clavier.nextLine().trim();
            if (saisie.isEmpty()) saisie = "localhost";

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByName(saisie), Reseau.PORT), 5000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.println("Message du serveur : " + in.readLine());   // numéro d'ordre

            while (true) {
                System.out.print("\nEntrez une opération (ex : 55 * 25) ou 'fin' pour quitter : ");
                String ligne = clavier.nextLine().trim();
                if (ligne.equalsIgnoreCase("fin")) {
                    out.println("fin");
                    break;
                }
                if (!Calculatrice.OPERATION.matcher(ligne).matches()) {
                    System.out.println("Syntaxe invalide : écrivez  opérande opérateur opérande  (opérateurs : + - * /)");
                    continue;
                }
                out.println(ligne);
                String reponse = in.readLine();
                if (reponse == null) { System.out.println("Le serveur a fermé la connexion."); break; }
                System.out.println("Résultat du serveur : " + reponse);
            }

            System.out.println("Fin de la communication, fermeture du socket");
            in.close(); out.close(); socket.close(); clavier.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

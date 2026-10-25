package activite32;

import commun.Calculatrice;
import commun.Reseau;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;

/** Activité 3-2 : client par objets, il lit d'abord son numéro d'ordre (Integer) puis envoie des Operation. */
public class Client {
    public static void main(String[] args) {
        try {
            Scanner clavier = new Scanner(System.in);
            System.out.print("Adresse IP du serveur (Entrée = localhost) : ");
            String saisie = clavier.nextLine().trim();
            if (saisie.isEmpty()) saisie = "localhost";

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByName(saisie), Reseau.PORT), 5000);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Integer numero = (Integer) in.readObject();
            System.out.println("Connecté : vous êtes le client n°" + numero);

            while (true) {
                System.out.print("\nEntrez une opération (ex : 55 * 25) ou 'fin' pour quitter : ");
                String ligne = clavier.nextLine().trim();
                if (ligne.equalsIgnoreCase("fin")) {
                    out.writeObject("FIN");
                    out.flush();
                    break;
                }
                Matcher m = Calculatrice.OPERATION.matcher(ligne);
                if (!m.matches()) {
                    System.out.println("Syntaxe invalide : écrivez  opérande opérateur opérande  (opérateurs : + - * /)");
                    continue;
                }
                Operation op = new Operation(Double.parseDouble(m.group(1)), m.group(2).charAt(0),
                        Double.parseDouble(m.group(3)));
                out.writeObject(op);
                out.flush();

                Resultat resultat = (Resultat) in.readObject();
                if (resultat.isErreur()) System.out.println("Résultat du serveur : Erreur : " + resultat.getMessage());
                else System.out.println("Résultat du serveur : " + op + " = " + Calculatrice.formater(resultat.getValeur()));
            }

            System.out.println("Fin de la communication, fermeture du socket");
            in.close(); out.close(); socket.close(); clavier.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

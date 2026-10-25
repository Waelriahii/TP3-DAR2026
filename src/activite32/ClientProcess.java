package activite32;

import commun.Calculatrice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Thread dédié à un client : reçoit des objets Operation, calcule, renvoie des objets Resultat. */
public class ClientProcess extends Thread {
    private final Socket socket;
    private final int numero;

    public ClientProcess(Socket socket, int numero) {
        super("ClientProcess-" + numero);
        this.socket = socket;
        this.numero = numero;
    }

    @Override
    public void run() {
        String prefixe = "[" + getName() + "] ";
        try {
            // Même ordre qu'au TP2 : flux de sortie + flush, puis flux d'entrée
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(Integer.valueOf(numero));        // numéro d'ordre envoyé au client
            out.flush();

            while (true) {
                Object recu = in.readObject();

                if (recu instanceof Operation) {
                    Operation op = (Operation) recu;
                    Resultat resultat;
                    try {
                        double r = Calculatrice.calculer(op.getOperande1(), op.getOperateur(), op.getOperande2());
                        resultat = Resultat.succes(r);

                        // Section critique : le compteur global est partagé entre tous les threads
                        int total = CompteurGlobal.incrementer();
                        System.out.println(prefixe + "Client n°" + numero + " : " + op + " = "
                                + Calculatrice.formater(r) + "  |  Compteur global = " + total);
                    } catch (ArithmeticException | IllegalArgumentException e) {
                        resultat = Resultat.echec(e.getMessage());
                        System.out.println(prefixe + "Client n°" + numero + " : " + op
                                + " -> Erreur : " + e.getMessage() + " (non compté)");
                    }
                    out.writeObject(resultat);
                    out.flush();

                } else if ("FIN".equals(recu)) {
                    break;
                }
            }
            in.close();
            out.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(prefixe + "connexion interrompue : " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignore) { }
            System.out.println("Client n°" + numero + " déconnecté");
        }
    }
}

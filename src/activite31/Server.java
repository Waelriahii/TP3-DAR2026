package activite31;

import commun.Reseau;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Activité 3-1 : serveur MULTI-THREAD.
 * Le thread principal ne fait qu'une chose : accepter les clients. Chaque client est confié à un thread dédié.
 */
public class Server {
    public static void main(String[] args) {
        try {
            InetAddress ip = (args.length > 0) ? InetAddress.getByName(args[0]) : Reseau.trouverIpLocale();
            InetSocketAddress adresse = new InetSocketAddress(ip, Reseau.PORT);

            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(adresse);
            System.out.println("Serveur multi-thread en attente de connexions sur " + adresse);

            int numero = 0;                                   // numéro d'ordre de connexion
            while (true) {                                    // le serveur ne s'arrête plus après un client
                Socket socket = serverSocket.accept();        // attend le client suivant
                numero++;
                System.out.println("Client n°" + numero + " connecté : " + socket.getRemoteSocketAddress());
                new ClientProcess(socket, numero).start();    // un thread dédié par client, puis on retourne à accept()
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

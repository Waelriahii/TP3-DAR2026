package activite32;

import commun.Reseau;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/** Activité 3-2 : serveur multi-thread du service de calculatrice par OBJETS (Operation / Resultat). */
public class Server {
    public static void main(String[] args) {
        try {
            InetAddress ip = (args.length > 0) ? InetAddress.getByName(args[0]) : Reseau.trouverIpLocale();
            InetSocketAddress adresse = new InetSocketAddress(ip, Reseau.PORT);

            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(adresse);
            System.out.println("Serveur calculatrice multi-thread en attente de connexions sur " + adresse);

            int numero = 0;
            while (true) {
                Socket socket = serverSocket.accept();
                numero++;
                System.out.println("Client n°" + numero + " connecté : " + socket.getRemoteSocketAddress());
                new ClientProcess(socket, numero).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

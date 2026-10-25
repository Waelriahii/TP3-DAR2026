package commun;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/** Outils réseau partagés par le client et le serveur. */
public class Reseau {
    public static final int PORT = 1234;

    /** Cherche l'adresse IPv4 privée (réseau local) de la machine. */
    public static InetAddress trouverIpLocale() throws IOException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
            Enumeration<InetAddress> adresses = ni.getInetAddresses();
            while (adresses.hasMoreElements()) {
                InetAddress a = adresses.nextElement();
                if (a instanceof Inet4Address && a.isSiteLocalAddress()) return a;
            }
        }
        return InetAddress.getLoopbackAddress(); // solution de repli
    }
}

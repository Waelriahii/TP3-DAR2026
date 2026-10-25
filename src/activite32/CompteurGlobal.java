package activite32;

/**
 * Compteur global partagé par TOUS les threads ClientProcess : c'est une ressource critique.
 * Les méthodes sont "synchronized" : un seul thread à la fois peut les exécuter (verrou sur la classe).
 */
public class CompteurGlobal {
    private static int valeur = 0;

    /** Incrémente et renvoie la nouvelle valeur, de façon atomique. */
    public static synchronized int incrementer() {
        valeur++;
        return valeur;
    }

    public static synchronized int getValeur() {
        return valeur;
    }
}

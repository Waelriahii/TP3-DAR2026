package activite32;

import java.io.Serializable;

/** Objet renvoyé par le serveur : soit une valeur, soit un message d'erreur. */
public class Resultat implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean erreur;
    private final double valeur;
    private final String message;

    private Resultat(boolean erreur, double valeur, String message) {
        this.erreur = erreur;
        this.valeur = valeur;
        this.message = message;
    }

    public static Resultat succes(double valeur)     { return new Resultat(false, valeur, null); }
    public static Resultat echec(String message)     { return new Resultat(true, 0, message); }

    public boolean isErreur()  { return erreur; }
    public double getValeur()  { return valeur; }
    public String getMessage() { return message; }
}

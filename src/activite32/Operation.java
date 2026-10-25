package activite32;

import commun.Calculatrice;

import java.io.Serializable;

/**
 * Objet envoyé par le client : premier opérande, opérateur, deuxième opérande.
 * "implements Serializable" autorise Java à convertir l'objet en suite d'octets.
 */
public class Operation implements Serializable {
    private static final long serialVersionUID = 1L;   // version de la classe (doit être identique des 2 côtés)

    private final double operande1;
    private final char operateur;
    private final double operande2;

    public Operation(double operande1, char operateur, double operande2) {
        this.operande1 = operande1;
        this.operateur = operateur;
        this.operande2 = operande2;
    }

    public double getOperande1() { return operande1; }
    public char getOperateur()   { return operateur; }
    public double getOperande2() { return operande2; }

    @Override
    public String toString() {
        return Calculatrice.formater(operande1) + " " + operateur + " " + Calculatrice.formater(operande2);
    }
}

package commun;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Logique de calcul et de validation, partagée par toutes les activités. */
public class Calculatrice {

    /** Syntaxe valide : opérande, opérateur (+ - * /), opérande (négatifs et décimaux acceptés). */
    public static final Pattern OPERATION = Pattern.compile(
            "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    public static double calculer(double a, char operateur, double b) {
        switch (operateur) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("division par zéro impossible");
                return a / b;
            default:
                throw new IllegalArgumentException("opérateur inconnu : " + operateur);
        }
    }

    /** 1375.0 -> "1375" ; 2.5 -> "2.5" */
    public static String formater(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    /** Traite une opération reçue sous forme de texte et renvoie la réponse (utilisé par l'activité 3-1). */
    public static String traiterLigne(String ligne) {
        Matcher m = OPERATION.matcher(ligne);
        if (!m.matches()) return "Erreur : opération invalide";
        double a = Double.parseDouble(m.group(1));
        char op = m.group(2).charAt(0);
        double b = Double.parseDouble(m.group(3));
        try {
            double r = calculer(a, op, b);
            return formater(a) + " " + op + " " + formater(b) + " = " + formater(r);
        } catch (ArithmeticException e) {
            return "Erreur : " + e.getMessage();
        }
    }
}

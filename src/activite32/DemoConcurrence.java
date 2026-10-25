package activite32;

/**
 * Démonstration (hors réseau) : pourquoi la synchronisation est indispensable.
 * 8 threads incrémentent chacun 20 000 fois un compteur, avec puis sans synchronisation.
 */
public class DemoConcurrence {
    static int sansSynchro = 0;
    static int avecSynchro = 0;

    // "compteur++" = 3 opérations (lire, ajouter 1, écrire). Un autre thread peut s'intercaler entre les trois :
    // les incrémentations se "marchent dessus" et certaines sont perdues.
    // Thread.yield() rend cet entrelacement très probable, même sur une machine à un seul cœur.
    static void incrementerSansSynchro() {
        int lu = sansSynchro;
        Thread.yield();
        sansSynchro = lu + 1;
    }

    // Même opération, mais un seul thread à la fois peut exécuter la méthode.
    static synchronized void incrementerAvecSynchro() {
        int lu = avecSynchro;
        Thread.yield();
        avecSynchro = lu + 1;
    }

    public static void main(String[] args) throws InterruptedException {
        final int THREADS = 8, ITERATIONS = 20_000;
        Thread[] ts = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < ITERATIONS; j++) {
                    incrementerSansSynchro();
                    incrementerAvecSynchro();
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.println("Valeur attendue            : " + (THREADS * ITERATIONS));
        System.out.println("Sans synchronisation       : " + sansSynchro);
        System.out.println("Avec synchronized          : " + avecSynchro);
    }
}

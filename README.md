# TP3-DAR2026 — Serveur multi-threads : sockets TCP en mode connecté

**Matière :** Développement d'applications réparties — LSI 3
**Établissement :** Faculté des Sciences de Sfax, Département d'informatique et des Communications
**Année universitaire :** 2026 – 2027
**Étudiant :** Wael Riahi
---

## 1. Introduction

Dans les TP1 et TP2, le serveur ne pouvait servir **qu'un seul client** : après `accept()`, il restait occupé avec ce client et s'arrêtait à sa déconnexion. Ce TP3 transforme le serveur en un **véritable serveur multi-threads** capable de servir plusieurs clients **en même temps**.

| Activité | Contenu |
|---|---|
| **3-1** | Serveur multi-thread qui accepte plusieurs clients, affiche leur adresse et leur numéro d'ordre, et envoie à chaque client son numéro (échange de chaînes, comme à l'activité 2-1) |
| **3-2** | Service de calculatrice par **objets** (activité 2-2) sur serveur multi-thread, avec un **compteur global synchronisé** du nombre d'opérations traitées |

## 2. Structure du projet

```
TP3-DAR2026/
└── src/
    ├── commun/
    │   ├── Reseau.java            → port + détection de l'IP locale
    │   └── Calculatrice.java      → calcul, validation de syntaxe, traitement d'une ligne de texte
    ├── activite31/
    │   ├── Server.java            → boucle d'acceptation des clients
    │   ├── ClientProcess.java     → thread dédié à un client (chaînes)
    │   └── Client.java            → client Java (chaînes)
    └── activite32/
        ├── Operation.java         → objet Serializable envoyé par le client
        ├── Resultat.java          → objet Serializable renvoyé par le serveur
        ├── CompteurGlobal.java    → compteur partagé et synchronisé
        ├── Server.java            → boucle d'acceptation des clients
        ├── ClientProcess.java     → thread dédié à un client (objets)
        ├── Client.java            → client Java (objets)
        └── DemoConcurrence.java   → démonstration de l'accès concurrent
```

## 3. Principe du serveur multi-threads

### 3.1 Pourquoi un serveur à un seul thread ne suffit pas

`accept()` est **bloquant**, et la lecture `readLine()` / `readObject()` l'est aussi. Un serveur à un seul thread qui dialogue avec un client ne revient plus à `accept()` tant que ce client n'a pas terminé : les autres clients attendent, ou sont refusés.

### 3.2 La solution : un thread par client

Le thread principal (le `main`) ne fait plus qu'**accepter** les connexions. Pour chaque client accepté, il crée un thread **`ClientProcess`** qui s'occupe seul de la communication avec ce client, puis il retourne immédiatement à `accept()` :

```java
int numero = 0;
while (true) {
    Socket socket = serverSocket.accept();            // attend le client suivant
    numero++;
    System.out.println("Client n°" + numero + " connecté : " + socket.getRemoteSocketAddress());
    new ClientProcess(socket, numero).start();        // thread dédié, puis retour à accept()
}
```

- `ClientProcess extends Thread` : sa méthode `run()` contient tout le dialogue avec **son** client (lecture, calcul, réponse, fermeture).
- Chaque thread possède **son propre `Socket`** et ses propres flux : les communications sont **indépendantes**.
- Le serveur tourne **indéfiniment** (boucle `while (true)`) ; on l'arrête manuellement (bouton Stop de l'IDE ou Ctrl+C).
- La variable `numero` n'est modifiée que par le thread principal : elle n'a pas besoin de synchronisation.

---

## 4. Activité 3-1 : Acceptation de plusieurs clients

### 4.1 Travail réalisé

- Un projet `TP3-DAR2026` a été créé, en reprenant le squelette d'échange de chaînes de l'activité 2-1.
- Le serveur **affiche pour chaque client** son adresse (`getRemoteSocketAddress()`, de la forme `/IP:port`) et son **numéro d'ordre** de connexion.
- Le serveur **envoie à chaque client son numéro d'ordre** dès la connexion :

```java
out.println("Bienvenue ! Vous êtes le client n°" + numero);
```

- Ensuite, chaque `ClientProcess` traite les opérations textuelles du client (`55 * 25`, etc.) jusqu'à la réception de `fin`.

### 4.2 Tests locaux

**Avec des clients Telnet** : dans plusieurs invites de commande, taper `telnet localhost 1234`. Chaque fenêtre affiche « Bienvenue ! Vous êtes le client n°X », puis on peut saisir une opération (par exemple `7 * 6`) et lire la réponse.
*(Telnet n'est pas activé par défaut sous Windows : Panneau de configuration → Programmes → Activer ou désactiver des fonctionnalités Windows → Client Telnet.)* Telnet marche ici parce que le protocole est du **texte simple, une ligne par message**.

**Avec des clients Java** : lancer plusieurs fois `activite31.Client`.

**Test réalisé** : 2 clients Java et 1 client en connexion TCP brute (équivalent de Telnet), connectés en même temps.

```
--- Console du serveur ---
Serveur multi-thread en attente de connexions sur /127.0.0.1:1234
Client n°1 connecté : /127.0.0.1:37220
Client n°2 connecté : /127.0.0.1:37232
Client n°3 connecté : /127.0.0.1:37246
[ClientProcess-2] Reçu "7 * 6" -> 7 * 6 = 42
[ClientProcess-1] Reçu "55 * 25" -> 55 * 25 = 1375
[ClientProcess-2] Reçu "5 / 0" -> Erreur : division par zéro impossible
[ClientProcess-1] Reçu "10 / 4" -> 10 / 4 = 2.5
[ClientProcess-3] Reçu "55 * 25" -> 55 * 25 = 1375
Client n°2 déconnecté
Client n°1 déconnecté
[ClientProcess-3] Reçu "10 / 4" -> 10 / 4 = 2.5
Client n°3 déconnecté

--- Client TCP brut / Telnet (client n°2) ---
Bienvenue ! Vous êtes le client n°2
7 * 6 = 42
Erreur : division par zéro impossible

--- Client Java (client n°1) ---
Message du serveur : Bienvenue ! Vous êtes le client n°1
Résultat du serveur : 55 * 25 = 1375
Résultat du serveur : 10 / 4 = 2.5
```

**Interprétation :**
- Les trois clients sont **connectés simultanément** et reçoivent chacun leur numéro (1, 2, 3) : le serveur ne s'arrête plus au premier client.
- Les messages des threads `ClientProcess-1`, `-2` et `-3` sont **entremêlés** dans la console du serveur : preuve que les clients sont servis **en parallèle** et non l'un après l'autre.
- Quand le client n°2 se déconnecte, les clients n°1 et n°3 continuent normalement : les communications sont **indépendantes**.

> **Remarque :** le client Telnet de Windows utilise l'encodage de la console ; les caractères accentués (`é`, `°`) peuvent s'afficher de travers. Cela n'a aucune incidence sur le fonctionnement.

### 4.3 Tests en réseau local

Le client Java utilise `InetAddress` et `InetSocketAddress` pour joindre le serveur à son adresse IP :

```java
socket.connect(new InetSocketAddress(InetAddress.getByName(saisie), Reseau.PORT), 5000);
```

Procédure : (1) relever l'IP de la machine serveur (`ipconfig` / `ip a`), (2) lancer `activite31.Server` sur cette machine, (3) lancer `activite31.Client` sur les autres machines du même réseau en saisissant l'IP du serveur. Le serveur affiche alors des adresses IP **différentes** pour chaque client.

> Si la connexion échoue, le pare-feu de la machine serveur bloque probablement le port 1234 : il faut autoriser Java (ou ce port) en entrée.

---

## 5. Activité 3-2 : Calculatrice multi-thread et accès concurrent

### Étape 1 : Service de calculatrice par objets sur serveur multi-thread

Le service de l'activité 2-2 est intégré au serveur multi-thread, avec une communication **par objets** :

- Le client envoie un objet **`Operation`** (opérande 1, opérateur, opérande 2).
- Le thread `ClientProcess` correspondant le reçoit (`readObject`), calcule, et renvoie un objet **`Resultat`** (`writeObject`).
- À la connexion, le serveur envoie au client son numéro d'ordre sous forme d'objet `Integer`.
- Le client a été mis à jour pour fonctionner selon le même principe : il lit d'abord son numéro, puis envoie des `Operation` et lit des `Resultat`.
- Comme au TP2, chaque côté crée son `ObjectOutputStream` (suivi d'un `flush()`) **avant** son `ObjectInputStream`, pour éviter un blocage mutuel.

```java
// Dans ClientProcess.run()
Object recu = in.readObject();
if (recu instanceof Operation) {
    Operation op = (Operation) recu;
    double r = Calculatrice.calculer(op.getOperande1(), op.getOperateur(), op.getOperande2());
    out.writeObject(Resultat.succes(r));
    out.flush();
}
```

**Tests** : plusieurs clients ont été lancés sur la machine locale (`localhost`), puis sur plusieurs machines du réseau local en saisissant l'IP du serveur (même procédure qu'en 4.3).

### Étape 3 : Gestion d'un accès concurrent

#### Le problème

On introduit un **compteur global** : le nombre total d'opérations traitées par le serveur, **tous clients confondus**. Il est partagé entre tous les threads `ClientProcess` : c'est une **ressource critique**.

Or `compteur++` n'est pas une opération indivisible : elle se décompose en **lire** la valeur, **ajouter 1**, **réécrire** la valeur. Si deux threads font cela en même temps, ils peuvent lire la même valeur et écrire le même résultat : **une incrémentation est perdue**.

#### Démonstration (classe `DemoConcurrence`)

8 threads incrémentent chacun 20 000 fois un compteur, avec et sans synchronisation :

```
Valeur attendue            : 160000
Sans synchronisation       : 80052
Avec synchronized          : 160000
```

Sans synchronisation, près de la moitié des incrémentations sont perdues ; avec `synchronized`, le résultat est exact. *(Le chiffre exact varie d'une exécution à l'autre. Le `Thread.yield()` placé dans la démonstration rend l'entrelacement des threads très probable, même sur un processeur à un seul cœur.)*

#### La solution : `synchronized`

```java
public class CompteurGlobal {
    private static int valeur = 0;

    public static synchronized int incrementer() {
        valeur++;
        return valeur;
    }
}
```

- Le mot-clé **`synchronized`** garantit qu'**un seul thread à la fois** exécute la méthode (verrou sur la classe, puisque la méthode est `static`). Les autres threads attendent leur tour.
- La méthode renvoie la **nouvelle valeur**, lue dans la même section critique : chaque thread obtient donc une valeur **unique**.

Utilisation dans `ClientProcess`, à chaque opération calculée :

```java
int total = CompteurGlobal.incrementer();
System.out.println(prefixe + "Client n°" + numero + " : " + op + " = "
        + Calculatrice.formater(r) + "  |  Compteur global = " + total);
```

> Choix de conception : seules les opérations **effectivement calculées** sont comptées. Une division par zéro, qui n'est pas calculée, n'incrémente pas le compteur.

#### Test avec plusieurs clients en parallèle

5 clients lancés simultanément ; chacun envoie 20 opérations (`i * k`) puis une division par zéro (`5 / 0`), soit 100 opérations valides et 5 erreurs au total.

```
--- Console du serveur (début) ---
Serveur calculatrice multi-thread en attente de connexions sur /127.0.0.1:1234
Client n°1 connecté : /127.0.0.1:51966
Client n°2 connecté : /127.0.0.1:51968
Client n°3 connecté : /127.0.0.1:51978
Client n°4 connecté : /127.0.0.1:51988
Client n°5 connecté : /127.0.0.1:51994
[ClientProcess-5] Client n°5 : 1 * 4 = 4  |  Compteur global = 4
[ClientProcess-4] Client n°4 : 1 * 1 = 1  |  Compteur global = 5
[ClientProcess-1] Client n°1 : 1 * 2 = 2  |  Compteur global = 1
[ClientProcess-2] Client n°2 : 1 * 5 = 5  |  Compteur global = 2
[ClientProcess-3] Client n°3 : 1 * 3 = 3  |  Compteur global = 3
[ClientProcess-2] Client n°2 : 2 * 5 = 10  |  Compteur global = 6
...

--- Console du serveur (fin) ---
[ClientProcess-1] Client n°1 : 5 / 0 -> Erreur : division par zéro impossible (non compté)
[ClientProcess-3] Client n°3 : 5 / 0 -> Erreur : division par zéro impossible (non compté)
[ClientProcess-5] Client n°5 : 5 / 0 -> Erreur : division par zéro impossible (non compté)
[ClientProcess-4] Client n°4 : 5 / 0 -> Erreur : division par zéro impossible (non compté)
Client n°2 déconnecté
...

--- Client n°3 ---
Connecté : vous êtes le client n°3
Résultat du serveur : 1 * 3 = 3
Résultat du serveur : 2 * 3 = 6
Résultat du serveur : 3 * 3 = 9
...
Résultat du serveur : Erreur : division par zéro impossible
```

**Vérification de la cohérence du compteur** (analyse du journal du serveur) :

| Vérification | Résultat |
|---|---|
| Nombre de lignes « Compteur global » | 100 |
| Valeurs distinctes du compteur | 100 |
| Valeur finale (maximale) | 100 |
| Toutes les valeurs de 1 à 100 présentes, sans doublon ni trou | Oui |
| Erreurs non comptées | 5 |
| Threads distincts ayant travaillé | 5 |

**Interprétation :**
- Le compteur atteint **exactement 100** : aucune incrémentation n'est perdue et aucune valeur n'est attribuée deux fois. Le compteur reste **cohérent** malgré les 5 clients simultanés.
- Les 5 divisions par zéro ne sont pas comptées, comme prévu.
- Les clients ont bien reçu les numéros d'ordre 1 à 5.
- On remarque que dans la console, « Compteur global = 4 » apparaît **avant** « Compteur global = 1 ». Ce n'est pas une erreur : l'incrémentation est atomique (chaque thread obtient une valeur unique), mais l'affichage a lieu **après** la sortie de la section critique ; un thread peut donc afficher sa valeur un peu plus tard qu'un autre. Les valeurs elles-mêmes sont correctes.

**Test sur plusieurs machines :** même procédure qu'en 4.3. Le compteur global reste unique, car il vit dans le **processus serveur**, quelle que soit la machine du client.

---

## 6. Conclusion

Ce TP m'a permis de comprendre et d'appliquer le principe d'un serveur multi-threads en Java :

- le thread principal se limite à **accepter** les connexions (`accept()`), et confie chaque client à un **thread dédié** (`ClientProcess`), ce qui permet de servir plusieurs clients **simultanément** avec des communications **indépendantes** ;
- le numéro d'ordre et l'adresse de chaque client permettent de les **identifier** côté serveur ;
- dès que des threads **partagent une ressource** (ici le compteur global), il faut **synchroniser** l'accès (`synchronized`) pour éviter les incohérences dues à l'accès concurrent, comme l'a montré la démonstration ;
- les applications restent utilisables en **réseau local**, avec `InetAddress` et `InetSocketAddress`.

## 7. Exécution du projet

```bash
# Compilation
javac -encoding UTF-8 -d out src/commun/*.java src/activite31/*.java src/activite32/*.java

# Activité 3-1  (terminal 1 = serveur, puis plusieurs terminaux = clients)
java -cp out activite31.Server
java -cp out activite31.Client

# Activité 3-2
java -cp out activite32.Server
java -cp out activite32.Client

# Démonstration de l'accès concurrent
java -cp out activite32.DemoConcurrence
```

Le serveur détecte automatiquement l'IP de la machine (ou on peut la donner en argument). Le client demande l'IP du serveur au démarrage (Entrée = `localhost`). Les deux activités utilisent le port 1234 : n'en lancer qu'une à la fois.

**Dépôt GitHub :** https://github.com/Waelriahii/TP3-DAR2026

# INF8480 TP2

## Configuration initiale

Ouvrer un terminal pour chaque ordinateur qu'on veut avec SSH:

```
ssh L4712-XX
```

On veut un ordinateur pour chaque client, serveur de calcul et serveur de nom (4 ordinateurs pour 2 serveurs de calcul).

Aller dans le dossier du projet et compiler avec ant:

```
[silixa@l4712-16 inf8480_tp2 (master)] $ ant
Buildfile: /usagers3/silixa/inf8480_tp2/build.xml

init:

build-class:

build-jar:

BUILD SUCCESSFUL
Total time: 0 seconds
```

## Démarrer le serveur de noms

**Important: redémarrer le serveur de noms chaque fois qu'on arrête les serveurs. Le serveur de noms ne détecte pas si un serveur n'existe plus.**

Sur le poste qu'on va utiliser comme serveur de noms, on va trouver l'adresse IP de l'ordinateur:

```
[silixa@l4712-16 inf8480_tp2 (master)] $ hostname -i
132.207.12.48
```

On va maintenant débuter le serveur de noms avec l'adresse IP et le port qui va être hôte du rmiregistry (utiliser 5001):

```
./nameService.sh ip_address port
```

où `ip_address` = l'adresse trouvé avec `hostname -i` et `port` = le port du rmiregistry (5001).

exemple:
```
[silixa@l4712-16 inf8480_tp2 (master)] $ ./nameService.sh 132.207.12.48 5001
HELP:
./nameService.sh ip_address port
NameService ready
```

## Démarrer les serveurs

Sur chacun des ordinateurs qu'on veut rouler les serveurs de calcul, trouver son addresse IP:

```
[silixa@l4712-15 inf8480_tp2 (master)] $ hostname -i
132.207.12.47
```

Démarrer le serveur avec:

```
./server.sh ip_address port q m
```

où `ip_address` est l'adresse IP trouvée à l'étape précédente, `q` est la capacité à 100% d'acceptation du serveur et `m` est le taux de résultats malicieux (0 à 100). *Utiliser un port différent du port du serveur de noms (ex. 5003).*

**Exemple mode sécurisé (0% malicieux)**:

```
[silixa@l4712-15 inf8480_tp2 (master)] $ ./server.sh 132.207.12.47 5003 5 0
HELP: 
./server.sh ip_address port q m

Server ready.
Registered
```

**Exemple mode non-sécurisé (40% malicieux)**:

```
[silixa@l4712-15 inf8480_tp2 (master)] $ ./server.sh 132.207.12.47 5003 5 40
HELP: 
./server.sh ip_address port q m

Server ready.
Registered
```

**Attention: les serveurs doivent être sur des ordinateurs différents!**

## Utiliser le répartiteur

**Faire attention de rouler le client en mode sécurisé ou non, sinon les résultats ne seront pas corrects.**

Finalement, sur l'ordinateur qui roule le client (différent du serveur de noms et des serveurs de calcul), exécuter:


**Mode sécurisé**

```
./client.sh file
```

où `file` est le fichier d'opérations à effectuer.

exemple:

```
[silixa@l4712-12 inf8480_tp2 (master)] $ ./client.sh operations-216
HELP: 
./client.sh file flag

216
Execution time: 264
```

*Lorsqu'on omet le `flag`, le répartiteur va rouler en mode sécurisé.*

**Mode non-sécurisé**

```
./client.sh file m
```

où `file` est le fichier d'opérations à effectuer et `m` est un indicateur qu'on est en mode non-sécurisé.

exemple:

```
[silixa@l4712-12 inf8480_tp2 (master)] $ ./client.sh operations-216 m
HELP: 
./client.sh file flag

216
Execution time: 578
```

Ça devrait afficher le résultat du calcul ainsi que le temps d'exécution.
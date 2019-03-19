# INF8480 TP2

## Configuration initiale

### IMPORTANT: À faire avant les tests

Ouvrez un terminal pour chacun des postes suivantes avec SSH:

- L4712-27
- L4712-26
- L4712-25
- L4712-24
- L4712-23
- L4712-22

```
ssh L4712-XX
```

Va dans le dossier du projet et compilez avec ant:

```
ant
```

Sur le poste qu'on va utiliser comme serveur de noms (L4712-27), on va trouver l'adresse IP de l'ordinateur:

```
hostname -i
```

Modifiez le fichier `nameService.config` dans le projet. Il devrait contenir une ligne avec l'adresse IP et le port (entre 5000 et 5050) de l'ordinateur qui va être hôte du serveur de noms. Il aurait l'air de:

```
132.207.12.59:5001
```

Sur l'ordinateur du serveur de noms (L4712-27), fait le commande suivante:

```
./nameService.sh ipAddress
```

où `ipAddress` est ce qu'on a trouvé avec `hostname -i` précédemment.

L'écran devrait afficher `NameService ready`

## Rouler les tests de performance - mode sécurisé

Redémarrez l'étape qui débute le NameService si vous avez arrêté des serveurs.

Sur chacun des ordinateurs qu'on veut rouler les serveurs de calcul, fait un:

```
./server.sh ipAddress port q
```

où q est la capacité à 100% d'acceptation du serveur.

exemple:

```
./server.sh 132.207.12.58 5003 5
```

**Les serveurs doivent être sur des ordinateurs différents!**

Finalement, sur l'ordinateur qui roule le client (différent du serveur de noms et des serveurs de calcul), fait:

```
./client.sh operations-X
```

où X est la terminaison d'un fichier operations.

exemple:

```
./client.sh operations-216
```

Ça devrait afficher le résultat du calcul ainsi que le temps d'exécution.

## Rouler les tests de performance - mode non-sécurisé

Redémarrez l'étape qui débute le NameService si vous avez arrêté des serveurs.

Sur chacun des ordinateurs qu'on veut rouler les serveurs de calcul, fait un:

```
./server.sh ipAddress port q m
```

où q est la capacité à 100% d'acceptation du serveur et m est le taux de résultats malicieux (entre 0 et 100).

exemple:

```
./server.sh 132.207.12.58 5003 5 40
```

**Les serveurs doivent être sur des ordinateurs différents!**

Finalement, sur l'ordinateur qui roule le client (différent du serveur de noms et des serveurs de calcul), fait:

```
./client.sh operations-X m
```

où X est la terminaison d'un fichier operations et m est un indicateur que l'algorithme doit gérer les résultats malicieux.

exemple:

```
./client.sh operations-216 m
```

Ça devrait afficher le résultat du calcul ainsi que le temps d'exécution.


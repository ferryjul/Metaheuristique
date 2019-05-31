RAPPORT DE PROJET
============
###Julien Ferry
###Quentin Genoud
###4IR-A


# Introduction

# Vérifications des solutions

##Algorithme du checker 

L'algorithme fonctionne en 2 temps. On va d'abord simuler l'évacuation en calculant, pour la solution proposée, la capacité restante sur chaque arc et à chaque instant. 
En ce faisant, on vérifie le respect des contraintes de capacité de chaque arc (ou ressource) et la réalisation de la fonction objectif. 
Dans un second temps, en utilisant la matrice remplie précédemment, on vérifie le respect des due date de chaque arc, en vérifiant que chaque arc ne soit plus utilisé après sa due date.

Tout d'abord, voici les **abréviations** utilisées :

- **T** = nombre d'unités de temps de la fonction objectif
- **E** = nombre d'arcs dans le graphe des routes d'évacuations
- **L** = longueur maximale d'un chemin d'évacuation
- **N** = nombre de sommets à évacuer
- **P** = nombre maximal de paquets de personnes devant quitter un sommet . 

Dans le cas général P(s) = ( (#personnes en s à t = 0) / (flot max d'évacuation de s) )
Pour rester dans le cas général on peut poser (dans le pire des cas) : P = nombre max de personnes sur un sommet (mais c'est une approximation très défavorable)

Soit edgesData, une matrice de la taille (T)*(E).

edgeData[t][e] représente la capacité restante pour l'arc (la ressource) e au temps t.

**Init** : (O(T*E))
Pour chaque case de edgesData mettre la capacité initiale de l'arc concerné

**Vérification de la contraine de capacité :** (O(N*P*L))
Pour chaque sommet s à évacuer :
	tInit <- début de l'évacuation de s
	Tant qu'il reste des gens à évacuer en s :
		t <- tInit
		Pour chaque arc e du chemin d'évacuation de s :
			si t > valeur de la fonction objectif alors solution invalide finsi (1)
			edgesData[t][e] -= nombre de personne du paquet évacué
			si edgesData[t][e] < 0 alors Solution invalide finsi (2)
			t += durée de parcours de e
		finPour
		tInit++
	fintantque
finpour
		
**Vérification de la contrainte des Due Date :** (O(E*T))
Pour chaque arc e du graphe :
	pour i allant de (date d'expiration de e) à (valeur de la fonction objectif) 
		si edgesData[i][e] != capacité de e
			alors solution invalide (3)
		finsi
	finpour
finpour

**(1)** La solution ne permet pas d'évacuer tout le monde en respectant la valeur voulue de la fonction objectif.

**(2)** La solution ne respecte pas les contraintes de capacité de chaque arc (ou ressource).

**(3)** La solution ne respecte pas les due date : des gens entrent encore sur un arc après sa date d'expiration.


- illustration par l'exemple (optionnel)
- complexité de l'algo
- intêret de cette méthode de vérification pour le problème étudié



# Calculs de borne inférieure et supérieure

## 1) Borne inférieure

Le problème à résoudre est un problème de minimisation, donc la borne inférieure est une valeur de la fonction objectif telle que la "vraie" valeur ne lui sera jamais inférieure (i.e. la solution réalisable ne sera jamais meilleure).
La borne inférieure que nous avons retenue est la suivante : c'est le maximum des temps d'évacuation des tronçons. On considère que les secteurs peuvent être évacués simultanément, le temps total est donc le temps de l'évacuation la plus longue, c'est le meilleur des cas.

- algo
- illustration par l'exemple (optionnel)
- complexité de l'algo
- résultats cohérents ?



## 2) Borne supérieure

Une borne supérieure est une valeur de la fonction objectif telle que la valeur optimale de la fonction objectif ne lui sera pas supérieure (i.e. la solution idéale sera forcément meilleure) et la solution générée doit être réalisable.
Notre borne supérieure est la somme d'évacuation de chaque noeud à évacuer individuellement. On considère que les secteurs sont évacués chacun à leur tour et qu'un secteur ne peut pas commencer à évacuer tant que le secteur en cours d'évacuation n'a pas fini d'évacuer tous ses habitants jusqu'au sommet sécurisé.
Cette méthode génère donc bien une borne supérieure, dont il est facile de prouver la validité, mais dont la valeur de la fonction objectif est assez grossière. Une méthode plus fine pourrait permettre de paralléliser les évacuations dont les chemins n'ont aucun arc en commun.

- algo
- illustration par l'exemple (optionnel)
- complexité de l'algo
- résultats cohérents ?

## Tableau de résultats (complété par graphiques)

# Intensification


Notre cycle d'intensification se déroule comme ci dessous :
On part d'une solution de départ valide, puis on effectue les étapes suivantes :
**Compactage** -> **Réduction des débits** ->**Compactage** -> **Augmentation des débits** -> **Compactage**

## Présentation des voisinages

Une solution est totalement spécifiée par :
- *la date de début d'évacuation de chaque noeud à évacuer*
- *le débit d'évacuation de chaque noeud à évacuer*

Les différentes étapes de notre cycle d'intensification explorent différents voisinages :

| Etape du cycle d'intensification 	| Solutions "voisines" de la solution courante (valeurs des débits et dates de début d'évacuation comparées à celles de la solution courante) 	|
|----------------------------------	|---------------------------------------------------------------------------------------------------------------------------------------------	|
| Compactage                       	| Solutions ayant des débits d'évacuation identiques,  mais une ou plusieurs dates de début d'évacuation inférieures                          	|
| Augmentation des débits          	| Solutions ayant des dates de début d'évacuation identiques, mais des débits d'évacuation potentiellement plus élevés                        	|
| Réduction des débits             	| Solutions ayant un ou plusieurs débits d'évacuation diminués, et une ou plusieurs dates d'évacuation augmentées                             	|


## Méthodes d'exploration des voisinages
//préciser, conditions d'arrêts, exploration des voisinages, plusieurs fonction d'évaluation ?

Cycle de **compactage** : On essaye de faire partir chaque groupe associé à un site de départ le plus tôt possible. Quand il n'est pas possible de diminuer la date de départ de l'un des sites (car on obtient pas une solution valide pour le checker), on essaye de diminuer la date de départ d'un autre site. On répète cela jusqu'à ce que l'on ne puisse plus du tout diminuer la date de départ de tous les sites sans obtenir de solution valide ou quand les dates de départ de chaque site sont t=0. 
*Note sur la performance : diminuer les dates d'évacuation unité de temps par unité de temps, et lancer une analyse du checker à chaque étape est bien sûr inconcevable d'un point de vue des performances. Aussi, nous calculons, au début du cycle, un facteur de diminution (égal initialement à la moitié de la valeur de la fonction objectif de la solution courante). Nous tentons de diminuer les dates d'évacuation de cette valeur. Lorsque ce n'est plus possible, nous divisons cette valeur par 2 et répétons le procédé jusqu'à ce qu'aucune solution ne soit possible et que le facteur de diminution soit égal à 1.*

Cycle de **réduction des débits** : On diminue la taille des débits (paquets de personnes) de certains sites de départ. Pour ce faire, on détermine les tronçons limitants puis on détermine les secteurs qui entrent en conflit sur ces tronçons. On diminue ensuite progressivement le débit de chaque secteurs en conflit individuellement (on obtient plusieurs solutions différentes). On fait aussi une solution où l'on diminue le débit de manière équivalente entre tous les secteurs en conflits. Si la solution obtenue est invalide, on stoppe la diminution du débit immédiatement pour le secteur en question sinon on continue. Une fois des solutions invalides atteintes pour tous les secteurs, on conserve la plus forte diminution pour chaque secteur (on a donc autant de solutions qu'il y avait de secteurs en conflits plus la solution où l'on réparti la diminution du débit sur tous les secteurs équitablement). On effectue un nouveau cycle de compression et on garde la meilleure de toutes les solutions (si il y en a plusieurs, on garde la première générée).

Remaque : réduire le débit d'évacuation entraîne une augmentation de la durée d'évacuation pour le noeud concerné. C'est pourquoi nous calculons la valeur exacte, en unités de temps, de cette augmentation et nous retardons les évacuations suivantes d'autant.

Cycle **d'augmentation des débits** : On part d'une solution et on essaye d'augmenter les débits sur tous les arcs jusqu'à que les solutions deviennent invalides (capacité des arcs dépassées par exemple). On refait ensuite un cycle de compression car il est possible qu'en ayant augmenté les débits on ait rendu réalisable une diminution des dates de départ pour certains noeuds.

présentation de résultats sur différentes instances (tableau voir graphique (optionnel))

## Analyse des voisinages : taille et complexité de l'exploration

| Etape du cycle d'intensification 	| Taille du voisinage                                                                                   	| Complexité de l'exploration                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                	|
|----------------------------------	|-------------------------------------------------------------------------------------------------------	|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	|
| Compactage                       	| `O(N)` si on ne s'intérresse qu'aux compactages "maximaux" (où `N` est le nombre de sommets à évacuer)    	|  `O(N*log(s)*complexité(Checker))` car pour atteindre les voisins on explore des solutions intermédaires, dans le pire des cas `log(s)` où `s` est la valeur de la fonction objectif initiale NB : Le logarithme vient du fait qu'on divise notre facteur par deux à chaque itération                                                                                                                                                                                                                                                                            	|
| Augmentation des débits          	| `O(N)` si on ne s'intérresse qu'aux augmentations "maximales" (où `N` est le nombre de sommets à évacuer) 	| `O(N*log(k)*complexité(Checker))` car pour atteindre les voisins on explore des solutions intermédaires, dans le pire des cas `log(k)` où `k` est la valeur initiale du facteur d'augmentation NB (1) : Le logarithme vient du fait qu'on divise notre facteur par deux à chaque itération NB (2) : Ici `log(k)` est donc une constante donc on pourrait donner une complexité théorique de `O(N*complexité(Checker))`                                                                                                                                               	|
| Réduction des débits             	| `O(N)` où `N` est le nombre de sommets à évacuer                                                          	| `O(N+complexité(Checker))` car : - on lance le Checker une fois pour connaitre l'arc limitant et les `L` noeuds qui l'utilisent au moment où sa capacité est violée - on génère ensuite `L+1` solutions en diminuant tour à tour les débits de chaque noeud d'évacuation empruntant l'arc limitant au moment critique (`L` solutions pour lesquelles on ne diminue le débit que pour un seul noeud d'évacuation et une autre pour laquelle on partage la diminution entre tous les `L` noeuds concernés) NB : Dans le pire cas `L = N`, d'où la complexité retenue ici 	|


En conclusion, lors de notre cycle d'intensification, la seule opération impossible sur la solution de départ est l'augmentation des dates de départ. En particulier, si une évacuation débute à t=0, alors ce sera toujours le cas dans les solutions explorées par notre cycle d'évacuation. C'est donc notamment sur cet aspect que nous avons axé notre processus de diversification.

# Diversification
<<<<<<< Updated upstream

Tout d'abord, il faut noter que la condition d'arrêt du cycle complet d'intensification (voir la partie précédente) s'arrête lorsque l'on arrive pas à obtenir une meilleure solution. Mais on peut s'autoriser à itérer un certain nombre de fois supplémentaires (modifiable) avant l'arrêt même lorsqu'on arrive pas à trouver une meilleur solution de suite. 

Ensuite, le processus de diversification que nous avons choisi d'appliquer est le **multi-start**. Cela consiste tout simplement à choisir plusieurs ordres d'évacuation différents, à tous les explorer et à choisir le meilleur des résultats à la fin. On choisit en fait un ordre d'évacuation des secteurs de départ totalement aléatoire (par exemple, ordre 1 d'évacuation des secteurs : 1->2->3->4, ordre 2 : 3->1->4->2 ...).
On peut choisir le nombre de séquence d'évacuations différentes que l'on veut explorer. Comme les séquences générées sont aléatoires, on peut donc tomber sur 2 ordres identiques mais si le nombre de noeuds de départs différents est important ça devient très rare. 
Le problème du multi-start, c'est que l'on applique notre cycle d'intensification autant de fois que le nombre de séquences d'évacuations différentes que l'on souhaite explorer. Cela peut donc prendre pas mal de temps surtout sur les instances les plus volumineuses. Pour améliorer la vitesse d'éxecution de notre algorithme, on a décidé de pouvoir utiliser différents threads pour pouvoir paralléliser les calculs des différentes solutions lorsqu'on utilise le multi-start. On peut désactiver cette fonctionnalité simplement avec un paramètre et on peut même choisir le nombre de threads qui tournent en simultanés.

Tableau de résultats
=======
 La condition d'arrêt du cycle complet d'intensification => on trouve pas de meilleurs solution, on s'autorise à itérer un certains nombre de fois (paramètrable) avant l'arrêt.

# Perforormances

Cette section présente les résultats obtenus pour 4 instances du problème : l'exemple simple du TD, une instance "sparse", une "medium", et une "dense". Pour chacune de ces instances, nous présentons le temps total de calcul, la valeur de la fonction objectif pour la solution obtenue, pour :
- un cycle d'intensification sans diversification : pas de multi-start et on s'arrête dès qu'on atteint un minimum local
- notre recherche locale (diversification et intensification) pour différents nombres de points de départ multi-start


>>>>>>> Stashed changes

# Conclusion

##Possibles améliorations
Nous pourrions améliorer plusieurs étapes de notre algorithme. La principale amélioration possible concerne la fonction modifyRates. Cette fonction, qui prend en argument une solution invalide du point de vue des capacités, trouve la ressource limitante (c'est à dire l'arc sur lequel la contrainte de capacité n'est pas respectée), et génère plusieurs solutions en diminuant le débit d'évacuation des noeuds utilisant la ressource au moment du conflit. Bien que les évacuations suivant celle dont le débit est réduit soient décalées avant de compenser l'allongement de sa durée, cette méthode génère parfois des solutions invalides. En effet, l'allongement de la durée d'évacuation d'un noeud entraine une utilisation prolongée de toutes les ressources du chemin d'évacuation (et pas seulement de la ressource identifiée plus tôt). Il serait bien sûr possible de ne générer que des solutions faisables, soit par une analyse plus profonde des modifications à apporter, soit en décalant plus fortement les évacuations concourantes. Néanmoins, la première idée entrainerait un allongement conséquent de la durée d'exécution de modifyRates, tandis que la deuxième ralentirait le cycle de compactage suivant chacune des solutions générées. Pour des raisons de performances, nous avons donc choisi de conserver notre méthode, bien qu'elle génère parfois des solutions inexploitables.
# Référence

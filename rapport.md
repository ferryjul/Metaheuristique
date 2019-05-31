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

Tout d'abord, voici les **abréviations** utilisée :

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

## 1) Borne inférieur

Le problème à résoudre est un problème de minimisation, donc la borne inférieure est une valeur de la fonction objectif telle que la "vraie" valeur ne lui sera jamais inférieure (i.e. la solution réalisable ne sera jamais meilleure).
La borne inférieure que nous avons retenue est la suivante : c'est le maximum des temps d'évacuation des tronçons. On considère que les secteurs peuvent être évacués simultanément, le temps total est donc le temps de l'évacuation la plus longue, c'est le meilleur des cas.

- algo
- illustration par l'exemple (optionnel)
- complexité de l'algo
- résultats cohérents ?



## 2) Borne supérieure

Une borne supérieure est une valeur de la fonction objectif telle que la valeur optimale de la fonction objectif ne lui sera pas supérieure (i.e. la solution idéale sera forcément meilleure) et la solution générée doit être réalisable.
Notre borne supérieur est la somme d'évacuation de chaque tronçon individuellement. On considère que les secteurs sont évacués chacun leur tour et qu'un secteur ne peut pas commencer à évacuer tant que le secteur en cours d'évacuation n'a pas fini d'évacuer tous ses habitants jusqu'au sommet sécurisé.

- algo
- illustration par l'exemple (optionnel)
- complexité de l'algo
- résultats cohérents ?

## Tableau de résultats (complété par graphiques)

# Intensification

## Voisinages

Les voisinages choisis sont
- illustration exemple (optionnel)
- taille des voisinages
- complexité des fonctions d'évaluation

## Algorithme d'intensification
//préciser, conditions d'arrêts, exploration des voisinages, plusieurs fonction d'évaluation ?

Notre intensification se déroule comme ci dessous :
On part de la **bonne supérieure** puis on effectue les cycles ci-dessous:
**Compactage** -> **Réduction des débits** ->**Compactage** -> **Augmentation des débits** -> **Compactage**

Cycle de **compactage** : On essaye de faire partir chaque groupe associé à un site de départ le plus tôt possible. Quand on arrive pas à diminuer la date de départ de l'un des sites (car on obtient pas une solution valide pour le checker), on essaye de diminuer la date de départ d'un autre site. On répète cela jusqu'à ce que l'on ne puisse plus du tout diminuer la date de départ de tous les sites sans obtenir de solution valide (on réessaye les sites auquel on déjà diminuer la date de départ antérieurement) ou quand les dates de départ de chaque site sont t=0.

Cycle de **réduction des débits** : On diminue la taille des débits (paquets de personnes) de certains sites de départ. Pour ce faire, on détermine les tronçons limitants puis on détermine les secteurs qui entrent en conflit sur ces tronçons. On diminue ensuite progressivement le débit de chaque secteurs en conflit individuellement (on obtient plusieurs solutions différentes). On fait aussi une solution où l'on diminue le débit de manière équivalente entre tous les secteurs en conflits. Si la solution obtenue est invalide, on stoppe la diminution du débit immédiatement pour le secteur en question sinon on continue. Une fois des solutions invalides atteintes pour tous les secteurs, on conserve la plus forte diminution pour chaque secteur (on a donc autant de solutions qu'il y avait de secteurs en conflits plus la solution où l'on réparti la diminution du débit sur tous les secteurs équitablement). On effectue un nouveau cycle de compression et on garde la meilleure de toutes les solutions (si il y en a plusieurs, on garde la première générée).

Remaque : réduire le débit d'évacuation, entraîne une augmentation de la durée d'évacuation pour le noeud concerné. C'est pourquoi nous calculons la valeur exacte, en unité de temps de cette augmentation et nous retardons les évacuations suivantes d'autant.

Cycle **d'augmentation des débits** : Part d'un solution et on essaye d'augmenter les débits sur tous les arcs jusqu'à que les solutions deviennent invalides (capacité des arcs dépassés par exemple). Une fois cela fait un refait un cycle de compression car il est possible que cela puisse améliorer notre solution.

présentation de résultats sur différentes instances (tableau voir graphique (optionnel))

# Diversification
 La condition d'arrêt du cycle complet d'intensification => on trouve pas de meilleurs solution, on s'autorise à itérer un certains nombre de fois (paramètrable) avant l'arrêt.
# Conclusion

##Possibles améliorations
Nous pourrions améliorer plusieurs étapes de notre algorithme. La principale amélioration possible concerne la fonction modifyRates. Cette fonction, qui prend en argument une solution invalide du point de vue des capacités, trouve la ressource limitante (c'est à dire l'arc sur lequel la contrainte de capacité n'est pas respectée), et génère plusieurs solutions en diminuant le débit d'évacuation des noeuds utilisant la ressource au moment du conflit. Bien que les évacuations suivant celle dont le débit est réduit soient décalées avant de compenser l'allongement de sa durée, cette méthode génère parfois des solutions invalides. En effet, l'allongement de la durée d'évacuation d'un noeud entraine une utilisation prolongée de toutes les ressources du chemin d'évacuation (et pas seulement de la ressource identifiée plus tôt). Il serait bien sûr possible de ne générer que des solutions faisables, soit par une analyse plus profonde des modifications à apporter, soit en décalant plus fortement les évacuations concourantes. Néanmoins, la première idée entrainerait un allongement conséquent de la durée d'exécution de modifyRates, tandis que la deuxième ralentirait le cycle de compactage suivant chacune des solutions générées. Pour des raisons de performances, nous avons donc choisi de conserver notre méthode, bien qu'elle génère parfois des solutions inexploitables.
# Référence

# Metaheuristiques - Projet de Julien Ferry et Quentin Genoud (INSA - 4ème année IR)

Pour compiler : 
>javac *.java

Pour lancer la recherche locale : 
>java Test

## Fonctions implémentant les fonctionalités requises

### Lecture d'un jeu de données
Un jeu de données est représenté par la classe `data`.
La classe `dataReader` avec sa méthode `public data Convert_File_In_Data(String File_Name)` permet de lire un fichier de données pour créer une instance de `data`.

Pour lire le jeu de données "exemple.full" :
> dataReader reader = new dataReader();
> data d = reader.Convert_File_In_Data("exemple.full");

Pour l'afficher dans la console :
> d.read_data();

### Lecture/écriture d'une solution
Une solution est représentée par la classe `Solution`.
La classe `SolutionIO` permet :
- de lire un fichier solution avec sa méthode `public Solution read(String File_Name)`
- d'écrire un fichier solution avec sa méthode `public int write(Solution sol, String File_Name)`

Pour lire la solution contenue dans le fichier "solutionExemple" :
> SolutionIO solIO = new SolutionIO();
> Solution sol = solIO.read("solutionExemple");

Pour écrire dans le fichier "solutionécrite.txt" la solution sol :
>int test = solIO.write(TD_sol, "solutionécrite.txt");
        if (test==0) { System.out.println("Fichier solution créé \n");}
        else { System.out.println("[ERROR] Fichier solution fail \n");}

### Vérifieur de solutions
Le vérifieur de solution est implémenté par la classe `Checker`. Sa configuration (et notamment pour prendre en considération les due date des ressources) est expliquée plus en détails dans la section "Configuration de nos programmes".
Il comporte deux constructeurs : 
- `public Checker()` : le checker va uniquer vérifier la validité de la solution. Il va remplir uniquement le champ `objectiveValue` (valeur de la fonction objectif si la solution est valide, -1 sinon) de la classe `CheckerReturn`. Ceci permet d'éviter des itérations non nécessaires.
-  `public Checker(boolean com)` : si `com` vaut `true`, le Checker va remplir tous les champs de la classe `CheckerReturn` si la solution est invalide.

Pour vérifier la validité de la Solution sol pour le jeu de données d :
> Checker ch = new Checker();
> CheckerReturn chR = ch.check(TD_data, infSol);
> if(chR.objectiveValue != -1) { System.out.println("Solution valide !"); }
> else { System.out.println("Solution invalide !"); }

### Calculs des bornes inférieures et supérieures
La classe `ComputeInfSup` implémente les calculs de bornes inférieures et supérieures.

Les méthodes `public static int computeInf(data inData)` et `public static int computeSup(data inData)` permettent respectivement de calculer la valeur de la borne inférieure et supérieure des solutions pour le jeu de données inData, tandis que les méthodes `public static Solution computeInfSolution(data inData)` et `public static Solution computeSupSolution(data inData)` génèrent et retournent les solutions associées à ces valeurs.

### Recherche locale
La classe `LocalSearch` contient les méthodes nécessaires à la recherche locale (intensification + diversification). Elle génère et enregistre les solutions trouvées. **Plusieurs options de configuration de notre recherche locale sont possibles, et expliquées dans la section suivante.** Elle affiche également des informations sur le déroulement du cycle, et, le cas échéant, sur les threads actifs.

Pour lancer la recherche locale pour le jeu de données `d` (instance de la classe `data`), pour l'instance au nom "NomInstance" (paramètre utilisé pour la génération du nom du fichier solution) :
> (new LocalSearch()).localSearch(d, "NomInstance");

Si l'utilisateur fait le choix d'utiliser le multi-threading, les Threads seront créés automatiquement par `public void localSearch(data d, String name)` en instanciant la classe `localSearchCalculations`, qui contient les mêmes méthodes que `LocalSearch`, organisées pour respecter la structure de l'interface `Runnable`.

## Configuration de nos programmes
Afin de rendre nos programmes facilement configurables, nous avons, pour plusieurs fichiers, mis en place plusieurs variables globales dont la modification permet de décider le comportement de nos algorithmes. Les variables les plus utiles sont présentées ici.

* `int debug` : présente dans plusieurs de nos fichiers, cette variable peut prendre les valeurs suivantes :
- 0 : pas d'affichage dans la console
- 1 : affichage minimal dans la console
- 2 : affichage de toutes les informations dans la console
- 3 : affichage détaillé des informations dans la console

* `Boolean checkDueDate` (fichier `Checker.java`) : indique à l'instance correspondante du Checker s'il doit vérifier la contrainte de due date des arcs ou non. Pour tenir compte de ces contraintes dans la recherche locale, IL NE FAUT PAS UTILISER CE PARAMETRE, mais celui ci-après (qui configure en fait celui-ci automatiquement).

* `Boolean respDueDates` (fichier `localSearch.java`) : S'il vaut vrai alors les solutions vont être générées en tenant compte de la contrainte de due date. Il est à noter que cette contrainte n'est vérifiée qu'après un cycle de compression, car les solutions que nous utilisons comme solutions initiales ne respectent en général pas ces contraintes. S'il vaut faux alors les solutions générées tiendront uniquement compte des contraintes de capacité.

* `int multiStartNbPoints` (fichier `localSearch.java`) : indique le nombre de séquences aléatoires que le programme va générer. Pour la valeur 0, seule une solution sera générée et intensifiée : il s'agira donc d'intensification "pure".

* `Boolean useMultiThreading` (fichier `localSearch.java`) : indique si les calculs doivent se faire ou non sur plusieurs threads.

* `int nbThreads` (fichier `localSearch.java`): dans le cas où l'utilisateur choisit d'utiliser le multithreading, indique le nombre maximum de threads qui peuvent s'exécuter en parallèle.

## Utilisation rapide de nos programmes
**Pour utiliser nos programmes simplement :**
Le fichier `Test.java` permet de lancer simplement la recherche locale sur une instance. Le calcul des valeurs d'une borne inférieure et d'une borne supérieure est également lancé par ce fichier.  Si `Boolean debug` vaut `true`, la recherche locale s'exécute pour l'exemple simple du TD. S'il faut `false`, alors l'instance résolue par la recherche locale est celle dont le nom est contenue dans la variable `String inst`. A noter que les instances doivent être contenues dans le répertoire `InstancesInt/` de notre repository, et que les fichiers générés le seront au format exigé, dans le dossier `Generated_best_solutions` sous le nom `nomInstanceAAAAMMJJHH:mm:ss` (où `AAAAMMJJHH:mm:ss` est la date de fin de l'exécution de notre recherche locale).

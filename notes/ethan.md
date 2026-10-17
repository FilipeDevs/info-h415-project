# Notes : bases de données orientées objet

## Synthèse

### Général

- Proche des langages de programmation orientés objets.
- Chaque objet dans la bdd a un identifiant unique permettant de facilement retrouver l'objet après son enregistrement.
- Dans la vraie vie : les bases de données relationnelles sont plus utilisées, même avec l'utilisation de langages de programmation orientés objets car très peu de SGBD orientés objet (moins de documentation, moins répandu, etc).
- Dans certains cas, nécessité de décrire des objets complexes.
- Un objet complexe est un objet qui n’est pas caractérisé par une seule valeur, mais par un ensemble structuré de valeurs.

### Avantages

- Toutes les informations de l'objet sont immédiatement disponibles.
- Avantageux lors de l'utilisation de langages de programmations orientés objets. Les objets du code peuvent être directement repris dans la base de données. Plus compliqué avec une base de données relationnelles.
- La complexité des objets vise à exécuter les demandes et les écritures complexes bien plus rapidement qu’avec un modèle relationnel. (Montrer avec les benchmarks)
- Enregistrement et interrogation rapide et simple.
- Les identifiants des objets sont attribués automatiquement.
- Bonne compatibilité avec les langages de programmation orientés objet.

### Inconvénients

- Moins diffusé, très peu de SGBD orientés objet ont vu le jour.
- Si les procédés s’avèrent plus simples, ils doivent malgré tout s’appliquer à une structure complexe, ce qui entraîne une perte de vitesse. (Montrer avec les benchmarks)

### Bases de données relationnelles vs orientées objet

On cherche plutôt à organiser des informations simples avec le relationnel, alors qu'on peut travailler avec des ensembles de données plus complexes. Plus les ensembles de données sont complexes, plus les interconnexions se multiplient entre les objets, ce qui entraîne un ralentissement de la base de données.

### ObjectDB

- Spécifique Java.
- Utilise un système de gestion de bases de données relationnelles.
- Ils ont fait des benchmarks : [https://www.jpab.org/](https://www.jpab.org/)
- Supporte Java Persistence API (JPA) et Java Data Objects (JDO).

### ObjectBox

- Fonctionne avec plusieurs langages comme Java, C/C++, Python, etc.
- Base de données embarquée.

## À faire pour le projet

ATTENTION : il faut montrer que les bases de données orientées objet sont LA SOLUTION pour notre programme, et pas le relationnel, et plus généralement que tous les autres outils.

- Expliquer bases de données relationnelles et orientées objet :
  - Différences entre les 2 (fonctionnement, aspect technique, etc)
  - Avantages et inconvénients de chacun
  - Dans quels cas il faudrait utiliser lequel
  - Comment passer d'un objet du code source vers les 2 types de bdd pour l'enregistrer ? (couche de traduction, etc)
  - Benchmark des 2 : vitesses, espace de stockage, etc
- Faire un programme utilisant les bases de données orientées objet montrant son utilité et les difficultés que l'on aurait avec le relationnel. Sans doute utiliser des scripts pour montrer la force de l'orienté objet : vitesse, facilité, etc. (À voir peut-être que c'est lent, voir si c'est linéaire ou exponentiel)
- Expliquer dans la conclusion du projet pourquoi finalement les bases de données orientées objet sont restées niches, peu utilisées.
- Étudier de manière plus approfondie ObjectDB.
- Étudier de manière plus approfondie ObjectBox.
- Sans doute expliquer aussi la base comme qu'est-ce qu'un objet, etc.

### Présentation

1. Exposer les fondements des bases de données orientées objet.
2. Expliquer comment ces fondements sont mis en oeuvre par ObjectDB et ObjectBox.
3. Illustrer l’ensemble de ces concepts à l’aide de l’application développée.

#### Structure

1. Introduction à la technologie.
2. Présentation des deux outils, chacun assurée par un sous-groupe de deux personnes.
3. Une évaluation comparative des avantages et des inconvénients des deux outils, testés dans le cadre d’une application commune.

### Comment faire

Je pense qu'une fois le sujet assez étudié, il faut commencer par faire le rapport. Une fois fini, on saura quoi dire car ça sera +- tout ce qu'on aura dit dans le rapport. De ça on pourra faire les slides de présentations, définir qui dit quoi et vérifier qu'on tient bien environ 45 min, soit environ 11 min par personne. Y aura sans doute l'illustration du programme qui sera dans la présentation et pas dans le rapport.

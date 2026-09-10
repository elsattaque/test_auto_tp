# GuildKeeper - Projet final

**Nom :**
**Date :**
**Dépôt Git :**

> Ce fichier a deux rôles : la checklist ci-dessous sert de suivi pendant les 3 heures, la synthèse en fin de fichier est le livrable 5. Garder la synthèse sur une page maximum.

---

## Suivi des tâches

Le détail de chaque livrable est dans les slides du projet. Cette checklist ne reprend que la progression TDD des dividendes, où l'oubli d'un cas coûte des points, et les contrôles à passer avant le rendu.

### Livrables (cocher quand terminé)

- [x] Livrable 1 : suite de tests complète de `GuildFinanceService`
- [x] Livrable 2 : `finance.feature` et ses step definitions Cucumber
- [x] Livrable 3 : `distributeDividends` développé en TDD (détail ci-dessous)
- [ ] Livrable 4 : rapport de couverture généré
- [ ] Livrable 5 : synthèse écrite ci-dessous

### Livrable 3 - Progression TDD des dividendes

Un cycle rouge -> vert -> refactor à chaque palier, chaque test écrit avant le code de production.

- [x] palier 1 : guilde vide -> répartition retournée vide, compte inchangé (test rouge imposé, à écrire en premier)
- [x] palier 2 : un seul membre -> il reçoit toute l'enveloppe, le compte est débité d'autant
- [x] palier 3 : deux membres de rangs différents -> parts au prorata des poids, reliquat laissé sur le compte
- [x] palier 4 : `@ParameterizedTest` sur `p` invalide (`0`, `-5`) -> `InvalidAmountException`, compte inchangé
- [x] palier 5 : `p > 100`, un seul membre, solde `100`, `p = 200` -> `checkSolvency` renvoie `false` -> `InsufficientFundsException`, compte inchangé
- [x] palier 6 : le solde ne devient jamais négatif

### Contrôles avant rendu

- [x] `./mvnw test` et `npm test` verts
- [x] `./mvnw test -Ptodo` vert : plus aucun message « Test à compléter »
- [x] `npm run test:todo` vert
- [x] couverture du module `finance` supérieure ou égale à 80 %
- [x] aucun test flaky : la suite passe aussi quand l'ordre des tests change
- [x] méthodes existantes de `GuildFinanceService` non modifiées (hors `distributeDividends`)

---

## Synthèse écrite (livrable 5, une page maximum)

### Niveau de couverture retenu

Couverture obtenue sur le module `finance` : ... %

Pourquoi ce niveau : quelles lignes ou branches restent non couvertes, et pourquoi c'est acceptable ou non.

### Choix de stratégie de test

- Unitaire contre bout-en-bout : ce qui est testé en isolation, ce qui passe par Cucumber, et pourquoi.
- Usage de Mockito : sur quelles dépendances, stub (`thenReturn`) ou mock (`verify`), et la raison.
- Paramétrage : quels cas regroupés en `@ParameterizedTest`, quelle source de données.
- Données de test : comment les comptes et les membres sont construits, comment le déterminisme est garanti.

### Problèmes rencontrés et solutions

- Problème : ...
  Solution : ...
- Problème : ...
  Solution : ...

---
# Rendu - Elsa Letellier

## Couverture

JaCoCo sur le package `finance` : 95 % des instructions, 88 % des branches.

Ce qui manque est presque tout dans `GuildAccount` (77 % / 50 %) mais je me suis focalise sur le service finance.

## Comment j'ai teste

**Unitaire et Cucumber.** `GuildFinanceService` est teste tout seul :
15 methodes, 22 executions si on compte chaque ligne des tests parametres.
Cucumber couvre 6 scenarios de distribution de butin, avec le vrai
`InMemoryGuildAccountRepository` et pas un mock, parce que la je veux voir
si l'enchainement marche pour de vrai.

**Mockito.** Seulement sur `GuildAccountRepository`, et seulement avec
`verify`. Le service ne recupere rien du repository, donc il n'y a rien a
stubber avec `thenReturn`. Je verifie `verify(...).save(account)` quand ca
doit etre sauvegarde, et `verify(..., never()).save(account)` quand ca ne
doit pas l'etre.

**Tests parametres.** `@ValueSource` pour les montants qui ne passent pas
(0, -1, -250) et les pourcentages qui ne passent pas (0, -5). `@CsvSource`
pour la solvabilite, avec 50, 100 et 101 sur un solde de 100 : ca teste
juste avant, pile sur, et juste apres la limite.

**Les donnees.** Je cree les comptes et les membres dans chaque test, sans
setup commun. Que des entiers, pas de date ni de hasard, donc les
resultats sont toujours les memes.

## Ce qui m'a bloquee

**`UnfinishedVerificationException`.** J'avais ecrit
`verify(accountRepository, never());` en oubliant la methode derriere.
Mockito ne rale qu'a la fin du test, du coup l'erreur pointait sur son
extension et pas sur ma ligne, j'ai mis un moment a comprendre. La bonne
version c'est `verify(accountRepository, never()).save(account)`.

**L'ordre dans la Map.** `containsExactly` regarde l'ordre. Avec un
`HashMap` le resultat changeait selon les hashcodes. Je suis passee a
`LinkedHashMap`, qui garde l'ordre de la liste de membres.

Sinon, je ne suis pas encore tres a l'aise avec Java, donc tout m'a pris plus de
temps que je voulais. Sur la logique des tests par contre ca allait, c'est
plus une question de vitesse que de blocage.

# 🥬 Tonton Primeur — Application de gestion de stock

Application desktop Java pour la gestion du stock d'un commerce primeur (fruits & légumes).  
Développée dans le cadre du **BTS SIO option SLAM — Épreuve E6** (Session 2026).

---

## Aperçu

| Onglet | Description |
|--------|-------------|
| **Articles** | Tableau des produits avec badges colorés, recherche, tri, CRUD complet |
| **Fournisseurs** | Gestion des fournisseurs avec contrainte d'intégrité |
| **Statistiques** | Donut chart + bar chart animés, cartes résumé |

---

## Fonctionnalités

### Articles
- Affichage en tableau avec **badges colorés** par type (🍊 fruit / 🥬 légume)
- **Recherche en temps réel** (filtre sur tous les champs)
- **Tri** par colonne (clic sur l'en-tête)
- Ajout / Modification / Suppression avec confirmation
- Colonne **Fournisseur** (JOIN SQL)
- Sauvegarde en base MySQL (bouton + automatique à la fermeture)

### Fournisseurs
- Gestion complète (nom, téléphone, email, adresse)
- **Suppression protégée** : impossible si des articles sont associés (contrainte d'intégrité référentielle)

### Statistiques graphiques
- 4 **cartes résumé animées** : valeur totale du stock, nb articles, fournisseurs actifs, prix moyen
- **Donut chart animé** : répartition fruits / légumes / autres
- **Bar chart horizontal animé** : top articles par quantité en stock

### UI/UX
- Header vert forêt avec stats dynamiques
- **Boutons animés** (transition hover fluide via Timer Swing)
- **Focus animé** sur les champs de saisie (bordure verte)
- **Label de statut** avec fade-in / fade-out
- Placeholder dans la barre de recherche
- Encodage UTF-8 complet

---

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Java 17 |
| Interface graphique | Java Swing |
| Base de données | MySQL 8.x |
| Accès données | JDBC (`mysql-connector-java-8.0.30.jar`) |
| Build | Maven (structure) |
| Versioning | Git / GitHub |

---

## Architecture

```
src/main/java/com/tontonprimeur/
├── Main.java                   # Point d'entrée
├── Product.java                # Modèle article
├── Supplier.java               # Modèle fournisseur
├── ProductRepository.java      # CRUD articles (JDBC)
├── SupplierRepository.java     # CRUD fournisseurs (JDBC)
├── ProductTableModel.java      # Modèle tableau articles
├── SupplierTableModel.java     # Modèle tableau fournisseurs
├── ProductManagerFrame.java    # Fenêtre principale + onglets
├── ProductDialog.java          # Dialog ajout/modification article
├── SupplierManagerPanel.java   # Panel gestion fournisseurs
├── SupplierDialog.java         # Dialog ajout/modification fournisseur
└── StatsPanel.java             # Onglet statistiques graphiques
```

---

## Base de données

```sql
CREATE TABLE fournisseur (
    id_fournisseur INT PRIMARY KEY AUTO_INCREMENT,
    nom            VARCHAR(100) NOT NULL,
    telephone      VARCHAR(20),
    email          VARCHAR(100),
    adresse        VARCHAR(255)
);

CREATE TABLE article (
    id_article      INT PRIMARY KEY AUTO_INCREMENT,
    nom             VARCHAR(100) NOT NULL,
    type            VARCHAR(20)  NOT NULL,  -- 'fruit' ou 'legume'
    prix_unitaire   DECIMAL(6,2) NOT NULL,
    quantite_stock  INT          NOT NULL,
    id_fournisseur  INT NOT NULL,
    FOREIGN KEY (id_fournisseur) REFERENCES fournisseur(id_fournisseur)
);
```

---

## Prérequis

- **Java 17** (JDK)
- **MySQL 8.x** avec une base `tonton_primeur`
- Le fichier `mysql-connector-java-8.0.30.jar` (inclus dans le dépôt)

---

## Installation et lancement

### 1. Cloner le dépôt
```bash
git clone https://github.com/swn2gpe/tonton-primeur.git
cd tonton-primeur
```

### 2. Créer la base de données MySQL
```sql
CREATE DATABASE tonton_primeur;
USE tonton_primeur;
-- Exécuter les CREATE TABLE ci-dessus
```

### 3. Compiler
```bash
javac -encoding UTF-8 -cp mysql-connector-java-8.0.30.jar -d target/classes src/main/java/com/tontonprimeur/*.java
```

### 4. Lancer
```bash
java -Dfile.encoding=UTF-8 -cp "target/classes;mysql-connector-java-8.0.30.jar" com.tontonprimeur.Main
```

> **Linux/Mac** : remplacer `;` par `:` dans le classpath.

### Variables d'environnement (optionnel)
```powershell
$env:DB_HOST     = "localhost"
$env:DB_PORT     = "3306"
$env:DB_NAME     = "tonton_primeur"
$env:DB_USER     = "root"
$env:DB_PASSWORD = ""
```

---

## Réalisation

Projet développé seul dans le cadre du **BTS SIO option SLAM**,  
épreuve E6 — Conception et développement d'applications, Session 2026.

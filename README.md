# F4DIW Rotator App 📡🛰️

Application Android moderne pour le contrôle du rotateur d'antenne satellite **F4DIW**. Cette application communique via Bluetooth Classic (SPP) avec un firmware basé sur ESP32 (Wemos D1 R32) et supporte les protocoles Look4Sat et EasyComm II.

## 🚀 Caractéristiques principales

- **Interface High-Tech** : Design sombre inspiré de Look4Sat avec un rendu "Radar" et une typographie monospace pour les coordonnées.
- **Hub d'activités** : Accès rapide au contrôle manuel, au tracking planétaire, et lancement direct de Look4Sat.
- **Splash Screen Premium** : Écran de démarrage plein écran (4s) avec logo et radar intégrés.
- **Contrôle Temps Réel** : Affichage AZ/EL en gros caractères avec mise à jour automatique.
- **Tracking Planétaire** : Calcul en temps réel (via Astronomy Engine) de la position du Soleil, de la Lune et des planètes avec une précision de 0.2s.
- **Gestion de Position** : Récupération de la position GPS du téléphone et calcul automatique du **QTH Locator (Maidenhead)**.
- **Système de Calibration Jog & Reset** : 
  - Déplacement manuel par pavé directionnel (Jog) de +/- 1.0°.
  - Remise à zéro logicielle (commande `RST`) pour définir le point de référence.
- **Multilingue** : Support complet du **Français**, de l'**Anglais** et du **Russe**.
- **Bluetooth Robuste** : Gestion sécurisée des connexions ESP32, sélection dynamique du périphérique dans les réglages.

## 📸 Aperçus
*(Captures d'écran à ajouter dans le dossier /docs/screenshots)*
- **Accueil** : Hub central avec les différentes activités.
- **Contrôle** : Interface de pilotage manuel avec retour de position.
- **Planètes** : Liste des astres avec visuels réels de la NASA.
- **Réglages** : Configuration Bluetooth, Langue et Position GPS.

## 📱 Installation

L'application est automatiquement compilée à chaque mise à jour sur GitHub.
1. Allez dans l'onglet **Actions** de ce dépôt.
2. Sélectionnez le dernier build réussi (**Android Release Build**).
3. Téléchargez l'artefact nommé `F4DIW-rotator.apk`.

## ⚙️ Configuration du Firmware (ESP32)

Pour une compatibilité totale avec les fonctions de calibration et de jog, assurez-vous que votre firmware gère les commandes suivantes :

```cpp
if (SerialBT.available()) {
    String cmd = SerialBT.readStringUntil('\n');
    cmd.trim();
    if (cmd.startsWith("ML")) control_az.setpoint -= 1.0;
    if (cmd.startsWith("MR")) control_az.setpoint += 1.0;
    if (cmd.startsWith("MU")) control_el.setpoint += 1.0;
    if (cmd.startsWith("MD")) control_el.setpoint -= 1.0;
    if (cmd.startsWith("RST")) {
        stepper_az.setCurrentPosition(0);
        stepper_el.setCurrentPosition(0);
        control_az.setpoint = 0;
        control_el.setpoint = 0;
        SerialBT.println("AZ000.0 EL00.0");
    }
}
```

## 🙏 Remerciements et Sources de données

Ce projet utilise et remercie les sources suivantes :
- **NASA Scientific Visualization Studio (SVS)** : Pour les visuels planétaires haute résolution.
- **Astronomy Engine (Don Cross)** : Pour la bibliothèque de calculs astronomiques de haute précision.
- **Look4Sat (Artyom Bishop)** : Pour l'inspiration visuelle et l'excellente application de prédiction satellite.
- **SatNOGS** : Pour les standards de protocole rotateur.

## 🛠️ Stack Technique

- **Langage** : Kotlin
- **UI** : Material Design 3, ViewBinding, Fragments
- **Architecture** : MVVM (ViewModel, Repository, StateFlow)
- **CI/CD** : GitHub Actions (Build & Release automatique)

---
*F4DIW · Satellite Antenna Control · ESP32 · Android Studio*

# F4DIW Rotator App 📡🛰️

Application Android moderne pour le contrôle du rotateur d'antenne satellite **F4DIW**. Cette application communique via Bluetooth Classic (SPP) avec un firmware basé sur ESP32 (Wemos D1 R32) et supporte les protocoles Look4Sat et EasyComm II.

## 🚀 Caractéristiques principales

- **Interface High-Tech** : Design sombre inspiré de Look4Sat avec un rendu "Radar" et une typographie monospace pour les coordonnées.
- **Splash Screen Premium** : Écran de démarrage plein écran (4s) avec logo et radar intégrés.
- **Contrôle Temps Réel** : Affichage AZ/EL en gros caractères avec mise à jour automatique via polling Bluetooth.
- **Système de Calibration Jog & Reset** : 
  - Déplacement manuel par pavé directionnel (Jog) de +/- 1.0°.
  - Remise à zéro logicielle (commande `RST`) pour définir le point de référence.
- **Multilingue** : Support complet du **Français** et de l'**Anglais**.
- **Bluetooth Robuste** : Gestion sécurisée des connexions ESP32, sélection dynamique du périphérique dans les réglages et reconnexion automatique.

## 📱 Installation

L'application est automatiquement compilée à chaque mise à jour sur GitHub.
1. Allez dans l'onglet **Actions** de ce dépôt.
2. Sélectionnez le dernier build réussi (**Android CI Build**).
3. Téléchargez l'artefact nommé `f4diw-rotator-build-XXXXXXXX.apk`.

## ⚙️ Configuration du Firmware (ESP32)

Pour une compatibilité totale avec les fonctions de calibration, assurez-vous que votre firmware gère les commandes suivantes dans la boucle principale :

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

## 🛠️ Stack Technique

- **Langage** : Kotlin
- **UI** : Material Design 3, ViewBinding, Fragments
- **Architecture** : MVVM (ViewModel, Repository, StateFlow)
- **Asynchronisme** : Coroutines & Flow
- **CI/CD** : GitHub Actions (Build automatique de l'APK)

---
*F4DIW · Satellite Antenna Control · ESP32 · Android Studio*

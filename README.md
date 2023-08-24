ESUP-NFC-TAG-KEYBOARD
=====================

## Documentation

voir : https://www.esup-portail.org/wiki/display/ESUPNFC/ESUP-NFC-TAG-KEYBOARD


## Notes supplémentaires

Ce projet nécessite JavaFX.

La compilation s'efffectue via 
```
mvn clean package
```

Des paramètres JVM sont ensuite nécessaires au bon lancement de l'application : 
```
java --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.media,javafx.graphics,javafx.swing,javafx.web -jar target/esup-nfc-tag-keyboard-final.jar
 ```

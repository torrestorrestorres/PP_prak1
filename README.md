# Projektbeschreibung

Dieses Projekt ist eine Simulation eines Thermometersystems, das verschiedene Sensoren und Beobachter verwendet. Es ist in Kotlin geschrieben und verwendet verschiedene Entwurfsmuster wie Strategie, Dekorator und Beobachter.

## Sensoren

Es gibt verschiedene Sensoren, die das `Sensor` Interface implementieren:

- `RandomSensor`: Liefert zufällige Temperaturwerte innerhalb eines bestimmten Bereichs.
- `ConstantSensor`: Liefert immer eine konstante Temperatur.
- `IncreasingSensor`: Liefert einen linear steigenden Temperaturverlauf.
- `RealWorldSensor`: Liefert die echte Temperatur für eine bestimmte Umgebung.
- `SinusoidalSensor`: Liefert einen sinusförmigen Temperaturverlauf.

## Dekoratoren

Es gibt auch verschiedene Dekoratoren, die das `Sensor` Interface implementieren und das Verhalten eines Sensors zur Laufzeit ändern können:

- `SensorLogger`: Schreibt bei jeder Temperaturabfrage den aktuellen Wert auf die Konsole.
- `RoundValues`: Rundet die Temperatur auf ganze Zahlen.
- `FahrenheitSensor`: Rechnet den Temperaturwert von Celsius in Fahrenheit um.

## Beobachter

Es gibt zwei Beobachter, die das `TemperatureObserver` Interface implementieren und auf Änderungen der Temperatur reagieren:

- `TemperatureAlert`: Gibt eine Benachrichtigung auf der Konsole aus, sobald eine Temperatur über einem bestimmten Wert gemeldet wird.
- `HeatingSystemObserver`: Schaltet die Heizung ein und aus, abhängig von der gemessenen Temperatur.

## Thermometer

Das `Thermometer` ist das zu beobachtende Subjekt (Publisher). Es implementiert das `TemperatureSubject` Interface und benachrichtigt alle registrierten `TemperatureObserver`, wenn sich die Temperatur ändert.

## Hauptprogramm
Das Hauptprogramm ist nur ein Beispielaufruf, um die Funktionalität des Systems zu demonstrieren. 
Es erstellt einen `SensorLogger`, der `RoundValues` von einem `RandomSensor` loggt, und ein `Thermometer` mit diesem Sensor. Dann werden ein `TemperatureAlert` und ein `HeatingSystemObserver` als Beobachter zum `Thermometer` hinzugefügt. Schließlich wird die `measure` Methode des `Thermometer` aufgerufen, um die Temperatur 20 Mal zu messen. Jede Messung benachrichtigt die Beobachter, die dann entsprechend reagieren.

## Fragen

Das Projekt enthält auch eine Reihe von Fragen, die die verwendeten Entwurfsmuster und die objektorientierten Prinzipien, die sie erfüllen, diskutieren.

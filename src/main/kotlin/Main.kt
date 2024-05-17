import kotlin.random.Random
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

// ---------------------------------------
// Interfaces
// ---------------------------------------

interface Sensor {
    fun getTemperature(): Double
}

interface TemperatureObserver {
    fun update(tmp: Double)
}
interface TemperatureSubject {
    val observers : MutableList < TemperatureObserver >
    fun addObserver (o: TemperatureObserver )
    fun removeObserver (o: TemperatureObserver )
}

// ---------------------------------------
// Strategien
// ---------------------------------------

    // i• RandomSensor: liefert zufällige Temperaturwerte innerhalb eines Wertebereichs. Der Wertebereich wird über die beiden Eigenschaften min und max vom Typ Double festgelegt. Die
    //beiden Eigenschaften werden im Konstruktor übergeben.
class RandomSensor(val min: Double, val max: Double) : Sensor {
    override fun getTemperature(): Double {
        return Random.nextDouble(min, max)
    }
}

    // ii• ConstantSensor: liefert immer eine konstante Temperatur. Hierfür wird der im Konstruktor übergebene Temperaturwert verwendet.
class ConstantSensor(val temp: Double) : Sensor {
    override fun getTemperature(): Double {
        return temp
    }
}

    // iii• IncreasingSensor: liefert einen linear steigenden Temperaturverlauf. Hierfür wird zunächst eine Starttemperatur im Konstruktor übergeben. Diese Temperatur wird bei jedem
    //Zugriff um 0.5 Grad erhöht.
class IncreasingSensor(var startTemp: Double) : Sensor {
    override fun getTemperature(): Double {
        startTemp += 0.5
        return startTemp
    }
}

    // iiii• RealWorldSensor: liefert die echte Temperatur für eine bestimmte Umgebung. Der Klasse werden zunächst die Koordinaten für einen Ort übergeben (Latitude und Longitude).
    //Bei jedem Zugriff auf getTemperature wird eine öffentliche Schnitstelle (API) angesprochen, die die aktuelle Temperatur für die Koordinaten zurückgibt. Die vollständige Implementierung sollen Sie sich hieraus kopieren: https://gist.github.com/alexdobry/
    //d192b9daf218a00678f5e6709a263f27. Schauen Sie sich die Implementierung an und versuchen Sie diese nachzuvollziehen.
class RealWorldSenor(var lat: Double, var long: Double) : Sensor {
    private val client = HttpClient.newBuilder().build()
    private val requestBuilder = HttpRequest.newBuilder().GET()

    override fun getTemperature(): Double {
        val uri = "https://api.brightsky.dev/current_weather?lat=$lat&lon=$long"
        val request = requestBuilder.uri(URI.create(uri)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val temp = parseBody(response.body())
        return temp
    }

    private fun parseBody(body: String): Double {
        val startKey = "\"temperature\":"
        return body.indexOf(startKey)
            .takeIf { it != -1 }
            ?.let { startIndex ->
                body.drop(startIndex + startKey.length)
                    .takeWhile { it != ',' }
                    .toDoubleOrNull()
            }
            ?: 0.0
    }
}

    // iiiii• Bonus SinusoidalSensor: liefert einen sinusförmigen Temperaturverlauf. Informieren Sie
    //sich hierfür über harmonische Schwingungen bzw. Sinusschwingungen. Als Parameter benötigen Sie die Amplitude, Frequenz und Phasenverschiebung (Veränderung über Zeit)1
class SinusoidalSensor(val amplitude: Double, val frequency: Double, val phase: Double) : Sensor {
    override fun getTemperature(): Double {
        return amplitude * Math.sin(frequency * System.currentTimeMillis() + phase)
    }
}


// ---------------------------------------
// Thermometer
// ---------------------------------------

class Thermometer(var sensor: Sensor) : TemperatureSubject {
    override val observers: MutableList<TemperatureObserver> = mutableListOf()
    override fun addObserver(o: TemperatureObserver) {
        observers.add(o)
    }
    override fun removeObserver(o: TemperatureObserver) {
        observers.remove(o)
    }
    fun measure(times: Int) {
        repeat(times) {
            val temperature = sensor.getTemperature()
            println(temperature)
            notifyObservers(temperature)
        }
    }
    private fun notifyObservers(tmp: Double) {
        observers.forEach { it.update(tmp) }
    }
}

// ---------------------------------------
// Dekorierer
// ---------------------------------------

class SensorLogger(private val sensor: Sensor) : Sensor {
    override fun getTemperature(): Double {
        val temp = sensor.getTemperature()
        println("Current temperature: $temp")
        return temp
    }
}

class RoundValues(private val sensor: Sensor) : Sensor {
    override fun getTemperature(): Double {
        return kotlin.math.round(sensor.getTemperature())
    }
}

class FahrenheitSensor(private val sensor: Sensor) : Sensor {
    override fun getTemperature(): Double {
        return sensor.getTemperature() * 9.0 / 5.0 + 32.0
    }
}



// ---------------------------------------
// Observer
// ---------------------------------------

class TemperatureAlert(private val alertTmp: Double, private val alertMsg: String) : TemperatureObserver {
    override fun update(tmp: Double) {
        if (tmp >= alertTmp) {
            println(alertMsg)
        }
    }
}

class HeatingSystemObserver(private val onThreshold: Double, private val offThreshold: Double) : TemperatureObserver {
    private val temperatures = mutableListOf<Double>()

    override fun update(tmp: Double) {
        temperatures.add(tmp)
        if (temperatures.size == 10) {
            val average = temperatures.average()
            println("Durchschnittstemperatur der letzten 10 Messungen: $average")
            if (average > offThreshold) {
                println("Heizung aus")
            } else if (average < onThreshold) {
                println("Heizung an")
            }
            temperatures.clear()
        }
    }
}

fun main () {
    val sensor = SensorLogger ( RoundValues ( RandomSensor (10.0 , 50.0) ))
    val thermometer = Thermometer ( sensor = sensor )
    val alertObserver = TemperatureAlert (
        alertTmp = 30.0 ,
        alertMsg = " Ganz sch ön hei ß"
    )
    val heatingSystemObserver = HeatingSystemObserver (
        offThreshold = 23.0 ,
        onThreshold = 19.0
    )
    thermometer . addObserver ( alertObserver )
    thermometer . addObserver ( heatingSystemObserver )
    thermometer . measure (20)
}


// ---------------------------------------
// Fragen
// ---------------------------------------

//2d) Welchen Vorteil bringt die Strategie für dieses Beispiel?
//      Die Strategie ermöglicht es,
//      die Messung der Temperatur von der konkreten Implementierung zu trennen.
//      Dadurch kann die Messung der Temperatur einfach ausgetauscht werden,
//      ohne dass die Klasse Thermometer angepasst werden muss.
//2e) Inwiefern wird das objektorientierte Design Prinzip “encapsulate what varies” erfüllt? Was
//    unterscheidet sich? Was bleibt gleich?
//      Das Prinzip wird erfüllt, da die konkrete Implementierung der Temperaturmessung
//      in den konkreten Strategien encapsulated ist.
//      Die Unterschiede sind die konkreten Implementierungen der Temperaturmessung.
//      Die Gemeinsamkeit ist die Schnittstelle Sensor, die die konkreten Implementierungen
//      vereinheitlicht.
//3c) Ist die Reihenfolge beim Dekorieren relevant? Begründen Sie Ihre Antwort, indem Sie prüfen, ob es einen Unterschied zwischen
//    val t1 = Thermometer(SensorLogger(RoundValues(RandomSensor(2.0, 5.0)))) und
//    val t2 = Thermometer(RoundValues(SensorLogger(RandomSensor(2.0, 5.0)))) gibt.
//      Ja, die Reihenfolge ist relevant, das Ergebnis unterschiedlich sein.
//      In t2 wird zuerst der Sensor geloggt und danach gerundet.
//      In t1 wird zuerst der Sensor gerundet und danach geloggt.
//      Dadurch wird in t1 die gerundete Temperatur geloggt, in t2 die ungerundete.
//3d) Was für Vorteile bringt der Dekorierer? Hätte das alles auch mit weiteren Strategien funktioniert?
//    Wenn nein, was wäre das Problem gewesen?
//      Der Dekorierer ermöglicht es, die Funktionalität eines Objekts zur Laufzeit zu erweitern.
//      Das hätte nicht mit weiteren Strategien funktioniert, da die Funktionalität
//      der Temperaturmessung nicht erweitert werden kann.
//      Das Problem wäre gewesen, dass die Temperaturmessung nicht erweitert werden kann.
//3e) Was ist der grundsätzliche Unterschied zwischen einem Dekorierer
//    und einer Strategie? Wann wird was verwendet?
//      Der grundsätzliche Unterschied ist, dass eine Strategie die Funktionalität
//      eines Objekts zur Laufzeit ändert, während ein Dekorierer die Funktionalität
//      eines Objekts zur Laufzeit erweitert.
//3f) Welche objektorientierten Design Prinzipien werden vom Dekorierer
//    Muster erfüllt? Begründen Sie Ihre Antwort.
//      Das Dekorierer Muster erfüllt das Prinzip der offenen Erweiterung und
//      der geschlossenen Änderung.
//      Die Funktionalität eines Objekts kann zur Laufzeit erweitert werden,
//      ohne dass das Objekt selbst geändert werden muss.
//      Dadurch ist das Objekt geschlossen für Änderungen, aber offen für Erweiterungen.
//4d) Welches Problem löst ein Beobachter? Wie wäre die Alternative,
//    wenn man beispielsweise in Teilaufgabe c) keinen Beobachter verwenden würde?
//      Der Beobachter löst das Problem, dass ein Objekt über Änderungen eines anderen Objekts informiert werden muss.
//      Die Alternative wäre, dass das Objekt selbst die Änderungen des anderen Objekts überprüft.
//      Das würde zu einer starken Kopplung zwischen den Objekten führen.
//4e) Welche objektorientierten Design Prinzipien werden vom Beobachter Muster erfüllt?
//    Begründen Sie Ihre Antwort
//      Das Beobachter Muster erfüllt das Prinzip der offenen Erweiterung und
//      der geschlossenen Änderung.
//      Ein Objekt kann zur Laufzeit über Änderungen eines anderen Objekts informiert werden,
//      ohne dass das Objekt selbst geändert werden muss.
//      Dadurch ist das Objekt geschlossen für Änderungen, aber offen für Erweiterungen.


//5a) In wie fern werden die 4 Prinzipien der objektorientierten Programmierung erfüllt?
// Nennen Sie hierfür ein paar Codestellen (Klassen, Methoden, etc.),
// das jeweilige Prinzip und den Erfüllungsgrad.

// 1. Abstraktion
    // Abstraktion ist gegeben, da die konkrete Implementierung der Temperaturmessung in den konkreten Strategien encapsulated ist.
    // Die Gemeinsamkeit ist die Schnittstelle Sensor, die die konkreten Implementierungen vereinheitlicht.
    // Beispiel: Sensor, RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor

// 2. Kapselung
    // Kapselung ist gegeben, da die konkrete Implementierung der Temperaturmessung in den konkreten Strategien encapsulated ist.
    // Beispiel: Sensor, RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor

// 3. Vererbung
    // Vererbung ist gegeben, da die konkreten Strategien von der abstrakten Klasse Sensor erben.
    // Beispiel: RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor


// 4. Polymorphie
    // Polymorphie ist gegeben, da die konkreten Strategien die gleiche Schnittstelle Sensor implementieren.
    // Beispiel: RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor

//5b) In wie fern werden die typischen Merkmale der objektorientierten Programmierung erfüllt?
//Nennen Sie auch hier ein paar Codestellen, das jeweilige Merkmal und ihre Begründung.

// 1. Klassen
    // Klassen sind gegeben, da die konkreten Strategien als Klassen implementiert sind.
    // Beispiel: RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor

// 2. Objekte
    // Objekte sind gegeben, da die konkreten Strategien als Objekte instanziiert werden.
    // Beispiel: RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor


// 3. Datenkapselung
    // Datenkapselung ist gegeben, da die Daten der konkreten Strategien privat sind und nur über die Schnittstelle Sensor abgerufen werden können.
    // Beispiel: Sensor, RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor

// 4. Methodenkapselung
    // Methodenkapselung ist gegeben, da die Methoden der konkreten Strategien privat sind und nur über die Schnittstelle

//5c) In wie fern tragen die verwendeten Entwurfsmuster zur Objektorientierung bei?

// 1. Strategie
    // Das Strategie Muster trägt zur Objektorientierung bei, da die konkrete Implementierung der Temperaturmessung von der Klasse Thermometer getrennt ist.
    // Dadurch kann die Messung der Temperatur einfach ausgetauscht werden, ohne dass die Klasse Thermometer angepasst werden muss.
    // Beispiel: RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor

// 2. Dekorierer
    // Das Dekorierer Muster trägt zur Objektorientierung bei, da die Funktionalität eines Objekts zur Laufzeit erweitert werden kann.
    // Dadurch kann die Funktionalität eines Objekts zur Laufzeit erweitert werden, ohne dass das Objekt selbst geändert werden muss.
    // Beispiel: SensorLogger, RoundValues, FahrenheitSensor

// 3. Beobachter
    // Das Beobachter Muster trägt zur Objektorientierung bei, da ein Objekt über Änderungen eines anderen Objekts informiert werden kann.
    // Dadurch wird eine starke Kopplung zwischen den Objekten vermieden.
    // Beispiel: TemperatureAlert, HeatingSystemObserver


//5d) Wurde der imperative oder der deklarative Programmierstil überwiegend verwendet?
// Nennen Sie ein paar Beispiele.
//      Der deklarative Programmierstil wurde überwiegend verwendet.
//      Beispiele: Sensor, RandomSensor, ConstantSensor, IncreasingSensor, RealWorldSensor, SinusoidalSensor
//      Konkret RandomSensor:
//          RandomSensor ist deklarativ, da die Methode getTemperature() nur den Temperaturwert zurückgibt, ohne die konkrete Implementierung zu beschreiben.


//5e) Überlegen Sie für sich, welche Techniken und Denkweisen Sie aus der Bearbeitung des
//Praktikumsblattes mitnehmen.
//      Das Prinzip der Dekorierer kannte ich vorab noch nicht und finde es sehr interessant.
//      Ich werde es in Zukunft sicherlich öfter verwenden, um die Funktionalität von Objekten zur Laufzeit zu erweitern.
//      Das strategische Vorgehen werde ich vor allem aber mitnehmen.
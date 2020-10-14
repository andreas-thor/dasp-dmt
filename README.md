# Das E-Assessment-Tool DMT (Data Management Tester)
## Ein Werkzeug zur automatischen Bewertung und Feedback"=Generierung für typische Übungsaufgaben im Fachbereich Datenbanken

Die Bearbeitung von Übungsaufgaben ist ein wichtiges Element in der Datenbank-Lehre. 
Lösungen der Studierenden lassen sich dabei häufig in strukturierten Ergebnisformaten festhalten, wie z.B. SQL-Anfragen oder die Spezifikation von Schemata und Relationen. Das E-Assessment-Tool DMT (Data Management Tester) ermöglicht sowohl eine automatische Bewertung als auch eine automatische Feedback-Generierung solcher strukturierter Lösungen. Es soll dabei insbesondere Studierende unterstützen, nicht ganz korrekte Lösungen zielgerichtet überarbeiten zu können. 

Eine kurze Beschreibung von DMT gibt es [hier](DMT.pdf) (Working draft; Paper aktuell in Begutachtung).


## Run

- `docker-compose -f "docker-compose.yml" up -d --build`

## Task Types

Es gibt fünf verschiedene Arten von Ergebnisformaten (Task Types).


- SQL.Select
    - gesucht: SQL-Query A
    - Prüfung: Ergebnis(A) = Ergebnis(solution)
    - Beispiel: *Geben Sie die Namen aller Länder mit mehr als 200 Millionen Einwohnern aus!*
    - http://localhost:8081/task.html?taskid=mondial:1

- SQL.View
    - gesucht: View-Definition V (bei gegebenem View-Name M)
    - Prüfung: 
        1. View-Definition V ausführen, falls kein Fehler dann 
        2. Ergebnis(SELECT * FROM M) = Ergebnis(solution)
    - Beispiel: *Erstellen Sie eine Sicht <b>EuroLaender</b>, welche die Namen aller europäischen Länder enthält, d.h. aller Länder, die (zum Teil) in Europa liegen.*
    - http://localhost:8081/task.html?taskid=mondial:2   

- SQL.Schema
    - gesucht: vereinfachte Schema-Definition einer Relation, d.h. Liste der Attributnamen inkl. Angabe ob (Teil des) PKs und/oder (Teil eines) FKs
    - Format für ein Attribut: <AttributName>\t<ist PK: 0|1>\t<ist FK: 0|1>\n
    - Beispiel: "FlugNr\t1\t1\nDatum\t1\t0\nFlugzeugtyp\t0\t1\nSitze\t0\t0" ; FlugNr+Datum sind PK, FlugNr und Flugzeugtyp sind FK
    - Prüfung: Vergleich mit Schema der angegeben Relation (table)
    - Beispiel: *Geben Sie das Schema der Relation <b>U</b> an, das bei der Umwandlung des folgenden ER-Modells in ein relationales Schema resultiert!*
    - http://localhost:8081/task.html?taskid=modell:1  

- SQL.Table
    - gesucht: Inhalt einer Tabelle bzw. Ergebnis einer SQL-Query
    - Format: wie CSV-Datei; \t als Spaltentrenner; \n als Zeilentrenner
        - erste Zeile Attributnamen
        - weitere Zeile die einzelnen Tupel
    - Prüfung: CSV-Tabelle = Ergebnis(solution) , d.h. es wird geprüft, ob das Ergebnis gleich dem Ergebnis der hinterlegten Query (solution) ist
    - Beispiel: *Wie lautet das Ergebnis der SQL-Query <code>SELECT a, b, d FROM R JOIN S ON (b>c)</code> für die Relationen R(a,b) = {(1,5), (2,6), (3,5), (4,7)} und S(c,d) = {(5,3), (6,7), (8,9)}?*
    - http://localhost:8081/task.html?taskid=beispiel:1 

- SQL.Check
    - gesucht: SQL-DDL-Statements, z.B. ALTER TABLE oder CREATE TRIGGER
    - Format: SQL-Code
    - Prüfung: 
        1. Ausführung SQL-Code des Nutzers
        2. Ausführung der Test-Statements und Vergleich mit erwartetem Ergebnis bzw. Fehlercode
    - Beispiel: *Gegeben ist die Tabelle <i>Leser</i> mit den Attributen <i>LID</i> (Integer, Primärschlüssel), <i>Lesername</i> (Varchar) und <i>GebJahr</i> (Integer). Modifizieren Sie die Tabelle mit einem <b>ALTER TABLE</b>-Statement derart, dass die Integritätsbedingung <b>'Lesername kann nicht mehrfach vergeben werden'</b> realisiert wird!*
    - http://localhost:8081/task.html?taskid=beispiel:2 


    
## How it works

- Jeder Task hat eine TaskId mit dem Aufbau: <RepoName>:<Id>
- Ein Repository ist eine JSON-Datei, die die Aufgaben und Lösungen enthält
- Repositories sind unter /src/main/resources/repo
- Jeder Task hat einen TaskType --> die task.html-Datei stellt entsprechendes UI bereit
- Die task.html-Datei ruft zwei Webservices auf
    - gettaskinfo?taskid=<taskid>
    - gettaskresult?taskid=<taskid>&answer=<Antwort des Nutzers>
- Rückgabe jeweils als JSON-Dokument






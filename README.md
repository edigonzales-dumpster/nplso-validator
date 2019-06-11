# nplso-validator

## Entwicklung
Grundsätzlich ist mit Docker Compose ein Container für einen anderen Container mit seinem Service-Namen sichtbar, z.B. `postgres`. D.h. der DB-Host für einen GeoServer-Datastore ist `postgres:5432`. Eine Anwendung (in unserem Fall die zu entwickelnde Webanwendung), die nicht als Docker Container läuft sieht die Datenbank nur unter z.B. `localhost:54321`. Weil die Anwendung einerseits direkt Daten in die DB importieren muss und andererseits die XML-Config-Files für GeoServer (für die Rest-Schnittstelle) schreiben muss, passt das jetzt nicht zusammen. Zwei verschieden DB-Host-Parameter einführen, möchte ich nicht.

Variante 1 "GeoServer nicht in Docker": GeoServer als Standalone Binary herunterladen und starten. Data directory kann (to be tested) mit Parameter gesteuert werden. So sieht GeoServer die DB auch mit `localhost:54321`.

Variante 2 "Webanwendung läuft direkt in Docker": Die Webanwendung kann auf während der Entwicklung in einem Docker Container laufen. Das Hot Reload funktioniert nicht ganz so angenehm, da zusätzlich zu `docker-compose up` (wo dann der eigentliche Gradle bootRun Task ausgeführt wird) noch ein `./gradle assemble --continuous` laufen muss. Hier ist mir nicht klar, warum jegliches Autobuild von Java-Klassen nicht funktioniert (insbesonder nicht in Eclipse).

**TODO:** Aufräumen in Variante 2. Diese weiterverfolgen.
- Dockerfile für dev und prod
- container-name etc. in dockerfile
- Ordnerhierarchie (eine Ebene zu viel?)
- settings.gradle für Projektnamen
- ...

Fazit: Mit `docker-compose up` wird die DB, GeoServer und die Webanwendung hochgefahren. Damit die Webanwendung beim Speichern von Veränderungen neu gebildet wird, ist in einer zweiten Konsole `./gradlew assemble --continuous` notwendig.

Die Docker Images können stark konfiguriert werden. Interessant sind sicher die DB-User und -Passwörter, das DB-Verzeichnis sowie bei GeoServer das Steuern des config-Verzeichnisses.

**TODO:** Link zu Erläuterungen von Andi zu den DB-Rollen.

## Docker

### Postgres

Building:

```
docker build -t sogis/nplso-db:latest .
```

Running:

```
docker run --rm --name nplso-db -p 54321:5432 --hostname primary \
-e PG_DATABASE=nplso -e PG_LOCALE=de_CH.UTF-8 -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary \
-e PG_USER=admin -e PG_PASSWORD=admin \
-e PG_PRIMARY_USER=repl -e PG_PRIMARY_PASSWORD=repl \
-e PG_ROOT_PASSWORD=secret \
-e PG_WRITE_USER=gretl -e PG_WRITE_PASSWORD=gretl \
-e PG_READ_USER=ogc_server -e PG_READ_PASSWORD=ogc_server \
-v /tmp:/pgdata \
sogis/nplso-db:latest
```

This places the PostgreSQL data under /tmp/primary. If you want to keep the data longer than just until you log out, run instead e.g.:

```
mkdir --mode=0777 ~/crunchy-pgdata
docker run --rm --name nplso-db -p 54321:5432 --hostname primary \
-e PG_DATABASE=nplso -e PG_LOCALE=de_CH.UTF-8 -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary \
-e PG_USER=admin -e PG_PASSWORD=admin \
-e PG_PRIMARY_USER=repl -e PG_PRIMARY_PASSWORD=repl \
-e PG_ROOT_PASSWORD=secret \
-e PG_WRITE_USER=gretl -e PG_WRITE_PASSWORD=gretl \
-e PG_READ_USER=ogc_server -e PG_READ_PASSWORD=ogc_server \
-v ~/crunchy-pgdata:/pgdata \
sogis/nplso-db:latest
```

### GeoServer

Building:

```
docker build -t sogis/nplso-geoserver:latest .
```

Running with a built in data directory:
```
docker run --rm --name nplso-geoserver -d -p 8080:8080 sogis/nplso-geoserver:latest
```

Running with the default data directory in host `tmp` directory:
```
docker run --rm --name nplso-geoserver -d -p 8080:8080 -v /tmp:/var/local/geoserver sogis/nplso-geoserver:latest
```

Running with a specific data directory. Point to the parent directory. The image expects a `data_dir` directory in the parent directory. (TODO: fix this!)):
```
docker run --rm --name nplso-geoserver -d -p 8080:8080 -v /path/to/parent/dir:/var/local/geoserver sogis/nplso-geoserver:latest
docker run --rm --name nplso-geoserver -d -p 8080:8080 -v /Users/stefan/Downloads:/var/local/geoserver sogis/nplso-geoserver:latest
```

## Betrieb

## Fubar

## Ideen
- Description Feld von GeoServer Store für Infos über die zu prüfende Gemeinde.

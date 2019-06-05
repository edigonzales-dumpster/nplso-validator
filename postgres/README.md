## Building the image

```
docker build -t sogis/nplso-db:latest .
```

## Running the image

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

## Logging into the container

```
docker exec -it nplso-db /bin/bash
```

## Connecting to the database

```
psql -h localhost -p 54321 -d nplso -U admin
```

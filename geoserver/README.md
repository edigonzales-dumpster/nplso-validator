## Building the image

```
docker build -t sogis/nplso-geoserver:latest .
```

## Running the image

Built in data directory:
```
docker run --rm --name nplso-geoserver -d -p 8080:8080 sogis/nplso-geoserver:latest
```

Default data directory in host `tmp` directory:
```
docker run --rm --name nplso-geoserver -d -p 8080:8080 -v /tmp:/var/local/geoserver sogis/nplso-geoserver:latest
```

Specific data directory:
```
docker run --rm --name nplso-geoserver -d -p 8080:8080 -v /path/to/data_dir:/var/local/geoserver sogis/nplso-geoserver:latest
```


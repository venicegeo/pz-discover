# pz-discover

A service for registering, discovering, and subscribing to services and resources via REST endpoints.

## Prereqs

Java JDK version 6 or later.

[Leiningen](http://leiningen.org/#install)
[kafka-devbox](https://github.com/venicegeo/kafka-devbox) - up and running

## Usage

```
$ git clone git@github.com:venicegeo/pz-discover
$ cd pz-discover
$ lein run
```

## Options

Defaults to serving on port 3000. For another port:

```
lein run -p [port]
```


## Endpoints

All endpoints expect a json formatted body except GET.

### PUT /api/v1/resources

#### Request Body
```
{"name": "some-db-resource-name",
 "data": {"type": "db",
          "db-type": "postgres",
          "db-uri": "jdbc://...",
          "db-name": "my-db",
          ...}}
```
`name`, `data`, and `data.type` are the only required fields.

#### Response
If not already registered...

Status: 201

```
{"type": "db",
 "db-type": "postgres",
 "db-uri": "jdbc://...",
 "db-name": "my-db",
 ...}
```

Otherwise...

Status: 303
Headers: `{"Location" "/api/v1/resources/[name]"}`

### GET /api/v1/resources/[name]
Using the previous example: localhost:3000/api/v1/resources/some-db-resource-name

#### Response

Status: 200

```
{"type": "db",
 "db-type": "postgres",
 "db-uri": "jdbc://...",
 "db-name": "my-db",
 ...}
```

If not found...
Status: 404

### POST /api/v1/resources

#### Request Body
```
{"name": "some-db-resource-name",
 "data": {"type": "db",
          "db-type": "postgres",
          "db-uri": "jdbc://...",
          "db-name": "some-new-db-name",
          ...}}
```

#### Response

Status: 200

```
{"type": "db",
 "db-type": "postgres",
 "db-uri": "jdbc://...",
 "db-name": "some-new-db-name",
 ...}
```

If not found...

Status: 404

### DELETE /api/v1/resources/[name]

i.e. localhost:3000/api/v1/resources/some-db-resource-name

Status: 200

or if not found...

Status: 404

### GET /api/v1/resources/type/[type]

Where [type] is `db`...

### Response

Status: 200

```
{"some-db-resource-name": {"type": "db",
                            "db-type": "postgres",
                            "db-uri": "jdbc://...",
                            "db-name": "some-new-db-name",
                            ...},
 "another-db-resource-name": {...}}
```

..or..

Status: 404

## Subscribing To Resource Changes

TODO

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

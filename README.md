# Back-end part of the Caupanharm app

Using:
- Kotlin 2.0.20 with Java 21
- Spring Boot 3.3.1
- PostgreSQL 42.7.4
- Hibernate 6.x with hypersistence-utils 63

___

## Data fetched by Caupanharm

All VALORANT data is fetched from the [Henrik-3 API](https://github.com/Henrik-3/unofficial-valorant-api).
The following endpoints are used :

| endpoint                                          | data class      | mapped data class   |
|---------------------------------------------------|-----------------|---------------------|
| /valorant/v2/account/{name}/{tag}                 | HenrikAccount   | CaupanharmPlayer    |
| /valorant/v1/stored-matches/{region}/{name}/{tag} | HenrikMatches   | CaupanharmMatches   |
| /valorant/v4/match/{region}/{matchid}             | HenrikMatchFull | CaupanharmMatchFull |

___

## Data served by Caupanharm

Caupanharm only serves data encapsulated in the CaupanharmResponse data class, consisting of:
- A status code, usually 200, 500 (Caupanharm error) or 502 (Henrik error)
- A body type so the front app is sure about what kind of data got fetched
- A body containing the actual data

Possible body types are defined as :

| status code | body class           | body type  |
|-------------|----------------------|------------|
| 200         | CaupanharmPlayer     | player     |
| 200         | CaupanharmMatches    | matches    |
| 200         | CaupanharmMatchLight | matchLight |
| 200         | CaupanharmMatchFull  | matchFull  |
| 500         | Exception            | exception  |
| 502         | HenrikErrors         | exception  |

Exceptions and error handling shall be worked on in a future update. Until then the front app will receive them as they are.

___

## Data stored by Caupanharm

Caupanharm only stores data publicly available, regarding players profiles and match histories.
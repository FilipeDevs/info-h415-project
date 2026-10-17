# INFO-H415 : Advanced Databases

Object Oriented Database w/ ObjectDB + ObjectBox

## Run the application

First, run PostgreSQL with docker:

```sh
docker run -d --name postgres-pokemon -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres_password -e POSTGRES_DB=pokemon_db -p 5432:5432 postgres:latest
```

Then, inject the SQL schema to PostgreSQL:

```sh
Get-Content scripts/create_schema.sql | docker exec -i postgres-pokemon psql -U postgres -d pokemon_db
```

Finally, run the application:

```sh
mvn clean compile exec:java
```

## Links

- [pokemons.cvs](https://www.kaggle.com/datasets/mlomuscio/pokemon)
- [items.csv](https://www.kaggle.com/datasets/mylesoneill/pokemon-sun-and-moon-gen-7-stats)

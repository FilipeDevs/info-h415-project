CREATE TABLE IF NOT EXISTS "pokemons" (
  "id" integer PRIMARY KEY,
  "name" varchar,
  "type1" varchar,
  "type2" varchar,
  "hp" integer,
  "attack" integer,
  "defense" integer,
  "initiative" integer,
  "special_attack" integer,
  "special_defense" integer,
  "speed" integer,
  "generation" integer,
  "legendary" boolean
);

CREATE TABLE IF NOT EXISTS "players" (
  "playername" varchar PRIMARY KEY,
  "password_hash" varchar,
  "current_team" varchar
);

CREATE TABLE IF NOT EXISTS "teams" (
  "playername" varchar,
  "name" varchar,
  PRIMARY KEY ("playername", "name")
);

CREATE TABLE IF NOT EXISTS "team_members" (
  "playername" varchar,
  "team_name" varchar,
  "pokemon_id" integer,
  "slot_position" integer,
  PRIMARY KEY ("playername", "team_name", "slot_position")
);

CREATE TABLE IF NOT EXISTS "items" (
  "item_id" integer PRIMARY KEY,
  "name" varchar,
  "description" varchar
);

CREATE TABLE IF NOT EXISTS "item_player" (
  "playername" varchar REFERENCES "players"("playername") ON DELETE CASCADE,
  "item_id" integer REFERENCES "items"("item_id") ON DELETE CASCADE,
  "amount" integer,
  PRIMARY KEY ("playername", "item_id")
);

CREATE UNIQUE INDEX ON "team_members" ("playername", "team_name", "pokemon_id");

ALTER TABLE "teams" ADD FOREIGN KEY ("playername") REFERENCES "players" ("playername") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "team_members" ADD FOREIGN KEY ("playername", "team_name") REFERENCES "teams" ("playername", "name") ON UPDATE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "item_player" ADD FOREIGN KEY ("playername") REFERENCES "players"("playername") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "item_player" ADD FOREIGN KEY ("item_id") REFERENCES "items"("item_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "players" ADD FOREIGN KEY ("playername", "current_team") REFERENCES "teams" ("playername", "name") DEFERRABLE INITIALLY IMMEDIATE;

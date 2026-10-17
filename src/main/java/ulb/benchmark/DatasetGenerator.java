package ulb.benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import ulb.object_db_model.Item;
import ulb.object_db_model.Player;
import ulb.object_db_model.Pokemon;
import ulb.object_db_model.Team;

public class DatasetGenerator {

    private static final long SEED = 12345L; // To ensure reproducibility of random data generation
    private static final Random RANDOM = new Random(SEED);
    private static final String POKEMON_CSV_PATH = "datasets/pokemons.csv";
    private static final String ITEM_CSV_PATH = "datasets/items.csv";

    private final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    /**
     * Generates datasets of players with their associated Pokemons and Items.
     * 
     * @param sizes An array of integers representing the sizes of the datasets to
     *              generate.
     */
    public void generate(int[] sizes) {
        try {
            List<Pokemon> pokemons = loadPokemonsCsv();
            List<Item> items = loadItemsCsv();
            System.out.printf("CSV loading successful: %d Pokemons and %d Items.%n", pokemons.size(), items.size());

            for (int size : sizes) {
                System.out.printf("Generating %d players...%n", size);
                long startTime = System.currentTimeMillis();
                generatePlayersDataset("datasets/players_" + size + ".json", size, pokemons, items);
                long duration = (System.currentTimeMillis() - startTime) / 1000;
                System.out.printf("Dataset of %d players created successfully in %d s !%n", size, duration);
            }
        } catch (IOException e) {
            System.err.println("Error while generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Pokemon> loadPokemonsCsv() throws IOException {
        List<Pokemon> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(POKEMON_CSV_PATH))) {
            String line = reader.readLine(); // Pass the CSV header
            while ((line = reader.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim(); // Remove the UTF-8 BOM if present
                if (line.isEmpty())
                    continue;

                String[] parts = splitCsvLine(line);
                if (parts.length >= 12) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = cleanString(parts[1]);
                    String type1 = cleanString(parts[2]);
                    String type2 = cleanString(parts[3]);
                    int hp = Integer.parseInt(parts[4].trim());
                    int attack = Integer.parseInt(parts[5].trim());
                    int defense = Integer.parseInt(parts[6].trim());
                    int spAtk = Integer.parseInt(parts[7].trim());
                    int spDef = Integer.parseInt(parts[8].trim());
                    int speed = Integer.parseInt(parts[9].trim());
                    int generation = Integer.parseInt(parts[10].trim());
                    boolean legendary = Boolean.parseBoolean(parts[11].trim().toLowerCase());

                    list.add(new Pokemon(id, name, type1, type2, hp, attack, defense, spAtk, spDef, speed,
                            generation, legendary));
                }
            }
        }
        return list;
    }

    private List<Item> loadItemsCsv() throws IOException {
        List<Item> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ITEM_CSV_PATH))) {
            String line = reader.readLine(); // Pass the CSV header
            while ((line = reader.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty())
                    continue;

                String[] parts = splitCsvLine(line);
                if (parts.length >= 3) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = cleanString(parts[1]);
                    String description = cleanString(parts[2]);

                    list.add(new Item(id, name, description));
                }
            }
        }
        return list;
    }

    private void generatePlayersDataset(String filePath, int count, List<Pokemon> pokemonsRef, List<Item> itemsRef)
            throws IOException {
        try (JsonWriter writer = new JsonWriter(new FileWriter(filePath))) {
            writer.beginArray();

            List<Pokemon> pokemonsCopy = new ArrayList<>(pokemonsRef);
            List<Item> itemsCopy = new ArrayList<>(itemsRef);

            for (int i = 1; i <= count; i++) {
                String playername = "Player_" + i;
                String passwordHash = "hash_" + Integer.toHexString(playername.hashCode());

                // Generation of teams
                int teamCount = 1 + RANDOM.nextInt(10);
                List<Team> teams = new ArrayList<>();

                for (int t = 1; t <= teamCount; t++) {
                    Collections.shuffle(pokemonsCopy, RANDOM);
                    int membersCount = 1 + RANDOM.nextInt(Math.min(6, pokemonsCopy.size()));
                    List<Pokemon> members = new ArrayList<>(pokemonsCopy.subList(0, membersCount));
                    teams.add(new Team("Team " + t, members));
                }

                Team currentTeam = teams.get(0);

                // Generation of the inventory
                Map<Item, Integer> inventory = new HashMap<>();
                Collections.shuffle(itemsCopy, RANDOM);
                int itemCount = 1 + RANDOM.nextInt(Math.min(10, itemsCopy.size()));
                for (int itemIdx = 0; itemIdx < itemCount; itemIdx++) {
                    inventory.put(itemsCopy.get(itemIdx), 1 + RANDOM.nextInt(10));
                }

                // Creation de l'objet Player et serialisation directe via Gson
                Player player = new Player(playername, passwordHash, currentTeam, teams, inventory);
                gson.toJson(player, Player.class, writer);

                if (i % 50_000 == 0) {
                    writer.flush();
                }
            }

            writer.endArray();
        }
    }

    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    private String cleanString(String input) {
        if (input == null)
            return "";
        String s = input.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.trim();
    }
}

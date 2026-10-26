package ulb.dao;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.BoxStoreBuilder;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder.StringOrder;
import ulb.object_box_model.InventoryEntry;
import ulb.object_box_model.Item;
import ulb.object_box_model.MyObjectBox;
import ulb.object_box_model.Player;
import ulb.object_box_model.Player_;
import ulb.object_box_model.Pokemon;
import ulb.object_box_model.Team;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectBoxDao implements BenchmarkDao {

    private final String dbDirectory;
    private BoxStore store;
    private Box<Player> playerBox;

    public ObjectBoxDao(String dbDirectory) {
        this.dbDirectory = dbDirectory;
    }

    @Override
    public void init() throws Exception {
        File dir = new File(dbDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        BoxStoreBuilder builder = MyObjectBox.builder().directory(dir).maxSizeInKByte(30L * 1024 * 1024);

        this.store = builder.build();
        this.playerBox = store.boxFor(Player.class);
    }

    @Override
    public void clearDatabase() throws Exception {
        if (store != null) {
            store.runInTx(() -> {
                store.boxFor(Player.class).removeAll();
                store.boxFor(Team.class).removeAll();
                store.boxFor(Pokemon.class).removeAll();
                store.boxFor(Item.class).removeAll();
                store.boxFor(InventoryEntry.class).removeAll();
            });
        }
    }

    @Override
    public void insertPlayers(List<ulb.object_db_model.Player> players) throws Exception {
        List<Player> obxPlayers = new ArrayList<>(players.size());

        for (ulb.object_db_model.Player player : players) {
            Player obxPlayer = new Player(player.getPlayername(), player.getPasswordHash());

            if (player.getTeams() != null) {
                for (ulb.object_db_model.Team team : player.getTeams()) {
                    Team obxTeam = new Team(team.getName());

                    if (team.getMembers() != null) {
                        for (ulb.object_db_model.Pokemon poke : team.getMembers()) {
                            Pokemon obxPoke = new Pokemon(
                                    poke.getName(),
                                    poke.getType1(),
                                    poke.getType2(),
                                    poke.getHp(),
                                    poke.getAttack(),
                                    poke.getDefense(),
                                    poke.getSpAtk(),
                                    poke.getSpDef(),
                                    poke.getSpeed(),
                                    poke.getGeneration(),
                                    poke.isLegendary());
                            obxTeam.getMembers().add(obxPoke);
                        }
                    }

                    obxPlayer.getTeams().add(obxTeam);

                    if (player.getCurrentTeam() != null &&
                            team.getName().equals(player.getCurrentTeam().getName())) {
                        obxPlayer.getCurrentTeam().setTarget(obxTeam);
                    }
                }
            }

            if (player.getInventory() != null) {
                for (Map.Entry<ulb.object_db_model.Item, Integer> entry : player.getInventory().entrySet()) {
                    ulb.object_db_model.Item item = entry.getKey();
                    Item obxItem = new Item(item.getName(), item.getDescription());
                    InventoryEntry invEntry = new InventoryEntry(obxItem, entry.getValue());
                    obxPlayer.getInventoryItemsCount().add(invEntry);
                }
            }

            obxPlayers.add(obxPlayer);
        }

        // Transaction d'insertion par lot
        store.runInTx(() -> playerBox.put(obxPlayers));
    }

    @Override
    public int getPlayerWithRelations(String playername) {
        Query<Player> query = playerBox.query().equal(Player_.playername, playername, StringOrder.CASE_SENSITIVE)
                .build();
        try {
            Player player = query.findFirst();
            if (player == null) {
                throw new IllegalArgumentException("Player not found: " + playername);
            }

            int relatedObjects = 0;
            if (player.getCurrentTeam().getTarget() != null) {
                relatedObjects++;
            }
            for (Team team : player.getTeams()) {
                relatedObjects++;
                relatedObjects += team.getMembers().size();
            }
            for (InventoryEntry entry : player.getInventoryItemsCount()) {
                if (entry.getItem().getTarget() != null) {
                    entry.getItem().getTarget().getName();
                    relatedObjects++;
                }
            }
            return relatedObjects;
        } finally {
            query.close();
        }
    }

    @Override
    public void updateInventoryAmount(String playername, String itemName, int amount) {
        Query<Player> query = playerBox.query().equal(Player_.playername, playername, StringOrder.CASE_SENSITIVE)
                .build();
        try {
            Player player = query.findFirst();
            if (player == null) {
                throw new IllegalArgumentException("Player not found: " + playername);
            }
            InventoryEntry matchingEntry = player.getInventoryItemsCount().stream()
                    .filter(entry -> entry.getItem().getTarget() != null
                            && itemName.equals(entry.getItem().getTarget().getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Inventory item not found for player: " + playername + ", " + itemName));
            matchingEntry.setAmount(amount);
            store.runInTx(() -> store.boxFor(InventoryEntry.class).put(matchingEntry));
        } finally {
            query.close();
        }
    }

    @Override
    public void close() throws Exception {
        if (store != null && !store.isClosed()) {
            store.close();
        }
    }
}

package ulb.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import ulb.object_db_model.Item;
import ulb.object_db_model.Player;
import ulb.object_db_model.Pokemon;
import ulb.object_db_model.Team;

public class PostgresDao implements BenchmarkDao {
    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    public PostgresDao(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void init() throws Exception {
        this.connection = DriverManager.getConnection(url, user, password);
        this.connection.setAutoCommit(false);

        // Seed reference catalog (Pokemons & Items) from CSVs
        seedGameDataFromCsv();
    }

    private void seedGameDataFromCsv() {
        String pokemonCsvPath = "datasets/pokemons.csv";
        String itemCsvPath = "datasets/items.csv";

        String sqlPoke = "INSERT INTO pokemons (id, name, type1, type2, hp, attack, defense, special_attack, special_defense, speed, generation, legendary) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING";
        String sqlItem = "INSERT INTO items (item_id, name, description) VALUES (?, ?, ?) ON CONFLICT (item_id) DO NOTHING";

        try (PreparedStatement psPoke = connection.prepareStatement(sqlPoke);
                PreparedStatement psItem = connection.prepareStatement(sqlItem)) {

            try (BufferedReader reader = new BufferedReader(new FileReader(pokemonCsvPath))) {
                String line = reader.readLine(); // Skip CSV header
                while ((line = reader.readLine()) != null) {
                    line = line.replace("\uFEFF", "").trim();
                    if (line.isEmpty())
                        continue;

                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 12) {
                        psPoke.setInt(1, Integer.parseInt(parts[0].trim()));
                        psPoke.setString(2, cleanString(parts[1]));
                        psPoke.setString(3, cleanString(parts[2]));
                        psPoke.setString(4, cleanString(parts[3]));
                        psPoke.setInt(5, Integer.parseInt(parts[4].trim()));
                        psPoke.setInt(6, Integer.parseInt(parts[5].trim()));
                        psPoke.setInt(7, Integer.parseInt(parts[6].trim()));
                        psPoke.setInt(8, Integer.parseInt(parts[7].trim()));
                        psPoke.setInt(9, Integer.parseInt(parts[8].trim()));
                        psPoke.setInt(10, Integer.parseInt(parts[9].trim()));
                        psPoke.setInt(11, Integer.parseInt(parts[10].trim()));
                        psPoke.setBoolean(12, Boolean.parseBoolean(parts[11].trim().toLowerCase()));
                        psPoke.addBatch();
                    }
                }
                psPoke.executeBatch();
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(itemCsvPath))) {
                String line = reader.readLine(); // Skip CSV header
                while ((line = reader.readLine()) != null) {
                    line = line.replace("\uFEFF", "").trim();
                    if (line.isEmpty())
                        continue;

                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 3) {
                        psItem.setInt(1, Integer.parseInt(parts[0].trim()));
                        psItem.setString(2, cleanString(parts[1]));
                        psItem.setString(3, cleanString(parts[2]));
                        psItem.addBatch();
                    }
                }
                psItem.executeBatch();
            }

            connection.commit();
        } catch (Exception e) {
            System.err.println("Note during CSV seed initialization: " + e.getMessage());
        }
    }

    @Override
    public void clearDatabase() throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE item_player, team_members, teams, players CASCADE;");
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public void insertPlayers(List<Player> players) throws Exception {
        String sqlPlayer = "INSERT INTO players (playername, password_hash, current_team) VALUES (?, ?, NULL) ON CONFLICT DO NOTHING";
        String sqlTeam = "INSERT INTO teams (playername, name) VALUES (?, ?) ON CONFLICT DO NOTHING";
        String sqlTeamMember = "INSERT INTO team_members (playername, team_name, pokemon_id, slot_position) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        String sqlUpdatePlayerTeam = "UPDATE players SET current_team = ? WHERE playername = ?";
        String sqlItemPlayer = "INSERT INTO item_player (playername, item_id, amount) VALUES (?, ?, ?) ON CONFLICT (playername, item_id) DO NOTHING";

        try (PreparedStatement psPlayer = connection.prepareStatement(sqlPlayer);
                PreparedStatement psTeam = connection.prepareStatement(sqlTeam);
                PreparedStatement psTeamMember = connection.prepareStatement(sqlTeamMember);
                PreparedStatement psUpdatePlayerTeam = connection.prepareStatement(sqlUpdatePlayerTeam);
                PreparedStatement psItemPlayer = connection.prepareStatement(sqlItemPlayer)) {

            int batchSize = 0;
            for (Player p : players) {
                psPlayer.setString(1, p.getPlayername());
                psPlayer.setString(2, p.getPasswordHash());
                psPlayer.addBatch();

                if (p.getTeams() != null) {
                    for (Team t : p.getTeams()) {
                        psTeam.setString(1, p.getPlayername());
                        psTeam.setString(2, t.getName());
                        psTeam.addBatch();

                        if (t.getMembers() != null) {
                            int slot = 1;
                            for (Pokemon member : t.getMembers()) {
                                psTeamMember.setString(1, p.getPlayername());
                                psTeamMember.setString(2, t.getName());
                                psTeamMember.setInt(3, member.getId());
                                psTeamMember.setInt(4, slot++);
                                psTeamMember.addBatch();
                            }
                        }
                    }
                }

                if (p.getCurrentTeam() != null) {
                    psUpdatePlayerTeam.setString(1, p.getCurrentTeam().getName());
                    psUpdatePlayerTeam.setString(2, p.getPlayername());
                    psUpdatePlayerTeam.addBatch();
                }

                if (p.getInventory() != null) {
                    for (Map.Entry<Item, Integer> entry : p.getInventory().entrySet()) {
                        psItemPlayer.setString(1, p.getPlayername());
                        psItemPlayer.setInt(2, entry.getKey().getId());
                        psItemPlayer.setInt(3, entry.getValue());
                        psItemPlayer.addBatch();
                    }
                }

                if (++batchSize % 5000 == 0) {
                    executeAllBatches(psPlayer, psTeam, psTeamMember, psUpdatePlayerTeam, psItemPlayer);
                }
            }

            executeAllBatches(psPlayer, psTeam, psTeamMember, psUpdatePlayerTeam, psItemPlayer);
            connection.commit();

        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public int getPlayerWithRelations(String playername) throws Exception {
        String sql = """
                SELECT p.playername, t.name, tm.pokemon_id, ip.item_id, ip.amount
                FROM players p
                LEFT JOIN teams t ON t.playername = p.playername
                LEFT JOIN team_members tm ON tm.playername = t.playername AND tm.team_name = t.name
                LEFT JOIN item_player ip ON ip.playername = p.playername
                WHERE p.playername = ?
                """;

        int rows = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playername);
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultSet.getString(1);
                    resultSet.getString(2);
                    resultSet.getObject(3);
                    resultSet.getObject(4);
                    resultSet.getObject(5);
                    rows++;
                }
            }
        }
        if (rows == 0) {
            throw new IllegalArgumentException("Player not found: " + playername);
        }
        return rows;
    }

    @Override
    public void updateInventoryAmount(String playername, String itemName, int amount) throws Exception {
        String sql = """
                UPDATE item_player ip
                SET amount = ?
                FROM items i
                WHERE ip.playername = ? AND ip.item_id = i.item_id AND i.name = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, amount);
            statement.setString(2, playername);
            statement.setString(3, itemName);
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException(
                        "Inventory item not found for player: " + playername + ", " + itemName);
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    private void executeAllBatches(PreparedStatement psPlayer,
            PreparedStatement psTeam, PreparedStatement psTeamMember,
            PreparedStatement psUpdatePlayerTeam, PreparedStatement psItemPlayer) throws Exception {
        psPlayer.executeBatch();
        psTeam.executeBatch();
        psTeamMember.executeBatch();
        psUpdatePlayerTeam.executeBatch();
        psItemPlayer.executeBatch();
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

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

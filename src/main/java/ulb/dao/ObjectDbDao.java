package ulb.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ulb.object_db_model.Player;

import java.io.File;
import java.util.List;

public class ObjectDbDao implements BenchmarkDao {
    private final String dbPath;
    private EntityManagerFactory emf;
    private EntityManager em;

    public ObjectDbDao(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void init() {
        this.reopenDatabase();
    }

    private void reopenDatabase() {
        if (em != null && em.isOpen())
            em.close();
        if (emf != null && emf.isOpen())
            emf.close();
        this.emf = Persistence.createEntityManagerFactory(dbPath);
        this.em = emf.createEntityManager();
    }

    @Override
    public void clearDatabase() {
        if (em != null && em.isOpen())
            em.close();
        if (emf != null && emf.isOpen())
            emf.close();

        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        this.emf = Persistence.createEntityManagerFactory(dbPath);
        this.em = emf.createEntityManager();
    }

    @Override
    public void insertPlayers(List<Player> players) {
        em.getTransaction().begin();
        int count = 0;

        for (Player player : players) {
            em.persist(player);

            // Regularly flush and clear to avoid keeping everything in the JVM memory.
            if (++count % 5000 == 0) {
                em.flush();
                em.clear();
            }
        }

        em.getTransaction().commit();
        em.clear();
    }

    @Override
    public int getPlayerWithRelations(String playername) {
        Player player = em.find(Player.class, playername);
        if (player == null) {
            throw new IllegalArgumentException("Player not found: " + playername);
        }

        int relatedObjects = 0;
        if (player.getCurrentTeam() != null) {
            relatedObjects++;
        }
        if (player.getTeams() != null) {
            for (var team : player.getTeams()) {
                relatedObjects++;
                if (team.getMembers() != null) {
                    relatedObjects += team.getMembers().size();
                }
            }
        }
        if (player.getInventory() != null) {
            relatedObjects += player.getInventory().size();
            player.getInventory().keySet().forEach(item -> item.getName());
        }
        return relatedObjects;
    }

    @Override
    public void updateInventoryAmount(String playername, String itemName, int amount) {
        em.getTransaction().begin();
        try {
            Player player = em.find(Player.class, playername);
            if (player == null || player.getInventory() == null) {
                throw new IllegalArgumentException("Player or inventory not found: " + playername);
            }

            var matchingItem = player.getInventory().keySet().stream()
                    .filter(item -> itemName.equals(item.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Inventory item not found for player: " + playername + ", " + itemName));
            player.getInventory().put(matchingItem, amount);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public void close() {
        if (em != null && em.isOpen())
            em.close();
        if (emf != null && emf.isOpen())
            emf.close();
    }
}

package ulb.object_box_model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class Player {

    @Id
    public long id;
    @Index
    private String playername;
    private String passwordHash;
    private ToOne<Team> currentTeam;
    private ToMany<Team> teams;
    private ToMany<InventoryEntry> inventoryItemsCount;

    public Player() {
    }

    public Player(String playername, String passwordHash) {
        this.id = 0; // ObjectBox will assign a unique ID when the object is stored
        this.playername = playername;
        this.passwordHash = passwordHash;
    }

    public long getId() {
        return id;
    }

    public String getPlayername() {
        return playername;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public ToOne<Team> getCurrentTeam() {
        return currentTeam;
    }

    public ToMany<Team> getTeams() {
        return teams;
    }

    public ToMany<InventoryEntry> getInventoryItemsCount() {
        return inventoryItemsCount;
    }
}

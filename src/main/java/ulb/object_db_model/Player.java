package ulb.object_db_model;

import java.util.Map;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Player {
    @Id
    private String playername;

    private String passwordHash;

    @OneToOne(cascade = CascadeType.ALL)
    private Team currentTeam;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "player_id")
    private List<Team> teams;

    @ElementCollection
    private Map<Item, Integer> inventory;

    public Player() {
    }

    public Player(String playername, String passwordHash, Team currentTeam, List<Team> teams,
            Map<Item, Integer> inventory) {
        this.playername = playername;
        this.passwordHash = passwordHash;
        this.currentTeam = currentTeam;
        this.teams = teams;
        this.inventory = inventory;
    }

    // Getters et Setters
    public String getPlayername() {
        return playername;
    }

    public void setPlayername(String playername) {
        this.playername = playername;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Team getCurrentTeam() {
        return currentTeam;
    }

    public void setCurrentTeam(Team currentTeam) {
        this.currentTeam = currentTeam;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Map<Item, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(Map<Item, Integer> inventory) {
        this.inventory = inventory;
    }
}

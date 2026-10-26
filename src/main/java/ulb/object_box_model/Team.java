package ulb.object_box_model;

import io.objectbox.relation.ToMany;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Team {
    @Id
    public long id;

    private String name;
    private ToMany<Pokemon> members;

    public Team() {
    }

    public Team(String name) {
        this.id = 0; // ObjectBox will assign a unique ID when the object is stored
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ToMany<Pokemon> getMembers() {
        return members;
    }
}

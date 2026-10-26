package ulb.object_db_model;

import javax.persistence.Embeddable;

@Embeddable
public class Item {
    private int id;
    private String name;
    private String description;

    protected Item() {
    }

    public Item(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

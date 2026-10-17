package ulb.object_box_model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Item {
    @Id
    public long itemId;
    private String name;
    private String description;

    public Item() {
    }

    public Item(String name, String description) {
        this.itemId = 0; // ObjectBox will assign a unique ID when the object is stored
        this.name = name;
        this.description = description;
    }

    public long getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

package ulb.object_box_model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class InventoryEntry {
    @Id
    public long id;

    private ToOne<Item> item;
    private int amount;

    public InventoryEntry() {
    }

    public InventoryEntry(Item item, int amount) {
        this.id = 0; // ObjectBox will assign a unique ID when the object is stored
        this.item.setTarget(item);
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public ToOne<Item> getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

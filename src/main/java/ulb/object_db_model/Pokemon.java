
package ulb.object_db_model;

import javax.persistence.Embeddable;

@Embeddable
public class Pokemon {
    private final int id;
    private final String name;
    private final String type1;
    private final String type2;
    private final int hp;
    private final int attack;
    private final int defense;
    private final int spAtk;
    private final int spDef;
    private final int speed;
    private final int generation;
    private final boolean legendary;

    public Pokemon(int id, String name, String type1, String type2, int hp, int attack, int defense, int spAtk,
            int spDef, int speed, int generation, boolean legendary) {
        this.id = id;
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spAtk = spAtk;
        this.spDef = spDef;
        this.speed = speed;
        this.generation = generation;
        this.legendary = legendary;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType1() {
        return type1;
    }

    public String getType2() {
        return type2;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpAtk() {
        return spAtk;
    }

    public int getSpDef() {
        return spDef;
    }

    public int getSpeed() {
        return speed;
    }

    public int getGeneration() {
        return generation;
    }

    public boolean isLegendary() {
        return legendary;
    }
}

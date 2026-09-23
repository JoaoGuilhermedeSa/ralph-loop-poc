package ots.charcreate.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps {@code players} - the character, per {@code specs/05-schema.md}.
 * Column names follow TFS conventions on purpose.
 */
@Entity
@Table(name = "players")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_key", nullable = false)
    private String nameKey;

    @Column(name = "vocation", nullable = false)
    private int vocation;

    @Column(name = "sex", nullable = false)
    private int sex;

    @Column(name = "town_id", nullable = false)
    private Integer townId;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "experience", nullable = false)
    private long experience;

    @Column(name = "health", nullable = false)
    private int health;

    @Column(name = "health_max", nullable = false)
    private int healthMax;

    @Column(name = "mana", nullable = false)
    private int mana;

    @Column(name = "mana_max", nullable = false)
    private int manaMax;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "look_type", nullable = false)
    private int lookType;

    @Column(name = "look_head", nullable = false)
    private int lookHead;

    @Column(name = "look_body", nullable = false)
    private int lookBody;

    @Column(name = "look_legs", nullable = false)
    private int lookLegs;

    @Column(name = "look_feet", nullable = false)
    private int lookFeet;

    @Column(name = "pos_x", nullable = false)
    private int posX;

    @Column(name = "pos_y", nullable = false)
    private int posY;

    @Column(name = "pos_z", nullable = false)
    private int posZ;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PlayerEntity() {
    }

    public PlayerEntity(Long accountId, String name, String nameKey, int vocation, int sex, Integer townId,
            int level, long experience, int health, int healthMax, int mana, int manaMax, int capacity,
            int lookType, int lookHead, int lookBody, int lookLegs, int lookFeet, int posX, int posY, int posZ,
            LocalDateTime createdAt) {
        this.accountId = accountId;
        this.name = name;
        this.nameKey = nameKey;
        this.vocation = vocation;
        this.sex = sex;
        this.townId = townId;
        this.level = level;
        this.experience = experience;
        this.health = health;
        this.healthMax = healthMax;
        this.mana = mana;
        this.manaMax = manaMax;
        this.capacity = capacity;
        this.lookType = lookType;
        this.lookHead = lookHead;
        this.lookBody = lookBody;
        this.lookLegs = lookLegs;
        this.lookFeet = lookFeet;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getNameKey() {
        return nameKey;
    }

    public int getVocation() {
        return vocation;
    }

    public int getSex() {
        return sex;
    }

    public Integer getTownId() {
        return townId;
    }

    public int getLevel() {
        return level;
    }

    public long getExperience() {
        return experience;
    }

    public int getHealth() {
        return health;
    }

    public int getHealthMax() {
        return healthMax;
    }

    public int getMana() {
        return mana;
    }

    public int getManaMax() {
        return manaMax;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getLookType() {
        return lookType;
    }

    public int getLookHead() {
        return lookHead;
    }

    public int getLookBody() {
        return lookBody;
    }

    public int getLookLegs() {
        return lookLegs;
    }

    public int getLookFeet() {
        return lookFeet;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosZ() {
        return posZ;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

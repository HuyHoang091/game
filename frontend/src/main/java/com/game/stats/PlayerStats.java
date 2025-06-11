package com.game.stats;

import com.game.data.GameData;
import com.game.model.*;

public class PlayerStats {
    private int basemaxHealth = 500;
    private Long maxHealth;
    private Long health;
    private int basemaxmana = 500;
    private Long maxmana;
    private Long mana;
    private int baseatk = 20;
    private Long atk;
    private int basedef = 2;
    private Long def;
    private double basecritRate = 0.0001;
    private double critRate;
    private double basecritDmg = 1.2;
    private double critDmg;
    private int expBase = 100;
    private Long characterId;

    public PlayerStats(Long characterId) {
        this.characterId = characterId;
        ChiSoGoc();
        ChiSoTB();
    }

    public void ChiSoGoc() {
        GameCharacter character = GameData.character.stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElse(null);

        if (character == null) {
            System.out.println("Character not found: " + characterId);
            return;
        }

        int level = character.getLevel();
        double levelMultiplier = Math.pow(1.1, level);
        maxHealth = (long)(basemaxHealth * levelMultiplier);
        maxmana = (long)(basemaxmana * levelMultiplier);
        health = maxHealth;
        mana = maxmana;
        atk = (long)(baseatk * levelMultiplier);
        def = (long)(basedef * levelMultiplier);
        critRate = Math.min(1.0, basecritRate * levelMultiplier);
        critDmg = Math.min(12.0, basecritDmg + 0.1 * level);
    }

    public void ChiSoGocGL() {
        double healthRatio = health / (double)maxHealth;
        double manaRatio = mana / (double)maxmana;
        GameCharacter character = GameData.character.stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElse(null);

        if (character == null) return;

        int level = character.getLevel();
        double levelMultiplier = Math.pow(1.1, level);
        maxHealth = (long)(basemaxHealth * levelMultiplier);
        maxmana = (long)(basemaxmana * levelMultiplier);
        health = (long)(maxHealth * healthRatio);
        mana = (long)(maxmana * manaRatio);
        atk = (long)(baseatk * levelMultiplier);
        def = (long)(basedef * levelMultiplier);
        critRate = Math.min(1.0, basecritRate * levelMultiplier);
        critDmg = Math.min(12.0, basecritDmg + 0.1 * level);
    }

    public void ChiSoTB() {
        for (GameInventory item : GameData.inventory) {
            if (item.isEquipped()) {
                GameItemInstance instance = GameData.itemInstance.stream()
                    .filter(i -> i.getId().equals(item.getItemInstanceId()))
                    .findFirst()
                    .orElse(null);
                if (instance != null) {
                    maxHealth += instance.getHp();
                    maxmana += instance.getMp();
                    health += instance.getHp();
                    mana += instance.getMp();
                    atk += instance.getAtk();
                    def += instance.getDef();
                    critRate += instance.getCritRate();
                    critDmg += instance.getCritDmg();
                }
            }
        }
    }

    public void takeDamage(Long damage, double damageReduction) {
        health -= (long)(damage - damage * damageReduction);
        if (health < 0) health = 0L;
    }

    public void heal(long amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void useMana(int amount) {
        mana -= amount;
        if (mana < 0) mana = 0L;
    }

    // Getters and Setters
    public Long getHealth() { return health; }
    public void setHealth(Long health) { this.health = health; }
    public Long getMaxHealth() { return maxHealth; }
    public void setMaxHealth(Long health) { this.maxHealth = health; }
    public Long getMana() { return mana; }
    public void setMana(Long mana) { this.mana = mana; }
    public Long getMaxMana() { return maxmana; }
    public Long getAtk() { return atk; }
    public Long getDef() { return def; }
    public double getCritRate() { return critRate; }
    public void setCritRate(double critRate) { this.critRate = critRate; }
    public double getCritDmg() { return critDmg; }
    public void setCritDmg(double critDmg) { this.critDmg = critDmg; }
    public int getExpBase() { return expBase; }
}
package com.game.data;

import com.game.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import com.game.DroppedItem;

public class GameData {
    public static GameUser user;
    public static List<GameCharacter> character;
    public static List<GameCharacterSkill> characterSkills = new ArrayList<>();
    public static List<GameInventory> inventory;
    public static List<GameItem> item;
    public static List<GameItemInstance> itemInstance = new ArrayList<>();
    public static List<GameMap> map;
    public static List<GameMonster> monster;
    public static List<GameMonsterDrop> monsterDrop;
    public static List<GameSkill> skills;
    public static List<GameSkillUpdateRequirements> skillUpdate;
    public static List<GameThuocTinh> thuoctinh;
    public static List<DroppedItem> droppedItems = new ArrayList<>(); // Initialize immediately
    public static String token;

    public static void loadItemInstances(List<GameItemInstance> apiInstances) {
        itemInstance = new ArrayList<>(apiInstances); // Create new ArrayList from API data
    }

    public static void loadCharacterSkills(List<GameCharacterSkill> apiCharacterSkill) {
        characterSkills = new ArrayList<>(apiCharacterSkill); // Create new ArrayList from API data
    }

    // Thêm một item vào kho
    public static void addItemToInventory(GameInventory gameInventory) {
        inventory.add(gameInventory);
    }

    public static String getItemIconById(Long itemId) {
        if (item == null) return "assets/items/default.png";
        
        for (GameItem gameItem : item) {
            if (gameItem.getId().equals(itemId)) {
                return gameItem.getIcon();
            }
        }
        return "assets/items/default.png";
    }

    // Thêm phương thức kiểm tra item tồn tại
    public static GameItem getItemById(Long itemId) {
        if (item == null) return null;
        
        for (GameItem gameItem : item) {
            if (gameItem.getId().equals(itemId)) {
                return gameItem;
            }
        }
        return null;
    }

    public static GameItemInstance createItemInstance(Long itemId, int monsterLevel) {
        GameItem item = getItemById(itemId);
        if (item == null) return null;

        GameItemInstance instance = new GameItemInstance();

        instance.setItemId(itemId);
        instance.setId((long) (itemInstance.size() + 1));
        
        // Level multiplier (higher level = better stats)
        float levelMultiplier = 1.0f + (monsterLevel * 1.2f); // 20% increase per level
        Random rand = new Random();

        // Base stats based on item type
        switch (item.getType().toLowerCase()) {
            case "vũ khí":
                // Weapons focus on ATK and crit stats
                instance.setAtk((int)((rand.nextInt(30) + 20) * levelMultiplier));  // 20-50 base ATK
                instance.setDef(0);
                instance.setHp((int)((rand.nextInt(20)) * levelMultiplier));  // 0-20 base HP
                instance.setMp((int)((rand.nextInt(15)) * levelMultiplier));  // 0-15 base MP
                instance.setCritRate(rand.nextDouble() * 0.03 * levelMultiplier);
                instance.setCritDmg((rand.nextDouble() * 0.05 + 0.1) * levelMultiplier);
                break;

            case "mũ giáp":
                // Armor focuses on DEF and HP
                instance.setAtk(0);
                instance.setDef((int)((rand.nextInt(20) + 10) * levelMultiplier));  // 10-30 base DEF
                instance.setHp((int)((rand.nextInt(50) + 50) * levelMultiplier));  // 50-100 base HP
                instance.setMp((int)((rand.nextInt(20)) * levelMultiplier));  // 0-20 base MP
                instance.setCritRate(0.0);
                instance.setCritDmg(0.0);
                break;

            case "áo giáp":
                // Accessories give mixed stats
                instance.setAtk((int)((rand.nextInt(10)) * levelMultiplier));  // 0-10 base ATK
                instance.setDef((int)((rand.nextInt(10)) * levelMultiplier));  // 0-10 base DEF
                instance.setHp((int)((rand.nextInt(30)) * levelMultiplier));  // 0-30 base HP
                instance.setMp((int)((rand.nextInt(30)) * levelMultiplier));  // 0-30 base MP
                instance.setCritRate(rand.nextDouble() * 0.015 * levelMultiplier);
                instance.setCritDmg(rand.nextDouble() * 0.03 * levelMultiplier);
                break;

            case "giày giáp":
                // Staves focus on MP and magic damage
                instance.setAtk((int)((rand.nextInt(20) + 10) * levelMultiplier));  // 10-30 base ATK
                instance.setDef((int)((rand.nextInt(10)) * levelMultiplier));  // 0-10 base DEF
                instance.setHp((int)((rand.nextInt(20)) * levelMultiplier));  // 0-20 base HP
                instance.setMp((int)((rand.nextInt(40) + 20) * levelMultiplier));  // 20-60 base MP
                instance.setCritRate(rand.nextDouble() * 0.02 * levelMultiplier);
                instance.setCritDmg(rand.nextDouble() * 0.04 * levelMultiplier);
                break;

            default:
                // Default balanced stats
                instance.setAtk((int)((rand.nextInt(20)) * levelMultiplier));
                instance.setDef((int)((rand.nextInt(20)) * levelMultiplier));
                instance.setHp((int)((rand.nextInt(30)) * levelMultiplier));
                instance.setMp((int)((rand.nextInt(20)) * levelMultiplier));
                instance.setCritRate(rand.nextDouble() * 0.01 * levelMultiplier);
                instance.setCritDmg(rand.nextDouble() * 0.02 * levelMultiplier);
                break;
        }
        Long nextId = itemInstance.stream()
            .map(GameItemInstance::getId)
            .max(Long::compareTo)
            .orElse(0L) + 1;
            
        instance.setItemId(itemId);
        instance.setId(nextId);

        itemInstance.add(instance);
        return instance;
    }

    public static GameCharacter getCharacterById(Long id) {
        return character.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public static void clear() {
        user = null;
        character = null;
        characterSkills = null;
        inventory = null;
        item = null;
        itemInstance = null;
        map = null;
        monster = null;
        monsterDrop = null;
        skills = null;
        skillUpdate = null;
        thuoctinh = null;
        if (droppedItems != null) {
            droppedItems = null;
        }
        token = null;
    }
}
// package com.game.stats;

// import com.game.data.GameData;
// import com.game.model.GameMonster;

// public class EnemyStats {
//     private Long basemaxHealth;
//     private Long maxHealth;
//     private Long health;
//     private int baseatk = 5;
//     private Long atk;
//     private int basedef = 2;
//     private Long def;
//     private int speed = 2;
//     private int level;
//     private String name;
//     private String type;
//     private Long monsterId;

//     public EnemyStats(Long baseHealth, Long monsterId) {
//         this.basemaxHealth = baseHealth;
//         this.monsterId = monsterId;
//         calculateBaseStats();
//     }

//     public void calculateBaseStats() {
//         GameMonster monster = GameData.monster.stream()
//                 .filter(c -> c.getId().equals(monsterId))
//                 .findFirst()
//                 .orElse(null);

//         if (monster == null) {
//             System.out.println("Monster not found: " + monsterId);
//             return;
//         }

//         this.level = monster.getLevel();
//         this.name = monster.getName();
//         this.type = monster.getBehavior();

//         double levelMultiplier = Math.pow(1.2, level); // tăng 10% mỗi cấp
//         maxHealth = (long) (basemaxHealth * levelMultiplier);
//         health = maxHealth;
//         atk = (long) (baseatk * levelMultiplier);
//         def = (long) (basedef * levelMultiplier);
//     }

//     public void heal(int amount) {
//         if (health < maxHealth) {
//             health += amount;
//             if (health > maxHealth) {
//                 health = maxHealth;
//             }
//         }
//     }

//     // Getters and Setters
//     public Long getHealth() {
//         return health;
//     }

//     public void setHealth(Long value) {
//         health = value;
//     }

//     public Long getMaxHealth() {
//         return maxHealth;
//     }

//     public Long getAtk() {
//         return atk;
//     }

//     public void setAtk(Long value) {
//         atk = value;
//     }

//     public Long getDef() {
//         return def;
//     }

//     public int getSpeed() {
//         return speed;
//     }

//     public void setSpeed(int value) {
//         speed = value;
//     }

//     public int getLevel() {
//         return level;
//     }

//     public String getName() {
//         return name;
//     }

//     public String getType() {
//         return type;
//     }

//     public void takeDamage(Long damage) {
//         health -= damage;
//         if (health < 0)
//             health = 0L;
//     }

//     public boolean isDead() {
//         return health <= 0;
//     }
// }
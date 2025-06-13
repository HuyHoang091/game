package com.game.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.Arc2D;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
import java.util.List;

import com.game.*;
import java.net.*;
import com.game.data.GameData;
import com.game.model.*;
import com.game.core.*;
import com.game.rendering.*;
import com.game.resource.*;
import com.game.state.*;

public class GamePanel extends JPanel {
    private GameLoop gameLoop;
    private InputHandler inputHandler;
    private GameRenderer renderer;
    private BossRoomState bossRoomState;
    // Các hằng số cấu hình chung
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 700;

    // Quản lý đối tượng game
    private Player player;
    private Long playerId;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<SkillEffect> skills = new ArrayList<>();
    private MapData currentMapData;
    
    // Quản lý kỹ năng
    private Rectangle normalAttackBounds;
    private Rectangle[] skillButtonBounds;
    private BufferedImage[] skillIcons;
    private boolean[] skillUnlocked;
    private Map<Long, SkillData> skillDataMap = new HashMap<>();
    
    // Tài nguyên hình ảnh
    private BufferedImage mapImage;
    private BufferedImage hudImage;
    private BufferedImage danhthuong;
    private BufferedImage[] up, down, left, right, skillEffectFramesL, skillEffectFramesR, idle, die;
    
    // Quản lý game thread
    private static GamePanel currentInstance;

    // Cooldown management
    private long lastNormalAttackTime = 0;
    private double attackSpeed = 5.0; // Attacks per second
    private Map<Integer, Long> skillLastUsedTime = new HashMap<>(); // Slot -> last used time
    private Map<Integer, Integer> skillCooldowns = new HashMap<>(); // Slot -> cooldown in milliseconds

    private boolean nearPortal = false;
    private boolean showPortalPrompt = false;
    private static final int PORTAL_SIZE = 50;
    private static final int INTERACTION_RANGE = 100;
    
    // Add portal position
    private int portalX = 740; // Set your desired X position
    private int portalY = 760; // Set your desired Y position
    private boolean isBossRoom = false;
    private boolean gameEnding = false;
    private boolean Loss = false;
    private long gameEndTime = 0;
    private static final int END_GAME_DELAY = 3000; // 3 seconds
    private boolean isBossInitialized = false;
    private boolean isLoadingMap = false;

    private GameWindow gameWindow;

    Enemy nearestEnemy = null;
    double nearestDistance = Double.MAX_VALUE;

    private boolean paused = false;

    // Constructor và khởi tạo
    public GamePanel(GameWindow gameWindow) {
        this.gameLoop = new GameLoop(this);
        this.gameWindow = gameWindow;
        currentInstance = this;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        inputHandler = new InputHandler(this);
        this.addKeyListener(inputHandler);
        this.addMouseListener(inputHandler);

        renderer = new GameRenderer(this);
        bossRoomState = new BossRoomState(this);

        skillButtonBounds = new Rectangle[4];
        skillIcons = new BufferedImage[4];
        skillUnlocked = new boolean[4];

        for (int i = 0; i < 4; i++) {
            skillButtonBounds[i] = new Rectangle();
        }
        // Initialize cooldown maps
        for (int i = 0; i < 4; i++) {
            skillLastUsedTime.put(i, 0L);
            skillCooldowns.put(i, 0);
        }
    }

    // region Quản lý tài nguyên
    public void loadResources(MapData mapData) {
        this.currentMapData = mapData;
        try {
            BufferedImage collisionImage;
            if (isBossRoom) {
                mapImage = ImageIO.read(getClass().getClassLoader().getResource("assets/bossroom.png"));
                collisionImage = ImageIO.read(getClass().getClassLoader().getResource("assets/collision_boss.png"));
            } else {
                mapImage = ImageIO.read(getClass().getClassLoader().getResource(mapData.mapBackground));
                collisionImage = ImageIO.read(getClass().getClassLoader().getResource(mapData.collisionLayer));
            }

            boolean[][] collisionMap = generateCollisionMap(collisionImage);

            hudImage = ImageIO.read(getClass().getClassLoader().getResource("assets/Khung/image.png"));
            danhthuong = ImageIO.read(getClass().getClassLoader().getResource("assets/Skill/danhthuong.png"));
    
            for (GameCharacter character : GameData.character) {
                if (character.getClassName().equals("LangKhach")) {
                    up = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "runR", 8);
                    down = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "runR", 8);
                    left = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "runL", 8);
                    right = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "runR", 8);
                    skillEffectFramesL = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "attackL", 9);
                    skillEffectFramesR = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "attackR", 9);
                    idle = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "idle", 6);
                    die = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "die", 12);

                    playerId = character.getId();
                }
            }
            BufferedImage[] skill1Frames = ResourceManager.getEffectAnimation("assets/Skill", "cat_nuoc");
            for (int i = 0; i < skill1Frames.length; i++) {
                skill1Frames[i] = ResourceManager.resizeImage(skill1Frames[i], 250, 250);
            }
            SkillData fireSkill = new SkillData(skill1Frames, 30, 10, Color.RED);
            player = new Player(300, 250, up, down, left, right, skillEffectFramesL, skillEffectFramesR, idle, die, collisionImage, playerId);

            player.addSkill(fireSkill);

            loadSkills();
            loadSkillIcons();

            enemies.clear();
            for (MapData.EnemyData enemyData : mapData.enemies) {
                String type = "Enemy";
                int count = 10;
                if (isBossRoom) {
                    type = "Boss";
                    count = 1;
                }
                if (enemyData.type.equals(type)) {
                    enemies.addAll(generateRandomEnemies(
                        count,
                        collisionMap,
                        mapImage.getWidth(),
                        mapImage.getHeight(),
                        enemyData.width,
                        enemyData.height,
                        enemyData.name,
                        enemyData.monsterId,
                        enemyData.health
                    ));
                }
            }
            
        } catch (IOException e) {
            System.out.println("Error loading images: " + e.getMessage());
        }
    }      

    public List<Enemy> generateRandomEnemies(
        int count, boolean[][] collisionMap, int mapWidth, int mapHeight,
        int monsterWidth, int monsterHeight, String monsterName, Long monsterId, Long health
    ) {
        List<Enemy> generatedEnemies = new ArrayList<>();
        Random rand = new Random();
        int spacing = 200;
        int maxAttempts = count * 100;

        while (generatedEnemies.size() < count && maxAttempts > 0) {
            int x = rand.nextInt(mapWidth - monsterWidth) + monsterWidth / 2;
            int y = rand.nextInt(mapHeight - monsterHeight) + monsterHeight / 2;

            if (!canSpawnAt(x, y + monsterHeight / 10, monsterWidth, monsterHeight / 10, collisionMap)) {
                maxAttempts--;
                continue;
            }

            boolean tooClose = false;
            for (Enemy e : generatedEnemies) {
                double dist = Point.distance(x, y, e.getX(), e.getY());
                if (dist < spacing) {
                    tooClose = true;
                    break;
                }
            }

            if (tooClose) {
                maxAttempts--;
                continue;
            }

            Enemy enemy = createEnemy(x, y, monsterWidth, monsterHeight, health, monsterId, monsterName, false);
            generatedEnemies.add(enemy);
            maxAttempts--;
        }

        return generatedEnemies;
    }

    public static boolean canSpawnAt(int x, int y, int w, int h, boolean[][] collisionMap) {
        int startX = Math.max(0, x - w / 2);
        int startY = Math.max(0, y - h / 2);
        int endX = Math.min(collisionMap.length - 1, x + w / 2);
        int endY = Math.min(collisionMap[0].length - 1, y + h / 2);

        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if (collisionMap[i][j]) {
                    return false; // Gặp vùng va chạm
                }
            }
        }

        return true;
    }

    public static boolean[][] generateCollisionMap(BufferedImage collisionImage) {
        int width = collisionImage.getWidth();
        int height = collisionImage.getHeight();
        boolean[][] map = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = collisionImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                map[x][y] = alpha != 0;  // true = có va chạm, false = đi được
            }
        }

        return map;
    }

    public Enemy createEnemy(int x, int y, int width, int height, Long health, Long monsterId, String name, boolean trieuhoi) {
        BufferedImage[] upFrames = ResourceManager.getEnemyAnimation(name, "runR");
        BufferedImage[] downFrames = ResourceManager.getEnemyAnimation(name, "runR");
        BufferedImage[] leftFrames = ResourceManager.getEnemyAnimation(name, "runL");
        BufferedImage[] rightFrames = ResourceManager.getEnemyAnimation(name, "runR");
        BufferedImage[] attackFramesL = ResourceManager.getEnemyAnimation(name, "attackL");
        BufferedImage[] attackFramesR = ResourceManager.getEnemyAnimation(name, "attackR");
        BufferedImage[] buffSkill = ResourceManager.getEnemyAnimation(name, "buff");
        BufferedImage[] targetSkill = ResourceManager.getEnemyAnimation(name, "skill");
        BufferedImage[] idle = ResourceManager.getEnemyAnimation(name, "idle");
        BufferedImage[] die = ResourceManager.getEnemyAnimation(name, "die");
        
        BufferedImage[] explosion = ResourceManager.getEffectAnimation("assets/Skill", "cat_nuoc");
        BufferedImage[] buffEffect = ResourceManager.getEffectAnimation("assets/Skill/Quanh_than/Buff", "buff_dien");

        return new Enemy(x, y, width, height, health, monsterId,
                        upFrames, downFrames, leftFrames, rightFrames, attackFramesL, attackFramesR, buffSkill, buffEffect, targetSkill, explosion, idle, die, trieuhoi);
    }

    public void loadSkills() {
        if (GameData.characterSkills != null) {
            for (GameCharacterSkill charSkill : GameData.characterSkills) {
                if (charSkill.getCharacterId().equals(playerId)) {
                    // Lấy thông tin skill từ GameData
                    GameSkill skillInfo = GameData.skills.stream()
                        .filter(s -> s.getId().equals(charSkill.getSkillId()))
                        .findFirst()
                        .orElse(null);

                    if (skillInfo != null) {
                        try {
                            // Lấy cấu hình trực tiếp từ skillInfo.getCauhinh()
                            String[] config = skillInfo.getCauhinh().split(",");
                            String animationPath = config[0].trim();          // "assets/Skill/Cat_nuoc"
                            int frameCount = Integer.parseInt(config[1].trim()); // 10
                            int width = Integer.parseInt(config[2].trim());      // 250
                            int height = Integer.parseInt(config[3].trim());     // 250

                            // Load animation frames với cấu hình từ DB
                            BufferedImage[] skillFrames = AnimationLoader.loadSkillFrames(
                                animationPath, 
                                frameCount,
                                width,
                                height
                            );

                            // Tạo SkillData với thông số từ DB
                            SkillData skillData = new SkillData(
                                skillFrames,
                                skillInfo.getDamage(),
                                skillInfo.getManaCost(),
                                Color.RED // Chuyển mã màu hex thành Color
                            );

                            // Lưu vào map và thêm vào player
                            skillDataMap.put(skillInfo.getId(), skillData);
                            player.addSkill(skillData);

                            // Lưu cooldown cho slot
                            if (charSkill.getSlot() > 0) {
                                skillCooldowns.put(charSkill.getSlot() - 1, 
                                    (int)(skillInfo.getCooldown() * 1000)); // Convert to milliseconds
                            }

                        } catch (IOException | NumberFormatException e) {
                            System.err.println("Lỗi tải animation cho skill " + 
                                            skillInfo.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void loadSkillIcons() {
        Arrays.fill(skillIcons, null);
        Arrays.fill(skillUnlocked, false);

        if (GameData.characterSkills != null) {
            for (GameCharacterSkill charSkill : GameData.characterSkills) {
                if (charSkill.getCharacterId().equals(playerId)) {
                    int slot = charSkill.getSlot() - 1;
                    if (slot >= 0 && slot < 4) {
                        GameSkill skill = GameData.skills.stream()
                            .filter(s -> s.getId().equals(charSkill.getSkillId()))
                            .findFirst()
                            .orElse(null);
                            
                        if (skill != null) {
                            try {
                                skillIcons[slot] = ImageIO.read(
                                    getClass().getClassLoader().getResource(skill.getIcon())
                                );
                                skillUnlocked[slot] = true;
                            } catch (IOException e) {
                                System.err.println("Error loading skill icon: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }
    // endregion

    // region Quản lý game loop
    
    public void update() {
        if (paused) return;
        
        if (player != null) {
            player.update();
    
            // Check for game end condition
            if (gameEnding) {
                if (System.currentTimeMillis() - gameEndTime >= END_GAME_DELAY) {
                    SwingUtilities.invokeLater(() -> {
                        if (GameData.user.getTiendo() < gameWindow.getInstance().getLevel()) {
                            GameData.user.setTiendo(gameWindow.getInstance().getLevel());
                        }
                        gameWindow.BackToMenu();
                    });
                }
                return;
            }

            // Skip boss checks while loading
            if (!isLoadingMap) {
                // Initialize boss when entering boss room
                if (isBossRoom && !isBossInitialized && !enemies.isEmpty()) {
                    isBossInitialized = true;
                }

                java.util.List<Enemy> currentEnemies = new ArrayList<>(enemies);

                nearestDistance = Double.MAX_VALUE;
                nearestEnemy = null;
        
                // Update existing enemies
                for (Enemy enemy : currentEnemies) {
                    enemy.update(player);
                    
                    // Add any summoned enemies from this enemy
                    if (!enemy.getPendingEnemies().isEmpty()) {
                        enemies.addAll(enemy.getPendingEnemies());
                        enemy.clearPendingEnemies();
                    }

                    double dx = player.getX() - enemy.getX();
                    double dy = player.getY() - enemy.getY();
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance < nearestDistance && distance <= 400) {
                        nearestDistance = distance; 
                        nearestEnemy = enemy;
                    }
                }

                // Only check for boss death if fully initialized and not loading
                if (isBossRoom && isBossInitialized && !isLoadingMap && enemies.isEmpty()) {
                    gameEnding = true;
                    gameEndTime = System.currentTimeMillis();
                }
            }
    
            enemies.removeIf(Enemy::isDead);

            if (player.isDead()) {
                Loss = true;
                gameEnding = true;
                gameEndTime = System.currentTimeMillis();
            }
            
            for (SkillEffect skill : skills) {
                if (skill != null) {
                    skill.update();
                }
            }
            skills.removeIf(SkillEffect::isExpired);

            // Only check portal proximity if not in boss room
            if (!isBossRoom) {
                int playerCenterX = player.getX() + player.getWidth()/2;
                int playerCenterY = player.getY() + player.getHeight()/2;
                
                double distanceToPortal = Math.sqrt(
                    Math.pow(playerCenterX - (portalX + PORTAL_SIZE/2), 2) +
                    Math.pow(playerCenterY - (portalY + PORTAL_SIZE/2), 2)
                );
                
                nearPortal = distanceToPortal < INTERACTION_RANGE;
                showPortalPrompt = nearPortal;
            }
        }
    }
    // endregion

    // region Vẽ giao diện
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render((Graphics2D)g);
    }
    // endregion

    // region Tiện ích

    public void loadBossRoom() {
        GlobalLoadingManager loadingManager = new GlobalLoadingManager(gameWindow);
        
        loadingManager.startLoading(() -> {
            
        });

        new Thread(() -> {
            isLoadingMap = true;

            isBossRoom = true;
            isBossInitialized = false;

            loadResources(currentMapData);

            isLoadingMap = false;

            loadingManager.setLoading(false);
        }).start();
    }
    // endregion

    // region Các getter/setter cho Player và trạng thái game
    public static GamePanel getInstance() { return currentInstance; }
    public Player getPlayer() { return player; }
    public Long getPlayerId() { return playerId; }
    public GameLoop getGameLoop() { return gameLoop; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<SkillEffect> getSkills() { return skills; }
    // endregion

    // region Các getter/setter cho hệ thống kỹ năng
    public Rectangle[] getSkillButtonBounds() { return skillButtonBounds; }
    public Rectangle getNormalAttackBounds() { return normalAttackBounds; }
    public boolean[] getSkillUnlocked() { return skillUnlocked; }
    public Map<Long, SkillData> getSkillDataMap() { return skillDataMap; }
    public BufferedImage getSkillIcon(int index) { return skillIcons[index]; }
    public boolean isSkillUnlocked(int index) { return skillUnlocked[index]; }
    public void setSkillButtonBound(int index, Rectangle bounds) { 
        skillButtonBounds[index] = bounds; 
    }
    public void setNormalAttackBounds(Rectangle bounds) { 
        normalAttackBounds = bounds; 
    }
    // endregion

    // region Quản lý thời gian hồi chiêu (cooldown)
    public long getLastNormalAttackTime() { return lastNormalAttackTime; }
    public double getAttackSpeed() { return attackSpeed; }
    public long getSkillLastUsedTime(int slot) { return skillLastUsedTime.get(slot); }
    public int getSkillCooldown(int slot) { return skillCooldowns.get(slot); }
    public void updateSkillLastUsedTime(int slot, long time) { 
        skillLastUsedTime.put(slot, time); 
    }
    public void updateNormalAttackCooldown(long time) {
        lastNormalAttackTime = time;
    }
    public void addSkill(SkillEffect skill) { skills.add(skill); }
    // endregion

    // region Getter cho tài nguyên hình ảnh
    public BufferedImage getMapImage() { return mapImage; }
    public BufferedImage getHudImage() { return hudImage; }
    public BufferedImage getDanhthuong() { return danhthuong; }
    // endregion

    // region Trạng thái cổng và phòng boss
    public boolean isNearPortal() { return nearPortal; }
    public boolean isBossRoom() { return isBossRoom; }
    public boolean isGameEnding() { return gameEnding; }
    public boolean isLoss() { return Loss; }
    public boolean isShowPortalPrompt() { return showPortalPrompt; }
    public int getPortalX() { return portalX; }
    public int getPortalY() { return portalY; }
    public int getEndGameSecondsLeft() {
        return (int)Math.ceil((END_GAME_DELAY - (System.currentTimeMillis() - gameEndTime)) / 1000.0);
    }

    public Enemy getNearestEnemy() {
        return nearestEnemy;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public GameRenderer getRenderer() {
        return renderer;
    }
    // endregion
}
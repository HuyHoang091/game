package com.game.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.Arc2D;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
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
    private BufferedImage[] up, down, left, right;
    
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
    private int portalX = 300; // Set your desired X position
    private int portalY = 250; // Set your desired Y position
    private boolean isBossRoom = false;
    private boolean gameEnding = false;
    private long gameEndTime = 0;
    private static final int END_GAME_DELAY = 3000; // 3 seconds
    private boolean isBossInitialized = false;
    private boolean isLoadingMap = false;

    // Constructor và khởi tạo
    public GamePanel() {
        this.gameLoop = new GameLoop(this);
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
            mapImage = ImageIO.read(getClass().getClassLoader().getResource(mapData.mapBackground));
            BufferedImage collisionImage = ImageIO.read(getClass().getClassLoader().getResource(mapData.collisionLayer));

            hudImage = ImageIO.read(getClass().getClassLoader().getResource("assets/Khung/image.png"));
            danhthuong = ImageIO.read(getClass().getClassLoader().getResource("assets/Skill/danhthuong.png"));
    
            for (GameCharacter character : GameData.character) {
                if (character.getClassName().equals("LangKhach")) {
                    up = AnimationLoader.loadAnimations("assets/Run/LangKhach/Right/", 8);
                    down = AnimationLoader.loadAnimations("assets/Run/LangKhach/Right/", 8);
                    left = AnimationLoader.loadAnimations("assets/Run/LangKhach/Left/", 8);
                    right = AnimationLoader.loadAnimations("assets/Run/LangKhach/Right/", 8);

                    playerId = character.getId();
                }
            }
            BufferedImage[] skill1Frames = AnimationLoader.loadSkillFrames("assets/Skill/Cat_nuoc", 10, 250, 250);
            SkillData fireSkill = new SkillData(skill1Frames, 30, 10, Color.RED);
            BufferedImage[] skillEffectFramesL = AnimationLoader.loadAnimations("assets/Run/LangKhach/Attack/Left/", 9);
            BufferedImage[] skillEffectFramesR = AnimationLoader.loadAnimations("assets/Run/LangKhach/Attack/Right/", 9);
            BufferedImage[] idle = AnimationLoader.loadAnimations("assets/Run/LangKhach/Idle/", 6);
            player = new Player(300, 250, up, down, left, right, skillEffectFramesL, skillEffectFramesR, idle, collisionImage, playerId);

            loadSkills();
            loadSkillIcons();

            player.addSkill(fireSkill);

            enemies.clear();
            for (MapData.EnemyData enemyData : mapData.enemies) {
                enemies.add(createEnemy(
                    enemyData.x,
                    enemyData.y,
                    250,
                    250,
                    enemyData.health,
                    enemyData.monsterId,
                    enemyData.name
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading images: " + e.getMessage());
        }
    }      

    public Enemy createEnemy(int x, int y, int width, int height, Long health, Long monsterId, String name) {
        BufferedImage[] upFrames = ResourceManager.getEnemyAnimations(name, "Right");
        BufferedImage[] downFrames = ResourceManager.getEnemyAnimations(name, "Right");
        BufferedImage[] leftFrames = ResourceManager.getEnemyAnimations(name, "Left");
        BufferedImage[] rightFrames = ResourceManager.getEnemyAnimations(name, "Right");
        BufferedImage[] attackFramesL = ResourceManager.getEnemyAnimations(name, "Attack/Left");
        BufferedImage[] attackFramesR = ResourceManager.getEnemyAnimations(name, "Attack/Right");
        BufferedImage[] buffSkill = ResourceManager.getEnemyAnimations(name, "Buff");
        BufferedImage[] buffEffect = ResourceManager.getSkillAnimations("assets/Skill/Quanh_than/Buff/Dien", "");
        BufferedImage[] targetSkill = ResourceManager.getEnemyAnimations(name, "Skill");
        BufferedImage[] explosion = ResourceManager.getSkillAnimations("assets/Skill/Cat_nuoc", "");
        BufferedImage[] idle = ResourceManager.getEnemyAnimations(name, "Idle");
        BufferedImage[] die = ResourceManager.getEnemyAnimations(name, "Die");

        return new Enemy(x, y, width, height, health, monsterId,
                        upFrames, downFrames, leftFrames, rightFrames, attackFramesL, attackFramesR, buffSkill, buffEffect, targetSkill, explosion, idle, die);
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

                            System.out.println("Đã tải skill: " + skillInfo.getName() + 
                                            " với cấu hình: " + animationPath + ", " + frameCount + "," + width + "," + height);

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
        if (player != null) {
            player.update();
    
            // Check for game end condition
            if (gameEnding) {
                if (System.currentTimeMillis() - gameEndTime >= END_GAME_DELAY) {
                    gameLoop.stopGameThread();
                    SwingUtilities.invokeLater(() -> {
                        Window window = SwingUtilities.getWindowAncestor(this);
                        if (window != null) {
                            window.dispose();
                        }
                        // new MapSelectScreen();
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
        
                // Update existing enemies
                for (Enemy enemy : currentEnemies) {
                    enemy.update(player);
                    
                    // Add any summoned enemies from this enemy
                    if (!enemy.getPendingEnemies().isEmpty()) {
                        enemies.addAll(enemy.getPendingEnemies());
                        enemy.clearPendingEnemies();
                    }
                }

                // Update enemies
                // for (Enemy enemy : enemies) {
                //     enemy.update(player);
                // }

                // Only check for boss death if fully initialized and not loading
                if (isBossRoom && isBossInitialized && !isLoadingMap && enemies.isEmpty()) {
                    gameEnding = true;
                    gameEndTime = System.currentTimeMillis();
                }
            }
    
            enemies.removeIf(Enemy::isDead);
            
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

    // region Xử lý input

    // region Tiện ích

    public void loadBossRoom() {
        isLoadingMap = true;

        // Load boss map data
        MapData bossMapData = new MapData("assets/image.png","assets/vatlieu.png");
        
         // Add boss enemy data
        MapData.EnemyData bossData = new MapData.EnemyData(300, 250, 100, 100, 1000L, 1L, "NightBorne");
        bossData.isBoss = true;
        bossMapData.enemies = new ArrayList<>(); // Initialize enemies list
        bossMapData.enemies.add(bossData);
        
        // Set boss room flag
        isBossRoom = true;
        isBossInitialized = false; // Reset initialization flag
        
        // Load the new map
        loadResources(bossMapData);

        isLoadingMap = false; // Clear loading flag
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
    public boolean isShowPortalPrompt() { return showPortalPrompt; }
    public int getPortalX() { return portalX; }
    public int getPortalY() { return portalY; }
    public int getEndGameSecondsLeft() {
        return (int)Math.ceil((END_GAME_DELAY - (System.currentTimeMillis() - gameEndTime)) / 1000.0);
    }
    // endregion
}
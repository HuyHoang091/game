package com.game.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.game.*;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import com.game.data.GameData;
import com.game.model.*;
import java.util.ConcurrentModificationException;
import java.awt.geom.Arc2D;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    // Các hằng số cấu hình chung
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 700;
    
    // Cấu hình nút kỹ năng
    private static final int SKILL_BUTTON_SIZE = 70;
    private static final int NORMAL_ATTACK_SIZE = 80;
    private static final int SKILL_PADDING = 10;

    // Quản lý đối tượng game
    private Player player;
    private Long playerId;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<SkillEffect> skills = new ArrayList<>();
    private ArrayList<Rectangle> collisionObjects = new ArrayList<>();
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
    private Thread gameThread;
    private volatile boolean isRunning = false;

    private static GamePanel currentInstance;

    // Cooldown management
    private long lastNormalAttackTime = 0;
    private double attackSpeed = 5.0; // Attacks per second
    private Map<Integer, Long> skillLastUsedTime = new HashMap<>(); // Slot -> last used time
    private Map<Integer, Integer> skillCooldowns = new HashMap<>(); // Slot -> cooldown in milliseconds

    // Constructor và khởi tạo
    public GamePanel() {
        currentInstance = this;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point click = e.getPoint();

                if (normalAttackBounds.contains(click)) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastNormalAttackTime >= (1000 / attackSpeed)) {
                        SkillEffect skill = player.castSkill(0, enemies, -50);
                        if (skill != null) {
                            skills.add(skill);
                            lastNormalAttackTime = currentTime;
                        }
                    }
                }

                for (int i = 0; i < 4; i++) {
                    final int index = i;
                    if (skillButtonBounds[i].contains(click) && skillUnlocked[i]) {
                        GameCharacterSkill charSkill = GameData.characterSkills.stream()
                            .filter(cs -> cs.getCharacterId().equals(playerId) && cs.getSlot() == index + 1)
                            .findFirst()
                            .orElse(null);

                        if (charSkill != null) {
                            SkillData skillData = skillDataMap.get(charSkill.getSkillId());
                            if (skillData != null) {
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - skillLastUsedTime.get(i) >= skillCooldowns.get(i)) {
                                    SkillEffect effect = player.castSkill(index + 1, enemies, -50);
                                    if (effect != null) {
                                        skills.add(effect);
                                        skillLastUsedTime.put(i, currentTime);
                                        System.out.println("Sử dụng skill slot " + (i + 1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

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

    public static GamePanel getInstance() {
        return currentInstance;
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
                if (character.getClassName().equals("Lãng khách")) {
                    up = new BufferedImage[] {
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_1.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_2.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_3.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_4.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_5.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_6.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_7.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_8.png"))
                    };
            
                    down = new BufferedImage[] {
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_1.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_2.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_3.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_4.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_5.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_6.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_7.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_8.png"))
                    };
            
                    left = new BufferedImage[] {
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_1_L.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_2_L.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_3_L.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_4_L.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_5_L.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_6_L.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_7_L.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_8_L.png"))
                    };
            
                    right = new BufferedImage[] {
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_1.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_2.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_3.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_4.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_5.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_6.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_7.png")),
                        ImageIO.read(getClass().getClassLoader().getResource("assets/Run/Walk_8.png"))
                    };

                    playerId = character.getId();
                }
            }

            BufferedImage[] skill1Frames = loadFrames("assets/Skill/Cat_nuoc", 10, 250, 250);

            SkillData fireSkill = new SkillData(skill1Frames, 30, 10, Color.RED);

            BufferedImage[] skillEffectFrames = {
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_1.png")),
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_2.png")),
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_3.png")),
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_4.png")),
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_5.png")),
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_6.png")),
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_7.png")),
                ImageIO.read(getClass().getClassLoader().getResource("assets/Player/Attack_8.png"))
            };
    
            player = new Player(300, 250, up, down, left, right, skillEffectFrames, collisionImage, playerId);

            loadSkills();
            loadSkillIcons();

            player.addSkill(fireSkill);

            enemies.clear();
            for (MapData.EnemyData enemyData : mapData.enemies) {
                enemies.add(new Enemy(
                    enemyData.x,
                    enemyData.y,
                    enemyData.width,
                    enemyData.height,
                    enemyData.health,
                    enemyData.monsterId
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading images: " + e.getMessage());
        }
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
                            BufferedImage[] skillFrames = loadFrames(
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

    private BufferedImage[] loadFrames(String folderPath, int count, int width, int height) throws IOException {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            String filePath = folderPath + "/" + (i + 1) + ".png";
            URL url = getClass().getClassLoader().getResource(filePath);
            if (url == null) {
                throw new IOException("Không tìm thấy file: " + filePath);
            }

            File file;
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Lỗi chuyển đổi URL thành URI: " + url, e);
            }

            if (!file.exists()) {
                throw new IOException("Không tìm thấy file: " + file.getAbsolutePath());
            }
    
            BufferedImage original = ImageIO.read(file);
    
            Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();
    
            frames[i] = resized;
        }
        return frames;
    }
    // endregion

    // region Quản lý game loop
    public void startGameThread() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    public void stopGameThread() {
        isRunning = false;
        if (gameThread != null) {
            try {
                gameThread.join();
                skills.clear();
                enemies.clear();
                collisionObjects.clear();
                if (GameData.droppedItems != null) {
                    GameData.droppedItems.clear();
                }
                mapImage = null;
                hudImage = null;
                up = down = left = right = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameThread = null;
        }
    }
    
    public void run() {
        while (isRunning) {
            update();
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void update() {
        if (player != null) {
            player.update();
    
            for (Enemy enemy : enemies) {
                enemy.update(player);
                enemy.attack(player);
            }
    
            enemies.removeIf(Enemy::isDead);
            
            for (SkillEffect skill : skills) {
                if (skill != null) {
                    skill.update();
                }
            }
            skills.removeIf(SkillEffect::isExpired);
        }
    }
    // endregion

    // region Vẽ giao diện
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        double scale = 1.8;
        g2d.scale(scale, scale);

        if (player != null) {
            int camX = player.getX() - (int) (WIDTH / (2 * scale));
            int camY = player.getY() - (int) (HEIGHT / (2 * scale));

            camX = Math.max(0, Math.min(camX, mapImage.getWidth() - (int) (WIDTH / scale)));
            camY = Math.max(0, Math.min(camY, mapImage.getHeight() - (int) (HEIGHT / scale)));

            g2d.drawImage(mapImage, -camX, -camY, null);
            player.draw(g2d, camX, camY);

            for (Enemy enemy : enemies) {
                enemy.draw(g2d, camX, camY);
            }

            try {
                if (GameData.droppedItems != null) {
                    for (DroppedItem item : GameData.droppedItems) {
                        if (item != null) {
                            item.draw(g2d, camX, camY);
                        }
                    }
                }
            } catch (ConcurrentModificationException e) {
                System.out.println("Bug tránh được tạm thời: " + e.getMessage());
            }

            g2d.scale(1 / scale, 1 / scale);

            for (SkillEffect skill : skills) {
                if (skill != null) {
                    skill.draw(g2d, camX, camY);
                }
            }

            int hudWidth = 200;
            int hudHeight = 100;
            g.drawImage(hudImage, 0, 0, hudWidth, hudHeight, null);

            int maxHealthBarWidth = 110;
            int healthBarHeight = 10;

            double healthPercent = (double) player.health / player.maxHealth;

            int currentHealthWidth = (int) (healthPercent * maxHealthBarWidth);

            g.setColor(Color.RED);
            g.fillRect(50, 30, currentHealthWidth, healthBarHeight);

            g.setColor(Color.BLACK);
            g.drawRect(50, 30, maxHealthBarWidth, healthBarHeight);

            g.setColor(Color.WHITE);
            g.drawString((long)(player.health) + "HP", 55, 39);

            int maxmanaBarWidth = 110;
            int manaBarHeight = 10;

            double manaPercent = (double) player.mana / player.maxmana;

            int currentmanaWidth = (int) (manaPercent * maxmanaBarWidth);

            g.setColor(Color.BLUE);
            g.fillRect(50, 50, currentmanaWidth, manaBarHeight);

            g.setColor(Color.BLACK);
            g.drawRect(50, 50, maxmanaBarWidth, manaBarHeight);

            g.setColor(Color.WHITE);
            g.drawString((long)(player.mana) + "MP", 55, 59);

            drawSkillButtons(g2d);
        }
    }
    
    private void drawSkillButtons(Graphics2D g) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        
        int normalX = screenWidth - NORMAL_ATTACK_SIZE - SKILL_PADDING;
        int normalY = screenHeight - NORMAL_ATTACK_SIZE - SKILL_PADDING;
        normalAttackBounds = new Rectangle(normalX, normalY, NORMAL_ATTACK_SIZE, NORMAL_ATTACK_SIZE);
        
        g.setColor(new Color(0, 0, 0, 150));
        g.fillOval(normalX, normalY, NORMAL_ATTACK_SIZE, NORMAL_ATTACK_SIZE);
        g.drawImage(danhthuong, normalX, normalY, NORMAL_ATTACK_SIZE, NORMAL_ATTACK_SIZE, null);
        
        double radius = NORMAL_ATTACK_SIZE + SKILL_PADDING;
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI / 2 + (i * Math.PI / 6);
            int skillX = normalX + NORMAL_ATTACK_SIZE/2 - SKILL_BUTTON_SIZE/2 
                        + (int)(radius * Math.cos(angle));
            int skillY = normalY + NORMAL_ATTACK_SIZE/2 - SKILL_BUTTON_SIZE/2 
                        - (int)(radius * Math.sin(angle));
            
            skillButtonBounds[i] = new Rectangle(skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE);
            
            g.setColor(new Color(0, 0, 0, 150));
            g.fillOval(skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE);
            
            if (skillUnlocked[i] && skillIcons[i] != null) {
                g.drawImage(skillIcons[i], skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE, null);
            }
            
            g.setColor(Color.GRAY);
            g.drawOval(skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String slotNum = String.valueOf(i + 1);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(slotNum, 
                skillX + (SKILL_BUTTON_SIZE - fm.stringWidth(slotNum))/2,
                skillY + SKILL_BUTTON_SIZE - 5);
        }

        long currentTime = System.currentTimeMillis();
    
        // Draw normal attack cooldown
        if (currentTime - lastNormalAttackTime < (1000 / attackSpeed)) {
            double progress = 1 - ((currentTime - lastNormalAttackTime) / (1000.0 / attackSpeed));
            drawCooldownOverlay(g, normalAttackBounds, progress);
        }

        // Draw skill cooldowns
        for (int i = 0; i < 4; i++) {
            if (skillUnlocked[i]) {
                long timeSinceLastUse = currentTime - skillLastUsedTime.get(i);
                int cooldown = skillCooldowns.get(i);
                
                if (timeSinceLastUse < cooldown) {
                    double progress = 1 - (timeSinceLastUse / (double)cooldown);
                    drawCooldownOverlay(g, skillButtonBounds[i], progress);
                    
                    // Draw cooldown text
                    int secondsLeft = (int)Math.ceil((cooldown - timeSinceLastUse) / 1000.0);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 16));
                    String cooldownText = String.valueOf(secondsLeft);
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(cooldownText,
                        skillButtonBounds[i].x + (SKILL_BUTTON_SIZE - fm.stringWidth(cooldownText))/2,
                        skillButtonBounds[i].y + SKILL_BUTTON_SIZE/2 + fm.getHeight()/2);
                }
            }
        }
    }

    private void drawCooldownOverlay(Graphics2D g, Rectangle bounds, double progress) {
        g.setColor(new Color(0, 0, 0, 150));
        Arc2D.Double arc = new Arc2D.Double(
            bounds.x, bounds.y, bounds.width, bounds.height,
            90, progress * 360, Arc2D.PIE
        );
        g.fill(arc);
    }
    // endregion

    // region Xử lý input
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastNormalAttackTime >= (1000 / attackSpeed)) {
                SkillEffect skill = player.castSkill(0, enemies, -50);
                if (skill != null) {
                    skills.add(skill);
                    lastNormalAttackTime = currentTime;
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_Q || 
               e.getKeyCode() == KeyEvent.VK_E || 
               e.getKeyCode() == KeyEvent.VK_R || 
               e.getKeyCode() == KeyEvent.VK_T) {
        
            int slot = switch (e.getKeyCode()) {
                case KeyEvent.VK_Q -> 0;
                case KeyEvent.VK_E -> 1;
                case KeyEvent.VK_R -> 2;
                case KeyEvent.VK_T -> 3;
                default -> -1;
            };

            if (slot >= 0 && skillUnlocked[slot]) {
                GameCharacterSkill charSkill = GameData.characterSkills.stream()
                    .filter(cs -> cs.getCharacterId().equals(playerId) && 
                                cs.getSlot() == slot + 1)
                    .findFirst()
                    .orElse(null);

                if (charSkill != null) {
                    SkillData skillData = skillDataMap.get(charSkill.getSkillId());
                    if (skillData != null) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - skillLastUsedTime.get(slot) >= skillCooldowns.get(slot)) {
                            SkillEffect effect = player.castSkill(slot + 1, enemies, -50);
                            if (effect != null) {
                                skills.add(effect);
                                skillLastUsedTime.put(slot, currentTime);
                                System.out.println("Sử dụng skill slot " + (slot + 1));
                            }
                        }
                    }
                }
            }
        } else {
            player.setDirection(e.getKeyCode(), true);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        player.setDirection(e.getKeyCode(), false);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    // endregion

    // region Tiện ích
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getClassLoader().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    // endregion
}
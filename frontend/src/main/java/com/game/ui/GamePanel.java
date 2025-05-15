package com.game.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.ArrayList;
import com.game.*;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import com.game.data.GameData;
import com.game.model.*;
import java.util.ConcurrentModificationException;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    ArrayList<SkillEffect> skills = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    Rectangle skillButton1Bounds;
    Rectangle skillButton2Bounds;

    private Inventory inventory = new Inventory();
    private boolean inventoryVisible = false;
    private List<Item> inventoryItems = new ArrayList<>();
    private static final int INVENTORY_ROWS = 5;    // 5 hàng
    private static final int INVENTORY_COLS = 10;   // 10 cột
    private static final int SLOT_SIZE = 50;        // Kích thước mỗi ô (50x50)
    private static final int INVENTORY_WIDTH = INVENTORY_COLS * SLOT_SIZE;  // Chiều rộng kho đồ
    private static final int INVENTORY_HEIGHT = INVENTORY_ROWS * SLOT_SIZE;

    private MapData currentMapData;

    final int WIDTH = 1200, HEIGHT = 700;
    Thread gameThread;
    BufferedImage mapImage,imgUp, imgDown, imgLeft, imgRight, hudImage, danhthuong;
    BufferedImage[] up, down, left, right;
    public static Player player;
    private ArrayList<Rectangle> collisionObjects = new ArrayList<>();
    private volatile boolean isRunning = false;
    private Long playerId;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                if (skillButton1Bounds != null && skillButton1Bounds.contains(mouseX, mouseY)) {
                    SkillEffect skill = player.castSkill(0, enemies, -50); // Skill 1
                    if (skill != null) skills.add(skill);
                }

                if (skillButton2Bounds != null && skillButton2Bounds.contains(mouseX, mouseY)) {
                    SkillEffect skill = player.castSkill(1, enemies, 20); // Skill 2
                    if (skill != null) skills.add(skill);
                }
            }
        });
    }

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

            // Tạo kỹ năng
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

            // Thêm kỹ năng vào player
            player.addSkill(fireSkill);
            // Thêm quái vật
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

    public void loadMap(MapData mapData) {
        loadResources(mapData);
        try {
            int dem = 1;
            for(GameInventory inventory1 : GameData.inventory) {
                if (inventory1.getCharacterId() == playerId) {
                    for(GameItem item : GameData.item) {
                        if (item.getId() == inventory1.getItemId()) {
                            BufferedImage itemIcon = ImageIO.read(getClass().getClassLoader().getResource(item.getIcon()));
                            Item gameItem = new Item(dem, item.getName(), itemIcon);
                            inventory.addItem(gameItem);

                            dem++;
                        }
                    }
                }
            }

            // Initialize dropped items list if null
            if (GameData.droppedItems == null) {
                GameData.droppedItems = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace(); // In lỗi ra để debug
        }
    }

    public void startGameThread() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void run() {
        while (isRunning) {
            update();
            repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Đánh dấu thread bị ngắt
                break;
            }
        }
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
                    GameData.droppedItems.clear(); // Clear dropped items when stopping game
                }
                mapImage = null;
                hudImage = null;
                imgUp = imgDown = imgLeft = imgRight = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameThread = null;
        }
    }

    public void update() {
        if (player != null) {
            player.update();
    
                // Cập nhật quái vật
            for (Enemy enemy : enemies) {
                enemy.update(player);
                enemy.attack(player); // Quái vật tấn công người chơi
            }
    
            // Xóa quái vật đã chết
            enemies.removeIf(Enemy::isDead);
            
            // Cập nhật hiệu ứng kỹ năng
            for (SkillEffect skill : skills) {
                if (skill != null) {
                    skill.update();
                }
            }
            skills.removeIf(SkillEffect::isExpired);
        }
    }

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
            // tileMap.draw(g2d, camX, camY);
            player.draw(g2d, camX, camY);

            // Vẽ quái vật
            for (Enemy enemy : enemies) {
                enemy.draw(g2d, camX, camY);
            }

            // Vẽ các item rơi trên bản đồ nếu chưa nhặt
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

            // Vẽ hiệu ứng kỹ năng
            for (SkillEffect skill : skills) {
                if (skill != null) {
                    skill.draw(g2d, camX, camY);
                }
            }

            // Vẽ khung HUD
            int hudWidth = 200; // Chiều rộng khung HUD
            int hudHeight = 100; // Chiều cao khung HUD
            g.drawImage(hudImage, 0, 0, hudWidth, hudHeight, null);

            // Vẽ thanh máu và mana
            int maxHealthBarWidth = 110; // cố định
            int healthBarHeight = 10;

            // Tính phần trăm máu hiện tại
            double healthPercent = (double) player.health / player.maxHealth;

            // Tính chiều dài thực tế của thanh máu theo %
            int currentHealthWidth = (int) (healthPercent * maxHealthBarWidth);

            // Vẽ thanh máu
            g.setColor(Color.RED);
            g.fillRect(50, 30, currentHealthWidth, healthBarHeight);

            // Vẽ khung viền
            g.setColor(Color.BLACK);
            g.drawRect(50, 30, maxHealthBarWidth, healthBarHeight);

            // (Tuỳ chọn) Vẽ số %
            g.setColor(Color.WHITE);
            g.drawString((int)(player.health) + "HP", 55, 39);

            int maxmanaBarWidth = 110; // cố định
            int manaBarHeight = 10;

            double manaPercent = (double) player.mana / player.maxmana;

            int currentmanaWidth = (int) (manaPercent * maxmanaBarWidth);

            g.setColor(Color.BLUE);
            g.fillRect(50, 50, currentmanaWidth, manaBarHeight);

            g.setColor(Color.BLACK);
            g.drawRect(50, 50, maxmanaBarWidth, manaBarHeight);

            g.setColor(Color.WHITE);
            g.drawString((int)(player.mana) + "MP", 55, 59);

            // Vẽ các nút kỹ năng
            int skillButtonSize = 70; // Kích thước nút kỹ năng
            int padding = 10; // Khoảng cách giữa các nút và cạnh màn hình
            int panelWidth = getWidth(); // Lấy chiều rộng thực tế của JPanel
            int panelHeight = getHeight(); // Lấy chiều cao thực tế của JPanel

            // Nút kỹ năng 1
            // g.drawImage(danhthuong, panelWidth - skillButtonSize - padding, panelHeight - skillButtonSize - padding, skillButtonSize, skillButtonSize, null);
            int skill1X = panelWidth - skillButtonSize - padding;
            int skill1Y = panelHeight - skillButtonSize - padding;
            g.drawImage(danhthuong, skill1X, skill1Y, skillButtonSize, skillButtonSize, null);
            skillButton1Bounds = new Rectangle(skill1X, skill1Y, skillButtonSize, skillButtonSize);
            if (inventoryVisible) {
                int startX = 100; // vị trí bắt đầu vẽ kho đồ
                int startY = 100;
            
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(startX - 10, startY - 10, INVENTORY_WIDTH + 20, INVENTORY_HEIGHT + 20);
            
                List<Item> items = inventory.getItems();  // lấy danh sách item từ Inventory
                for (int i = 0; i < items.size(); i++) {
                    int row = i / INVENTORY_COLS;
                    int col = i % INVENTORY_COLS;
                    int x = startX + col * SLOT_SIZE;
                    int y = startY + row * SLOT_SIZE;
            
                    g2d.setColor(Color.GRAY);
                    g2d.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);
            
                    Item item = items.get(i);
                    if (item.getIcon() != null) {
                        g2d.drawImage(item.getIcon(), x + 5, y + 5, SLOT_SIZE - 10, SLOT_SIZE - 10, null);
                    }
                }
            } 
        }
    }

    public BufferedImage[] loadFrames(String folderPath, int count, int newWidth, int newHeight) throws IOException {
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
    
            // Tạo ảnh mới với kích thước mong muốn
            Image scaled = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();
    
            frames[i] = resized;
        }
        return frames;
    }
    
    private void drawInventory(Graphics2D g2d) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        
        // Tính toán tọa độ x, y để căn giữa kho đồ
        int inventoryX = (screenWidth - INVENTORY_WIDTH) / 2;
        int inventoryY = (screenHeight - INVENTORY_HEIGHT) / 2;
    
        // Vẽ nền kho đồ (một hình chữ nhật với nền màu xám)
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(inventoryX, inventoryY, INVENTORY_WIDTH, INVENTORY_HEIGHT);
    
        // Vẽ khung kho đồ
        g2d.setColor(Color.BLACK);
        g2d.drawRect(inventoryX, inventoryY, INVENTORY_WIDTH, INVENTORY_HEIGHT);
    
        // Vẽ các ô trống trong kho đồ
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int slotX = inventoryX + col * SLOT_SIZE;
                int slotY = inventoryY + row * SLOT_SIZE;
    
                // Vẽ nền cho ô trống
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
    
                // Vẽ các item nếu có
                // Nếu item có trong ô này, vẽ nó
                Item item = getItemInSlot(row, col); // Giả sử có phương thức getItemInSlot để lấy item tại ô
                if (item != null) {
                    BufferedImage itemIcon = item.getIcon();
                    if (itemIcon != null) {
                        g2d.drawImage(itemIcon, slotX, slotY, SLOT_SIZE, SLOT_SIZE, null);
                    }
                }
            }
        }
    }
    
    // Phương thức giả lập lấy item trong kho đồ
    private Item getItemInSlot(int row, int col) {
        // Trả về item tại vị trí ô (row, col)
        // Giả sử kho đồ của bạn là một danh sách các item
        int index = row * INVENTORY_COLS + col;
        if (index < inventoryItems.size()) {
            return inventoryItems.get(index);  // Giả sử bạn có danh sách items trong kho
        }
        return null;  // Nếu không có item thì trả về null
    }
    
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getClassLoader().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // Sử dụng kỹ năng
            SkillEffect skill = player.castSkill(0, enemies, -50); // Skill 1
                    if (skill != null) skills.add(skill);
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            // Sử dụng kỹ năng
            inventoryVisible = !inventoryVisible;  // Đổi trạng thái hiển thị kho đồ
                repaint();
        } else {
            player.setDirection(e.getKeyCode(), true);
        }
    }

    public void keyReleased(KeyEvent e) {
        player.setDirection(e.getKeyCode(), false);
    }

    public void keyTyped(KeyEvent e) {}
}
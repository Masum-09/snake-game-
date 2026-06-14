import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// ─── Direction Enum ───────────────────────────────────────────────────────────
enum Direction {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

    final int dx, dy;
    Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }

    boolean isOpposite(Direction other) {
        return this.dx == -other.dx && this.dy == -other.dy;
    }
}

// ─── Point ────────────────────────────────────────────────────────────────────
class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
    Point copy()        { return new Point(x, y); }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
}

// ─── Food ────────────────────────────────────────────────────────────────────
abstract class Food {
    Point position;
    int value;
    Color color;

    Food(Point position, int value, Color color) {
        this.position = position;
        this.value    = value;
        this.color    = color;
    }

    void draw(Graphics2D g, int cell) {
        g.setColor(color);
        int margin = cell / 5;
        g.fillOval(position.x * cell + margin,
                   position.y * cell + margin,
                   cell - margin * 2, cell - margin * 2);
    }
}

class NormalFood extends Food {
    NormalFood(Point pos) { super(pos, 10, new Color(80, 200, 120)); }
}

class BonusFood extends Food {
    BonusFood(Point pos) { super(pos, 30, new Color(255, 190, 60)); }

    @Override
    void draw(Graphics2D g, int cell) {
        g.setColor(color);
        int margin = cell / 6;
        int[] xp = {
            position.x * cell + cell / 2,
            position.x * cell + cell - margin,
            position.x * cell + margin
        };
        int[] yp = {
            position.y * cell + margin,
            position.y * cell + cell - margin,
            position.y * cell + cell - margin
        };
        g.fillPolygon(xp, yp, 3);
    }
}

class SpeedFood extends Food {
    SpeedFood(Point pos) { super(pos, 5, new Color(100, 180, 255)); }
}

// ─── PowerUp ──────────────────────────────────────────────────────────────────
class PowerUp {
    enum Type { SLOW, SHRINK }

    Type type;
    Point position;
    long spawnTime;
    static final long LIFESPAN_MS = 7000;

    PowerUp(Type type, Point pos) {
        this.type = type;
        this.position = pos;
        this.spawnTime = System.currentTimeMillis();
    }

    boolean isExpired() { return System.currentTimeMillis() - spawnTime > LIFESPAN_MS; }

    void draw(Graphics2D g, int cell) {
        long remaining = LIFESPAN_MS - (System.currentTimeMillis() - spawnTime);
        float alpha = Math.min(1f, remaining / 1500f);

        Color c = type == Type.SLOW ? new Color(200, 120, 255) : new Color(255, 100, 100);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 220)));

        int m = cell / 5;
        int x = position.x * cell + m, y = position.y * cell + m;
        int s = cell - m * 2;
        g.fillRoundRect(x, y, s, s, 6, 6);

        g.setColor(new Color(255, 255, 255, (int)(alpha * 200)));
        g.setFont(new Font("Monospaced", Font.BOLD, cell / 2));
        FontMetrics fm = g.getFontMetrics();
        String label = type == Type.SLOW ? "S" : "X";
        g.drawString(label,
            position.x * cell + (cell - fm.stringWidth(label)) / 2,
            position.y * cell + (cell + fm.getAscent()) / 2 - 1);
    }
}

// ─── Snake ────────────────────────────────────────────────────────────────────
class Snake {
    private final Deque<Point> body = new ArrayDeque<>();
    private Direction dir    = Direction.RIGHT;
    private Direction nextDir = Direction.RIGHT;

    private static final Color HEAD_COLOR = new Color(230, 230, 230);
    private static final Color BODY_COLOR = new Color(170, 170, 170);

    Snake(int startX, int startY) {
        body.addFirst(new Point(startX,     startY));
        body.addFirst(new Point(startX + 1, startY));
        body.addFirst(new Point(startX + 2, startY));
    }

    void setDirection(Direction d) {
        if (!d.isOpposite(dir)) nextDir = d;
    }

    /** Returns the new head position without modifying the snake */
    Point nextHead() {
        Point head = getHead();
        return new Point(head.x + nextDir.dx, head.y + nextDir.dy);
    }

    /** Moves the snake. Returns the removed tail (for length tracking). */
    Point move(boolean grow) {
        dir = nextDir;
        body.addFirst(nextHead());
        if (grow) return null;
        return body.removeLast();
    }

    /** Shrink by up to n segments (keep minimum 3) */
    void shrink(int n) {
        for (int i = 0; i < n && body.size() > 3; i++) body.removeLast();
    }

    Point getHead() { return body.peekFirst(); }
    List<Point> getBody() { return new ArrayList<>(body); }
    int length() { return body.size(); }

    boolean selfCollides() {
        Point head = getHead();
        List<Point> tail = new ArrayList<>(body);
        tail.removeFirst();
        return tail.contains(head);
    }

    void draw(Graphics2D g, int cell) {
        List<Point> pts = getBody();
        for (int i = pts.size() - 1; i >= 0; i--) {
            Point p = pts.get(i);
            float t = 1f - (float) i / pts.size();
            int alpha = 120 + (int)(t * 135);

            Color c = (i == 0) ? HEAD_COLOR : BODY_COLOR;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));

            int margin = (i == 0) ? 1 : 3;
            g.fillRoundRect(p.x * cell + margin, p.y * cell + margin,
                            cell - margin * 2, cell - margin * 2, 6, 6);

            // eyes on head
            if (i == 0) {
                g.setColor(new Color(20, 20, 20));
                int ex = p.x * cell + cell / 2 - 2 + dir.dx * (cell / 4);
                int ey = p.y * cell + cell / 2 - 2 + dir.dy * (cell / 4);
                g.fillOval(ex - 2, ey - 2, 4, 4);
            }
        }
    }
}

// ─── GameBoard ────────────────────────────────────────────────────────────────
class GameBoard {
    final int cols, rows, cell;

    GameBoard(int cols, int rows, int cell) {
        this.cols = cols; this.rows = rows; this.cell = cell;
    }

    boolean inBounds(Point p) {
        return p.x >= 0 && p.x < cols && p.y >= 0 && p.y < rows;
    }

    Point randomFreePoint(Set<Point> occupied) {
        Random rng = new Random();
        Point p;
        do { p = new Point(rng.nextInt(cols), rng.nextInt(rows)); }
        while (occupied.contains(p));
        return p;
    }

    void drawGrid(Graphics2D g) {
        g.setColor(new Color(30, 30, 30));
        for (int x = 0; x <= cols; x++)
            g.drawLine(x * cell, 0, x * cell, rows * cell);
        for (int y = 0; y <= rows; y++)
            g.drawLine(0, y * cell, cols * cell, y * cell);
    }
}

// ─── ScoreBoard ───────────────────────────────────────────────────────────────
class ScoreBoard {
    private int score, highScore, level, foodEaten;
    private static final int LEVEL_UP_EVERY = 5;

    void addPoints(int pts) {
        score     += pts;
        foodEaten += 1;
        highScore  = Math.max(highScore, score);
        level      = 1 + foodEaten / LEVEL_UP_EVERY;
    }

    void reset() { score = 0; foodEaten = 0; level = 1; }

    int getScore()     { return score; }
    int getHighScore() { return highScore; }
    int getLevel()     { return level; }

    /** Base tick delay in ms — decreases with level */
    int getTickDelay() { return Math.max(60, 200 - (level - 1) * 15); }
}

// ─── GameState ────────────────────────────────────────────────────────────────
enum GameState { MENU, PLAYING, PAUSED, GAME_OVER }

// ─── Game (controller) ────────────────────────────────────────────────────────
class Game {
    static final int COLS = 28, ROWS = 22, CELL = 24;

    private Snake      snake;
    private GameBoard  board;
    private ScoreBoard scores;
    private GameState  state;

    private Food       food;
    private BonusFood  bonusFood;
    private SpeedFood  speedFood;
    private PowerUp    powerUp;

    private boolean    slowActive;
    private long       slowEnd;
    private int        bonusFoodCountdown; // food eaten until bonus appears
    private Random     rng = new Random();

    Game() {
        board  = new GameBoard(COLS, ROWS, CELL);
        scores = new ScoreBoard();
        state  = GameState.MENU;
    }

    void startNewGame() {
        snake            = new Snake(COLS / 2, ROWS / 2);
        scores.reset();
        bonusFood        = null;
        speedFood        = null;
        powerUp          = null;
        slowActive       = false;
        bonusFoodCountdown = 3 + rng.nextInt(3);
        spawnFood();
        state = GameState.PLAYING;
    }

    private Set<Point> occupied() {
        Set<Point> occ = new HashSet<>(snake.getBody());
        if (food   != null) occ.add(food.position);
        if (bonusFood != null) occ.add(bonusFood.position);
        if (speedFood != null) occ.add(speedFood.position);
        if (powerUp   != null) occ.add(powerUp.position);
        return occ;
    }

    private void spawnFood() {
        food = new NormalFood(board.randomFreePoint(occupied()));
    }

    void tick() {
        if (state != GameState.PLAYING) return;

        // expire power-ups
        if (slowActive && System.currentTimeMillis() > slowEnd) slowActive = false;
        if (powerUp != null && powerUp.isExpired())             powerUp   = null;

        Point nextHead = snake.nextHead();

        // Wall collision
        if (!board.inBounds(nextHead)) { state = GameState.GAME_OVER; return; }

        // Collect food?
        boolean grow    = false;
        boolean ate     = false;

        if (nextHead.equals(food.position)) {
            scores.addPoints(food.value);
            grow = true; ate = true;
            spawnFood();
            bonusFoodCountdown--;
            if (bonusFoodCountdown <= 0) {
                if (bonusFood == null) bonusFood = new BonusFood(board.randomFreePoint(occupied()));
                bonusFoodCountdown = 3 + rng.nextInt(3);
                // random chance for power-up
                if (powerUp == null && rng.nextFloat() < 0.4f)
                    powerUp = new PowerUp(
                        rng.nextBoolean() ? PowerUp.Type.SLOW : PowerUp.Type.SHRINK,
                        board.randomFreePoint(occupied()));
            }
            // random speed food
            if (speedFood == null && rng.nextFloat() < 0.25f)
                speedFood = new SpeedFood(board.randomFreePoint(occupied()));
        }

        if (bonusFood != null && nextHead.equals(bonusFood.position)) {
            scores.addPoints(bonusFood.value);
            grow = true; bonusFood = null;
        }

        if (speedFood != null && nextHead.equals(speedFood.position)) {
            scores.addPoints(speedFood.value);
            speedFood = null;
            // temporary speed boost — handled by returning a shorter delay
            slowActive = false;
        }

        if (powerUp != null && nextHead.equals(powerUp.position)) {
            if (powerUp.type == PowerUp.Type.SLOW) {
                slowActive = true;
                slowEnd    = System.currentTimeMillis() + 4000;
            } else {
                snake.shrink(4);
            }
            powerUp = null;
        }

        snake.move(grow);

        // Self-collision
        if (snake.selfCollides()) { state = GameState.GAME_OVER; }
    }

    int getTickDelay() {
        if (slowActive) return scores.getTickDelay() * 2;
        return scores.getTickDelay();
    }

    void setDirection(Direction d) { snake.setDirection(d); }
    void togglePause() {
        if (state == GameState.PLAYING) state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.PLAYING;
    }

    // Getters for renderer
    Snake      getSnake()     { return snake; }
    GameBoard  getBoard()     { return board; }
    ScoreBoard getScores()    { return scores; }
    GameState  getState()     { return state; }
    Food       getFood()      { return food; }
    BonusFood  getBonusFood() { return bonusFood; }
    SpeedFood  getSpeedFood() { return speedFood; }
    PowerUp    getPowerUp()   { return powerUp; }
    boolean    isSlowActive() { return slowActive; }
}

// ─── Renderer ─────────────────────────────────────────────────────────────────
class GameRenderer extends JPanel {
    private static final Color BG         = new Color(18, 18, 18);
    private static final Color GRID_COLOR = new Color(28, 28, 28);
    private static final Font  MONO       = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font  MONO_B     = new Font("Monospaced", Font.BOLD, 13);
    private static final Font  BIG        = new Font("Monospaced", Font.BOLD, 32);
    private static final Font  MED        = new Font("Monospaced", Font.PLAIN, 15);

    private final Game game;

    GameRenderer(Game game) {
        this.game = game;
        int w = Game.COLS * Game.CELL;
        int h = Game.ROWS * Game.CELL + 40;
        setPreferredSize(new Dimension(w, h));
        setBackground(BG);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = Game.COLS * Game.CELL;
        int h = Game.ROWS * Game.CELL;

        // ── HUD bar at top ──
        drawHUD(g, w);

        // ── Board area (offset down by 40) ──
        g.translate(0, 40);

        // Grid
        g.setColor(GRID_COLOR);
        for (int x = 0; x <= Game.COLS; x++) g.drawLine(x * Game.CELL, 0, x * Game.CELL, h);
        for (int y = 0; y <= Game.ROWS; y++) g.drawLine(0, y * Game.CELL, w, y * Game.CELL);

        // Border
        g.setColor(new Color(50, 50, 50));
        g.setStroke(new BasicStroke(2));
        g.drawRect(0, 0, w, h);
        g.setStroke(new BasicStroke(1));

        GameState state = game.getState();

        if (state == GameState.MENU)      { drawMenu(g, w, h); return; }

        // Game entities
        if (game.getFood()      != null) game.getFood().draw(g, Game.CELL);
        if (game.getBonusFood() != null) game.getBonusFood().draw(g, Game.CELL);
        if (game.getSpeedFood() != null) game.getSpeedFood().draw(g, Game.CELL);
        if (game.getPowerUp()   != null) game.getPowerUp().draw(g, Game.CELL);
        game.getSnake().draw(g, Game.CELL);

        if (state == GameState.PAUSED)    drawPause(g, w, h);
        if (state == GameState.GAME_OVER) drawGameOver(g, w, h);
    }

    private void drawHUD(Graphics2D g, int w) {
        g.setColor(new Color(26, 26, 26));
        g.fillRect(0, 0, w, 40);
        g.setColor(new Color(50, 50, 50));
        g.drawLine(0, 39, w, 39);

        ScoreBoard sc = game.getScores();

        g.setFont(MONO_B);
        g.setColor(new Color(200, 200, 200));
        g.drawString("SCORE  " + sc.getScore(), 14, 26);

        g.setColor(new Color(130, 130, 130));
        g.setFont(MONO);
        g.drawString("BEST " + sc.getHighScore(), w / 2 - 40, 26);

        g.setColor(new Color(100, 180, 255));
        g.drawString("LV " + sc.getLevel(), w - 70, 26);

        if (game.isSlowActive()) {
            g.setColor(new Color(200, 120, 255));
            g.drawString("SLOW", w - 130, 26);
        }
    }

    private void drawMenu(Graphics2D g, int w, int h) {
        overlay(g, w, h, 0.85f);
        g.setFont(BIG);
        g.setColor(new Color(220, 220, 220));
        centerText(g, "SNAKE", w, h / 2 - 50);

        g.setFont(MED);
        g.setColor(new Color(130, 130, 130));
        centerText(g, "press  ENTER  to start", w, h / 2 + 10);

        g.setFont(MONO);
        g.setColor(new Color(80, 80, 80));
        centerText(g, "arrows / WASD  ·  P pause  ·  R restart", w, h / 2 + 44);

        drawLegend(g, w, h);
    }

    private void drawLegend(Graphics2D g, int w, int h) {
        int startY = h - 78;
        g.setFont(MONO);

        Object[][] items = {
            {new Color(80, 200, 120),  "●", "+10  normal"},
            {new Color(255, 190, 60),  "▲", "+30  bonus"},
            {new Color(100, 180, 255), "●", "+5   speed boost"},
            {new Color(200, 120, 255), "S", "     slow field"},
            {new Color(255, 100, 100), "X", "     shrink"},
        };

        int colW = w / items.length;
        for (int i = 0; i < items.length; i++) {
            g.setColor((Color) items[i][0]);
            g.drawString((String) items[i][1], 14 + i * colW, startY);
            g.setColor(new Color(90, 90, 90));
            g.drawString((String) items[i][2], 14 + i * colW, startY + 18);
        }
    }

    private void drawPause(Graphics2D g, int w, int h) {
        overlay(g, w, h, 0.6f);
        g.setFont(BIG);
        g.setColor(new Color(200, 200, 200));
        centerText(g, "PAUSED", w, h / 2);
        g.setFont(MONO);
        g.setColor(new Color(100, 100, 100));
        centerText(g, "press  P  to resume", w, h / 2 + 38);
    }

    private void drawGameOver(Graphics2D g, int w, int h) {
        overlay(g, w, h, 0.75f);
        g.setFont(BIG);
        g.setColor(new Color(220, 80, 80));
        centerText(g, "GAME OVER", w, h / 2 - 30);

        g.setFont(MED);
        g.setColor(new Color(180, 180, 180));
        centerText(g, "score  " + game.getScores().getScore(), w, h / 2 + 12);

        g.setFont(MONO);
        g.setColor(new Color(90, 90, 90));
        centerText(g, "press  R  to restart  ·  ENTER  for menu", w, h / 2 + 46);
    }

    private void overlay(Graphics2D g, int w, int h, float alpha) {
        g.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
        g.fillRect(0, 0, w, h);
    }

    private void centerText(Graphics2D g, String s, int w, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (w - fm.stringWidth(s)) / 2, y);
    }
}

// ─── SnakeGame (entry point) ──────────────────────────────────────────────────
public class SnakeGame extends JFrame implements KeyListener {
    private final Game         game;
    private final GameRenderer renderer;
    private       Timer        timer;

    public SnakeGame() {
        super("Snake");
        game     = new Game();
        renderer = new GameRenderer(game);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(18, 18, 18));
        add(renderer);
        pack();
        setLocationRelativeTo(null);
        addKeyListener(this);

        startTimer(150);
    }

    private void startTimer(int delay) {
        if (timer != null) timer.stop();
        timer = new Timer(delay, e -> {
            game.tick();
            // Adaptive tick rate
            int desired = game.getTickDelay();
            if (timer.getDelay() != desired) {
                timer.setDelay(desired);
                timer.setInitialDelay(desired);
            }
            renderer.repaint();
        });
        timer.start();
    }

    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        switch (game.getState()) {
            case MENU -> {
                if (k == KeyEvent.VK_ENTER) game.startNewGame();
            }
            case PLAYING -> {
                switch (k) {
                    case KeyEvent.VK_UP,    KeyEvent.VK_W -> game.setDirection(Direction.UP);
                    case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> game.setDirection(Direction.DOWN);
                    case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> game.setDirection(Direction.LEFT);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> game.setDirection(Direction.RIGHT);
                    case KeyEvent.VK_P -> game.togglePause();
                    case KeyEvent.VK_R -> game.startNewGame();
                }
            }
            case PAUSED -> {
                if (k == KeyEvent.VK_P || k == KeyEvent.VK_ESCAPE) game.togglePause();
                if (k == KeyEvent.VK_R) game.startNewGame();
            }
            case GAME_OVER -> {
                if (k == KeyEvent.VK_R)     game.startNewGame();
                if (k == KeyEvent.VK_ENTER) { /* back to menu – just reset state */ game.startNewGame(); }
            }
        }
        renderer.repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame().setVisible(true));
    }
}
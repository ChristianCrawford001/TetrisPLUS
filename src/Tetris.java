import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Tetris extends JFrame {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;

    private GamePanel gamePanel;

    public Tetris() {
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tetris());
    }
}

class GamePanel extends JPanel {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    private static final int GAME_SPEED = 500;

    private Color[][] board;
    private Piece currentPiece;
    private Piece nextPiece;
    private Piece heldPiece;
    private Timer timer;
    private int score;
    private boolean gameOver;
    private Random random;
    private boolean canHold;

    public GamePanel() {
        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE + 300, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);

        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        random = new Random();
        score = 0;
        gameOver = false;
        canHold = true;

        currentPiece = new Piece(random);
        nextPiece = new Piece(random);
        heldPiece = null;

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        resetGame();
                    }
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        movePiece(-1, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        movePiece(1, 0);
                        break;
                    case KeyEvent.VK_DOWN:
                        movePiece(0, 1);
                        break;
                    case KeyEvent.VK_UP:
                        rotatePiece();
                        break;
                    case KeyEvent.VK_SPACE:
                        dropPiece();
                        break;
                    case KeyEvent.VK_C:
                    case KeyEvent.VK_SHIFT:
                        holdPiece();
                        break;
                }
                repaint();
            }
        });

        timer = new Timer(GAME_SPEED, e -> {
            if (!gameOver) {
                if (!movePiece(0, 1)) {
                    placePiece();
                    clearLines();
                    currentPiece = nextPiece;
                    nextPiece = new Piece(random);
                    canHold = true;
                    if (checkCollision(currentPiece, currentPiece.x, currentPiece.y)) {
                        gameOver = true;
                    }
                }
                repaint();
            }
        });
        timer.start();
    }

    private void resetGame() {
        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        currentPiece = new Piece(random);
        nextPiece = new Piece(random);
        heldPiece = null;
        score = 0;
        gameOver = false;
        canHold = true;
        timer.start();
        repaint();
    }

    private void holdPiece() {
        if (!canHold) return;

        if (heldPiece == null) {
            heldPiece = new Piece(currentPiece);
            currentPiece = nextPiece;
            nextPiece = new Piece(random);
        } else {
            Piece temp = new Piece(heldPiece);
            heldPiece = new Piece(currentPiece);
            currentPiece = temp;
        }

        currentPiece.x = 3;
        currentPiece.y = 0;
        canHold = false;
    }

    private boolean movePiece(int dx, int dy) {
        if (!checkCollision(currentPiece, currentPiece.x + dx, currentPiece.y + dy)) {
            currentPiece.x += dx;
            currentPiece.y += dy;
            return true;
        }
        return false;
    }

    private void rotatePiece() {
        int[][] rotated = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                rotated[j][3 - i] = currentPiece.shape[i][j];
            }
        }

        int[][] oldShape = currentPiece.shape;
        currentPiece.shape = rotated;

        if (checkCollision(currentPiece, currentPiece.x, currentPiece.y)) {
            currentPiece.shape = oldShape;
        }
    }

    private void dropPiece() {
        while (movePiece(0, 1));
    }

    private int calculateGhostY() {
        int ghostY = currentPiece.y;
        while (!checkCollision(currentPiece, currentPiece.x, ghostY + 1)) {
            ghostY++;
        }
        return ghostY;
    }

    private boolean checkCollision(Piece piece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (piece.shape[i][j] == 1) {
                    int boardX = newX + j;
                    int boardY = newY + i;

                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                        return true;
                    }

                    if (boardY >= 0 && board[boardY][boardX] != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void placePiece() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (currentPiece.shape[i][j] == 1) {
                    int boardY = currentPiece.y + i;
                    int boardX = currentPiece.x + j;
                    if (boardY >= 0) {
                        board[boardY][boardX] = currentPiece.color;
                    }
                }
            }
        }
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean fullLine = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == null) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                linesCleared++;
                for (int k = i; k > 0; k--) {
                    board[k] = board[k - 1].clone();
                }
                board[0] = new Color[BOARD_WIDTH];
                i++;
            }
        }
        score += linesCleared * 100;
    }

    private void drawPiecePreview(Graphics g, Piece piece, int offsetX, int offsetY) {
        if (piece == null) return;

        g.setColor(piece.color);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (piece.shape[i][j] == 1) {
                    g.fillRect(offsetX + j * 20, offsetY + i * 20, 20, 20);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(offsetX + j * 20, offsetY + i * 20, 20, 20);
                    g.setColor(piece.color);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw board
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] != null) {
                    g.setColor(board[i][j]);
                    g.fillRect(j * BLOCK_SIZE, i * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(j * BLOCK_SIZE, i * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }

        // Draw grid
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g.drawLine(0, i * BLOCK_SIZE, BOARD_WIDTH * BLOCK_SIZE, i * BLOCK_SIZE);
        }
        for (int j = 0; j <= BOARD_WIDTH; j++) {
            g.drawLine(j * BLOCK_SIZE, 0, j * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
        }

        // Draw ghost piece
        if (!gameOver) {
            int ghostY = calculateGhostY();
            g.setColor(new Color(currentPiece.color.getRed(),
                    currentPiece.color.getGreen(),
                    currentPiece.color.getBlue(), 80));
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (currentPiece.shape[i][j] == 1) {
                        int x = (currentPiece.x + j) * BLOCK_SIZE;
                        int y = (ghostY + i) * BLOCK_SIZE;
                        g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                        g.setColor(Color.GRAY);
                        g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                        g.setColor(new Color(currentPiece.color.getRed(),
                                currentPiece.color.getGreen(),
                                currentPiece.color.getBlue(), 80));
                    }
                }
            }
        }

        // Draw current piece
        if (!gameOver) {
            g.setColor(currentPiece.color);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (currentPiece.shape[i][j] == 1) {
                        int x = (currentPiece.x + j) * BLOCK_SIZE;
                        int y = (currentPiece.y + i) * BLOCK_SIZE;
                        g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                        g.setColor(currentPiece.color);
                    }
                }
            }
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, BOARD_WIDTH * BLOCK_SIZE + 20, 30);

        // Draw hold box
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Hold (C/Shift):", BOARD_WIDTH * BLOCK_SIZE + 20, 70);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(BOARD_WIDTH * BLOCK_SIZE + 20, 80, 90, 90);
        drawPiecePreview(g, heldPiece, BOARD_WIDTH * BLOCK_SIZE + 30, 90);

        // Draw next piece box
        g.setColor(Color.WHITE);
        g.drawString("Next:", BOARD_WIDTH * BLOCK_SIZE + 20, 200);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(BOARD_WIDTH * BLOCK_SIZE + 20, 210, 90, 90);
        drawPiecePreview(g, nextPiece, BOARD_WIDTH * BLOCK_SIZE + 30, 220);

        // Draw controls
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Controls:", BOARD_WIDTH * BLOCK_SIZE + 20, 340);
        g.drawString("← → Move", BOARD_WIDTH * BLOCK_SIZE + 20, 360);
        g.drawString("↑ Rotate", BOARD_WIDTH * BLOCK_SIZE + 20, 380);
        g.drawString("↓ Soft Drop", BOARD_WIDTH * BLOCK_SIZE + 20, 400);
        g.drawString("Space Hard Drop", BOARD_WIDTH * BLOCK_SIZE + 20, 420);
        g.drawString("C/Shift Hold", BOARD_WIDTH * BLOCK_SIZE + 20, 440);

        // Game over message
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", 50, BOARD_HEIGHT * BLOCK_SIZE / 2 - 20);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Press ENTER to restart", 35, BOARD_HEIGHT * BLOCK_SIZE / 2 + 20);
        }
    }
}

class Piece {
    int[][] shape;
    Color color;
    int x, y;
    int type;

    private static final int[][][] SHAPES = {
            {{1,1,1,1},{0,0,0,0},{0,0,0,0},{0,0,0,0}}, // I
            {{1,1,0,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}}, // O
            {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}, // T
            {{1,1,0,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}, // S
            {{0,1,1,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}}, // Z
            {{1,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}, // L
            {{0,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}, // J
            {{1,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}, // U (new shape)
            {{0,1,0,0},{1,1,1,0},{0,1,0,0},{0,0,0,0}}  // Plus (new shape)
    };

    private static final Color[] COLORS = {
            Color.CYAN, Color.YELLOW, Color.MAGENTA,
            Color.GREEN, Color.RED, Color.ORANGE, Color.BLUE,
            new Color(255, 105, 180), // Pink for Y shape
            new Color(138, 43, 226)   // Purple for Plus shape
    };

    public Piece(Random random) {
        this.type = random.nextInt(SHAPES.length);
        shape = new int[4][4];
        for (int i = 0; i < 4; i++) {
            shape[i] = SHAPES[type][i].clone();
        }
        color = COLORS[type];
        x = 3;
        y = 0;
    }

    public Piece(Piece other) {
        this.type = other.type;
        this.shape = new int[4][4];
        for (int i = 0; i < 4; i++) {
            this.shape[i] = SHAPES[type][i].clone();
        }
        this.color = other.color;
        this.x = 3;
        this.y = 0;
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import java.util.Random;

public class Tetris extends JFrame {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22; // 2 extra rows for pieces spawning outside visible area
    private final int BLOCK_SIZE = 30;
    private final Color[] COLORS = {Color.BLACK, Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED};

    private JPanel boardPanel;
    private Timer timer;
    private int[][] board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private Random random;
    private boolean isPaused;
    private boolean isGameOver;
    private int score;

    public Tetris() {
        setTitle("Tetris");
        setSize(BOARD_WIDTH * BLOCK_SIZE, (BOARD_HEIGHT - 2) * BLOCK_SIZE); // Subtract 2 rows for hidden pieces
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        boardPanel.setBackground(Color.WHITE);
        add(boardPanel, BorderLayout.CENTER);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        random = new Random();
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused && !isGameOver) {
                    moveDown();
                }
            }
        });
        startGame();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startGame() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        score = 0;
        isPaused = false;
        isGameOver = false;
        timer.start();
        newPiece();
    }

    private void newPiece() {
        currentPiece = nextPiece != null ? nextPiece : Tetromino.randomTetromino(random);
        nextPiece = Tetromino.randomTetromino(random);

        // Check if new piece can be spawned
        if (!canMove(currentPiece, 0, 0)) {
            gameOver();
            return;
        }

        // Spawn piece at the top-middle of the board
        currentPiece.setX(BOARD_WIDTH / 2 - currentPiece.getSize() / 2);
        currentPiece.setY(0);

        // Check for full rows and remove them
        checkRows();

        // Repaint board
        boardPanel.repaint();
    }

    private void moveDown() {
        if (canMove(currentPiece, 0, 1)) {
            currentPiece.setY(currentPiece.getY() + 1);
        } else {
            placePiece();
            newPiece();
        }
        boardPanel.repaint();
    }

    private void moveLeft() {
        if (canMove(currentPiece, -1, 0)) {
            currentPiece.setX(currentPiece.getX() - 1);
        }
        boardPanel.repaint();
    }

    private void moveRight() {
        if (canMove(currentPiece, 1, 0)) {
            currentPiece.setX(currentPiece.getX() + 1);
        }
        boardPanel.repaint();
    }

    private void rotatePiece() {
        currentPiece.rotate();
        if (!canMove(currentPiece, 0, 0)) {
            currentPiece.rotateBack();
        }
        boardPanel.repaint();
    }

    private boolean canMove(Tetromino piece, int dx, int dy) {
        for (int row = 0; row < piece.getSize(); row++) {
            for (int col = 0; col < piece.getSize(); col++) {
                if (piece.getShape()[row][col] != 0) {
                    int newX = piece.getX() + col + dx;
                    int newY = piece.getY() + row + dy;
                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT || board[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placePiece() {
        for (int row = 0; row < currentPiece.getSize(); row++) {
            for (int col = 0; col < currentPiece.getSize(); col++) {
                if (currentPiece.getShape()[row][col] != 0) {
                    int boardX = currentPiece.getX() + col;
                    int boardY = currentPiece.getY() + row;
                    board[boardY][boardX] = currentPiece.getColorIndex();
                }
            }
        }
        clearLines();
    }

    private void clearLines() {
        ArrayList<Integer> fullRows = new ArrayList<>();
        for (int row = BOARD_HEIGHT - 1; row >= 0; row--) {
            boolean isFull = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                fullRows.add(row);
                score += 100; // Increase score for each cleared row
            }
        }

        for (int row : fullRows) {
            for (int r = row; r > 0; r--) {
                System.arraycopy(board[r - 1], 0, board[r], 0, BOARD_WIDTH);
            }
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[0][col] = 0;
            }
        }
        boardPanel.repaint();
    }

    private void checkRows() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            boolean isEmpty = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] != 0) {
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                continue;
            }
            for (int r = row; r > 0; r--) {
                System.arraycopy(board[r - 1], 0, board[r], 0, BOARD_WIDTH);
            }
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[0][col] = 0;
            }
        }
    }

    private void gameOver() {
        isGameOver = true;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        startGame();
    }

    private void drawBoard(Graphics g) {
        // Draw current pieces on the board
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                int colorIndex = board[row][col];
                if (colorIndex != 0) {
                    g.setColor(COLORS[colorIndex]);
                    g.fillRect(col * BLOCK_SIZE, (row - 2) * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }

        // Draw current piece
        if (!isPaused && !isGameOver && currentPiece != null) {
            int[][] shape = currentPiece.getShape();
            for (int row = 0; row < currentPiece.getSize(); row++) {
                for (int col = 0; col < currentPiece.getSize(); col++) {
                    int colorIndex = shape[row][col];
                    if (colorIndex != 0) {
                        g.setColor(COLORS[colorIndex]);
                        g.fillRect((currentPiece.getX() + col) * BLOCK_SIZE, (currentPiece.getY() + row - 2) * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                    }
                }
            }
        }

        // Draw next piece preview
        g.setColor(Color.BLACK);
        g.drawString("Next piece:", BOARD_WIDTH * BLOCK_SIZE + 20, 20);
        if (nextPiece != null) {
            int[][] nextShape = nextPiece.getShape();
            for (int row = 0; row < nextPiece.getSize(); row++) {
                for (int col = 0; col < nextPiece.getSize(); col++) {
                    int colorIndex = nextShape[row][col];
                    if (colorIndex != 0) {
                        g.setColor(COLORS[colorIndex]);
                        g.fillRect((BOARD_WIDTH + 1 + col) * BLOCK_SIZE, (3 + row) * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                    }
                }
            }
        }

        // Draw score
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, BOARD_WIDTH * BLOCK_SIZE + 20, 200);

        // Draw game over message
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.drawString("GAME OVER", 50, 300);
        }
    }

    private void handleKeyPress(KeyEvent e) {
        if (isGameOver) {
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                moveRight();
                break;
            case KeyEvent.VK_DOWN:
                moveDown();
                break;
            case KeyEvent.VK_UP:
                rotatePiece();
                break;
            case KeyEvent.VK_SPACE:
                placePiece();
                break;
            case KeyEvent.VK_P:
                isPaused = !isPaused;
                if (isPaused) {
                    timer.stop();
                } else {
                    timer.start();
                }
                boardPanel.repaint();
                break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Tetris();
        });
    }
}

class Tetromino {
    private int[][] shape;
    private int size;
    private int x;
    private int y;
    private int colorIndex;

    private static final int[][][] SHAPES = {
            { {1, 1, 1, 1} },  // I-shape
            { {1, 1, 0},
              {0, 1, 1} },   // Z-shape
            { {0, 1, 1},
              {1, 1, 0} },   // S-shape
            { {1, 1},
              {1, 1} },      // O-shape
            { {1, 1, 1},
              {0, 1, 0} },   // T-shape
            { {1, 1},
              {1, 0},
              {1, 0} },     // L-shape
            { {1, 1},
              {0, 1},
              {0, 1} }      // J-shape
    };

    private static final int[] COLORS = { 0, 1, 2, 3, 4, 5, 6 };

    private Tetromino(int[][] shape, int colorIndex) {
        this.shape = shape;
        this.size = shape.length;
        this.colorIndex = colorIndex;
    }

    public static Tetromino randomTetromino(Random random) {
        int index = random.nextInt(SHAPES.length);
        int[][] shape = SHAPES[index];
        int colorIndex = COLORS[index + 1]; // Skip 0 (background color)
        return new Tetromino(shape, colorIndex);
    }

    public void rotate() {
        int[][] rotated = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                rotated[col][size - 1 - row] = shape[row][col];
            }
        }
        shape = rotated;
    }

    public void rotateBack() {
        int[][] rotated = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                rotated[row][col] = shape[col][size - 1 - row];
            }
        }
        shape = rotated;
    }

    public int[][] getShape() {
        return shape;
    }

    public int getSize() {
        return size;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getColorIndex() {
        return colorIndex;
    }
}
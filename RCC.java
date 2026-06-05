import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RCC extends JFrame {
    private Piece[][] board = new Piece[10][9];
    private JLabel[][] cells = new JLabel[10][9];
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean redTurn = true;
    private JLabel statusLabel;
    private JLabel redWinLabel;
    private JLabel blackWinLabel;
    private JLabel drawLabel;
    private int redWins = 0, blackWins = 0, draws = 0;
    
    // Traditional piece positions
    private static final Map<String, String> TRADITIONAL = new HashMap<>();
    static {
        String[] black = {"車", "馬", "象", "士", "將", "士", "象", "馬", "車"};
        for (int i = 0; i < 9; i++) TRADITIONAL.put("0," + i, black[i]);
        TRADITIONAL.put("2,1", "砲");
        TRADITIONAL.put("2,7", "砲");
        int[] pawnCols = {0, 2, 4, 6, 8};
        for (int i = 0; i < 5; i++) TRADITIONAL.put("3," + pawnCols[i], "兵");
        
        String[] red = {"車", "馬", "象", "士", "帥", "士", "象", "馬", "車"};
        for (int i = 0; i < 9; i++) TRADITIONAL.put("9," + i, red[i]);
        TRADITIONAL.put("7,1", "炮");
        TRADITIONAL.put("7,7", "炮");
        for (int i = 0; i < 5; i++) TRADITIONAL.put("6," + pawnCols[i], "卒");
    }
    
    private static final String[] PIECE_TYPES = {"車", "馬", "象", "士", "砲", "炮", "兵", "卒"};
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RCC().setVisible(true));
    }
    
    public RCC() {
        setTitle("Random Chinese Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 950);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Top status
        JPanel topPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Red's turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(255, 255, 200));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(statusLabel, BorderLayout.CENTER);
        
        // Score panel
        JPanel scorePanel = new JPanel(new FlowLayout());
        redWinLabel = new JLabel("Red: " + redWins);
        blackWinLabel = new JLabel("Black: " + blackWins);
        drawLabel = new JLabel("Draws: " + draws);
        redWinLabel.setFont(new Font("Arial", Font.BOLD, 14));
        blackWinLabel.setFont(new Font("Arial", Font.BOLD, 14));
        drawLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scorePanel.add(redWinLabel);
        scorePanel.add(blackWinLabel);
        scorePanel.add(drawLabel);
        topPanel.add(scorePanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Board
        BoardPanel boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);
        
        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> newGame());
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        gameMenu.add(newGame);
        gameMenu.addSeparator();
        gameMenu.add(exit);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem rules = new JMenuItem("Rules");
        rules.addActionListener(e -> showRules());
        helpMenu.add(rules);
        
        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        
        newGame();
    }
    
    private void newGame() {
        randomizeBoard();
        redTurn = true;
        selectedRow = -1;
        selectedCol = -1;
        statusLabel.setText("Red's turn");
        updateBoardDisplay();
        repaint();
    }
    
    private void randomizeBoard() {
        // Clear board
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 9; j++)
                board[i][j] = null;
        
        // Get all positions except kings
        java.util.ArrayList<String> positions = new java.util.ArrayList<String>();
        for (String key : TRADITIONAL.keySet()) {
            positions.add(key);
        }
        positions.remove("9,4");
        positions.remove("0,4");
        
        // Randomize pieces
        java.util.ArrayList<String> pieces = new java.util.ArrayList<String>();
        for (int i = 0; i < positions.size(); i++) {
            pieces.add(PIECE_TYPES[i % PIECE_TYPES.length]);
        }
        Collections.shuffle(pieces);
        
        // Place randomized pieces
        for (int i = 0; i < positions.size(); i++) {
            String[] pos = positions.get(i).split(",");
            int row = Integer.parseInt(pos[0]);
            int col = Integer.parseInt(pos[1]);
            boolean isRed = (row >= 6);
            board[row][col] = new Piece(pieces.get(i), isRed, false);
        }
        
        // Place kings
        board[9][4] = new Piece("帥", true, true);
        board[0][4] = new Piece("將", false, true);
    }
    
    private void updateBoardDisplay() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece p = board[row][col];
                if (p != null) {
                    if (p.revealed) {
                        cells[row][col].setText(p.type);
                        cells[row][col].setForeground(p.isRed ? new Color(200, 50, 50) : Color.BLACK);
                    } else {
                        cells[row][col].setText("?");
                        cells[row][col].setForeground(Color.GRAY);
                    }
                } else {
                    cells[row][col].setText("");
                }
            }
        }
        repaint();
    }
    
    private void tryMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        
        if (piece == null) {
            statusLabel.setText("No piece there!");
            return;
        }
        
        if (piece.isRed != redTurn) {
            statusLabel.setText(redTurn ? "Red's turn!" : "Black's turn!");
            return;
        }
        
        // Check if move is valid
        if (!isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
            statusLabel.setText("Invalid move!");
            return;
        }
        
        // Make the move
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        
        // Reveal piece if hidden
        if (!piece.revealed) {
            piece.revealed = true;
            statusLabel.setText("Piece revealed: " + piece.type);
        }
        
        updateBoardDisplay();
        
        // Switch turns
        redTurn = !redTurn;
        statusLabel.setText(redTurn ? "Red's turn" : "Black's turn");
        selectedRow = -1;
        selectedCol = -1;
    }
    
    private boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (toRow < 0 || toRow >= 10 || toCol < 0 || toCol >= 9) return false;
        if (board[toRow][toCol] != null && board[toRow][toCol].isRed == piece.isRed) return false;
        
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        
        String type = piece.revealed ? piece.type : TRADITIONAL.get(fromRow + "," + fromCol);
        if (type == null) type = piece.type;
        
        // King
        if (type.equals("帥") || type.equals("將")) {
            if (piece.isRed && (toRow < 7 || toRow > 9 || toCol < 3 || toCol > 5)) return false;
            if (!piece.isRed && (toRow < 0 || toRow > 2 || toCol < 3 || toCol > 5)) return false;
            if (dr + dc != 1) return false;
            return !isFlyingKing(toRow, toCol, piece.isRed);
        }
        
        // Chariot
        if (type.equals("車")) {
            return isClearPath(fromRow, fromCol, toRow, toCol);
        }
        
        // Horse
        if (type.equals("馬")) {
            if (dr == 2 && dc == 1) {
                int blockRow = fromRow + (toRow - fromRow) / 2;
                return board[blockRow][fromCol] == null;
            }
            if (dr == 1 && dc == 2) {
                int blockCol = fromCol + (toCol - fromCol) / 2;
                return board[fromRow][blockCol] == null;
            }
            return false;
        }
        
        // Elephant
        if (type.equals("象")) {
            if (dr == 2 && dc == 2) {
                int midRow = (fromRow + toRow) / 2;
                int midCol = (fromCol + toCol) / 2;
                return board[midRow][midCol] == null;
            }
            return false;
        }
        
        // Advisor
        if (type.equals("士")) {
            if (piece.isRed && (toRow < 7 || toRow > 9 || toCol < 3 || toCol > 5)) return false;
            if (!piece.isRed && (toRow < 0 || toRow > 2 || toCol < 3 || toCol > 5)) return false;
            return (dr == 1 && dc == 1);
        }
        
        // Cannon
        if (type.equals("炮") || type.equals("砲")) {
            int between = countPiecesBetween(fromRow, fromCol, toRow, toCol);
            boolean hasTarget = board[toRow][toCol] != null;
            if (hasTarget) return between == 1;
            return between == 0 && isClearPath(fromRow, fromCol, toRow, toCol);
        }
        
        // Pawn
        if (type.equals("兵") || type.equals("卒")) {
            int dir = piece.isRed ? -1 : 1;
            boolean crossedRiver = piece.isRed ? (toRow < 5) : (toRow > 4);
            if (toCol == fromCol && toRow == fromRow + dir) return true;
            if (crossedRiver && dr == 0 && dc == 1) return true;
            return false;
        }
        
        return false;
    }
    
    private boolean isClearPath(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow) {
            int step = (toCol > fromCol) ? 1 : -1;
            for (int c = fromCol + step; c != toCol; c += step)
                if (board[fromRow][c] != null) return false;
            return true;
        }
        if (fromCol == toCol) {
            int step = (toRow > fromRow) ? 1 : -1;
            for (int r = fromRow + step; r != toRow; r += step)
                if (board[r][fromCol] != null) return false;
            return true;
        }
        return false;
    }
    
    private int countPiecesBetween(int fromRow, int fromCol, int toRow, int toCol) {
        int count = 0;
        if (fromRow == toRow) {
            int step = (toCol > fromCol) ? 1 : -1;
            for (int c = fromCol + step; c != toCol; c += step)
                if (board[fromRow][c] != null) count++;
        } else if (fromCol == toCol) {
            int step = (toRow > fromRow) ? 1 : -1;
            for (int r = fromRow + step; r != toRow; r += step)
                if (board[r][fromCol] != null) count++;
        }
        return count;
    }
    
    private boolean isFlyingKing(int row, int col, boolean isRed) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != null) {
                    String type = board[i][j].type;
                    if ((isRed && type.equals("將")) || (!isRed && type.equals("帥"))) {
                        if (col == j) {
                            int minRow = Math.min(row, i);
                            int maxRow = Math.max(row, i);
                            for (int r = minRow + 1; r < maxRow; r++)
                                if (board[r][col] != null) return false;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void showRules() {
        String rules = "Random Chinese Chess Rules:\n\n" +
            "• Only Kings are visible at start\n" +
            "• Other pieces show as '?'\n" +
            "• First move reveals the piece's true type\n\n" +
            "Click a piece, then click destination to move!";
        JOptionPane.showMessageDialog(this, rules, "Rules", JOptionPane.INFORMATION_MESSAGE);
    }
    
    class BoardPanel extends JPanel {
        private int cellW, cellH;
        private int startX, startY;
        
        public BoardPanel() {
            setBackground(new Color(255, 218, 145));
            setLayout(null);
            
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 9; col++) {
                    final int r = row, c = col;
                    cells[r][c] = new JLabel("", SwingConstants.CENTER);
                    cells[r][c].setFont(new Font("SimHei", Font.BOLD, 22));
                    cells[r][c].setForeground(Color.BLACK);
                    cells[r][c].addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            if (selectedRow == -1) {
                                if (board[r][c] != null && board[r][c].isRed == redTurn) {
                                    selectedRow = r;
                                    selectedCol = c;
                                    statusLabel.setText("Selected. Click destination.");
                                    repaint();
                                } else if (board[r][c] != null) {
                                    statusLabel.setText(redTurn ? "Red's turn!" : "Black's turn!");
                                }
                            } else {
                                tryMove(selectedRow, selectedCol, r, c);
                                selectedRow = -1;
                                selectedCol = -1;
                                repaint();
                            }
                        }
                    });
                    add(cells[r][c]);
                }
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            calculateDimensions();
            drawBoardLines(g);
            drawPieces(g);
            positionCells();
            
            // Draw highlight for selected piece
            if (selectedRow != -1) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(255, 255, 0, 100));
                g2.setStroke(new BasicStroke(3));
                int x = startX + selectedCol * cellW;
                int y = startY + selectedRow * cellH;
                g2.drawOval(x - cellW/2, y - cellH/2, cellW, cellH);
            }
        }
        
        private void calculateDimensions() {
            int w = getWidth();
            int h = getHeight();
            cellW = (w - 60) / 8;
            cellH = (h - 80) / 9;
            int size = Math.min(cellW, cellH);
            cellW = size;
            cellH = size;
            startX = (w - cellW * 8) / 2;
            startY = (h - cellH * 9) / 2;
        }
        
        private void drawBoardLines(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            
            int rightEdge = startX + 8 * cellW;
            int bottomEdge = startY + 9 * cellH;
            int riverTop = startY + 4 * cellH;
            int riverBottom = startY + 5 * cellH;
            
            // Horizontal lines
            for (int i = 0; i < 10; i++) {
                int y = startY + i * cellH;
                g2.drawLine(startX, y, rightEdge, y);
            }
            
            // Vertical lines
            for (int i = 0; i < 9; i++) {
                int x = startX + i * cellW;
                if (i == 0 || i == 8) {
                    g2.drawLine(x, startY, x, bottomEdge);
                } else {
                    g2.drawLine(x, startY, x, riverTop);
                    g2.drawLine(x, riverBottom, x, bottomEdge);
                }
            }
            
            // Palace diagonals
            int palaceLeft = startX + 3 * cellW;
            int palaceRight = startX + 5 * cellW;
            g2.drawLine(palaceLeft, startY, palaceRight, startY + 2 * cellH);
            g2.drawLine(palaceRight, startY, palaceLeft, startY + 2 * cellH);
            g2.drawLine(palaceLeft, startY + 7 * cellH, palaceRight, startY + 9 * cellH);
            g2.drawLine(palaceRight, startY + 7 * cellH, palaceLeft, startY + 9 * cellH);
            
            // Cross markers
            int[][] crosses = {
                {2,1},{2,7},{3,0},{3,2},{3,4},{3,6},{3,8},
                {6,0},{6,2},{6,4},{6,6},{6,8},{7,1},{7,7}
            };
            g2.setColor(new Color(120, 80, 40));
            for (int[] p : crosses) {
                int x = startX + p[1] * cellW;
                int y = startY + p[0] * cellH;
                int arm = Math.max(3, cellW / 6);
                g2.drawLine(x - arm, y, x - 2, y);
                g2.drawLine(x + 2, y, x + arm, y);
                g2.drawLine(x, y - arm, x, y - 2);
                g2.drawLine(x, y + 2, x, y + arm);
                g2.fillOval(x - 2, y - 2, 4, 4);
            }
        }
        
        private void drawPieces(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 9; col++) {
                    Piece p = board[row][col];
                    if (p != null) {
                        int x = startX + col * cellW;
                        int y = startY + row * cellH;
                        int radius = cellW / 2 - 4;
                        
                        // Shadow
                        g2.setColor(new Color(0, 0, 0, 50));
                        g2.fillOval(x - radius + 2, y - radius + 2, radius * 2, radius * 2);
                        
                        // Main circle
                        if (!p.revealed) {
                            g2.setColor(new Color(150, 150, 150));
                        } else if (p.isRed) {
                            g2.setColor(new Color(220, 80, 80));
                        } else {
                            g2.setColor(new Color(70, 70, 70));
                        }
                        g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                        
                        // Border
                        g2.setColor(Color.BLACK);
                        g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                    }
                }
            }
        }
        
        private void positionCells() {
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 9; col++) {
                    int x = startX + col * cellW - cellW/2;
                    int y = startY + row * cellH - cellH/2;
                    cells[row][col].setBounds(x, y, cellW, cellH);
                }
            }
        }
    }
    
    class Piece {
        String type;
        boolean isRed;
        boolean revealed;
        
        Piece(String type, boolean isRed, boolean revealed) {
            this.type = type;
            this.isRed = isRed;
            this.revealed = revealed;
        }
    }
}
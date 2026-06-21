import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TicTacToe extends JFrame {

    // ===================== CONFIG =====================
    private static final int SIZE = 3;
    private static final Color BG_COLOR       = new Color(15, 17, 26);
    private static final Color GRID_COLOR     = new Color(40, 44, 66);
    private static final Color X_COLOR        = new Color(99, 179, 237);   // blue
    private static final Color O_COLOR        = new Color(252, 129, 74);   // orange
    private static final Color WIN_COLOR      = new Color(72, 199, 142);   // green highlight
    private static final Color STATUS_COLOR   = new Color(200, 205, 230);
    private static final Color BUTTON_BG      = new Color(30, 33, 48);
    private static final Color RESET_COLOR    = new Color(99, 179, 237);

    // ===================== STATE ======================
    private char[][] board = new char[SIZE][SIZE];
    private char currentPlayer = 'X';
    private boolean gameOver = false;
    private int[] winLine = null; // stores winning cells: [r1,c1, r2,c2, r3,c3]

    // ===================== UI ==========================
    private CellButton[][] cells = new CellButton[SIZE][SIZE];
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private int scoreX = 0, scoreO = 0;

    // ==================================================
    public TicTacToe() {
        super("Tic Tac Toe");
        initBoard();
        buildUI();
        setVisible(true);
    }

    private void initBoard() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                board[r][c] = ' ';
        currentPlayer = 'X';
        gameOver = false;
        winLine = null;
    }

    // ────────── UI BUILD ──────────
    private void buildUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 8, 24));

        JLabel title = new JLabel("TIC TAC TOE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(200, 205, 230));
        panel.add(title, BorderLayout.NORTH);

        scoreLabel = new JLabel(getScoreText(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        scoreLabel.setForeground(new Color(120, 130, 160));
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(scoreLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildGrid() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_COLOR);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel grid = new JPanel(new GridLayout(SIZE, SIZE, 6, 6));
        grid.setBackground(GRID_COLOR);
        grid.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 6));
        grid.setPreferredSize(new Dimension(360, 360));

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                CellButton btn = new CellButton(r, c);
                cells[r][c] = btn;
                grid.add(btn);
            }
        }

        outer.add(grid);
        return outer;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 24, 24, 24));

        statusLabel = new JLabel(getStatusText(), SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        statusLabel.setForeground(STATUS_COLOR);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JButton reset = new JButton("New Game");
        reset.setFont(new Font("SansSerif", Font.BOLD, 13));
        reset.setForeground(BG_COLOR);
        reset.setBackground(RESET_COLOR);
        reset.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
        reset.setFocusPainted(false);
        reset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reset.addActionListener(e -> resetGame());

        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(reset, BorderLayout.CENTER);
        return panel;
    }

    // ────────── GAME LOGIC ──────────
    private void handleClick(int row, int col) {
        if (gameOver || board[row][col] != ' ') return;

        board[row][col] = currentPlayer;
        cells[row][col].repaint();

        int[] win = checkWin();
        if (win != null) {
            winLine = win;
            gameOver = true;
            if (currentPlayer == 'X') scoreX++; else scoreO++;
            scoreLabel.setText(getScoreText());
            statusLabel.setText("🎉 Player " + currentPlayer + " wins!");
            statusLabel.setForeground(WIN_COLOR);
            highlightWin();
        } else if (isBoardFull()) {
            gameOver = true;
            statusLabel.setText("It's a draw! 🤝");
            statusLabel.setForeground(new Color(200, 180, 80));
        } else {
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            statusLabel.setText(getStatusText());
            statusLabel.setForeground(STATUS_COLOR);
        }
    }

    /** Returns {r1,c1, r2,c2, r3,c3} of winning line, or null */
    private int[] checkWin() {
        // rows
        for (int r = 0; r < SIZE; r++)
            if (allSame(r,0, r,1, r,2))
                return new int[]{r,0, r,1, r,2};
        // cols
        for (int c = 0; c < SIZE; c++)
            if (allSame(0,c, 1,c, 2,c))
                return new int[]{0,c, 1,c, 2,c};
        // diagonals
        if (allSame(0,0, 1,1, 2,2)) return new int[]{0,0, 1,1, 2,2};
        if (allSame(0,2, 1,1, 2,0)) return new int[]{0,2, 1,1, 2,0};
        return null;
    }

    private boolean allSame(int r1,int c1, int r2,int c2, int r3,int c3) {
        char ch = board[r1][c1];
        return ch != ' ' && ch == board[r2][c2] && ch == board[r3][c3];
    }

    private boolean isBoardFull() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == ' ') return false;
        return true;
    }

    private void highlightWin() {
        if (winLine == null) return;
        for (int i = 0; i < 6; i += 2)
            cells[winLine[i]][winLine[i+1]].setWinHighlight(true);
        repaint();
    }

    private void resetGame() {
        initBoard();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                cells[r][c].setWinHighlight(false);
                cells[r][c].repaint();
            }
        statusLabel.setText(getStatusText());
        statusLabel.setForeground(STATUS_COLOR);
    }

    private String getStatusText() {
        return "Player " + currentPlayer + "'s turn  (" + (currentPlayer=='X' ? "✕" : "○") + ")";
    }

    private String getScoreText() {
        return "  X  " + scoreX + "  —  " + scoreO + "  O  ";
    }

    // ────────── CELL BUTTON ──────────
    private class CellButton extends JPanel {
        final int row, col;
        boolean winHighlight = false;

        CellButton(int r, int c) {
            this.row = r; this.col = c;
            setBackground(BUTTON_BG);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 110));
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e)  { handleClick(row, col); }
                public void mouseEntered(MouseEvent e)  {
                    if (!gameOver && board[row][col] == ' ')
                        setBackground(new Color(40, 44, 66));
                }
                public void mouseExited(MouseEvent e)   {
                    if (!winHighlight) setBackground(BUTTON_BG);
                }
            });
        }

        void setWinHighlight(boolean h) {
            winHighlight = h;
            setBackground(h ? new Color(20, 60, 40) : BUTTON_BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            char ch = board[row][col];
            if (ch == ' ') return;

            int w = getWidth(), h = getHeight();
            int pad = 22;

            if (ch == 'X') {
                g2.setColor(winHighlight ? WIN_COLOR : X_COLOR);
                g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(pad, pad, w-pad, h-pad);
                g2.drawLine(w-pad, pad, pad, h-pad);
            } else {
                g2.setColor(winHighlight ? WIN_COLOR : O_COLOR);
                g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(pad, pad, w-2*pad, h-2*pad);
            }
        }
    }

    // ────────── MAIN ──────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new TicTacToe();
        });
    }
}
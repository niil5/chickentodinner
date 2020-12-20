package edu.upc.epsevg.prop.amazons;

/**
 *
 * @author Bernat Orellana
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class AmazonsBoard extends MouseAdapter {

    private JFrame mainFrame;

    private BufferedImage image = null;
    private BufferedImage image_QB;
    private BufferedImage image_QW;
    int midaTauler;
    int midaCasella;
    int marginW;
    int marginH;
    int timeoutSeconds;

    private IPlayer players[];
    private GameStatus status;

    private CellType curPlayer = CellType.PLAYER1;
    private UIStates gameEstatus;
    private JControlsPanel controlPanel;
    private JPanel boardPanel;
    private int baseX;
    private int baseY;

    private Level level;
    
    Point selectedQueenPosition;

    private IPlayer getCurrentPlayer() {
        return players[CellType.toColor01(curPlayer)];
    }

    private boolean isCurrentPlayerAuto() {
        return getCurrentPlayer() instanceof IAuto;
    }

    private Point convertScreenToBoard(double x, double y) {

        if (x < marginW || y < marginW || x > midaTauler + marginW || y > midaTauler + marginH) {
            return null;
        }

        int xx = (int) ((x - marginW) / midaCasella);
        int yy = (int) ((y - marginH) / midaCasella);
        return new Point(xx, yy);
    }

    private void showMessageAndButton(String A, String B, String buttonMessage, boolean buttonEnabled) {

        controlPanel.highlightPlayer(curPlayer);

        if (curPlayer == CellType.PLAYER1) {
            controlPanel.setPlayer1Message(A);
            controlPanel.setPlayer2Message(B);

        } else {
            controlPanel.setPlayer2Message(A);
            controlPanel.setPlayer1Message(B);
        }
        controlPanel.setButtonText(buttonMessage);
        controlPanel.setButtonEnabled(buttonEnabled);
    }

    private enum UIStates {
        INIT,
        PLAYING_QUEEN_FROM,
        PLAYING_QUEEN_TO,
        PLAYING_ARROW,
        END_GAME
    }
    


    public AmazonsBoard() {

        initComponents();

    }

    AmazonsBoard(IPlayer player1, IPlayer player2, int timeoutSeconds, Level level) {
        this.status = new GameStatus(level);
        this.timeoutSeconds = timeoutSeconds;
        this.players = new IPlayer[2];
        this.players[0] = player1;
        this.players[1] = player2;

        this.gameEstatus = UIStates.INIT;
        this.curPlayer = CellType.PLAYER1;
        
        this.level = level;
        
        initComponents();
        showCurrentStatus();
    }

    private void showCurrentStatus() {

        switch (gameEstatus) {
            case INIT: {
                controlPanel.setThinking(false);
                controlPanel.setPlayer1Name(players[0].getName());
                controlPanel.setPlayer2Name(players[1].getName());
                String clicToStart = "Click START !";
                controlPanel.setPlayer1Message(clicToStart);
                controlPanel.setPlayer2Message(clicToStart);
                controlPanel.setButtonText("Start the game");
                controlPanel.setButtonEnabled(true);
            }
            break;
            case END_GAME: {
                controlPanel.setThinking(false);
                showMessageAndButton("YOU WIN ! :-D ", "You lose :_(", "Another game?", true);
            }
            break;
            case PLAYING_QUEEN_FROM: {
                controlPanel.setThinking(false);
                String waiting = "Waiting....";
                String yourTurn = isCurrentPlayerAuto() ? "Thinking..." : "Your Turn. Choose queen to move.";
                showMessageAndButton(yourTurn, waiting, "Stop", !isCurrentPlayerAuto());
            }
            break;

            case PLAYING_QUEEN_TO: {
                controlPanel.setThinking(false);
                String waiting = "Waiting....";
                String yourTurn = isCurrentPlayerAuto() ? "Thinking..." : "Please choose queen destiny.";
                showMessageAndButton(yourTurn, waiting, "Stop", !isCurrentPlayerAuto());
            }
            break;
            case PLAYING_ARROW: {
                controlPanel.setThinking(false);
                String waiting = "Waiting....";
                String yourTurn = isCurrentPlayerAuto() ? "Thinking..." : "Please place the arrow.";
                showMessageAndButton(yourTurn, waiting, "Stop", !isCurrentPlayerAuto());
            }
            break;
        }
    }

    void OnStartClicked() {
        status = new GameStatus(level);
        boardPanel.repaint();
        curPlayer = CellType.PLAYER1;
        if (gameEstatus == UIStates.PLAYING_QUEEN_FROM) { //wish to STOP
            gameEstatus = UIStates.INIT;
            showCurrentStatus();
        } else if (gameEstatus == UIStates.INIT || gameEstatus == UIStates.END_GAME) {
            gameEstatus = UIStates.PLAYING_QUEEN_FROM;
            showCurrentStatus();
            startTurn();
        }

    }

    private void startTurn() {
        if (isCurrentPlayerAuto()) {
            this.controlPanel.setThinking(true);
            Mover m = new Mover();
            Watchdog w = new Watchdog(m, timeoutSeconds);
            m.setWatchdog(w);
            w.execute();
            m.execute();
            //(new Mover()).doInBackground();
        } else {

        }
    }

    private void endTurn() {
        
        if (status.isGameOver()) {
            gameEstatus = UIStates.END_GAME;
            showCurrentStatus();
        } else {
            curPlayer = CellType.opposite(curPlayer);
            gameEstatus = UIStates.PLAYING_QUEEN_FROM;
            showCurrentStatus();
            startTurn();
        }
    }

    class Watchdog extends SwingWorker<Void, Object> {

        Mover m;
        int timeoutSeconds;

        Watchdog(Mover m, int timeoutSeconds) {
            this.m = m;
            this.timeoutSeconds = timeoutSeconds;
        }

        @Override
        public Void doInBackground() {
            try {
                Thread.sleep(timeoutSeconds * 1000);
            } catch (InterruptedException ex) {
            }
            return null;
        }

        @Override
        protected void done() {
            m.timeout();
        }
    }

    class Mover extends SwingWorker<Move, Object> {

        Watchdog w;
        boolean hasMoved = false;
        
        Mover() {

        }

        public void timeout() {
            if(!hasMoved) getCurrentPlayer().timeout();
        }

        @Override
        public Move doInBackground() {
            try {
                Move m = getCurrentPlayer().move(new GameStatus(status));//, curPlayer); 
                String info="Profunditat màxima:"+m.getMaxDepthReached()+"\n";
                      info+="Node explorats:    "+m.getNumerOfNodesExplored();
                AmazonsBoard.this.controlPanel.setInfo(info);
                hasMoved = true;
                return m;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                System.out.println(sw.toString());
                e.printStackTrace();
            }
            return null;
        }

        public void setWatchdog(Watchdog w){
         this.w = w;   
        }
        
        @Override
        protected void done() {
            try {
                Move m = get();
                if(w!=null) {
                    w.cancel(true);
                }

                status.moveAmazon(m.getAmazonFrom(), m.getAmazonTo());
                status.placeArrow(m.getArrowTo());
                 
                AmazonsBoard.this.controlPanel.setThinking(false);
                //System.out.println(">" + status.toString());
                boardPanel.repaint();
                endTurn();
            } catch (Exception ignore) {
            }
        }

    }

    private int getX(int col) {
        return (int) (marginW + midaCasella * (col + 0.5));
    }

    private int getY(int fil) {
        return (int) (marginH + midaCasella * (fil + 0.5));
    }

    private void initComponents() {
        try {
            image = ImageIO.read(getClass().getResource("/resources/back2.jpg"));
            image_QW = ImageIO.read(getClass().getResource("/resources/white_queen.png"));
            image_QB = ImageIO.read(getClass().getResource("/resources/black_queen.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mainFrame = new JFrame();
        //mainMap.addComponentListener(this);
        //mainMap.setResizable(false);

        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Color darkColor = new Color(45, 72, 106, 255);
                Color whiteColor = new Color(255, 255, 255, 255);
                Color backColor = new Color(241, 200, 134, 255);
                Color blackColor = new Color(20, 30, 50, 255);
                Color highlightColor = new Color(255, 0, 255, 50);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                midaTauler = (int) (Math.min(getWidth(), getHeight()) * 0.9);
                midaCasella = midaTauler / status.getSize();
                midaTauler = midaCasella * status.getSize();
                marginW = (int) ((getWidth() - midaTauler) * 0.5);
                marginH = (int) ((getHeight() - midaTauler) * 0.5);

                /*  g2d.setColor(whiteColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(whiteColor);*/
                //g2d.drawRect(0, 0, getWidth(), getHeight());
                //Draw the board square
                g2d.setColor(blackColor);
                //g2d.setStroke(new BasicStroke(6));
                g2d.fillRect(marginW - 3, marginH - 3, midaTauler + 6, midaTauler + 6);

                g.setColor(whiteColor);
                g2d.fillRect(marginW, marginH, midaTauler, midaTauler);

                //Draw cells
                int oh = 0;
                for (int i = 0; i < status.getSize(); i++) {
                    int ow = 0;
                    for (int j = 0; j < status.getSize(); j++) {
                        // -----------------------------------
                        // Draw black cells
                        if ((i + j) % 2 == 0) {
                            g2d.setColor(darkColor);
                            g2d.fillRect(marginW + ow, marginH + oh, midaCasella, midaCasella);
                        }
                        // -----------------------------------
                        // Show highlight                                               
                        if (status.isHighlighted(new Point(j, i))) {
                            g2d.setColor(highlightColor);
                            g2d.fillRect(marginW + ow, marginH + oh, midaCasella, midaCasella);
                        }

                        CellType cell = (status.getPos(j, i));
                        switch (cell) {
                            case PLAYER1:
                                paintQueen(g2d, true, AmazonsBoard.this.getX(j), AmazonsBoard.this.getY(i), midaCasella / 2);
                                break;
                            case PLAYER2:
                                paintQueen(g2d, false, AmazonsBoard.this.getX(j), AmazonsBoard.this.getY(i), midaCasella / 2);
                                break;
                            case ARROW:
                                paintCross(g2d, AmazonsBoard.this.getX(j), AmazonsBoard.this.getY(i), (int) (midaCasella / 2.25));
                                break;
                        }

                        ow += midaCasella;
                    }
                    oh += midaCasella;
                }
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(500, 500);//(int) (n * dx + (n - 1) * h) + 200, (int) (n * dy + 200));
            }
        };

        boardPanel.addMouseListener(this);

        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        controlPanel = new JControlsPanel(this);
        controlPanel.setThinking(true);
        mainPane.add(controlPanel, BorderLayout.WEST);
        mainPane.add(boardPanel, BorderLayout.CENTER);

        Dimension dB = boardPanel.getPreferredSize();
        Dimension dP = controlPanel.getMinimumSize();
        Dimension d = new Dimension(dB.width + dP.width, dB.height);
        mainFrame.setMinimumSize(d);
        mainFrame.add(mainPane);
        mainFrame.pack();
        mainFrame.setVisible(true);

    }

    @Override
    public void mouseClicked(MouseEvent me) {
        
        if (gameEstatus == UIStates.PLAYING_QUEEN_FROM && !isCurrentPlayerAuto()) {

            selectedQueenPosition = convertScreenToBoard(me.getX(), me.getY());
            if (selectedQueenPosition != null) {

                if (status.getPos(selectedQueenPosition) == curPlayer) { // la reina és del mateix color que la que toca jugar

                    gameEstatus = UIStates.PLAYING_QUEEN_TO;
                    status.setQueenHighlight(selectedQueenPosition);
                    showCurrentStatus();
                }
            }
            System.out.println(">" + status.toString());
            boardPanel.repaint();
            //endTurn();
        } else if (gameEstatus == UIStates.PLAYING_QUEEN_TO) {
            Point to = convertScreenToBoard(me.getX(), me.getY());
            if (to != null && status.isHighlighted(to)) {
                status.moveAmazon(selectedQueenPosition, to);
                status.setQueenHighlight(null);
                boardPanel.repaint();
                gameEstatus = UIStates.PLAYING_ARROW;
                showCurrentStatus();
            }
        } else if (gameEstatus == UIStates.PLAYING_ARROW) {
            Point to = convertScreenToBoard(me.getX(), me.getY());
            if (to != null && status.getPos(to) == CellType.EMPTY) {
                status.placeArrow(to);
                
                boardPanel.repaint();
                endTurn();
            }
        }
    }

    protected void paintCross(Graphics2D g2, int x, int y, int radius) {

        int crossColor = 0xFFFF0000;
        g2.setColor(new Color(crossColor, true));
        radius *= 0.7;
        g2.drawLine(x - radius, y - radius, x + radius, y + radius);
        g2.drawLine(x - radius, y + radius, x + radius, y - radius);

    }

    protected void paintQueen(Graphics2D g2, boolean isWhiteQueen, int x, int y, int radius) {
        boolean isWhite = true;
        int queenRadius = (int) (radius * 0.85);
        int x1 = x - queenRadius;
        int y1 = y - queenRadius;
        x -= radius;
        y -= radius;
        int size = radius * 2;

        // Retains the previous state
        Paint oldPaint = g2.getPaint();

        // Fills the circle with solid blue color
        //g2.setColor(new Color(0x0153CC));
        int backColor = isWhite ? 0xFFFFFFFF : 0xFF333333;
        g2.setColor(new Color(backColor, true));
        g2.fillOval(x, y, size - 1, size - 1);
        g2.setColor(new Color(0x000000, true));
        g2.drawOval(x, y, size - 1, size - 1);

        // Adds shadows at the top
        Paint p;
        p = new GradientPaint(x, y, new Color(0.0f, 0.0f, 0.0f, 0.4f),
                x, y + size, new Color(0.0f, 0.0f, 0.0f, 0.0f));
        g2.setPaint(p);
        g2.fillOval(x, y, size - 1, size - 1);

        // Adds highlights at the bottom 
        {
            //Color i =isWhite? new Color(1.0f, 1.0f, 1.0f, 0.0f);
            //Color f = new Color(1.0f, 1.0f, 1.0f, 0.4f); 
            Color i = isWhite ? new Color(160, 160, 160, 127) : new Color(1.0f, 1.0f, 1.0f, 0.0f);
            Color f = isWhite ? new Color(0.0f, 0.0f, 0.0f, 0.1f) : new Color(1.0f, 1.0f, 1.0f, 0.4f);

            p = new GradientPaint(x, y, i,
                    x, y + size, f);
            g2.setPaint(p);
            g2.fillOval(x, y, size - 1, size - 1);
        }
        // Creates dark edges for 3D effect
        //Color i = new Color(6, 76, 160, 127);
        //Color f = new Color(0.0f, 0.0f, 0.0f, 0.8f); 
        {
            Color i = isWhite ? new Color(250, 250, 250, 127) : new Color(6, 76, 160, 127);
            Color f = isWhite ? new Color(0.0f, 0.0f, 0.0f, 0.2f) : new Color(0.0f, 0.0f, 0.0f, 0.8f);
            p = new RadialGradientPaint(new Point2D.Double(x + size / 2.0,
                    y + size / 2.0), size / 2.0f,
                    new float[]{0.0f, 1.0f},
                    new Color[]{i,
                        f});
            g2.setPaint(p);
            g2.fillOval(x, y, size - 1, size - 1);
        }

        g2.drawImage(isWhiteQueen ? image_QW : image_QB, x1, y1, queenRadius * 2, queenRadius * 2, null);

        // Adds oval specular highlight at the top left
        p = new RadialGradientPaint(new Point2D.Double(x + size / 2.0,
                y + size / 2.0), size / 1.4f,
                new Point2D.Double(45.0, 25.0),
                new float[]{0.0f, 0.5f},
                new Color[]{new Color(1.0f, 1.0f, 1.0f, 0.4f),
                    new Color(1.0f, 1.0f, 1.0f, 0.0f)},
                RadialGradientPaint.CycleMethod.NO_CYCLE);
        g2.setPaint(p);
        g2.fillOval(x, y, size - 1, size - 1);

        // Restores the previous state
        g2.setPaint(oldPaint);

    }

}

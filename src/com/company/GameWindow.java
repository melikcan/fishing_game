package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GameWindow extends JFrame implements MouseListener, MouseMotionListener {

    private int score = 0;
    private int high_score = 0;
    private final int FPS = 60;
    private final int MAX_FISH = 16;
    private final int BOAT_Y = 130;
    private int time = 120;
    private int mouseX, mouseY;
    private int ropeLength;
    private int pressed;
    private int count;
    private boolean hookFull;
    private boolean clicked;

    private final JButton START_BUTTON = new JButton();
    private JLabel timeLabel, scoreLabel;
    private Font font;
    private BufferedImage hookImage, seaweed, layer1, layer2, restart, restart_hover, restart_background;

    private Fish[] fishArr;
    private Fisherman boat;

    /**
     * Constructor
     */
    public GameWindow() {

        /** Main Window */
        super("Fishing Game");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setContentPane(new JLabel(new ImageIcon("images/start_screen.png")));

        /** Score text and time */
        font = new Font("Segoe Print", Font.PLAIN, 30);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBounds(0, -10, 790, 60);
        add(panel);

        scoreLabel = new JLabel("Score : " + score);
        panel.add(scoreLabel, BorderLayout.EAST);
        scoreLabel.setFont(font);

        timeLabel = new JLabel("Time Left: " + time);
        panel.add(timeLabel, BorderLayout.WEST);
        timeLabel.setFont(font);

        /** Start Button  */
        add(START_BUTTON);
        START_BUTTON.setContentAreaFilled(false);
        START_BUTTON.setBorderPainted(false);
        START_BUTTON.setFocusPainted(false);
        START_BUTTON.setBounds(204, 300, 392, 90);
        START_BUTTON.setIcon(new ImageIcon("images/start_now_1.png"));
        START_BUTTON.setRolloverIcon(new ImageIcon("images/start_now_2.png"));
        START_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startGame();
            }
        });

        try {
            layer1 = ImageIO.read(new File("images/layer1.png"));
            layer2 = ImageIO.read(new File("images/layer2.png"));
            hookImage = ImageIO.read(new File("images/hook.png"));
            seaweed = ImageIO.read(new File("images/seaweed.png"));
            restart = ImageIO.read(new File("images/restart.png"));
            restart_hover = ImageIO.read(new File("images/restart2.png"));
            restart_background = ImageIO.read(new File("images/restart3.png"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot find the file.", "IOException", JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Custom drawing panel for objects (inner class)
     */
    class DrawCanvas extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(layer1, 0, 0, this); //draw background
            boat.draw(g); // draw fisherman and his boat
            g.drawImage(layer2, 0, 0, this); //draw background layer
            g.drawLine(boat.getRodX(), BOAT_Y, boat.getRodX(), BOAT_Y + ropeLength); //draw the string
            g.drawImage(hookImage, boat.getRodX() - 5, BOAT_Y + ropeLength, this); //draw the hook

            for (Fish fish : fishArr) {
                if (fish != null)
                    fish.draw(g);
            }

            g.drawImage(seaweed, 525, 300, this); //draw seaweed

            //draw restart button
            if (time == 0) {
                g.setFont(font);
                g.drawImage(restart_background, 150, 260, this);
                g.drawString("Highest score: " + high_score, 170, 300);

                if (mouseX < 596 && mouseX > 204 && mouseY > 330 && mouseY < 400) {
                    g.drawImage(restart_hover, 204, 300, this);
                } else {
                    g.drawImage(restart, 204, 300, this);
                }
            }
        }
    }

    public void startGame() {

        /** Switch to game screen */
        START_BUTTON.setVisible(false);
        remove(START_BUTTON);

        DrawCanvas canvas = new DrawCanvas();
        this.setLayout(new BorderLayout());
        this.add(canvas, BorderLayout.CENTER);

        addMouseListener(this);
        addMouseMotionListener(this);

        fishArr = new Fish[MAX_FISH];
        boat = new Fisherman(350, BOAT_Y);

        /** Start the fisherman and fish moving */
        final Thread gameThread = new Thread() {
            public void run() {
                count = 0;
                while (true) {

                    gameUpdate();
                    repaint();

                    /** Stop the game if time is up */
                    if (time == 0) {
                        gameOver();
                    } else if (time == 121) {
                        time = 120;
                        return;
                    }

                    try {
                        Thread.sleep(1000 / FPS);
                    } catch (InterruptedException ex) {
                        //
                    }
                }
            }
        };
        gameThread.start();
    }

    void gameUpdate() {
        /** Update the timer text */
        count += 1;
        if (count % 60 == 0 && time != 0) {
            time -= 1;
            timeLabel.setText("Time Left: " + time);
        }

        /** Generate fish */
        for (int i = 0; i < MAX_FISH; i++) {
            if ((fishArr[i] == null || fishArr[i].outBoundary()) && ((int) (Math.random() * 1337)) == 1) {
                fishArr[i] = new Fish();
            }
        }

        /** Move the fish for each game tick */
        for (Fish fish : fishArr) {
            if (fish != null) {
                fish.moveOneStep(boat.getRodX(), BOAT_Y + ropeLength);
                if (!hookFull && time > 0) fish.setCaught(boat.getRodX(), BOAT_Y + ropeLength);
                if (fish.isCaught) hookFull = true;
            }
        }

        /** Update boat and ropeLength with mouse behaviour */
        if (ropeLength < 5) ropeLength = 5;
        if (ropeLength > 520) ropeLength = 520;
        ropeLength += pressed;
        boat.setX(mouseX - 77);

        /** Delete the caught fish and add its points to the scoreboard */
        for (int i = 0; i < MAX_FISH; i++) {
            if (fishArr[i] != null && fishArr[i].y < 200) {
                if (time > 0) score += fishArr[i].points;
                fishArr[i] = null;
                hookFull = false;
            }
        }
        scoreLabel.setText("Score : " + score);
    }

    /**
     * Reset the variables and prepare the game for restarting
     */
    void gameOver() {
        pressed = -4;
        if (score > high_score) high_score = score;
        if (mouseX < 596 && mouseX > 204 && mouseY > 330 && mouseY < 400 && clicked) {
            time = 121; //terminate the thread before starting a new one
            score = 0;
            hookFull = false;
            startGame();
        }
        clicked = false;
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        mouseX = me.getX();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        mouseX = me.getX();
        mouseY = me.getY();
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (count > 7260) clicked = true;
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (time > 0) pressed = 4;
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        pressed = -4;
    }
}

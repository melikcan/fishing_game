package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Fisherman extends JPanel {

    private int x, y;
    private BufferedImage boatImage;
    private static final int WIDTH = 160;

    /**
     * Constructor
     */
    public Fisherman(int x, int y) {

        this.x = x;
        this.y = y;

        try {
            boatImage = ImageIO.read(new File("images/fisherman.png"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot find the boat.", "IOException", JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Setter for x value
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * X-coordinate for fishing rod's tip
     */
    public int getRodX() {
        return x + WIDTH;
    }

    /**
     * Draw itself using the given graphic context
     */
    public void draw(Graphics g) {
        g.drawImage(boatImage, x, y, this);
    }
}

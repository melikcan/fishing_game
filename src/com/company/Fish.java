package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Fish extends JPanel {

    public float x, y;
    private float speedX, speedY;
    private int size, type;
    public int points;
    public boolean isCaught;

    private BufferedImage fish1, fish2, fish3, fish4, fish5, fish6, fish7;

    /**
     * Constructor
     */
    public Fish() {

        x = 0;
        y = (float) (Math.random() * 550 + 200);
        speedX = (float) (Math.random() * 2 + 1);
        speedY = (float) (Math.random() * 2 - 1);

        type = generateType();
        size = setSize(type);
        points = getPoints(type);
        isCaught = false;

        /** Generate some of the fish from the right side of the window */
        if ((int) y % 2 == 0) {
            x = 850;
            speedX = -speedX;
        }

        /** Read fish images from the file to access later */
        try {
            fish1 = ImageIO.read(new File("images/fish1.png"));
            fish2 = ImageIO.read(new File("images/fish2.png"));
            fish3 = ImageIO.read(new File("images/fish3.png"));
            fish4 = ImageIO.read(new File("images/fish4.png"));
            fish5 = ImageIO.read(new File("images/fish5.png"));
            fish6 = ImageIO.read(new File("images/fish6.png"));
            fish7 = ImageIO.read(new File("images/fish7.png"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot find the fish.", "IOException", JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Generate a new type by random
     */
    private int generateType() {

        int i = (int) (Math.random() * 16);

        switch (i) {
            case 4:
            case 5:
            case 6:
                return 2; //normal fish
            case 7:
                return 3; //rare fish 1
            case 8:
                return 4; //rare fish 2
            case 9:
                return 5; //junk 1
            case 10:
                return 6; //junk 2
            case 11:
                return 7; //junk 3
            default:
                return 1; //common fish
        }
    }

    /**
     * Get related fish image for the given type
     */
    private BufferedImage getImage(int type) {

        switch (type) {
            case 2:
                return fish2;
            case 3:
                return fish3;
            case 4:
                return fish4;
            case 5:
                return fish5;
            case 6:
                return fish6;
            case 7:
                return fish7;
            default:
                return fish1;
        }
    }

    /**
     * Decide on points for each fish type
     */
    private int getPoints(int type) {

        switch (type) {
            case 2:
                return 200; //normal fish
            case 3:
                return 400; //rare fish
            case 4:
                return 600; //rare fish
            case 5:
            case 6:
            case 7:
                return -500; //garbage
            default:
                return 100; //common fish
        }
    }

    /**
     * Get proper image size according to the fish type
     */
    private int setSize(int type) {
        switch (type) {
            case 2:
                return 65; //normal fish
            case 3:
                return 55; //rare fish: green
            case 4:
                return 40; //rare fish: purple
            case 5:
                return 65; //garbage: boot
            case 6:
                return 35; //garbage: can
            case 7:
                return 65; //garbage: tire
            default:
                return 50; //common fish
        }
    }

    /**
     * Check if fish and hook intersects
     */
    public void setCaught(int hookX, int hookY) {
        if (hookX > x - size / 1.2 && hookX < x) {
            if (hookY > y - size && hookY < y - size / 1.2) {
                isCaught = true;
            }
        }
    }

    /**
     * Check if fish are out of the boundaries
     */
    public boolean outBoundary() {
        return (x > 850 || x < -50);
    }

    /**
     * Draw itself using the given graphic context
     */
    public void draw(Graphics g) {

        if (speedX > 0)
            g.drawImage(getImage(type), (int) (x - size), (int) (y - size), this);

        else {
            /** Flip the fish image horizontally if the fish is moving to the left */
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-getImage(type).getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            g.drawImage(op.filter(getImage(type), null), (int) (x - size), (int) (y - size), this);
        }
    }

    /**
     * Make one move, checking the upper and down boundaries
     */
    public void moveOneStep(int hookX, int hookY) {
        /** Cause them to change direction if the hook is close */
        if ((type < 5) && (hookX > x - 80 && hookX < x + 60) && (y > hookY - 30 && y < hookY + 100)) {
            if (((int) (Math.random() * (48 / type))) == 1 && !isCaught) {
                speedX = -speedX;
                speedY = -speedY;
            }
        }

        /** Cause them to change direction randomly */
        if (type < 5 && ((int) (Math.random() * (2400 / type))) == 1 && !isCaught) speedX = -speedX;
        if (type < 5 && ((int) (Math.random() * (1200 / type))) == 1 && !isCaught) speedY = -speedY;

        /** Move the caught fish with the hook */
        if (isCaught) {
            x = hookX + size / 2;
            y = hookY + size;
        }

        /** Move the fish accordingly to its speed if nothing special happens */
        else {
            x += speedX;
            y += speedY;

            if (y < 210 + size) {
                speedY = -speedY;
                y = 210 + size;
            } else if (y > 660 - size) {
                speedY = -speedY;
                y = 660 - size;
            }
        }
    }
}

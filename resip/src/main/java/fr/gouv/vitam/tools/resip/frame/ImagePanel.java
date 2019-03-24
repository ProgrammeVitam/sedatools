package fr.gouv.vitam.tools.resip.frame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class ImagePanel extends JPanel implements Serializable {
    Image image = null;
    public ImagePanel(Image image) {
        this.image = image;
    }
    public ImagePanel() {
    }
    public void setImage(Image image){
        this.image = image;
    }
    public Image getImage(Image image){
        return image;
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background
        if (image != null) { //there is a picture: draw it
            int height = this.getSize().height;
            int width = this.getSize().width;
            //g.drawImage(image, 0, 0, this); //use image size
            g.drawImage(image,0,0, width, height, this);
        }
    }
}
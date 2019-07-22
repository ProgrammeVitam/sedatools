package fr.gouv.vitam.tools.resip.frame;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

/**
 * The type Image panel.
 */
public class ImagePanel extends JPanel implements Serializable {
    /**
     * The Image.
     */
    Image image = null;

    /**
     * Instantiates a new Image panel.
     *
     * @param image the image
     */
    public ImagePanel(Image image) {
        this.image = image;
    }

    /**
     * Instantiates a new Image panel.
     */
    public ImagePanel() {
    }

    /**
     * Set image.
     *
     * @param image the image
     */
    public void setImage(Image image){
        this.image = image;
    }

    /**
     * Get image image.
     *
     * @param image the image
     * @return the image
     */
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
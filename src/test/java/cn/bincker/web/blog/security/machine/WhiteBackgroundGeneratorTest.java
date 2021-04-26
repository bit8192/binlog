package cn.bincker.web.blog.security.machine;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static org.junit.jupiter.api.Assertions.*;

class WhiteBackgroundGeneratorTest {

    @Test
    void generator() {
        int width = 1960;
        int height = 1080;
        JFrame jFrame = new JFrame("img");

        jFrame.setSize(width, height);

        JLabel label = new JLabel();
        jFrame.add(BorderLayout.CENTER, label);
        label.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                Image image = new ApiBackgroundGenerator().generator(width, height);
                label.setIcon(new ImageIcon(image));
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        jFrame.setVisible(true);

        Image image = new ApiBackgroundGenerator().generator(width, height);
        label.setIcon(new ImageIcon(image));

        while (jFrame.isVisible()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
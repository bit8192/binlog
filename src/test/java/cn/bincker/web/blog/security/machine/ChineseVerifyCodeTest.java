package cn.bincker.web.blog.security.machine;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static org.junit.jupiter.api.Assertions.*;

class ChineseVerifyCodeTest {

    @Test
    void generate() {
        int width = 350;
        int height = 350;
        JFrame jFrame = new JFrame("test");
        jFrame.setSize(width, height);

        JLabel label = new JLabel();
        IBackgroundGenerator backgroundGenerator = new ApiBackgroundGenerator();
        label.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                IVerifyCode<?> verifyCode = new ChineseVerifyCode(width, height, 2, 4, 10, 90, 28, 20, new String[]{"宋体"}, backgroundGenerator);
                VerifyQuestion<?> verifyQuestion = verifyCode.generate();
                label.setIcon(new ImageIcon(verifyQuestion.getQuestion()));
            }
            @Override
            public void mousePressed(MouseEvent mouseEvent) {}
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {}
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}
            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        jFrame.add(BorderLayout.CENTER, label);
        jFrame.setVisible(true);

        IVerifyCode<?> verifyCode = new ChineseVerifyCode(width, height, 2, 4, 10, 90, 28, 20, new String[]{"宋体"}, backgroundGenerator);
        VerifyQuestion<?> verifyQuestion = verifyCode.generate();
        label.setIcon(new ImageIcon(verifyQuestion.getQuestion()));

        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (jFrame.isVisible());
    }

    @Test
    void verify() {
    }
}
package cn.bincker.web.blog.security.machine;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChineseVerifyCode implements IVerifyCode<ChineseVerifyCode.ChineseVerifyCodeAnswer>{
    private static final Logger log = LoggerFactory.getLogger(ChineseVerifyCode.class);
    public static final String TITLE = "请按顺序在图像中点击：";
    private final int width;
    private final int height;
    private final int padding;
    private final int rotateLimit;
    private final int fontSize;
    private final int titleFontSize;
    private final IBackgroundGenerator backgroundGenerator;
    private final Random random;
    private final int minCharNumber;
    private final int maxCharNumber;
    private final Font[] fonts;

    public ChineseVerifyCode(int width, int height, int minCharNumber, int maxCharNumber, int padding, int rotateLimit, int fontSize, int titleFontSize, String[] fontNames, IBackgroundGenerator backgroundGenerator) {
        Assert.isTrue(width - padding*2 - fontSize * maxCharNumber > 0, "图像太窄或字体太大");
        this.width = width;
        this.height = height;
        this.minCharNumber = minCharNumber;
        this.maxCharNumber = maxCharNumber;
        this.padding = padding;
        this.rotateLimit = rotateLimit;
        this.fontSize = fontSize;
        this.titleFontSize = titleFontSize;
        this.backgroundGenerator = backgroundGenerator;
        this.random = new Random();
        this.fonts = new Font[fontNames.length];
        for (int i = 0, fontNamesLength = fontNames.length; i < fontNamesLength; i++) {
            String fontName = fontNames[i];
            this.fonts[i] = new Font(fontName, Font.PLAIN | Font.BOLD, fontSize);
        }
    }

    @Override
    public VerifyQuestion<ChineseVerifyCodeAnswer> generate() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(backgroundGenerator.generator(width, height), 0, 0, null);

        List<String> stringList = getVerifyStrings();
        List<Pair<String,Point>> pointList = new ArrayList<>(maxCharNumber);
        for (String str : stringList) {
            Point point;
            while (true) {
                point = getRandomPoint();
                boolean flag = false;
                for (Pair<String, Point> pair : pointList) {
                    if (pair.getSecond().distance(point) < fontSize * 2) {//两个字之间的距离要有两个字的宽度
                        flag = true;
                        break;
                    }
                }
                if (!flag) break;
            }
            pointList.add(Pair.of(str, point));
            drawRandomChar(point, str, fonts[random.nextInt(fonts.length)], graphics, image);
        }

        Font titleFont = new Font(this.fonts[random.nextInt(this.fonts.length)].getFontName(), Font.PLAIN, titleFontSize);

        //画标题底色
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .5f));
        graphics.setColor(Color.WHITE);
        graphics.fill(new RoundRectangle2D.Float(padding/2f, padding, graphics.getFontMetrics(titleFont).stringWidth(TITLE), titleFontSize + padding  / 2f, 5, 5));

        //画标题
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .5f));
        graphics.setColor(Color.BLACK);
        graphics.setFont(titleFont);
        graphics.drawString(TITLE, padding, padding + titleFontSize);
        int textStartX = padding + graphics.getFontMetrics().stringWidth(TITLE);

        textStartX += (width - textStartX - titleFontSize*pointList.size())/4 * random.nextDouble();
        for (int i = 0; i < pointList.size(); i++) {
            Pair<String, Point> item = pointList.get(i);
            drawRandomChar(new Point(textStartX + i * titleFontSize, padding + titleFontSize/2), item.getFirst(), titleFont, graphics, image);
            textStartX += (width - textStartX - titleFontSize*(pointList.size() - i))/4 * random.nextDouble();
        }

        return new VerifyQuestion<>(new ChineseVerifyCodeAnswer(pointList), image);
    }

    /**
     * 获取验证字符列表
     */
    private List<String> getVerifyStrings() {
        List<String> result = new ArrayList<>(maxCharNumber);
        int number = minCharNumber + random.nextInt(maxCharNumber - minCharNumber + 1);
        for (int i = 0; i < number; i++) {
            result.add(String.valueOf(getRandomSimpleChineseChar()));
        }
        return result;
    }

    /**
     * 画随机中文到随机位置
     */
    private void drawRandomChar(Point point, String str, Font font, Graphics2D graphics, BufferedImage image){
        //设置画笔
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .9f));
        graphics.setFont(font);

        //计算随机角度
        double rotate = (rotateLimit % 180 * 1.0)/180*random.nextDouble()*Math.PI*(random.nextDouble()>0.5?-1:1);
        //计算文字宽度
        int width = graphics.getFontMetrics().charWidth(str.charAt(0));

        //计算绘制文字原点
        Point printPoint = new Point(point.x - width / 2, point.y + font.getSize() / 2);

        /*
        //画文字绘制原点
        graphics.setColor(Color.RED);
        graphics.drawLine(printPoint.x - 10, printPoint.y, printPoint.x + 10, printPoint.y);
        graphics.drawLine(printPoint.x, printPoint.y - 10, printPoint.x, printPoint.y + 10);

        //画文字中心
        graphics.setColor(Color.RED);
        graphics.drawLine(point.x - 10, point.y, point.x + 10, point.y);
        graphics.drawLine(point.x, point.y - 10, point.x, point.y + 10);
        */

        //计算平均色
        Color avgColor = getAvgColor(image, point, font.getSize());
        int colorTotal = avgColor.getRed() + avgColor.getGreen() + avgColor.getBlue();
        int randomAddColor = (int) (50 + 20 * random.nextDouble());
        if(colorTotal > 768 - colorTotal){
            graphics.setColor(new Color(
                            Math.abs(avgColor.getRed() - randomAddColor),
                            Math.abs(avgColor.getGreen() - randomAddColor),
                            Math.abs(avgColor.getBlue() - randomAddColor)
                    )
            );
        }else{
            graphics.setColor(new Color(
                    (avgColor.getRed() + randomAddColor) % 256,
                    (avgColor.getGreen() + randomAddColor) % 256,
                    (avgColor.getBlue() + randomAddColor) % 256
            ));
        }

        graphics.rotate(rotate, point.x, point.y);
        graphics.drawString(str, printPoint.x, printPoint.y);
        graphics.rotate(-rotate, point.x, point.y);
    }

    /**
     * 取平均颜色值
     */
    private Color getAvgColor(BufferedImage image, Point point, int range) {
        int r = 0,g = 0,b = 0;
        int startX = point.x - range / 2;
        int startY = point.y - range / 2;
        try {
            for (int y = 0; y < range; y++) {
                for (int x = 0; x < range; x++) {
                    int rgb = image.getRGB(startX + x, startY + y);
                    r += rgb >> 16 & 0xff;
                    g += rgb >> 8 & 0xff;
                    b += rgb & 0xff;
                }
            }
        }catch (ArrayIndexOutOfBoundsException e){
            log.error(
                    "计算平均色值超出范围，请调整字体大小和边距 point.x=" + point.x +
                            "\tpoint.y=" + point.y +
                            "\tstartX=" + startX +
                            "\tstartY=" + startY +
                            "\trange=" + range +
                            "\timgWidth=" + image.getWidth() +
                            "\timgHeight=" + image.getHeight(),
                    e
            );
        }
        int size = range * range;
        return new Color(r / size, g / size, b / size);
    }

    /**
     * 随机取一个点(计算了padding和文字大小限制)，这里文字原点在文字中间，要考虑说明文字空间
     */
    private Point getRandomPoint(){
        return new Point(
                (int) (padding + fontSize / 2 + (width - padding*2 - fontSize) * random.nextDouble()),
                (int) (padding + titleFontSize + padding + fontSize / 2 + (height - padding*3 - fontSize - titleFontSize) * random.nextDouble())
        );
    }

    /**
     * 随机中文字符
     */
    private char getRandomSimpleChineseChar(){
        return (char)(0x4e00 + ((char) ((0x9fa5 - 0x4e00) * random.nextDouble())));
    }

    @Override
    public boolean verify(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        if(httpSession.isNew()) return false;
        VerifyQuestion<?> question = (VerifyQuestion<?>) httpSession.getAttribute(SESSION_ATTRIBUTE_ANSWER);
        if(question == null) return false;
        ChineseVerifyCodeAnswer answer = (ChineseVerifyCodeAnswer) question.getAnswer();
        if(answer == null) return false;
        for (int i = 0, pointsSize = answer.points.size(); i < pointsSize; i++) {
            Pair<String, Point> point = answer.points.get(i);
            String pointParam = request.getParameter("point" + i);
            if(!StringUtils.hasText(pointParam)) return false;
            String[] pointParamSplitArr = pointParam.split(",");
            if(pointParamSplitArr.length < 2) return false;
            if(point.getSecond().distance(new Point(Integer.parseInt(pointParamSplitArr[0]), Integer.parseInt(pointParamSplitArr[1]))) > fontSize / 2f){
                log.debug(
                        "验证码验证错误：\tindex=" + i +
                                "\ttext=" + point.getFirst() +
                                "\tanswerX,paramX=" + point.getSecond().x + "," + pointParamSplitArr[0] +
                                "\tanswerY,paramY=" + point.getSecond().y + "," + pointParamSplitArr[1]
                );
                return false;
            }
        }
        return true;
    }

    /**
     * 验证码答案
     */
    @Data
    public static class ChineseVerifyCodeAnswer implements Serializable {
        private List<Pair<String,Point>> points;

        public ChineseVerifyCodeAnswer(List<Pair<String, Point>> points) {
            this.points = points;
        }
    }
}

package com.pbad.auth.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * 验证码工具类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public class CaptchaUtil {

    /**
     * 验证码字符集（排除容易混淆的字符：0, O, 1, I, l）
     */
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 4;

    /**
     * 图片宽度
     */
    private static final int IMAGE_WIDTH = 140;

    /**
     * 图片高度
     */
    private static final int IMAGE_HEIGHT = 50;

    /**
     * 生成验证码图片和验证码值
     *
     * @return CaptchaResult 包含验证码值和Base64编码的图片
     */
    public static CaptchaResult generateCaptcha() {
        // 生成随机验证码
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        String codeValue = code.toString();

        // 创建图片
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 设置背景色（浅灰色）
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 绘制干扰线
        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(IMAGE_WIDTH);
            int y1 = random.nextInt(IMAGE_HEIGHT);
            int x2 = random.nextInt(IMAGE_WIDTH);
            int y2 = random.nextInt(IMAGE_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码字符
        Font font = new Font("Arial", Font.BOLD, 32);
        g.setFont(font);
        
        // 获取字体度量，用于准确计算字符位置
        FontMetrics fm = g.getFontMetrics();
        int fontHeight = fm.getHeight();
        int fontAscent = fm.getAscent();
        
        // 计算字符间距，留出左右边距
        int padding = 10;
        int availableWidth = IMAGE_WIDTH - 2 * padding;
        int charSpacing = availableWidth / CODE_LENGTH;
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            // 随机颜色
            Color color = new Color(
                    random.nextInt(100) + 50,
                    random.nextInt(100) + 50,
                    random.nextInt(100) + 50
            );
            g.setColor(color);

            // 计算字符位置，确保字符完整显示
            int charWidth = fm.charWidth(codeValue.charAt(i));
            int x = padding + charSpacing * i + (charSpacing - charWidth) / 2;
            // y坐标使用基线位置，确保字符不被裁剪
            int y = (IMAGE_HEIGHT + fontAscent) / 2 + random.nextInt(6) - 3;
            double angle = (random.nextDouble() - 0.5) * 0.25; // -14度到14度

            // 旋转绘制
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(codeValue.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }

        // 绘制干扰点
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(IMAGE_WIDTH);
            int y = random.nextInt(IMAGE_HEIGHT);
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillOval(x, y, 2, 2);
        }

        g.dispose();

        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return new CaptchaResult(codeValue, base64Image);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private final String code;
        private final String imageBase64;

        public CaptchaResult(String code, String imageBase64) {
            this.code = code;
            this.imageBase64 = imageBase64;
        }

        public String getCode() {
            return code;
        }

        public String getImageBase64() {
            return imageBase64;
        }
    }
}


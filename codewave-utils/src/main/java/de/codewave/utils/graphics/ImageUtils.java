package de.codewave.utils.graphics;

import de.codewave.utils.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * Utility classes for images.
 */
public class ImageUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * Create an image from a byte array with the image data and a mime type.
     *
     * @param data
     *
     * @return Image created from the byte array or <code>null</code> if the byte array could not be converted into an image.
     */
    private static BufferedImage createImage(byte[] data) {
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(data);
            return ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read image from byte array.", e);
            }
        } finally {
            IOUtils.close(byteArrayInputStream);
        }
        return null;
    }

    /**
     * Get image data from an image using the specified type.
     *
     * @param image Image.
     * @param type  Type.
     *
     * @return Image data.
     */
    private static byte[] getImageData(BufferedImage image, String type) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get byte array from image.", e);
            }
        } finally {
            IOUtils.close(outputStream);
        }
        return null;
    }

    /**
     * Resize an image to the specified maximum size, i.e. neither the width nor the height will be more than the specified maximum size.
     *
     * @param data    Image data.
     * @param maxSize Maximum size.
     *
     * @return Resized image or original image if neither width nor height are greater than the maximum size.
     */
    public static byte[] resizeImageWithMaxSize(byte[] data, int maxSize) {
        BufferedImage image = createImage(data);
        if (image != null) {
            int targetWidth = image.getWidth();
            int targetHeight = image.getHeight();
            if (image.getWidth() >= image.getHeight()) {
                if (image.getWidth() == maxSize) {
                    return data; // image already has perfect size
                }
                targetHeight = (int)((double)targetHeight * (double)maxSize / (double)image.getWidth());
                targetWidth = maxSize;
            } else if (image.getHeight() >= image.getWidth()) {
                if (image.getHeight() == maxSize) {
                    return data; // image already has perfect size
                }
                targetWidth = (int)((double)targetWidth * (double)maxSize / (double)image.getHeight());
                targetHeight = maxSize;
            }
            Graphics2D graphics = image.createGraphics();
            BufferedImage resizedImage = graphics.getDeviceConfiguration().createCompatibleImage(targetWidth,
                                                                                                           targetHeight,
                                                                                                           image.getColorModel().getTransparency());
            graphics.dispose();
            graphics = resizedImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, 0, 0, image.getWidth(), image.getHeight(), null);
            graphics.dispose();
            return getImageData(resizedImage, "jpeg");
        }
        return null;
    }

    /**
     * Get the maximum size of an image (i.e. either width or height, depending on which one is greater).
     *
     * @param data    Image data.
     *
     * @return Maximum size of the image.
     */
    public static int getMaxSize(byte[] data) {
        BufferedImage image = createImage(data);
        if (image != null) {
            return Math.max(image.getWidth(), image.getHeight());
        }
        return 0;
    }
}
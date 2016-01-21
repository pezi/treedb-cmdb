/*
* (C) Copyright 2014-2016 Peter Sauer (http://treedb.at/).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package at.treedb.ci;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.mortennobel.imagescaling.MultiStepRescaleOp;

/**
 * <p>
 * Image manipulation class
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class ImageManipulation {
    /**
     * Helper class for a rotated image.
     *
     */
    static public class RotatedImage {
        private byte[] data; // binary image data
        private int width; // image width
        private int height; // image height

        /**
         * Creates a {@code RotatedImage) object.
         * 
         * @param data
         *            binary image data
         * @param width
         *            image width
         * @param height
         *            image height
         */
        public RotatedImage(byte data[], int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        /**
         * Returns the image width.
         * 
         * @return image width
         */
        public int getWidth() {
            return width;
        }

        /**
         * Returns the image height.
         * 
         * @return image height
         */
        public int getHeight() {
            return height;
        }

        /**
         * Return the binary image data.
         * 
         * @return image data
         */
        public byte[] getData() {
            return data;
        }

    }

    /**
     * Rotates a (big) {@code BufferedImage} object.
     * 
     * @param degree
     *            angle of rotation in degree
     * @return rotated {@code BufferedImage}
     * 
     *         <p>
     *         <b>Hint:</b> Rotating big <code>BufferedImage</code> per affine
     *         transformation throws a
     *         <code>java.awt.image.ImagingOpException: Unable to transform src image</code>
     *         exception. Some hints can be found according this problem in the
     *         Internet, but the reason for this problem is not really clear.
     *         Therefore this alternative code is used for image rotation.
     *         </p>
     */
    public static BufferedImage rotateBigImage(BufferedImage image, int degree) {
        double angle = Math.toRadians(degree);
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int w = image.getWidth();
        int h = image.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin);
        int newh = (int) Math.floor(h * cos + w * sin);
        BufferedImage result = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.translate((neww - w) / 2, (newh - h) / 2);
        g.rotate(angle, w / 2, h / 2);
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }

    /**
     * Rotates an image according its EXIF orientation information (auto
     * rotate).
     * 
     * @param image
     *            {@code CIimage}
     * @return {@code RotatedImage} object
     * @throws IOException
     */
    public static RotatedImage rotateImage(CIimage image) throws IOException {
        if (!image.isAutoRotate() || image.getOrientation() == ImageOrientation.TOP) {
            return new RotatedImage(image.getData(), image.getWidth(), image.getHeight());
        }
        int width;
        int height;
        BufferedImage bsrc = ImageIO.read(new ByteArrayInputStream(image.getData()));
        width = image.getWidth();
        height = image.getHeight();
        int tmp;
        AffineTransform t = new AffineTransform();
        int angle = 0;

        switch (image.getOrientation().getOrientation()) {

        case 3:
            angle = 180;
            t.translate(width, height);
            t.quadrantRotate(2);
            break;
        case 6:
            angle = 90;
            t.translate(height, 0);
            t.quadrantRotate(1);
            // swap width/height
            tmp = width;
            width = height;
            width = tmp;
            break;
        case 8:
            angle = 270;
            t.translate(0, width);
            t.quadrantRotate(3);
            // swap width/height
            tmp = width;
            width = height;
            width = tmp;
            break;
        }

        AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bdest = op.filter(bsrc, bdest);

        // BufferedImage bdest = rotateBigImage(bsrc, angle);

        ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
        ImageIO.write(bdest, image.getMimeType().getExtension(), imagebuffer);
        return new RotatedImage(imagebuffer.toByteArray(), width, height);
    }

    /**
     * <p>
     * Rotates a {@code BufferedImage} object according its EXIF orientation.
     * </p>
     * 
     * @param image
     *            {@code CIimage} containing the EXIF orientation
     * @param bsrc
     *            {@code BufferedImage} object
     * @return {@code BufferedImage} object
     * @throws IOException
     */
    public static BufferedImage rotateBufferedImage(CIimage image, BufferedImage bsrc) throws IOException {
        if (!image.isAutoRotate() || image.getOrientation() == ImageOrientation.TOP) {
            return bsrc;
        }
        int width = bsrc.getWidth();
        int height = bsrc.getHeight();

        AffineTransform t = new AffineTransform();
        int tmp;
        switch (image.getOrientation().getOrientation()) {

        case 3:
            t.translate(width, height);
            t.quadrantRotate(2);
            break;
        case 6:
            t.translate(height, 0);
            t.quadrantRotate(1);
            // swap width/height
            tmp = width;
            width = height;
            height = tmp;
            break;
        case 8:
            t.translate(0, width);
            t.quadrantRotate(3);
            // swap width/height
            tmp = width;
            width = height;
            height = tmp;
            break;
        }
        AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bdest = op.filter(bsrc, bdest);
        return bdest;
    }

    /**
     * 
     * @param sourceImage
     * @param width
     *            new width of
     * @param height
     * @return
     */
    public static BufferedImage resize(BufferedImage sourceImage, int width, int height) {
        MultiStepRescaleOp resizeOp = new MultiStepRescaleOp(width, height);
        return resizeOp.filter(sourceImage, null);
    }
}

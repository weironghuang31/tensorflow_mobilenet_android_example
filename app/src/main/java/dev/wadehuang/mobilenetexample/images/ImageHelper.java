package dev.wadehuang.mobilenetexample.images;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;

import java.nio.ByteBuffer;

public class ImageHelper {
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255;
    static final int kMaxChannelValue = 262143;

    public static float[] bitmapToFloat(Bitmap bitmap) {

        int[] intValues = new int[bitmap.getWidth() * bitmap.getHeight()];
        float[] result = new float[bitmap.getWidth() * bitmap.getHeight() * 3];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());


        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            result[i * 3 + 0] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            result[i * 3 + 1] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            result[i * 3 + 2] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
        }

        return result;
    }

    /**
     * Convert yuv 420 image to Bitmap, use the same Bitmap to save memory
     *
     * @param image
     * @return
     */
    public static void imageToBitmap(Image image, Bitmap bitmap) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int[] argb = new int[imageWidth * imageHeight];
        final Image.Plane[] planes = image.getPlanes();
        final byte[][] yuvBytes = new byte[3][];

        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }

        final int yRowStride = planes[0].getRowStride();
        final int uvRowStride = planes[1].getRowStride();
        final int uvPixelStride = planes[1].getPixelStride();

        convertYUV420ToARGB8888(
                yuvBytes[0],
                yuvBytes[1],
                yuvBytes[2],
                imageWidth,
                imageHeight,
                yRowStride,
                uvRowStride,
                uvPixelStride,
                argb);

        bitmap.setPixels(argb, 0, imageWidth, 0, 0, imageWidth, imageHeight);
    }

    private static void convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int[] out) {

        int i = 0;
        for (int y = 0; y < height; y++) {
            int pY = yRowStride * y;
            int pUV = uvRowStride * (y >> 1);

            for (int x = 0; x < width; x++) {
                int uv_offset = pUV + (x >> 1) * uvPixelStride;
                out[i++] =
                        YUV2RGB(
                                convertByteToInt(yData, pY + x),
                                convertByteToInt(uData, uv_offset),
                                convertByteToInt(vData, uv_offset));
            }
        }
    }

    private static int convertByteToInt(byte[] arr, int pos) {
        return arr[pos] & 0xFF;
    }

    private static int YUV2RGB(int nY, int nU, int nV) {
        nY -= 16;
        nU -= 128;
        nV -= 128;
        if (nY < 0) nY = 0;

        int nR = 1192 * nY + 1634 * nV;
        int nG = 1192 * nY - 833 * nV - 400 * nU;
        int nB = 1192 * nY + 2066 * nU;

        nR = Math.min(kMaxChannelValue, Math.max(0, nR));
        nG = Math.min(kMaxChannelValue, Math.max(0, nG));
        nB = Math.min(kMaxChannelValue, Math.max(0, nB));

        nR = (nR >> 10) & 0xff;
        nG = (nG >> 10) & 0xff;
        nB = (nB >> 10) & 0xff;

        return 0xff000000 | (nR << 16) | (nG << 8) | nB;
    }

}

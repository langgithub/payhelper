package com.lang.payhelper.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/8/12
 *     desc  : 图片相关工具类
 * </pre>
 */
public class ImageUtils {

    private ImageUtils() {
        throw new UnsupportedOperationException("u can't fuck me...");
    }


    /**
     * byteArr转bitmap
     *
     * @param bytes 字节数组
     * @return bitmap对象
     */
    public static Bitmap bytes2Bitmap(byte[] bytes) {
        return (bytes == null || bytes.length == 0) ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * drawable转bitmap
     *
     * @param drawable drawable对象
     * @return bitmap对象
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
    }

    /**
     * bitmap转drawable
     *
     * @param resources resources对象
     * @param bitmap    bitmap对象
     * @return drawable对象
     */
    public static Drawable bitmap2Drawable(Resources resources, Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(resources, bitmap);
    }


    /**
     * byteArr转drawable
     *
     * @param resources resources对象
     * @param bytes     字节数组
     * @return drawable对象
     */
    public static Drawable bytes2Drawable(Resources resources, byte[] bytes) {
        return bitmap2Drawable(resources, bytes2Bitmap(bytes));
    }

    /**
     * 根据文件路径获取bitmap
     *
     * @param file 文件路径
     * @return bitmap
     */
    public static Bitmap getBitmapByFile(File file) {
        if (file == null) return null;
        return getBitmapByFile(file.getPath());
    }

    /**
     * 根据文件路径获取bitmap
     *
     * @param filePath 文件路径
     * @return bitmap
     */
    public static Bitmap getBitmapByFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    /**
     * 根据文件路径获取bitmap
     *
     * @param res 资源对象
     * @param id  资源id
     * @return bitmap
     */
    public static Bitmap getBitmapByResource(Resources res, int id) {
        return BitmapFactory.decodeResource(res, id);
    }

    /**
     * @param filePath 文件路径
     * @return bitmap
     */
    public static Bitmap getBitmapByFile(String filePath, int reqWidth, int reqHeight) {
        if (TextUtils.isEmpty(filePath)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height >> 1;
            int halfWidth = width >> 1;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize <<= 1;
            }
        }
        return inSampleSize;
    }

    /**
     * 缩放图片
     *
     * @param src       源图片
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @return 缩放后的图片
     */
    public static Bitmap scaleImage(Bitmap src, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
    }

    /**
     * 缩放图片
     *
     * @param src         源图片
     * @param scaleWidth  缩放宽度比
     * @param scaleHeight 缩放高度比
     * @return 缩放后的图片
     */
    public static Bitmap scaleImage(Bitmap src, float scaleWidth, float scaleHeight) {
        if (src == null) return null;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap res = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (!src.isRecycled()) src.recycle();
        return res;
    }

    /**
     * 旋转图片
     *
     * @param src     源图片
     * @param degrees 旋转角度
     */
    public static Bitmap rotateBitmap(Bitmap src, int degrees) {
        if (src == null || degrees == 0) return src;
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, src.getWidth() / 2, src.getHeight() / 2);
        Bitmap res = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (!src.isRecycled()) src.recycle();
        return res;
    }

    /**
     * 获取图片旋转角度
     *
     * @param path 路径
     */
    public static int getRotateDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                default:
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 转为圆形图片
     *
     * @param src 源图片
     * @return 圆形图片
     */
    public static Bitmap toRound(Bitmap src) {
        if (src == null) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.TRANSPARENT);
        canvas.drawCircle(width / 2, height / 2, width / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rect, rect, paint);
        if (!src.isRecycled()) src.recycle();
        return output;
    }

    /**
     * 转为圆角图片
     *
     * @param src 源图片
     * @param ret 圆角的度数
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(Bitmap src, float ret) {
        if (null == src) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap out = Bitmap.createBitmap(width, height,
                Config.ARGB_8888);
        BitmapShader bitmapShader = new BitmapShader(src,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);
        RectF rectf = new RectF(0, 0, width, height);
        Canvas canvas = new Canvas(out);
        canvas.drawRoundRect(rectf, ret, ret, paint);
        canvas.save();
        canvas.restore();
        if (!src.isRecycled()) src.recycle();
        return out;
    }

    /**
     * 快速模糊
     * <p>先缩小原图，对小图进行模糊，再放大回原先尺寸</p>
     *
     * @param context 上下文
     * @param src     源图片
     * @param scale   缩小倍数
     * @param radius  模糊半径
     * @return 模糊后的图片
     */
    public static Bitmap fastBlur(Context context, Bitmap src, int scale, float radius) {
        if (isEmptyBitmap(src)) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        int scaleWidth = width / scale;
        int scaleHeight = height / scale;
        if (scaleWidth == 0 || scaleHeight == 0) return null;
        Bitmap scaled = Bitmap.createBitmap(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaled);
        canvas.scale(1 / (float) scale, 1 / (float) scale);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        PorterDuffColorFilter filter = new PorterDuffColorFilter(
                Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);
        if (!src.isRecycled()) src.recycle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            scaled = renderScriptBlur(context, scaled, radius);
        } else {
            scaled = stackBlur(scaled, (int) radius, true);
        }
        if (scale == 1) return scaled;
        Bitmap res = Bitmap.createScaledBitmap(scaled, width, height, true);
        if (scaled != null && !scaled.isRecycled()) scaled.recycle();
        return res;
    }

    /**
     * renderScript模糊图片
     * <p>API大于17</p>
     *
     * @param context 上下文
     * @param src     源图片
     * @param radius  模糊度(0...25)
     * @return 模糊后的图片
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap renderScriptBlur(Context context, Bitmap src, float radius) {
        if (isEmptyBitmap(src)) return null;
        RenderScript rs = null;
        try {
            rs = RenderScript.create(context);
            rs.setMessageHandler(new RenderScript.RSMessageHandler());
            Allocation input = Allocation.createFromBitmap(rs, src, Allocation.MipmapControl.MIPMAP_NONE, Allocation
                    .USAGE_SCRIPT);
            Allocation output = Allocation.createTyped(rs, input.getType());
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            if (radius > 25) {
                radius = 25.0f;
            } else if (radius <= 0) {
                radius = 1.0f;
            }
            blurScript.setInput(input);
            blurScript.setRadius(radius);
            blurScript.forEach(output);
            output.copyTo(src);
        } finally {
            if (rs != null) {
                rs.destroy();
            }
        }
        return src;
    }

    /**
     * stack模糊图片
     *
     * @param src              源图片
     * @param radius           模糊半径
     * @param canReuseInBitmap 是否回收
     * @return stackBlur模糊图片
     */
    public static Bitmap stackBlur(Bitmap src, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = src;
        } else {
            bitmap = src.copy(src.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }

    /**
     * 添加颜色边框
     *
     * @param src         源图片
     * @param borderWidth 边框宽度
     * @param color       边框的颜色值
     * @return 带颜色边框图
     */
    public static Bitmap addFrame(Bitmap src, int borderWidth, int color) {
        if (isEmptyBitmap(src)) return null;
        int newWidth = src.getWidth() + borderWidth;
        int newHeight = src.getHeight() + borderWidth;
        Bitmap out = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Rect rec = canvas.getClipBounds();
        rec.bottom--;
        rec.right--;
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        canvas.drawRect(rec, paint);
        canvas.drawBitmap(src, borderWidth / 2, borderWidth / 2, null);
//        int save = canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();
        if (!src.isRecycled()) src.recycle();
        return out;
    }

    /**
     * 添加倒影
     *
     * @param src              源图片的
     * @param reflectionHeight 倒影高度
     * @return 倒影图
     */
    public static Bitmap addReflection(Bitmap src, int reflectionHeight) {
        if (isEmptyBitmap(src)) return null;
        final int REFLECTION_GAP = 0;
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        if (0 == srcWidth || srcHeight == 0) return null;
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        Bitmap reflectionBitmap = Bitmap.createBitmap(src, 0, srcHeight - reflectionHeight,
                srcWidth, reflectionHeight, matrix, false);
        if (null == reflectionBitmap) return null;
        Bitmap out = Bitmap.createBitmap(srcWidth, srcHeight + reflectionHeight,
                Config.ARGB_8888);
        if (null == out) return null;
        Canvas canvas = new Canvas(out);
        canvas.drawBitmap(src, 0, 0, null);
        canvas.drawBitmap(reflectionBitmap, 0, srcHeight + REFLECTION_GAP, null);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        LinearGradient shader = new LinearGradient(0, srcHeight, 0,
                out.getHeight() + REFLECTION_GAP,
                0x70FFFFFF, 0x00FFFFFF, Shader.TileMode.MIRROR);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.DST_IN));
        canvas.save();
        canvas.drawRect(0, srcHeight, srcWidth,
                out.getHeight() + REFLECTION_GAP, paint);
        canvas.restore();
        if (!src.isRecycled()) src.recycle();
        if (!reflectionBitmap.isRecycled()) reflectionBitmap.recycle();
        return out;
    }

    /**
     * 添加水印文字
     *
     * @param src      源图片
     * @param text     文本
     * @param textSize 字体大小
     * @param color    颜色
     * @param x        起始坐标x
     * @param y        起始坐标y
     * @return 带有水印文字的图片
     */
    public static Bitmap addText(Bitmap src, String text, int textSize, int color, float x, float y) {
        android.graphics.Bitmap.Config bitmapConfig = src.getConfig();
        if (bitmapConfig == null) bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        src = src.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(src);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        canvas.drawText(text, x, y, paint);
        return src;
    }


    /**
     * 转为灰度图像
     *
     * @param src 源图片
     * @return 灰度图
     */
    public static Bitmap toGray(Bitmap src) {
        return toGray(src, false);
    }

    /**
     * 转为灰度图像
     *
     * @param src     源图片
     * @param recycle 是否回收
     * @return 灰度图
     */
    public static Bitmap toGray(Bitmap src, boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap grayBitmap = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(grayBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixColorFilter);
        canvas.drawBitmap(src, 0, 0, paint);
        if (recycle && !src.isRecycled()) src.recycle();
        return grayBitmap;
    }


    /**
     * 根据文件名判断文件是否为图片
     *
     * @param file 　文件
     */
    public static boolean isImage(File file) {
        return file != null && isImage(file.getPath());
    }

    /**
     * 根据文件名判断文件是否为图片
     *
     * @param filePath 　文件路径
     */
    public static boolean isImage(String filePath) {
        String path = filePath.toUpperCase();
        return path.endsWith(".PNG") || path.endsWith(".JPG")
                || path.endsWith(".JPEG") || path.endsWith(".BMP")
                || path.endsWith(".GIF");
    }


    /**
     * 流获取图片类型
     *
     * @param is 图片输入流
     * @return 图片类型
     */
    public static String getImageType(InputStream is) {
        if (is == null) return null;
        try {
            byte[] bytes = new byte[8];
            return is.read(bytes, 0, 8) != -1 ? getImageType(bytes) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取图片类型
     *
     * @param bytes bitmap的前8字节
     * @return 图片类型
     */
    public static String getImageType(byte[] bytes) {
        if (isJPEG(bytes)) return "JPEG";
        if (isGIF(bytes)) return "GIF";
        if (isPNG(bytes)) return "PNG";
        if (isBMP(bytes)) return "BMP";
        return null;
    }

    private static boolean isJPEG(byte[] b) {
        return b.length >= 2
                && (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    private static boolean isGIF(byte[] b) {
        return b.length >= 6
                && b[0] == 'G' && b[1] == 'I'
                && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(byte[] b) {
        return b.length >= 8
                && (b[0] == (byte) 137 && b[1] == (byte) 80
                && b[2] == (byte) 78 && b[3] == (byte) 71
                && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(byte[] b) {
        return b.length >= 2
                && (b[0] == 0x42) && (b[1] == 0x4d);
    }

    /**
     * 判断bitmap对象是否为空
     *
     * @param src 源图片
     * @return {@code true}: 是<br>{@code false}: 否
     */
    private static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}

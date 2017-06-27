package android.idigisign.com.zoomrelativelayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Android Studio.
 * User: xin
 * Date: 20/01/2016 0020
 * Time: 11:41:12 AM
 * Version: V 1.0
 */

public class ZoomView extends FrameLayout {

    private final String TAG = getClass().getName();

    public ZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public ZoomView(final Context context) {
        super(context);
    }

    /**
     * Zooming view listener interface.
     *
     * @author karooolek
     *
     */
    public interface ZoomViewListener {

        void onZoomStarted(float zoomX, float zoomY, float zoomx, float zoomy);

        void onZooming(float zoomX, float zoomY, float zoomx, float zoomy);

        void onZoomEnded(float zoomX, float zoomY, float zoomx, float zoomy);
    }

    // zooming
//    float zoom = 1.0f;
    float zoomX = 1.0f;
    float zoomY = 1.0f;
    float maxZoomX = 4.0f;
    float minZoomX = 1.0f;
    float maxZoomY = 4.0f;
    float minZoomY = 1.0f;
    float smoothZoomX = 1.0f;
    float smoothZoomY = 1.0f;
    float focusX, focusY;
    float smoothFocusX, smoothFocusY;
    private boolean scrolling; // NOPMD by karooolek on 29.06.11 11:45

    // minimap variables
    private boolean showMinimap = false;
    private int miniMapColor = Color.WHITE;
    private int miniMapHeight = -1;
    private String miniMapCaption;
    private float miniMapCaptionSize = 10.0f;
    private int miniMapCaptionColor = Color.WHITE;

    // touching variables
    private long lastTapTime;
    private float touchStartX, touchStartY;
    private float touchLastX, touchLastY;
    private float startd;
    private boolean pinching;
    private float lastd;
    private float lastdx1, lastdy1;
    private float lastdx2, lastdy2;

    // drawing
    private final Matrix m = new Matrix();
    private final Paint p = new Paint();

    // listener
    ZoomViewListener listener;

    private Bitmap ch;

    public float getZoomX() {
        return zoomX;
    }

    public float getZoomY() {
        return zoomY;
    }

    public float getMaxZoomX() {
        return maxZoomX;
    }

    public float getMinZoomX() {
        return minZoomX;
    }

    public float getMaxZoomY() {
        return maxZoomY;
    }

    public float getMinZoomY() {
        return minZoomY;
    }

    public void setMaxZoomX(final float maxZoomX) {
        if (maxZoomX < 1.0f) {
            return;
        }

        this.maxZoomX = maxZoomX;
    }

    public void setMinZoomX(float minZoomX) {
        this.minZoomX = minZoomX;
        if (minZoomX < 1.0f) {
            return;
        }
        this.minZoomX = minZoomX;
    }

    public void setMaxZoomY(final float maxZoomY) {
        if (maxZoomY < 1.0f) {
            return;
        }
        this.maxZoomY = maxZoomY;
    }

    public void setMinZoomY(float minZoomY) {
        this.minZoomY = minZoomY;
        if (minZoomY < 1.0f) {
            return;
        }
        this.minZoomY = minZoomY;
    }

    public void setMiniMapEnabled(final boolean showMiniMap) {
        this.showMinimap = showMiniMap;
    }

    public boolean isMiniMapEnabled() {
        return showMinimap;
    }

    public void setMiniMapHeight(final int miniMapHeight) {
        if (miniMapHeight < 0) {
            return;
        }
        this.miniMapHeight = miniMapHeight;
    }

    public int getMiniMapHeight() {
        return miniMapHeight;
    }

    public void setMiniMapColor(final int color) {
        miniMapColor = color;
    }

    public int getMiniMapColor() {
        return miniMapColor;
    }

    public String getMiniMapCaption() {
        return miniMapCaption;
    }

    public void setMiniMapCaption(final String miniMapCaption) {
        this.miniMapCaption = miniMapCaption;
    }

    public float getMiniMapCaptionSize() {
        return miniMapCaptionSize;
    }

    public void setMiniMapCaptionSize(final float size) {
        miniMapCaptionSize = size;
    }

    public int getMiniMapCaptionColor() {
        return miniMapCaptionColor;
    }

    public void setMiniMapCaptionColor(final int color) {
        miniMapCaptionColor = color;
    }

    public void zoomTo(final float zoomX, final float zoomY, final float x, final float y) {
        this.zoomX = Math.min(zoomX, maxZoomX);
        this.zoomY = Math.min(zoomY, maxZoomY);
        focusX = x;
        focusY = y;
        smoothZoomTo(this.zoomX, this.zoomY, x, y);
    }

    public void smoothZoomTo(final float zoomX, final float zoomY, final float x, final float y) {
        smoothZoomX = clamp(minZoomX, zoomX, maxZoomX);
        smoothZoomY = clamp(minZoomY, zoomY, maxZoomY);
        smoothFocusX = x;
        smoothFocusY = y;
        if (listener != null) {
            listener.onZoomStarted(smoothZoomX, smoothZoomY, x, y);
        }
    }

    public ZoomViewListener getListener() {
        return listener;
    }

    public void setListner(final ZoomViewListener listener) {
        this.listener = listener;
    }

    public float getZoomFocusX() {
        return focusX * zoomX;
    }

    public float getZoomFocusY() {
        return focusY * zoomY;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        // single touch
        if (ev.getPointerCount() == 1) {
            processSingleTouchEvent(ev);
        }

        // // double touch
        if (ev.getPointerCount() == 2) {
            processDoubleTouchEvent(ev);
        }

        // redraw
        getRootView().invalidate();
        invalidate();

        return true;
    }

    private void processSingleTouchEvent(final MotionEvent ev) {

        final float x = ev.getX();
        final float y = ev.getY();

        final float w = miniMapHeight * (float) getWidth() / getHeight();
        final float h = miniMapHeight;
        final boolean touchingMiniMap = x >= 10.0f && x <= 10.0f + w
                && y >= 10.0f && y <= 10.0f + h;

        if (showMinimap && smoothZoomX > 1.0f && smoothZoomY > 1.0f && touchingMiniMap) {
            processSingleTouchOnMinimap(ev);
        } else {
            processSingleTouchOutsideMinimap(ev);
        }
    }

    private void processSingleTouchOnMinimap(final MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();

        final float w = miniMapHeight * (float) getWidth() / getHeight();
        final float h = miniMapHeight;
        final float zx = (x - 10.0f) / w * getWidth();
        final float zy = (y - 10.0f) / h * getHeight();
        smoothZoomTo(smoothZoomX, smoothZoomY, zx, zy);
    }

    private void processSingleTouchOutsideMinimap(final MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        float lx = x - touchStartX;
        float ly = y - touchStartY;
        final float l = (float) Math.hypot(lx, ly);
        float dx = x - touchLastX;
        float dy = y - touchLastY;
        touchLastX = x;
        touchLastY = y;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = x;
                touchStartY = y;
                touchLastX = x;
                touchLastY = y;
                dx = 0;
                dy = 0;
                lx = 0;
                ly = 0;
                scrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (scrolling || (smoothZoomX > 1.0f && smoothZoomY > 1.0f && l > 30.0f)) {
                    if (!scrolling) {
                        scrolling = true;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                    }
                    smoothFocusX -= dx / zoomX;
                    smoothFocusY -= dy / zoomY;
                    return;
                }
                break;

            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP:

                // tap
                if (l < 30.0f) {
                    // check double tap
                    if (System.currentTimeMillis() - lastTapTime < 500) {
                        if (smoothZoomX == 1.0f && smoothZoomY == 1.0f) {
                            smoothZoomTo(maxZoomX, maxZoomY, x, y);
                        } else {
                            smoothZoomTo(1.0f, 1.0f, getWidth() / 2.0f,
                                    getHeight() / 2.0f);
                        }
                        lastTapTime = 0;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return;
                    }

                    lastTapTime = System.currentTimeMillis();

                    performClick();
                }
                break;

            default:
                break;
        }

        ev.setLocation(focusX + (x - 0.5f * getWidth()) / zoomX, focusY
                + (y - 0.5f * getHeight()) / zoomY);

        ev.getX();
        ev.getY();

        super.dispatchTouchEvent(ev);
    }

    private void processDoubleTouchEvent(final MotionEvent ev) {
        final float x1 = ev.getX(0);
        final float dx1 = x1 - lastdx1;
        lastdx1 = x1;
        final float y1 = ev.getY(0);
        final float dy1 = y1 - lastdy1;
        lastdy1 = y1;
        final float x2 = ev.getX(1);
        final float dx2 = x2 - lastdx2;
        lastdx2 = x2;
        final float y2 = ev.getY(1);
        final float dy2 = y2 - lastdy2;
        lastdy2 = y2;

        // pointers distance
        final float d = (float) Math.hypot(x2 - x1, y2 - y1);
        final float dd = d - lastd;
        lastd = d;
        final float ld = Math.abs(d - startd);

        Math.atan2(y2 - y1, x2 - x1);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startd = d;
                pinching = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (pinching || ld > 30.0f) {
                    pinching = true;
                    final float dxk = 0.5f * (dx1 + dx2);
                    final float dyk = 0.5f * (dy1 + dy2);
                    smoothZoomTo(Math.max(1.0f, zoomX * d / (d - dd)), Math.max(1.0f, zoomY * d / (d - dd)), focusX - dxk
                            / zoomX, focusY - dyk / zoomY);
                }

                break;

            case MotionEvent.ACTION_UP:
            default:
                pinching = false;
                break;
        }

        ev.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(ev);
    }

    private float clamp(final float min, final float value, final float max) {
        return Math.max(min, Math.min(value, max));
    }

    private float lerp(final float a, final float b, final float k) {
        return a + (b - a) * k;
    }

    private float bias(final float a, final float b, final float k) {
        return Math.abs(b - a) >= k ? a + k * Math.signum(b - a) : b;
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {

        // do zoom
        zoomX = lerp(bias(zoomX, smoothZoomX, 0.05f), smoothZoomX, 0.2f);
        zoomY = lerp(bias(zoomY, smoothZoomY, 0.05f), smoothZoomY, 0.2f);
        smoothFocusX = clamp(0.5f * getWidth() / smoothZoomX, smoothFocusX,
                getWidth() - 0.5f * getWidth() / smoothZoomX);
        smoothFocusY = clamp(0.5f * getHeight() / smoothZoomY, smoothFocusY,
                getHeight() - 0.5f * getHeight() / smoothZoomY);

        focusX = lerp(bias(focusX, smoothFocusX, 0.1f), smoothFocusX, 0.35f);
        focusY = lerp(bias(focusY, smoothFocusY, 0.1f), smoothFocusY, 0.35f);
        if (zoomX != smoothZoomX && zoomY != smoothZoomY && listener != null) {
            listener.onZooming(zoomX, zoomY, focusX, focusY);
        }

        final boolean animating = Math.abs(zoomX - smoothZoomX) > 0.0000001f
                || Math.abs(zoomY - smoothZoomY) > 0.0000001f
                || Math.abs(focusX - smoothFocusX) > 0.0000001f
                || Math.abs(focusY - smoothFocusY) > 0.0000001f;

        // nothing to draw
        if (getChildCount() == 0) {
            return;
        }

        // prepare matrix
        m.setTranslate(0.5f * getWidth(), 0.5f * getHeight());
        m.preScale(zoomX, zoomY);
        m.preTranslate(
                -clamp(0.5f * getWidth() / zoomX, focusX, getWidth() - 0.5f
                        * getWidth() / zoomX),
                -clamp(0.5f * getHeight() / zoomY, focusY, getHeight() - 0.5f
                        * getHeight() / zoomY));

        // get view
        final View v = getChildAt(0);
        m.preTranslate(v.getLeft(), v.getTop());

        // get drawing cache if available
        if (animating && ch == null && isAnimationCacheEnabled()) {
            v.setDrawingCacheEnabled(true);
            ch = v.getDrawingCache();
        }

        // draw using cache while animating
        if (animating && isAnimationCacheEnabled() && ch != null) {
            p.setColor(0xffffffff);
            canvas.drawBitmap(ch, m, p);
        } else { // zoomed or cache unavailable
            ch = null;
            canvas.save();
            canvas.concat(m);
            v.draw(canvas);
            canvas.restore();
        }

        if (showMinimap) {
            if (miniMapHeight < 0) {
                miniMapHeight = getHeight() / 4;
            }

            canvas.translate(10.0f, 10.0f);

            p.setColor(0x80000000 | 0x00ffffff & miniMapColor);
            final float w = miniMapHeight * (float) getWidth() / getHeight();
            final float h = miniMapHeight;
            canvas.drawRect(0.0f, 0.0f, w, h, p);

            if (miniMapCaption != null && miniMapCaption.length() > 0) {
                p.setTextSize(miniMapCaptionSize);
                p.setColor(miniMapCaptionColor);
                p.setAntiAlias(true);
                canvas.drawText(miniMapCaption, 10.0f,
                        10.0f + miniMapCaptionSize, p);
                p.setAntiAlias(false);
            }

            p.setColor(0x80000000 | 0x00ffffff & miniMapColor);
            final float dx = w * focusX / getWidth();
            final float dy = h * focusY / getHeight();
            canvas.drawRect(dx - 0.5f * w / zoomX, dy - 0.5f * h / zoomY, dx
                    + 0.5f * w / zoomX, dy + 0.5f * h / zoomY, p);

            canvas.translate(-10.0f, -10.0f);
        }
        getRootView().invalidate();
        invalidate();
    }
}
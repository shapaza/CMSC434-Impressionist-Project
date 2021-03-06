package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;
    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();
    private Bitmap _imageViewBitmap;
    private boolean invertColor = false;
    private VelocityTracker velocityTracker = null;

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = false;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;
    //private ArrayList<PaintPoint> _listPaintPoints = new ArrayList<PaintPoint>();

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);
        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
        _imageViewBitmap = _imageView.getDrawingCache();
    }

    public void invertColor() {
        if (invertColor == true)
            invertColor = false;
        else
            invertColor = true;
    }

    public boolean getInvertColor() {
        return invertColor;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    public Bitmap get_offScreenBitmap() {
        return _offScreenBitmap;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //_listPaintPoints.clear();

        //TODO
        if(_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);

        _imageViewBitmap = _imageView.getDrawingCache();

//        for (PaintPoint p : _listPaintPoints) {
//            float touchX = p.getX();
//            float touchY = p.getY();
//            Paint pointPaint = p.getPaint();
//
//            if (p.getBrushType() == BrushType.Square)
//                canvas.drawRect(touchX, touchY, touchX + p.getBrushRadius(), touchY + p.getBrushRadius(), pointPaint);
//            if (p.getBrushType() == BrushType.Circle)
//                canvas.drawCircle(touchX, touchY, p.getBrushRadius(), pointPaint);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        float curTouchX = motionEvent.getX();
        float curTouchY = motionEvent.getY();
        int curTouchXRounded = (int) curTouchX;
        int curTouchYRounded = (int) curTouchY;
        float brushRadius = _defaultRadius;
        int index = motionEvent.getActionIndex();
        int action = motionEvent.getActionMasked();
        int pointerId = motionEvent.getPointerId(index);


        if (curTouchXRounded < 0) curTouchXRounded = 0;
        if (curTouchYRounded < 0) curTouchYRounded = 0;
        if (curTouchXRounded >= _imageViewBitmap.getWidth()) curTouchXRounded = _imageViewBitmap.getWidth() - 1;
        if (curTouchYRounded >= _imageViewBitmap.getHeight()) curTouchYRounded = _imageViewBitmap.getHeight() - 1;

        int pixel = _imageViewBitmap.getPixel(curTouchXRounded, curTouchYRounded);

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(velocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    velocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    velocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                velocityTracker.addMovement(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                int historySize = motionEvent.getHistorySize();
                _paint.setColor(pixel);
                _paint.setStrokeWidth(brushRadius);

                // if color inversion is set to true, then get the complimentary colors
                if (invertColor) {
                    int alpha = Color.alpha(pixel);
                    int red = Color.red(pixel);
                    int blue = Color.blue(pixel);
                    int green = Color.green(pixel);

                    // find compliments
                    red = (~red) & 0xff;
                    blue = (~blue) & 0xff;
                    green = (~green) & 0xff;

                    int invertedColor = Color.argb(alpha, red, blue, green);
                    _paint.setColor(invertedColor);
                }

                for (int i = 0; i < historySize; i++) {

                    float touchX = motionEvent.getHistoricalX(i);
                    float touchY = motionEvent.getHistoricalY(i);

                    if (_brushType == _brushType.Square)
                        _offScreenCanvas.drawRect(touchX, touchY, touchX + brushRadius, touchY + brushRadius, _paint);
                    if (_brushType == _brushType.Circle)
                        _offScreenCanvas.drawCircle(touchX, touchY, brushRadius, _paint);
                    if (_brushType == _brushType.MotionBrush) {
                        velocityTracker.addMovement(motionEvent);
                        velocityTracker.computeCurrentVelocity(1000);

                        int xVelocity = Math.abs((int)velocityTracker.getXVelocity(pointerId));
                        int yVelocity = Math.abs((int) velocityTracker.getYVelocity(pointerId));
                        int finalVel = Math.max(xVelocity, yVelocity);
                        brushRadius = getMotionBrushRadius(finalVel);

                        _offScreenCanvas.drawCircle(touchX, touchY, brushRadius, _paint);
                    }

                }

//                for (int i = 0; i < historySize; i++) {
//                    float touchX = motionEvent.getHistoricalX(i);
//                    float touchY = motionEvent.getHistoricalY(i);
//
//                    PaintPoint paintPoint = new PaintPoint(touchX, touchY, brushRadius, _brushType, _paint);
//                    _listPaintPoints.add(paintPoint);
//
//                }
//
//                PaintPoint currPaintPoint = new PaintPoint(curTouchX, curTouchY, brushRadius, _brushType, _paint);
//                _listPaintPoints.add(currPaintPoint);
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.recycle();
                velocityTracker = null;
                break;
        }

        invalidate();
        return true;
    }

    private int getMotionBrushRadius(int finalVel) {
        int brushRadius = _defaultRadius;

        if (finalVel >= 0 && finalVel < 100)
            brushRadius = 5;
        if (finalVel >= 100 && finalVel < 120)
            brushRadius = 6;
        if (finalVel >= 120 && finalVel < 150)
            brushRadius = 10;
        if (finalVel >= 150 && finalVel < 180)
            brushRadius = 12;
        if (finalVel >= 180 && finalVel < 220)
            brushRadius = 15;
        if (finalVel >= 220 && finalVel < 250)
            brushRadius = 20;
        if (finalVel >= 250 && finalVel < 300)
            brushRadius = 25;
        if (finalVel >= 300 && finalVel < 330)
            brushRadius = 30;
        if (finalVel >= 330)
            brushRadius = 40;

        return brushRadius;
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }

    private class PaintPoint {
        private Paint _paint = new Paint();
        private PointF _point;
        private float _brushRadius;
        private BrushType brush;

        public PaintPoint(float x, float y, float brushRadius, BrushType b, Paint paintSrc){
            // Copy the fields from paintSrc into this paint
            _paint.set(paintSrc);
            _point = new PointF(x, y);
            _brushRadius = brushRadius;
            brush = b;
        }

        public Paint getPaint(){
            return _paint;
        }

        public float getX(){
            return _point.x;
        }

        public float getY(){
            return _point.y;
        }

        public float getBrushRadius(){
            return _brushRadius;
        }

        public BrushType getBrushType() {return brush;}
    }
}


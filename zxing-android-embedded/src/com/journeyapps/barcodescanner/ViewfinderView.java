/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.journeyapps.barcodescanner;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.R;
import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public class ViewfinderView extends View implements IViewFinder {
    protected static final String TAG = ViewfinderView.class.getSimpleName();

    protected static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    protected static final long ANIMATION_DELAY = 80L;
    protected static final int CURRENT_POINT_OPACITY = 0xA0;
    protected static final int MAX_RESULT_POINTS = 20;
    protected static final int POINT_SIZE = 6;

    protected final Paint paint;
    protected Bitmap resultBitmap;
    protected final int maskColor;
    protected final int resultColor;
    protected final int laserColor;
    protected final int resultPointColor;
    protected int scannerAlpha;
    protected List<ResultPoint> possibleResultPoints;
    protected List<ResultPoint> lastPossibleResultPoints;
    protected CameraPreview cameraPreview;

    private final int mDefaultBorderColor = getResources().getColor(R.color.zxing_colorAccent);
    private final int mDefaultBorderStrokeWidth = getResources().getInteger(R.integer.zxing_viewfinder_border_width);
    private final int mDefaultBorderLineLength = getResources().getInteger(R.integer.zxing_viewfinder_border_length);

    protected Paint mBorderPaint;
    protected int mBorderLineLength;

    // Cache the framingRect and previewFramingRect, so that we can still draw it after the preview
    // stopped.
    protected Rect framingRect;
    protected Rect previewFramingRect;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Resources resources = getResources();

        // Get setted attributes on view
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder);

        this.maskColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_mask,
                resources.getColor(R.color.zxing_viewfinder_mask));
        this.resultColor = attributes.getColor(R.styleable.zxing_finder_zxing_result_view,
                resources.getColor(R.color.zxing_result_view));
        this.laserColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_laser,
                resources.getColor(R.color.zxing_viewfinder_laser));
        this.resultPointColor = attributes.getColor(R.styleable.zxing_finder_zxing_possible_result_points,
                resources.getColor(R.color.zxing_possible_result_points));

        attributes.recycle();

        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
        lastPossibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
        init();
    }

    private void init() {

        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);
        mBorderPaint.setAntiAlias(true);

        mBorderLineLength = mDefaultBorderLineLength;
    }



    @Override
    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);
    }

    @Override
    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }

    @Override
    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }



    @Override
    public void setBorderCornerRounded(boolean isBorderCornersRounded) {
        if (isBorderCornersRounded) {
            mBorderPaint.setStrokeJoin(Paint.Join.ROUND);
        } else {
            mBorderPaint.setStrokeJoin(Paint.Join.BEVEL);
        }
    }

    @Override
    public void setBorderAlpha(float alpha) {
        int colorAlpha = (int) (255 * alpha);
        mBorderPaint.setAlpha(colorAlpha);
    }

    @Override
    public void setBorderCornerRadius(int borderCornersRadius) {
        mBorderPaint.setPathEffect(new CornerPathEffect(borderCornersRadius));
    }


    //


    @Override
    public void setLaserColor(int laserColor) {

    }

    @Override
    public void setMaskColor(int maskColor) {

    }

    @Override
    public void setLaserEnabled(boolean isLaserEnabled) {

    }

    @Override
    public void setViewFinderOffset(int offset) {

    }

    @Override
    public void setSquareViewFinder(boolean isSquareViewFinder) {

    }

    @Override
    public void setupViewFinder() {

    }


    public void drawViewFinderBorder(Canvas canvas) {
        Rect framingRect = getFramingRect();

        // Top-left corner
        Path path = new Path();
        path.moveTo(framingRect.left, framingRect.top + mBorderLineLength);
        path.lineTo(framingRect.left, framingRect.top);
        path.lineTo(framingRect.left + mBorderLineLength, framingRect.top);
        canvas.drawPath(path, mBorderPaint);

        // Top-right corner
        path.moveTo(framingRect.right, framingRect.top + mBorderLineLength);
        path.lineTo(framingRect.right, framingRect.top);
        path.lineTo(framingRect.right - mBorderLineLength, framingRect.top);
        canvas.drawPath(path, mBorderPaint);

        // Bottom-right corner
        path.moveTo(framingRect.right, framingRect.bottom - mBorderLineLength);
        path.lineTo(framingRect.right, framingRect.bottom);
        path.lineTo(framingRect.right - mBorderLineLength, framingRect.bottom);
        canvas.drawPath(path, mBorderPaint);

        // Bottom-left corner
        path.moveTo(framingRect.left, framingRect.bottom - mBorderLineLength);
        path.lineTo(framingRect.left, framingRect.bottom);
        path.lineTo(framingRect.left + mBorderLineLength, framingRect.bottom);
        canvas.drawPath(path, mBorderPaint);
    }

    //End

    public void setCameraPreview(CameraPreview view) {
        this.cameraPreview = view;
        view.addStateListener(new CameraPreview.StateListener() {
            @Override
            public void previewSized() {
                refreshSizes();
                invalidate();
            }

            @Override
            public void previewStarted() {

            }

            @Override
            public void previewStopped() {

            }

            @Override
            public void cameraError(Exception error) {

            }

            @Override
            public void cameraClosed() {

            }
        });
    }



    @Override
    public Rect getFramingRect() {
        return framingRect;
    }

    protected void refreshSizes() {
        if(cameraPreview == null) {
            return;
        }
        Rect framingRect = cameraPreview.getFramingRect();
        Rect previewFramingRect = cameraPreview.getPreviewFramingRect();
        if(framingRect != null && previewFramingRect != null) {
            this.framingRect = framingRect;
            this.previewFramingRect = previewFramingRect;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }



        final Rect frame = framingRect;
        final Rect previewFrame = previewFramingRect;

        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {

            // Draw a red "laser scanner" line through the middle to show decoding is active
            paint.setColor(laserColor);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            final int middle = frame.height() / 2 + frame.top;
            canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);

            final float scaleX = frame.width() / (float) previewFrame.width();
            final float scaleY = frame.height() / (float) previewFrame.height();

            final int frameLeft = frame.left;
            final int frameTop = frame.top;

            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (final ResultPoint point : lastPossibleResultPoints) {
                    canvas.drawCircle(
                            frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint
                    );
                }
                lastPossibleResultPoints.clear();
            }

            // draw current possible result points
            if (!possibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                for (final ResultPoint point : possibleResultPoints) {
                    canvas.drawCircle(
                            frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint
                    );
                }

                // swap and clear buffers
                final List<ResultPoint> temp = possibleResultPoints;
                possibleResultPoints = lastPossibleResultPoints;
                lastPossibleResultPoints = temp;
                possibleResultPoints.clear();
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);

            drawViewFinderBorder(canvas);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param result An image of the result.
     */
    public void drawResultBitmap(Bitmap result) {
        resultBitmap = result;
        invalidate();
    }

    /**
     * Only call from the UI thread.
     *
     * @param point a point to draw, relative to the preview frame
     */
    public void addPossibleResultPoint(ResultPoint point) {
        if (possibleResultPoints.size() < MAX_RESULT_POINTS)
            possibleResultPoints.add(point);
    }
}

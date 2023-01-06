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
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

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
public class ViewfinderView extends View {
    protected static final String TAG = ViewfinderView.class.getSimpleName();

    protected static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    protected static final long ANIMATION_DELAY = 80L;
    protected static final int CURRENT_POINT_OPACITY = 0xA0;
    protected static final int MAX_RESULT_POINTS = 20;
    protected static final int POINT_SIZE = 6;

    protected final Paint paint;
    protected Bitmap resultBitmap;
    protected int maskColor;
    protected final int resultColor;
    protected final int laserColor;
    protected final int resultPointColor;
    protected boolean laserVisibility;
    protected int scannerAlpha;
    protected List<ResultPoint> possibleResultPoints;
    protected List<ResultPoint> lastPossibleResultPoints;
    protected int realtimeRotation  = 0;
    protected Rect mFRectFinalPoint;
    protected List<ResultPoint> mResultPoint;
    private final List<ResultPoint>mTransformedResultPoint;
    protected CameraPreview cameraPreview;

    // Cache the framingRect and previewSize, so that we can still draw it after the preview
    // stopped.
    protected Rect framingRect;
    protected Size previewSize;

    private final int mDefaultBorderColor = ContextCompat.getColor(getContext(),R.color.zxing_colorBlueLight);
    private final int mDefaultBorderStrokeWidth = getResources().getInteger(R.integer.zxing_viewfinder_border_width);
    private final int mDefaultBorderLineLength = getResources().getInteger(R.integer.zxing_viewfinder_border_length);

    protected Paint mBorderPaint;
    protected int mBorderLineLength;



    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Resources resources = getResources();

        // Get setted attributes on view
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder);

        this.maskColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_mask,
                ContextCompat.getColor(getContext(),R.color.zxing_viewfinder_mask));
        this.resultColor = attributes.getColor(R.styleable.zxing_finder_zxing_result_view,
                ContextCompat.getColor(getContext(),R.color.zxing_result_view));
        this.laserColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_laser,
                ContextCompat.getColor(getContext(),R.color.zxing_viewfinder_laser));
        this.resultPointColor = attributes.getColor(R.styleable.zxing_finder_zxing_possible_result_points,
                ContextCompat.getColor(getContext(),R.color.zxing_possible_result_points));
        this.laserVisibility = attributes.getBoolean(R.styleable.zxing_finder_zxing_viewfinder_laser_visibility,
                true);

        attributes.recycle();

        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
        lastPossibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
        mResultPoint = new ArrayList<>();
        mTransformedResultPoint = new ArrayList<>();
        mFRectFinalPoint = new Rect();
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

    protected void refreshSizes() {
        if (cameraPreview == null) {
            return;
        }
        Rect framingRect = cameraPreview.getFramingRect();
        Size previewSize = cameraPreview.getPreviewSize();
        if (framingRect != null && previewSize != null) {
            this.framingRect = framingRect;
            this.previewSize = previewSize;
        }
    }


    public void drawViewFinderBorder(Canvas canvas) {
        Rect framingRect = this.framingRect;
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

    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewSize == null) {
            return;
        }

        final Rect frame = framingRect;
        final Size previewSize = this.previewSize;

        final int width = getWidth();
        final int height = getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
        Log.d(TAG,"Rect result "+mResultPoint.toString());
        if (resultBitmap != null) {
            try {
                // Draw the opaque result bitmap over the scanning rectangle
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(resultPointColor);
                int x = 0;
                int y;
                if (mResultPoint.size() > 2) {
                    y = 2;
                    int mPoint1 = Math.round(mResultPoint.get(x).getX()) + framingRect.left;
                    int mPoint2 = Math.round(mResultPoint.get(y).getX()) + framingRect.left;
                    if (mPoint1 > mPoint2) {
                        mFRectFinalPoint.left = mPoint2;
                        mFRectFinalPoint.right = mPoint1;
                    } else {
                        mFRectFinalPoint.left = mPoint1;
                        mFRectFinalPoint.right = mPoint2;
                    }

                    int mPoint3 = Math.round(mResultPoint.get(y).getY()) + framingRect.top;
                    int mPoint4 = Math.round(mResultPoint.get(x).getY()) + framingRect.top;
                    if (mPoint3 > mPoint4) {
                        mFRectFinalPoint.top = mPoint4;
                        mFRectFinalPoint.bottom = mPoint3;
                    } else {
                        mFRectFinalPoint.top = mPoint3;
                        mFRectFinalPoint.bottom = mPoint4;
                    }
                } else if (mResultPoint.size() > 1 && mTransformedResultPoint.size() > 1) {
                    y = 1;
                    int mPoint1 = Math.round(mResultPoint.get(x).getX()) + framingRect.left;
                    int mPoint2 = Math.round(mResultPoint.get(y).getX()) + framingRect.left;
                    if (mPoint1 > mPoint2) {
                        mFRectFinalPoint.left = mPoint2;
                        mFRectFinalPoint.right = mPoint1;
                    } else {
                        mFRectFinalPoint.left = mPoint1;
                        mFRectFinalPoint.right = mPoint2;
                    }

                    int mPoint4 = Math.round(mTransformedResultPoint.get(y).getY());
                    int mWidthMap = (mFRectFinalPoint.right - mFRectFinalPoint.left) / 2;
                    int mPointBottomFinal = mPoint4 - mWidthMap;
                    int mPointTopFinal = mPointBottomFinal - framingRect.top;
                    int mPoint3 = mPointTopFinal + framingRect.top;

                    if (mPoint3 > mPoint4) {
                        mFRectFinalPoint.top = mPoint4;
                        mFRectFinalPoint.bottom = mPoint3;
                    } else {
                        mFRectFinalPoint.top = mPoint3;
                        mFRectFinalPoint.bottom = mPoint4;
                    }
                } else {
                    return;
                }
                Log.d(TAG, "mResultPoint rect " + framingRect);
                Log.d(TAG, "mResultPoint " + mResultPoint);
                Log.d(TAG, "mResultPoint final " + mFRectFinalPoint);
                if (realtimeRotation != 0 && android.provider.Settings.System.getInt(getContext().getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
                    canvas.rotate(realtimeRotation, mFRectFinalPoint.centerX(), mFRectFinalPoint.centerY());
                    canvas.drawBitmap(resultBitmap, null, mFRectFinalPoint, paint);
                } else {
                    canvas.drawBitmap(resultBitmap, null, mFRectFinalPoint, paint);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            // If wanted, draw a red "laser scanner" line through the middle to show decoding is active
            if (laserVisibility) {
                paint.setColor(laserColor);

                paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

                final int middle = frame.height() / 2 + frame.top;
                canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
            }

            final float scaleX = this.getWidth() / (float) previewSize.width;
            final float scaleY = this.getHeight() / (float) previewSize.height;

            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (final ResultPoint point : lastPossibleResultPoints) {
                    canvas.drawCircle(
                             (int) (point.getX() * scaleX),
                             (int) (point.getY() * scaleY),
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
                            (int) (point.getX() * scaleX),
                            (int) (point.getY() * scaleY),
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
    public void drawResultBitmap(Bitmap result,int realtimeRotation) {
        this.resultBitmap = result;
        this.realtimeRotation = realtimeRotation;
        invalidate();
    }

    public void addResultPoint(List<ResultPoint> ls){
        mResultPoint.clear();
        mResultPoint.addAll(ls);
    }

    public void addTransferResultPoint(List<ResultPoint> ls){
        mTransformedResultPoint.clear();
        mTransformedResultPoint.addAll(ls);
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

    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    public void setLaserVisibility(boolean visible) {
        this.laserVisibility = visible;
    }
}

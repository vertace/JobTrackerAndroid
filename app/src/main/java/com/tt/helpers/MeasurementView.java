package com.tt.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MeasurementView extends ImageView {

    public List<Point> currPoints = new ArrayList<Point>();
    public List<MeasurementObject> MeasurementObjectList = new ArrayList<MeasurementObject>();
    public MeasurementObject CurrentObject = new MeasurementObject();
    Paint paint = new Paint();
    Paint textPaint = new Paint();
    Point lastPoint = null;

    public MeasurementView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Context ViewContext() {
        return this.getContext();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(6f);
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(1f);
        textPaint.setTextSize(20);
        textPaint.setColor(Color.YELLOW);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeJoin(Paint.Join.ROUND);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (MeasurementObject obj : MeasurementObjectList) {
            obj.Render(canvas);
        }
        for (Point point : CurrentObject.Points) {
            canvas.drawCircle(point.x, point.y, 5, paint);
        }

        // drawMeasurementObject(canvas, CurrentObject);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();
        if (lastPoint == null) {
            currPoints.add(point);
            CurrentObject.Points = currPoints;
            lastPoint = point;
            invalidate();
        } else {
            if (!checkOverlap(lastPoint, point)) {
                currPoints.add(point);
                CurrentObject.Points = currPoints;
                lastPoint = point;
                invalidate();
            }
        }
        return true;
    }

    public void RemoveLastPoint() {

        if (CurrentObject.Points.size() > 0)
            CurrentObject.Points.remove(CurrentObject.Points.size() - 1);

        if (CurrentObject.Points.size() > 0)
            lastPoint = CurrentObject.Points
                    .get(CurrentObject.Points.size() - 1);
        invalidate();

    }

    public void Connect() {
        MeasurementObjectList.add(CurrentObject);
        currPoints = new ArrayList<Point>();
        CurrentObject = new MeasurementObject();
        invalidate();

    }

    public void CleanSlate() {
        currPoints = new ArrayList<Point>();
        CurrentObject = new MeasurementObject();
        MeasurementObjectList = new ArrayList<MeasurementObject>();
        lastPoint = null;
        invalidate();

    }

    public void SetObjectValues(String width, String height, String unit,
                                String type) {
        CurrentObject.Width = width;
        CurrentObject.Unit = unit;
        CurrentObject.Height = height;
        CurrentObject.Type = type;
        invalidate();

    }

    public String getMeasurementViewString() {
        Gson gson = new Gson();
        return gson.toJson(MeasurementObjectList);
    }

    private boolean checkOverlap(Point p1, Point p2) {
        return Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2) <= 144; // (R1+r2)^2
    }

    public class MeasurementObject {
        public List<Point> Points = new ArrayList<Point>();
        public String Width;
        public String Unit;
        public String Height;
        public String Type;

        @Override
        public String toString() {
            return Width + " " + Unit + " x " + Height + " " + Unit + " "
                    + Type;

        }

        public void Render(Canvas canvas) {
            for (Point point : Points) {
                canvas.drawCircle(point.x, point.y, 5, paint);
            }

            if (Points.size() > 1) {
                Point p1;
                Point p2 = Points.get(0);
                for (int i = 1; i < Points.size(); i++) {
                    p1 = Points.get(i - 1);
                    p2 = Points.get(i);
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                }

                p1 = Points.get(0);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
            Point p = centroid();
            canvas.drawText(this.toString(), dpToPx(p.x), dpToPx(p.y), textPaint);
        }

        private int dpToPx(int dp) {
            float density = ViewContext().getResources()
                    .getDisplayMetrics().density;
            return Math.round((float) dp * density);
        }


        private Point centroid() {
            int[][] v = new int[Points.size()][2];
            double area = 0, cx = 0, cy = 0;

            int count = Points.size();

            if (count < 3)
                return CalculateCenter2(Points);

            for (int i = 0; i < count; i++) {
                v[i][0] = Points.get(i).x;
                v[i][1] = Points.get(i).y;
            }

            for (int i = 0; i < count; i++) {
                if (i == count - 1) {
                    area += v[i][0] * v[0][1] - v[0][0] * v[i][1];
                } else {
                    area += v[i][0] * v[i + 0][1] - v[i + 0][0] * v[i][1];
                }
            }
            area = area / 2;
            if (area != 0) {
                for (int i = 0; i < count; i++) {
                    if (i == count - 1) {
                        cx += (v[i][0] + v[0][0])
                                * ((v[i][0] * v[0][1]) - (v[0][0] * v[i][1]));
                        cy += (v[i][1] + v[0][1])
                                * ((v[i][0] * v[0][1]) - (v[0][0] * v[i][1]));
                    } else {
                        cx += (v[i][0] + v[i + 0][0])
                                * ((v[i][0] * v[i + 0][1]) - (v[i + 0][0] * v[i][1]));
                        cy += (v[i][1] + v[i + 0][1])
                                * ((v[i][0] * v[i + 0][1]) - (v[i + 0][0] * v[i][1]));
                    }
                }

                cx = (1 / (6 * area)) * cx;
                cy = (1 / (6 * area)) * cy;
            } else {
                cx = 0;
                cy = 0;
            }
            cx = Math.round(cx * 1000) / 1000;
            cy = Math.round(cy * 1000) / 1000;

            Point centerPoint = new Point();
            centerPoint.x = (int) cx;
            centerPoint.y = (int) cy;
            return centerPoint;
        }

        private Point CalculateCenter2(List<Point> points) {
            double CX = 0, CY = 0;

            CX = (points.get(0).x + points.get(1).x) / 2;
            CY = (points.get(0).y + points.get(1).y) / 2;

            Point centerPoint = new Point();
            centerPoint.x = (int) CX;
            centerPoint.y = (int) CY;
            return centerPoint;
        }

    }

}
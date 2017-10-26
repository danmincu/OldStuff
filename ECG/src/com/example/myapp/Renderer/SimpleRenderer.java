package com.example.myapp.Renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import com.example.myapp.DataModel.BluetoothInput;
import com.example.myapp.DataModel.IImageStorage;
import com.example.myapp.DropBox.IDropboxSession;
import com.example.myapp.IDisplayImage;
import com.example.myapp.IHeartbeat;
import com.example.myapp.Location.ILocation;

import java.io.File;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

public class SimpleRenderer implements ISimpleRenderer {
    public static final int samplesPerSecond = 400;
    final int imageWidthPx = 4096;
    final int imageHeightPx = 720;
    IDisplayImage displayImage;
    Integer heartBeat = null;
    private Context context;
    private IDropboxSession dropboxSession;
    private IImageStorage imageStorage;
    private IHeartbeat heartbeat;
    private ILocation location;

    public SimpleRenderer(Context context, IImageStorage imageStorage, IDropboxSession dropboxSession, ILocation location) {
        this.context = context;
        this.imageStorage = imageStorage;
        this.dropboxSession = dropboxSession;
        this.location = location;
    }

    public void renderAsync(List<Integer> data) {
        if (this.displayImage != null)
            this.displayImage.showToast("Rendering");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            (new RenderTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
        else
            (new RenderTask()).execute(data);
    }

    @Override
    public void setRenderer(IDisplayImage displayImage) {
        this.displayImage = displayImage;

    }

    @Override
    public Integer getHeartbeat() {
        return heartBeat;
    }

    @Override
    public void setHeartbeat(IHeartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    private class RenderTask extends AsyncTask<List<Integer>, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(List<Integer>... params) {


            try {
                ArrayList<Double> values = new ArrayList<Double>();
                
                int samplesToBeRenderedCount = BluetoothInput.TRIGGER_UI_SAMPLE_LENGTH;

//                String sample ="266,263,263,262,260,260,265,263,263,263,261,260,266,263,262,265,264,262,265,261,262,262,267,262,263,261,261,261,260,260,264,264,262,263,263,261,263,263,265,262,265,261,264,262,263,261,262,260,264,262,262,262,262,262,264,265,267,265,270,269,269,270,271,268,268,269,272,273,269,267,268,265,262,265,263,264,262,262,260,266,261,263,265,260,260,263,260,261,264,261,256,253,265,282,298,312,325,316,301,286,265,251,244,254,259,262,263,262,260,262,263,260,261,264,262,262,261,263,261,260,263,262,263,262,260,261,260,262,264,264,264,267,261,261,265,264,264,264,264,266,269,267,267,268,268,268,272,271,271,271,271,271,275,272,272,276,272,270,275,273,273,276,275,272,275,272,273,272,272,270,271,269,271,265,265,263,264,262,263,262,262,261,263,264,263,261,261,262,264,264,266,264,263,262,263,263,264,261,261,259,261,264,262,262,262,261,261,265,263,262,265,262,262,265,262,261,264,263,261,264,261,261,261,260,263,265,263,262,261,261,260,262,261,261,261,260,262,264,262,262,263,262,260,262,264,263,264,265,264,267,267,268,269,267,267,270,270,270,269,268,268,268,268,266,264,266,263,263,264,265,262,262,261,262,264,262,262,261,260,262,263,259,256,251,263,280,297,311,330,319,298,282,270,253,244,254,259,258,262,260,263,262,264,262,264,261,261,260,261,263,263,263,263,262,261,260,263,264,262,260,261,260,263,264,262,262,269,263,264,267,267,265,269,268,267,272,271,270,271,269,272,274,273,272,273,272,271,276,274,272,276,272,270,275,272,273,277,276,270,274,272,270,269,273,266,266,264,265,262,260,264,263,263,262,261,261,261,263,264,263,264,265,263,264,264,265,262,267,261,262,261,262,261,265,261,262,265,264,263,262,268,261,265,264,263,262,263,262,263,263,262,261,260,261,263,261,262,260,260,261,263,262,262,264,260,259,262,261,262,264,263,261,262,261,262,261,261,262,264,265,266,265,270,269,269,269,269,269,269,269,271,273,271,270,265,264,262,263,260,263,264,262,263,264,262,261,261,260,263,263,262,261,265,258,255,253,261,278,293,315,326,319,302,287,270,253,245,252,256,261,261,260,260,263,263,262,262,260,260,263,262,262,261,262,260,262,262,263,262,262,262,261,262,263,262,260,260,262,264,263,264,270,264,266,268,269,269,273,270,269,272,272,272,270,278,271,273,275,272,272,272,272,277,274,273,272,273,270,275,272,273,275,273,269,271,269,268,266,266,266,264,263,262,261,260,260,263,262,262,261,262,263,263,266,266,263,264,263,262,261,263,263,267,260,261,262,264,262,266,261,261,264,262,261,264,263,262,264,262,262,261,260,263,263,262,262,261,259,262,263,263,261,265,261,262,265,265,265,264,263,260,263,263,263,261,260,263,263,261,262,265,264,264,266,268,267,267,267,267,269,270,270,271,268,268,268,268,266,264,262,263,261,263,264,261,263,268,262,263,263,263,266,263,262,264,260,256,254,263,278,295,310,324,321,309,288,274,258,246,252,256,262,263,260,261,261,261,261,264,262,262,261,261,259,263,261,262,262,261,259,263,262,263,263,261,260,263,263,263,262,267,262,264,265,267,264,269,267,268,270,269,268,269,268,271,273,272,272,271,271,273,274,274,273,276,273,273,275,273,274,276,271,272,274,272,272,273,270,265,267,265,265,266,261,258,263,263,265,264,261,263,263,262,264,263,264,264,266,265,262,262,262,261,261,263,263,263,268,262,262,262,263,262,262,267,262,264,262,261,262,260,261,265,262,263,261,260,262,266,261,262,264,263,262,265,261,262,262,267,261,263,265,262,261,260,258,263,263,262,261,261,260,264,264,266,267,267,267,268,269,269,269,273,270,272,270,271,268,266,265,266,265,261,263,263,261,261,264,262,262,267,262,261,263,263,261,265,260,256,255,259,274,290,305,324,322,306,289,274,256,243,251,257,261,262,262,260,262,261,263,264,263,260,263,261,262,260,259,263,263,262,266,261,262,260,264,261,262,262,266,262,262,265,265,265,265,265,267,269,269,267,269,268,269,273,270,269,271,270,272,275,273,274,273,272,273,277,274,273,277,276,273,276,272,272,271,275,271,273,269,268,267,264,261,264,262,262,262,260,259,263,262,263,264,264,261,264,264,264,263,265,263,263,263,260,260,262,261,262,263,262,262,267,261,261,263,263,261,265,262,261,263,263,262,260,261,261,264,262,262,267,262,261,265,260,265,264,264,262,263,264,265,262,261,261,263,262,261,263,261,260,264,262,262,265,266,264,267,266,268,267,267,269,270,272,269,268,270,267,269,270,267,264,265,262,262,262,263,260,265,261,262,265,263,261,261,261,262,265,261,256,253,257,272,291,306,319,323,307,290,280,260,244,252,255,258,264,261,261,262,260,259,263,262,265,262,261,261,263,262,261,261,264,260,263,261,262,263,265,261,262,262,264,261,261,262,265,264,265,265,269,264,268,269,270,267,268,269,272,274,271,272,271,272,273,275,273,272,273,273,273,275,275,273,273,272,272,274,270,270,269,267,267,269,265,265,266,262,260,263,261,262,264,266,261,263,262,263,263,262,263,265,265,263,261,260,262,262,263,263,261,262,262,264,263,263,262,261,263,262,265,263,261,261,260,261,265,262,262,263,262,262,265,262,261,262,262,261,265,262,262,261,261,259,263,263,262,263,261,258,264,261,261,261,261,261,264,263,266,267,270,267,269,270,273,268,269,268,272,270,270,269,266,266,265,266,263,260,261,261,263,265,263,262,261,262,264,263,263,263,263,259,258,255,256,272,287,301,318,329,310,294,281,262,246,252,255,259,262,261,263,263,262,260,262,261,260,261,263,262,261,260,261,262,262,262,263,261,260,263,262,261,261,263,260,263,263,264,264,266,265,266,267,269,266,267,269,270,273,271,270,271,270,272,276,272,273,273,272,273,276,274,275,279,274,274,277,274,272,273,277,271,275,268,268,267,265,267,266,262,262,262,262,258,264,261,262,262,261,260,265,265,266,263,267,262,264,263,263,261,261,260,264,263,262,263,264,262,262,263,262,262,265,262,261,261,265,262,261,261,263,263,264,262,260,259,262,265,262,261,262,260,262,263,262,262,262,261,260,263,263,261,265,264,261,262,261,262,262,262,263,267,265,268,267,273,272,270,270,270,270,268,271,269,269,266,264,262,260,264,263,262,262,265,262,263,262,263,260,261,265,262,266,262,258,254,253,269,289,303,319,327,310,295,285,263,248,247,253,258,263,261,262,265,265,262,265,261,261,262,265,260,263,263,262,261,261,260,265,262,262,261,260,261,262,263,263,262,261,262,264,264,265,266,267,266,267,269,270,268,272,270,272,274,271,270,271";
//                String[] svalues = sample.split(",");
//                for (int i=0; i < svalues.length; i++)
//                {
//                    values.add(Double.parseDouble(svalues[i]));
//                }
//                for (int i=svalues.length + 1; i < BluetoothInput.TRIGGER_UI_SAMPLE_LENGTH; i++)
//                {
//                    values.add(Double.parseDouble("250"));
//                }

                for (Integer b : params[0]) {
                    values.add(b.doubleValue());
                }

                int total_seconds = values.size() / samplesPerSecond;

                Double min = Double.MAX_VALUE;
                Double max = Double.MIN_VALUE;
                for (Double val : values) {
                    if (min > val)
                        min = val;
                    if (max < val)
                        max = val;
                }

                double heartbeatThreshold = max - (max - min) * 0.3;

                //Allocate a new Bitmap
                final Bitmap bitmap = Bitmap.createBitmap(imageWidthPx, imageHeightPx, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.save();
                canvas.drawColor(Color.WHITE);

                Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                myPaint.setStrokeWidth(0.2f);
                myPaint.setColor(0xffff0000);//color.RED

                int scale = imageWidthPx / (5 * total_seconds);
                for (int i = 0; i < (total_seconds +1)* 5; i++) {

                    if (i % 5 == 0)
                        myPaint.setStrokeWidth(1f);
                    else
                        myPaint.setStrokeWidth(.3f);

                    canvas.drawLine(i * scale, 0, i * scale, imageHeightPx, myPaint);
                    canvas.drawLine(0, i * scale, imageWidthPx, i * scale, myPaint);
                }

                myPaint.setStrokeWidth(3.8f);
                myPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
                myPaint.setColor(0xff0000ff);


                // HEARTBEAT ALGORITHM
                int numHeartbeat = 0;
                int climbing = 1;


                int lastX = 0;
                int lastY = imageHeightPx / 2;

                if (values.size() > 0 && values.get(0) > heartbeatThreshold) {
                    numHeartbeat++;
                    climbing = 1;
                }

                for (int i = 0; i < values.size(); i++) {

                    //compute heartbeat by counting the peaks
                    if (values.get(i) >= heartbeatThreshold && climbing != 1) {
                        climbing = 1;
                        numHeartbeat++;
                    }

                    if (values.get(i) < heartbeatThreshold) {
                        climbing = 0;
                    }


                    int x = (i * imageWidthPx / values.size());


                    double yScale = 0.75 * imageHeightPx / max;
                    int y = ((Double) (0.95 * imageHeightPx - values.get(i) * yScale)).intValue();
                    //int y = values.get(i).intValue();

                    canvas.drawLine(lastX, lastY, x, y, myPaint);
                    lastX = x;
                    lastY = y;
                }

                int factor = 6 / (BluetoothInput.TRIGGER_UI_SAMPLE_LENGTH / samplesToBeRenderedCount);

                myPaint.setTextSize(40);
                canvas.drawText("ECG - Carleton U Group 31 - " + SimpleRenderer.this.imageStorage.storageStamp()
                        + " - heart rate:" + Integer.toString(numHeartbeat * factor) + " bpm", 10, 40, myPaint);
                Location location = SimpleRenderer.this.location.getLocation();
                if (location != null)
                    canvas.drawText("Location - Lat:" + Double.toString(location.getLatitude()) + " Lon:" + Double.toString(location.getLongitude()), 10, 90, myPaint);


                File file = SimpleRenderer.this.imageStorage.storeImage(bitmap);
                try {
                    if (SimpleRenderer.this.dropboxSession.putFileAsync(file, SimpleRenderer.this.displayImage))
                        this.publishProgress("Saving image to dropbox");
                } catch (Exception e) {
                    this.publishProgress("Exception when attempting to save the file to dropbox " + e.getLocalizedMessage());
                }


                //display numHeartbeat - because this is for only 10 seconds multiply with 6 to find out bpm
                this.publishProgress(new String[]{"Heartbeats", Integer.toString(numHeartbeat * factor)});

                SimpleRenderer.this.heartBeat = new Integer(numHeartbeat * factor);

                return bitmap;

            } catch (Exception ex) {
                this.publishProgress(ex.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (SimpleRenderer.this.displayImage != null) {
                SimpleRenderer.this.displayImage.displayImage(result);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values[0].equalsIgnoreCase("Heartbeats")) {
                if (heartbeat != null)
                    heartbeat.Update(values[1]);
            } else {
                if (SimpleRenderer.this.displayImage != null) {
                    for (String s : values) {
                        SimpleRenderer.this.displayImage.showToast(s);
                    }
                }
            }
        }
    }

}

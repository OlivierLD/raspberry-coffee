package oliv.android.graphicsample;

/*
 * Tutorial at
 * - https://stackoverflow.com/questions/17954596/how-to-draw-circle-by-canvas-in-android
 * - https://mkyong.com/android/android-imageview-example/
 */
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image=(ImageView)findViewById(R.id.graphicView1);
        createBitMap();
    }

    private void createBitMap() {
        Bitmap bitMap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);  //creates bmp
        bitMap = bitMap.copy(bitMap.getConfig(), true);     //lets bmp to be mutable
        Canvas canvas = new Canvas(bitMap);                 //draw a canvas in defined bmp

        Paint paint = new Paint();
        // smooths
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4.5f);
        // opacity
        //p.setAlpha(0x80); //
        canvas.drawCircle(50, 50, 30, paint);
        image.setImageBitmap(bitMap);
    }
}

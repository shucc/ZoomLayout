package android.idigisign.com.zoomrelativelayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private ZoomView zoomView;

    private ImageView pdfImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zoomView = (ZoomView) findViewById(R.id.zoomview);
        pdfImage = (ImageView) findViewById(R.id.pdf_image);

        Glide.with(this).load(R.drawable.showki).into(pdfImage);

        int screenHeight = ScreenUtil.height(this).px;
        int screenWidth = ScreenUtil.width(this).px;

//        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) pdfImage.getLayoutParams();
//        param.height = screenHeight;
//        param.width = screenHeight * 9 / 8;
//        pdfImage.setLayoutParams(param);

        findViewById(R.id.imageView3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "我是邮件", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


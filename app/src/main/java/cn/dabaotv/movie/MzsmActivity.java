package cn.dabaotv.movie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import cn.dabaotv.video.R;

public class MzsmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mzsm);
        TextView textView = findViewById(R.id.tv_mzsm);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
}

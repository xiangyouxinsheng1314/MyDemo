package com.demo.listview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListViewDemoActivity extends AppCompatActivity {
    private DragListView dragListView;
    private ListViewAdapter adapter;
    List<String> data = Arrays.asList("本人签收", "邮件签收章", "门卫签收","前台签收","家人签收","同事代签","物管代签","代理点代签","学校代理点签收","补签");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_demo);
        dragListView = findViewById(R.id.drag_list_view);
        data = new ArrayList<>(data);
        adapter = new ListViewAdapter(this,data);
        dragListView.setAdapter(adapter);
        dragListView.setDragItemListener(new DragListView.SimpleAnimationDragItemListener() {
            private Rect mFrame = new Rect();
            @Override
            public boolean canExchange(int srcPosition, int position) {
                boolean result = adapter.exchange(srcPosition, position);
                return result;
            }

            @Override
            public boolean canDrag(View itemView, int x, int y) {
                View dragger = itemView.findViewById(R.id.iv_drag);
                if (dragger == null || dragger.getVisibility() != View.VISIBLE) {
                    return false;
                }
                float tx = x - itemView.getX();
                float ty = y - itemView.getY();
                dragger.getHitRect(mFrame);
                int temp = dp2px(5);
                mFrame.left -= temp;
                mFrame.right += temp;
                mFrame.top -= temp;
                mFrame.bottom += temp;
                if (mFrame.contains((int) tx, (int) ty)) {
                    return true;
                }
                return false;
            }

            @Override
            public void beforeDrawingCache(View itemView) {

            }

            @Override
            public Bitmap afterDrawingCache(View itemView, Bitmap bitmap) {
                return null;
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    public int dp2px(float dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
    }

}

package kevin.com.firstline;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class FruitRecyclerView extends RecyclerView {
    private static final String TAG = "FruitRecyclerView";
    private FruitAdapter fa;
    //LinearLayoutManager layoutManager;
    StaggeredGridLayoutManager layoutManager;
    //GridLayoutManager layoutManager;

    public FruitRecyclerView(Context context) {
        super(context);
        Log.i(TAG, "trace FruitRecyclerView: ctor");
    }

    public FruitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "trace FruitRecyclerView: ctor with attrs");
        //layoutManager = new LinearLayoutManager(context);
        //layoutManager.setOrientation(HORIZONTAL);

        layoutManager = new StaggeredGridLayoutManager(3, VERTICAL);

        //GridLayoutManager  layoutManager = new GridLayoutManager(context, 3, VERTICAL, false);

        setLayoutManager(layoutManager);
        fa = new FruitAdapter(context, Fruits.getFruits());
        setAdapter(fa);
        ((MainActivity)context).onFruitRecyclerViewCreated(this);   //NOT elegant!!!
    }

    private int findMax(int[] v) {
        int max = v[0];
        for (int x: v
             ) {
            if (x > max) {
                max = x;
            }
        }
        Log.i(TAG, "findMax: " + max);
        return max;
    }
/*
    private int findLastVisibleItemPosition() {
        int[] lastPositions = layoutManager.findLastVisibleItemPositions(null);
        Log.i(TAG, "findLastVisibleItemPosition: " + lastPositions);
        for (int x:lastPositions) {
            Log.i(TAG, "findLastVisibleItemPosition: " + x);
        }
        return findMax(lastPositions);
    }
*/
    public void notifyItemInserted(int position) {
        fa.notifyItemInserted(position);
        //scrollToPosition(findLastVisibleItemPosition());
        //scrollToPosition(Fruits.getFruits().size() - 1);    // This can cause unexpected effect!!
        smoothScrollToPosition(Fruits.getFruits().size() - 1);  // This is OK!
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        final int heightSize = MeasureSpec.getSize(heightSpec);
        Log.i(TAG, "onMeasure: " + heightSize);
    }

    public void notifyDataSetChanged() {
        fa.notifyDataSetChanged();
        scrollToPosition(0);
    }
}

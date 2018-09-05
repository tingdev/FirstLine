package kevin.com.firstline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.widget.Toast;

import java.util.List;
import java.util.zip.Inflater;

public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder> {
    private static final String TAG = "FruitAdapter";
    private List<Fruit> fruits;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView)view.findViewById(R.id.fruit_image);
            name = (TextView)view.findViewById(R.id.fruit_name);
        }
    }

    public FruitAdapter(Context context, List<Fruit> fruits) {
        this.context = context;
        this.fruits = fruits;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: " + parent);
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit, parent, false);
        final ViewHolder vh = new ViewHolder(v);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = vh.getAdapterPosition();
                Fruit f = fruits.get(pos);
                //Toast.makeText(context, f.getName() + ", position " + pos + " clicked!", Toast.LENGTH_SHORT).show();

                MainActivity ma = ((MainActivity)context);
                ma.getSupportFragmentManager().beginTransaction().add(R.id.main_layout, new FruitDetailFragment(f)).addToBackStack(null).commit();
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: " + holder + "-" + position);
        Fruit f = fruits.get(position);
        holder.image.setImageResource(f.getImageId());
        holder.name.setText(f.getName() + "\n" + f.getId());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return fruits.size();
    }
}

package kevin.com.firstline;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.litepal.crud.DataSupport;

import java.lang.ref.WeakReference;
import java.util.List;

public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder> {
    private static final String TAG = "FruitAdapter";
    private List<Fruit> fruits;
    private WeakReference<Context> contextWeakReference;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView image;
        TextView desc;
        ImageView dot;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView)view;
            image = (ImageView)view.findViewById(R.id.fruit_image);
            desc = (TextView)view.findViewById(R.id.fruit_desc);
            dot = (ImageView)view.findViewById(R.id.another_fruit_detail_view_prompt);
        }
    }

    public FruitAdapter(Context context, List<Fruit> fruits) {
        this.contextWeakReference = new WeakReference<>(context);
        this.fruits = fruits;
    }

    private boolean isForAnotherDetailView(int fruitId) {
        return (fruitId % 2 == 0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.i(TAG, "onCreateViewHolder: " + parent);
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit, parent, false);
        final ViewHolder vh = new ViewHolder(v);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = vh.getAdapterPosition();
                Fruit f = fruits.get(pos);
                //Toast.makeText(context, f.getName() + ", position " + pos + " clicked!", Toast.LENGTH_SHORT).show();

                // just for fun! ('normal fragment' or 'collapsing tool bar')
                if (isForAnotherDetailView(f.getFid())) {
                    Intent intent = new Intent(contextWeakReference.get(), AnotherFruitDetailActivity.class);
                    intent.putExtra("item", f);
                    contextWeakReference.get().startActivity(intent);
                } else {
                    MainActivity ma = ((MainActivity)contextWeakReference.get());
                    ma.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FruitDetailFragment().setFruit(f)).addToBackStack(null).commit();
                }

                // DON'T rely on isSaved()!!!!!
                // save() will change the id internally!!!!
                //Log.i(TAG, "onClick: save fruit " + f.isSaved() + f.getId() +  f.save() + f.getId());

                List<Fruit> list = DataSupport.select("fid", "detail").where("fid = ?", String.format("%d", f.getFid())).find(Fruit.class);
                for (Fruit ff:list) {
                    Log.i(TAG, "f name " + f.getName());
                }
                Log.i(TAG, "onClick: " + list);
                int match = list.size();
                /*
                also this works!
                Cursor c = DataSupport.findBySQL(String.format("select fid from fruit where fid = %d", f.getFid()));
                int match = c.getCount();
                c.close();
                */

                Log.i(TAG, "fid already in db? " + match);
                if (match == 0) {
                    Log.i(TAG, "save fruit " + f.save());
                }

            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Log.i(TAG, "onBindViewHolder: " + holder + "-" + position);
        Fruit f = fruits.get(position);
        //holder.image.setImageResource(f.getImageId());
        Glide.with(contextWeakReference.get()).load(f.getImageId()).into(holder.image);
        holder.desc.setText(f.getName() + "\n" + f.getDetail() + "\n" + f.getPrice() + "\n" + f.getFid());
        if (isForAnotherDetailView(f.getFid())) {
            holder.dot.setVisibility(View.VISIBLE);
        } else {
            holder.dot.setVisibility(View.INVISIBLE);
        }
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

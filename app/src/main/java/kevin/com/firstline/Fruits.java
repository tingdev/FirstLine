package kevin.com.firstline;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fruits {
    private static List<Fruit> fruits = new ArrayList<Fruit>();
    private static final String TAG = "Fruits";

    public static void init() {
        fruits.clear();
        int id = 0;
        fruits.add(new Fruit(id++, R.drawable.apple, "apple", "red US apple", 1.99));
        fruits.add(new Fruit(id++, R.drawable.banana, "banana", "yummy phillipines production!", 2.99));
        fruits.add(new Fruit(id++, R.drawable.cherry, "cherry", "great taste\nBrazil production, fresh! order now. 20% off", 3.99));
        fruits.add(new Fruit(id++, R.drawable.grape, "grape", "colorful, yummy", 4.99));
        fruits.add(new Fruit(id++, R.drawable.kiwi, "kiwi", "most fresh", 5.99));
        fruits.add(new Fruit(id++, R.drawable.orange, "orange", "fresh", 6.99));
        fruits.add(new Fruit(id++, R.drawable.pineapple, "pineapple", "Taiwan production, fresh", 7.99));
        fruits.add(new Fruit(id++, R.drawable.strawberry, "strawberry", "sweet", 8.99));
        fruits.add(new Fruit(id++, R.drawable.tomato, "tomato", "beautiful", 9.99));
    }

    public static List<Fruit> getFruits() {
        return fruits;
    }

    public static void shuffle() {
        int size = fruits.size();
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < size * 100; i++) {
            int idx = r.nextInt(size);
            Log.i(TAG, "shuffle: " + idx);
            Fruit f = fruits.remove(idx);
            fruits.add(f);
        }
    }
}

package kevin.com.firstline;

import java.util.ArrayList;
import java.util.List;

public class Fruits {
    private static List<Fruit> fruits = new ArrayList<Fruit>();

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
}

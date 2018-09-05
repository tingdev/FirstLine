package kevin.com.firstline;

import java.util.ArrayList;
import java.util.List;

public class Fruits {
    private static List<Fruit> fruits = new ArrayList<Fruit>();

    public static void init() {
        fruits.clear();
        int id = 0;
        fruits.add(new Fruit(id++, R.drawable.apple, "apple, red US apple"));
        fruits.add(new Fruit(id++, R.drawable.banana, "banana, yummy phillipines production!"));
        fruits.add(new Fruit(id++, R.drawable.cherry, "cherry\ngreat taste\nBrazil production, fresh! order now. 20% off"));
        fruits.add(new Fruit(id++, R.drawable.grape, "grape"));
        fruits.add(new Fruit(id++, R.drawable.kiwi, "kiwi\nmost fresh"));
        fruits.add(new Fruit(id++, R.drawable.orange, "orange"));
        fruits.add(new Fruit(id++, R.drawable.pineapple, "pineapple\nTaiwan production, fresh"));
        fruits.add(new Fruit(id++, R.drawable.strawberry, "strawberry"));
        fruits.add(new Fruit(id++, R.drawable.tomato, "tomato"));
    }

    public static List<Fruit> getFruits() {
        return fruits;
    }
}

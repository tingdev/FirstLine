package kevin.com.firstline;

public class Fruit {
    private int id;
    private int imageId;
    private String name;

    Fruit(int id, int imageId, String name) {
        this.id = id;
        this.imageId = imageId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }
}

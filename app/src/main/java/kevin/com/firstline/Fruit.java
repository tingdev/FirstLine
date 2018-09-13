package kevin.com.firstline;

import org.litepal.crud.DataSupport;

public class Fruit extends DataSupport {
    //private int id;     //optional for litepal, if declared, used  as id in

    private int fid;
    private int imageId;
    private String name;
    private String detail;
    private double price;

    //LitePal requires the default constructor!!!
    public Fruit() {

    }

    public Fruit(int fid, int imageId, String name, String detail) {
        this.fid = fid;
        this.imageId = imageId;
        this.name = name;
        this.detail = detail;
    }

    public Fruit(int fid, int imageId, String name, String detail, double price) {
        this.fid = fid;
        this.imageId = imageId;
        this.name = name;
        this.detail = detail;
        this.price = price;
    }

    public int getFid() {
        return fid;
    }

    public int getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public double getPrice() {
        return price;
    }
/*
    public int getId() {
        return id;
    }
*/
}

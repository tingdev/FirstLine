package kevin.com.firstline;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

public class Fruit extends DataSupport implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fid);
        dest.writeInt(imageId);
        dest.writeString(name);
        dest.writeString(detail);
        dest.writeDouble(price);
    }

    public static final Parcelable.Creator<Fruit> CREATOR = new Parcelable.Creator<Fruit>() {
        @Override
        public Fruit createFromParcel(Parcel source) {
            return new Fruit(source);
        }

        @Override
        public Fruit[] newArray(int size) {
            return new Fruit[size];
        }
    };

    public Fruit(Parcel source) {
        fid = source.readInt();
        imageId = source.readInt();
        name = source.readString();
        detail = source.readString();
        price = source.readDouble();
    }
/*
    public int getId() {
        return id;
    }
*/
}

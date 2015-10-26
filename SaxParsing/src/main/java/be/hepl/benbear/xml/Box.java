package be.hepl.benbear.xml;

public class Box<T> {

    public T item;

    public Box(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

}

package com.example.quickstart;


public class Model {

    public static final int headerType = 0;
    public static final int itemType = 1;

    public int type;
    public String date;
    public String category;
    public String name;
    public String Price;
    public int Position;

    public Model(int type, String date, String category, String name, String price, int position) {
        this.type = type;
        this.date = date;
        this.category = category;
        this.name = name;
        Price = price;
        Position = position;
    }
}

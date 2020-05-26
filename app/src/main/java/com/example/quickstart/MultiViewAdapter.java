package com.example.quickstart;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MultiViewAdapter extends RecyclerView.Adapter {
    private ArrayList<Model> dataSet;
    Context context;
    int total_types;

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{
        public Context context;
        TextView headerText;
        TextView headerMMyyyy;
        TextView headerDay;
        TextView headerPrice;

        public HeaderViewHolder(View itemView,Context context) {
            super(itemView);

            this.context = context;
            headerText = (TextView) itemView.findViewById(R.id.txt_header);
            headerMMyyyy = (TextView) itemView.findViewById(R.id.txt_header_mm_yyyy);
            headerDay = (TextView) itemView.findViewById(R.id.txt_header_day);
            headerPrice = (TextView) itemView.findViewById(R.id.txt_headerPrice);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        public final Context context;
        TextView nameText;
        TextView priceText;


        public ItemViewHolder(View itemView, final Context context) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(R.id.txtItemName);
            priceText = (TextView) itemView.findViewById(R.id.txtItemPrice);
            this.context = context;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){ ;
                        String edt_date = dataSet.get(pos).date;
                        String edt_price = dataSet.get(pos).Price;
                        String edt_category = dataSet.get(pos).category;
                        String edt_name = dataSet.get(pos).name;
                        int row_position = dataSet.get(pos).Position;
                        ((DataActivity) context).Update(row_position,edt_date,edt_category,edt_name,edt_price);
                    }
                }
            });
        }
    }

    public MultiViewAdapter(ArrayList<Model> data, Context context) {
        this.dataSet = data;
        this.context = context;
        this.total_types = dataSet.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case Model.headerType:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.headerview,parent,false);
                return new HeaderViewHolder(view,context);

             case Model.itemType:
                 view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview,parent,false);
                 return new ItemViewHolder(view,context);
        }

        return null;
    }
    @Override
    public int getItemViewType(int position){

        switch (dataSet.get(position).type){
            case 0:
                return Model.headerType;
            case 1:
                return Model.itemType;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Model object = dataSet.get(position);
        if (object != null) {
            switch (object.type) {
                case Model.headerType:
                    ((HeaderViewHolder) holder).headerText.setText(object.date);
                    ((HeaderViewHolder) holder).headerMMyyyy.setText(object.category);
                    ((HeaderViewHolder) holder).headerDay.setText(object.name);
                    ((HeaderViewHolder) holder).headerPrice.setText(object.Price);

                    break;
                case Model.itemType:
                    ((ItemViewHolder) holder).nameText.setText(dataSet.get(position).name);
                    ((ItemViewHolder) holder).priceText.setText(dataSet.get(position).Price);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

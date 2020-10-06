package com.missing.nfp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final String TAG = "RecyclerViewAdapter";
    private Context mContext;
    private List<Cell> mData;
    static final int NumRows = 7;  //TODO: Set to 2 for free version, any amt for paid version


    public int getNumRows() {
        return NumRows;
    }

    public RecyclerViewAdapter(Context mContext, List<Cell> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;

        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position % NumRows == 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item, viewGroup, false);
            return new ItemViewHolder(view);

        } else if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_item, viewGroup, false);
            return new HeaderViewHolder(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof HeaderViewHolder) {

            //set the Header Label
            String text = 1+position/NumRows+"";
            ((HeaderViewHolder) holder).txtName.setText(text);

            //similarly bind other UI components or perform operations

        }else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).tv_cell_code.setText(mData.get(position).getSummation());
            ((ItemViewHolder) holder).img_sticker.setImageResource(mData.get(position).getSticker());
            ((ItemViewHolder) holder).cv_cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mContext, NfpEntry.class);

                    //Pass data to NfpEntry activity
                    Log.d(TAG,"Cell #: " + position);
                    Log.d(TAG, "Putting extras now.");
                    intent.putExtra("INDEX", position);
                    intent.putExtra("DATE", mData.get(position).getDate());
                    intent.putExtra("CODE", mData.get(position).getCode());
                    intent.putExtra("COMMENTS", mData.get(position).getComments());
                    intent.putExtra("STICKER", mData.get(position).getSticker());

                    //mContext.startActivity(intent);
                    ((Activity) mContext).startActivityForResult(intent,333);
                }
            });

        }




    }

    @Override
    public int getItemCount() {
        // Add more counts to accomodate header
        return this.mData.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public View View;
        private final TextView txtName;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            View = itemView;

            // add your ui components here like this below
            txtName = View.findViewById(R.id.id_header_label);

        }
    }
    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tv_cell_code;
        ImageView img_sticker;
        CardView cv_cardview;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_cell_code = itemView.findViewById(R.id.id_cell_code);
            img_sticker = itemView.findViewById(R.id.id_sticker_code);
            cv_cardview = itemView.findViewById(R.id.id_whole_cell);
        }
    }




}

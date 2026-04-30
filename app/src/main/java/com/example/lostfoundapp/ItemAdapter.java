package com.example.lostfoundapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

/**
 * 修复了 OOM 内存崩溃的版本
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<LostFoundItem> itemList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
    }

    public ItemAdapter(Context context, List<LostFoundItem> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        LostFoundItem item = itemList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvType.setText(item.getType());
        holder.tvCategory.setText(item.getCategory() + " | " + item.getLocation());
        holder.tvTimestamp.setText(item.getTimestamp());

        // 关键修复：不要直接使用 setImageURI，而是加载缩略图
        if (item.getImageUri() != null) {
            try {
                Uri uri = Uri.parse(item.getImageUri());
                String path = uri.getPath();
                if (path != null && new File(path).exists()) {
                    // 只加载 100x100 的缩略图，极大地节省内存
                    Bitmap thumb = decodeSampledBitmap(path, 100, 100);
                    holder.ivThumbnail.setImageBitmap(thumb);
                } else {
                    holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } catch (Exception e) {
                holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    // 采样缩放方法
    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateList(List<LostFoundItem> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvName, tvType, tvCategory, tvTimestamp;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvType = itemView.findViewById(R.id.tvItemType);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvTimestamp = itemView.findViewById(R.id.tvItemTimestamp);
        }
    }
}

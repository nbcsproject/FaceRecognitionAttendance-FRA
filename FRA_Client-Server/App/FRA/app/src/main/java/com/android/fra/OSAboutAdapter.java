package com.android.fra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class OSAboutAdapter extends RecyclerView.Adapter<OSAboutAdapter.ViewHolder> {
    private Context mContext;
    private List<About> mAboutList;
    private OSAboutAdapter.onItemClickListener listener;
    private List<String> urlList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout about_view;
        TextView titleText;
        TextView authorText;
        TextView descriptionText;

        public ViewHolder(View view) {
            super(view);
            about_view = (LinearLayout) view.findViewById(R.id.about_view);
            titleText = (TextView) view.findViewById(R.id.title_view);
            authorText = (TextView) view.findViewById(R.id.author_view);
            descriptionText = (TextView) view.findViewById(R.id.description_view);
        }
    }

    public OSAboutAdapter(List<About> aboutList) {
        mAboutList = aboutList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.os_about_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        About about = mAboutList.get(position);
        holder.titleText.setText(about.getTitleText());
        holder.authorText.setText(about.getAuthorText());
        holder.descriptionText.setText(about.getDescriptionText());
        urlList = listener.getUrlList();
        holder.about_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("url", urlList.get(position));
                ((Activity) mContext).startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAboutList.size();
    }

    public void setListener(OSAboutAdapter.onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        List<String> getUrlList();
    }

}

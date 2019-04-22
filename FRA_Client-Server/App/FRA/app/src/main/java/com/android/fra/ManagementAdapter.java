package com.android.fra;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.fra.db.Face;
import com.bumptech.glide.Glide;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;

public class ManagementAdapter extends RecyclerView.Adapter<ManagementAdapter.ViewHolder> {
    private Context mContext;
    private List<Face> mFaceList;
    private int opened = -1;
    private Map<Integer, Boolean> map = new LinkedHashMap<>();
    private onItemClickListener listener;
    private boolean inDeletionMode = false;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View managementView;
        CheckBox checkBoxView;
        ImageView imageView;
        TextView uidTextView;
        TextView nameTextView;
        TextView phoneTextView;
        TextView departmentTextView;
        TextView postTextView;
        TextView emailTextView;
        FrameLayout detailInfoView;
        LinearLayout root_view;
        ImageButton edit_button;

        public ViewHolder(View view) {
            super(view);
            managementView = view;
            checkBoxView = (CheckBox) view.findViewById(R.id.worker_ifCheck);
            imageView = (ImageView) view.findViewById(R.id.worker_gender);
            uidTextView = (TextView) view.findViewById(R.id.worker_uid);
            nameTextView = (TextView) view.findViewById(R.id.worker_name);
            phoneTextView = (TextView) view.findViewById(R.id.detail_phone);
            departmentTextView = (TextView) view.findViewById(R.id.detail_department);
            postTextView = (TextView) view.findViewById(R.id.detail_post);
            emailTextView = (TextView) view.findViewById(R.id.detail_email);
            detailInfoView = (FrameLayout) view.findViewById(R.id.detail_info);
            root_view = (LinearLayout) view.findViewById(R.id.root_view);
            edit_button = (ImageButton) view.findViewById(R.id.detail_edit_button);
        }
    }

    public ManagementAdapter(List<Face> faceList) {
        mFaceList = faceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        final View view = LayoutInflater.from(mContext).inflate(R.layout.information_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Face face = mFaceList.get(holder.getAdapterPosition());
                Intent intent = new Intent(mContext, EditActivity.class);
                intent.putExtra("uid", face.getUid());
                intent.putExtra("name", face.getName());
                intent.putExtra("gender", face.getGender());
                intent.putExtra("phone", face.getPhone());
                intent.putExtra("department", face.getDepartment());
                intent.putExtra("post", face.getPost());
                intent.putExtra("email", face.getEmail());
                ((Activity) mContext).startActivityForResult(intent, 1);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.checkBoxView.setVisibility(inDeletionMode ? VISIBLE : View.GONE);
        final Face face = mFaceList.get(position);
        holder.uidTextView.setText(face.getUid());
        holder.nameTextView.setText(face.getName());
        holder.phoneTextView.setText(face.getPhone());
        holder.departmentTextView.setText(face.getDepartment());
        if (face.getPost() != null && !face.getPost().equals("") && !face.getPost().equals("null")) {
            holder.postTextView.setText(face.getPost());
        } else {
            holder.postTextView.setText("暂无");
        }
        if (face.getEmail() != null && !face.getEmail().equals("") && !face.getEmail().equals("null")) {
            holder.emailTextView.setText(face.getEmail());
        } else {
            holder.emailTextView.setText("暂无");
        }

        if (face.getGender().equals("male")) {
            Glide.with(mContext).load(R.drawable.ic_male).into(holder.imageView);
        } else if (face.getGender().equals("female")) {
            Glide.with(mContext).load(R.drawable.ic_female).into(holder.imageView);
        }
        holder.detailInfoView.measure(0, 0);
        final int height = holder.detailInfoView.getMeasuredHeight();
        if (position == opened) {
            show(holder.detailInfoView, height);
        } else {
            dismiss(holder.detailInfoView, height);
        }

        holder.root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inDeletionMode == true) {
                    if (!holder.checkBoxView.isChecked()) {
                        holder.checkBoxView.setChecked(true);
                    } else {
                        holder.checkBoxView.setChecked(false);
                    }
                } else {
                    holder.detailInfoView.measure(0, 0);
                    final int height = holder.detailInfoView.getMeasuredHeight();
                    if (holder.detailInfoView.getVisibility() == View.VISIBLE) {
                        opened = -1;
                        notifyItemChanged(holder.getAdapterPosition());
                    } else {
                        int openedTemp = opened;
                        opened = position;
                        notifyItemChanged(openedTemp);
                        notifyItemChanged(opened);
                    }
                }
            }
        });

        holder.root_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (opened == -1) {
                    if (!holder.checkBoxView.isChecked()) {
                        holder.checkBoxView.setChecked(true);
                        /*布局变色可以添加在这里*/
                        inDeletionMode = true;
                        listener.showText();
                        notifyDataSetChanged();
                    }
                }
                return false;
            }
        });

        holder.checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    map.put(position, true);
                    if (map.size() == mFaceList.size()) {
                        listener.setText("取消全选");
                    }
                } else {
                    map.remove(position);
                    listener.setText("全选");
                }
            }
        });

        if (map.containsKey(position)) {
            holder.checkBoxView.setChecked(map.get(position));
        } else {
            holder.checkBoxView.setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        return mFaceList.size();
    }

    public void show(final View v, int height) {
        v.setVisibility(VISIBLE);
        ValueAnimator animator = ValueAnimator.ofInt(0, height);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value;
                v.setLayoutParams(v.getLayoutParams());
            }
        });
        animator.start();
    }

    public void dismiss(final View v, int height) {
        ValueAnimator animator = ValueAnimator.ofInt(height, 0);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                if (value == 0) {
                    v.setVisibility(View.GONE);
                }
                v.getLayoutParams().height = value;
                v.setLayoutParams(v.getLayoutParams());
            }
        });
        animator.start();
    }

    public boolean getInDeletionMode() {
        return this.inDeletionMode;
    }

    public void setInDeletionMode(boolean inDeletionMode) {
        this.inDeletionMode = inDeletionMode;
        notifyDataSetChanged();
    }

    public Map<Integer, Boolean> getMap() {
        return this.map;
    }

    public void setMap(Map<Integer, Boolean> map) {
        this.map = map;
    }

    public void setListener(onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        void showText();

        void setText(String text);
    }

}

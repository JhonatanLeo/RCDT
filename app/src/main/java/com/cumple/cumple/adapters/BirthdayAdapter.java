package com.cumple.cumple.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cumple.cumple.R;
import com.cumple.cumple.models.Birthday;

import java.util.List;

public class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder> {
    private List<Birthday> birthdayList;
    private OnBirthdayListener onBirthdayListener;

    public BirthdayAdapter(List<Birthday> birthdayList, OnBirthdayListener onBirthdayListener) {
        this.birthdayList = birthdayList;
        this.onBirthdayListener = onBirthdayListener;
    }

    @NonNull
    @Override
    public BirthdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_birthday, parent, false);
        return new BirthdayViewHolder(view, onBirthdayListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BirthdayViewHolder holder, int position) {
        Birthday birthday = birthdayList.get(position);
        Context context = holder.itemView.getContext();

        holder.nameTextView.setText(birthday.getName());

        // Formatear fecha
        String[] months = context.getResources().getStringArray(R.array.months);
        String formattedDate = birthday.getDay() + " " + months[birthday.getMonth() - 1];
        holder.dateTextView.setText(formattedDate);

        // Mostrar teléfono si existe
        if (birthday.getPhone() != null && !birthday.getPhone().isEmpty()) {
            holder.locationTextView.setText(birthday.getPhone());
            holder.locationTextView.setVisibility(View.VISIBLE);
        } else {
            holder.locationTextView.setVisibility(View.GONE);
        }

        // Mostrar ubicación si existe
        if (birthday.getUbicacion() != null && !birthday.getUbicacion().isEmpty()) {
            holder.locationTextView.setText(birthday.getUbicacion());
            holder.locationTextView.setVisibility(View.VISIBLE);
        } else {
            holder.locationTextView.setVisibility(View.GONE);
        }

        // Mostrar días restantes
        int daysLeft = birthday.getDaysLeft();
        if (daysLeft == 0) {
            holder.daysLeftTextView.setText(context.getString(R.string.days_left_today));
            holder.daysLeftTextView.setBackgroundResource(R.drawable.days_left_background);
            holder.priorityIndicator.setBackgroundResource(R.color.priorityHigh);
        } else if (daysLeft == 1) {
            holder.daysLeftTextView.setText(context.getString(R.string.days_left_tomorrow));
            holder.daysLeftTextView.setBackgroundResource(R.drawable.days_left_background);
            holder.priorityIndicator.setBackgroundResource(R.color.priorityHigh);
        } else if (daysLeft <= 7) {
            holder.daysLeftTextView.setText(context.getString(R.string.days_left_format, daysLeft));
            holder.priorityIndicator.setBackgroundResource(R.color.priorityMedium);
        } else {
            holder.daysLeftTextView.setText(context.getString(R.string.days_left_format, daysLeft));
            holder.priorityIndicator.setBackgroundResource(R.color.priorityLow);
        }
    }

    @Override
    public int getItemCount() {
        return birthdayList.size();
    }

    public class BirthdayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTextView;
        TextView dateTextView;
        TextView locationTextView;
        TextView daysLeftTextView;
        View priorityIndicator;
        OnBirthdayListener onBirthdayListener;

        public BirthdayViewHolder(@NonNull View itemView, OnBirthdayListener onBirthdayListener) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_name);
            dateTextView = itemView.findViewById(R.id.tv_date);
            locationTextView = itemView.findViewById(R.id.tv_location);
            daysLeftTextView = itemView.findViewById(R.id.tv_days_left);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);

            this.onBirthdayListener = onBirthdayListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onBirthdayListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onBirthdayListener.onBirthdayClick(position);
                }
            }
        }
    }

    public interface OnBirthdayListener {
        void onBirthdayClick(int position);
    }

    public void updateData(List<Birthday> newBirthdayList) {
        this.birthdayList = newBirthdayList;
        notifyDataSetChanged();
    }

    public Birthday getBirthday(int position) {
        return birthdayList.get(position);
    }
}
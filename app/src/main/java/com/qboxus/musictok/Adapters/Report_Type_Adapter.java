package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Models.Report_Type_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

public class Report_Type_Adapter  extends RecyclerView.Adapter<Report_Type_Adapter.CustomViewHolder > {

    public Context context;
    private OnItemClickListener listener;
    private ArrayList<Report_Type_Get_Set> dataList;


    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int positon, Report_Type_Get_Set item, View view);
    }



    public Report_Type_Adapter(Context context, ArrayList<Report_Type_Get_Set> dataList, OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_report_list,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }



    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        Report_Type_Get_Set item= dataList.get(i);
        holder.setIsRecyclable(false);

        holder.bind(i,item,listener);
        holder.report_name.setText(item.title);

        Log.d(Variables.tag,item.title);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView report_name;
        RelativeLayout rlt_report;

        public CustomViewHolder(View view) {
            super(view);


            report_name=view.findViewById(R.id.report_name);
            rlt_report=view.findViewById(R.id.rlt_report);
        }

        public void bind(final int postion, final Report_Type_Get_Set item, final OnItemClickListener listener) {

            rlt_report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion,item,v);
                }
            });
        }


    }
}

package com.patelheggere.tripplanner.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.core.utilities.Utilities;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.model.EventDetailModel;
import com.patelheggere.tripplanner.utils.UtilsClass;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventHolder>{

    private static final String TAG = "AssessmentListAdapter";
    private final SelectEditDelete mListener;
    List<EventDetailModel> jobList;
    private Context mContext;
    private AlertDialog alertDialog;
    private int pos;

    public EventAdapter(Context mContext, List<EventDetailModel> jobList) {
        this.jobList = jobList;
        this.mContext = mContext;
        mListener = (SelectEditDelete)this.mContext;
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_item, viewGroup, false);
        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventHolder currentaffairsViewHolder, final int i) {
        final EventDetailModel eventDetailModel = jobList.get(i);
        if(eventDetailModel!=null) {
            if(eventDetailModel.getEventName()!=null)
            {
                currentaffairsViewHolder.title.setText(eventDetailModel.getEventName());
            }
            if(eventDetailModel.getPlaceName()!=null){
                currentaffairsViewHolder.place.setText(eventDetailModel.getPlaceName());
            }
            if(eventDetailModel.getTimeStamp()!=0)
            {
                currentaffairsViewHolder.date.setText(UtilsClass.getDateTimeFromMilli(eventDetailModel.getTimeStamp()));
            }
            currentaffairsViewHolder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener!=null)
                    {
                        mListener.selectedEditPosition(eventDetailModel);
                    }
                }
            });

            currentaffairsViewHolder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener!=null)
                    {
                        mListener.selectedDeletePosition(eventDetailModel);
                    }
                }
            });
            if(eventDetailModel.getContactPerson()!=null)
                currentaffairsViewHolder.name.setText(eventDetailModel.getContactPerson());
            else
                currentaffairsViewHolder.name.setText("NA");
            if(eventDetailModel.getContactNumber()!=null)
                currentaffairsViewHolder.phone.setText(eventDetailModel.getContactNumber());
            else
                currentaffairsViewHolder.phone.setText("NA");

            if(eventDetailModel.getImageURL()!=null)
            {
                currentaffairsViewHolder.imageViewAttachment.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(eventDetailModel.getImageURL()).into(currentaffairsViewHolder.imageViewAttachment);
            }
            currentaffairsViewHolder.imageViewAttachment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    LayoutInflater layoutInflater =(LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View mView = layoutInflater.inflate(R.layout.image_full_view, null);
                    ImageView close = mView.findViewById(R.id.imageViewClose);
                    ImageView imageViewAttachment = mView.findViewById(R.id.imageViewAttachment);
                    //imageViewAttachment.setImageDrawable(currentaffairsViewHolder.imageViewAttachment.getDrawable());
                    Log.d(TAG, "onClick: "+eventDetailModel.getImageURL());
                    Glide.with(mContext).load(eventDetailModel.getImageURL()).into(imageViewAttachment);
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                    builder.setView(mView);
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public class EventHolder extends RecyclerView.ViewHolder {
        TextView title, date, place, name, phone;
        ImageView imageViewEdit, imageViewDelete, imageViewAttachment;
        public EventHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewEventName);
            date = itemView.findViewById(R.id.textViewDateTime);
            place = itemView.findViewById(R.id.textViewPlaceName);
            imageViewDelete = itemView.findViewById(R.id.deleteIcon);
            imageViewEdit = itemView.findViewById(R.id.editIcon);
            imageViewAttachment = itemView.findViewById(R.id.imageViewAttachment);
            name = itemView.findViewById(R.id.textViewContactNameValue);
            phone = itemView.findViewById(R.id.textViewContactPhoneValue);
        }
    }

    public interface SelectEditDelete {
        void selectedEditPosition(EventDetailModel position);
        void selectedDeletePosition(EventDetailModel position);
    }
}
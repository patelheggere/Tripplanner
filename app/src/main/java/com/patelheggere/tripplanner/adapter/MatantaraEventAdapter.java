package com.patelheggere.tripplanner.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.model.PlaceDetails;
import com.patelheggere.tripplanner.utils.UtilsClass;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MatantaraEventAdapter extends RecyclerView.Adapter<MatantaraEventAdapter.EventHolder>{

    private static final String TAG = "AssessmentListAdapter";
    private final SelectEditDelete mListener;
    List<PlaceDetails> jobList;
    private Context mContext;
    private AlertDialog alertDialog;
    private int pos;

    public MatantaraEventAdapter(Context mContext, List<PlaceDetails> jobList) {
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
        final PlaceDetails PlaceDetails = jobList.get(i);
        if(PlaceDetails!=null) {
            if(PlaceDetails.getEventName()!=null)
            {
                currentaffairsViewHolder.title.setText(PlaceDetails.getEventName());
            }
            if(PlaceDetails.getName()!=null){
                currentaffairsViewHolder.place.setText(PlaceDetails.getName());
            }

         //   currentaffairsViewHolder.imageViewEdit.setVisibility(View.GONE);
            currentaffairsViewHolder.imageViewEdit.setImageResource(R.drawable.ic_check);
            currentaffairsViewHolder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener!=null)
                    {
                        mListener.selectedEditPosition(PlaceDetails);
                    }
                }
            });
            currentaffairsViewHolder.imageViewDelete.setImageResource(R.drawable.ic_gps);
            currentaffairsViewHolder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener!=null)
                    {
                       mListener.selectedDeletePosition(PlaceDetails);

                    }
                }
            });

            /*
            if(PlaceDetails.getContactPerson()!=null)
                currentaffairsViewHolder.name.setText(PlaceDetails.getContactPerson());
            else
                currentaffairsViewHolder.name.setText("NA");
            if(PlaceDetails.getContactNumber()!=null)
                currentaffairsViewHolder.phone.setText(PlaceDetails.getContactNumber());
            else
                currentaffairsViewHolder.phone.setText("NA");

            if(PlaceDetails.getImageURL()!=null)
            {
                currentaffairsViewHolder.imageViewAttachment.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(PlaceDetails.getImageURL()).into(currentaffairsViewHolder.imageViewAttachment);
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
                    Log.d(TAG, "onClick: "+PlaceDetails.getImageURL());
                    Glide.with(mContext).load(PlaceDetails.getImageURL()).into(imageViewAttachment);
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
            */
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
        void selectedEditPosition(PlaceDetails position);
        void selectedDeletePosition(PlaceDetails position);
    }
}
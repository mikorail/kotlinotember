package com.example.notembernew.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.notembernew.R;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context){
        this.context = context;
    }

    public int[] slide_background={
            R.drawable.slider1,
            R.drawable.slider2,
            R.drawable.slider3
    };

    public String[] slide_header={
        "Eficiency",
        "Faster",
            "Comfortable"
    };

    public String[] slide_desc={
            "bla bla bla",
            "bla bla bla",
            "bla bla bla"
    };
    @Override
    public int getCount() {
        return slide_header.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (ConstraintLayout) o;
    }

    public Object instantiateItem(ViewGroup container,int position){
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout,container,false);

        ImageView slideBackground = (ImageView) view.findViewById(R.id.slide_background);
        TextView slideHeader = (TextView) view.findViewById(R.id.slide_header);
        TextView slideDesc = (TextView) view.findViewById(R.id.slide_desc);

        slideBackground.setImageResource(slide_background[position]);
        slideHeader.setText(slide_header[position]);
        slideDesc.setText(slide_desc[position]);

        container.addView(view);

        return view;
    }

    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((ConstraintLayout)object);
    }
}

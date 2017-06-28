package luismelendezcom.weatherfab;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class WeatherRecyclerviewAdapter extends RecyclerView.Adapter<WeatherRecyclerviewAdapter.ForecastHolder> {

    ArrayList<Weather> forecasts;
    Context context;

    public WeatherRecyclerviewAdapter(Context context, ArrayList<Weather> forecastList) {
        this.context =context;
        this.forecasts = forecastList;
    }

    @Override
    public ForecastHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ForecastHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ForecastHolder holder, int position) {
        Weather day= forecasts.get(position);
        holder.dayTextView.setText(context.getString(R.string.day_description,day.dayOfWeek,day.description));
        holder.lowTempTextView.setText(context.getString(R.string.low_temp,day.minTemp));
        holder.hiTempTextView.setText(context.getString(R.string.high_temp,day.maxTemp));
        holder.humidityTextView.setText(context.getString(R.string.humidity,day.humidity));
        Picasso.with(context).load(day.iconURL).into(holder.conditionImageView);

    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    public void swapWeatherList(ArrayList<Weather> forecasts){
      this.forecasts =forecasts;
      notifyDataSetChanged();

    }


    public class ForecastHolder  extends RecyclerView.ViewHolder{

        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTempTextView;
        TextView hiTempTextView;
        TextView humidityTextView;

        public ForecastHolder(View itemView) {
            super(itemView);
            conditionImageView = (ImageView)itemView.findViewById(R.id.conditionImageView);
            dayTextView =(TextView)itemView.findViewById(R.id.dayTextView);
            lowTempTextView =(TextView)itemView.findViewById(R.id.lowTextView);
            hiTempTextView =(TextView)itemView.findViewById(R.id.hiTextView);
            humidityTextView =(TextView)itemView.findViewById(R.id.humidityTextView);
        }
    }
}

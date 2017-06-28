package luismelendezcom.weatherfab;

import org.json.JSONObject;


public class JsonEvent {

  private JSONObject forecast;

   public JsonEvent(JSONObject forecast){
       this.forecast= forecast;

   }

    public JSONObject getForecast() {
        return forecast;
    }
}

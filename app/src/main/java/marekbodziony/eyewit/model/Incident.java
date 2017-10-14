package marekbodziony.eyewit.model;

/**
 * Created by MBodziony on 2017-10-14.
 */

public class Incident {

    private  String lat = "00";
    private  String lon = "00";
    private long time = 0;
    private String videoURL;

    public Incident(){}

    public Incident(String lat, String lon, long time) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
    }

    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }
    public String getLon() {
        return lon;
    }
    public void setLon(String lon) {
        this.lon = lon;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public String getVideoURL() {
        return videoURL;
    }
    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }
}

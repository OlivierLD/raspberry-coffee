package tideengine.contracts;

import tideengine.Constituents;
import tideengine.Stations;
import tideengine.TideStation;

import java.util.Map;

public interface BackendDataComputer {

    void connect();
    void disconnect();
    Map<String, TideStation> getStationData() throws Exception;
    Stations getTideStations() throws Exception;
    Constituents buildConstituents() throws Exception;
    TideStation reloadOneStation(String stationName) throws Exception;
    void setVerbose(boolean b);

}

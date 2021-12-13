package tideengine.contracts;

import tideengine.Constituents;
import tideengine.Stations;
import tideengine.TideStation;

import java.util.Map;

/**
 * This interface is common to all the implementation of the data store that is used
 * to compute the coefficients and tide stations.
 */
public interface BackendDataComputer {

    void connect() throws Exception;
    void disconnect() throws Exception;
    Map<String, TideStation> getStationData() throws Exception;
    Stations getTideStations() throws Exception;
    Constituents buildConstituents() throws Exception;
    TideStation reloadOneStation(String stationName) throws Exception;
    void setVerbose(boolean b);

}

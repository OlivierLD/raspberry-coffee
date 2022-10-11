package tideengine.contracts;

import tideengine.Constituents;
import tideengine.Stations;
import tideengine.TideStation;

import java.util.Map;

/**
 * This interface is common to all the implementations of the data store that is used
 * to compute the coefficients and tide stations.
 */
public interface BackendDataComputer {

    void connect() throws Exception;
    void disconnect() throws Exception;
    Map<String, TideStation> getStationData() throws Exception;
    Stations getTideStations(boolean verbose) throws Exception;
    Constituents buildConstituents(boolean verbose) throws Exception;
    TideStation reloadOneStation(String stationName) throws Exception;
    void setVerbose(boolean b);

}

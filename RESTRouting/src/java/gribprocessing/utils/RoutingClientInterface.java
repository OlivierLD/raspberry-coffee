package gribprocessing.utils;

import java.util.List;

public interface RoutingClientInterface
{
  public void routingNotification(List<List<RoutingPoint>> all, RoutingPoint closest);

}

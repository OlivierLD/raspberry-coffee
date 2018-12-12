var set = function(device, state)
{
  request(device, state);
};

var SERVICE_ROOT = "/relay-access";

var request = function(pin, status)
{
  var restRequest = SERVICE_ROOT + "?dev=" + pin + "&status=" + status;
  var ajax = new XMLHttpRequest();
  ajax.open("GET", restRequest, false);
  ajax.send(null);
};

# Requires a
# $ pip install requests
# $ pip install json
# $ pip install pyyaml
#
# Good requests 101-tutorial at https://realpython.com/api-integration-in-python/
#

import sys
import requests
import json
import yaml

DEFAULT_AGENT_PROP_FILE_NAME = "agent.yaml"

DEFAULT_HOST_NAME = "localhost"
DEFAULT_PROTOCOL = "http"
DEFAULT_HOST_PORT = "9990"

ROOT_PATH = "/agent"

CONFIG_PATH = "/config"
STATUS_PATH = "/status"
START_PATH = "/start"
STOP_PATH = "/stop"
DESTROY_PATH = "/destroy"

host_name = DEFAULT_HOST_NAME
protocol = DEFAULT_PROTOCOL
host_port = DEFAULT_HOST_PORT

# For the agent config, default values
AGENT_SERVICE_URL = "http://localhost:8060"
AGENT_MESSAGE_PATH = "/agent/transport"
AGENT_TENANT_NAME = "demo"
AGENT_NAMESPACE = "AGENT1"
AGENT_APIGW_INPUT_PATH = "/apigw/input/"
AGENT_MAX_POLLING_THREADS = "1"
AGENT_POLLING_INTERVAL = "60000"
AGENT_THREAD_POOL_SIZE = "10"
AGENT_SECURITY_CONFIG_FILE_PATH = "~/.oci/config"
AGENT_SECURITY_PROFILE = "DEFAULT"

# Actual values
agent_service_URL = AGENT_SERVICE_URL
agent_message_path = AGENT_MESSAGE_PATH
agent_tenant_name = AGENT_TENANT_NAME
agent_name_space = AGENT_NAMESPACE
agent_api_gw_input_path = AGENT_APIGW_INPUT_PATH
agent_max_polling_threads = AGENT_MAX_POLLING_THREADS
agent_polling_interval = AGENT_POLLING_INTERVAL
agent_thread_pool_size = AGENT_THREAD_POOL_SIZE
agent_security_config_path = AGENT_SECURITY_CONFIG_FILE_PATH
agent_security_profile = AGENT_SECURITY_PROFILE


def display_help():
    print("Commands (not case-sensitive) are:")
    print("- Q | Quit | Exit")
    print("- Help")
    print("- Status")
    print("- Start")
    print("- Stop")
    print("- Destroy")


def get_agent_status():
    uri = "{}://{}:{}{}{}".format(protocol, host_name, host_port, ROOT_PATH, STATUS_PATH)
    print("Using {}".format(uri))
    resp = requests.get(uri)
    if resp.status_code != 200:
        raise Exception('GET /tasks/ {}'.format(resp.status_code))
    else:
        json_obj = json.loads(resp.content)
        # print(json.dumps(json_obj, indent=2))
        print('Status {}\nReceived {}'.format(resp.status_code, json.dumps(json_obj, indent=2)))


def config_agent():
    uri = "{}://{}:{}{}{}".format(protocol, host_name, host_port, ROOT_PATH, CONFIG_PATH)
    print("Using {}".format(uri))
    payload = {}
    resp = requests.post(uri,
                         json=payload,
                         headers={
                             "agentServiceURL": agent_service_URL,
                             "agentMessagePath": agent_message_path,
                             "tenantName": agent_tenant_name,
                             "nameSpace": agent_name_space,
                             "apigwInputPath": agent_api_gw_input_path,
                             "maxPollingThreads": agent_max_polling_threads,
                             "pollingInterval": agent_polling_interval,
                             "threadPoolSize": agent_thread_pool_size,
                             "credentialConfigFilePath": agent_security_config_path,
                             "credentialProfile": agent_security_profile})
    if resp.status_code != 201:
        raise Exception('POST /config/ {}'.format(resp.status_code))
    print('Status {}, Content {}'.format(resp.status_code, json.dumps(json.loads(resp.content), indent=2)))


def start_agent():
    try:
        # Configure first
        config_agent()
        # Then move on
        uri = "{}://{}:{}{}{}".format(protocol, host_name, host_port, ROOT_PATH, START_PATH)
        print("Using {}".format(uri))
        payload = {}
        resp = requests.post(uri, json=payload)
        if resp.status_code != 201:
            raise Exception('POST /start/ {}'.format(resp.status_code))
        print('Status {}, Content {}'.format(resp.status_code, json.dumps(json.loads(resp.content), indent=2)))
    except Exception as api_error:
        print("- Error {}".format(api_error))


def stop_agent():
    uri = "{}://{}:{}{}{}".format(protocol, host_name, host_port, ROOT_PATH, STOP_PATH)
    print("Using {}".format(uri))
    payload = {}
    resp = requests.post(uri, json=payload)
    if resp.status_code != 201:
        raise Exception('POST /stop/ {}'.format(resp.status_code))
    print('Status {}, Content {}'.format(resp.status_code, json.dumps(json.loads(resp.content), indent=2)))


def destroy_agent():
    uri = "{}://{}:{}{}{}".format(protocol, host_name, host_port, ROOT_PATH, DESTROY_PATH)
    print("Using {}".format(uri))
    payload = {}
    resp = requests.post(uri, json=payload)
    if resp.status_code != 201:
        raise Exception('POST /destroy/ {}'.format(resp.status_code))
    print('Status {}, Content {}'.format(resp.status_code, json.dumps(json.loads(resp.content), indent=2)))


#
# Script main part
#

# Read config file values
PROP_ARG_PREFIX = '--prop:'

prop_file_name = DEFAULT_AGENT_PROP_FILE_NAME
for arg in sys.argv:
    if arg[:len(PROP_ARG_PREFIX)] == PROP_ARG_PREFIX:
        prop_file_name = arg[len(PROP_ARG_PREFIX):]

print("Reading {}".format(prop_file_name))
try:
    with open(prop_file_name) as prop_file:
        yaml_props = yaml.load(prop_file)
        # print(yaml_props)
        for key in yaml_props.keys():
            # print("Key {}".format(key))
            if key == 'lcm-service':
                lcm_map = yaml_props.get(key)
                for lcm_k in lcm_map:
                    print("\t=>{}: {}".format(lcm_k, lcm_map.get(lcm_k)))
                    if lcm_k == 'protocol':
                        protocol = lcm_map.get(lcm_k)
                    elif lcm_k == 'hostname':
                        host_name = lcm_map.get(lcm_k)
                    elif lcm_k == 'hostport':
                        host_port = lcm_map.get(lcm_k)
            elif key == 'serviceurl':
                agent_service_URL = yaml_props.get(key)
                print("agent_service_URL: {}".format(agent_service_URL))
            elif key == 'tenantname':
                agent_tenant_name = yaml_props.get(key)
                print("agent_tenant_name: {}".format(agent_tenant_name))
            elif key == 'namespace':
                agent_name_space = yaml_props.get(key)
                print("agent_name_space: {}".format(agent_name_space))
            elif key == 'agentmessagepath':
                agent_message_path = yaml_props.get(key)
                print("agent_message_path: {}".format(agent_message_path))
            elif key == 'apigwinputpath':
                agent_api_gw_input_path = yaml_props.get(key)
                print("agent_api_gw_input_path: {}".format(agent_api_gw_input_path))
            elif key == 'security':
                sec_map = yaml_props.get(key)
                for sec_k in sec_map:
                    print("\t=>{}: {}".format(sec_k, sec_map.get(sec_k)))
                    if lcm_k == 'configfilepath':
                        agent_security_config_path = sec_map.get(sec_k)
                    elif lcm_k == 'profile':
                        agent_security_profile = sec_map.get(sec_k)
            elif key == 'taskscheduler':
                ts_map = yaml_props.get(key)
                for ts_k in ts_map:
                    print("\t=>{}: {}".format(ts_k, ts_map.get(ts_k)))
                    if lcm_k == 'maxpollingthreads':
                        agent_max_polling_threads = str(ts_map.get(ts_k))
                    elif lcm_k == 'pollinginterval':
                        agent_polling_interval = str(ts_map.get(ts_k))
                    elif lcm_k == 'threadpoolsize':
                        agent_thread_pool_size = str(ts_map.get(ts_k))

except FileNotFoundError as fnf_error:
    print("- Prop File {} Not Found, {}, aborting".format(prop_file_name, fnf_error))
    sys.exit(1)

display_help()
keep_looping = True
while keep_looping:
    user_input = input("> ")
    if user_input.upper() == 'Q' or user_input.upper() == 'QUIT' or user_input.upper() == 'EXIT':
        keep_looping = False
    elif user_input.upper() == 'HELP':
        display_help()
    elif user_input.upper() == 'START':
        start_agent()
    elif user_input.upper() == 'STOP':
        stop_agent()
    elif user_input.upper() == 'DESTROY':
        destroy_agent()
    elif user_input.upper() == 'STATUS':
        get_agent_status()
    else:
        print("Duh? {}".format(user_input))

print("Bye!")

import yaml
import json

with open('sample.yaml') as props:
    the_dict = yaml.safe_load(props)
    print(f"Obtained a {type(the_dict)}")
    print(f"From YAML: {json.dumps(the_dict)}")  # Use json.dumps, for double quotes...

# ty.py
# For python3
# Use to process HypersonicSQL scripts to sqlite.
# Redirect stdout into a file.
#
import sys

file_name = 'populate_tables.sql'
if len(sys.argv) > 1:
    file_name = sys.argv[1]

print(f"Processing {file_name}...")

proceed: bool = False
try:
    file1 = open(file_name, 'r')
    proceed = True
except Exception as ex:
    print(f"Error: {ex}")
    proceed = False

if proceed:
    Lines = file1.readlines()

    count = 0
    # Strips the newline character
    for line in Lines:
        count += 1
        if line.startswith('select "Building '):  # and not line.strip().endswith("';"):
            if (line.strip().endswith("';")):
                # print("replacing ")
                # print('{}";'.format(line.strip()[0:len(line.strip()) - 2]))
                print(f'{line.strip()[0:len(line.strip()) - 2]}";')
            else:
                # print("Found [{}]".format(line.strip()))
                # print('{}";'.format(line.strip()))
                print(f'{line.strip()}";')
        else:
           ## print("Line #{}: {}".format(count, line.strip()))
           # print(f"Line #{count}: {line.strip()}")
           # print("{}".format(line.strip()))
           print(f"{line.strip()}")

    file1.close()

print("Bye!")

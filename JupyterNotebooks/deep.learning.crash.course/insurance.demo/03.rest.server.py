from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import tensorflow as tf
import numpy as np
import sys

sample_data = {"1": "First", "2": "Second", "3": "Third", "4": "Fourth"}

tf_version = tf.__version__
print("------------------------------------")
print("TensorFlow version", tf_version)
print("Keras version", tf.keras.__version__)
print("------------------------------------")

coded_colors = ["red", "green", "yellow"]


class CoreFeatures:

	model_name = 'insurance.h5'
	model = None

	def get_model_name(self):
		return self.model_name

	def set_model_name(self, name):
		print("Setting model name to {}".format(name))
		self.model_name = name

	def set_model(self, _model):
		self.model = _model

	def get_model(self):
		return self.model

	def load_or_reload_model(self):
		try:
			# tf.keras.backend.clear_session()
			print("\t\tLoading model {}".format(self.model_name))
			_model = tf.keras.models.load_model(self.model_name)
			print(">> Model is now loaded")
			# model._make_predict_function()
			_model.summary()
			return _model
		except OSError as ose:
			raise OSError('Model {} not found'.format(self.model_name))


core = CoreFeatures()

try:
	core.set_model(core.load_or_reload_model())
except OSError as ose:
	print('Model not found?')
	print(ose)
	sys.exit(1)  # Bam!


# Defining a HTTP request Handler class
class ServiceHandler(BaseHTTPRequestHandler):
	# sets basic headers for the server
	def _set_headers(self):
		self.send_response(200)
		self.send_header('Content-type', 'application/json')
		# reads the length of the Headers
		length = int(self.headers['Content-Length'])
		# reads the contents of the request
		content = self.rfile.read(length)
		temp = str(content).strip('b\'')
		self.end_headers()
		return temp

	# GET Method Definition
	def do_GET(self):
		print("GET method")
		# defining all the headers
		self.send_response(200)
		self.send_header('Content-type', 'application/json')
		self.end_headers()
		#
		full_path = self.path
		splitted = full_path.split('?')
		path = splitted[0]
		qs = None
		if len(splitted) > 1:
			qs = splitted[1]
		# The parameters into a map
		prm_map = {}
		if qs is not None:
			qs_prms = qs.split('&')
			for qs_prm in qs_prms:
				nv_pair = qs_prm.split('=')
				if len(nv_pair) == 2:
					prm_map[nv_pair[0]] = nv_pair[1]
				else:
					print("oops, no equal sign in {}".format(qs_prm))

		if path == "/claim":  # GET /claim?speed=XXX&age=YY&mpy=ZZ
			print("Claim request")
			str_age = '0'
			try:
				str_age = prm_map['age']
			except Exception as exception:
				print("Exception {}, using default value".format(exception))
			str_speed = '0'
			try:
				str_speed = prm_map['speed']
			except Exception as exception:
				print("Exception {}, using default value".format(exception))
			str_mpy = '0'
			try:
				str_mpy = prm_map['mpy']
			except Exception as exception:
				print("Exception {}, using default value".format(exception))

			print("{}, {}, {}".format(str_age, str_speed, str_mpy))

			speed = int(str_speed)
			age = int(str_age)
			mpy = int(str_mpy)
			print("{}, {}, {}".format(type(speed), type(age), type(mpy)))

			try:
				input_data = np.array([[speed, age, mpy]])
				print("Input data shape: {}".format(input_data.shape))
				core.get_model().summary()  # Quick sanity check

				prediction = core.get_model().predict(input_data)

				print('predictions shape:', prediction.shape)
				print("Prediction {}".format(prediction))
				final_color = None
				probability = 0
				for pred in prediction:
					print("{}, max's index {}, {:0.2f}%".format(pred, np.argmax(pred), pred[np.argmax(pred)] * 100))
					print(
						"Final result: {}, {:0.2f}%".format(coded_colors[np.argmax(pred)], pred[np.argmax(pred)] * 100))
					final_color = coded_colors[np.argmax(pred)]
					probability = pred[np.argmax(pred)] * 100
					print("Predicted result {}, {:0.2f}%".format(final_color, probability))
				final_payload = {"risk": final_color, "probability": probability}
				self.wfile.write(json.dumps(final_payload).encode())
			except Exception as exception:
				error = {"message": "{}".format(exception)}
				self.send_response(500)
				self.wfile.write(json.dumps(error).encode())
		elif path == "/current-model":
			current_model = {"name": core.get_model_name()}
			self.send_response(200)
			self.wfile.write(json.dumps(current_model).encode())
		else:
			# prints all the keys and values of the json file
			self.wfile.write(json.dumps(sample_data).encode())

	# VIEW method definition
	def do_VIEW(self):
		# dict var. for pretty print
		display = {}
		temp = self._set_headers()
		# check if the key is present in the dictionary
		if temp in sample_data:
			display[temp] = sample_data[temp]
			# print the keys required from the json file
			self.wfile.write(json.dumps(display).encode())
		else:
			error = "NOT FOUND!"
			self.wfile.write(bytes(error, 'utf-8'))
			self.send_response(404)

	# POST method definition
	def do_POST(self):
		print("POST request, {}".format(self.path))
		if self.path.startswith("/model/"):  # /POST model/{model-name}
			new_model_name = self.path[len("/model/"):]
			print("Will load new model: {}".format(new_model_name))
			core.set_model_name(new_model_name)
			self.send_response(201)
			response = {"status": "OK"}
			self.wfile.write(json.dumps(response).encode())
		else:
			temp = self._set_headers()
			key = 0
			# getting key and value of the data dictionary
			for key, value in sample_data.items():
				pass
			index = int(key) + 1
			sample_data[str(index)] = str(temp)
			# write the changes to the json file
			with open("db.json", 'w+') as file_data:
				json.dump(sample_data, file_data)
	# self.wfile.write(json.dumps(data[str(index)]).encode())

	# PUT method Definition
	def do_PUT(self):
		print("PUT request, {}".format(self.path))
		if self.path == "/reload-model":  # PUT /reload-model
			print("Will reload {}".format(core.get_model_name()))
			self.send_response(201)
			response = {"status": "OK"}
			try:
				core.set_model(core.load_or_reload_model())
			except OSError as ose:
				self.send_response(404)
				response = {"status": "Model not found"}
			self.wfile.write(json.dumps(response).encode())
		elif self.path.startswith("/reload-model/"):  # PUT /reload-model/{model-name}
			new_model_name = self.path[len("/reload-model/"):]
			print("Will load new model: {}".format(new_model_name))
			core.set_model_name(new_model_name)
			self.send_response(201)
			response = {"status": "OK"}
			try:
				core.set_model(core.load_or_reload_model())
			except OSError as ose:
				self.send_response(404)
				response = {"status": "Model not found"}
			self.wfile.write(json.dumps(response).encode())
		else:
			temp = self._set_headers()
			# separating input into key and value
			x = temp[:1]
			y = temp[2:]
			# check if key is in data
			if x in sample_data:
				sample_data[x] = y
				# write the changes to file
				with open("db.json", 'w+') as file_data:
					json.dump(sample_data, file_data)
			# self.wfile.write(json.dumps(data[str(x)]).encode())
			else:
				error = "NOT FOUND!"
				self.wfile.write(bytes(error, 'utf-8'))
				self.send_response(404)

	# DELETE method definition
	def do_DELETE(self):
		temp = self._set_headers()
		# check if the key is present in the dictionary
		if temp in sample_data:
			del sample_data[temp]
			# write the changes to json file
			with open("db.json", 'w+') as file_data:
				json.dump(sample_data, file_data)
		else:
			error = "NOT FOUND!"
			self.wfile.write(bytes(error, 'utf-8'))
			self.send_response(404)


# Server Initialization
port = 8080
print("Starting server on port {}".format(port))
server = HTTPServer(('127.0.0.1', port), ServiceHandler)
server.serve_forever()

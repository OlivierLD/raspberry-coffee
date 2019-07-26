The goal here is to do in Java all what's done in Python.

Java seems to be lagging a bit behind...

With DL4J, the `KerasImport` raises a 
```
 Unsupported keras layer type BatchNormalizationV1
```

To load a model with `SavedModelBundle.load()`, it needs to have tags (visible with `saved_model_cli`).
Otherwise, use:
```java
    Path modelPath = Paths.get(MODEL_LOCATION);
    byte[] graph = Files.readAllBytes(modelPath);

    try (Graph g = new Graph()) {
        g.importGraphDef(graph);
        ...
``` 

---

- Good article on TensorFlow for [Python & Java](https://medium.com/@alexkn15/tensorflow-save-model-for-use-in-java-or-c-ab351a708ee4)
- Good too: <https://www.baeldung.com/tensorflow-java>
- TensorFlow, Java compared to Python (with the differences) <https://stackabuse.com/how-to-use-tensorflow-with-java/>

### Cool tools
- `saved_model_cli`, to know what tag to use in `SavedModelBundle.load()`.
```
 $ saved_model_cli show --dir ./model
```
- `tensorboard`
    - in Python, when creating the `graph`, use:
```python
writer = tf.summary.FileWriter('.')
writer.add_graph(tf.get_default_graph())
writer.flush()
```
then from the command line:
```
 $ tensorboard --logdir .
```
and reach <http://localhost:6006/> from your browser.

---

### Questions

- Java can generate a TensorFlow Graph, and use it in a Session. **Can you save it?**
    - Not yet (Jul-2019)...
- Can `Keras` generate ProtoBuf (`.pb`) models (consumable by Java)?
    - See this: 
        - <https://medium.com/@johnsondsouza23/export-keras-model-to-protobuf-for-tensorflow-serving-101ad6c65142>
        - <https://www.dlology.com/blog/how-to-convert-trained-keras-model-to-tensorflow-and-make-prediction/>

---

## 2 different approaches
- [DL4J](https://deeplearning4j.org/)
- [TensorFlow for Java](https://www.tensorflow.org/install/lang_java)

### For TensorFlow in Java
Do follow instructions [here](https://www.tensorflow.org/install/lang_java#download) (JNI settings).

---

Requires the following dependencies (in `build.gradle`):
```
  compile 'org.tensorflow:tensorflow:1.13.1'
  compile 'org.deeplearning4j:deeplearning4j-modelimport:1.0.0-beta4'
  compile 'org.nd4j:nd4j-native-platform:1.0.0-beta4'
```

`ND4J` is a Java library similar to the Python's `NumPy`.


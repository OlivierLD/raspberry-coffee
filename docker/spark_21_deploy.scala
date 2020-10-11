/* # Loading and Using Machine Learning Pipelines in Scala */

/* Copyright © 2010–2020 Cloudera. All rights reserved.
   Not to be reproduced or shared without prior written
   consent from Cloudera. */


/*
## Overview
*/

/*
In this module we demonstrate how to load and use the pipeline model object
that we saved in our Python session.  In addition, we give you a glimpse of the
Spark Scala API.  You will find that it looks quite similar to the Python API.
*/

/*
**Note**: // comments do not render Markdown.
*/


/*
## Create a SparkSession
*/

import org.apache.spark.sql.SparkSession
val spark = SparkSession.builder.appName("deploy").getOrCreate()

/*
**Note**:  We do not have to create a SparkSession in a Scala session because
it is automatically created for us; however, doing so will allow us to the run
this script as a standalone Spark application.
*/
spark

/*
Note that Spark is running in yarn client mode by default:
*/
spark.conf.get("spark.master")
spark.conf.get("spark.submit.deployMode")


/*
## Load the data
*/

/*
Let us read the enhanced ride data from HDFS:
*/
val rides = spark.read.parquet("/duocar/joined_all/")

/*
**Note:** The Spark keyword `val` indicates that `rides` is an immutable
object.
*/


/*
## Load a PipelineModel object
*/

/*
If we simply want to apply our existing classifier to new data, then we load
and use our PipelineModel instance:
*/
import org.apache.spark.ml.PipelineModel
val pipelineModel = PipelineModel.load("models/pipeline_model")

/*
## Inspect the pipeline model
*/
/*
Extract the hyperparameter tuning model:
*/
import org.apache.spark.ml.tuning.TrainValidationSplitModel
val tvsModel = pipelineModel.stages(5).asInstanceOf[TrainValidationSplitModel]

/*
Extract the random forest classification model:
*/
import org.apache.spark.ml.classification.RandomForestClassificationModel
val rfModel = tvsModel.bestModel.asInstanceOf[RandomForestClassificationModel]

/*
Examine the hyperparameters:
*/
rfModel.getMaxDepth
rfModel.getSubsamplingRate
rfModel.getNumTrees


/*
## Apply a PipelineModel object
*/

/*
Use the `transform` method to apply the pipeline model to our new DataFrame:
*/
val classified = pipelineModel.transform(rides)

/*
**Important:** The input DataFrame must include the required columns:
*/
classified.printSchema()


/*
## Evaluate the pipeline model:
*/

/*
Use the `persist` method to cache our classified DataFrame in (worker) memory:
*/
classified.persist()

/*
Verify that the cancelled rides have been removed:
*/
classified.groupBy("cancelled").count().show()

/*
Generate the confusion matrix:
*/
classified.
  groupBy("prediction").
  pivot("star_rating").
  count().
  orderBy("prediction").
  na.fill(0).
  show()

/*
**Note**: Scala does not provide a `crosstab` method, so we use the `pivot`
method instead.
*/

/*
**Note**: We must put the dots at the end of the line rather than the
beginning of the line.
*/

/*
Compute the classifier accuracy using the `MulticlassClassificationEvaluator`:
*/
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
val evaluator = new MulticlassClassificationEvaluator().
  setPredictionCol("prediction").
  setLabelCol("star_rating").
  setMetricName("accuracy")
evaluator.evaluate(classified)

/*
Unpersist the DataFrame:
*/
classified.unpersist()


/*
## Exercises

None
*/


/*
## References

[Spark Scala API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.package)
*/


/*
## Stop the SparkSession
*/
spark.stop()

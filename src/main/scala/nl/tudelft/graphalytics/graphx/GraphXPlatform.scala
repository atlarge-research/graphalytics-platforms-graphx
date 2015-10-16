/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.graphx

import nl.tudelft.graphalytics.reporting.granula.GranulaManager
import nl.tudelft.graphalytics.reporting.logging.{GraphalyticLogger, GraphXLogger}
import nl.tudelft.graphalytics.{PlatformExecutionException, Platform}
import nl.tudelft.graphalytics.domain._
import nl.tudelft.pds.granula.modeller.graphx.job.GraphX
import nl.tudelft.pds.granula.modeller.model.Model
import nl.tudelft.pds.granula.modeller.model.job.JobModel
import org.apache.commons.configuration.{ConfigurationException, PropertiesConfiguration}
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import nl.tudelft.graphalytics.graphx.bfs.BreadthFirstSearchJob
import nl.tudelft.graphalytics.graphx.cd.CommunityDetectionJob
import nl.tudelft.graphalytics.graphx.conn.ConnectedComponentsJob
import nl.tudelft.graphalytics.graphx.evo.ForestFireModelJob
import nl.tudelft.graphalytics.graphx.stats.LocalClusteringCoefficientJob
import org.apache.hadoop.yarn.client.api.YarnClient
import org.apache.hadoop.yarn.client.cli.LogsCLI
import org.apache.hadoop.yarn.conf.YarnConfiguration

/**
 * Constants for GraphXPlatform
 */
object GraphXPlatform {
	val HDFS_DIRECTORY_KEY = "hadoop.hdfs.directory"
	val HDFS_DIRECTORY = "graphalytics"

	val CONFIG_PATH = "graphx.properties"
	val CONFIG_JOB_NUM_EXECUTORS = "graphx.job.num-executors"
	val CONFIG_JOB_EXECUTOR_MEMORY = "graphx.job.executor-memory"
	val CONFIG_JOB_EXECUTOR_CORES = "graphx.job.executor-cores"
}

/**
 * Graphalytics Platform implementation for GraphX. Manages the datasets on HDFS and launches the appropriate
 * GraphX jobs.
 */
class GraphXPlatform extends Platform {
	import GraphXPlatform._

	var pathsOfGraphs : Map[String, String] = Map()

	/* Parse the GraphX configuration file */
	val config = Properties.fromFile(CONFIG_PATH).getOrElse(Properties.empty())
	System.setProperty("spark.executor.cores", config.getString(CONFIG_JOB_EXECUTOR_CORES).getOrElse("1"))
	System.setProperty("spark.executor.memory", config.getString(CONFIG_JOB_EXECUTOR_MEMORY).getOrElse("2g"))
	System.setProperty("spark.executor.instances", config.getString(CONFIG_JOB_NUM_EXECUTORS).getOrElse("1"))

	val hdfsDirectory = config.getString(HDFS_DIRECTORY_KEY).getOrElse(HDFS_DIRECTORY)

	def uploadGraph(graph : Graph, filePath : String) = {
		val localPath = new Path(filePath)
		val hdfsPath = new Path(s"$hdfsDirectory/${getName}/input/${graph.getName}")

		val fs = FileSystem.get(new Configuration())
		fs.copyFromLocalFile(localPath, hdfsPath)
		fs.close()

			pathsOfGraphs += (graph.getName -> hdfsPath.toUri.getPath)
	}

	def preBenchmark(benchmark : Benchmark) : Unit = {

		GraphXLogger.stopCoreLogging
		if(GranulaManager.isLoggingEnabled) {
			val logDataPath = benchmark.getLogPath()
			GraphXLogger.startPlatformLogging(logDataPath + "/OperationLog/driver.logs")
		}
	}

	def postBenchmark(benchmark : Benchmark) : Unit = {
		if(GranulaManager.isLoggingEnabled) {
			val logDataPath = benchmark.getLogPath
			GraphXLogger.stopPlatformLogging
			GraphXLogger.collectYarnLogs(logDataPath)
		}
		GraphXLogger.startCoreLogging
	}


	def executeAlgorithmOnGraph(benchmark : Benchmark) : PlatformBenchmarkResult = {
		val algorithmType = benchmark.getAlgorithm()
		val graph = benchmark.getGraph()
		val parameters = benchmark.getAlgorithmParameters()

		try  {

			val path = pathsOfGraphs(graph.getName)
			val outPath = s"$hdfsDirectory/${getName}/output/${algorithmType.name}-${graph.getName}"
			val format = graph.getGraphFormat
			
			val job = algorithmType match {
				case Algorithm.BFS => new BreadthFirstSearchJob(path, format, outPath, parameters)
				case Algorithm.CD => new CommunityDetectionJob(path, format, outPath, parameters)
				case Algorithm.CONN => new ConnectedComponentsJob(path, format, outPath)
				case Algorithm.EVO => new ForestFireModelJob(path, format, outPath, parameters)
				case Algorithm.STATS => new LocalClusteringCoefficientJob(path, format, outPath)
				case x => throw new IllegalArgumentException(s"Invalid algorithm type: $x")
			}


			if (job.hasValidInput) {
				job.runJob
				// TODO: After executing the job, any intermediate and output data should be
				// verified and/or cleaned up. This should preferably be configurable.

				new PlatformBenchmarkResult(NestedConfiguration.empty())
			} else {
				throw new IllegalArgumentException("Invalid parameters for job")
			}
		} catch {
			case e : Exception => throw new PlatformExecutionException("GraphX job failed with exception: ", e)
		}
	}

	def deleteGraph(graphName : String) = {
		// TODO: Delete graph data from HDFS to clean up. This should preferably be configurable.
	}

	def getName() : String = "graphx"

	def getPlatformConfiguration: NestedConfiguration =
		try {
			val configuration: PropertiesConfiguration = new PropertiesConfiguration("graphx.properties")
			NestedConfiguration.fromExternalConfiguration(configuration, "graphx.properties")
		}
		catch {
			case ex: ConfigurationException => NestedConfiguration.empty
		}

	def getGranulaModel: JobModel = new GraphX;
}

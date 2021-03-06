/*
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
package science.atlarge.graphalytics.graphx.cdlp

import java.util

import science.atlarge.graphalytics.domain.algorithms.CommunityDetectionLPParameters
import science.atlarge.graphalytics.graphx.{GraphXJobTest, ValidationGraphUtils}
import science.atlarge.graphalytics.validation.GraphStructure
import science.atlarge.graphalytics.validation.algorithms.cdlp.{CommunityDetectionLPOutput, CommunityDetectionLPValidationTest}
import science.atlarge.graphalytics.domain.algorithms.CommunityDetectionLPParameters
import science.atlarge.graphalytics.graphx.{GraphXJobTest, ValidationGraphUtils}
import science.atlarge.graphalytics.validation.GraphStructure
import science.atlarge.graphalytics.validation.algorithms.cdlp.{CommunityDetectionLPOutput, CommunityDetectionLPValidationTest}

/**
 * Integration test for Community Detection job on GraphX.
 *
 * @author Tim Hegeman
 */
class CommunityDetectionLPJobTest extends CommunityDetectionLPValidationTest with GraphXJobTest {

	override def executeDirectedCommunityDetection(graph : GraphStructure, parameters : CommunityDetectionLPParameters)
	: CommunityDetectionLPOutput = {
		val (vertexData, edgeData) = ValidationGraphUtils.directedValidationGraphToVertexEdgeList(graph)
		executeCommunityDetection(vertexData, edgeData, true, parameters)
	}

	override def executeUndirectedCommunityDetection(graph : GraphStructure, parameters : CommunityDetectionLPParameters)
	: CommunityDetectionLPOutput = {
		val (vertexData, edgeData) = ValidationGraphUtils.undirectedValidationGraphToVertexEdgeList(graph)
		executeCommunityDetection(vertexData, edgeData, false, parameters)
	}

	private def executeCommunityDetection(vertexData : List[String], edgeData : List[String], directed : Boolean,
			parameters : CommunityDetectionLPParameters) : CommunityDetectionLPOutput = {
		val cdJob = new CommunityDetectionLPJob("", "", directed, "", parameters)
		val (vertexOutput, _) = executeJob(cdJob, vertexData, edgeData)
		val outputAsJavaMap = new util.HashMap[java.lang.Long, java.lang.Long](vertexOutput.size)
		vertexOutput.foreach { case (vid, value) => outputAsJavaMap.put(vid, value) }
		new CommunityDetectionLPOutput(outputAsJavaMap)
	}

}

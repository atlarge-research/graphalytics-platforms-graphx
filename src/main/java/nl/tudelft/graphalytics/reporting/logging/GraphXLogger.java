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
package nl.tudelft.graphalytics.reporting.logging;

import org.apache.log4j.*;

/**
 * Created by wlngai on 9-9-15.
 */
public class GraphXLogger extends GraphalyticLogger {

    public static void startPlatformLogging(String fileName) {

        System.out.println("Redirecting to Graphx");

        Logger.getRootLogger().removeAllAppenders();
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(fileName);
        fa.setLayout(new PatternLayout("%d [%t] %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.INFO);
        fa.setAppend(true);
        fa.activateOptions();
        Logger.getRootLogger().addAppender(fa);

    }

    public static void startCoreLogging() {
        GraphalyticLogger.startCoreLogging();
    }

    public static void collectYarnLogs(String logDataPath){
        GraphalyticLogger.collectYarnLogs(logDataPath);
    }

    public static void stopCoreLogging(){
        GraphalyticLogger.stopCoreLogging();
    }

    public static void stopPlatformLogging() {
        waitInterval(1);
        Logger.getRootLogger().removeAllAppenders();
    }


}

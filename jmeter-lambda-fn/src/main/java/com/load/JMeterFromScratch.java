package com.load;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.blazemeter.jmeter.RandomCSVDataSetConfig;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


public class JMeterFromScratch extends Base {

    int threads = 200;
    int loops = 100;
    int rampup = 1;
    String url = "sessions-api-go-lt.us-east-1.elasticbeanstalk.com";
    Integer port = -1;
    String path="/";
    String method = "POST";
    String name = "GET_Sessions";
    String protocol ="http";
    String logPath = "src/main/java/Jmeter/example1.jtl";
    String body="{ \"testvalue\" : \"Testing QA 8 Message\" }";
    String queryParams="";
    boolean isQueryParams = false;
    String s3Folder = "temp";
    public void runTest() throws Exception {

        UUID uuid = UUID.randomUUID();
        String threadName = "Thread_" +name+"_"+uuid.toString()+"_"+System.currentTimeMillis();
        stringBuilder = new StringBuilder();
        stringBuilder = new StringBuilder();
        //logsObj.deleteFile(logPath);
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        File jmeterHome = new File("jmeter-lambda-fn/src/main/java/Jmeter/");
        String slash = System.getProperty("file.separator");

        if (jmeterHome.exists()) {
            File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
            if (jmeterProperties.exists()) {
                //JMeter Engine
                StandardJMeterEngine jmeter = new StandardJMeterEngine();

                //JMeter initialization (properties, log levels, locale, etc)
                JMeterUtils.setJMeterHome(jmeterHome.getPath());
                JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
               // JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
                JMeterUtils.initLocale();

                String csvFilePath = "/Users/sjain/Work/trials/jmeter-from-code/src/main/resources/GET_Sessions_temp.csv";
                RandomCSVDataSetConfig csvDataSet = createAndConfigureRandomCsvDataSet(csvFilePath);

                HeaderManager manager = new HeaderManager();
                manager.add(new Header("Content-Type", "application/json"));
                manager.add(new Header("Accept", "application/json"));
                manager.add(new Header("Cache-Control", "no-cache"));
                manager.setName(JMeterUtils.getResString("header_manager_title")); // $NON-NLS-1$
                manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
                manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());

                // Second HTTP Sampler
                HTTPSamplerProxy samplerProxy = createAndConfigureHttpSampler();
//               samplerProxy.setHeaderManager(manager)
                // Loop Controller
                LoopController loopController = createAndConfigureLoopController(samplerProxy);
                // Thread Group
                ThreadGroup threadGroup = createAndConfigureThreadGroup(loopController, threadName);


                // Test Plan
                TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
                testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
                testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
                testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());


                HashTree mainTree = new ListedHashTree();

                HashTree tpConfig =  mainTree.add(testPlan);
                HashTree tgConfig = tpConfig.add(threadGroup);
                HashTree samplerConfig = tgConfig.add(samplerProxy, manager);
                tgConfig.add(csvDataSet);

                try {
                    SaveService.saveTree(mainTree, new FileOutputStream(jmeterHome + slash + "example.jmx"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //add Summarizer output to get test progress in stdout like:
                // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
                Summariser summer = null;
                String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
                if (summariserName.length() > 0) {
                    summer = new Summariser(summariserName);
                }
                // Store execution results into a .jtl file
                String logFile = jmeterHome + slash + "example.jtl";
                ResultCollector logger = new ResultCollector(summer);
                logger.setFilename(logFile);
                mainTree.add(mainTree.getArray()[0], logger);
                System.out.println("check point 4444");

                // Run Test Plan
                jmeter.configure(mainTree);
                jmeter.run();

                System.out.println("Test completed. See " + jmeterHome + slash + "example.jtl file for results");
                System.out.println("JMeter .jmx script is available at " + jmeterHome + slash + "example.jmx");
//                System.out.println(finalizeResult());
            }
        }
        response.setBody("jmeter.home property is not set or pointing to incorrect location");
    }

    private HTTPSamplerProxy createAndConfigureHttpSampler() {
        HTTPSamplerProxy samplerProxy = new HTTPSamplerProxy();
        samplerProxy.setProtocol(protocol);
        samplerProxy.setDomain(url);
//        samplerProxy.setDomain("${URL}");
        if(port!=-1) {
            samplerProxy.setPort(port);
        }
//        samplerProxy.setPath(path);
        samplerProxy.setPath("${path}");
        samplerProxy.setMethod(method);
        samplerProxy.setName(name);
        if(method.equalsIgnoreCase("POST") ||
                method.equalsIgnoreCase("PATCH")){
            //if(path.toLowerCase().contains("s3.amazonaws.com")) {
//                JsonElement element = new Gson().fromJson ("${body}", JsonElement.class);
//                JsonObject jsonObj = element.getAsJsonObject();
//                body = jsonObj.toString();
                samplerProxy.addNonEncodedArgument("body", "${body}", "");
//            }else {
//                JsonElement element = new Gson().fromJson (body, JsonElement.class);
//                JsonObject jsonObj = element.getAsJsonObject();
//                body = jsonObj.toString();
//                samplerProxy.addNonEncodedArgument("body", body, "");
//            }
            samplerProxy.setPostBodyRaw(true);
            samplerProxy.setFollowRedirects(true);
            samplerProxy.setAutoRedirects(true);
            samplerProxy.setUseKeepAlive(true);
        }
        if(isQueryParams) {
            String [] params = queryParams.split(",");
            for (int i = 0; i <params.length ; i++) {
                String param = params[i].trim();
                String key = param.split("=")[0].trim();
                String value = param.split("=")[1].trim();
                System.out.println(key + "  " + value);
                samplerProxy.addArgument(key, value);
            }
        }
        samplerProxy.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        samplerProxy.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        return samplerProxy;
    }

    private LoopController createAndConfigureLoopController(HTTPSamplerProxy httpSampler) {
        LoopController loopController = new LoopController();
        loopController.setLoops(loops);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        return loopController;
    }

    private ThreadGroup createAndConfigureThreadGroup(LoopController loopController, String threadName) {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName(threadName);
        threadGroup.setNumThreads(threads);
        threadGroup.setRampUp(rampup);
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        return threadGroup;
    }

    private  CSVDataSet createAndConfigureCsvDataSet(String csvFilePath) {
        // CSV Data Set
        File file = new File(csvFilePath);
        if (file.exists()) {
            System.out.println("CSV-File found: {}"+ file.getAbsoluteFile());
        } else {
            System.out.println("CSV-File NOT found: {}" + file.getAbsoluteFile());
        }
        CSVDataSet csvDataSet = new CSVDataSet();
        csvDataSet.setEnabled(true);
        csvDataSet.setProperty(new StringProperty("filename", csvFilePath));
        csvDataSet.setProperty(new StringProperty("variableNames", "URL"));
        csvDataSet.setProperty(new StringProperty("delimiter", "||"));
        csvDataSet.setProperty(new StringProperty("shareMode", "shareMode.all"));
        csvDataSet.setProperty("quoted", false);
        csvDataSet.setProperty("recycle", true);
        csvDataSet.setProperty("stopThread", false);
        return csvDataSet;
    }

    private RandomCSVDataSetConfig createAndConfigureRandomCsvDataSet(String csvFilePath) {
        // CSV Data Set
        File file = new File(csvFilePath);
        if (file.exists()) {
            System.out.println("CSV-File found: {}"+ file.getAbsoluteFile());
        } else {
            System.out.println("CSV-File NOT found: {}" + file.getAbsoluteFile());
        }
//        RandomCSVReader csvDataSet = new RandomCSVReader(csvFilePath, "UTF-8", ",", true, true, false, true);
        RandomCSVDataSetConfig csvDataSet = new RandomCSVDataSetConfig();

        csvDataSet.setEnabled(true);
        csvDataSet.setProperty(new StringProperty("filename", csvFilePath));
        csvDataSet.setProperty(new StringProperty("variableNames", "path,body"));
        csvDataSet.setProperty(new StringProperty("delimiter", "||"));
        csvDataSet.setProperty("fileEncoding", "UTF-8");
        csvDataSet.setProperty("randomOrder", true);
//        csvDataSet.setProperty(new StringProperty("shareMode", "shareMode.all"));
//        csvDataSet.setProperty("quoted", false);
//        csvDataSet.setProperty("recycle", true);
//        csvDataSet.setProperty("stopThread", false);
//        csvDataSet.setFileEncoding();
        return csvDataSet;
    }
    public static void main(String[] argv)  {
        try {
            new JMeterFromScratch().runTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.load;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.blazemeter.jmeter.RandomCSVDataSetConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.UUID;

public class JmeterHandler extends Base implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        isHeaders = false;
        System.out.println( "event.getHttpMethod() : " + event.getHttpMethod());
        if (event.getQueryStringParameters() != null) {
            if (event.getQueryStringParameters().containsKey("threads")) {
                threads = Integer.parseInt(event.getQueryStringParameters().get("threads"));
                System.out.println("threads: " + threads);
            }
            if (event.getQueryStringParameters().containsKey("loops")) {
                loops = Integer.parseInt(event.getQueryStringParameters().get("loops"));
                System.out.println("loops: " + loops);
            }
            if (event.getQueryStringParameters().containsKey("rampup")) {
                rampup = Integer.parseInt(event.getQueryStringParameters().get("rampup"));
                System.out.println("rampup: " + rampup);
            }
            if (event.getQueryStringParameters().containsKey("name")) {
                name = event.getQueryStringParameters().get("name");
                System.out.println("name: " + name);
            }
            if (event.getQueryStringParameters().containsKey("port")) {
                port = Integer.parseInt(event.getQueryStringParameters().get("port"));
                System.out.println("port: " + port);
            }
            if (event.getQueryStringParameters().containsKey("url")) {
                url = event.getQueryStringParameters().get("url");
                System.out.println("url: " + url);
            }
            if (event.getQueryStringParameters().containsKey("path")) {
                path = event.getQueryStringParameters().get("path");
                System.out.println("path: " + path);
            }
            if (event.getQueryStringParameters().containsKey("method")) {
                method = event.getQueryStringParameters().get("method");
                System.out.println("method: " + method);
            }
            if (event.getQueryStringParameters().containsKey("protocol")) {
                protocol = event.getQueryStringParameters().get("protocol");
                System.out.println("protocol: " + protocol);
            }
            if (event.getQueryStringParameters().containsKey("queryParams")) {
                queryParams = event.getQueryStringParameters().get("queryParams");
                if (queryParams == "" || queryParams == null)
                    isQueryParams = false;
                else
                    isQueryParams = true;
            }
            if (event.getQueryStringParameters().containsKey("s3folder")) {
                s3folder = event.getQueryStringParameters().get("s3folder");
                System.out.println("s3folder: " + s3folder);
            }
            if (event.getQueryStringParameters().containsKey("fileIndex")) {
                csvFileIndex = Integer.parseInt(event.getQueryStringParameters().get("fileIndex"));
                System.out.println("csvFileIndex: " + csvFileIndex);
            }
            if (event.getQueryStringParameters().containsKey("testPlan")) {
                testPlan = event.getQueryStringParameters().get("testPlan");
                System.out.println("testPlan: " + testPlan);
            }

            if (event.getQueryStringParameters().containsKey("headers")) {
                headers = event.getQueryStringParameters().get("headers");
                System.out.println("1 headers " + headers + " isHeaders " + isHeaders );
                if(headers==""){
                    System.out.println("11111");
                }
                if(headers == null){
                    System.out.println("22222");
                }
                if(headers.equals(null)){
                    System.out.println("33333");
                }
                if(headers.equals("null")){
                    System.out.println("4444");
                }
                if (headers == "" || headers == null || headers.equals(null) || headers.equals("null")) {
                    isHeaders = false;
                    System.out.println("2 headers " + headers + " isHeaders " + isHeaders);
                }
                else {
                    System.out.println("3 headers " + headers + " isHeaders " + isHeaders );
                    isHeaders = true;
                }
            }
        }
        System.out.println( "event.getHttpMethod() : " + event.getHttpMethod());
        System.out.println( "event.getRequestContext().getHttpMethod() : " + event.getRequestContext().getHttpMethod());
//        if (event.getRequestContext().getHttpMethod().equalsIgnoreCase("POST")) {
//            body = event.getBody();
//        }
        csvFilePath = "/tmp";
        if(path.toLowerCase().contains("s3.amazonaws.com")) {
            getTestDataFromS3(path);
        }
        UUID uuid = UUID.randomUUID();
        String threadName = "Thread_" + name + "_" + uuid.toString();
        stringBuilder = new StringBuilder();
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        File jmeterHome = new File("src/main/java/Jmeter/");
        String slash = System.getProperty("file.separator");

        if (jmeterHome.exists()) {
            System.out.println("Jmeter home is set");
            File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
            if (jmeterProperties.exists()) {
                //JMeter Engine
                StandardJMeterEngine jmeter = new StandardJMeterEngine();

                //JMeter initialization (properties, log levels, locale, etc)
                JMeterUtils.setJMeterHome(jmeterHome.getPath());
                JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
                JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
                JMeterUtils.initLocale();

                HeaderManager manager = new HeaderManager();
//                manager.add(new Header("Content-Type", "application/json"));
//                manager.add(new Header("Accept", "application/json"));
                manager.add(new Header("Cache-Control", "no-cache"));

                if(isHeaders){
                    System.out.println("headers '" + headers+ "'");
                    try {
                        headers = URLDecoder.decode(headers, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    System.out.println("headers '" + headers+ "'");
                    String []  heads = headers.split(",");
                    for (int i = 0; i <heads.length ; i++) {
                        String head = heads[i];
                        String[] pair = head.split(":");
                        String headerName = pair[0];
                        String headerVal = pair[1];
                        System.out.println("headerName " +  headerName);
                        manager.add(new Header(headerName, headerVal));
                    }
                }
                System.out.println("check point 1111");
                manager.setName(JMeterUtils.getResString("header_manager_title")); // $NON-NLS-1$
                manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
                manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());

                RandomCSVDataSetConfig csvDataSet = createAndConfigureRandomCsvDataSet(csvFilePath);
                // Second HTTP Sampler
                HTTPSamplerProxy samplerProxy = createAndConfigureHttpSampler();
//               samplerProxy.setHeaderManager(manager)
                // Loop Controller
                LoopController loopController = createAndConfigureLoopController(samplerProxy);
                // Thread Group
                ThreadGroup threadGroup = createAndConfigureThreadGroup(loopController, threadName);

                System.out.println("check point 2222");
                // Test Plan
                try {
                    System.out.println("check point 222211111");
                    TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
                    System.out.println("check point 22222222222");
//                    testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
                    System.out.println("check point 2222222222211111");
//                testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
                    testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
                    System.out.println("check point 2222222222211111");
                }catch (Exception exception){
                    System.out.println("exception: " + exception.getMessage());
                    exception.printStackTrace();
                }

                System.out.println("check point 222233333");
                HashTree mainTree = new ListedHashTree();

                HashTree tpConfig =  mainTree.add(testPlan);
                HashTree tgConfig = tpConfig.add(threadGroup);
                tgConfig.add(samplerProxy, manager);
                if(path.toLowerCase().contains("s3.amazonaws.com")) {
                    tgConfig.add(csvDataSet);
                }
                System.out.println("check point 33333");
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
                response.setBody("success");
                return response;
            }
        }
        response.setBody("jmeter.home property is not set or pointing to incorrect location");
        return response;
    }

    private HTTPSamplerProxy createAndConfigureHttpSampler() {
        HTTPSamplerProxy samplerProxy = new HTTPSamplerProxy();
        samplerProxy.setProtocol(protocol);
        //samplerProxy.setDomain(url);
        samplerProxy.setDomain(url);
        if(port!=-1) {
            samplerProxy.setPort(port);
        }
        if(path.toLowerCase().contains("s3.amazonaws.com")) {
            samplerProxy.setPath("${path}");
        }else {
            samplerProxy.setPath(path);
        }
        samplerProxy.setMethod(method);
        samplerProxy.setName(name);
        if(method.equalsIgnoreCase("POST") ||
                method.equalsIgnoreCase("PATCH")){
            if(path.toLowerCase().contains("s3.amazonaws.com")) {
                samplerProxy.addNonEncodedArgument("body", "${body}", "");
            }else {
                JsonElement element = new Gson().fromJson (body, JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();
                body = jsonObj.toString();
                samplerProxy.addNonEncodedArgument("body", body, "");
            }
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

    private CSVDataSet createAndConfigureCsvDataSet(String csvFilePath) {
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
        csvDataSet.setProperty(new StringProperty("variableNames", "path,body"));
        csvDataSet.setProperty(new StringProperty("delimiter", "|"));
        csvDataSet.setProperty(new StringProperty("shareMode", "shareMode.all"));
        csvDataSet.setProperty("quoted", false);
        csvDataSet.setProperty("recycle", true);
        csvDataSet.setProperty("stopThread", false);
        csvDataSet.setQuotedData(true);
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
        RandomCSVDataSetConfig csvDataSet = new RandomCSVDataSetConfig();

        csvDataSet.setEnabled(true);
        csvDataSet.setProperty(new StringProperty("filename", csvFilePath));
        csvDataSet.setProperty(new StringProperty("variableNames", "path,body"));
        csvDataSet.setProperty(new StringProperty("delimiter", "|"));
        csvDataSet.setProperty("fileEncoding", "UTF-8");
        csvDataSet.setProperty("randomOrder", true);
        return csvDataSet;
    }



    private void getTestDataFromS3(String s3URL){
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        System.out.println("Downloading test file");

        try {
            URI fileToBeDownloaded = new URI(s3URL);
            AmazonS3URI s3URI = new AmazonS3URI(fileToBeDownloaded);
            System.out.println("Bucket: " + s3URI.getBucket() + "   " + s3URI.getKey());
            String key = s3URI.getKey();
            String newKey = key.split(".csv")[0]+"-"+csvFileIndex+".csv";
            System.out.println("New Bucket: " + s3URI.getBucket() + "   " + newKey);
            S3Object s3Object = s3Client.getObject(s3URI.getBucket(), newKey);
            csvFilePath = csvFilePath+"/"+name+".csv";
            System.out.println(csvFilePath);
            File file = new File(csvFilePath);
            s3Client.getObject(new GetObjectRequest("qe-performance",s3Object.getKey()), file);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}

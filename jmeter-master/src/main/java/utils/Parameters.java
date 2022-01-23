package utils;

import models.TestField;
import org.json.simple.JSONObject;
import utils.logs.PrintLogs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

public class Parameters {

    public String testCase;
    public int threads;
    public int loops;
    public int rampup;
    public int lambdaConcurrency;
    public int requests;
    public int concurrency;
    public int port;
    public String url;
    public String protocol;
    public String method;
    public String path;
    public String body;
    public String queryParams;
    public String [] regions;
    public String x_api_key;
    public String environment;
    public int lambda_invocations;
    public String s3folder;
    public String testPlan;
    public String headers;

    PrintLogs printLogs = new PrintLogs(getClass());

    Properties properties = Properties.getInstance();

    public Parameters(TestField testField, String x_api_key, String environment, String random) {
        this.testCase = testField.getId();
        this.requests = testField.getRequests();
        this.concurrency = testField.getConcurrency();
        if(properties.getEnv().toLowerCase().contains("staging")){
            this.url = testField.getStagingUrl();
        }else{
            this.url = testField.getQaUrl();
        }
        this.protocol = testField.getProtocol();
        this.method = testField.getMethod();
        this.path = testField.getPath();
        this.port = testField.getPort();
        this.body = testField.getBody();
        this.queryParams = testField.getQueryParams();
        this.regions = testField.getRegions().split(",");;
        this.x_api_key = x_api_key;
        this.environment = environment;
        this.s3folder = random;
        this.headers = getHeadersObject(testField);
    }

    public void distributeLoad(){
        double minThreads = properties.getMinThreads();
        double maxThreads = properties.getMaxThreads();
        double maxLoops = properties.getMaxLoops();
        double maxLambdaConcurrency = properties.getMaxLambdaConcurrency();
        double maxTotalConcurrency = maxThreads*maxLambdaConcurrency;

        if(this.concurrency> maxTotalConcurrency){
            printLogs.info("Maximum concurrency can be obtained is "+(int)maxTotalConcurrency+", setting concurrency to "+(int)maxTotalConcurrency);
            this.concurrency = (int)maxTotalConcurrency;
        }

        if(this.requests<this.concurrency){
            printLogs.info("Concurrency is greater than total requests, setting to equal to requests: " + this.requests);
            this.concurrency = this.requests;
        }


        double requests = this.requests;
        double concurrency = this.concurrency;
        double threads;
        double loops;
        double rampup = 1;
        double lambda_concurrency=1;
        double lambda_loop=1;

        threads = minThreads;
        threads = concurrency / lambda_concurrency;
        while (lambda_concurrency < maxLambdaConcurrency && threads>maxThreads) {
            lambda_concurrency++;
            threads = concurrency / lambda_concurrency;
        }
        loops = requests / (threads * lambda_concurrency);
        while (loops > maxLoops) {
            lambda_loop++;
            loops = Math.ceil(requests / (lambda_concurrency * threads * lambda_loop));
        }

        this.threads = (int)Math.ceil(threads);
        this.lambdaConcurrency = (int)Math.ceil(lambda_concurrency);
        this.lambda_invocations =(int) Math.ceil(lambda_loop)*this.lambdaConcurrency;
        this.loops = (int)Math.ceil(loops);
        this.rampup = Math.max((int)(Math.ceil(threads/40)),1);

        printLogs.info("Requests: " + requests +  ", Concurrency: " +  concurrency);
        printLogs.info("Distribution: " +
                " threads: " +  this.threads +
                " loops: " + this.loops+
                " rampup: " + this.rampup+
                " lambda_concurrency: " + this.lambdaConcurrency +
                " lambda_loops:" + lambda_loop +
                " total_lambda_invocations: " + lambda_invocations);
    }

    public String URLEncoded(String input){
        try {
            input = URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return input;
    }

    public String getHeadersObject(TestField testField){
        //headers
        String headers =null;
        testPlan = System.getProperty("testPlan");
        JSONObject objectKeys = testField.getHeaders();
        if(objectKeys!=null){
            Set keys = objectKeys.keySet();
            Iterator<String> iterator = keys.iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                String value = (String) objectKeys.get(key);
                if(value.contains("{{{") && value.contains("}}}")){
                    String [] temp = value.split("\\{\\{\\{");
                    value = temp[0];
                    String template = temp[1].replaceAll("}}}","");
                    value = value + System.getProperty(template);
                }
                if (headers == null) {
                    headers =   key + ":" + URLEncoded(value);
                }else{
                    headers = headers +","+key + ":" + URLEncoded(value);
                }
            }
        }

        return headers;
    }
}

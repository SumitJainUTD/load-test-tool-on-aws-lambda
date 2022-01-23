package utils;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import java.io.File;
import java.io.IOException;

public class Properties {

    private String x_api_key;
    private String x_access_key;
    private String x_secret_key;
    private String env;
    private String logsBasePath;
    private String jmeterHome;
    private String htmlReportPath;
    private String htmlReportPathForS3Logs;
    private String allRunSummaryLogPath;
    private String allRunSummaryLogPathForS3Logs;
    private String dataPath;
    private String s3bucketName;

    private Double minThreads;
    private Double maxThreads;
    private Double maxLoops;
    private Double maxLambdaConcurrency;

    private String testPlan;

    private String qa_us_east_1;
    private String qa_us_east_2;
    private String qa_us_west_1;

    private String staging_us_east_1;
    private String staging_us_east_2;
    private String staging_us_west_1;

    private String us_east_1;
    private String us_east_2;
    private String us_west_1;

    private static Properties properties = null;

    public String getX_api_key() {
        return x_api_key;
    }

    public void setX_api_key(String x_api_key) {
        this.x_api_key = x_api_key;
    }
    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public static Properties getInstance() {
        String propertiesFileName="src/main/resources/load-test.properties";
        if (properties == null) {
            JavaPropsMapper mapper = new JavaPropsMapper();
            try {
                properties = mapper.readValue(new File(propertiesFileName), Properties.class);
                properties.loadLatestProperties();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return properties;
    }

    public void loadLatestProperties() {
        if (System.getProperty("env") != null) {
            setEnv(System.getProperty("env"));
        }

        if (System.getProperty("x_api_key") != null) {
            setX_api_key(System.getProperty("x_api_key"));
        }

        if(System.getProperty("x_access_key")!=null){
            setX_access_key(System.getProperty("x_access_key"));
        }

        if(System.getProperty("x_secret_key")!=null){
            setX_access_key(System.getProperty("x_secret_key"));
        }

        if(properties.getEnv().equalsIgnoreCase("qa")){
            properties.setUs_east_1(properties.qa_us_east_1);
            properties.setUs_east_2(properties.qa_us_east_2);
            properties.setUs_west_1(properties.qa_us_west_1);
        } else{
            properties.setUs_east_1(properties.staging_us_east_1);
            properties.setUs_east_2(properties.staging_us_east_2);
            properties.setUs_west_1(properties.staging_us_west_1);
        }

    }

    public String getLogsBasePath() {
        return logsBasePath;
    }

    public void setLogsBasePath(String logsBasePath) {
        this.logsBasePath = logsBasePath;
    }

    public String getJmeterHome() {
        return jmeterHome;
    }

    public void setJmeterHome(String jmeterHome) {
        this.jmeterHome = jmeterHome;
    }

    public String getHtmlReportPath() {
        return htmlReportPath;
    }

    public void setHtmlReportPath(String htmlReportPath) {
        this.htmlReportPath = htmlReportPath;
    }

    public String getHtmlReportPathForS3Logs() {
        return htmlReportPathForS3Logs;
    }

    public void setHtmlReportPathForS3Logs(String htmlReportPathForS3Logs) {
        this.htmlReportPathForS3Logs = htmlReportPathForS3Logs;
    }

    public String getAllRunSummaryLogPath() {
        return allRunSummaryLogPath;
    }

    public void setAllRunSummaryLogPath(String allRunSummaryLogPath) {
        this.allRunSummaryLogPath = allRunSummaryLogPath;
    }

    public String getAllRunSummaryLogPathForS3Logs() {
        return allRunSummaryLogPathForS3Logs;
    }

    public void setAllRunSummaryLogPathForS3Logs(String allRunSummaryLogPathForS3Logs) {
        this.allRunSummaryLogPathForS3Logs = allRunSummaryLogPathForS3Logs;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getS3bucketName() {
        return s3bucketName;
    }

    public void setS3bucketName(String s3bucketName) {
        this.s3bucketName = s3bucketName;
    }

    public Double getMinThreads() {
        return minThreads;
    }

    public void setMinThreads(Double minThreads) {
        this.minThreads = minThreads;
    }

    public Double getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(Double maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Double getMaxLoops() {
        return maxLoops;
    }

    public void setMaxLoops(Double maxLoops) {
        this.maxLoops = maxLoops;
    }

    public Double getMaxLambdaConcurrency() {
        return maxLambdaConcurrency;
    }

    public void setMaxLambdaConcurrency(Double maxLambdaConcurrency) {
        this.maxLambdaConcurrency = maxLambdaConcurrency;
    }

    public String getUs_east_1() {
        return us_east_1;
    }

    public void setUs_east_1(String us_east_1) {
        this.us_east_1 = us_east_1;
    }

    public String getUs_east_2() {
        return us_east_2;
    }

    public void setUs_east_2(String us_east_2) {
        this.us_east_2 = us_east_2;
    }

    public String getUs_west_1() {
        return us_west_1;
    }

    public void setUs_west_1(String us_west_1) {
        this.us_west_1 = us_west_1;
    }

    public String getX_access_key() {
        return x_access_key;
    }

    public void setX_access_key(String x_access_key) {
        this.x_access_key = x_access_key;
    }

    public String getX_secret_key() {
        return x_secret_key;
    }

    public void setX_secret_key(String x_secret_key) {
        this.x_secret_key = x_secret_key;
    }
}



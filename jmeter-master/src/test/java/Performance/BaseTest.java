package Performance;


import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import utils.APIHelper;
import utils.Parameters;
import utils.Properties;
import utils.ResultSummary;
import utils.logs.ExtentManager;
import utils.logs.ExtentTestManager;
import utils.logs.PrintLogs;
import utils.resultsToEKS.GenerateResultsForELK;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseTest implements ITestListener {

    PrintLogs printLogs = new PrintLogs(getClass(), "PerformanceReport.html");
    Properties properties = Properties.getInstance();
    int totalNoOfLogsinS3 = 0;
    ResultSummary finalResultSummary;
    static int noOfRegions;
    static int regionCalled;
    static Parameters parameters;


    String logsBasePath = properties.getLogsBasePath();
    String jmeterHome = properties.getJmeterHome();
    String htmlReportPath = properties.getHtmlReportPath();
    String htmlReportPathForS3Logs = properties.getHtmlReportPathForS3Logs();
    String allRunSummaryLogPath = properties.getAllRunSummaryLogPath();
    String allRunSummaryLogPathForS3Logs = properties.getAllRunSummaryLogPathForS3Logs();
    SimpleDateFormat sdf = new SimpleDateFormat("dd_hh_mm_ss");
    String s3bucketName = properties.getS3bucketName();
    String accessKey = properties.getX_access_key(); ;
    String secret = properties.getX_secret_key() ;
    String testPlan = properties.getTestPlan();
    String dataPath = properties.getDataPath();

    String random = sdf.format(new Date())+"_"+ System.currentTimeMillis();
    static HashMap<String, String[]> testCasesMap = new HashMap<>();
    static ArrayList<String> testCasesList = new ArrayList<>();
    GenerateResultsForELK generateResultsForELK = new GenerateResultsForELK();
    APIHelper apiHelper = new APIHelper(properties.getEnv());
    TestHelper testHelper = new TestHelper();

    @Override
    public void onTestStart(ITestResult iTestResult) {
        String testCase = iTestResult.getParameters()[0].toString();
        ExtentTestManager.startTest(testCase);
        new TestHelper().purgeDirectory(new File("src/data"));
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        ExtentManager.getInstance().flush();
        new TestHelper().purgeDirectory(new File("src/data"));
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        ExtentManager.getInstance().flush();
        new TestHelper().purgeDirectory(new File("src/data"));
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        new TestHelper().purgeDirectory(new File("src/data"));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        new TestHelper().purgeDirectory(new File("src/data"));
    }

    @Override
    public void onStart(ITestContext iTestContext) {
        File f = new File("src/Reports");
        if(!f.exists()){
            f.mkdir();
        }
        TestHelper testHelper = new TestHelper();
        testHelper.purgeDirectory(f);
        File data = new File("src/data");
        if(!data.exists()){
            data.mkdir();
        }
    }

    @Override
    public void onFinish(ITestContext iTestContext) {

    }

    @AfterSuite
    public void afterSuite(){
        try {
            File f = new File("src/S3Reports");
            testHelper.purgeDirectory(f);
            testHelper.downloadS3Logs();
            testHelper.consolidateAllS3Logs();
            testHelper.createHTMLReport(allRunSummaryLogPathForS3Logs, htmlReportPathForS3Logs);
            generateResultsForELK.sendAllTestResultsEKS(jmeterHome, allRunSummaryLogPathForS3Logs, testPlan);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

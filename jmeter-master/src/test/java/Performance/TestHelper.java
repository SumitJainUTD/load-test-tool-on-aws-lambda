package Performance;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper extends MasterTest {

    public void createHTMLReport (String logPath, String htmlPath){
        try {
            Runtime.getRuntime().exec(jmeterHome + "/bin/jmeter -g " + logPath + " -o " + htmlPath);
            printLogs.info("Final HTML report is being created.....");
            Thread.sleep(45*1000);
        }catch (IOException io){
            printLogs.error("Unable to create final HTML report " + io.getStackTrace());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void purgeDirectory(File dir) {
        if(dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory())
                    purgeDirectory(file);
                file.delete();
            }
        }
    }

    public void downloadS3Logs(){
        String key = "Logs/"+parameters.s3folder;
        String dirPath = "src/S3Reports/";
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();
        AWSCredentials myCredentials = new BasicAWSCredentials(accessKey,
                secret);
        AmazonS3Client s3Client = new AmazonS3Client(myCredentials);
        System.out.println("Listing objects");
        ObjectListing objectListing = s3Client.listObjects(new ListObjectsRequest()
                .withBucketName("qe-performance").withPrefix(key));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
            File file = new File(dirPath+"/"+objectSummary.getKey());
            s3Client.getObject(new GetObjectRequest("qe-performance",objectSummary.getKey()), file);
        }
    }

    public void consolidateAllS3Logs() throws IOException {
//        String[] testNames = parameters.testCase();
        String dirPath = "src/S3Reports/Summary/";
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();
        File OverAllFile = new File(allRunSummaryLogPathForS3Logs);
        File consDir = new File("src/S3Reports/Logs/"+parameters.s3folder);
        File[] directoryListing = consDir.listFiles();
        boolean isHeaderDone = false;
        FileWriter fstream = new FileWriter(OverAllFile, true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,grpThreads,allThreads,URL,Latency,IdleTime");
        out.newLine();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isDirectory() == false) {
                    if (!(child.getName().toLowerCase().contains("lambda") && child.getName().toLowerCase().contains("warm"))) {


                        System.out.println("merging " + child.getName());
                        FileInputStream fis;

                        fis = new FileInputStream(child);
                        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

                        String aLine;
                        while ((aLine = in.readLine()) != null) {
                            aLine = aLine.replaceAll("\\s+", "").trim().replaceAll("\u0000", "");
                            if (aLine.toLowerCase().contains("timeStamp".toLowerCase())) {
                                //Copy the header for the first time (header from the log)
                                if (!isHeaderDone) {
                                    out.write(aLine);
                                    out.newLine();
                                    isHeaderDone = true;
                                }
                            } else {

                                out.write(aLine);
                                out.newLine();
                            }
                        }
                        in.close();
                    }
                }
            }
            System.out.println();
        }
        out.flush();
        out.close();
    }

    public void deleteSplitedFilesFromS3() {
        AWSCredentials myCredentials = new BasicAWSCredentials(accessKey,
                secret);
        String s3DataFolder = "TestData/" + testPlan + "/";
        AmazonS3Client s3Client = new AmazonS3Client(myCredentials);
        File consDir = new File("src/data");
        File[] directoryListing = consDir.listFiles();
        for (File child : directoryListing) {
            s3Client.deleteObject(s3bucketName, s3DataFolder + child.getName());
        }
    }

    public void areAllTestsCompleted() {
        String key = "Logs/" + parameters.s3folder + "/" + parameters.testCase;
        printLogs.info("key: " + key);
//        String key = "Logs/27_10_23_11_1551284591589";//+parameters.s3folder;
        AWSCredentials myCredentials = new BasicAWSCredentials(accessKey,
                secret);
        AmazonS3Client s3Client = new AmazonS3Client(myCredentials);
        int currentLogs = 0;
        printLogs.info("Waiting for " + parameters.testCase + " to be completed..");
        int counter = 0;
        int limit = 90;
        while (counter < limit && currentLogs < totalNoOfLogsinS3) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentLogs = s3Client.listObjects(new ListObjectsRequest().withBucketName("qe-performance").withPrefix(key)).getObjectSummaries().size();
            double percentage = (currentLogs * 100) / totalNoOfLogsinS3;
            counter++;

            printLogs.info("Expected Log files: " + totalNoOfLogsinS3 + " , Actual Log files: " + currentLogs + " percentage completed: " + percentage);
        }
        if (currentLogs == totalNoOfLogsinS3) {
            printLogs.pass(parameters.testCase + " is completed");
        } else {
            printLogs.error(parameters.testCase + " is not completed in the given time");
        }
    }

    public void splitCSVInS3(int splits, String testCase) throws IOException {
        printLogs.info(parameters.path);
        if (!parameters.path.toLowerCase().contains("s3.amazonaws.com")) {
            return;
        }
//        String testCase = "GET_Sessions";
        String s3DataFolder = "TestData/" + testPlan + "/";
        String key = s3DataFolder + testCase + ".csv";//+parameters.testCase;
        System.out.println("key: " + key);
        String dirPath = "src/data/";
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();
        String mainFilePath = "src/data/" + testCase + ".csv";
        System.out.println("mainFilePath" + mainFilePath);
        AWSCredentials myCredentials = new BasicAWSCredentials(accessKey,
                secret);
        AmazonS3Client s3Client = new AmazonS3Client(myCredentials);
        System.out.println("Listing objects");
        ObjectListing objectListing = s3Client.listObjects(new ListObjectsRequest()
                .withBucketName(s3bucketName).withPrefix(key));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
            File file = new File(mainFilePath);
            s3Client.getObject(new GetObjectRequest(s3bucketName, objectSummary.getKey()), file);
        }

        Path path = Paths.get(mainFilePath);
        long totalRecords = Files.lines(path).count();
        int recordsForEach = (int) (totalRecords / splits);
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(mainFilePath));
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            for (int i = 0; i < splits; i++) {
                File splitFile = new File(dirPath + testCase + "-" + i + ".csv");
                FileWriter fstream = new FileWriter(splitFile, true);
                BufferedWriter out = new BufferedWriter(fstream);
                String aLine;
                for (int j = 0 + (i * recordsForEach); j < recordsForEach * (i + 1); j++) {
                    aLine = in.readLine();
                    out.write(aLine);
                    out.newLine();
                }
                out.flush();
                out.close();
            }
        } catch (IOException exp) {
            exp.printStackTrace();
        }
        File file = new File(mainFilePath);
        file.delete();
        File consDir = new File("src/data");
        File[] directoryListing = consDir.listFiles();
        for (File child : directoryListing) {
            if (child.isDirectory() == false) {
                s3Client.putObject(new PutObjectRequest(s3bucketName, s3DataFolder + child.getName(), child));
            }
        }
    }
}


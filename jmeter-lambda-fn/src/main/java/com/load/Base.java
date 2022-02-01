package com.load;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

public class Base {


    StringBuilder stringBuilder;
    int threads = 1;
    int loops = 1;
    int rampup = 2;
    String url = "example.com/";
    int port = -1;
    String path="/";
    String method = "GET";
    String name = "Open example.com";
    String protocol ="http";
    String logPath = "src/main/java/Jmeter/results.jtl";
    String body="{}";
    String queryParams="";
    boolean isQueryParams = false;
    String s3folder = "temp";
    String csvFilePath = "/tmp";
    int csvFileIndex = 0;
    String testPlan = "";
    boolean isHeaders = false;
    String headers="";


    public void writeLogsToS3(String folder){
        System.out.println("Writing Logs to S3 to folder : " + folder);
        try {
            AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
            String bucketName = "qe-performance";

            //create a file to be uploaded in s3
            String randomName = name+"-s3logs-"+ UUID.randomUUID();
            File file = File.createTempFile( randomName, ".txt");
            file.deleteOnExit();
            System.out.println("Random file Name : " + randomName);
            Writer writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write(stringBuilder.toString());
            writer.close();

            System.out.println("Uploading a new object to S3 from a file " + file.getName());
            s3.putObject(new PutObjectRequest(bucketName, "Logs/"+folder+"/"+file.getName(), file));

        }catch (Exception exp){
            System.out.println("Exception in writing logs to s3 " + exp.getMessage());
            exp.printStackTrace();
        }
    }
}

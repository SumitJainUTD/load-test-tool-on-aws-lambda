package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import models.TestField;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataProvider {

    String TEST_DATA_LOCATION = "src/test/resources/performance-data.csv";
    String TEST_DATA_SESSIONS_JSON="src/test/resources/session-test-data.json";
    String testSuite = "src/test/resources/session-config.json";

    public DataProvider(String testPlan) {
        testPlan = System.getProperty("testPlan");
        System.out.println("testPlan " +  testPlan);
        System.out.println("env " +  System.getProperty("env"));

        testSuite = "src/test/resources/"+testPlan+".json";

        System.out.println("Executing test suite : " + testSuite);
    }

    public Iterator<Object[]> getTestCases(Class clazz, String method) {
        String className = clazz.getSimpleName();
        List<Object[]> testCases = new ArrayList<>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(TEST_DATA_LOCATION));
            List<String[]> csvList = csvReader.readAll();
            int range = -1;
            for (int i = 0; i < csvList.size(); i++) {
                String[] rowData = csvList.get(i);
                if (rowData[0].equalsIgnoreCase("true") && rowData[1].equalsIgnoreCase(className)
                        && rowData[2].equalsIgnoreCase(method)) {
                    if(range==-1){
                        range = getRange(csvList, (i));
                    }
                    rowData = Arrays.copyOfRange(rowData, 3, range);
                    testCases.add(rowData);
                }
            }
        } catch (IOException exp) {
            exp.printStackTrace();
            return testCases.iterator();
        }
        return testCases.iterator();
    }

    private int getRange(List<String[]> csvList, int index){
        int header = -1;
        for (int i = index; i >=0 ; i--) {
            if(csvList.get(i)[0].equalsIgnoreCase("Execute")) {
                header = i;
                break;
            }
        }
        return csvList.get(header).length;
    }


    public Iterator<Object> getTestCasesFromJSON() {
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in file
        try {
            List<TestField> testData = mapper.readValue(new File(testSuite), mapper.getTypeFactory().constructCollectionType(List.class, TestField.class));
            List<Object> testFields = Collections.singletonList(testData);
            return testFields.iterator();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
package org.sapiens;

import java.io.File;
import java.util.Map;
import java.util.Date;
import java.util.Random;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;

import org.testng.annotations.Test;
import org.apache.poi.ss.usermodel.Cell;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Genereate SA ID Number based on DateOfBirth and Gender:
public class GenerateIDNumber {
    static String inputfilePath = "C:\\Users\\AB0229G\\MyWorkspace\\sti-generate-sa-id\\config\\IDNumberGenerate.xlsx";
    static SimpleDateFormat formatTime;
    static String tempidNumber;
    static String finalidNumber;
    static int lastDigit = 0;
    static FileInputStream fis;
    static XSSFWorkbook wb;
    static XSSFSheet sheet;
    static int excelRowCount = 0;
    static String curDateTime;
    static FileWriter idNumbers;
    ValidateSouthAfricanID obj;
    String dob;
    String genderFlag;
    Boolean checkIDStatus = false;

    public GenerateIDNumber() throws IOException {
        Date dateTime = new Date();
        formatTime = new SimpleDateFormat("MMddyyyy_kkmmss");
        curDateTime = formatTime.format(dateTime);
        idNumbers = new FileWriter(
                System.getProperty("user.dir") + "/TestResults/" + "ListOfIDNumbers_" + curDateTime + ".txt");
        obj = new ValidateSouthAfricanID();
    }

    @Test(dataProvider = "TestData")
    public void genereateIDNumber(Map<Object, Object> inputRequestData) throws IOException {
        try {
            if (excelRowCount == 0) {
                idNumbers.write("DateOfBirth[YYYYMMDD]" + "\t" + "Gender" + "\t" + "SA ID Number" + "\t" + "ID Validation Status" + "\n");
            }
            dob = inputRequestData.get("DateOfBirth[YYYYMMDD]").toString();   // YYYYMMDD
            genderFlag = inputRequestData.get("Gender").toString();

            while (checkIDStatus == false) {
                Thread.sleep(500);
                tempidNumber = configureIDNumber(dob, genderFlag, tempidNumber);
                Thread.sleep(500);
                lastDigit = generateLuhnDigit(tempidNumber);
                finalidNumber = tempidNumber + lastDigit;
                Thread.sleep(500);
                checkIDStatus = obj.isValidIdNumber(finalidNumber);
            }

            // Update the results:
            idNumbers.write(inputRequestData.get("DateOfBirth[YYYYMMDD]") + "\t\t\t\t" + inputRequestData.get("Gender") + "\t\t" + finalidNumber + "\t" + checkIDStatus + "\n");
            excelRowCount++;
            checkIDStatus = false;  // Reset back to default state:
        } catch (Exception ex) {
            ex.printStackTrace();
            wb.close();
            idNumbers.close();
            excelRowCount++;
        }
    }

    @DataProvider(name = "TestData")
    public Object[][] dataSupplier() throws IOException {
        DataFormatter dataFormatter = new DataFormatter();
        String formattedCellStr = "";

        File file = new File(inputfilePath);
        fis = new FileInputStream(file);
        wb = new XSSFWorkbook(fis);
        sheet = wb.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        int lastCellNum = sheet.getRow(0).getLastCellNum();
        Object[][] obj = new Object[lastRowNum][1];
        Cell cell;

        for (int i = 0; i < lastRowNum; i++) {
            Map<Object, Object> datamap = new HashMap<>();
            for (int j = 0; j < lastCellNum; j++) {
                cell = sheet.getRow(i + 1).getCell(j);
                formattedCellStr = dataFormatter.formatCellValue(cell).toString();
                datamap.put(sheet.getRow(0).getCell(j).toString(), formattedCellStr);
            }
            obj[i][0] = datamap;
        }
        // Close Input File Object:
        fis.close();
        return obj;
    }

    private static String configureIDNumber(String dob, String genderFlag, String idNumber) {
        try {
            //        System.out.println(dob.substring(2,4) + dob.substring(6,8) + dob.substring(4,6));
            Random r = new Random();
            // gender. 0 – 4 for female and 5 -  9 for male
            int femaleRange = 4;
            int maleRange = 9;
            int genderRange;
            Thread.sleep(500);
            if (genderFlag.equalsIgnoreCase("M"))
                genderRange = r.nextInt(maleRange - 5) + 5;
            else
                genderRange = r.nextInt(femaleRange - 0) + 0;
            Thread.sleep(500);
            int subGenderRange = r.nextInt(999 - 000) + 000;

            idNumber = dob.substring(2, 4) + dob.substring(4, 6) + dob.substring(6, 8) + genderRange + subGenderRange + 0 + 8;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return idNumber;
    }

    private static int generateLuhnDigit(String idNumber) {
        int count = 0;
        for (int i = 0; i < idNumber.length(); i++) {
            int multiple = (count % 2) + 1;
            count++;
            int temp = multiple * Integer.parseInt(String.valueOf(idNumber.charAt(i)));
            temp = (int) Math.floor(temp / 10) + (temp % 10);
            lastDigit += temp;
        }
        lastDigit = (lastDigit * 9) % 10;
        return lastDigit;
    }

    @AfterClass
    private void afterClass() {
        try {
            wb.close();
            idNumbers.close();
            System.out.println("Test Completed..!!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}

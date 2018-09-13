package com.madgaze.sensortester;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

public class CsvOperator {
    // 读取 .csv 文件
    public static void readCsv(List<RecordBean> records, String path) {
        records.clear();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));  // 防止出现乱码
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
                RecordBean record = new RecordBean();
                record.orientation =  Integer.parseInt(csvRecord.get("orientation"));
                record.x = Float.parseFloat(csvRecord.get("x"));
                record.y = Float.parseFloat(csvRecord.get("y"));
                record.z = Float.parseFloat(csvRecord.get("z"));
                records.add(record);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 写入 .csv 文件
    public static void writeCsv(List<RecordBean> records, String path) {
        try {
            File file = new File(path);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));  // 防止出现乱码
            // 添加头部
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("orientation", "x", "y", "z"));
            // 添加内容
            for (int i = 0; i < records.size(); i++) {
                csvPrinter.printRecord(
                        records.get(i).orientation,
                        records.get(i).x,
                        records.get(i).y,
                        records.get(i).z);
            }
            csvPrinter.printRecord();
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

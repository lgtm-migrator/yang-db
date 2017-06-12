package com.kayhut.test.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.kayhut.test.scenario.ETLUtils.*;

/**
 * Created by Roman on 06/06/2017.
 */
public class IngestPeopleToES {


    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = getClient();
        BulkProcessor processor = getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\demo_data_6June2017\\persons.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.NUMBER)
                        .addColumn("firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("lastName", CsvSchema.ColumnType.STRING)
                        .addColumn("gender", CsvSchema.ColumnType.STRING)
                        .addColumn("birthDate", CsvSchema.ColumnType.STRING)
                        .addColumn("deathDate", CsvSchema.ColumnType.STRING)
                        .addColumn("height", CsvSchema.ColumnType.NUMBER)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });

        String index = "people";
        String type = PERSON;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> person = reader.readValue(line);
                String id = id(type, person.remove("id").toString());

                person.put("birthDate", sdf.format(new Date(Long.parseLong(person.get("birthDate").toString()) * 1000)));
                person.put("deathDate", sdf.format(new Date(Long.parseLong(person.get("deathDate").toString()))));

                processor.add(new IndexRequest(index, type, id).source(person));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }

 }
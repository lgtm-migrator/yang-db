package com.kayhut.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by benishue on 19-Feb-17.
 */
public class QueryTest {

    private ObjectMapper mapper = new ObjectMapper();
    private static Query q1Obj = new Query();
    private static Query q2Obj = new Query();
    private static Query q3_1Obj = new Query();
    private static Query q3_2Obj = new Query();
    private static Query q5Obj = new Query();
    private static Query q11Obj = new Query();


    @Test
    public void testQ1Serialization() throws IOException, JSONException {
        String q1ActualJSON = mapper.writeValueAsString(q1Obj);
        String q1ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q1\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"EConcrete\",\"eTag\":\"A\",\"eID\":\"12345678\",\"eType\":\"Person\",\"eName\":\"Brandon Stark\",\"next\":2},{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"own\",\"dir\":\"R\",\"next\":3},{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\"}]}";

        JSONAssert.assertEquals(q1ExpectedJSONString, q1ActualJSON,false);
    }

    @Test
    public void testQ2Serialization() throws IOException, JSONException {
        String q2ActualJSON = mapper.writeValueAsString(q2Obj);
        String q2ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q2\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"EConcrete\",\"eTag\":\"A\",\"eID\":\"12345678\",\"eType\":\"Person\",\"eName\":\"Brandon Stark\",\"next\":2},{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"own\",\"dir\":\"R\",\"next\":3},{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\",\"next\":4},{\"eNum\":4,\"type\":\"Rel\",\"rType\":\"fire\",\"dir\":\"R\",\"next\":5},{\"eNum\":5,\"type\":\"ETyped\",\"eTag\":\"C\",\"eType\":\"Dragon\"}]}";

        JSONAssert.assertEquals(q2ExpectedJSONString, q2ActualJSON,false);
    }

    @Test
    public void testQ3_1Serialization() throws IOException, JSONException {
        String q3_1ActualJSON = mapper.writeValueAsString(q3_1Obj);
        String q3_1ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q3-1\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Dragon\",\"next\":2},{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"own\",\"dir\":\"L\",\"next\":3},{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Person\",\"next\":4},{\"eNum\":4,\"type\":\"EProp\",\"pType\":\"1.1\",\"pTag\":\"1\",\"con\":{\"op\":\"eq\",\"expr\":\"Brandon\"}}]}";

        JSONAssert.assertEquals(q3_1ExpectedJSONString, q3_1ActualJSON,false);
    }

    @Test
    public void testQ3_2Serialization() throws IOException, JSONException {
        String q3_2ActualJSON = mapper.writeValueAsString(q3_2Obj);
        String q3_2ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q3-2\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},{\"eNum\":2,\"type\":\"Quant1\",\"qType\":\"all\",\"next\":[3,4]},{\"eNum\":3,\"type\":\"EProp\",\"pType\":\"1.1\",\"pTag\":\"1\",\"con\":{\"op\":\"eq\",\"expr\":\"Brandon\"}},{\"eNum\":4,\"type\":\"Rel\",\"rType\":\"own\",\"dir\":\"R\",\"next\":5},{\"eNum\":5,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\"}]}";

        JSONAssert.assertEquals(q3_2ExpectedJSONString, q3_2ActualJSON,false);
    }

    @Test
    public void testQ5Serialization() throws IOException, JSONException {
        String q5ActualJSON = mapper.writeValueAsString(q5Obj);
        String q5ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q5\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},{\"eNum\":2,\"type\":\"Quant1\",\"qType\":\"all\",\"next\":[3,5,11]},{\"eNum\":3,\"type\":\"Rel\",\"rType\":\"own\",\"dir\":\"R\",\"next\":4},{\"eNum\":4,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\"},{\"eNum\":5,\"type\":\"Rel\",\"rType\":\"freeze\",\"dir\":\"R\",\"next\":6},{\"eNum\":6,\"type\":\"ETyped\",\"eTag\":\"C\",\"eType\":\"Dragon\",\"next\":7},{\"eNum\":7,\"type\":\"Rel\",\"rType\":\"fire\",\"dir\":\"R\",\"next\":8},{\"eNum\":8,\"type\":\"ETyped\",\"eTag\":\"D\",\"eType\":\"Dragon\",\"next\":9},{\"eNum\":9,\"type\":\"Rel\",\"rType\":\"fire\",\"dir\":\"R\",\"next\":10},{\"eNum\":10,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\"},{\"eNum\":11,\"type\":\"Rel\",\"rType\":\"freeze\",\"dir\":\"R\",\"next\":12},{\"eNum\":12,\"type\":\"ETyped\",\"eTag\":\"E\",\"eType\":\"Person\",\"next\":13},{\"eNum\":13,\"type\":\"Rel\",\"rType\":\"own\",\"dir\":\"R\",\"next\":14},{\"eNum\":14,\"type\":\"ETyped\",\"eTag\":\"D\",\"eType\":\"Dragon\"}],\"nonidentical\":[[\"C\",\"E\"]]}";

        JSONAssert.assertEquals(q5ExpectedJSONString, q5ActualJSON,false);
    }

    @Test
    public void testQ11Serialization() throws IOException, JSONException {
        String q11ActualJSON = mapper.writeValueAsString(q11Obj);
        String q11ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q11\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},{\"eNum\":2,\"type\":\"Quant1\",\"qType\":\"all\",\"next\":[3,6]},{\"eNum\":3,\"type\":\"Rel\",\"rType\":\"subject\",\"dir\":\"R\",\"next\":5,\"b\":4},{\"eNum\":4,\"type\":\"RelProp\",\"pType\":\"1.2\",\"pTag\":\"1\",\"con\":{\"op\":\"empty\"}},{\"eNum\":5,\"type\":\"EConcrete\",\"eTag\":\"B\",\"eID\":\"22345670\",\"eType\":\"Guild\",\"eName\":\"Masons\"},{\"eNum\":6,\"type\":\"Rel\",\"rType\":\"registered\",\"dir\":\"R\",\"next\":8,\"b\":7},{\"eNum\":7,\"type\":\"RelProp\",\"pType\":\"1\",\"pTag\":\"2\",\"con\":{\"op\":\"ge\",\"expr\":\"1011-01-01T00:00:00.000\"}},{\"eNum\":8,\"type\":\"ETyped\",\"eTag\":\"C\",\"eType\":\"Person\",\"next\":9},{\"eNum\":9,\"type\":\"Rel\",\"rType\":\"subject\",\"dir\":\"R\",\"next\":11,\"b\":10},{\"eNum\":10,\"type\":\"RelProp\",\"pType\":\"1.2\",\"pTag\":\"3\",\"con\":{\"op\":\"ge\",\"expr\":\"1010-06-01T00:00:00.000\"}},{\"eNum\":11,\"type\":\"Quant2\",\"qType\":\"some\",\"next\":[12,13]},{\"eNum\":12,\"type\":\"EConcrete\",\"eTag\":\"D\",\"eID\":\"22345671\",\"eType\":\"Guild\",\"eName\":\"Saddlers\"},{\"eNum\":13,\"type\":\"EConcrete\",\"eTag\":\"E\",\"eID\":\"22345672\",\"eType\":\"Guild\",\"eName\":\"Blacksmiths\"}]}";

        JSONAssert.assertEquals(q11ExpectedJSONString, q11ActualJSON,false);
    }

    @Test
    public void testDeSerialization() throws Exception {
        String q1ExpectedJson = readJsonToString("Q001.json");
        Query q1Obj = new ObjectMapper().readValue(q1ExpectedJson, Query.class);
        Assert.assertNotNull(q1Obj);
        String q1ActualJSON = mapper.writeValueAsString(q1Obj);
        JSONAssert.assertEquals(q1ExpectedJson, q1ActualJSON,false);

        String q2ExpectedJson = readJsonToString("Q002.json");
        Query q2Obj = new ObjectMapper().readValue(q2ExpectedJson, Query.class);
        Assert.assertNotNull(q1Obj);
        String q2ActualJSON = mapper.writeValueAsString(q2Obj);
        JSONAssert.assertEquals(q2ExpectedJson, q2ActualJSON,false);

        String q3_1ExpectedJson = readJsonToString("Q003-1.json");
        Query q3_1Obj = new ObjectMapper().readValue(q3_1ExpectedJson, Query.class);
        Assert.assertNotNull(q3_1Obj);
        String q3_1ActualJSON = mapper.writeValueAsString(q3_1Obj);
        JSONAssert.assertEquals(q3_1ExpectedJson, q3_1ActualJSON,false);


        String q3_2ExpectedJson = readJsonToString("Q003-2.json");
        Query q3_2Obj = new ObjectMapper().readValue(q3_2ExpectedJson, Query.class);
        Assert.assertNotNull(q3_2Obj);
        String q3_2ActualJSON = mapper.writeValueAsString(q3_2Obj);
        JSONAssert.assertEquals(q3_2ExpectedJson, q3_2ActualJSON,false);


        String q4ExpectedJson = readJsonToString("Q004.json");
        Query q4Obj = new ObjectMapper().readValue(q4ExpectedJson, Query.class);
        Assert.assertNotNull(q4Obj);
        String q4ActualJSON = mapper.writeValueAsString(q4Obj);
        JSONAssert.assertEquals(q4ExpectedJson, q4ActualJSON,false);


        String q5ExpectedJson = readJsonToString("Q005.json");
        Query q5Obj = new ObjectMapper().readValue(q5ExpectedJson, Query.class);
        Assert.assertNotNull(q5Obj);
        String q5ActualJSON = mapper.writeValueAsString(q5Obj);
        JSONAssert.assertEquals(q5ExpectedJson, q5ActualJSON,false);

        String q11ExpectedJson = readJsonToString("Q005.json");
        Query q11Obj = new ObjectMapper().readValue(q11ExpectedJson, Query.class);
        Assert.assertNotNull(q11Obj);
        String q11ActualJSON = mapper.writeValueAsString(q11Obj);
        JSONAssert.assertEquals(q11ExpectedJson, q11ActualJSON,false);

    }

    private static void createQ1()
    {
        q1Obj.setOnt("Dragons");
        q1Obj.setName("Q1");
        List<EBase> elements = new ArrayList<EBase>();

        /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
         {
          "eNum": 1,
          "type": "EConcrete",
          "eTag": "A",
          "eID": "12345678",
          "eType": 1,
          "eName": "Brandon Stark",
          "next": 2
         }
        */
        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("12345678");
        eConcrete.seteType("Person");
        eConcrete.seteName("Brandon Stark");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        /*
        {
          "eNum": 2,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 3
        }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType("own");
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        elements.add(rel);


        /*
        {
          "eNum": 3,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
        */
        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType("Dragon");
        elements.add(eTyped);

        q1Obj.setElements(elements);
    }

    private static void createQ2()
    {
        q2Obj.setOnt("Dragons");
        q2Obj.setName("Q2");
        List<EBase> elements = new ArrayList<EBase>();

        /*
            {
              "eNum": 0,
              "type": "Start",
              "next": 1
            }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
            {
              "eNum": 1,
              "type": "EConcrete",
              "eTag": "A",
              "eID": "12345678",
              "eType": 1,
              "eName": "Brandon Stark",
              "next": 2
            }
        */

        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("12345678");
        eConcrete.seteType("Person");
        eConcrete.seteName("Brandon Stark");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        /*
            {
              "eNum": 2,
              "type": "Rel",
              "rType": 1,
              "dir": "R",
              "next": 3
            }
         */

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType("own");
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        elements.add(rel);

        /*
            {
              "eNum": 3,
              "type": "ETyped",
              "eTag": "B",
              "eType": 2,
              "next": 4
            }
        */

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType("Dragon");
        eTyped.setNext(4);
        elements.add(eTyped);

        /*
        {
            "eNum": 4,
            "type": "Rel",
            "rType": 3,
            "dir": "R",
            "next": 5
        }
        */

        Rel rel2 = new Rel();
        rel2.seteNum(4);
        rel2.setrType("fire");
        rel2.setDir(Rel.Direction.R);
        rel2.setNext(5);
        elements.add(rel2);

        /*
        {
            "eNum": 5,
            "type": "ETyped",
            "eTag": "C",
            "eType": 2
        }
        */

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(5);
        eTyped2.seteTag("C");
        eTyped2.seteType("Dragon");
        elements.add(eTyped2);

        q2Obj.setElements(elements);
    }

    private static void createQ3_1()
    {
        q3_1Obj.setOnt("Dragons");
        q3_1Obj.setName("Q3-1");
        List<EBase> elements = new ArrayList<EBase>();

        /*
            {
              "eNum": 0,
              "type": "Start",
              "next": 1
            }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
        {
          "eNum": 1,
          "type": "ETyped",
          "eTag": "A",
          "eType": 2,
          "next": 2
        }
         */

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("Dragon");
        eTyped.setNext(2);
        elements.add(eTyped);

        /*
         {
          "eNum": 2,
          "type": "Quant1",
          "qType": "all",
          "next": [
            3,
            4
          ]
        }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType("own");
        rel.setDir(Rel.Direction.L);
        rel.setNext(3);
        elements.add(rel);

        /*
            {
              "eNum": 3,
              "type": "ETyped",
              "eTag": "B",
              "eType": 1,
              "next": 4
            }
         */

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("Person");
        eTyped2.setNext(4);
        elements.add(eTyped2);

        /*
            {
              "eNum": 4,
              "type": "EProp",
              "pType": "1.1",
              "pTag": "1",
              "con": {
                "op": "eq",
                "expr": "Brandon"
              }
         */

        EProp eProp = new EProp();
        eProp.seteNum(4);
        eProp.setpType("1.1");
        eProp.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("Brandon");
        eProp.setCon(con);
        elements.add(eProp);

        q3_1Obj.setElements(elements);
    }

    private static void createQ3_2() {
        q3_2Obj.setOnt("Dragons");
        q3_2Obj.setName("Q3-2");

        List<EBase> elements = new ArrayList<EBase>();

        /*
            {
              "eNum": 0,
              "type": "Start",
              "next": 1
            }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
            {
              "eNum": 1,
              "type": "ETyped",
              "eTag": "A",
              "eType": 2,
              "next": 2
            }
         */

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("Person");
        eTyped.setNext(2);
        elements.add(eTyped);

        /*
         {
          "eNum": 2,
          "type": "Quant1",
          "qType": "all",
          "next": [
            3,
            4
          ]
        }
         */

        Quant1 quant1 = new Quant1();
        quant1.seteNum(2);
        quant1.setqType(QuantType.all);
        quant1.setNext(Arrays.asList(3,4));
        elements.add(quant1);

        /*
        {
          "eNum": 3,
          "type": "EProp",
          "pType": 1,
          "con": {
            "op": "eq",
            "value": "Brandon"
          }
        }*/

        EProp eProp = new EProp();
        eProp.seteNum(3);
        eProp.setpType("1.1");
        eProp.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("Brandon");
        eProp.setCon(con);
        elements.add(eProp);

        /*
        {
          "eNum": 4,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 5
        }
         */

        Rel rel1 = new Rel();
        rel1.seteNum(4);
        rel1.setrType("own");
        rel1.setDir(Rel.Direction.R);
        rel1.setNext(5);
        elements.add(rel1);

        /*
        {
          "eNum": 5,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
         */

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(5);
        eTyped2.seteTag("B");
        eTyped2.seteType("Dragon");
        elements.add(eTyped2);

        q3_2Obj.setElements(elements);
    }

    private static void createQ5() {

        q5Obj.setOnt("Dragons");
        q5Obj.setName("Q5");
        List<EBase> elements = new ArrayList<EBase>();
        /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */
        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);
       /*{
            "eNum": 1,
            "type": "ETyped",
            "eTag": "A",
            "eType": 1,
            "next": 2
     }*/
      ETyped eTyped1 = new ETyped();
      eTyped1.seteNum(1);
      eTyped1.seteTag("A");
      eTyped1.seteType("Person");
      eTyped1.setNext(2);
      elements.add(eTyped1);

        /*
        {
          "eNum": 2,
          "type": "Quant1",
          "qType": "all",
          "next": [
            3,
            5,
            11
          ]
        }
         */
        Quant1 quant1 = new Quant1();
        quant1.seteNum(2);
        quant1.setqType(QuantType.all);
        quant1.setNext(Arrays.asList(3,5,11));
        elements.add(quant1);
        /*
        {
          "eNum": 3,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 4
        }
         */
        Rel rel1 = new Rel();
        rel1.seteNum(3);
        rel1.setrType("own");
        rel1.setDir(Rel.Direction.R);
        rel1.setNext(4);
        elements.add(rel1);

        /*
        {
          "eNum": 4,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
         */

      ETyped eTyped2 = new ETyped();
      eTyped2.seteNum(4);
      eTyped2.seteTag("B");
      eTyped2.seteType("Dragon");
      elements.add(eTyped2);

          /*
        {
          "eNum": 5,
          "type": "Rel",
          "rType": 4,
          "dir": "R",
          "next": 6
        }
         */

        Rel rel2 =  new Rel();
        rel2.seteNum(5);
        rel2.setrType("freeze");
        rel2.setDir(Rel.Direction.R);
        rel2.setNext(6);
        elements.add(rel2);

        /*
        {
          "eNum": 6,
          "type": "ETyped",
          "eTag": "C",
          "eType": 1,
          "next": 7
        }
         */

         ETyped eTyped3 = new ETyped();
         eTyped3.seteNum(6);
         eTyped3.seteTag("C");
         eTyped3.seteType("Dragon");
         eTyped3.setNext(7);
         elements.add(eTyped3);

        /*
        {
          "eNum": 7,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 8
        }
         */

        Rel rel3 = new Rel();
        rel3.seteNum(7);
        rel3.setrType("fire");
        rel3.setDir(Rel.Direction.R);
        rel3.setNext(8);
        elements.add(rel3);

         /*
        {
          "eNum": 8,
          "type": "ETyped",
          "eTag": "D",
          "eType": 2,
          "next": 9
        }
         */
         ETyped eTyped4 = new ETyped();
         eTyped4.seteNum(8);
         eTyped4.seteTag("D");
         eTyped4.seteType("Dragon");
         eTyped4.setNext(9);
         elements.add(eTyped4);

         /*
        {
          "eNum": 9,
          "type": "Rel",
          "rType": 3,
          "dir": "R",
          "next": 10
        }
         */

        Rel rel4 = new Rel();
        rel4.seteNum(9);
        rel4.setrType("fire");
        rel4.setDir(Rel.Direction.R);
        rel4.setNext(10);
        elements.add(rel4);

        /*
        {
          "eNum": 10,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
         */

        ETyped eTyped5 = new ETyped();
        eTyped5.seteNum(10);
        eTyped5.seteTag("B");
        eTyped5.seteType("Dragon");
        elements.add(eTyped5);

        /*
        {
          "eNum": 11,
          "type": "Rel",
          "rType": 4,
          "dir": "R",
          "next": 12
        }
         */

        Rel rel5 = new Rel();
        rel5.seteNum(11);
        rel5.setrType("freeze");
        rel5.setDir(Rel.Direction.R);
        rel5.setNext(12);
        elements.add(rel5);

        /*
        {
          "eNum": 12,
          "type": "ETyped",
          "eTag": "E",
          "eType": 1,
          "next": 13
        }
         */

        ETyped eTyped6 = new ETyped();
        eTyped6.seteNum(12);
        eTyped6.seteTag("E");
        eTyped6.seteType("Person");
        eTyped6.setNext(13);
        elements.add(eTyped6);

        /*
        {
          "eNum": 13,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 14
        }
        */

        Rel rel6 =  new Rel();
        rel6.seteNum(13);
        rel6.setrType("own");
        rel6.setDir(Rel.Direction.R);
        rel6.setNext(14);
        elements.add(rel6);

        /*
        {
          "eNum": 14,
          "type": "ETyped",
          "eTag": "D",
          "eType": 2
        }
         */

        ETyped eTyped7 = new ETyped();
        eTyped7.seteNum(14);
        eTyped7.seteTag("D");
        eTyped7.seteType("Dragon");
        elements.add(eTyped7);


       q5Obj.setNonidentical(Arrays.asList(Arrays.asList("C","E")));
       q5Obj.setElements(elements);
    }

    private static void createQ11(){
        q11Obj.setOnt("Dragons");
        q11Obj.setName("Q11");
        List<EBase> elements = new ArrayList<>();

        /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
        */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
        {
          "eNum": 1,
          "type": "ETyped",
          "eTag": "A",
          "eType": 1,
          "next": 2
        }
        */

        ETyped eTyped1 = new ETyped();
        eTyped1.seteNum(1);
        eTyped1.seteTag("A");
        eTyped1.seteType("Person");
        eTyped1. setNext(2);
        elements.add(eTyped1);

        /*
        {
          "eNum": 2,
          "type": "Quant1",
          "qType": "all",
          "next": [
            3,
            6
          ]
        }
        */

        Quant1 quant1 = new Quant1();
        quant1.seteNum(2);
        quant1.setqType(QuantType.all);
        quant1.setNext(Arrays.asList(3,6));
        elements.add(quant1);

        /*
        {
          "eNum": 3,
          "type": "Rel",
          "rType": 6,
          "dir": "R",
          "next": 5,
          "b": 4
        }
        */

        Rel rel1 = new Rel();
        rel1.seteNum(3);
        rel1.setrType("subject");
        rel1.setDir(Rel.Direction.R);
        rel1.setNext(5);
        rel1.setB(4);
        elements.add(rel1);

        /*
        {
          "eNum": 4,
          "type": "RelProp",
          "pType": "1.2",
          "pTag": "1",
          "con": {
            "op": "empty"
          }
        */

        Constraint conRelProp1 = new Constraint();
        conRelProp1.setOp(ConstraintOp.empty);

        RelProp relProp1 = new RelProp();
        relProp1.seteNum(4);
        relProp1.setpType("1.2");
        relProp1.setpTag("1");
        relProp1.setCon(conRelProp1);
        elements.add(relProp1);

        /*
        {
          "eNum": 5,
          "type": "EConcrete",
          "eTag": "B",
          "eID": "22345670",
          "eType": 4,
          "eName": "Masons"
        }
        */

        EConcrete eConcrete1 = new EConcrete();
        eConcrete1.seteNum(5);
        eConcrete1.seteTag("B");
        eConcrete1.seteID("22345670");
        eConcrete1.seteType("Guild");
        eConcrete1.seteName("Masons");
        elements.add(eConcrete1);

        /*
        {
          "eNum": 6,
          "type": "Rel",
          "rType": 5,
          "dir": "R",
          "next": 8,
          "b": 7
        }
        */

        Rel rel2 = new Rel();
        rel2.seteNum(6);
        rel2.setrType("registered");
        rel2.setDir(Rel.Direction.R);
        rel2.setNext(8);
        rel2.setB(7);
        elements.add(rel2);

        /*
        {
          "eNum": 7,
          "type": "RelProp",
          "pType": "1",
          "pTag": "2",
          "con": {
            "op": "ge",
            "expr": "1011-01-01T00:00:00.000"
          }
        }
        */

        Constraint conRelProp2 = new Constraint();
        conRelProp2.setOp(ConstraintOp.ge);
        conRelProp2.setExpr("1011-01-01T00:00:00.000");
        RelProp relProp2 = new RelProp();
        relProp2.seteNum(7);
        relProp2.setpType("1");
        relProp2.setpTag("2");
        relProp2.setCon(conRelProp2);
        elements.add(relProp2);

        /*
        {
          "eNum": 8,
          "type": "ETyped",
          "eTag": "C",
          "eType": 1,
          "next": 9
        }
        */

        ETyped eTyped3 = new ETyped();
        eTyped3.seteNum(8);
        eTyped3.seteTag("C");
        eTyped3.seteType("Person");
        eTyped3.setNext(9);
        elements.add(eTyped3);

        /*
        {
          "eNum": 9,
          "type": "Rel",
          "rType": 6,
          "dir": "R",
          "next": 11,
          "b": 10
        }
        */

        Rel rel3 = new Rel();
        rel3.seteNum(9);
        rel3.setrType("subject");
        rel3.setDir(Rel.Direction.R);
        rel3.setNext(11);
        rel3.setB(10);
        elements.add(rel3);

        /*
        {
          "eNum": 10,
          "type": "RelProp",
          "pType": "1.2",
          "pTag": "3",
          "con": {
            "op": "ge",
            "expr": "1010-06-01T00:00:00.000"
          }
        },
        */
        Constraint conRelProp3 = new Constraint();
        conRelProp3.setOp(ConstraintOp.ge);
        conRelProp3.setExpr("1010-06-01T00:00:00.000");
        RelProp relProp3 = new RelProp();
        relProp3.seteNum(10);
        relProp3.setpType("1.2");
        relProp3.setpTag("3");
        relProp3.setCon(conRelProp3);
        elements.add(relProp3);

        /*
        {
          "eNum": 11,
          "type": "Quant2",
          "qType": "some",
          "next": [
            12,
            13
          ]
        }
        */

        Quant2 quant2 = new Quant2();
        quant2.seteNum(11);
        quant2.setqType(QuantType.some);
        quant2.setNext(Arrays.asList(12,13));
        elements.add(quant2);


        /*
        {
          "eNum": 12,
          "type": "EConcrete",
          "eTag": "D",
          "eID": "22345671",
          "eType": 4,
          "eName": "Saddlers"
        }
        */

        EConcrete eConcrete2 = new EConcrete();
        eConcrete2.seteNum(12);
        eConcrete2.seteTag("D");
        eConcrete2.seteID("22345671");
        eConcrete2.seteType("Guild");
        eConcrete2.seteName("Saddlers");
        elements.add(eConcrete2);

        /*
        {
          "eNum": 13,
          "type": "EConcrete",
          "eTag": "E",
          "eID": "22345672",
          "eType": 4,
          "eName": "Blacksmiths"
        }
        */

        EConcrete eConcrete3 = new EConcrete();
        eConcrete3.seteNum(13);
        eConcrete3.seteTag("E");
        eConcrete3.seteID("22345672");
        eConcrete3.seteType("Guild");
        eConcrete3.seteName("Blacksmiths");
        elements.add(eConcrete3);

        q11Obj.setElements(elements);

    }

    private String readJsonToString(String jsonFileName) throws Exception {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("QueryJsons/" + jsonFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Before
    public void setup() {
    }

    @BeforeClass
    public static void setUpOnce() {
        createQ1();
        createQ2();
        createQ3_1();
        createQ3_2();
        createQ5();
        createQ11();
    }




}

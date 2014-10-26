package org.itest.test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.commons.lang.RandomStringUtils;
import org.itest.impl.HierarchicalStreamReaderAssignment;
import org.itest.impl.HierarchicalStreamReaderMergerImpl;
import org.itest.test.example2.InterfaceExample;
import org.itest.test.example2.MyInterface;
import org.itest.test.example2.MyInterfaceImpl;
import org.itest.test.example6.Group;
import org.itest.test.example6.Person;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rumcajs on 10/25/14.
 */
public class XStreamTest {
    @Test
    public void test() throws Exception {
        XStream xStream = new XStream();


        Map<Person, Group> m = new HashMap<Person, Group>();
        Group g = generate();
        m.put(g.persons.get(0), g);
        String s = xStream.toXML(m);
        System.out.println(s);
//        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
//        s = xstream.toXML(m);
//        System.out.println("s = " + s);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(s.getBytes()));
//        System.out.println(doc);
//        System.out.println(doc.getDocumentElement());
//        System.out.println(doc.getChildNodes());
//        System.out.println(doc.getChildNodes().getLength());
//
//
//        XPath xPath = XPathFactory.newInstance().newXPath();
//        NodeList nodeList = (NodeList) xPath.evaluate("/map/entry[1]/org.itest.test.example6.Group[2]", doc.getDocumentElement(), XPathConstants.NODESET);
//        print("", nodeList);
//        System.out.println(nodeList.getLength());

        HierarchicalStreamReader reader=new XppDriver().createReader(new StringReader(s));
        HierarchicalStreamReaderAssignment e=new HierarchicalStreamReaderAssignment(null,reader);
        HierarchicalStreamReaderMergerImpl merger=new HierarchicalStreamReaderMergerImpl(e);
        Map<Person,Group> o = (Map<Person, Group>) xStream.unmarshal(merger);
        System.out.println(o);
        System.out.println(o.size());
        //standard way
        o= (Map<Person, Group>) xStream.fromXML(s);
        System.out.println(o);
        System.out.println(o.size());

    }

    @Test
    public void test2() {
        ReflectionProvider r = JVM.newReflectionProvider();
        InterfaceExample interfaceExample = (InterfaceExample) r.newInstance(InterfaceExample.class);
        Class iType = r.getFieldType(interfaceExample, "myInterface", InterfaceExample.class);

        System.out.println(interfaceExample.myInterface);
        System.out.println("iType = " + iType);
        Object myInterface = r.newInstance(iType);
        System.out.println("myInterface = " + myInterface);


    }
    @Test
    public void test4(){
        InterfaceExample interfaceExample=new InterfaceExample();
        interfaceExample.myInterface=new MyInterfaceImpl<Date>();
        XStream xStream=new XStream();
        //xStream.autodetectAnnotations(true);
        String s = xStream.toXML(interfaceExample);
        System.out.println("s = " + s);

        xStream.alias("b",InterfaceExample.class);
        xStream.registerLocalConverter(InterfaceExample.class,"myInterface",new Converter() {
            @Override
            public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

            }

            @Override
            public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                return new MyInterfaceImpl<Object>();
            }

            @Override
            public boolean canConvert(Class type) {
                return MyInterface.class==type;
            }
        });
        //s="<org.itest.test.example2.InterfaceExample><myInterface></myInterface></org.itest.test.example2.InterfaceExample>";
        s="<b><myInterface class=\"org.itest.test.example2.MyInterfaceImpl2\"/></b>";
        interfaceExample = (InterfaceExample) xStream.fromXML(s);

        MyInterface myInterface=interfaceExample.myInterface;
        System.out.println("myInterface = " + myInterface);
    }
    private void print(String s, NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node e = nodeList.item(i);
            if (e.getNodeValue() == null) {
                System.out.println(s + i + e.getNodeName());
                print(s + "\t", e.getChildNodes());
            } else {
                System.out.println(s + i + e.getNodeName() + " : " + e.getNodeValue() + "/" + e.getNodeType());
            }
        }
    }

    public Group generate() {
        Group g = new Group("");
        g.name = RandomStringUtils.randomAlphabetic(10);
        g.persons = new ArrayList<Person>();
        for (int i = 0; i < 3; i++) {
            g.persons.add(generatePerson());
        }
        return g;
    }

    private Person generatePerson() {
        Person p = new Person();
        p.name = RandomStringUtils.randomAlphabetic(5);
        return p;
    }
}

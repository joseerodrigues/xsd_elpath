package com.cave.xsdelpath.main;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author José Rodrigues
 */
public class XsdElementPath {    

    public static String getAttributeValue(String attrName, Node node) {
        String ret = null;

        if (node != null && node.hasAttributes() && node.getAttributes().getNamedItem(attrName) != null) {
            ret = node.getAttributes().getNamedItem(attrName).getNodeValue();
        }

        return ret;
    }

    public static String getElementType(Node node) {
        return getAttributeValue("type", node);
    }

    public static String getElementTypeRef(Node node) {
        String ret = getAttributeValue("ref", node);

        if (ret == null && (!node.getNodeName().equals("xs:element"))) {

            NodeList childNodes = node.getChildNodes();

            if (node.hasChildNodes()) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    String ref = getElementTypeRef(childNodes.item(i));

                    if (ref != null) {
                        ret = ref;
                        break;
                    }
                }
            }
        }

        return ret;
    }

    public static String getElementExtensionType(Node node) {
        String ret = getAttributeValue("base", node);

        if (ret == null && (!node.getNodeName().equals("xs:element"))) {

            NodeList childNodes = node.getChildNodes();

            if (node.hasChildNodes()) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    String ref = getElementExtensionType(childNodes.item(i));

                    if (ref != null) {
                        ret = ref;
                        break;
                    }
                }
            }
        }

        return ret;
    }

    public static String getElementName(Node node) {
        return getAttributeValue("name", node);
    }

    public static Map<String, Map<String, Node>> findElements(Node node) {
        HashMap<String, Map<String, Node>> ret = new HashMap<String, Map<String, Node>>();

        String nodeName = node.getNodeName();
        String name = getElementName(node);

        ret.put(nodeName, new HashMap<String, Node>());

        if (name != null) {

            Map<String, Node> children = ret.get(nodeName);

            children.put(name, node);
        }

        NodeList list = node.getChildNodes();
        if (list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                Map<String, Map<String, Node>> found = findElements(list.item(i));

                for (String kn : found.keySet()) {
                    if (ret.containsKey(kn)) {

                        ret.get(kn).putAll(found.get(kn));

                    } else {
                        ret.put(kn, found.get(kn));
                    }
                }
            }
        }

        return ret;
    }        

    public static List<Node> findTypes(String typeName, Map<String, Map<String, Node>> elements) {

        ArrayList<Node> ret = new ArrayList<Node>();

        for (Entry<String, Map<String, Node>> entry : elements.entrySet()) {

            Map<String, Node> nodes = entry.getValue();

            if (nodes.containsKey(typeName)) {
                ret.add(nodes.get(typeName));
            }
        }

        return ret;
    }

    public static List<Node> reduceTypesTo(String nodeNames[], List<Node> foundTypes) {
        ArrayList<Node> ret = new ArrayList<Node>();

        for (int i = 0; i < nodeNames.length; i++) {
            for (Node n : foundTypes) {
                if (n.getNodeName().equals(nodeNames[i])) {
                    ret.add(n);
                }
            }
        }

        return ret;
    }
    
    public static Map<String, Node> generatePaths(boolean isType, String currentPath, Node node, Map<String, Map<String, Node>> elements) {

        HashMap<String, Node> ret = new HashMap<String, Node>();

        String mName = getElementName(node);
        String path = currentPath;

        if (mName != null && !isType) {
            path = currentPath + "/" + mName;
        }

        NodeList list = node.getChildNodes();

        if (list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                Map<String, Node> printPath = generatePaths(false, path, list.item(i), elements);

                ret.putAll(printPath);
            }
        }

        String eType = getElementType(node);
        String refType = getElementTypeRef(node);
        String extType = getElementExtensionType(node);

        if (eType != null) {
            String xsdTypeNodeNames[] = {"xs:complexType", "xs:simpleType"};
            
            List<Node> findTypes = reduceTypesTo(xsdTypeNodeNames, findTypes(eType, elements));

            Node typeEl = null;

            if (findTypes.size() == 1) {
                typeEl = findTypes.get(0);
            }

            if (typeEl != null) {
                Map<String, Node> printPath = generatePaths(true, path, typeEl, elements);

                ret.putAll(printPath);
            }

        }
        if (refType != null) {

            List<Node> findTypes = findTypes(refType, elements);

            Node typeEl = null;

            if (findTypes.size() == 1) {
                typeEl = findTypes.get(0);
            }

            if (typeEl != null) {
                Map<String, Node> printPath = generatePaths(true, path, typeEl, elements);

                ret.putAll(printPath);
            }

        }
        if (extType != null) {         

            List<Node> findTypes = findTypes(extType, elements);

            Node typeEl = null;

            if (findTypes.size() == 1) {
                typeEl = findTypes.get(0);
            }

            if (typeEl != null) {
                Map<String, Node> printPath = generatePaths(true, path, typeEl, elements);

                ret.putAll(printPath);
            }

        }


        ret.put(path, node);

        return ret;
    }

    public static Node getRootElement(Node node) {
        Node ret = null;

        if (getElementName(node) == null) {
            NodeList childNodes = node.getChildNodes();

            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node n = getRootElement(childNodes.item(i));

                    if (n != null) {
                        ret = n;
                        break;
                    }
                }
            }
        } else {
            ret = node;
        }
        return ret;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("XSD Filename argument Missing.");
            System.out.println("Use with : <xsdFile.xsd> [ElementName]");
            System.exit(1);
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XsdElementPath.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (builder == null) {
            System.err.println("Builder is null.");
            System.exit(1);
        }
        
         String filePath = args[0];

        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Non Existing file : " + filePath);
            System.exit(1);
        } else if (!file.canRead()) {
            System.err.println("Unable to read file : " + filePath);
            System.err.println("Please check if you have read privileges.");
            System.exit(1);
        }

        Document document = null;
        try {
            document = builder.parse(filePath);
        } catch (SAXException ex) {
            
        } catch (IOException ex) {
            
        }

        if (document == null) {
            System.err.println("Error processing document.");
            System.exit(1);
        }

        Map<String, Map<String, Node>> elements = findElements(document.getDocumentElement());

        Node root = null;
        String rootName = null;

        if (args.length == 2) {
            String elRootName = args[1];

            Map<String, Node> relements = elements.get("xs:element");            
            
            for (Entry<String, Node> entry : relements.entrySet()) {
                if (entry.getKey().equals(elRootName)) {
                    root = entry.getValue();
                    rootName = elRootName;
                    break;
                }
            }

        } else {
            root = getRootElement(document);
            rootName = getElementName(root);
        }

        List<String> outputData = new ArrayList<String>();

        if (rootName != null) {

            for (Entry<String, Node> entry : elements.get("xs:element").entrySet()) {
                Node element = entry.getValue();
                String name = entry.getKey();

                if (name.equals(rootName)) {

                    Map<String, Node> printPath = generatePaths(false, "", element, elements);

                    for (String path : printPath.keySet()) {
                        outputData.add(path + "\t" + getElementType(printPath.get(path)));
                    }
                }
            }

            Collections.sort(outputData);

            for (String outLine : outputData) {
                System.out.println(outLine);
            }

        }else{
            System.out.println("Element not found.");
        }
    }
}

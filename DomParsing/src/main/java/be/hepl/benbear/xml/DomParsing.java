package be.hepl.benbear.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DomParsing {

    private final String file;

    public DomParsing(String file) {

        this.file = file;
    }

    private String parse() throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(Files.newInputStream(Paths.get(file)));

        NodeList attributes = document.getElementsByTagName("attribute");
        for(int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);

            if("true".equals(item.getAttributes().getNamedItem("multiple").getTextContent())) {
                // TODO Create other entity
            }


            if(item.getParentNode().getNodeName().equals("attribute")) {
                continue;
            }

            NodeList children = item.getChildNodes();
            for(int j = 0; j < children.getLength(); j++) {
                if(!children.item(j).getNodeName().equals("attribute")) {
                    continue;
                }
                // TODO Convert multi attribute node
            }
        }


        return "";
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.out.println("No xml to read");
            return;
        }
        String parsed = new DomParsing(args[0]).parse();
        System.out.println(parsed);
        try(BufferedWriter writer = Files.newBufferedWriter(
            Paths.get(args[0].replace(".xml", "_mod.xml")),
            StandardCharsets.UTF_8
        )) {
            writer.write(parsed);
        }
    }
}

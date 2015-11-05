package be.hepl.benbear.xml;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DomParsing {

    private final Path file;
    private final Path out;

    public DomParsing(Path file, Path out) {
        this.file = file;
        this.out = out;
    }

    private String attrToString(NodeUtil node) {
        NamedNodeMap attributes = node.getAttributes();
        StringBuilder builder = new StringBuilder().append('{');
        for(int i = 0; i < attributes.getLength(); i++) {
            builder.append(attributes.item(i));
            if(i != attributes.getLength() - 1) {
                builder.append(", ");
            }
        }
        return builder.append('}').toString();
    }

    private void parse() throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(Files.newInputStream(file));
        NodeFactory f = new NodeFactory(document);
        NodeUtil root = f.getByName("schema").get(0);

        List<NodeUtil> attributes;
        while(true) {
            attributes = f.getByName("attribute");
            NodeUtil item = null;
            for(NodeUtil attribute : attributes) {
                if(attribute.getAttribute("multiple").isPresent()) {
                    item = attribute;
                    break;
                }
            }

            if(item == null) {
                break;
            }

            NodeUtil entity = item.closestParent("entity").get();
            String entityName = entity
                .closestChild("name").get()
                .getTextContent();
            String attributeName = item.closestChild("name").get()
                .getTextContent();

            System.out.println("(MULTIPLE) " + entityName + ": " + item.closestChild("name").get().getTextContent());

            // Not multiple anymore
            item.getAttributes().removeNamedItem("multiple");

            root
                .appendChild(
                    // Create the entity
                    f
                        .create("entity")
                        .appendChild(f.create("name", entityName + '_' + attributeName))
                        .appendChild(
                            f
                                .create("attribute")
                                .setAttribute(f, "primary", "true")
                                .appendChild(f.create("name", "id"))
                        )
                        .appendChild(item.cloneNode(true))
                )
                .appendChild(
                    // Create the relation
                    f
                        .create("relation")
                        .appendChild(f.create("name", entityName + '_' + entityName + '_' + attributeName))
                        .appendChild(
                            f
                                .create("from")
                                .appendChild(f.create("entity-name", entityName))
                                .appendChild(f.create("cardinality", "1-n")) // TODO Check this value
                        )
                        .appendChild(
                            f
                                .create("to")
                                .appendChild(f.create("entity-name", entityName + '_' + attributeName))
                                .appendChild(f.create("cardinality", "1-1")) // TODO Check this value
                        )
                );
        }

        /*
            if(!item.closestParent("attribute").isPresent()) {
                continue;
            }

            System.out.println("(MULIVALUE) " + entity
                .closestChild("name").get()
                .getTextContent() + ": " + item.closestChild("name").get().getTextContent()
                + " / " + item.closestParent("attribute").get().closestChild("name").get().getTextContent());
            for(NodeUtil subItem : item.getChildNodes()) {
                if(!subItem.getNodeName().equals("attribute")) {
                    continue;
                }

                // TODO Convert multi attribute node
                //entity.appendChild(
                //    f
                //        .create("attribute")
                //        .appendChild(f.create("name", item.getNo))
                //);

                // Cleanup
                entity.removeChild(item.node);
            }
        }*/

        documentToXml(document);
    }

    private void documentToXml(Document document) throws TransformerException, IOException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer idTransform = transFactory.newTransformer();
        idTransform.setOutputProperty(OutputKeys.METHOD, "xml");
        idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
        Source input = new DOMSource(document);
        Result output = new StreamResult(Files.newBufferedWriter(out, StandardCharsets.UTF_8));
        idTransform.transform(input, output);
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.out.println("No xml to read");
            return;
        }
        new DomParsing(Paths.get(args[0]), Paths.get(args[0].replace(".xml", "_mod.xml"))).parse();
    }
}

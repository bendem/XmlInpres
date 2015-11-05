package be.hepl.benbear.xml;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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

        flattenCompoundValues(f, root);
        extractMultiValues(f, root);
        documentToXml(document);
    }

    private void flattenCompoundValues(NodeFactory f, NodeUtil root) {
        while(true) {
            Optional<NodeUtil> item = f.getByName("attribute").stream()
                .filter(att -> att.closestParent("attribute").isPresent())
                .findFirst();

            if(!item.isPresent()) {
                break;
            }

            NodeUtil entity = item.get().closestParent("entity").get();
            NodeUtil attr = item.get().closestParent("attribute").get();
            String attrName = attr.closestChild("name").get().getTextContent();

            System.out.println("(MULIVALUE) "
                + entity.closestChild("name").get().getTextContent()
                + ": " + attrName
                + " / " + item.get().closestChild("name").get().getTextContent());

            attr.getChildNodes().stream()
                .filter(sub -> sub.getNodeName().equals("attribute"))
                .forEach(sub -> {
                    NodeUtil cloned = sub.cloneNode(true);

                    NodeUtil subName = cloned.closestChild("name").get();
                    subName.setTextContent(attrName + '_' + subName.getTextContent());

                    NamedNodeMap nodeAttributes = attr.getAttributes();
                    for(int i = 0; i < nodeAttributes.getLength(); i++) {
                        cloned.getAttributes().setNamedItem(nodeAttributes.item(i).cloneNode(false));
                    }

                    entity.appendChild(cloned);
                });
            entity.removeChild(attr);
        }
    }

    private void extractMultiValues(NodeFactory f, NodeUtil root) {
        while(true) {
            Optional<NodeUtil> item = f.getByName("attribute").stream()
                .filter(att -> att.getAttribute("multiple").isPresent())
                .findFirst();

            if(!item.isPresent()) {
                break;
            }

            NodeUtil attr = item.get();
            String entityName = attr
                .closestParent("entity").get()
                .closestChild("name").get()
                .getTextContent();
            String attributeName = attr
                .closestChild("name").get()
                .getTextContent();

            System.out.println("(MULTIPLE) " + entityName + ": " + attr.closestChild("name").get().getTextContent());

            // Not multiple anymore
            attr.getAttributes().removeNamedItem("multiple");

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
                        .appendChild(attr.cloneNode(true))
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
                                .appendChild(f.create("cardinality", "1-n"))
                        )
                        .appendChild(
                            f
                                .create("to")
                                .appendChild(f.create("entity-name", entityName + '_' + attributeName))
                                .appendChild(f.create("cardinality", "1-1"))
                        )
                );
        }
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

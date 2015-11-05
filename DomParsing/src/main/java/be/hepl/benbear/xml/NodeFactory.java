package be.hepl.benbear.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NodeFactory {

    private final Document document;

    public NodeFactory(Document document) {
        this.document = document;
    }

    public NodeUtil create(String name) {
        return new NodeUtil(document.createElement(name));
    }

    public NodeUtil create(String name, String text) {
        return new NodeUtil(document.createElement(name)).setTextContent(text);
    }

    public Attr createAttribute(String name, String value) {
        Attr attribute = document.createAttribute(name);
        attribute.setValue(value);
        return attribute;
    }

    public List<NodeUtil> getByName(String name) {
        List<NodeUtil> list = new ArrayList<>();
        NodeList elements = document.getElementsByTagName(name);
        for(int i = 0; i < elements.getLength(); i++) {
            list.add(new NodeUtil(elements.item(i)));
        }
        return list;
    }
}

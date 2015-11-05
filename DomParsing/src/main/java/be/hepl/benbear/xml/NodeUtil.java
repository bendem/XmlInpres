package be.hepl.benbear.xml;

import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeUtil {

    private static Optional<Node> closestChildEntity(Node node, String name) {
        NodeList children = node.getChildNodes();

        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i).getNodeName().equals(name)) {
                return Optional.of(children.item(i));
            }
        }

        for(int i = 0; i < children.getLength(); i++) {
            Optional<Node> child = closestChildEntity(children.item(i), name);
            if(child.isPresent()) {
                return child;
            }
        }

        return Optional.empty();
    }

    private static Optional<Node> closestParentEntity(Node node, String name) {
        Node parent = node.getParentNode();
        while(parent != null) {
            if(name.equals(parent.getNodeName())) {
                return Optional.of(parent);
            }
            parent = parent.getParentNode();
        }

        return Optional.empty();
    }

    public final Node node;

    public NodeUtil(Node node) {
        this.node = node;
    }

    public Optional<NodeUtil> closestChild(String name) {
        return closestChildEntity(node, name).map(NodeUtil::new);
    }

    public Optional<NodeUtil> closestParent(String name) {
        return closestParentEntity(node, name).map(NodeUtil::new);
    }

    public NodeUtil appendChild(NodeUtil node) {
        appendChild(node.node);
        return this;
    }

    public NodeUtil removeChild(NodeUtil node) {
        removeChild(node.node);
        return this;
    }

    public Optional<String> getAttribute(String name) {
        NamedNodeMap attributes = getAttributes();
        Node item;
        for(int i = 0; i < attributes.getLength(); i++) {
            item = attributes.item(i);
            if(item.getNodeName().equals(name)) {
                return Optional.of(item.getNodeValue());
            }
        }
        return Optional.empty();
    }

    public NodeUtil setAttribute(NodeFactory f, String name, String value) {
        node.getAttributes().setNamedItem(f.createAttribute(name, value));
        return this;
    }

    // ====================================
    // Proxy
    public String getNodeName() {
        return node.getNodeName();
    }

    public NamedNodeMap getAttributes() {
        return node.getAttributes();
    }

    public String getTextContent() throws DOMException {
        return node.getTextContent();
    }

    public NodeUtil getFirstChild() {
        return new NodeUtil(node.getFirstChild());
    }

    public NodeUtil getNextSibling() {
        return new NodeUtil(node.getNextSibling());
    }

    public Object getFeature(String s, String s1) {
        return node.getFeature(s, s1);
    }

    public String getLocalName() {
        return node.getLocalName();
    }

    public String lookupNamespaceURI(String s) {
        return node.lookupNamespaceURI(s);
    }

    public Document getOwnerDocument() {
        return node.getOwnerDocument();
    }

    public boolean isEqualNode(Node node) {
        return this.node.isEqualNode(node);
    }

    public short getNodeType() {
        return node.getNodeType();
    }

    public short compareDocumentPosition(Node node) throws DOMException {
        return this.node.compareDocumentPosition(node);
    }

    public Object setUserData(String s, Object o, UserDataHandler userDataHandler) {
        return node.setUserData(s, o, userDataHandler);
    }

    public NodeUtil getParentNode() {
        return new NodeUtil(node.getParentNode());
    }

    public String getBaseURI() {
        return node.getBaseURI();
    }

    public List<NodeUtil> getChildNodes() {
        List<NodeUtil> list = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i) {
            list.add(new NodeUtil(children.item(i)));
        }
        return list;
    }

    public NodeUtil getPreviousSibling() {
        return new NodeUtil(node.getPreviousSibling());
    }

    public boolean isSameNode(Node node) {
        return this.node.isSameNode(node);
    }

    public boolean hasAttributes() {
        return node.hasAttributes();
    }

    public NodeUtil insertBefore(Node node, Node node1) throws DOMException {
        return new NodeUtil(this.node.insertBefore(node, node1));
    }

    public NodeUtil setNodeValue(String s) throws DOMException {
        node.setNodeValue(s);
        return this;
    }

    public String getNamespaceURI() {
        return node.getNamespaceURI();
    }

    public boolean hasChildNodes() {
        return node.hasChildNodes();
    }

    public NodeUtil setTextContent(String s) throws DOMException {
        node.setTextContent(s);
        return this;
    }

    public NodeUtil replaceChild(Node node, Node node1) throws DOMException {
        return new NodeUtil(this.node.replaceChild(node, node1));
    }

    public NodeUtil normalize() {
        node.normalize();
        return this;
    }

    public String getPrefix() {
        return node.getPrefix();
    }

    public boolean isDefaultNamespace(String s) {
        return node.isDefaultNamespace(s);
    }

    public Object getUserData(String s) {
        return node.getUserData(s);
    }

    public boolean isSupported(String s, String s1) {
        return node.isSupported(s, s1);
    }

    public NodeUtil cloneNode(boolean b) {
        return new NodeUtil(node.cloneNode(b));
    }

    public NodeUtil getLastChild() {
        return new NodeUtil(node.getLastChild());
    }

    public String lookupPrefix(String s) {
        return node.lookupPrefix(s);
    }

    public String getNodeValue() throws DOMException {
        return node.getNodeValue();
    }

    public NodeUtil appendChild(Node node) throws DOMException {
        return new NodeUtil(this.node.appendChild(node));
    }

    public NodeUtil removeChild(Node node) throws DOMException {
        return new NodeUtil(this.node.removeChild(node));
    }

    public NodeUtil setPrefix(String s) throws DOMException {
        node.setPrefix(s);
        return this;
    }
}

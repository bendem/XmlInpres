package be.hepl.benbear.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SaxParsing {

    private final String file;

    public SaxParsing(String file) {
        this.file = file;
    }

    public String parse() throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(true);
        SAXParser parser = parserFactory.newSAXParser();
        StringBuilder builder = new StringBuilder();
        Box<Integer> level = new Box<>(0);

        parser.parse(Files.newInputStream(Paths.get(file)), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                builder
                    .append(indent(level.item++))
                    .append(qName)
                    ;

                if(attributes.getLength() != 0) {
                    builder.append(" [");
                    for(int i = 0; i < attributes.getLength(); ++i) {
                        builder
                            .append(attributes.getQName(i))
                            .append('=')
                            .append(attributes.getValue(i))
                            ;
                        if(i != attributes.getLength() - 1) {
                            builder.append(", ");
                        }
                    }
                    builder.append(']');
                }

                builder.append(" (\n");
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                builder
                    .append(indent(--level.item))
                    .append(")\n")
                    ;
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                builder
                    .append(indent(level.item))
                    .append('"')
                    .append(ch, start, length)
                    .append('"')
                    .append('\n')
                    ;
            }

            @Override
            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });

        return builder.toString();
    }

    private static char[] indent(int level) {
        char[] a = new char[level * 2];
        Arrays.fill(a, ' ');
        return a;
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.out.println("No xml to read");
            return;
        }
        String parsed = new SaxParsing(args[0]).parse();
        System.out.println(parsed);
        Files.newBufferedWriter(
            Paths.get(args[0].replace(".xml", ".txt")),
            StandardCharsets.UTF_8
        ).write(parsed);
    }
}

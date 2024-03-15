package gerar_XML;

import interfaces.GerarPersistencia;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

public class Gerar_XML implements GerarPersistencia {

    private String fileType;

    @Override
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    @Override
    public String readPersistencia() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("lista.xml"));
            return documentToString(document);
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            return "Erro ao ler o arquivo XML: " + e.getMessage();
        }
    }

    @Override
    public void writePersistencia(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(content);
            writeXML(document, "lista.xml");
            System.out.println("Arquivo XML atualizado com sucesso!");
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            System.out.println("Erro ao escrever no arquivo XML: " + e.getMessage());
        }
    }

    private void writeXML(Document document, String filePath) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }

    private String documentToString(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File("temp.xml"));
        transformer.transform(source, result);
        return result.getOutputStream().toString();
    }
}

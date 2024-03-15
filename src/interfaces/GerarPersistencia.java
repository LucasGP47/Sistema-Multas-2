package interfaces;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

public interface GerarPersistencia {
    String readPersistencia() throws XMLStreamException, IOException, SAXException, ParserConfigurationException;
	void setFileType(String fileType);
	String getFileType();
	void writePersistencia(String content);
}

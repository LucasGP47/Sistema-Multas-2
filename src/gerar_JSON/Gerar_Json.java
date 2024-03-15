package gerar_JSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import interfaces.GerarPersistencia;

public class Gerar_Json implements GerarPersistencia {
	private String fileType;

    public Gerar_Json() {
       
    }
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
            Path path = Paths.get("C:\\Users\\lucas\\OneDrive\\Documentos\\LISTA_POO\\lista.json"); 
            return new String(Files.readAllBytes(path));
        } catch (NoSuchFileException e) {
            return "O arquivo JSON não foi encontrado.";
        } catch (IOException e) {
            return "Erro ao ler o arquivo JSON: " + e.getMessage();
        }
    }

	
    @Override
    public void writePersistencia(String content) {
        try {
            Path path = Paths.get("C:\\Users\\lucas\\OneDrive\\Documentos\\LISTA_POO\\lista.json");
            Files.write(path, content.getBytes());
            System.out.println("Arquivo JSON atualizado com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao escrever no arquivo JSON");
        }
    }
}

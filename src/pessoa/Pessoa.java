package pessoa;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import conexao_SQL.Conexao_SQL;
import gerar_JSON.Gerar_Json;
import gerar_XML.Gerar_XML;
import interfaces.PessoaService;

public class Pessoa implements PessoaService {

    private String CNH;
    private String cpf;
    
    public String consultarCNH(String titular) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray pessoas = json.getJSONArray("pessoas");

            for (int i = 0; i < pessoas.length(); i++) {
                JSONObject pessoa = pessoas.getJSONObject(i);
                if (pessoa.getString("nome").equalsIgnoreCase(titular)) {
                    return pessoa.getString("cnh");
                }
            }

            return "CNH não encontrada para o titular: " + titular;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter CNH em JSON.";
        }
    }

    @Override
    public boolean pesquisarNome(String name, String fileType) {
        switch (fileType.toUpperCase()) {
            case "SQL":
                return pesquisarNomeSQL(name);
            case "JSON":
                return pesquisarNomeJson(name);
            case "XML":
                return pesquisarNomeXML(name);    
            default:
                return false;
        }
    }
    
    private boolean pesquisarNomeXML(String name) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            String xmlContent = xmlManager.readPersistencia();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlContent)));
            document.getDocumentElement().normalize();

            NodeList pessoasList = document.getElementsByTagName("pessoa");

            for (int temp = 0; temp < pessoasList.getLength(); temp++) {
                Element pessoaElement = (Element) pessoasList.item(temp);
                String pessoaNome = pessoaElement.getAttribute("nome");
                if (pessoaNome.equalsIgnoreCase(name)) {
                    this.cpf = pessoaElement.getAttribute("cpf");
                    this.CNH = pessoaElement.getAttribute("cnh");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean pesquisarNomeJson(String name) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
         
            if (!jsonContentStr.trim().startsWith("{")) {
                throw new JSONException("Conteúdo JSON inválido");
            }
            
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray pessoas = json.getJSONArray("pessoas");

            for (int i = 0; i < pessoas.length(); i++) {
                JSONObject pessoa = pessoas.getJSONObject(i);
                if (pessoa.getString("nome").equalsIgnoreCase(name)) {
                    this.cpf = pessoa.getString("cpf");
                    this.CNH = pessoa.getString("cnh");
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean pesquisarNomeSQL(String name) {
        try {
            Connection con = Conexao_SQL.run_connection();
            String usuario = "select * from pessoa_fisica where nome=?";
            PreparedStatement stmt = con.prepareStatement(usuario);
            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.cpf = rs.getString("cpf");
                this.CNH = rs.getString("cnh");
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String status(String name, String fileType) {
    	switch (fileType.toUpperCase()) {
    	case "SQL":
    		return statusSQL(name);
    	case "JSON":
    		return statusJson(name);
    	case "XML":
            return statusXML(name);	
    	default:
            return "Tipo de persistência não suportado: " + fileType;
    	}
    }
    
    private String statusXML(String name) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            String xmlContent = xmlManager.readPersistencia();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlContent.getBytes());
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();

            NodeList pessoasList = doc.getElementsByTagName("pessoa");

            StringBuilder result = new StringBuilder();

            for (int temp = 0; temp < pessoasList.getLength(); temp++) {
                Element pessoaElement = (Element) pessoasList.item(temp);
                String pessoaNome = pessoaElement.getAttribute("nome");
                if (pessoaNome.equalsIgnoreCase(name)) {
                    result.append("\n//////////////////////////////////\n");
                    result.append(String.format("Nome: %s \n", pessoaNome));
                    result.append(String.format("CPF: %s \n", pessoaElement.getAttribute("cpf")));
                    result.append(String.format("CNH: %s \n", pessoaElement.getAttribute("cnh")));

                    return result.toString();
                }
            }

            return "Nenhum resultado encontrado.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados do XML.";
        }
    }

    private String statusJson(String name) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray pessoas = json.getJSONArray("pessoas");

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < pessoas.length(); i++) {
                JSONObject pessoa = pessoas.getJSONObject(i);
                if (pessoa.getString("nome").equalsIgnoreCase(name)) {
                    result.append("\n//////////////////////////////////\n");
                    result.append(String.format("Nome: %s \n", pessoa.getString("nome")));
                    result.append(String.format("CPF: %s \n", pessoa.getString("cpf")));
                    result.append(String.format("CNH: %s \n", pessoa.getString("cnh")));

                    return result.toString();
                }
            }

            return "Nenhum resultado encontrado.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados em JSON.";
        }
    }

    
    private String statusSQL(String name) {
        try {
            Connection con = Conexao_SQL.run_connection();
            String query = "SELECT * FROM pessoa_fisica WHERE nome=?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder();

            if (rs.next()) {
                result.append("\n//////////////////////////////////\n");
                result.append(String.format("Nome: %s \n", rs.getString("nome")));
                result.append(String.format("CPF: %s \n", rs.getString("cpf")));
                result.append(String.format("CNH: %s \n", rs.getString("cnh")));

                return result.toString();
            } else {
                return "Nenhum resultado encontrado.";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Erro ao obter dados.";
        }
    }

    @Override
    public String getCPF() {
        return cpf;
    }

    @Override
    public void setCPF(String cpf) {
        this.cpf = cpf;
    }

    @Override
    public String getCNH() {
        return CNH;
    }

    @Override
    public void setCNH(String cNH) {
        CNH = cNH;
    }
}

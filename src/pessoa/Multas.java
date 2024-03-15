package pessoa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import conexao_SQL.Conexao_SQL;
import gerar_JSON.Gerar_Json;
import gerar_XML.Gerar_XML;
import interfaces.CarteiraService;
import interfaces.MultasService;

public class Multas implements MultasService {

	public Multas(CarteiraService carteira) {
    }

    @Override
    public String consultarMulta(String cpf, String placa, String fileType) {
    	switch (fileType.toUpperCase()) {
    	case "SQL":
    		return consultarMultaSQL(cpf, placa);
    	case "JSON":
    		return consultarMultaJson(cpf, placa);
        case "XML":
            return consultarMultaXML(cpf, placa);	
    	default:
    		return "formato nao suportado";
    	}
    	
    }
    
    private String consultarMultaXML(String cpf, String placa) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            NodeList multasList = xmlManager.getNodeList("multa");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < multasList.getLength(); i++) {
                Node node = multasList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element multaElement = (Element) node;
                    String multaCpf = multaElement.getAttribute("cpf");
                    String multaPlaca = multaElement.getAttribute("placa_veiculo");

                    if (multaCpf.equalsIgnoreCase(cpf) && multaPlaca.equalsIgnoreCase(placa)) {
                        result.append("\n//////////////////////////////////\n");
                        result.append(String.format("CPF: %s \n", multaCpf));
                        result.append(String.format("Placa do Veículo: %s \n", multaPlaca));
                        result.append(String.format("Valor da Multa: %s \n", multaElement.getAttribute("valor")));
                        result.append(String.format("Data da Multa: %s \n", multaElement.getAttribute("data_aplicacao")));
                        result.append(String.format("Motivo da multa: %s \n", multaElement.getAttribute("descricao")));
                        return result.toString();
                    }
                }
            }
            result.append("Nenhuma multa encontrada para o CPF e placa fornecidos.");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados da multa em XML.";
        }
    }

    private String consultarMultaJson(String cpf, String placa) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray multasArray = json.getJSONArray("multas");

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < multasArray.length(); i++) {
                JSONObject multa = multasArray.getJSONObject(i);
                if (multa.getString("cpf").equalsIgnoreCase(cpf) && multa.getString("placa_veiculo").equalsIgnoreCase(placa)) {
                    result.append("\n//////////////////////////////////\n");
                    result.append(String.format("CPF: %s \n", multa.getString("cpf")));
                    result.append(String.format("Placa do Veículo: %s \n", multa.getString("placa_veiculo")));
                    result.append(String.format("Valor da Multa: %s \n", multa.getInt("valor")));
                    result.append(String.format("Data da Multa: %s \n", multa.getString("data_aplicacao")));
                    result.append(String.format("Motivo da multa: %s \n", multa.getString("descricao")));
                    return result.toString();
                }
            }

            result.append("Nenhuma multa encontrada para o CPF e placa fornecidos.");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados da multa em JSON.";
        }
    }
    
    private String consultarMultaSQL(String cpf, String placa) {
        try {
            Connection con = Conexao_SQL.run_connection();
            String query = "SELECT * FROM multas WHERE cpf = ? AND placa_veiculo = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, cpf);
            stmt.setString(2, placa);

            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder();

            if (rs.next()) {
                result.append("\n//////////////////////////////////\n");
                result.append(String.format("CPF: %s \n", rs.getString("cpf")));
                result.append(String.format("Placa do Veículo: %s \n", rs.getString("placa_veiculo")));
                result.append(String.format("Valor da Multa: %s \n", rs.getString("valor")));
                result.append(String.format("Data da Multa: %s \n", rs.getString("data_aplicacao")));
                result.append(String.format("Motivo da multa: %s \n", rs.getString("descricao")));
            } else {
                result.append("Nenhuma multa encontrada para o CPF e placa fornecidos.");
            }

            return result.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erro ao obter dados da multa.";
        }
    }
    
    @Override
    public double obterValorMulta(String cpf, String placa, String fileType) {
       	switch (fileType.toUpperCase()) {
    	case "SQL":
    		return obterValorMultaSQL(cpf, placa);
    	case "JSON":
    		return obterValorMultaJson(cpf, placa);
    	case "XML":
            return obterValorMultaXML(cpf, placa);	
    	default:
    		return 0;
    	}
    }
    
    private double obterValorMultaXML(String cpf, String placa) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            Document doc = xmlManager.lerXML();
            doc.getDocumentElement().normalize();

            NodeList multasList = doc.getElementsByTagName("multa");

            double valorMulta = 0;

            for (int temp = 0; temp < multasList.getLength(); temp++) {
                Element multaElement = (Element) multasList.item(temp);

                String multaCpf = multaElement.getAttribute("cpf");
                String multaPlaca = multaElement.getAttribute("placa_veiculo");

                if (multaCpf.equalsIgnoreCase(cpf) && multaPlaca.equalsIgnoreCase(placa)) {
                    valorMulta = Double.parseDouble(multaElement.getAttribute("valor"));
                    break;
                }
            }

            return valorMulta;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private double obterValorMultaJson(String cpf, String placa) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray multasArray = json.getJSONArray("multas");

            double valorMulta = 0;

            for (int i = 0; i < multasArray.length(); i++) {
                JSONObject multa = multasArray.getJSONObject(i);
                if (multa.getString("cpf").equalsIgnoreCase(cpf) && multa.getString("placa_veiculo").equalsIgnoreCase(placa)) {
                    valorMulta = multa.getDouble("valor");
                    break;
                }
            }

            return valorMulta;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private double obterValorMultaSQL(String cpf, String placa) {
        try {
            Connection con = Conexao_SQL.run_connection();
            String query = "SELECT valor FROM multas WHERE cpf = ? AND placa_veiculo = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, cpf);
            stmt.setString(2, placa);

            ResultSet rs = stmt.executeQuery();

            double valorMulta = 0;
            if (rs.next()) {
                valorMulta = rs.getDouble("valor");
            }

            return valorMulta;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

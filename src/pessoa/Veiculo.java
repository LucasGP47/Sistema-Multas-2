package pessoa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import interfaces.VeiculoService;

public class Veiculo implements VeiculoService {

	public Veiculo(PessoaService pessoa) {
    }
	
    @Override
    public String consultarVeiculosMultadosPorCNH(String CNH, String fileType) {
        switch (fileType.toUpperCase()) {
            case "SQL":
                return consultarVeiculosMultadosPorCNHSQL(CNH);
            case "JSON":
                return consultarVeiculosMultadosPorCNHJson(CNH);
            case "XML":
            	return consultarVeiculosMultadosPorCNHXML(CNH);
            default:
                return "Formato não suportado";
        }
    }
    
    private String consultarVeiculosMultadosPorCNHXML(String CNH) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            Document doc = xmlManager.lerXML();
            doc.getDocumentElement().normalize();

            NodeList veiculosList = doc.getElementsByTagName("veiculo");

            StringBuilder result = new StringBuilder();

            for (int temp = 0; temp < veiculosList.getLength(); temp++) {
                Element veiculoElement = (Element) veiculosList.item(temp);
                String cnhTitular = veiculoElement.getAttribute("cnh_titular");
                String situacaoVeiculo = veiculoElement.getAttribute("situacao_veiculo");
                if (cnhTitular.equalsIgnoreCase(CNH) && situacaoVeiculo.equalsIgnoreCase("MULTADO")) {
                    result.append("\n//////////////////////////////////\n");
                    result.append(String.format("Placa: %s \n", veiculoElement.getAttribute("placa")));
                    result.append(String.format("Nome do Titular: %s \n", veiculoElement.getAttribute("nome_titular")));
                    result.append(String.format("Situação do Veículo: %s \n", situacaoVeiculo));
                    result.append(String.format("Valor da Multa: %s \n", veiculoElement.getAttribute("valor_multa")));
                }
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados do veículo em XML.";
        }
    }
    
    private String consultarVeiculosMultadosPorCNHJson(String CNH) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray veiculos = json.getJSONArray("veiculos");

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < veiculos.length(); i++) {
                JSONObject veiculo = veiculos.getJSONObject(i);
                if (veiculo.getString("cnh_titular").equalsIgnoreCase(CNH) && veiculo.getString("situacao_veiculo").equalsIgnoreCase("MULTADO")) {
                    result.append("\n//////////////////////////////////\n");
                    result.append(String.format("Placa: %s \n", veiculo.getString("placa")));
                    result.append(String.format("Nome do Titular: %s \n", veiculo.getString("nome_titular")));
                    result.append(String.format("Situação do Veículo: %s \n", veiculo.getString("situacao_veiculo")));
                    result.append(String.format("Valor da Multa: %s \n", veiculo.getString("valor_multa")));
                }
            }

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados do veículo em JSON.";
        }
    }


    private String consultarVeiculosMultadosPorCNHSQL(String CNH) {
        try {
            Connection con = Conexao_SQL.run_connection();
            String query = "SELECT * FROM veiculo WHERE cnh = ? AND situacao_veiculo = 'MULTADO'";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, CNH);

            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder();

            while (rs.next()) {
                result.append("\n//////////////////////////////////\n");
                result.append(String.format("Placa: %s \n", rs.getString("PLACA")));
                result.append(String.format("Nome do Titular: %s \n", rs.getString("nome_titular")));
                result.append(String.format("Situação do Veículo: %s \n", rs.getString("situacao_veiculo")));
                result.append(String.format("Valor da Multa: %s \n", rs.getString("valor_multa")));
            }

            return result.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erro ao obter dados do veículo.";
        }
    }
    
    public double obterValorMultaPorVeiculo(String placa, String fileType) {
    	switch (fileType.toUpperCase()) {
    	case "SQL":
    		return obterValorMultaPorVeiculoSQL(placa);
    	case "JSON":
    		return obterValorMultaPorVeiculoJson(placa);
    	case "XML":
    		return obterValorMultaPorVeiculoXML(placa);	
    	default:
    		return 0;
    	}
    }
    
    private double obterValorMultaPorVeiculoXML(String placa) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            Document doc = xmlManager.lerXML();
            doc.getDocumentElement().normalize();

            NodeList veiculosList = doc.getElementsByTagName("veiculo");

            double valorMulta = 0;

            for (int temp = 0; temp < veiculosList.getLength(); temp++) {
                Element veiculoElement = (Element) veiculosList.item(temp);

                String veiculoPlaca = veiculoElement.getAttribute("placa");

                if (veiculoPlaca.equalsIgnoreCase(placa)) {
                    valorMulta = Double.parseDouble(veiculoElement.getAttribute("valor_multa"));
                    break;
                }
            }

            return valorMulta;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private double obterValorMultaPorVeiculoJson(String placa) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray veiculos = json.getJSONArray("veiculos");

            double valorMulta = 0;

            for (int i = 0; i < veiculos.length(); i++) {
                JSONObject veiculo = veiculos.getJSONObject(i);
                if (veiculo.getString("placa").equalsIgnoreCase(placa)) {
                    valorMulta = veiculo.getDouble("valor_multa");
                    break; 
                }
            }

            return valorMulta;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double obterValorMultaPorVeiculoSQL(String placa) {
        try {
            Connection con = Conexao_SQL.run_connection();
            String query = "SELECT valor_multa FROM veiculo WHERE PLACA = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, placa);

            ResultSet rs = stmt.executeQuery();

            double valorMulta = 0;
            if (rs.next()) {
                valorMulta = rs.getDouble("valor_multa");
            }

            return valorMulta;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public void pagarMulta(String placa, double valorPago, String fileType) {
    	switch (fileType.toUpperCase()) {
    	case "SQL": 
    		pagarMultaSQL(placa, valorPago, fileType);
    	case "JSON":
    		pagarMultasJson(placa, valorPago, fileType);
    	case "XML":
    		pagarMultasXML(placa, valorPago);
    	}
    }
    
    private void pagarMultasXML(String placa, double valorPago) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            Document doc = xmlManager.lerXML();
            doc.getDocumentElement().normalize();

            NodeList veiculosList = doc.getElementsByTagName("veiculo");

            for (int temp = 0; temp < veiculosList.getLength(); temp++) {
                Element veiculoElement = (Element) veiculosList.item(temp);

                String veiculoPlaca = veiculoElement.getAttribute("placa");

                if (veiculoPlaca.equalsIgnoreCase(placa)) {
                    double valorMultaAtual = Double.parseDouble(veiculoElement.getAttribute("valor_multa"));

                    if (valorPago >= valorMultaAtual) {
                        veiculoElement.setAttribute("valor_multa", "0");
                        veiculoElement.setAttribute("situacao_veiculo", "REGULAR");
                        System.out.println("Multa paga com sucesso para o veículo com placa " + placa + "!");
                    } else {
                        double novoValorMulta = valorMultaAtual - valorPago;
                        veiculoElement.setAttribute("valor_multa", String.valueOf(novoValorMulta));
                        System.out.println("Parte da multa paga para o veículo com placa " + placa + "! Valor restante: " + novoValorMulta);
                    }
                    xmlManager.escreverXML(doc);

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao pagar a multa do veículo com placa " + placa);
        }
    }

    
    private void pagarMultasJson(String placa, double valorPago, String fileType) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray veiculos = json.getJSONArray("veiculos");

            for (int i = 0; i < veiculos.length(); i++) {
                JSONObject veiculo = veiculos.getJSONObject(i);
                if (veiculo.getString("placa").equalsIgnoreCase(placa)) {
                    double valorMultaAtual = veiculo.getDouble("valor_multa");

                    if (valorPago >= valorMultaAtual) {
                        veiculo.put("valor_multa", 0);
                        veiculo.put("situacao_veiculo", "REGULAR");
                        System.out.println("Multa paga com sucesso para o veículo com placa " + placa + "!");
                    } else {
                        double novoValorMulta = valorMultaAtual - valorPago;
                        veiculo.put("valor_multa", novoValorMulta);
                        System.out.println("Parte da multa paga para o veículo com placa " + placa + "! Valor restante: " + novoValorMulta);
                    }

                    jsonContent.writePersistencia(json.toString());
                    break; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao pagar a multa do veículo com placa " + placa);
        }
    }


    private void pagarMultaSQL(String placa, double valorPago, String fileType) {
        try {
            Connection con = Conexao_SQL.run_connection();
            double valorMultaAtual = obterValorMultaPorVeiculo(placa, fileType);

            if (valorPago >= valorMultaAtual) {
                String updateQuery = "UPDATE veiculo SET valor_multa = 0, situacao_veiculo = 'REGULAR' WHERE PLACA = ?";
                PreparedStatement updateStmt = con.prepareStatement(updateQuery);
                updateStmt.setString(1, placa);
                updateStmt.executeUpdate();

                System.out.println("Multa paga com sucesso para o veículo com placa " + placa + "!");
            } else {
                double novoValorMulta = valorMultaAtual - valorPago;
                String updateQuery = "UPDATE veiculo SET valor_multa = ? WHERE PLACA = ?";
                PreparedStatement updateStmt = con.prepareStatement(updateQuery);
                updateStmt.setDouble(1, novoValorMulta);
                updateStmt.setString(2, placa);
                updateStmt.executeUpdate();

                System.out.println("Parte da multa paga para o veículo com placa " + placa + "! Valor restante: " + novoValorMulta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao pagar a multa do veículo com placa " + placa);
        }
    }
    
    public String consultarVeiculos(String CNH, String fileType) {
    	switch(fileType.toUpperCase()) {
    	case "SQL":
    		return consultarVeiculosSQL(CNH);
    	case "JSON":
    		return consultarVeiculosJson(CNH);
    	case "XML":
    		return consultarVeiculosXML(CNH);	
    	default:
    		return "num rolou";
    	}
    }
    
    private String consultarVeiculosXML(String CNH) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            Document doc = xmlManager.lerXML();
            doc.getDocumentElement().normalize();

            NodeList veiculosList = doc.getElementsByTagName("veiculo");

            StringBuilder result = new StringBuilder();

            for (int temp = 0; temp < veiculosList.getLength(); temp++) {
                Element veiculoElement = (Element) veiculosList.item(temp);

                if (pessoaPossuiCNH((JSONObject) veiculoElement, CNH)) {
                    result.append("\n//////////////////////////////////\n");
                    result.append(String.format("Placa: %s \n", veiculoElement.getAttribute("placa")));
                    result.append(String.format("Nome do Titular: %s \n", veiculoElement.getAttribute("nome_titular")));
                    result.append(String.format("Situação do Veículo: %s \n", veiculoElement.getAttribute("situacao_veiculo")));
                    result.append(String.format("Valor da Multa: %s \n", veiculoElement.getAttribute("valor_multa")));
                }
            }

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados do veículo em XML.";
        }
    }
    
    private String consultarVeiculosJson(String CNH) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            JSONArray veiculos = json.getJSONArray("veiculos");

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < veiculos.length(); i++) {
                JSONObject veiculo = veiculos.getJSONObject(i);
                if (pessoaPossuiCNH(veiculo, CNH)) {
                    result.append("\n//////////////////////////////////\n");
                    result.append(String.format("Placa: %s \n", veiculo.getString("placa")));
                    result.append(String.format("Nome do Titular: %s \n", veiculo.getString("nome_titular")));
                    result.append(String.format("Situação do Veículo: %s \n", veiculo.getString("situacao_veiculo")));
                    result.append(String.format("Valor da Multa: %s \n", veiculo.getInt("valor_multa")));
                }
            }

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados do veículo em JSON.";
        }
    }

    private boolean pessoaPossuiCNH(JSONObject veiculo, String CNH) {
        try {
            String titular = veiculo.getString("nome_titular");
            Pessoa pessoa = new Pessoa();
            return pessoa.consultarCNH(titular).equals(CNH);
        } catch (JSONException e) {
            return false;
        }
    }
    
    private String consultarVeiculosSQL(String CNH) {
    	try {
            Connection con = Conexao_SQL.run_connection();
            String query = "SELECT * FROM veiculo WHERE cnh = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, CNH);

            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder();

            while (rs.next()) {
                result.append("\n//////////////////////////////////\n");
                result.append(String.format("Placa: %s \n", rs.getString("PLACA")));
                result.append(String.format("Nome do Titular: %s \n", rs.getString("nome_titular")));
                result.append(String.format("Situação do Veículo: %s \n", rs.getString("situacao_veiculo")));
                result.append(String.format("Valor da Multa: %s \n", rs.getString("valor_multa")));
            }

            return result.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erro ao obter dados do veículo.";
        }
    }
}

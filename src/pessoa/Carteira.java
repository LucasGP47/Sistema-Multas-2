package pessoa;

import java.io.ByteArrayInputStream;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

public class Carteira implements CarteiraService {

    private Pessoa pessoa;
    private Veiculo veiculo;
    private String CNH;
    private int multa;

    public Carteira(Pessoa pessoa, Veiculo veiculo) {
        this.setPessoa(pessoa);
        this.veiculo = veiculo;
    }
    
    public String statusCarteira(String name, String fileType) {
   
    	    switch (fileType.toUpperCase()) {
    	        case "SQL":
    	            return statusCarteiraSQL(name, fileType);
    	        case "JSON":
    	            return statusCarteiraJSON(name, fileType);
    	        case "XML":
    	        	return statusCarteiraXML(name, fileType);
    	        default:
    	            return "Tipo de persistência não suportado: " + fileType;
    	    }
    	}
    
    private String statusCarteiraXML(String name, String fileType) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            String xmlContent = xmlManager.readPersistencia();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlContent.getBytes());
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();

            NodeList carteiraList = doc.getElementsByTagName("carteira");

            for (int i = 0; i < carteiraList.getLength(); i++) {
                Node node = carteiraList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element carteiraElement = (Element) node;
                    if (carteiraElement.getAttribute("nome").equalsIgnoreCase(name)) {
                        String cnh = carteiraElement.getAttribute("cnh");
                        int qtyVeiculos = Integer.parseInt(carteiraElement.getAttribute("qty_veiculos"));
                        double valorMulta = Double.parseDouble(carteiraElement.getAttribute("valor_multa"));

                        StringBuilder result = new StringBuilder();
                        result.append("\n//////////////////////////////////\n");
                        result.append(String.format("CNH: %s \n", cnh));
                        result.append(String.format("Nome: %s \n", name));
                        result.append(String.format("Número de veículos cadastrados: %d \n", qtyVeiculos));
                        result.append(String.format("Valor total da multa: %.2f \n", valorMulta));

                        return result.toString();
                    }
                }
            }
            return "Nenhum resultado encontrado.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados em XML.";
        }
    }


    private String statusCarteiraSQL(String name, String fileType) {
    		try {
    			Connection con = Conexao_SQL.run_connection();
    			String query = "SELECT * FROM situacao_carteira WHERE nome LIKE ?";
    			PreparedStatement stmt = con.prepareStatement(query);
    			stmt.setString(1, name);

    			ResultSet rs = stmt.executeQuery();

    			StringBuilder result = new StringBuilder();

    			if (rs.next()) {
    				this.CNH = rs.getString("CNH");
    				this.multa = rs.getInt("valor_multa");

    				result.append("\n//////////////////////////////////\n");
    				result.append(String.format("CNH: %s \n", rs.getString("CNH")));
    				result.append(String.format("Nome: %s \n", rs.getString("nome")));
    				result.append(String.format("Número de veículos cadastrados: %s \n", rs.getString("qty_veiculos")));
    				result.append(String.format("Valor total da multa: %s \n", rs.getString("valor_multa")));

    				String infoVeiculos = veiculo.consultarVeiculosMultadosPorCNH(this.CNH, fileType);
    				if (this.multa > 0)
    					result.append("\nVeículos Multados Associados:\n").append(infoVeiculos);

    				return result.toString();
    			} else {
    				return "Nenhum resultado encontrado.";
    			}
    		} catch (SQLException e) {
    			e.printStackTrace();
    			return "Erro ao obter dados.";
    		}
    }
    
    private String statusCarteiraJSON(String name, String fileType) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String JsonCont = jsonContent.readPersistencia(); 
            JSONObject json = new JSONObject(JsonCont);
            JSONArray carteiras = json.getJSONArray("carteiras");
            JSONObject resultCarteira = null;
            for (int i = 0; i < carteiras.length(); i++) {
                JSONObject carteira = carteiras.getJSONObject(i);
                if (carteira.getString("nome").equalsIgnoreCase(name)) {
                    resultCarteira = carteira;
                    break;
                }
            }
            StringBuilder result = new StringBuilder();
            if (resultCarteira != null) {
                this.CNH = resultCarteira.getString("cnh");
                this.multa = resultCarteira.getInt("valor_multa");
                
                result.append("\n//////////////////////////////////\n");
                result.append(String.format("CNH: %s \n", resultCarteira.getString("cnh")));
                result.append(String.format("Nome: %s \n", resultCarteira.getString("nome")));
                result.append(String.format("Número de veículos cadastrados: %s \n", resultCarteira.getString("qty_veiculos")));
                result.append(String.format("Valor total da multa: %s \n", resultCarteira.getDouble("valor_multa")));
            } else {
                result.append("Nenhum resultado encontrado.");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter dados em JSON.";
        }
    }

    
    public void pagarMultas(String fileType, String name, String placa) {
    	switch (fileType.toUpperCase()) {
    	case "SQL":
    		pagarMultasSQL(fileType);
    		break;
    	case "JSON":
    		pagarMultasJson(fileType, name, placa);
    		break;
    	case "XML":
    		pagarMultasXML(fileType, name, placa);
    	}
    }
    
    private void pagarMultasXML(String fileType, String nomeTitular, String placa) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            String xmlContent = xmlManager.readPersistencia();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlContent.getBytes());
            Document document = builder.parse(input);
            document.getDocumentElement().normalize();

            NodeList multasList = document.getElementsByTagName("multa");
            NodeList carteirasList = document.getElementsByTagName("carteira");
            NodeList veiculosList = document.getElementsByTagName("veiculo");

            String CNH = null;
            for (int i = 0; i < carteirasList.getLength(); i++) {
                Element carteira = (Element) carteirasList.item(i);
                if (carteira.getAttribute("nome").equalsIgnoreCase(nomeTitular)) {
                    CNH = carteira.getAttribute("cnh");
                    break;
                }
            }

            if (CNH == null) {
                System.out.println("Nenhuma CNH encontrada para o titular informado.");
                return;
            }

            for (int i = 0; i < multasList.getLength(); i++) {
                Element multa = (Element) multasList.item(i);
                if (multa.getAttribute("cpf").equals(CNH) && multa.getAttribute("placa_veiculo").equalsIgnoreCase(placa)) {
                    double valorMulta = Double.parseDouble(multa.getAttribute("valor"));
                    multa.getParentNode().removeChild(multa);

                    for (int j = 0; j < carteirasList.getLength(); j++) {
                        Element carteira = (Element) carteirasList.item(j);
                        if (carteira.getAttribute("cnh").equals(CNH)) {
                            double valorMultaAtual = Double.parseDouble(carteira.getAttribute("valor_multa"));
                            carteira.setAttribute("valor_multa", String.valueOf(valorMultaAtual - valorMulta));
                            break;
                        }
                    }

                    for (int j = 0; j < veiculosList.getLength(); j++) {
                        Element veiculo = (Element) veiculosList.item(j);
                        if (veiculo.getAttribute("placa").equalsIgnoreCase(placa)) {
                            veiculo.setAttribute("situacao_veiculo", "REGULAR");
                            double valorMultaVeiculo = Double.parseDouble(veiculo.getAttribute("valor_multa"));
                            veiculo.setAttribute("valor_multa", String.valueOf(valorMultaVeiculo - valorMulta));
                            break;
                        }
                    }
                    break;
                }
            }

            xmlManager.writePersistencia(document.toString());

            System.out.println("Multas pagas com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao pagar multas em XML.");
        }
    }

    private void pagarMultasJson(String fileType, String nomeTitular, String placa) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String JsonCont = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(JsonCont);
            JSONArray multas = json.getJSONArray("multas");
            JSONArray pessoas = json.getJSONArray("pessoas");
            JSONArray carteiras = json.getJSONArray("carteiras");
            String CNH = null;
            String CPF = null;

            for (int i = 0; i < carteiras.length(); i++) {
                JSONObject carteira = carteiras.getJSONObject(i);
                if (carteira.getString("nome").equalsIgnoreCase(nomeTitular)) {
                    CNH = carteira.getString("cnh");
                    break;
                }
            }
            if (CNH == null) {
                System.out.println("Nenhuma CNH encontrada para o titular informado.");
                return;
            }

            for (int i = 0; i < pessoas.length(); i++) {
                JSONObject pessoa = pessoas.getJSONObject(i);
                if (pessoa.getString("cnh").equals(CNH)) {
                    CPF = pessoa.getString("cpf");
                    break;
                }
            }

            double totalMulta = 0;
            for (int i = 0; i < multas.length(); i++) {
                JSONObject multa = multas.getJSONObject(i);
                if (multa.getString("cpf").equals(CPF) && multa.getString("placa_veiculo").equalsIgnoreCase(placa)) {
                    totalMulta += multa.getDouble("valor");
                    multas.remove(i); 
                    i--;
                }
            }

            for (int i = 0; i < carteiras.length(); i++) {
                JSONObject carteira = carteiras.getJSONObject(i);
                if (carteira.getString("cnh").equals(CNH)) {
                    double valorMultaAtual = carteira.getDouble("valor_multa");
                    carteira.put("valor_multa", valorMultaAtual - totalMulta);
                    break;
                }
            }

            JSONArray veiculos = json.getJSONArray("veiculos");
            for (int i = 0; i < veiculos.length(); i++) {
                JSONObject veiculo = veiculos.getJSONObject(i);
                if (veiculo.getString("placa").equalsIgnoreCase(placa)) {
                    veiculo.put("situacao_veiculo", "REGULAR");
                    double valorMultaVeiculo = veiculo.getDouble("valor_multa");
                    veiculo.put("valor_multa", valorMultaVeiculo - totalMulta);
                    break;
                }
            }

            System.out.println("Multas pagas com sucesso!");

            jsonContent.writePersistencia(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao pagar multas em JSON.");
        }
    }

    private void pagarMultasSQL(String fileType) {
        try {
            Connection con = Conexao_SQL.run_connection();

            String queryMulta = "SELECT valor_multa FROM situacao_carteira WHERE CNH = ?";
            PreparedStatement stmtMulta = con.prepareStatement(queryMulta);
            stmtMulta.setString(1, CNH);
            ResultSet rsMulta = stmtMulta.executeQuery();

            double valorTotalMulta = 0;
            if (rsMulta.next()) {
                valorTotalMulta = rsMulta.getDouble("valor_multa");
            }

            String infoVeiculos = veiculo.consultarVeiculosMultadosPorCNH(this.CNH, fileType);

            if (infoVeiculos.isEmpty()) {
                System.out.println("Nenhum veículo multado associado encontrado.");
                return;
            }

            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);

            while (valorTotalMulta > 0) {

                System.out.println("Escolha qual veículo pagar (informe a placa):");
                String placaEscolhida = scanner.nextLine().toUpperCase();

                double valorMultaVeiculo = veiculo.obterValorMultaPorVeiculo(placaEscolhida, fileType);

                String updateQueryCarteira = "UPDATE situacao_carteira SET valor_multa = ? WHERE CNH = ?";
                PreparedStatement updateStmtCarteira = con.prepareStatement(updateQueryCarteira);
                updateStmtCarteira.setDouble(1, valorTotalMulta - valorMultaVeiculo);
                updateStmtCarteira.setString(2, CNH);
                updateStmtCarteira.executeUpdate();

                String updateQueryVeiculo = "UPDATE veiculo SET situacao_veiculo = 'REGULAR', valor_multa = 0 WHERE PLACA = ?";
                PreparedStatement updateStmtVeiculo = con.prepareStatement(updateQueryVeiculo);
                updateStmtVeiculo.setString(1, placaEscolhida);
                updateStmtVeiculo.executeUpdate();
                
                String deleteQueryMultas = "DELETE FROM multas WHERE cpf = (SELECT cpf FROM pessoa_fisica WHERE CNH = ?) AND placa_veiculo = ?";
                PreparedStatement deleteStmtMultas = con.prepareStatement(deleteQueryMultas);
                deleteStmtMultas.setString(1, CNH);
                deleteStmtMultas.setString(2, placaEscolhida);
                deleteStmtMultas.executeUpdate();

                System.out.println("Multa paga com sucesso!");

                queryMulta = "SELECT valor_multa FROM situacao_carteira WHERE CNH = ?";
                stmtMulta = con.prepareStatement(queryMulta);
                stmtMulta.setString(1, CNH);
                rsMulta = stmtMulta.executeQuery();

                if (rsMulta.next()) {
                    valorTotalMulta = rsMulta.getDouble("valor_multa");
                } else {
                    valorTotalMulta = 0;
                }

                if (valorTotalMulta > 0) {
                    System.out.println("Deseja pagar outra multa? (S/N)");
                    String escolha = scanner.nextLine().toUpperCase();
                    if (!escolha.equals("S")) {
                        System.out.println("Retornando ao menu principal.");
                        break;
                    }
                } else {
                    System.out.println("Todas as multas foram pagas. Retornando ao menu principal.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao pagar as multas.");
        }
    }
    
    public double getValorMulta(String cnh, String fileType) {
        switch (fileType.toUpperCase()) {
            case "SQL":
                return getValorMultaSQL(cnh);
            case "JSON":
                return getValorMultaJSON(cnh);
            case "XML":
                return getValorMultaXML(cnh);
            default:
            	return 0;
        }
    }
    
    private double getValorMultaXML(String cnh) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            String xmlContent = xmlManager.readPersistencia();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlContent.getBytes());
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            NodeList carteiraList = doc.getElementsByTagName("carteira");
            for (int temp = 0; temp < carteiraList.getLength(); temp++) {
                Element carteiraElement = (Element) carteiraList.item(temp);
                String cnhCarteira = carteiraElement.getAttribute("cnh");
                if (cnh.equals(cnhCarteira)) {
                    return Double.parseDouble(carteiraElement.getAttribute("valor_multa"));
                }
            }
            return 0; 
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private double getValorMultaJSON(String cnh) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String JsonCont = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(JsonCont);
            JSONArray carteiras = json.getJSONArray("carteiras");
            for (int i = 0; i < carteiras.length(); i++) {
                JSONObject carteira = carteiras.getJSONObject(i);
                if (carteira.getString("cnh").equals(cnh)) {
                    return carteira.getDouble("valor_multa");
                }
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; 
        }
    }

    
    public double getValorMultaSQL(String cnh) {
        try {
            Connection con = Conexao_SQL.run_connection();
            String query = "SELECT valor_multa FROM situacao_carteira WHERE CNH = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, cnh);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("valor_multa");
            } else {
                return 0; 
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return -1; 
        }
    }
    
    public String getCNH() {
        return CNH;
    }

    public void setCNH(String CNH) {
        this.CNH = CNH;
    }
    
    public void setMulta(int multa) {
        this.multa = multa;
    }

    public int getMulta() {
        return multa;
    }

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
}

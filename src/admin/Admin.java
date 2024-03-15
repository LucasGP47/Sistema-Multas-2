package admin;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import conexao_SQL.Conexao_SQL;
import gerar_JSON.Gerar_Json;
import gerar_XML.Gerar_XML;
import interfaces.AdminService;
import pessoa.Carteira;
import pessoa.Multas;

public class Admin implements AdminService {

    private Carteira carteira; 
    private Multas multas; 
    public Admin(Carteira carteira, Multas multas) {
        this.carteira = carteira;
        this.multas = multas;
    }
    
    public Admin() {
    	
    }
    
    public void setPersistencia(String fileType) {
    }
    
    private String obterNomePorCPFJson(JSONObject json, String cpf) throws JSONException {
        JSONArray pessoas = json.getJSONArray("pessoas");
        for (int i = 0; i < pessoas.length(); i++) {
            JSONObject pessoa = pessoas.getJSONObject(i);
            if (pessoa.getString("cpf").equalsIgnoreCase(cpf)) {
                return pessoa.getString("nome");
            }
        }

        return null;
    }
    
    @Override
    public void consultarTabelasPorCPF(String cpf, String fileType) {
    	switch (fileType.toUpperCase()) {
    	case "SQL":
    		 consultarTabelasPorCPFSQL(cpf);
    	case "JSON":
    		consultarTabelasPorCPFJson(cpf);
    	case "XML":
    		consultarTabelasPorCPFXML(cpf);	
    	}
    }
    
    private void consultarTabelasPorCPFXML(String cpf) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            String xmlContent = xmlManager.readPersistencia();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlContent.getBytes());
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            
            NodeList veiculosList = doc.getElementsByTagName("veiculo");
            boolean found = false;
            
            for (int temp = 0; temp < veiculosList.getLength(); temp++) {
                Element veiculoElement = (Element) veiculosList.item(temp);
                if (veiculoElement.getAttribute("cpf_titular").equalsIgnoreCase(cpf)) {
                    if (!found) {
                        System.out.println("Veículos associados ao CPF " + cpf + ":");
                        found = true;
                    }
                    System.out.println("\n//////////////////////////////////\n");
                    System.out.println(String.format("Placa: %s \n", veiculoElement.getAttribute("placa")));
                    System.out.println(String.format("Nome do Titular: %s \n", veiculoElement.getAttribute("nome_titular")));
                    System.out.println(String.format("Situação do Veículo: %s \n", veiculoElement.getAttribute("situacao_veiculo")));
                    System.out.println(String.format("Valor da Multa: %s \n", veiculoElement.getAttribute("valor_multa")));
                }
            }
            
            if (!found) {
                System.out.println("Nenhum veículo encontrado para o CPF: " + cpf);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao obter dados do veículo em XML.");
        }
    }


    private void consultarTabelasPorCPFJson(String cpf) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            String nomeTitular = obterNomePorCPFJson(json, cpf);
            if (nomeTitular != null) {
                System.out.println("Veículos associados ao CPF " + cpf + " (Titular: " + nomeTitular + "):");             
                JSONArray veiculos = json.getJSONArray("veiculos");
                for (int i = 0; i < veiculos.length(); i++) {
                    JSONObject veiculo = veiculos.getJSONObject(i);
                    if (veiculo.getString("nome_titular").equalsIgnoreCase(nomeTitular)) {
                        System.out.println("\n//////////////////////////////////\n");
                        System.out.println(String.format("Placa: %s \n", veiculo.getString("placa")));
                        System.out.println(String.format("Nome do Titular: %s \n", veiculo.getString("nome_titular")));
                        System.out.println(String.format("Situação do Veículo: %s \n", veiculo.getString("situacao_veiculo")));
                        System.out.println(String.format("Valor da Multa: %s \n", veiculo.getInt("valor_multa")));
                    }
                }
            } else {
                System.out.println("Nome do titular não encontrado para o CPF: " + cpf);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao obter dados do veículo em JSON.");
        }
    }
    
    private void consultarTabelasPorCPFSQL(String cpf) {
        try {
            Connection con = Conexao_SQL.run_connection();     
            String queryVeiculos = "SELECT * FROM veiculo WHERE CNH = (SELECT CNH FROM pessoa_fisica WHERE CPF = ?)";
            PreparedStatement stmtVeiculos = con.prepareStatement(queryVeiculos);
            stmtVeiculos.setString(1, cpf);
            ResultSet rsVeiculos = stmtVeiculos.executeQuery();
            System.out.println("Veículos associados ao CPF " + cpf + ":");
            while (rsVeiculos.next()) {
                System.out.println("\n//////////////////////////////////\n");
                System.out.println(String.format("Placa: %s \n", rsVeiculos.getString("PLACA")));
                System.out.println(String.format("Nome do Titular: %s \n", rsVeiculos.getString("nome_titular")));
                System.out.println(String.format("Situação do Veículo: %s \n", rsVeiculos.getString("situacao_veiculo")));
                System.out.println(String.format("Valor da Multa: %s \n", rsVeiculos.getString("valor_multa")));
            }
            String queryCarteira = "SELECT * FROM situacao_carteira WHERE CNH = (SELECT CNH FROM pessoa_fisica WHERE CPF = ?)";
            PreparedStatement stmtCarteira = con.prepareStatement(queryCarteira);
            stmtCarteira.setString(1, cpf);
            ResultSet rsCarteira = stmtCarteira.executeQuery();
            System.out.println("\nSituação da Carteira associada ao CPF " + cpf + ":");
            while (rsCarteira.next()) {
                System.out.println("\n//////////////////////////////////\n");
                System.out.println(String.format("CNH: %s \n", rsCarteira.getString("CNH")));
                System.out.println(String.format("Nome: %s \n", rsCarteira.getString("nome")));
                System.out.println(String.format("Número de veículos cadastrados: %s \n", rsCarteira.getString("qty_veiculos")));
                System.out.println(String.format("Valor total da multa: %s \n", rsCarteira.getString("valor_multa")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao consultar tabelas por CPF.");
        }
    }
    
    @Override
    public void inserirMulta(String fileType) {
    	switch (fileType.toUpperCase()) {
    	case "SQL": 
    		inserirMultaSQL();
    	case "JSON":
    		inserirMultaJson();
    	case "XML":
    		inserirMultaXML();	
    	}
    }
    
    private void inserirMultaXML() {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            Document doc = xmlManager.readPersistencia();
            doc.getDocumentElement().normalize();
            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite o CPF do infrator:");
            String cpf = scanner.nextLine();
            System.out.println("Digite a placa do veículo:");
            String placa = scanner.nextLine();
            System.out.println("Digite o valor da multa:");
            double valorMulta = scanner.nextDouble();
            scanner.nextLine();
            System.out.println("Digite o motivo da multa:");
            String motivoMulta = scanner.nextLine();
            
            Element multaElement = doc.createElement("multa");
            multaElement.setAttribute("cpf", cpf);
            multaElement.setAttribute("placa_veiculo", placa);
            multaElement.setAttribute("valor", Double.toString(valorMulta));
            multaElement.setAttribute("descricao", motivoMulta);
            
            LocalDate dataAtual = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dataFormatada = formatter.format(dataAtual);
            multaElement.setAttribute("data_aplicacao", dataFormatada);
            
            // Obtendo o nó multas para adicionar a nova multa
            NodeList multasList = doc.getElementsByTagName("multas");
            Node multasNode = null;
            if (multasList.getLength() > 0) {
                multasNode = multasList.item(0);
            } else {
                // Se não houver nenhum nó multas, criamos um novo elemento multas
                multasNode = doc.createElement("multas");
                doc.getDocumentElement().appendChild(multasNode);
            }
            
            // Adicionando a nova multa ao nó multas
            multasNode.appendChild(multaElement);
            
            xmlManager.writePersistencia(doc);
            
            System.out.println("Multa aplicada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao aplicar a multa.");
        }
    }

    private void inserirMultaJson() {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
            System.out.println("Digite o CPF do infrator:");
            String cpf = scanner.nextLine();
            System.out.println("Digite a placa do veículo:");
            String placa = scanner.nextLine();
            System.out.println("Digite o valor da multa:");
            double valorMulta = scanner.nextDouble();
            scanner.nextLine();
            System.out.println("Digite o motivo da multa:");
            String motivoMulta = scanner.nextLine();
            JSONArray pessoas = json.getJSONArray("pessoas");
            String nomeTitular = "";
            String cnh = "";
            boolean pessoaEncontrada = false;
            for (int i = 0; i < pessoas.length(); i++) {
                JSONObject pessoa = pessoas.getJSONObject(i);
                if (pessoa.getString("cpf").equalsIgnoreCase(cpf)) {
                    pessoaEncontrada = true;
                    nomeTitular = pessoa.getString("nome");
                    cnh = pessoa.getString("cnh");
                    break;
                }
            }
            if (!pessoaEncontrada) {
                System.out.println("Erro: Pessoa não encontrada.");
                return;
            }
            JSONArray carteiras = json.getJSONArray("carteiras");
            for (int i = 0; i < carteiras.length(); i++) {
                JSONObject carteira = carteiras.getJSONObject(i);
                if (carteira.getString("cnh").equalsIgnoreCase(cnh)) {
                    double valorTotalMulta = carteira.getDouble("valor_multa") + valorMulta;
                    carteira.put("valor_multa", valorTotalMulta);
                    break;
                }
            }        
            LocalDate dataAtual = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dataFormatada = formatter.format(dataAtual);
            JSONObject novaMulta = new JSONObject();
            novaMulta.put("cpf", cpf);
            novaMulta.put("data_aplicacao", dataFormatada);
            novaMulta.put("placa_veiculo", placa);
            novaMulta.put("valor", valorMulta);
            novaMulta.put("descricao", motivoMulta);
            json.getJSONArray("multas").put(novaMulta);
            JSONArray veiculos = json.getJSONArray("veiculos");
            for (int i = 0; i < veiculos.length(); i++) {
                JSONObject veiculo = veiculos.getJSONObject(i);
                if (veiculo.getString("placa").equalsIgnoreCase(placa) && veiculo.getString("nome_titular").equalsIgnoreCase(nomeTitular)) {
                    veiculo.put("situacao_veiculo", "MULTADO");
                    double valorMultaAtualVeiculo = veiculo.optDouble("valor_multa", 0.0);
                    veiculo.put("valor_multa", valorMultaAtualVeiculo + valorMulta);
                    break;
                }
            }
            jsonContent.writePersistencia(json.toString());
            System.out.println("Multa aplicada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao aplicar a multa.");
        }
    }

    private void inserirMultaSQL() {
        try {
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
            System.out.println("Digite o CPF do infrator:");
            String cpf = scanner.nextLine();
            System.out.println("Digite a placa do veículo:");
            String placa = scanner.nextLine();
            System.out.println("Digite o valor da multa:");
            int valorMulta = scanner.nextInt();
            scanner.nextLine(); 
            System.out.println("Digite o motivo da multa:");
            String motivoMulta = scanner.nextLine();
            Connection con = Conexao_SQL.run_connection();        
            String updateQueryCarteira = "UPDATE situacao_carteira SET valor_multa = valor_multa + ? WHERE CNH = (SELECT CNH FROM pessoa_fisica WHERE CPF = ?)";
            PreparedStatement updateStmtCarteira = con.prepareStatement(updateQueryCarteira);
            updateStmtCarteira.setInt(1, valorMulta);
            updateStmtCarteira.setString(2, cpf);
            updateStmtCarteira.executeUpdate();
            String updateQueryVeiculo = "UPDATE veiculo SET situacao_veiculo = 'MULTADO', valor_multa = valor_multa + ? WHERE PLACA = ?";
            PreparedStatement updateStmtVeiculo = con.prepareStatement(updateQueryVeiculo);
            updateStmtVeiculo.setDouble(1, valorMulta);
            updateStmtVeiculo.setString(2, placa);
            updateStmtVeiculo.executeUpdate();
            String insertQueryMulta = "INSERT INTO multas (cpf, placa_veiculo, valor, descricao) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmtMulta = con.prepareStatement(insertQueryMulta);
            insertStmtMulta.setString(1, cpf);
            insertStmtMulta.setString(2, placa);
            insertStmtMulta.setDouble(3, valorMulta);
            insertStmtMulta.setString(4, motivoMulta);
            insertStmtMulta.executeUpdate();
            System.out.println("Multa aplicada com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao aplicar a multa.");
        }
    }
    
    @Override
    public void removerMulta(String cpf, String fileType) {
    	switch (fileType.toUpperCase()) {
    	case "SQL": 
    		removerMultaSQL(cpf, fileType);
    	case "JSON":
    		removerMultaJson(cpf, fileType);
    	case "XML":
    		removerMultaXML(cpf, fileType);	
    	}
    }
    
    private void removerMultaXML(String cpf, String placa) {
        try {
            Gerar_XML xmlManager = new Gerar_XML();
            Document doc = xmlManager.readPersistencia();
            doc.getDocumentElement().normalize();
            NodeList multasList = doc.getElementsByTagName("multa");
            boolean multaEncontrada = false;
            for (int i = 0; i < multasList.getLength(); i++) {
                Node node = multasList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element multaElement = (Element) node;
                    String multaCpf = multaElement.getAttribute("cpf");
                    String multaPlaca = multaElement.getAttribute("placa_veiculo");
                    if (multaCpf.equalsIgnoreCase(cpf) && multaPlaca.equalsIgnoreCase(placa)) {
                        node.getParentNode().removeChild(node);
                        multaEncontrada = true;
                        break;
                    }
                }
            }
            if (multaEncontrada) {
                xmlManager.writePersistencia(doc);
                System.out.println("Multa removida com sucesso!");
            } else {
                System.out.println("Multa não encontrada para remoção.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao remover a multa do arquivo XML.");
        }
    }

    public void removerMultaJson(String cpf, String fileType) {
        try {
            Gerar_Json jsonContent = new Gerar_Json();
            String jsonContentStr = jsonContent.readPersistencia();
            JSONObject json = new JSONObject(jsonContentStr);
            String CNH = "";
            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(System.in);
            System.out.println("Informe a placa do veículo:");
            String placa = scanner.nextLine().toUpperCase();          
            JSONArray pessoas = json.getJSONArray("pessoas");
            for (int i = 0; i < pessoas.length(); i++) {
                JSONObject pessoa = pessoas.getJSONObject(i);
                if (pessoa.getString("cpf").equalsIgnoreCase(cpf)) {
                	CNH = pessoa.getString("cnh");
                    break;
                }
            }
            double valorMultaRemovida = multas.obterValorMulta(cpf, placa, fileType);
            double valorAtualMulta = carteira.getValorMulta(CNH, fileType);           
            System.out.println("valormultaremovida: " + valorMultaRemovida);
            System.out.println("valorAtualMulta: " + valorAtualMulta);        
            if (valorMultaRemovida <= valorAtualMulta) {
                for (int i = 0; i < pessoas.length(); i++) {
                	JSONArray carteiras = json.getJSONArray("carteiras");
                    JSONObject carteira = carteiras.getJSONObject(i);
                    if (carteira.getString("cnh").equalsIgnoreCase(CNH)) {
                        double valorMultaAtual = carteira.getDouble("valor_multa");
                        carteira.put("valor_multa", valorMultaAtual - valorMultaRemovida);
                        break;
                    }
                }
                JSONArray veiculos = json.getJSONArray("veiculos");
                for (int i = 0; i < veiculos.length(); i++) {
                    JSONObject veiculo = veiculos.getJSONObject(i);
                    if (veiculo.getString("placa").equalsIgnoreCase(placa)) {
                        veiculo.put("situacao_veiculo", "REGULAR");
                        veiculo.put("valor_multa", 0.0);
                        break;
                    }
                }
                JSONArray multas = json.getJSONArray("multas");
                for (int i = 0; i < multas.length(); i++) {
                    JSONObject multa = multas.getJSONObject(i);
                    if (multa.getString("cpf").equalsIgnoreCase(cpf) && multa.getString("placa_veiculo").equalsIgnoreCase(placa)) {
                        multas.remove(i);
                        break;
                    }
                }
                jsonContent.writePersistencia(json.toString());

                System.out.println("Multa removida com sucesso!");
            } else {
                System.out.println("Erro: O valor da multa a ser removida é maior do que o valor atual na carteira.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao remover a multa.");
        }
    }
    
    private void removerMultaSQL(String cpf, String fileType) {
        try {
            Connection con = Conexao_SQL.run_connection();
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);          
            System.out.println("Digite a CNH do infrator:");            
            String cnh = scanner.nextLine();
            System.out.println("Informe a placa do veículo: ");
            String placa = scanner.nextLine().toUpperCase();
            double valorMultaRemovida = multas.obterValorMulta(cpf, placa, fileType);
            double valorAtualMulta = carteira.getValorMulta(cnh, fileType);
            if (valorMultaRemovida <= valorAtualMulta) {
                String queryCarteira = "UPDATE situacao_carteira SET valor_multa = valor_multa - ? WHERE CNH = (SELECT CNH FROM pessoa_fisica WHERE CPF = ?)";
                PreparedStatement stmtCarteira = con.prepareStatement(queryCarteira);
                stmtCarteira.setDouble(1, valorMultaRemovida);
                stmtCarteira.setString(2, cpf);
                stmtCarteira.executeUpdate();
                String queryVeiculo = "UPDATE veiculo SET valor_multa = 0, situacao_veiculo = 'REGULAR' WHERE PLACA = ? AND CNH = (SELECT CNH FROM pessoa_fisica WHERE CPF = ?)";
                PreparedStatement stmtVeiculo = con.prepareStatement(queryVeiculo);
                stmtVeiculo.setString(1, placa);
                stmtVeiculo.setString(2, cpf);
                stmtVeiculo.executeUpdate();
                String queryRemoverMulta = "DELETE FROM multas WHERE cpf = ? AND placa_veiculo = ?";
                PreparedStatement stmtRemoverMulta = con.prepareStatement(queryRemoverMulta);
                stmtRemoverMulta.setString(1, cpf);
                stmtRemoverMulta.setString(2, placa);
                stmtRemoverMulta.executeUpdate();
                System.out.println("Multa removida com sucesso!");
            } else {
                System.out.println("Erro: O valor da multa a ser removida é maior do que o valor atual na carteira.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao remover a multa.");
        }
    }


}

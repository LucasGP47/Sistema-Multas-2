package main;

import java.util.Scanner;

import pessoa.Carteira;
import pessoa.Multas;
import pessoa.Pessoa;
import pessoa.Veiculo;
import admin.Admin;
import interfaces.AdminService;
import interfaces.CarteiraService;
import interfaces.GerarPersistencia;
import interfaces.MultasService;
import interfaces.PessoaService;
import interfaces.VeiculoService;
import gerar_JSON.Gerar_Json;
import gerar_XML.Gerar_XML;

public class Main {

    private final PessoaService pessoa;
    private final CarteiraService carteira;
    private final VeiculoService veiculo;
    private final MultasService multas;
    private final AdminService admin;
    private final GerarPersistencia gerarPersistencia;
	public Main(PessoaService pessoa, CarteiraService carteira, VeiculoService veiculo, MultasService multas, AdminService admin, GerarPersistencia gerarPersistencia) {
        this.pessoa = pessoa;
        this.carteira = carteira;
        this.veiculo = veiculo;
        this.multas = multas;
        this.admin = admin;
        this.gerarPersistencia = gerarPersistencia; 
    }

    public void start() {
        Scanner entrada = new Scanner(System.in);
        
        String fileType = gerarPersistencia.getFileType();

        System.out.println("Olá, seja bem-vindo ao SAGM, o Sistema Amador de Gerenciamento de Multas! Por favor, informe seu nome: ");

        String name = entrada.next();

        if (!pessoa.pesquisarNome(name, fileType) && !name.equals("Admin")) {
            System.out.println("Usuário não encontrado!");

        } else if (name.equals("Admin")) {
        	Admin admin2 = new Admin();
			admin2.setPersistencia(fileType);
            System.out.println("Seja Bem Vindo Administrador.");

            boolean sairAdmin = false;

            while (!sairAdmin) {
                System.out.println("Operações disponíveis para o Administrador:");
                System.out.println("1) Consultar tabelas associadas a um CPF. 2) Aplicar uma Multa. 3) Remover Multa. 4) Sair do programa");

                int optAdmin = 0;

                if (entrada.hasNextInt()) {
                    optAdmin = entrada.nextInt();
                } else {
                    System.out.println("Entrada inválida. Por favor, insira um número inteiro.");
                    entrada.next();
                    continue;
                }

                switch (optAdmin) {
                    case 1:
                        System.out.println("Informe o CPF para consultar as tabelas associadas: ");
                        String cpfAdmin = entrada.next();
                        admin.consultarTabelasPorCPF(cpfAdmin, fileType);
                        break;
                    case 4:
                        System.out.println("Saindo do modo administrador.");
                        sairAdmin = true;
                        break;
                    case 2:
                        admin.inserirMulta(fileType);
                        break;
                    case 3:
                        System.out.println("Informe o CPF: ");
                        String cpfRemoverMulta = entrada.next();
                        admin.removerMulta(cpfRemoverMulta, fileType);
                        break;
                    default:
                        System.out.println("Opção inválida. Por favor, escolha novamente.");
                }
            }
        } else {
            boolean sair = false;

            System.out.println("Seja Bem Vindo " + name);

            while (!sair) {
                System.out.println("Por favor, selecione a operação. 1) Consultar meus dados. 2) Situação da Carteira. 3) Veículos cadastrados. 4) Consultar Detalhes da Multa. 5) Sair");

                int opt = 0;

                if (entrada.hasNextInt()) {
                    opt = entrada.nextInt();
                } else {
                    System.out.println("Entrada inválida. Por favor, insira um número inteiro.");
                    entrada.next();
                    continue;
                }

                switch (opt) {
                    case 1:
                        System.out.println(pessoa.status(name, fileType));
                        break;
                    case 2:
                        System.out.println(carteira.statusCarteira(name, fileType));

                        if (carteira.getMulta() > 0) {
                            System.out.println("Multa encontrada! Deseja pagar a multa? (Digite 'sim' ou 'nao')");
                            String resposta = entrada.next();

                            if (resposta.equalsIgnoreCase("sim")) {
                            	System.out.println("Informe a placa do veiculo:");
                            	String placa = entrada.next().toUpperCase();
                                carteira.pagarMultas(fileType, name, placa);
                                System.out.println("Multa zerada com sucesso! Dados atualizados: ");
                                System.out.println(carteira.statusCarteira(name, fileType));
                            } else {
                                System.out.println("Retornando à tela inicial...");
                            }
                        }
                        break;
                    case 3:
                    	System.out.println(carteira.getCNH());
                        if (carteira.getCNH() != null) {
                            System.out.println(veiculo.consultarVeiculos(carteira.getCNH(), fileType));
                        } else {
                            System.out.println("A CNH não está disponível. Consulte a situação da carteira primeiro.");
                        }
                        break;
                    case 4:
                        System.out.println("Digite a placa do veículo para consultar as multas: ");
                        String placa = entrada.next().toUpperCase();
                        System.out.println(multas.consultarMulta(pessoa.getCPF(), placa, fileType));
                        break;
                    case 5:
                        System.out.println("Finalizando o programa. Obrigado pela atenção!");
                        sair = true;
                        break;
                    default:
                        System.out.println("Opção inválida. Por favor, escolha novamente.");
                }
            }
        }
       
        entrada.close();
    }

    public static void main(String[] args) {
        Pessoa pessoa = new Pessoa();
        Veiculo veiculo = new Veiculo(pessoa);
        Carteira carteira = new Carteira(pessoa, veiculo);
        Multas multas = new Multas(carteira);
        Admin admin = new Admin(carteira, multas);
        GerarPersistencia gerarPersistencia = null;
        String fileType = "xml"; 
        if (fileType.equalsIgnoreCase("json")) {
            gerarPersistencia = new Gerar_Json();
        } else if (fileType.equalsIgnoreCase("xml")) {
            gerarPersistencia = new Gerar_XML();
        } else {
            System.out.println("Usando SQL");
            return;
        }
        gerarPersistencia.setFileType(fileType);
        
        new Main(pessoa, carteira, veiculo, multas, admin, gerarPersistencia).start();
    }
}

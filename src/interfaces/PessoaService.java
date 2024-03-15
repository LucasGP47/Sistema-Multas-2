package interfaces;

public interface PessoaService {
 boolean pesquisarNome(String name, String fileType);
 String status(String name, String fileType);
 String getCPF();
 void setCPF(String cpf);
 String getCNH();
 void setCNH(String cNH);
}

package interfaces;

public interface AdminService {
	  void consultarTabelasPorCPF(String cpf, String fileType);
	  void inserirMulta(String fileType);
	  void removerMulta(String cpf, String fileType);

}

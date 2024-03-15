package interfaces;

public interface MultasService {
    String consultarMulta(String cpf, String placa, String fileType);
    double obterValorMulta(String cpf, String placa, String fileType);
}

package interfaces;

public interface VeiculoService {
    String consultarVeiculosMultadosPorCNH(String CNH, String fileType);
    double obterValorMultaPorVeiculo(String placa, String fileType);
    void pagarMulta(String placa, double valorPago, String fileType);
    String consultarVeiculos(String CNH, String fileType);
}

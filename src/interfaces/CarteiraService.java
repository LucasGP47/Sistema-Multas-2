package interfaces;

public interface CarteiraService {
    String statusCarteira(String name, String fileType);
    void pagarMultas(String fileType, String name, String placa);
    double getValorMulta(String cnh, String fileType);
    String getCNH();
    void setCNH(String CNH);
    void setMulta(int multa);
    int getMulta();
}

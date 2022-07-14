package gr.grnet.pccapi.dto;

/**
 * A class that represents a Provider served as response by the Provider endpoint
 */
public class ProviderResponseDTO {

    public int id;
    public String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

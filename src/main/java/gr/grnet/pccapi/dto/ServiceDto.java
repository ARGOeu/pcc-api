package gr.grnet.pccapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * DTO class for the service domain resource
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ServiceDto {
    public Long id;
    public String name;
}

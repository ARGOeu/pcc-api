package gr.grnet.pccapi.client.hrls;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HRLSHandle is the DTO that represents the data that the lookup service will return when the
 * {@link HRLSClient} performs the {@link HRLSClient#getHandles}
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HRLSHandle {
  public String type;
  public String value;
}

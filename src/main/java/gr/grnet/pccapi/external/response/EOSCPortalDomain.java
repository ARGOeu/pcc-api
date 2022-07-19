package gr.grnet.pccapi.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * The {@link EOSCPortalDomain EOSC-Portal Domain} is an entity described and offered by the EOSC-Portal. Typically an EOSC-Portal Scientific Domain has the following structure :
 * {
 *    "id":"scientific_domain-agricultural_sciences",
 *    "name":"Agricultural Sciences",
 *    "description":"Sciences dealing with food and fibre production and processing. They include the technologies of soil cultivation, crop cultivation and harvesting, animal production, and the processing of plant and animal products for human consumption and use.",
 *    "parentId":null,
 *    "type":"Scientific domain",
 *    "extras":{
 *       "icon":"ic_analytics.svg",
 *       "icon_active":"ic_analytics_active.svg"
 *    }
 * }
 *
 * In our API we keep from EOSC-Portal Scientific Domain the following attributes:
 * - id -> domainId
 * - name
 * - description
*/
public class EOSCPortalDomain {

    @EqualsAndHashCode.Include
    @JsonProperty("id")
    public String domainId;
    @EqualsAndHashCode.Include
    public String name;
    @EqualsAndHashCode.Include
    public String description;
}

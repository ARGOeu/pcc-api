package gr.grnet.pccapi.service;

import gr.grnet.pccapi.client.hrls.HRLSClient;
import gr.grnet.pccapi.client.hrls.HRLSHandle;
import gr.grnet.pccapi.dto.FiltersDto;
import gr.grnet.pccapi.dto.HandleDto;
import gr.grnet.pccapi.enums.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReverseLookUpService {

  @Inject Logger logger;

  @Inject @RestClient HRLSClient hrlsClient;

  /**
   * search uses the {@link HRLSClient} to perform a lookup based on the provided filters. It will
   * always provide a value for the query parameter retrieverecords with a default value being
   * false. Yt will always transform the response to a {@link HandleDto} list hiding the different
   * approach of the HRLS service derived from retrieverecords query parameter.
   *
   * @param filtersDto The filters that will be provided to the HRLS service
   * @param page the page number, defaults to 0
   * @param limit the limit size, defaults to 10
   * @return list of {@link HandleDto}
   */
  public List<HandleDto> search(FiltersDto filtersDto, Long page, Long limit) {

    boolean fullrecords = false;
    List<HandleDto> responseDto = new ArrayList<>();
    Map<String, String> params =
        new HashMap<String, String>(
            Map.of(
                "page", page != null ? page.toString() : "0",
                "limit", limit != null ? limit.toString() : "10"));

    for (Map.Entry<String, String> filterDto : filtersDto.getFilters().entrySet()) {
      Filter filter;
      // check that the filter is supported
      try {
        filter = Filter.valueOf(filterDto.getKey().toUpperCase());
      } catch (IllegalArgumentException illegalArgumentException) {
        throw new BadRequestException(
            String.format("Filter %s is unsupported", filterDto.getKey()));
      }

      if (filter.equals(Filter.RETRIEVE_RECORDS)) {
        if (filterDto.getValue().equalsIgnoreCase("true")) {
          fullrecords = true;
        }
      } else {
        params.put(filter.toString(), filterDto.getValue());
      }

      logger.infof("Filter %s with value %s", filter.toString(), filterDto.getValue());
    }

    params.put(Filter.RETRIEVE_RECORDS.toString(), String.valueOf(fullrecords));
    try {
      if (fullrecords) {
        Map<String, List<HRLSHandle>> result = hrlsClient.getHandles(params);
        for (Map.Entry<String, List<HRLSHandle>> handle : result.entrySet()) {
          HandleDto handleDto = new HandleDto();
          handleDto.setHandle(handle.getKey());
          handle.getValue().stream()
              .filter(v -> !v.getType().equals("HS_ADMIN"))
              .forEach(v -> handleDto.addType(v.getType(), v.getValue()));
          responseDto.add(handleDto);
        }
      } else {
        hrlsClient
            .getHandlesFlat(params)
            .forEach(v -> responseDto.add(new HandleDto().setHandle(v)));
      }
    } catch (Exception ex) {
      logger.error("Failed to communicate with HRLS.", ex);
      throw new InternalServerErrorException();
    }

    return responseDto;
  }
}

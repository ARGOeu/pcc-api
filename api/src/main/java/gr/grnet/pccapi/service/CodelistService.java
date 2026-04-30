package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.CodelistDto;
import gr.grnet.pccapi.entity.Codelist;
import gr.grnet.pccapi.enums.CodelistCategory;
import gr.grnet.pccapi.mapper.CodelistMapper;
import gr.grnet.pccapi.repository.CodelistRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class CodelistService {

  @Inject CodelistRepository codelistRepository;

  /**
   * Returns the available codes
   *
   * @return The stored codes has been turned into a response body.
   */
  public List<CodelistDto> fetchAll() {

    var codes = codelistRepository.findAll().list();

    return CodelistMapper.INSTANCE.codelistsToResponseDto(codes);
  }

  public List<CodelistDto> fetchByCategory(String category) {

    List<Codelist> codes = new ArrayList<>();
    if (category == null) {
      codes = codelistRepository.findAll().list();
    } else {
      CodelistCategory codelistCategory =
          CodelistMapper.INSTANCE.validateCodelistCategory(category.toLowerCase());
      if (codelistCategory == null) {
        return null;
      }
      codes = codelistRepository.find("category", category).list();
    }
    return CodelistMapper.INSTANCE.codelistsToResponseDto(codes);
  }
}

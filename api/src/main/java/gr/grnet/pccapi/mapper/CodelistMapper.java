package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.CodelistDto;
import gr.grnet.pccapi.entity.Codelist;
import gr.grnet.pccapi.enums.CodelistCategory;
import java.util.List;
import javax.ws.rs.BadRequestException;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CodelistMapper {
  CodelistMapper INSTANCE = Mappers.getMapper(CodelistMapper.class);

  CodelistDto codelistToResponseDto(Codelist codelist);

  List<CodelistDto> codelistsToResponseDto(List<Codelist> codelists);

  @Named("validateCodelistCategory")
  default CodelistCategory validateCodelistCategory(String category) {
    CodelistCategory codelistCategoryT = null;
    try {
      codelistCategoryT = CodelistCategory.getEnumByText(category);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid codelist category value");
    }
    return codelistCategoryT;
  }
}

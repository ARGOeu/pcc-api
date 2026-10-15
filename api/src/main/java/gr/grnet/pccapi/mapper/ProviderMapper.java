package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.ContactDto;
import gr.grnet.pccapi.dto.ContactFullDto;
import gr.grnet.pccapi.dto.provider.ProviderRequestDto;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.entity.Contact;
import gr.grnet.pccapi.entity.Provider;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gr.grnet.pccapi.entity.ProviderContactJunction;
import gr.grnet.pccapi.enums.ContactType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProviderMapper {

  ProviderMapper INSTANCE = Mappers.getMapper(ProviderMapper.class);

  /**
   * Converts a Provider instance to ProviderResponseDTO
   *
   * @param provider A Provider instance
   * @return The converted ProviderResponseDTO provider
   */
  @Mapping(target = "contacts", source = "contacts", qualifiedByName = "contactsToDtos")
  ProviderResponseDTO providerToResponse(Provider provider);

  /**
   * Converts a list of Provider instances to a list of ProviderResponseDTO instances
   *
   * @param providers A list of Provider instances
   * @return The converted ProviderResponseDTO providers list
   */
  List<ProviderResponseDTO> providersToResponse(List<Provider> providers);


  @Named("contactToDto")
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "contactName")
  @Mapping(target = "email", source = "contactEmail")
  @Mapping(target = "type", source = "contactType", qualifiedByName = "mapTypeToString")
  ContactDto contactToDto(Contact contact);

  @Named("contactsToDtos")
  default List<ContactDto> contactsToDtos(Set<Contact> contacts) {
    if (contacts == null) {
      return List.of();
    }
    return contacts.stream()
            .map(this::contactToDto)
            .collect(Collectors.toList());
  }

  @Named("contactsFullToDtos")
  default List<ContactFullDto> contactsFullToDtos(Set<ProviderContactJunction> providerContactJunctions) {
    if (providerContactJunctions == null) {
      return List.of();
    }
    return providerContactJunctions.stream()
            .map(this::contactFullToDto)
            .collect(Collectors.toList());
  }

  @Named("contactFullToDto")
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "email", source = "email")
  @Mapping(target = "type", source = "type")
  @Mapping(target = "providers", source = "providers")
  ContactFullDto contactFullToDto(ProviderContactJunction providerContactJunction);

  @Named("mapTypeToString")
  default String mapTypeToString(ContactType contactType) {
    return contactType != null ? contactType.name() : null;
  }

  Provider providerRequestToEntity(ProviderRequestDto request);


  @Mapping(target = "id", ignore = true)
  @Mapping(target = "contactName", source = "name")
  @Mapping(target = "contactEmail", source = "email")
  @Mapping(target = "contactType", source = "type", qualifiedByName = "mapStringToType")
  Contact contactDtoToEntity(ContactDto dto);

  @Named("mapStringToType")
  default ContactType mapStringToType(String type) {
    return type != null ? ContactType.valueOf(type) : null;
  }}

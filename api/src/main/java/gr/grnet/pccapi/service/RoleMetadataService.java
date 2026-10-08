package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.metadata.MetadataAttributeDto;
import gr.grnet.pccapi.dto.metadata.RoleAssignmentMetadataResponseDto;
import gr.grnet.pccapi.dto.metadata.RoleMetadataResponseDto;
import gr.grnet.pccapi.enums.resources.PrefixResource;
import gr.grnet.pccapi.enums.resources.ProviderResource;
import jakarta.enterprise.context.ApplicationScoped;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RoleMetadataService {

    public RoleMetadataResponseDto getRoleMetadata() {

        var response = new RoleMetadataResponseDto();

        response.attributes = List.of(
                attribute("preferred_name", "Preferred Name", true),
                attribute("description", "Description", false)
        );

        return response;
    }

    public RoleAssignmentMetadataResponseDto getRoleAssignmentMetadata() {

        var response = new RoleAssignmentMetadataResponseDto();

        response.resources = Map.of(
                ProviderResource.PROVIDER.resourceName(), List.of(
                        attribute("preferred_role_name", "Preferred Role Name", true),
                        attribute("provider_name", "Provider Name", true),
                        attribute("role_description", "Role Description", false)
                ),
                PrefixResource.PREFIX.resourceName(), List.of(
                        attribute("preferred_role_name", "Preferred Role Name", true),
                        attribute("prefix_name", "Prefix Name", true),
                        attribute("role_description", "Role Description", false)
                )
        );

        return response;
    }


    private MetadataAttributeDto attribute(String key, String label, boolean required) {

        var dto = new MetadataAttributeDto();
        dto.key = key;
        dto.label = label;
        dto.required = required;

        return dto;
    }
}
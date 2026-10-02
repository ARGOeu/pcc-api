package gr.grnet.pccapi;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.repositories.RoleEndpointRepository;

import java.util.ArrayList;
import java.util.List;
@Alternative
@Priority(1)
@ApplicationScoped
public class TestRoleEndpointRepository implements RoleEndpointRepository {

    private List<RoleEndpoint> store = new ArrayList<>();

    public void set(List<RoleEndpoint> values) {
        this.store = values;
    }

    public void reset() {
        this.store = new ArrayList<>();
    }

    @Override
    public List<RoleEndpoint> list(String column, String id) {
        return store;
    }

    @Override
    public void create(RoleEndpoint entity) {

    }

    @Override
    public RoleEndpoint findById(Long id) {
        return null;
    }

    @Override
    public void update(RoleEndpoint entity) {

    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public void deleteByRoleIdAndEndpointId(String roleId, String securedEndpointId) {

    }

    @Override
    public void deleteByRoleIdAndEndpointIds(String roleId, List<String> securedEndpointId) {

    }

    @Override
    public void deleteByRoleId(String roleId) {

    }

    @Override
    public List<RoleEndpoint> findAll() {
        return List.of();
    }

}
package gr.grnet.pccapi.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.Optional;

/**
 * Base repository interface with common CRUD operations for all entities.
 *
 * @param <E>  Entity type
 * @param <ID> ID type
 */
public interface Repository<E, ID> extends PanacheRepositoryBase<E, ID> {

    /**
     * Retrieves an entity by its identifier as an Optional.
     *
     * @param id entity identifier
     * @return optional entity
     */
    default Optional<E> searchByIdOptional(ID id){
        return findByIdOptional(id);
    }

    /**
     * Checks whether an entity exists by a specific field value.
     *
     * @param fieldName entity field name
     * @param value field value
     * @return true if an entity exists with the given field value
     */
    default boolean existsByField(String fieldName, Object value) {
        return find(fieldName + " = ?1", value)
                .firstResultOptional()
                .isPresent();
    }
}

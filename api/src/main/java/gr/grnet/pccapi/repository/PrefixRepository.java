package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Page;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.PageQueryImpl;
import gr.grnet.pccapi.entity.Prefix;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrefixRepository implements PanacheRepositoryBase<Prefix, Integer> {

  /**
   * Checks if the given prefix name has already been used
   *
   * @param name of the prefix
   * @return true or false
   */
  public boolean existsByName(String name) {
    return find("name", name).count() >= 1L;
  }

  /**
   * Return the prefix for the given name
   *
   * @param name of the prefix
   * @return true or false
   */
  public Prefix findByName(String name) {
    return find("name", name).singleResult();
  }

  /**
   * Retrieves a page of Prefixes submitted by the specified user.
   *
   * @param page The index of the page to retrieve (starting from 0).
   * @param size The maximum number of assessments to include in a page.
   * @return A list of Prefixes objects representing the prefixes in the requested page.
   */
  public PageQuery<Prefix> fetchPrefixesByPage(int page, int size) {

    var panache = findAll().page(page, size);

    var pageable = new PageQueryImpl<Prefix>();
    pageable.list = panache.list();
    pageable.index = page;
    pageable.size = size;
    pageable.count = panache.count();
    pageable.page = Page.of(page, size);

    return pageable;
  }
}

package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Page;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.PageQueryImpl;
import gr.grnet.pccapi.entity.Prefix;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.StringJoiner;

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
  public PageQuery<Prefix> fetchPrefixesByPage(String search, String provider, String domain, String contractType, int page, int size) {

    var joiner = new StringJoiner(StringUtils.SPACE);
    joiner.add("from prefix p");

    var conditions = new StringJoiner(" AND ");
    var map = new HashMap<String, Object>();

    if (StringUtils.isNotEmpty(search)) {
      conditions.add(
              "(p.name ilike :search "
                      + "or p.owner ilike :search "
                      + "or p.usedBy ilike :search)");
      map.put("search", "%" + search + "%");
    }

    if (StringUtils.isNotEmpty(provider)) {
      conditions.add("p.provider.name ilike :provider");
      map.put("provider", provider);
    }

    if (StringUtils.isNotEmpty(domain)) {
      conditions.add("p.domain.name ilike :domain");
      map.put("domain", domain);
    }

    if (StringUtils.isNotEmpty(contractType)) {
      conditions.add("p.contractType.name ilike :contractType");
      map.put("contractType", contractType);
    }

    if (conditions.length() > 0) {
      joiner.add("WHERE");
      joiner.add(conditions.toString());
    }

    joiner.add("order by p.name ASC");

    var panache = find(joiner.toString(), map).page(page, size);

    var pageable = new PageQueryImpl<Prefix>();
    pageable.list = panache.list();
    pageable.index = page;
    pageable.size = size;
    pageable.count = panache.count();
    pageable.page = Page.of(page, size);

    return pageable;
  }
}

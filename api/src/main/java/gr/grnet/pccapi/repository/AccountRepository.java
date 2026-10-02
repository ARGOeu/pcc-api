package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Account;
import gr.grnet.pccapi.entity.Page;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.PageQueryImpl;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@ApplicationScoped
public class AccountRepository implements Repository<Account, String> {

    public List<Account> findByPrefixId(Integer prefixId) {
        return find("prefix.id = ?1", prefixId).list();
    }

    public Optional<Account> findByIdAndPrefixId(String accountId, Integer prefixId) {
        return find("id = ?1 and prefix.id = ?2", accountId, prefixId)
                .firstResultOptional();
    }

    public PageQuery<Account> fetchAccountsByPrefixIdAndPage(Integer prefixId, String search, int page, int size) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from Account a");

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        conditions.add("a.prefix.id = :prefixId");
        map.put("prefixId", prefixId);

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(a.email ilike :search " +
                            "or a.endpoint ilike :search)"
            );
            map.put("search", "%" + search + "%");
        }

        joiner.add("WHERE");
        joiner.add(conditions.toString());
        joiner.add("order by a.email ASC");

        var panache = find(joiner.toString(), map).page(page, size);

        var pageable = new PageQueryImpl<Account>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
}
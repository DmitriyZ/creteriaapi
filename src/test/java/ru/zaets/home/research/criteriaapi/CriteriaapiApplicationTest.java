package ru.zaets.home.research.criteriaapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.zaets.home.research.criteriaapi.entity.Cart;
import ru.zaets.home.research.criteriaapi.entity.Cart_;
import ru.zaets.home.research.criteriaapi.entity.Item;
import ru.zaets.home.research.criteriaapi.repository.CartRepository;
import ru.zaets.home.research.criteriaapi.repository.ItemRepository;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
@PropertySource("classpath*:application.properties")
public class CriteriaapiApplicationTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    CartRepository poiRepository;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    private static Specification<Item> excludeItemsInCarts() {
        return (Specification<Item>) (root, query, cb) -> {

            Subquery<Item> subquery = query.subquery(Item.class);
            Root<Cart> subfrom = subquery.from(Cart.class);
            ListJoin<Cart, Item> setJoin = subfrom.join(Cart_.items);
            subquery.select(setJoin);

            return cb.not(root.in(subquery));
        };
    }

    @Test
    public void contextLoads() {
        itemRepository.save(Item.builder().name("one").build());
        itemRepository.save(Item.builder().name("two").build());

        final Item item1 = Item.builder().name("item in cart 1").build();
        final Item item2 = Item.builder().name("item in cart 2").build();

        final List<Item> items = Arrays.asList(item1, item2);
        itemRepository.saveAll(items);

        final Cart cart = Cart.builder().name("cart").items(items).build();
        poiRepository.save(cart);

        System.out.println(itemRepository.findAll());

        System.out.println();
        System.out.println();
        System.out.println();

        final List<Item> all = itemRepository.findAll(excludeItemsInCarts());
        System.out.println();
        System.out.println();
        all.forEach(System.out::println);

    }


}

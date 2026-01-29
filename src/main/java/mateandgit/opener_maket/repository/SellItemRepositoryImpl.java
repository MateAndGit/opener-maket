package mateandgit.opener_maket.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import mateandgit.opener_maket.domain.SellItem;

import java.util.Collections;
import java.util.List;

import static mateandgit.opener_maket.domain.QItem.item;
import static mateandgit.opener_maket.domain.QSellItem.sellItem;

@RequiredArgsConstructor
public class SellItemRepositoryImpl implements SellItemRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SellItem> searchProducts(String keyword, String sortType) {

        if (keyword == null || keyword.length() < 2){
            return Collections.emptyList();
        }

        return queryFactory
                .selectFrom(sellItem)
                .join(sellItem.item, item).fetchJoin()
                .where(item.name.contains(keyword))
                .orderBy(getSortOrder(sortType))
                .limit(20)
                .fetch();
    }

    private OrderSpecifier<?> getSortOrder(String sortType) {
        if (sortType == null) return sellItem.id.desc();

        return switch (sortType) {
            case "sales" -> sellItem.totalSales.desc();     // Most purchases
            case "rating" -> sellItem.averageRating.desc(); // Highest rating
            case "price_asc" -> sellItem.price.asc();       // Lowest price
            case "price_desc" -> sellItem.price.desc();     // Highest price
            default -> sellItem.id.desc();                  // Newest
        };
    }
}

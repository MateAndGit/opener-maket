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
            case "sales" -> sellItem.totalSales.desc();     // 구매 많은 순
            case "rating" -> sellItem.averageRating.desc(); // 별점 높은 순
            case "price_asc" -> sellItem.price.asc();       // 가격 낮은 순
            case "price_desc" -> sellItem.price.desc();     // 가격 높은 순
            default -> sellItem.id.desc();                  // 최신 순
        };
    }
}

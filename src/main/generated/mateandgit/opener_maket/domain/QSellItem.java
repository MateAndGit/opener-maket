package mateandgit.opener_maket.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSellItem is a Querydsl query type for SellItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSellItem extends EntityPathBase<SellItem> {

    private static final long serialVersionUID = 1416080749L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSellItem sellItem = new QSellItem("sellItem");

    public final NumberPath<Double> averageRating = createNumber("averageRating", Double.class);

    public final EnumPath<mateandgit.opener_maket.domain.status.DealStatus> dealStatus = createEnum("dealStatus", mateandgit.opener_maket.domain.status.DealStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QItem item;

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final NumberPath<Integer> stockQuantity = createNumber("stockQuantity", Integer.class);

    public final NumberPath<Integer> totalRatingCount = createNumber("totalRatingCount", Integer.class);

    public final NumberPath<Integer> totalSales = createNumber("totalSales", Integer.class);

    public final QUser user;

    public QSellItem(String variable) {
        this(SellItem.class, forVariable(variable), INITS);
    }

    public QSellItem(Path<? extends SellItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSellItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSellItem(PathMetadata metadata, PathInits inits) {
        this(SellItem.class, metadata, inits);
    }

    public QSellItem(Class<? extends SellItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new QItem(forProperty("item"), inits.get("item")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}


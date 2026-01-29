package mateandgit.opener_maket.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoupon is a Querydsl query type for Coupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoupon extends EntityPathBase<Coupon> {

    private static final long serialVersionUID = -1439262194L;

    public static final QCoupon coupon = new QCoupon("coupon");

    public final NumberPath<Integer> currentUsageCount = createNumber("currentUsageCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isStackable = createBoolean("isStackable");

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> totalQuantity = createNumber("totalQuantity", Integer.class);

    public final EnumPath<mateandgit.opener_maket.domain.status.CouponType> type = createEnum("type", mateandgit.opener_maket.domain.status.CouponType.class);

    public QCoupon(String variable) {
        super(Coupon.class, forVariable(variable));
    }

    public QCoupon(Path<? extends Coupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoupon(PathMetadata metadata) {
        super(Coupon.class, metadata);
    }

}


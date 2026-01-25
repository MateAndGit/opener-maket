package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;
import mateandgit.opener_maket.dto.AddItemRequest;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public static Item createItem(AddItemRequest request, Category category) {
        return Item.builder()
                .name(request.itemName())
                .description(request.description())
                .category(category)
                .build();
    }

    public void setCategory(Category category) {
        this.category = category;
        category.getItems().add(this);
    }
}

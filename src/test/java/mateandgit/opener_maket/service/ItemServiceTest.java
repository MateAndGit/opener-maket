package mateandgit.opener_maket.service;

import mateandgit.opener_maket.domain.Category;
import mateandgit.opener_maket.domain.SellItem;
import mateandgit.opener_maket.dto.AddItemRequest;
import mateandgit.opener_maket.dto.SingUpRequest;
import mateandgit.opener_maket.repository.CategoryRepository;
import mateandgit.opener_maket.repository.ItemRepository;
import mateandgit.opener_maket.repository.SellItemRepository;
import mateandgit.opener_maket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static mateandgit.opener_maket.domain.status.CategoryStatus.BOOK;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SellItemRepository sellItemRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserService userService;

    @BeforeEach
    void setUp() {

        Category category = Category.builder()
                .categoryStatus(BOOK)
                .build();

        categoryRepository.save(category);

        SingUpRequest request = new SingUpRequest("test@test.com", "password1234");
        userService.join(request);
    }

    @Test
    @DisplayName("Item registration success")
    void shouldRegisterItem() {

        // given
        AddItemRequest request = new AddItemRequest(
                "test@test.com",
                BOOK,
                "Harry Potter",
                "Harry Potter and the Sorcerer's Stone version.",
                new BigDecimal("2000"),
                50

        );

        // when
        Long sellItemId = itemService.addItem(request);

        // then
        SellItem sellItem = sellItemRepository.findById(sellItemId).get();
//        assertThat(sellItem.getUser().getEmail()).isEqualTo("test@test.com");
//        assertThat(sellItem.getItem().getCategory().getCategoryStatus()).isEqualTo(BOOK);
//        assertThat(sellItem.getItem().getName()).isEqualTo("Harry Potter");
//        assertThat(sellItem.getItem().getDescription()).isEqualTo("Harry Potter and the Sorcerer's Stone version.");
//        assertThat(sellItem.getPrice()).isEqualTo(1000);
//        assertThat(sellItem.getStockQuantity()).isEqualTo(50);

        assertThat(sellItem)
                .extracting("price", "stockQuantity")
                .containsExactly(new BigDecimal("2000"), 50);

        assertThat(sellItem.getItem())
                .extracting("name", "description")
                .containsExactly("Harry Potter", "Harry Potter and the Sorcerer's Stone version.");
    }

}
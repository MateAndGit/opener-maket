package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import mateandgit.opener_maket.domain.Category;
import mateandgit.opener_maket.domain.Item;
import mateandgit.opener_maket.domain.SellItem;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.dto.AddItemRequest;
import mateandgit.opener_maket.dto.ItemResponse;
import mateandgit.opener_maket.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final SellItemRepository sellItemRepository;
    private final SellItemRepositoryCustom sellItemRepositoryCustom;
    private final CategoryRepository categoryRepository;

    public Long addItem(AddItemRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Category category = categoryRepository.findByCategoryStatus(request.category())
                .orElseThrow(() -> new IllegalArgumentException("category not found"));

        Item item = Item.createItem(request, category);
        item.setCategory(category);
        itemRepository.save(item);

        SellItem sellItem = SellItem.createSellItem(request , user, item);
        sellItem.setUser(user);
        sellItemRepository.save(sellItem);

        return sellItem.getId();
    }

    public List<ItemResponse> searchItems(String keyword, String sortBy) {
        if (keyword == null || keyword.trim().length() < 2) {
            throw new IllegalArgumentException("검색어는 2글자 이상이어야 합니다.");
        }

        List<SellItem> results = sellItemRepositoryCustom.searchProducts(keyword, sortBy);

        return results.stream()
                .map(ItemResponse::from)
                .toList();
    }
}

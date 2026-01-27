package mateandgit.opener_maket.repository;

import mateandgit.opener_maket.domain.SellItem;

import java.util.List;

public interface SellItemRepositoryCustom {
    List<SellItem> searchProducts(String keyword, String sortType);
}

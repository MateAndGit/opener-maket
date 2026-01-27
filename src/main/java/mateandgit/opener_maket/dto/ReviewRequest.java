package mateandgit.opener_maket.dto;

public  record ReviewRequest(
        String buyerEmail,
        Long orderId,
        Long sellItemId,
        int rating
) {}
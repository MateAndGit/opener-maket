package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import mateandgit.opener_maket.domain.Order;
import mateandgit.opener_maket.domain.OrderItem;
import mateandgit.opener_maket.domain.SellItem;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.domain.status.OrderStatus;
import mateandgit.opener_maket.dto.ReviewRequest;
import mateandgit.opener_maket.dto.SingUpRequest;
import mateandgit.opener_maket.repository.OrderItemRepository;
import mateandgit.opener_maket.repository.OrderRepository;
import mateandgit.opener_maket.repository.SellItemRepository;
import mateandgit.opener_maket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static mateandgit.opener_maket.domain.status.OrderStatus.PAYMENT_COMPLETED;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SellItemRepository sellItemRepository;
    
    public Long join(SingUpRequest request) {

        validateUserExist(request);

        User user = User.createUser(request);
        userRepository.save(user);

        return user.getId();
        
    }

    public List<User> findUsers() {
        return userRepository.findAll();
    }

    private void validateUserExist(SingUpRequest request) {
        userRepository.findByEmail(request.email())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("user already exist");
                });
    }

    private void createReview(ReviewRequest request) {

        User buyer = userRepository.findByEmail(request.buyerEmail())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("order not found"));

        if (!order.getUser().getEmail().equals(request.buyerEmail()) ||
                order.getStatus() != OrderStatus.PAYMENT_COMPLETED) {
            throw new IllegalStateException("review cannot be created");
        }

        OrderItem targetItem = order.getOrderItems().stream()
                .filter(oi -> oi.getSellItem().getItem().getId().equals(request.sellItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("order item not found"));

        SellItem sellItem = targetItem.getSellItem();

        sellItem.applyNewRating(request.rating());
    }
}

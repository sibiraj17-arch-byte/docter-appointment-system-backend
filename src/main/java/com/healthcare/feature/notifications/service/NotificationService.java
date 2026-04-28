package com.healthcare.feature.notifications.service;

import com.healthcare.entity.Notification;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.notifications.dto.NotificationDTO;
import com.healthcare.feature.notifications.repository.NotificationRepository;
import com.healthcare.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal.getUserId();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setUserId(n.getUser().getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }

    public List<NotificationDTO> getMyNotifications() {
        Long userId = getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications() {
        Long userId = getCurrentUserId();
        return notificationRepository.findByUserIdAndIsReadFalse(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public long getUnreadCount() {
        Long userId = getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationDTO markAsRead(Long id) {
        Long userId = getCurrentUserId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (!notification.getUser().getId().equals(userId)) {
            throw new com.healthcare.exception.UnauthorizedException("Cannot modify another user's notifications");
        }
        if (Boolean.FALSE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        return toDTO(notification);
    }

    @Transactional
    public String markAllAsRead() {
        Long userId = getCurrentUserId();
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
        return unread.isEmpty() ? "No unread notifications" : "Marked all notifications as read";
    }
}

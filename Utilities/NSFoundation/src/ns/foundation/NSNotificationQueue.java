package ns.foundation;

import java.util.EnumSet;
import java.util.Set;


public class NSNotificationQueue extends NSObject {
  
  public enum NSPostingStyle {
    WhenIdle,
    ASAP,
    Now
  }
  
  public enum NSNotificationCoalescing {
    OnName,
    OnSender
  }

  private static final NSNotificationQueue _defaultQueue = new NSNotificationQueue();

  private transient final NSNotificationCenter _center;
  private transient final NSMutableArray<NSNotificationQueueEntry> _asapQueue = new NSMutableArray<NSNotificationQueueEntry>();
  private transient final NSMutableArray<NSNotificationQueueEntry> _idleQueue = new NSMutableArray<NSNotificationQueueEntry>();
  
  public static NSNotificationQueue defaultQueue() {
    return _defaultQueue;
  }

  public NSNotificationQueue() {
    this(NSNotificationCenter.defaultCenter());
  }
  
  private class NSNotificationQueueEntry {
    private final NSArray<String> _modes;
    private final NSNotification _notification;
    
    NSNotificationQueueEntry(NSNotification notification, NSArray<String> modes) {
      _notification = notification;
      _modes = modes;
    }
    
    public NSNotification notification() {
      return _notification;
    }
    
    public NSArray<String> modes() {
      return _modes;
    }
  }

  public void asapProcessMode(String mode) {
    boolean hasNotifications = false;
    
    do {
      int count = _asapQueue.count();
      
      hasNotifications = false;
      for (int i = 0; i < count && !hasNotifications; i++) {
        NSNotificationQueueEntry qd = _asapQueue.objectAtIndex(i);
        NSArray<String> modes = qd.modes();
        
        if (modes == null || modes.containsObject(mode)) {
          _asapQueue.removeObjectAtIndex(i);
          
          hasNotifications = true;
          _center.postNotification(qd.notification());
        }
      }
    } while (hasNotifications);
  }
  
  public boolean hasIdleNotificationsInMode(String mode) {
    int count = _idleQueue.count();
    
    while (--count >= 0) {
      NSNotificationQueueEntry check = _idleQueue.objectAtIndex(count);
      NSArray<String> modes = check.modes();
      if (modes == null || modes.containsObject(mode)) {
        return true;
      }
    }
    return false;
  }
  
  public void idleProcessMode(String mode) {
    NSMutableArray<NSNotificationQueueEntry> idle = new NSMutableArray<NSNotificationQueueEntry>();
    int count = _idleQueue.count();
    
    while (--count >= 0) {
      NSNotificationQueueEntry check = _idleQueue.objectAtIndex(count);
      NSArray<String> modes = check.modes();
      
      if (modes == null || modes.containsObject(mode)) {
        idle.addObject(check);
        _idleQueue.removeObjectAtIndex(count);
      }
    }
    
    count = idle.count();
    while (--count >= 0) {
      NSNotificationQueueEntry check = idle.objectAtIndex(count);
      
      _center.postNotification(check.notification());
    }
  }
  
  protected void coalesceNotificationInQueueWithCoalesceMask(NSNotification notification, NSMutableArray<NSNotificationQueueEntry> queue, Set<NSNotificationCoalescing> coalesceMask) {
    if (coalesceMask != null && !coalesceMask.isEmpty()) {
      int count = queue.count();
      
      while(--count >= 0) {
        NSNotification check = queue.objectAtIndex(count).notification();
        boolean remove = false;
        
        if (coalesceMask.contains(NSNotificationCoalescing.OnName)) {
          if (check.name().equals(notification.name())) {
            remove = true;
          }
        }
        
        if (coalesceMask.contains(NSNotificationCoalescing.OnSender)) {
          if (check.object() == notification.object()) {
            remove = true;
          }
        }
        
        if (remove) {
          queue.removeObjectAtIndex(count);
        }
      }
    }
  }
  
  protected boolean canPlaceNotificationInQueueWithCoalesceMask(NSNotification notification, NSMutableArray<NSNotificationQueueEntry> queue, Set<NSNotificationCoalescing> coalesceMask) {
    if (coalesceMask != null && !coalesceMask.isEmpty()) {
      int count = queue.count();
      for (int i = 0; i < count; i++) {
        NSNotification check = queue.objectAtIndex(i).notification();
        
        if (coalesceMask.contains(NSNotificationCoalescing.OnName)) {
          if (check.name().equals(notification.name())) {
            return false;
          }
        }
        
        if (coalesceMask.contains(NSNotificationCoalescing.OnSender)) {
          if (check.name().equals(notification.object())) {
            return false;
          }
        }
      }
    }
    
    return true;
  }
  
  public NSNotificationQueue(NSNotificationCenter notificationCenter) {
    _center = notificationCenter;
  }

  public void enqueueNotification(NSNotification notification, NSPostingStyle postingStyle) {
    enqueueNotificationWithCoalesceMaskForModes(notification, postingStyle, EnumSet.of(NSNotificationCoalescing.OnName, NSNotificationCoalescing.OnSender), null);
  }

  public void enqueueNotificationWithCoalesceMaskForModes(NSNotification notification, NSPostingStyle postingStyle, Set<NSNotificationCoalescing> coalesceMask, NSArray<String> modes) {
    if (postingStyle == null) {
      throw new IllegalArgumentException("postingStyle cannot be null");
    }
    if (postingStyle == NSPostingStyle.Now) {
      _center.postNotification(notification);
    } else {
      NSMutableArray<NSNotificationQueueEntry> queue = null;
      
      if (postingStyle == NSPostingStyle.WhenIdle) {
        queue = _idleQueue;
      } else {
        queue = _asapQueue;
      }
      
      coalesceNotificationInQueueWithCoalesceMask(notification, queue, coalesceMask);
      if (canPlaceNotificationInQueueWithCoalesceMask(notification, queue, coalesceMask)) {
        queue.addObject(new NSNotificationQueueEntry(notification, modes));
      }
    }
  }

  public void dequeueMatchingNotifications(NSNotification notification, Set<NSNotificationCoalescing> coalesceMask) {
      dequeueNotificationInQueueWithCoalesceMask(notification, _idleQueue, coalesceMask);
      dequeueNotificationInQueueWithCoalesceMask(notification, _asapQueue, coalesceMask);
  }

  protected void dequeueNotificationInQueueWithCoalesceMask(NSNotification notification, NSMutableArray<NSNotificationQueueEntry> queue, Set<NSNotificationCoalescing> coalesceMask) {
    int count = queue.count();

    while(--count >= 0) {
      NSNotification check = queue.objectAtIndex(count).notification();
      boolean remove = false;

      if (coalesceMask == null || coalesceMask.isEmpty()) {
        if (notification.equals(check)) {
          remove = true;
        }
      } else { 
        if (coalesceMask.contains(NSNotificationCoalescing.OnName)) {
          if (check.name().equals(notification.name())) {
            remove = true;
          }
        }

        if (coalesceMask.contains(NSNotificationCoalescing.OnSender)) {
          if (check.object() == notification.object()) {
            remove = true;
          }
        }
      }
      if (remove) {
        queue.removeObjectAtIndex(count);
      }
    }
  }
  
}
package com.skkil.sync.notification.model;

import com.skkil.sync.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "notification_preferences")
@Getter
public class NotificationPreference {

  @Id private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @MapsId
  private User user;

  @Column(name = "in_app_enabled")
  private boolean inAppEnabled = true;

  public void setUser(User user) {
    this.user = user;
  }

  public void updateInAppEnabled(boolean inAppEnabled) {
    this.inAppEnabled = inAppEnabled;
  }
}

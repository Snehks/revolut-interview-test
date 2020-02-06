package com.revolut.interview.persistence;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
@EntityListeners(BaseEntity.ChangeListener.class)
public abstract class BaseEntity {

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private void onCreate() {
        setUpdated(setCreated(LocalDateTime.now(Clock.systemUTC())));
    }

    private void onUpdate() {
        setCreated(LocalDateTime.now(Clock.systemUTC()));
    }

    public LocalDateTime getCreated() {
        return created;
    }

    private LocalDateTime setCreated(LocalDateTime created) {
        this.created = created;
        return created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    private void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public static class ChangeListener {

        @PrePersist
        public void setLastCreate(Object entity) {
            if (entity instanceof BaseEntity) {
                ((BaseEntity) entity).onCreate();
            }
        }

        @PreUpdate
        public void setLastUpdated(Object entity) {
            if (entity instanceof BaseEntity) {
                ((BaseEntity) entity).onUpdate();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity entity = (BaseEntity) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

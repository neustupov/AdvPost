package ru.neustupov.advpost.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisingPost extends AbstractEntity {

    @NotNull
    private String text;
    @NotNull
    private String token;
    @NotNull
    private LocalDate periodFrom;
    @NotNull
    private LocalDate periodTo;
    @NotNull
    private LocalTime time;
    private boolean comments;
    private Long repostId;
    @OneToMany(fetch = FetchType.EAGER)
    private List<AdvertisingPhoto> photos;

    public AdvertisingPost(AdvertisingPostDTO dto) {
        this.text = dto.getText();
        this.token = dto.getToken();
        this.periodFrom = dto.getPeriodFrom();
        this.periodTo = dto.getPeriodTo();
        this.time = dto.getTime();
        Optional.of(dto.isComments()).ifPresent(c -> this.comments = c);
        Optional.of(dto.getRepostId()).ifPresent(r -> this.repostId = r);
        Optional.of(dto.isComments()).ifPresent(c -> this.comments = c);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(LocalDate periodFrom) {
        this.periodFrom = periodFrom;
    }

    public LocalDate getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(LocalDate periodTo) {
        this.periodTo = periodTo;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public boolean isComments() {
        return comments;
    }

    public void setComments(boolean comments) {
        this.comments = comments;
    }

    public Long getRepostId() {
        return repostId;
    }

    public void setRepostId(Long repostId) {
        this.repostId = repostId;
    }

    public List<AdvertisingPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<AdvertisingPhoto> photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        return "AdvertisingPost{" +
                "text='" + text + '\'' +
                ", token='" + token + '\'' +
                ", periodFrom=" + periodFrom +
                ", periodTo=" + periodTo +
                ", time=" + time +
                ", comments=" + comments +
                ", repostId=" + repostId +
                ", photos=" + photos +
                ", id=" + id +
                '}';
    }
}

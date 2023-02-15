package br.com.youtube.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SchedulerLog extends BaseEntity {

    @Column(name = "execution_start", nullable = false)
    private Date executionStart;

    @Column(name = "execution_end", nullable = true)
    private Date executionEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private Status status;

    @Column(name = "log", columnDefinition = "TEXT")
    private String log;

    public enum Type {
        PLAYLIST
    }

    public enum Status {
        SUCCESS, ERROR;
    }

}

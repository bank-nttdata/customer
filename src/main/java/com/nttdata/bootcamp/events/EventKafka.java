package com.nttdata.bootcamp.events;

import com.nttdata.bootcamp.entity.enums.EventType;
import lombok.Data;
import java.util.Date;

@Data
public abstract class EventKafka<T> {
    private String id;
    private Date date;
    private EventType type;
    private T data;
}

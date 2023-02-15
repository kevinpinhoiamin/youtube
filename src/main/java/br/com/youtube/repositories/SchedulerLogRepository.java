package br.com.youtube.repositories;

import javax.enterprise.context.ApplicationScoped;

import br.com.youtube.entities.SchedulerLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class SchedulerLogRepository implements PanacheRepository<SchedulerLog> {

}

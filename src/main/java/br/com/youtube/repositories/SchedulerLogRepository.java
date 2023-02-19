package br.com.youtube.repositories;

import javax.enterprise.context.ApplicationScoped;

import br.com.youtube.entities.SchedulerLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class SchedulerLogRepository implements PanacheRepository<SchedulerLog> {

    public SchedulerLog findLastByType(SchedulerLog.Type type) {
        return find("type", Sort.by("id", Sort.Direction.Descending), type)
                .firstResult();
    }

}

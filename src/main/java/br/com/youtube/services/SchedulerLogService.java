package br.com.youtube.services;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import br.com.youtube.entities.SchedulerLog;
import br.com.youtube.repositories.SchedulerLogRepository;

@ApplicationScoped
public class SchedulerLogService {

    @Inject
    SchedulerLogRepository repository;

    public SchedulerLog findLastByType(SchedulerLog.Type type) {
        return this.repository.findLastByType(type);
    }

    @Transactional
    public SchedulerLog logStart(SchedulerLog.Type type) {
        SchedulerLog schedulerLog = new SchedulerLog();
        schedulerLog.setExecutionStart(new Date());
        schedulerLog.setType(type);
        this.repository.persist(schedulerLog);

        return schedulerLog;
    }

    @Transactional
    public void logEnd(Long id, SchedulerLog.Status status) {
        this.logEnd(id, status, null);
    }

    @Transactional
    public void logEnd(Long id, SchedulerLog.Status status, String log) {
        SchedulerLog schedulerLog = this.repository.findById(id);
        if (schedulerLog == null) {
            return;
        }

        schedulerLog.setExecutionEnd(new Date());
        schedulerLog.setStatus(status);
        schedulerLog.setLog(log);
        this.repository.persist(schedulerLog);
    }

}

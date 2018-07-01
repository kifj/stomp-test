package x1.stomp.test;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import x1.stomp.control.QuoteUpdater;

@Singleton
@Startup
public class TimerUtil {
  @Resource
  private TimerService timerService;

  @Inject
  private Logger log;
  
  @PostConstruct
  public void setup() {
    timerService.getAllTimers().forEach(timer -> {
      if (timer.isPersistent() && timer.getInfo().equals(QuoteUpdater.INFO_TEXT)) {
        timer.cancel();
        log.info("canceled {}", timer);
      }
    });
  }
}

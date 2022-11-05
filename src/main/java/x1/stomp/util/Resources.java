package x1.stomp.util;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resources {

  @Produces
  @PersistenceContext
  private EntityManager em;

  @Produces
  public Logger produceLog(InjectionPoint injectionPoint) {
    return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
  }

  @StockMarket
  @Resource(name = "java:/jms/queue/stocks")
  private Queue stockMarketQueue;

  @Produces
  @StockMarket
  public Queue getStockMarketQueue() {
    return stockMarketQueue;
  }

  @StockMarket
  @Resource(name = "java:/jms/topic/quotes")
  private Topic quoteTopic;

  @Produces
  @StockMarket
  public Topic getQuoteTopic() {
    return quoteTopic;
  }
}

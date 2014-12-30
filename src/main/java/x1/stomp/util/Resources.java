package x1.stomp.util;

import javax.annotation.Resource;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

  @Produces
  @Resource(mappedName = "java:/ConnectionFactory")
  private ConnectionFactory connectionFactory;

  @StockMarket
  @Resource(mappedName = "queue/stocks")
  private Queue stockMarketQueue;

  @Produces
  @StockMarket
  public Queue getStockMarketQueue() {
    return stockMarketQueue;
  }

  @StockMarket
  @Resource(mappedName = "topic/quotes")
  private Topic quoteTopic;

  @Produces
  @StockMarket
  public Topic getQuoteTopic() {
    return quoteTopic;
  }

  @Produces
  @StockMarket
  public Connection createConnection() throws JMSException {
    return connectionFactory.createConnection();
  }

  public void closeConnection(@Disposes @StockMarket Connection connection) throws JMSException {
    connection.close();
  }

}

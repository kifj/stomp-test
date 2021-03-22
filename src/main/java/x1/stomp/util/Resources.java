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
  @Resource(lookup = "java:/JmsXA")
  private ConnectionFactory connectionFactory;

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

  @Produces
  @StockMarket
  public Connection createConnection() throws JMSException {
    return connectionFactory.createConnection();
  }

  public void closeConnection(@Disposes @StockMarket Connection connection) throws JMSException {
    connection.close();
  }

}

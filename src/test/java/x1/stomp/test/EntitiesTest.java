package x1.stomp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import x1.stomp.model.Share;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Testcontainers")
@DisplayName("Entities")
public class EntitiesTest {
  @Container
  private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  private static EntityManagerFactory emf;
  private EntityManager em;

  @BeforeAll
  void setupEntityManagerFactory() {
    System.setProperty("jdbc.url", postgres.getJdbcUrl());
    System.setProperty("jdbc.username", postgres.getUsername());
    System.setProperty("jdbc.password", postgres.getPassword());
    emf = Persistence.createEntityManagerFactory("testcontainers");
  }

  @BeforeEach
  void setup() {
    em = emf.createEntityManager();
  }

  @AfterEach
  void teardown() {
    em.close();
  }

  @AfterAll
  void teardownEntityManagerFactory() {
    if (emf != null) {
      emf.close();
    }
  }

  @Test
  void insertShare() {
    var tx = em.getTransaction();

    tx.begin();
    var c1 = em.createNamedQuery(Share.COUNT_ALL, Long.class).getSingleResult();

    var s1 = new Share("TEST1");
    s1.setName("Test Company 1");
    em.persist(s1);
    assertNotNull(s1.getVersion());
    tx.commit();

    tx.begin();
    var c2 = em.createNamedQuery(Share.COUNT_ALL, Long.class).getSingleResult();
    assertEquals(c1 + 1, c2);

    var s2 = em.find(Share.class, s1.getId());
    assertNotNull(s2);
    assertEquals(s1.getKey(), s2.getKey());

    var s3 = em.createNamedQuery(Share.FIND_BY_KEY, Share.class).setParameter("key", s1.getKey()).getSingleResult();
    assertNotNull(s3);
    assertEquals(s1.getKey(), s3.getKey());

    var l1 = em.createNamedQuery(Share.LIST_ALL, Share.class).setMaxResults(1).getResultList();
    assertEquals(1, l1.size());
    tx.commit();
  }

  @Test
  void updateShare() {
    var tx = em.getTransaction();

    tx.begin();
    var s1 = new Share("TEST3");
    s1.setName("Test Company 3");
    em.persist(s1);
    tx.commit();

    tx.begin();
    var s2 = em.find(Share.class, s1.getId());
    assertNotNull(s2);

    var v2 = s2.getVersion();

    s2.setName("Test Company");
    s2 = em.merge(s2);
    tx.commit();

    tx.begin();
    var s3 = em.find(Share.class, s1.getId());
    assertNotNull(s3);
    assertEquals(v2 + 1, s3.getVersion());
    tx.commit();
  }

  @Test
  void deleteShare() {
    var tx = em.getTransaction();

    tx.begin();
    var s1 = new Share("TEST2");
    s1.setName("Test Company 2");
    em.persist(s1);
    tx.commit();

    tx.begin();
    em.remove(s1);
    tx.commit();

    tx.begin();
    var s2 = em.find(Share.class, s1.getId());
    assertNull(s2);
  }

  @Test
  void insertShareInvalid() {
    var tx = em.getTransaction();

    tx.begin();
    var s1 = new Share("TEST4");
    s1.setName("");
    em.persist(s1);
    var ex = assertThrows(PersistenceException.class, () -> tx.commit());
    if (ex.getCause() instanceof ConstraintViolationException cvex) {
      var constraintViolations = cvex.getConstraintViolations();
      assertEquals(2, constraintViolations.size());
    } else {
      fail("Expected cause ConstraintViolationException, was " + ex.getCause());
    }
    tx.rollback();
  }
}

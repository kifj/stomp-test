package x1.stomp.test;

import java.util.EnumSet;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.junit.jupiter.api.Test;

import x1.stomp.model.Share;

public class SchemaExportTest {
  @Test
  public void testSchemaExport() {
    MetadataSources metadata = new MetadataSources(
        new StandardServiceRegistryBuilder().applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
            .applySetting(AvailableSettings.DEFAULT_SCHEMA, "stocks").build());
    new SchemaExport().setOutputFile("target/generated/ddl.sql").setFormat(true).create(EnumSet.of(TargetType.SCRIPT),
        metadata.addAnnotatedClass(Share.class).buildMetadata());
  }
}
